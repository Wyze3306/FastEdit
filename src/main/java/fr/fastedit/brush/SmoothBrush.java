package fr.fastedit.brush;

import cn.nukkit.block.BlockState;
import cn.nukkit.level.Level;
import fr.fastedit.block.Blocks;
import fr.fastedit.block.Pattern;
import fr.fastedit.edit.EditSession;
import fr.fastedit.math.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SmoothBrush extends Brush {

    private final int iterations;

    public SmoothBrush(Pattern fillPattern, double radius, int iterations) {
        super(fillPattern, radius);
        this.iterations = Math.max(1, iterations);
    }

    @Override public String kind() { return "smooth"; }

    @Override protected void shape(Vec3 center, Consumer<Vec3> out) {}

    @Override
    protected void plan(EditSession es, Level level, Vec3 hit) {
        int r = (int) radius;
        Map<Long, Integer> heights = new HashMap<>();
        for (int dz = -r; dz <= r; dz++) {
            for (int dx = -r; dx <= r; dx++) {
                if (dx * dx + dz * dz > r * r) continue;
                int x = hit.x() + dx;
                int z = hit.z() + dz;
                heights.put(key(x, z), highestSolidY(level, x, z, hit.y(), r));
            }
        }
        for (int it = 0; it < iterations; it++) heights = smoothPass(heights);

        BlockState air = Blocks.air();
        for (var e : heights.entrySet()) {
            int x = (int) (e.getKey() >> 32);
            int z = e.getKey().intValue();
            int targetY = e.getValue();
            for (int y = hit.y() - r; y <= hit.y() + r; y++) {
                Vec3 v = new Vec3(x, y, z);
                BlockState target = y <= targetY ? pattern.next(v) : air;
                es.plan(v, target);
            }
        }
    }

    private Map<Long, Integer> smoothPass(Map<Long, Integer> heights) {
        Map<Long, Integer> next = new HashMap<>(heights.size());
        for (var e : heights.entrySet()) {
            int x = (int) (e.getKey() >> 32);
            int z = e.getKey().intValue();
            int sum = 0, count = 0;
            for (int dz = -1; dz <= 1; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    Integer h = heights.get(key(x + dx, z + dz));
                    if (h != null) { sum += h; count++; }
                }
            }
            next.put(e.getKey(), count == 0 ? e.getValue() : sum / count);
        }
        return next;
    }

    private int highestSolidY(Level level, int x, int z, int around, int range) {
        for (int y = around + range; y >= around - range; y--) {
            BlockState s = level.getBlockStateAt(x, y, z);
            if (s != null && !"minecraft:air".equals(s.getIdentifier())) return y;
        }
        return around - range;
    }

    private static long key(int x, int z) { return (((long) x) << 32) | (z & 0xFFFFFFFFL); }
}
