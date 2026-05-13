package fr.fastedit.edit;

import cn.nukkit.Server;
import cn.nukkit.block.BlockState;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.Plugin;
import fr.fastedit.math.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Two-phase edit pipeline.
 *
 *   1. PLAN — runs on a virtual thread. The planner produces an EditSession
 *      filled with (pos, target) tuples. No world I/O happens here; just CPU.
 *
 *   2. APPLY — runs on the main thread, sliced. Every tick we drain at most
 *      BLOCKS_PER_TICK entries: for each one we read the current state (for
 *      undo), then write the target with setBlockStateAt — a raw write that
 *      skips physics/redstone updates, exactly what world-edit wants.
 *
 * The undo entry is pushed once the queue's last block has been applied, so
 * //undo replays a complete edit even if it spanned multiple ticks.
 */
public final class EditEngine {

    public static final int BLOCKS_PER_TICK = 8_000;

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
                synchronized (queue) {
                    queue.addLast(new PendingJob(session, onDone, onError, undoSink, 0));
                }
            } catch (Throwable t) {
                Server.getInstance().getScheduler().scheduleTask(plugin,
                    () -> { if (onError != null) onError.accept(t); });
            }
        });
    }

    public void apply(EditSession session, UndoBuffer undoSink, Consumer<Integer> onDone) {
        synchronized (queue) {
            queue.addLast(new PendingJob(session, onDone, null, undoSink, 0));
        }
    }

    private void tick() {
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

            int written = 0;
            for (int i = from; i < to; i++) {
                BlockChange c = list.get(i);
                if (filter != null && !filter.test(lvl, c.pos)) { c.target = null; continue; }
                c.previous = lvl.getBlockStateAt(c.pos.x(), c.pos.y(), c.pos.z());
                writeRaw(lvl, c.pos, c.target);
                written++;
            }

            budget -= (to - from);
            job.applied += written;
            job.cursor = to;

            if (job.cursor >= list.size()) {
                synchronized (queue) { queue.pollFirst(); }
                if (job.undoSink != null && job.applied > 0) {
                    List<BlockChange> kept = new java.util.ArrayList<>(job.applied);
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

    public static void writeRaw(Level level, Vec3 pos, BlockState state) {
        level.setBlockStateAt(pos.x(), pos.y(), pos.z(), state);
    }

    /**
     * Undo helper — replays a previously-recorded entry back into the world.
     * Runs on the main thread, sliced like a regular apply.
     */
    public void replay(Level level, List<BlockChange> changes, boolean forward, Consumer<Integer> onDone) {
        EditSession s = new EditSession(level, changes.size());
        for (BlockChange c : changes) {
            BlockState target = forward ? c.target : c.previous;
            if (target == null) continue;
            s.plan(c.pos, target);
        }
        apply(s, null, onDone);
    }

    private static final class PendingJob {
        final EditSession session;
        final Consumer<Integer> onDone;
        final Consumer<Throwable> onError;
        final UndoBuffer undoSink;
        int cursor;
        int applied;

        PendingJob(EditSession s, Consumer<Integer> onDone, Consumer<Throwable> onError,
                   UndoBuffer undoSink, int cursor) {
            this.session = s; this.onDone = onDone; this.onError = onError;
            this.undoSink = undoSink; this.cursor = cursor;
        }
    }
}
