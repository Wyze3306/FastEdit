package fr.fastedit.clipboard;

import cn.nukkit.block.BlockState;
import fr.fastedit.block.Blocks;
import fr.fastedit.math.Region;
import fr.fastedit.math.Vec3;

import java.util.HashMap;
import java.util.Map;

public class Clipboard {

    private final int width, height, length;
    private final BlockState[] data;
    private final Map<Integer, String> originals = new HashMap<>();
    private Vec3 offset;

    public Clipboard(int width, int height, int length) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.data = new BlockState[width * height * length];
    }

    public static Clipboard ofRegion(Region region) {
        return new Clipboard(region.width(), region.height(), region.length());
    }

    public int width()  { return width; }
    public int height() { return height; }
    public int length() { return length; }
    public Vec3 offset() { return offset == null ? new Vec3(0, 0, 0) : offset; }
    public void setOffset(Vec3 o) { this.offset = o; }

    public BlockState get(int x, int y, int z) { return data[index(x, y, z)]; }
    public void set(int x, int y, int z, BlockState state) { data[index(x, y, z)] = state; }

    public void setOriginal(int x, int y, int z, String javaId) {
        if (javaId == null) originals.remove(index(x, y, z));
        else originals.put(index(x, y, z), javaId);
    }

    public String original(int x, int y, int z) { return originals.get(index(x, y, z)); }
    public int unknownCount() { return originals.size(); }

    public BlockState getOrAir(int x, int y, int z) {
        BlockState s = get(x, y, z);
        return s == null ? Blocks.air() : s;
    }

    private int index(int x, int y, int z) {
        return (y * length + z) * width + x;
    }

    public Clipboard rotated90() {
        Clipboard rotated = new Clipboard(length, height, width);
        for (int y = 0; y < height; y++)
            for (int z = 0; z < length; z++)
                for (int x = 0; x < width; x++) {
                    rotated.set(length - 1 - z, y, x, get(x, y, z));
                    String orig = original(x, y, z);
                    if (orig != null) rotated.setOriginal(length - 1 - z, y, x, orig);
                }
        Vec3 off = offset();
        rotated.setOffset(new Vec3(length - 1 - off.z(), off.y(), off.x()));
        return rotated;
    }

    public Clipboard flipped(char axis) {
        Clipboard out = new Clipboard(width, height, length);
        for (int y = 0; y < height; y++)
            for (int z = 0; z < length; z++)
                for (int x = 0; x < width; x++) {
                    int nx = axis == 'x' ? width - 1 - x : x;
                    int ny = axis == 'y' ? height - 1 - y : y;
                    int nz = axis == 'z' ? length - 1 - z : z;
                    out.set(nx, ny, nz, get(x, y, z));
                    String orig = original(x, y, z);
                    if (orig != null) out.setOriginal(nx, ny, nz, orig);
                }
        Vec3 off = offset();
        int ox = axis == 'x' ? width  - 1 - off.x() : off.x();
        int oy = axis == 'y' ? height - 1 - off.y() : off.y();
        int oz = axis == 'z' ? length - 1 - off.z() : off.z();
        out.setOffset(new Vec3(ox, oy, oz));
        return out;
    }
}
