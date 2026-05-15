package fr.fastedit.edit;

import cn.nukkit.Server;
import cn.nukkit.block.BlockState;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import cn.nukkit.network.protocol.UpdateSubChunkBlocksPacket;
import cn.nukkit.network.protocol.types.BlockChangeEntry;
import cn.nukkit.plugin.Plugin;
import fr.fastedit.math.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class EditEngine {

    public static final int BLOCKS_PER_TICK = 20_000;
    public static final int MAX_EDIT_SIZE = 5_000_000;

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
                if (session.size() > MAX_EDIT_SIZE) {
                    Server.getInstance().getScheduler().scheduleTask(plugin, () -> {
                        if (onError != null) onError.accept(new IllegalStateException(
                            "edit too large (" + session.size() + " blocks, max " + MAX_EDIT_SIZE + ")"));
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
            s.plan(c.pos, target);
        }
        apply(s, null, onDone);
    }

    private void tick() {
        try { tickInner(); }
        catch (Throwable t) { plugin.getLogger().error("[FastEdit] tick crashed", t); }
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
            for (int i = from; i < to; i++) {
                BlockChange c = list.get(i);
                int x = c.pos.x(), y = c.pos.y(), z = c.pos.z();
                if (filter != null && !filter.test(lvl, c.pos)) { c.target = null; continue; }

                IChunk chunk = lvl.getChunk(x >> 4, z >> 4, true);
                if (chunk == null) { c.target = null; continue; }

                c.previous = chunk.getBlockState(x & 15, y, z & 15, 0);
                chunk.setBlockState(x & 15, y, z & 15, c.target, 0);
                written++;

                SubKey key = new SubKey(x >> 4, y >> 4, z >> 4);
                UpdateSubChunkBlocksPacket pkt = packets.computeIfAbsent(key,
                    k -> new UpdateSubChunkBlocksPacket(k.cx << 4, k.sy << 4, k.cz << 4));
                pkt.standardBlocks.add(new BlockChangeEntry(
                    new BlockVector3(x, y, z),
                    c.target.unsignedBlockStateHash(),
                    UpdateBlockPacket.FLAG_ALL,
                    -1,
                    BlockChangeEntry.MessageType.NONE));
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
                if (job.undoSink != null && job.applied > 0) {
                    List<BlockChange> kept = new ArrayList<>(job.applied);
                    for (BlockChange c : list) if (c.target != null) kept.add(c);
                    job.undoSink.push(new UndoBuffer.Entry(lvl.getName(), kept));
                }
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
        int cursor;
        int applied;

        PendingJob(EditSession s, Consumer<Integer> onDone, UndoBuffer undoSink) {
            this.session = s; this.onDone = onDone; this.undoSink = undoSink;
        }
    }
}
