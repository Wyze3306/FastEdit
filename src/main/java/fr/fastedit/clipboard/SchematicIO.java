package fr.fastedit.clipboard;

import cn.nukkit.Server;
import cn.nukkit.block.BlockState;
import cn.nukkit.level.structure.Structure;
import cn.nukkit.level.structure.StructureAPI;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.Tag;
import fr.fastedit.FastEdit;
import fr.fastedit.block.BlockAliases;
import fr.fastedit.block.Blocks;
import fr.fastedit.math.Vec3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public final class SchematicIO {

    private SchematicIO() {}

    public static File pluginDir() {
        File dir = new File(FastEdit.get().getDataFolder(), "schematics");
        dir.mkdirs();
        return dir;
    }

    public static File structureDir() {
        return new File(Server.getInstance().structurePath);
    }

    public static List<String> list() {
        TreeSet<String> out = new TreeSet<>();
        collect(pluginDir(),    "", out, true);
        collect(structureDir(), "", out, false);
        return new ArrayList<>(out);
    }

    private static void collect(File dir, String prefix, TreeSet<String> out, boolean keepExt) {
        if (dir == null || !dir.isDirectory()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                collect(f, prefix.isEmpty() ? f.getName() + ":" : prefix + f.getName() + "/", out, keepExt);
            } else {
                String name = f.getName();
                if (name.endsWith(".mcstructure") || name.endsWith(".schem") || name.endsWith(".schematic")) {
                    out.add(prefix + (keepExt ? name : stripExt(name)));
                }
            }
        }
    }

    private static String stripExt(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }

    public static void save(Clipboard clip, String name) {
        int w = clip.width(), h = clip.height(), l = clip.length();
        BlockState[][][][] bs = new BlockState[2][w][h][l];
        BlockState air = Blocks.air();
        for (int y = 0; y < h; y++)
            for (int z = 0; z < l; z++)
                for (int x = 0; x < w; x++) {
                    BlockState s = clip.get(x, y, z);
                    bs[0][x][y][z] = s == null ? air : s;
                    bs[1][x][y][z] = air;
                }
        Structure structure = new Structure(bs, new HashMap<>(), new ArrayList<>(), w, h, l, 0, 0, 0);
        StructureAPI.save(structure, name);
    }

    public static Clipboard load(String name) throws Exception {
        File f = resolve(name);
        if (f == null) {
            Clipboard fromStructureApi = loadStructureApi(name);
            if (fromStructureApi != null) return fromStructureApi;
            return null;
        }
        String lower = f.getName().toLowerCase();
        if (lower.endsWith(".mcstructure")) return loadMcStructure(f);
        if (lower.endsWith(".schem"))       return loadSponge(f);
        if (lower.endsWith(".schematic"))   return loadLegacy(f);
        return null;
    }

    public static boolean exists(String name) {
        return resolve(name) != null || StructureAPI.exists(name);
    }

    private static File resolve(String name) {
        for (File root : new File[]{ pluginDir(), structureDir() }) {
            File direct = new File(root, name);
            if (direct.isFile()) return direct;
            for (String ext : new String[]{ ".mcstructure", ".schem", ".schematic" }) {
                File f = new File(root, name + ext);
                if (f.isFile()) return f;
            }
        }
        return null;
    }

    private static Clipboard loadStructureApi(String name) {
        Structure s = StructureAPI.load(name);
        if (s == null) return null;
        return structureToClipboard(s);
    }

    private static Clipboard loadMcStructure(File file) throws Exception {
        CompoundTag root = NBTIO.read(file, java.nio.ByteOrder.LITTLE_ENDIAN);
        Structure s = Structure.fromNbtAsync(root).join();
        return structureToClipboard(s);
    }

    private static Clipboard structureToClipboard(Structure s) {
        int w = s.getSizeX(), h = s.getSizeY(), l = s.getSizeZ();
        Clipboard clip = new Clipboard(w, h, l);
        BlockState[][][][] bs = s.getBlockStates();
        for (int y = 0; y < h; y++)
            for (int z = 0; z < l; z++)
                for (int x = 0; x < w; x++)
                    clip.set(x, y, z, bs[0][x][y][z]);
        clip.setOffset(new Vec3(0, 0, 0));
        return clip;
    }

    private static Clipboard loadSponge(File file) throws Exception {
        CompoundTag root = NBTIO.readCompressed(new ByteArrayInputStream(Files.readAllBytes(file.toPath())));
        CompoundTag schem = root.contains("Schematic") ? root.getCompound("Schematic") : root;
        int w = schem.getShort("Width") & 0xFFFF;
        int h = schem.getShort("Height") & 0xFFFF;
        int l = schem.getShort("Length") & 0xFFFF;

        CompoundTag blocks = schem.contains("Blocks") ? schem.getCompound("Blocks") : null;
        CompoundTag palette = blocks != null && blocks.contains("Palette")
            ? blocks.getCompound("Palette")
            : schem.getCompound("Palette");
        byte[] blockData = blocks != null && blocks.contains("Data")
            ? blocks.getByteArray("Data")
            : schem.getByteArray("BlockData");

        BlockState[] states = new BlockState[paletteSize(palette)];
        String[] unknowns = new String[states.length];
        buildPalette(palette, states, unknowns);

        Clipboard clip = new Clipboard(w, h, l);
        clip.setOffset(spongeOffset(schem));

        int cursor = 0;
        int i = 0;
        long volume = (long) w * h * l;
        while (cursor < blockData.length && i < volume) {
            int value = 0;
            int shift = 0;
            while (true) {
                byte b = blockData[cursor++];
                value |= (b & 0x7F) << shift;
                if ((b & 0x80) == 0) break;
                shift += 7;
            }
            int x = i % w;
            int z = (i / w) % l;
            int y = i / (w * l);
            BlockState s = value >= 0 && value < states.length ? states[value] : null;
            clip.set(x, y, z, s == null ? Blocks.air() : s);
            if (value >= 0 && value < unknowns.length && unknowns[value] != null) {
                clip.setOriginal(x, y, z, unknowns[value]);
            }
            i++;
        }
        return clip;
    }

    private static Vec3 spongeOffset(CompoundTag schem) {
        int[] off = schem.getIntArray("Offset");
        if (off == null || off.length < 3) return new Vec3(0, 0, 0);
        return new Vec3(-off[0], -off[1], -off[2]);
    }

    private static int paletteSize(CompoundTag palette) {
        int max = 0;
        for (Map.Entry<String, Tag> e : palette.getEntrySet())
            max = Math.max(max, palette.getInt(e.getKey()) + 1);
        return max;
    }

    private static void buildPalette(CompoundTag palette, BlockState[] states, String[] unknowns) {
        for (Map.Entry<String, Tag> e : palette.getEntrySet()) {
            int idx = palette.getInt(e.getKey());
            String javaId = stripState(e.getKey());
            String mapped = BlockAliases.translate(javaId);
            BlockState st = Blocks.state(mapped);
            if (st == null && !mapped.equals(javaId)) st = Blocks.state(javaId);
            if (st == null) {
                states[idx] = Blocks.placeholder();
                unknowns[idx] = javaId;
            } else {
                states[idx] = st;
            }
        }
    }

    private static String stripState(String spec) {
        int bracket = spec.indexOf('[');
        String id = bracket < 0 ? spec : spec.substring(0, bracket);
        if (!id.contains(":")) id = "minecraft:" + id;
        return id;
    }

    private static Clipboard loadLegacy(File file) throws Exception {
        CompoundTag root = NBTIO.readCompressed(new ByteArrayInputStream(Files.readAllBytes(file.toPath())));
        CompoundTag schem = root.contains("Schematic") ? root.getCompound("Schematic") : root;
        int w = schem.getShort("Width") & 0xFFFF;
        int h = schem.getShort("Height") & 0xFFFF;
        int l = schem.getShort("Length") & 0xFFFF;
        byte[] blocks = schem.getByteArray("Blocks");
        byte[] add = schem.containsByteArray("AddBlocks") ? schem.getByteArray("AddBlocks") : null;

        Clipboard clip = new Clipboard(w, h, l);
        for (int y = 0; y < h; y++)
            for (int z = 0; z < l; z++)
                for (int x = 0; x < w; x++) {
                    int idx = (y * l + z) * w + x;
                    int id = blocks[idx] & 0xFF;
                    if (add != null && idx / 2 < add.length) {
                        int half = add[idx / 2] & 0xFF;
                        id |= ((idx % 2 == 0 ? half & 0x0F : (half >> 4) & 0x0F) << 8);
                    }
                    String javaId = LegacyIds.lookup(id);
                    if (javaId == null) {
                        clip.set(x, y, z, Blocks.placeholder());
                        clip.setOriginal(x, y, z, "legacy:" + id);
                    } else {
                        BlockState st = Blocks.state(BlockAliases.translate(javaId));
                        if (st == null) st = Blocks.state(javaId);
                        if (st == null) {
                            clip.set(x, y, z, Blocks.placeholder());
                            clip.setOriginal(x, y, z, javaId);
                        } else {
                            clip.set(x, y, z, st);
                        }
                    }
                }
        return clip;
    }
}
