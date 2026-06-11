package fr.fastedit.edit;

import cn.nukkit.level.ChunkLoader;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.IChunk;

/**
 * No-op ChunkLoader registered on every chunk an edit touches, for the
 * lifetime of that edit. A pinned chunk is "in use" for PNX, so the chunk
 * GC can neither unload it nor push it through the async saveChunks() path
 * whose setChanged(false)-after-serialize races our main-thread writes —
 * the race that punched random holes into big pastes when the chunk was
 * later reloaded from a disk copy missing those writes.
 */
final class ChunkPin implements ChunkLoader {

    private final Level level;
    private final int id;

    /** Must be created on the main thread: the loader-id counter is unsynchronized. */
    ChunkPin(Level level) {
        this.level = level;
        this.id = Level.generateChunkLoaderId(this);
    }

    @Override public int getLoaderId() { return id; }
    @Override public boolean isLoaderActive() { return true; }
    @Override public Position getPosition() { return new Position(0, 0, 0, level); }
    @Override public double getX() { return 0; }
    @Override public double getZ() { return 0; }
    @Override public Level getLevel() { return level; }
    @Override public void onChunkChanged(IChunk chunk) {}
    @Override public void onChunkLoaded(IChunk chunk) {}
    @Override public void onChunkUnloaded(IChunk chunk) {}
}
