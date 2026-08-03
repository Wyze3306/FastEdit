package fr.fastedit.clipboard;

import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtUtils;
import org.powernukkitx.nbt.tag.CompoundTag;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Uncompressed NBT file IO. PNX 3 dropped {@code NBTIO} for the CloudburstMC
 * streams, which speak {@link NbtMap}; these bridge back to PNX's
 * {@link CompoundTag} and keep the byte layouts the plugin already writes.
 */
public final class NbtFiles {

    private NbtFiles() {}

    /** Big-endian — what the plugin's own .dat files are written in. */
    public static CompoundTag read(File file) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file));
             var reader = NbtUtils.createReader(in)) {
            return toCompound(reader.readTag());
        }
    }

    /** Little-endian — Bedrock's .mcstructure byte order. */
    public static CompoundTag readLE(File file) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file));
             var reader = NbtUtils.createReaderLE(in)) {
            return toCompound(reader.readTag());
        }
    }

    public static void write(CompoundTag tag, File file) throws IOException {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
             var writer = NbtUtils.createWriter(out)) {
            writer.writeTag(tag.toNetwork());
        }
    }

    private static CompoundTag toCompound(Object root) throws IOException {
        if (!(root instanceof NbtMap map)) {
            throw new IOException("expected a compound root, got "
                + (root == null ? "nothing" : root.getClass().getSimpleName()));
        }
        return CompoundTag.fromNetwork(map);
    }
}
