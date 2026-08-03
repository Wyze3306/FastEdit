package fr.fastedit.session;

import org.powernukkitx.level.Level;
import fr.fastedit.clipboard.Clipboard;
import fr.fastedit.edit.UndoBuffer;
import fr.fastedit.math.Region;
import fr.fastedit.math.Vec3;

public class Session {

    private final java.util.UUID owner;
    private final UndoBuffer undo = new UndoBuffer(20);

    private Level level;
    private Vec3 pos1;
    private Vec3 pos2;
    private Clipboard clipboard;
    private long lastBrushUseMs;
    private long lastToolUseMs;
    private String lastToolAction;
    private boolean outlineVisible = true;

    public Session(java.util.UUID owner) { this.owner = owner; }

    public java.util.UUID owner() { return owner; }
    public UndoBuffer undo()      { return undo; }

    public Level level()         { return level; }
    public Vec3 pos1()           { return pos1; }
    public Vec3 pos2()           { return pos2; }
    public Clipboard clipboard() { return clipboard; }
    public long lastBrushUseMs() { return lastBrushUseMs; }
    public long lastToolUseMs()  { return lastToolUseMs; }
    public String lastToolAction() { return lastToolAction; }

    public void setLevel(Level level)     { this.level = level; }
    public void setPos1(Level l, Vec3 v)  { this.level = l; this.pos1 = v; }
    public void setPos2(Level l, Vec3 v)  { this.level = l; this.pos2 = v; }
    public void setClipboard(Clipboard c) { this.clipboard = c; }
    public void setLastBrushUseMs(long ms) { this.lastBrushUseMs = ms; }
    public void setLastTool(long ms, String action) { this.lastToolUseMs = ms; this.lastToolAction = action; }

    public boolean outlineVisible() { return outlineVisible; }
    public void setOutlineVisible(boolean visible) { this.outlineVisible = visible; }

    public boolean hasSelection() { return level != null && pos1 != null && pos2 != null; }

    public Region region() {
        if (!hasSelection()) return null;
        return new Region(pos1, pos2);
    }

    public void clearSelection() {
        pos1 = null;
        pos2 = null;
    }
}
