package fr.fastedit.edit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * History buffer keyed by <em>transaction</em>. A paste is streamed as many
 * internal segments but shares one transaction id, so a single {@code //undo}
 * reverts the whole structure at once. A global block budget caps retained
 * blocks: once exceeded the oldest transactions are evicted so the heap can
 * never blow up regardless of edit size.
 */
public final class UndoBuffer {

    public record Entry(String levelName, List<BlockChange> changes, long txn) {}

    /** ~96 B per retained change → ~16M blocks ≈ ~1.5 GB worst case ceiling. */
    public static final long MAX_TOTAL_BLOCKS = 16_000_000L;

    private static final AtomicLong TXN = new AtomicLong(1);
    public static long nextTxn() { return TXN.getAndIncrement(); }

    private final Deque<Entry> undo = new ArrayDeque<>();
    private final Deque<Entry> redo = new ArrayDeque<>();
    private long undoBlocks;
    private long redoBlocks;

    public UndoBuffer() {}
    public UndoBuffer(int ignoredCapacity) {}

    public void push(Entry e) {
        undo.push(e);
        undoBlocks += e.changes().size();
        redo.clear();
        redoBlocks = 0;
        while (undoBlocks > MAX_TOTAL_BLOCKS && undo.size() > 1) {
            Entry old = undo.pollLast();
            undoBlocks -= old.changes().size();
        }
    }

    /** Pops every entry of the newest transaction (the whole last edit). */
    public List<Entry> popUndoGroup() {
        if (undo.isEmpty()) return List.of();
        long txn = undo.peek().txn();
        List<Entry> group = new ArrayList<>();
        while (!undo.isEmpty() && undo.peek().txn() == txn) {
            Entry e = undo.poll();
            undoBlocks -= e.changes().size();
            group.add(e);
            redo.push(e);
            redoBlocks += e.changes().size();
        }
        while (redoBlocks > MAX_TOTAL_BLOCKS && redo.size() > 1) {
            Entry old = redo.pollLast();
            redoBlocks -= old.changes().size();
        }
        return group;
    }

    public List<Entry> popRedoGroup() {
        if (redo.isEmpty()) return List.of();
        long txn = redo.peek().txn();
        List<Entry> group = new ArrayList<>();
        while (!redo.isEmpty() && redo.peek().txn() == txn) {
            Entry e = redo.poll();
            redoBlocks -= e.changes().size();
            group.add(e);
            undo.push(e);
            undoBlocks += e.changes().size();
        }
        while (undoBlocks > MAX_TOTAL_BLOCKS && undo.size() > 1) {
            Entry old = undo.pollLast();
            undoBlocks -= old.changes().size();
        }
        return group;
    }

    public int undoSize() { return undo.size(); }
    public int redoSize() { return redo.size(); }
}
