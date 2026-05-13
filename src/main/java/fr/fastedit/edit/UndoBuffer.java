package fr.fastedit.edit;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class UndoBuffer {

    public record Entry(String levelName, List<BlockChange> changes) {}

    private final int capacity;
    private final Deque<Entry> undo = new ArrayDeque<>();
    private final Deque<Entry> redo = new ArrayDeque<>();

    public UndoBuffer(int capacity) { this.capacity = capacity; }

    public void push(Entry e) {
        if (undo.size() >= capacity) undo.pollLast();
        undo.push(e);
        redo.clear();
    }

    public Entry popUndo() {
        Entry e = undo.poll();
        if (e != null) redo.push(e);
        return e;
    }

    public Entry popRedo() {
        Entry e = redo.poll();
        if (e != null) undo.push(e);
        return e;
    }

    public int undoSize() { return undo.size(); }
    public int redoSize() { return redo.size(); }
}
