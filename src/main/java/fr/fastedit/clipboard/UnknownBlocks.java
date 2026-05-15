package fr.fastedit.clipboard;

import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import fr.fastedit.FastEdit;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UnknownBlocks {

    private static final Map<String, Map<Long, String>> BY_WORLD = new ConcurrentHashMap<>();
    private static boolean loaded;

    private UnknownBlocks() {}

    public static synchronized void load() {
        if (loaded) return;
        loaded = true;
        File f = file();
        if (!f.exists()) return;
        try {
            CompoundTag root = NBTIO.read(f);
            ListTag<CompoundTag> entries = root.getList("entries", CompoundTag.class);
            for (int i = 0; i < entries.size(); i++) {
                CompoundTag e = entries.get(i);
                record(e.getString("world"), e.getInt("x"), e.getInt("y"), e.getInt("z"), e.getString("id"), false);
            }
        } catch (Exception e) {
            FastEdit.get().getLogger().warning("[FastEdit] failed to load unknowns: " + e.getMessage());
        }
    }

    public static synchronized void save() {
        ListTag<CompoundTag> entries = new ListTag<>();
        for (var w : BY_WORLD.entrySet()) {
            for (var e : w.getValue().entrySet()) {
                long packed = e.getKey();
                int x = (int) (packed >> 38);
                int y = (int) ((packed >> 26) & 0xFFF);
                int z = (int) (packed << 38 >> 38);
                entries.add(new CompoundTag()
                    .putString("world", w.getKey())
                    .putInt("x", x).putInt("y", y).putInt("z", z)
                    .putString("id", e.getValue()));
            }
        }
        try {
            File f = file();
            f.getParentFile().mkdirs();
            NBTIO.write(new CompoundTag().putList("entries", entries), f);
        } catch (Exception e) {
            FastEdit.get().getLogger().warning("[FastEdit] failed to save unknowns: " + e.getMessage());
        }
    }

    /**
     * Records an unknown block in memory only. Call {@link #flush()} once after a
     * batch (e.g. a paste) to persist — never persist per-block, that rewrites the
     * whole .dat file on disk for every block and stalls/OOMs large pastes.
     */
    public static void record(String world, int x, int y, int z, String javaId) {
        record(world, x, y, z, javaId, false);
    }

    public static void flush() {
        save();
    }

    private static void record(String world, int x, int y, int z, String javaId, boolean persist) {
        if (world == null || javaId == null) return;
        BY_WORLD.computeIfAbsent(world, k -> new ConcurrentHashMap<>()).put(pack(x, y, z), javaId);
        if (persist) save();
    }

    public static String lookup(String world, int x, int y, int z) {
        Map<Long, String> map = BY_WORLD.get(world);
        return map == null ? null : map.get(pack(x, y, z));
    }

    public static void clear(String world, int x, int y, int z) {
        Map<Long, String> map = BY_WORLD.get(world);
        if (map != null) map.remove(pack(x, y, z));
    }

    private static long pack(int x, int y, int z) {
        return ((long) x << 38) | (((long) y & 0xFFF) << 26) | ((long) z & 0x3FFFFFF);
    }

    private static File file() {
        return new File(FastEdit.get().getDataFolder(), "unknown_blocks.dat");
    }
}
