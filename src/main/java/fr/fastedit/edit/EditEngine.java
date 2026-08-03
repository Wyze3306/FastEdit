package fr.fastedit.edit;

import org.cloudburstmc.protocol.bedrock.data.ActorBlockSyncMessageId;
import org.cloudburstmc.protocol.bedrock.data.BlockChangeEntry;
import org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateSubChunkBlocksPacket;
import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.block.BlockState;
import org.powernukkitx.level.Level;
import org.powernukkitx.level.format.IChunk;
import org.powernukkitx.math.BlockVector3;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.utils.RuntimeBlockDefinition;
import fr.fastedit.math.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class EditEngine {

    public static final int BLOCKS_PER_TICK = 40_000;

    /**
     * NEIGHBORS|NETWORK — what the old {@code UpdateBlockPacket.FLAG_ALL} int
     * meant. PNX 3 exposes the flags as an enum set, but the per-entry field in
     * {@link BlockChangeEntry} is still the raw protocol bitmask.
     */
    private static final int UPDATE_FLAGS_ALL =
        (1 << UpdateBlockPacket.Flag.NEIGHBORS.ordinal())
        | (1 << UpdateBlockPacket.Flag.NETWORK.ordinal());

    /** Blocks materialised per streaming segment (one undo step each). */
    public static final int SEGMENT_BLOCKS = 100_000;
    /** Max segments queued-but-not-applied → bounds peak memory under load. */
    public static final int MAX_PENDING_SEGMENTS = 8;

    /** Plans the next slice of a large edit; lets huge edits run in bounded memory. */
    public interface Segmenter {
        /** Plan up to {@code maxBlocks} into {@code s}. Return true if more remain. */
        boolean fill(EditSession s, int maxBlocks) throws Exception;
    }

    private static EditEngine INSTANCE;
    public static EditEngine get() { return INSTANCE; }

    public static void boot(Plugin plugin) {
        if (INSTANCE != null) return;
        INSTANCE = new EditEngine(plugin);
        plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, INSTANCE::tick, 1);
    }

    private final Plugin plugin;
    private final ExecutorService planners =
        Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("fastedit-plan-", 0).factory());
    private final Deque<PendingJob> queue = new ArrayDeque<>();

    private EditEngine(Plugin plugin) { this.plugin = plugin; }

    public void submit(Level level,
                       Consumer<EditSession> planner,
                       Consumer<Integer> onDone,
                       Consumer<Throwable> onError,
                       UndoBuffer undoSink) {
        EditSession session = new EditSession(level, 0);
        planners.execute(() -> {
            try {
                planner.accept(session);
                long cap = safeBlockCap();
                if (session.size() > cap) {
                    Server.getInstance().getScheduler().scheduleTask(plugin, () -> {
                        if (onError != null) onError.accept(new IllegalStateException(
                            "edit too large for free memory (" + session.size()
                            + " blocks, ~" + cap + " safe right now) — split it or //paste "
                            + "which streams big edits automatically"));
                    });
                    return;
                }
                synchronized (queue) {
                    queue.addLast(new PendingJob(session, onDone, undoSink));
                }
            } catch (Throwable t) {
                plugin.getLogger().error("[FastEdit] edit planning failed", t);
                Server.getInstance().getScheduler().scheduleTask(plugin,
                    () -> { if (onError != null) onError.accept(t); });
            }
        });
    }

    /**
     * Largest single non-streaming edit that comfortably fits in free heap right
     * now (~96 B/change incl. undo retention, using ≤35 % of free memory). Never
     * below 2M so normal selections always pass.
     */
    public static long safeBlockCap() {
        Runtime r = Runtime.getRuntime();
        long free = r.maxMemory() - (r.totalMemory() - r.freeMemory());
        long cap = (long) (free * 0.35 / 96);
        return Math.max(cap, 2_000_000L);
    }

    /**
     * Applies an edit of <em>any</em> size without OOM: the {@link Segmenter} is
     * pumped for {@link #SEGMENT_BLOCKS}-sized slices, each queued as its own job
     * (one undo step), with back-pressure so at most {@link #MAX_PENDING_SEGMENTS}
     * segments are in flight. Peak memory is bounded no matter the total volume.
     */
    public void submitStreaming(Level level, Segmenter seg,
                                Consumer<Long> onDone, Consumer<Throwable> onError,
                                UndoBuffer undoSink) {
        planners.execute(() -> {
            final AtomicInteger outstanding = new AtomicInteger();
            final AtomicBoolean failed = new AtomicBoolean();
            final long txn = UndoBuffer.nextTxn();
            final Set<Long> touched = ConcurrentHashMap.newKeySet();
            // Lazily created on the main thread by the first tick; shared by
            // every segment so the pins live until the single final finalize.
            final ChunkPin[] pin = new ChunkPin[1];
            long total = 0;
            try {
                boolean more = true;
                while (more) {
                    EditSession s = new EditSession(level, Math.min(SEGMENT_BLOCKS, 1 << 16));
                    more = seg.fill(s, SEGMENT_BLOCKS);
                    if (s.size() == 0) continue;
                    while (outstanding.get() >= MAX_PENDING_SEGMENTS) {
                        if (failed.get()) return;
                        Thread.sleep(4);
                    }
                    total += s.size();
                    outstanding.incrementAndGet();
                    Consumer<Integer> segDone = n -> outstanding.decrementAndGet();
                    synchronized (queue) {
                        queue.addLast(new PendingJob(s, segDone, undoSink, txn, touched, false, pin));
                    }
                }
                while (outstanding.get() > 0) Thread.sleep(8);
                final long applied = total;
                Server.getInstance().getScheduler().scheduleTask(plugin, () -> {
                    finalizeChunks(level, touched, pin[0]);  // persist+relight+resend once
                    if (onDone != null) onDone.accept(applied);
                });
            } catch (Throwable t) {
                failed.set(true);
                plugin.getLogger().error("[FastEdit] streaming edit failed", t);
                // Drain in-flight segments before unpinning, else the pins
                // lapse while they are still writing.
                try { while (outstanding.get() > 0) Thread.sleep(8); }
                catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                Server.getInstance().getScheduler().scheduleTask(plugin, () -> {
                    finalizeChunks(level, touched, pin[0]);  // still unpin what we touched
                    if (onError != null) onError.accept(t);
                });
            }
        });
    }

    public void apply(EditSession session, UndoBuffer undoSink, Consumer<Integer> onDone) {
        synchronized (queue) {
            queue.addLast(new PendingJob(session, onDone, undoSink));
        }
    }

    public void replay(Level level, List<BlockChange> changes, boolean forward, Consumer<Integer> onDone) {
        EditSession s = new EditSession(level, changes.size());
        for (BlockChange c : changes) {
            BlockState target = forward ? c.target : c.previous;
            if (target == null) continue;
            BlockState l1;
            if (forward) {
                l1 = c.layer1;
            } else if (c.layer1 != null) {
                // undo: restore the water we displaced, or clear it if none
                l1 = c.prevLayer1 != null ? c.prevLayer1 : fr.fastedit.block.Blocks.air();
            } else {
                l1 = null;
            }
            s.plan(c.pos, target, l1);
        }
        apply(s, null, onDone);
    }

    private void tick() {
        try { tickInner(); }
        catch (Throwable t) {
            // Never let a poison job sit at the head and re-crash every tick.
            PendingJob bad;
            synchronized (queue) { bad = queue.pollFirst(); }
            plugin.getLogger().error("[FastEdit] tick crashed — dropped 1 job", t);
            if (bad != null) {
                // Streamed segments share their pin; the stream's own finalize
                // still runs (segDone below) and unpins. Solo jobs unpin here.
                if (bad.ownsFinalize) {
                    try { finalizeChunks(bad.session.level(), bad.touched, bad.pin[0]); }
                    catch (Throwable ignored) {}
                }
                if (bad.onDone != null) {
                    try { bad.onDone.accept(bad.applied); } catch (Throwable ignored) {}
                }
            }
        }
    }

    private void tickInner() {
        int budget = BLOCKS_PER_TICK;
        while (budget > 0) {
            PendingJob job;
            synchronized (queue) { job = queue.peekFirst(); }
            if (job == null) return;

            List<BlockChange> list = job.session.changes();
            int from = job.cursor;
            int to   = Math.min(from + budget, list.size());
            Level lvl = job.session.level();
            var filter = job.session.filter();

            Map<SubKey, UpdateSubChunkBlocksPacket> packets = new HashMap<>();
            int written = 0;
            int lastCx = Integer.MIN_VALUE, lastCz = Integer.MIN_VALUE;
            for (int i = from; i < to; i++) {
                BlockChange c = list.get(i);
                int x = c.pos.x(), y = c.pos.y(), z = c.pos.z();
                if (filter != null && !filter.test(lvl, c.pos)) { c.target = null; continue; }
                // Out-of-world Y (clipboard pasted near build limits) must be
                // skipped — indexing a sub-chunk past its range threw AIOOBE,
                // crashed the tick, and the job span the queue forever.
                if (!lvl.isYInRange(y)) { c.target = null; continue; }

                try {
                    int cx = x >> 4, cz = z >> 4;
                    IChunk chunk = lvl.getChunk(cx, cz, true);
                    if (chunk == null) { c.target = null; continue; }

                    // Re-mark on every entry, not just the first: PNX's GC
                    // saves only hasChanged() chunks, so blocks written to a
                    // chunk evicted+reloaded by a later segment are otherwise
                    // dropped (whole bands of a big schematic vanished).
                    if (cx != lastCx || cz != lastCz) {
                        chunk.setChanged();
                        // Pin every touched chunk for the whole edit: PNX's GC
                        // serializes evicted chunks ASYNC then setChanged(false),
                        // racing our writes — a chunk evicted mid-paste and
                        // reloaded from disk came back with holes. Pinned
                        // chunks are "in use" and never enter that path.
                        if (job.touched.add(chunkKey(cx, cz))) {
                            if (job.pin[0] == null) job.pin[0] = new ChunkPin(lvl);
                            try { lvl.registerChunkLoader(job.pin[0], cx, cz, false); } catch (Throwable ignored) {}
                        }
                        lastCx = cx; lastCz = cz;
                    }

                    c.previous = chunk.getBlockState(x & 15, y, z & 15, 0);
                    chunk.setBlockState(x & 15, y, z & 15, c.target, 0);
                    if (c.layer1 != null) {
                        c.prevLayer1 = chunk.getBlockState(x & 15, y, z & 15, 1);
                        chunk.setBlockState(x & 15, y, z & 15, c.layer1, 1);
                    }
                    written++;

                    SubKey key = new SubKey(x >> 4, y >> 4, z >> 4);
                    UpdateSubChunkBlocksPacket pkt = packets.computeIfAbsent(key, k -> {
                        UpdateSubChunkBlocksPacket p = new UpdateSubChunkBlocksPacket();
                        p.setChunkX(k.cx << 4);
                        p.setChunkY(k.sy << 4);
                        p.setChunkZ(k.cz << 4);
                        return p;
                    });
                    pkt.getStandardBlocks().add(new BlockChangeEntry(
                        new BlockVector3(x, y, z).toNetwork(),
                        new RuntimeBlockDefinition((int) c.target.unsignedBlockStateHash()),
                        UPDATE_FLAGS_ALL,
                        -1,
                        ActorBlockSyncMessageId.NONE));
                } catch (Throwable perBlock) {
                    c.target = null; // drop this block, keep the edit going
                    job.dropped++;
                    if (job.firstDropCause == null) job.firstDropCause = perBlock;
                }
            }

            for (var entry : packets.entrySet()) {
                SubKey k = entry.getKey();
                lvl.addChunkPacket(k.cx, k.cz, entry.getValue());
            }

            budget -= (to - from);
            job.applied += written;
            job.cursor = to;

            if (job.cursor >= list.size()) {
                synchronized (queue) { queue.pollFirst(); }
                if (job.dropped > 0) {
                    // Per-block failures are recoverable but must not be
                    // invisible: silent drops looked like random paste holes.
                    plugin.getLogger().warning("[FastEdit] dropped " + job.dropped
                        + " block(s) in this job — first cause: " + job.firstDropCause);
                }
                if (job.undoSink != null && job.applied > 0) {
                    List<BlockChange> kept = new ArrayList<>(job.applied);
                    for (BlockChange c : list) if (c.target != null) kept.add(c);
                    job.undoSink.push(new UndoBuffer.Entry(lvl.getName(), kept, job.txn));
                }
                if (job.ownsFinalize) finalizeChunks(lvl, job.touched, job.pin[0]);
                if (job.onDone != null) {
                    try { job.onDone.accept(job.applied); }
                    catch (Throwable t) { plugin.getLogger().error("[FastEdit] onDone threw: " + t.getMessage()); }
                }
            }
        }
    }

    private record SubKey(int cx, int sy, int cz) {}

    private static class PendingJob {
        final EditSession session;
        final Consumer<Integer> onDone;
        final UndoBuffer undoSink;
        final long txn;
        /** Chunk keys touched by this job (shared across a streamed paste). */
        final Set<Long> touched;
        /** Whether THIS job finalises the chunks (false for streamed segments). */
        final boolean ownsFinalize;
        /** One-slot holder for the pin, lazily filled on the main thread. */
        final ChunkPin[] pin;
        int cursor;
        int applied;
        int dropped;
        Throwable firstDropCause;

        PendingJob(EditSession s, Consumer<Integer> onDone, UndoBuffer undoSink) {
            this(s, onDone, undoSink, UndoBuffer.nextTxn(), new HashSet<>(), true, new ChunkPin[1]);
        }

        PendingJob(EditSession s, Consumer<Integer> onDone, UndoBuffer undoSink,
                   long txn, Set<Long> touched, boolean ownsFinalize, ChunkPin[] pin) {
            this.session = s; this.onDone = onDone; this.undoSink = undoSink;
            this.txn = txn; this.touched = touched; this.ownsFinalize = ownsFinalize;
            this.pin = pin;
        }
    }

    private static long chunkKey(int cx, int cz) {
        return ((long) cx << 32) | (cz & 0xFFFFFFFFL);
    }

    /** Persist + relight + fully resend, then unpin, every touched chunk (main thread). */
    private void finalizeChunks(Level lvl, Set<Long> touched, ChunkPin pin) {
        for (long key : touched) {
            int cx = (int) (key >> 32);
            int cz = (int) key;
            // true: reload a chunk GC-evicted before finalize so it is still
            // relit and resent (skipping null left bands stale client-side).
            IChunk c = lvl.getChunk(cx, cz, true);
            if (c != null) {
                try {
                    c.setChanged();
                    c.recalculateHeightMap();
                } catch (Throwable ignored) {}
                for (Player p : lvl.getChunkPlayers(cx, cz).values()) {
                    try { lvl.requestChunk(cx, cz, p); } catch (Throwable ignored) {}
                }
            }
            // Unpin last: setChanged() above guarantees the eventual unload
            // takes the synchronous-save path with the complete chunk.
            if (pin != null) {
                try { lvl.unregisterChunkLoader(pin, cx, cz); } catch (Throwable ignored) {}
            }
        }
    }
}
