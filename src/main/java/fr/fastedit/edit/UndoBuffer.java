package fr.fastedit.edit;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * History buffer with a global memory ceiling. A huge edit is applied as many
 * segments (one {@link Entry} each), so {@code //undo} reverts it segment by
 * segment. Total retained blocks are capped at {@link #MAX_TOTAL_BLOCKS}: once
 * exceeded, the oldest segments are evicted so the heap can never blow up,
 * regardless of how large the edit was.
 */
public final class UndoBuffer {

    public record Entry(String levelName, List<BlockChange> changes) {}

    /** ~96 B per retained change → ~4M blocks ≈ ~380 MB worst case. */
    public static final long MAX_TOTAL_BLOCKS = 4_000_000L;

    private final Deque<Entry> undo = new ArrayDeque<>();
    private final Deque<Entry> redo = new ArrayDeque<>();
    private long undoBlocks;
    private long redoBlocks;

    public UndoBuffer() {}
    /** Legacy constructor; capacity is now a memory budget, the count is ignored. */
    public UndoBuffer(int ignoredCapacity) {}

    public void push(Entry e) {
        undo.push(e);
        undoBlocks += e.changes().size();
        redo.clear();
        redoBlocks = 0;
        trim(undo, this::undoBlocks, this::setUndoBlocks);
    }

    public Entry popUndo() {
        Entry e = undo.poll();
        if (e != null) {
            undoBlocks -= e.changes().size();
            redo.push(e);
            redoBlocks += e.changes().size();
            trim(redo, this::redoBlocks, this::setRedoBlocks);
        }
        return e;
    }

    public Entry popRedo() {
        Entry e = redo.poll();
        if (e != null) {
            redoBlocks -= e.changes().size();
            undo.push(e);
            undoBlocks += e.changes().size();
            trim(undo, this::undoBlocks, this::setUndoBlocks);
        }
        return e;
    }

    public int undoSize() { return undo.size(); }
    public int redoSize() { return redo.size(); }

    private long undoBlocks() { return undoBlocks; }
    private long redoBlocks() { return redoBlocks; }
    private void setUndoBlocks(long v) { undoBlocks = v; }
    private void setRedoBlocks(long v) { redoBlocks = v; }

    /** Evict oldest entries until under budget, but always keep the newest one. */
    private void trim(Deque<Entry> dq, java.util.function.LongSupplier get,
                      java.util.function.LongConsumer set) {
        long blocks = get.getAsLong();
        while (blocks > MAX_TOTAL_BLOCKS && dq.size() > 1) {
            Entry old = dq.pollLast();
            blocks -= old.changes().size();
        }
        set.accept(blocks);
    }
}
