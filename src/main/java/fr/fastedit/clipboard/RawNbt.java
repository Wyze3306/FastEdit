package fr.fastedit.clipboard;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

public final class RawNbt {

    private RawNbt() {}

    public static Map read(byte[] gzipped) throws IOException {
        try (DataInputStream in = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(gzipped)))) {
            int type = in.readByte();
            if (type != 10) throw new IOException("expected TAG_Compound, got " + type);
            readUtf(in);
            return readCompound(in);
        }
    }

    public static final class Map extends LinkedHashMap<String, Object> {
        public boolean has(String k)        { return containsKey(k); }
        public int    getInt(String k)      { Object o = get(k); return o instanceof Number n ? n.intValue() : 0; }
        public short  getShort(String k)    { Object o = get(k); return o instanceof Number n ? n.shortValue() : 0; }
        public byte[] getByteArray(String k){ Object o = get(k); return o instanceof byte[] b ? b : new byte[0]; }
        public int[]  getIntArray(String k) { Object o = get(k); return o instanceof int[] a ? a : new int[0]; }
        public String getString(String k)   { Object o = get(k); return o instanceof String s ? s : ""; }
        public Map    getCompound(String k) { Object o = get(k); return o instanceof Map m ? m : new Map(); }
    }

    private static Map readCompound(DataInputStream in) throws IOException {
        Map map = new Map();
        while (true) {
            int type = in.readByte() & 0xFF;
            if (type == 0) return map;
            String name = readUtf(in);
            map.put(name, readPayload(in, type));
        }
    }

    private static Object readPayload(DataInputStream in, int type) throws IOException {
        return switch (type) {
            case 1  -> in.readByte();
            case 2  -> in.readShort();
            case 3  -> in.readInt();
            case 4  -> in.readLong();
            case 5  -> in.readFloat();
            case 6  -> in.readDouble();
            case 7  -> readByteArray(in);
            case 8  -> readUtf(in);
            case 9  -> readList(in);
            case 10 -> readCompound(in);
            case 11 -> readIntArray(in);
            case 12 -> readLongArray(in);
            default -> throw new IOException("unknown NBT tag: " + type);
        };
    }

    private static byte[] readByteArray(DataInputStream in) throws IOException {
        int len = in.readInt();
        if (len < 0) throw new IOException("negative array length: " + len);
        byte[] b = new byte[len];
        in.readFully(b);
        return b;
    }

    private static int[] readIntArray(DataInputStream in) throws IOException {
        int len = in.readInt();
        if (len < 0) throw new IOException("negative int-array length: " + len);
        int[] a = new int[len];
        for (int i = 0; i < len; i++) a[i] = in.readInt();
        return a;
    }

    private static long[] readLongArray(DataInputStream in) throws IOException {
        int len = in.readInt();
        if (len < 0) throw new IOException("negative long-array length: " + len);
        long[] a = new long[len];
        for (int i = 0; i < len; i++) a[i] = in.readLong();
        return a;
    }

    private static List<Object> readList(DataInputStream in) throws IOException {
        int childType = in.readByte() & 0xFF;
        int len = in.readInt();
        if (len < 0) throw new IOException("negative list length: " + len);
        List<Object> list = new ArrayList<>(Math.min(len, 1 << 16));
        for (int i = 0; i < len; i++) list.add(readPayload(in, childType));
        return list;
    }

    private static String readUtf(DataInputStream in) throws IOException {
        int len = in.readUnsignedShort();
        byte[] b = new byte[len];
        in.readFully(b);
        return new String(b, StandardCharsets.UTF_8);
    }
}
