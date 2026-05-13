package fr.fastedit.clipboard;

import cn.nukkit.block.BlockState;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.registry.Registries;
import fr.fastedit.block.Blocks;
import fr.fastedit.math.Vec3;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gzipped-NBT schematic format (.fschem).
 *
 *   Width / Height / Length    int
 *   OffsetX / OffsetY / OffsetZ int (anchor relative to the origin corner)
 *   Palette                    list of strings ("minecraft:stone", ...)
 *   Blocks                     int array of palette indices (size = w*h*l)
 *
 * Block states beyond the default state aren't preserved here — they collapse
 * to their default state on load. This keeps the format trivial to read by
 * hand and is what most casual builds need. Extensible: bump a "Version"
 * field if you need to add proper state hashes later.
 */
public final class Schematic {

    private Schematic() {}

    public static byte[] save(Clipboard clip) throws Exception {
        int w = clip.width(), h = clip.height(), l = clip.length();
        Map<String, Integer> paletteIndex = new HashMap<>();
        List<String> palette = new ArrayList<>();
        int[] blocks = new int[w * h * l];

        paletteIndex.put(Blocks.AIR_ID, 0);
        palette.add(Blocks.AIR_ID);

        int idx = 0;
        for (int y = 0; y < h; y++)
            for (int z = 0; z < l; z++)
                for (int x = 0; x < w; x++) {
                    BlockState s = clip.get(x, y, z);
                    String id = s == null ? Blocks.AIR_ID : s.getIdentifier();
                    Integer p = paletteIndex.get(id);
                    if (p == null) {
                        p = palette.size();
                        palette.add(id);
                        paletteIndex.put(id, p);
                    }
                    blocks[idx++] = p;
                }

        ListTag<StringTag> paletteTag = new ListTag<>();
        for (String s : palette) paletteTag.add(new StringTag(s));

        CompoundTag root = new CompoundTag()
            .putInt("Version", 1)
            .putInt("Width", w)
            .putInt("Height", h)
            .putInt("Length", l)
            .putInt("OffsetX", clip.offset().x())
            .putInt("OffsetY", clip.offset().y())
            .putInt("OffsetZ", clip.offset().z())
            .putList("Palette", paletteTag)
            .putIntArray("Blocks", blocks);

        return NBTIO.writeGZIPCompressed(new CompoundTag().putCompound("FastEdit", root));
    }

    public static Clipboard load(byte[] data) throws Exception {
        CompoundTag wrap = NBTIO.readCompressed(new ByteArrayInputStream(data));
        CompoundTag root = wrap.getCompound("FastEdit");
        int w = root.getInt("Width");
        int h = root.getInt("Height");
        int l = root.getInt("Length");

        ListTag<StringTag> paletteTag = root.getList("Palette", StringTag.class);
        String[] palette = new String[paletteTag.size()];
        for (int i = 0; i < palette.length; i++) palette[i] = paletteTag.get(i).data;

        int[] blocks = root.getIntArray("Blocks");

        Clipboard clip = new Clipboard(w, h, l);
        clip.setOffset(new Vec3(root.getInt("OffsetX"), root.getInt("OffsetY"), root.getInt("OffsetZ")));

        int idx = 0;
        for (int y = 0; y < h; y++)
            for (int z = 0; z < l; z++)
                for (int x = 0; x < w; x++) {
                    String id = palette[blocks[idx++]];
                    var props = Registries.BLOCK.getBlockProperties(id);
                    clip.set(x, y, z, props == null ? Blocks.air() : props.getDefaultState());
                }
        return clip;
    }

    public static void writeFile(File file, Clipboard clip) throws Exception {
        byte[] bytes = save(clip);
        file.getParentFile().mkdirs();
        try (var out = new FileOutputStream(file)) {
            out.write(bytes);
        }
    }

    public static Clipboard readFile(File file) throws Exception {
        return load(Files.readAllBytes(file.toPath()));
    }
}
