package fr.fastedit.edit;

import cn.nukkit.Server;
import cn.nukkit.block.BlockState;
import cn.nukkit.level.Level;
import cn.nukkit.level.generator.object.BlockManager;
import cn.nukkit.plugin.Plugin;
import fr.fastedit.math.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class EditEngine {

    public static final int BLOCKS_PER_TICK = 60_000;

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
                    queue.addLast(new PendingJob(session, onDone, undoSink));
                }
            } catch (Throwable t) {
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
        catch (Throwable t) { plugin.getLogger().error("[FastEdit] tick crashed: " + t.getMessage()); }
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

            BlockManager manager = new BlockManager(lvl);
            int written = 0;
            for (int i = from; i < to; i++) {
                BlockChange c = list.get(i);
                if (filter != null && !filter.test(lvl, c.pos)) { c.target = null; continue; }
                c.previous = lvl.getBlockStateAt(c.pos.x(), c.pos.y(), c.pos.z());
                manager.setBlockStateAt(c.pos.x(), c.pos.y(), c.pos.z(), c.target);
                written++;
            }
            if (written > 0) manager.applySubChunkUpdate();

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
