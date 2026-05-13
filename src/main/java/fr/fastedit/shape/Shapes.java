package fr.fastedit.shape;

import fr.fastedit.math.Region;
import fr.fastedit.math.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Shapes {

    private Shapes() {}

    public static void cuboid(Region region, Consumer<Vec3> out) {
        for (Vec3 v : region) out.accept(v);
    }

    public static void walls(Region region, Consumer<Vec3> out) {
        Vec3 lo = region.min(), hi = region.max();
        for (int y = lo.y(); y <= hi.y(); y++) {
            for (int x = lo.x(); x <= hi.x(); x++) {
                out.accept(new Vec3(x, y, lo.z()));
                if (lo.z() != hi.z()) out.accept(new Vec3(x, y, hi.z()));
            }
            for (int z = lo.z() + 1; z <= hi.z() - 1; z++) {
                out.accept(new Vec3(lo.x(), y, z));
                if (lo.x() != hi.x()) out.accept(new Vec3(hi.x(), y, z));
            }
        }
    }

    public static void sphere(Vec3 center, double radius, boolean hollow, Consumer<Vec3> out) {
        int r = (int) Math.ceil(radius);
        double rSq = radius * radius;
        double inner = (radius - 1) * (radius - 1);
        for (int dy = -r; dy <= r; dy++)
            for (int dz = -r; dz <= r; dz++)
                for (int dx = -r; dx <= r; dx++) {
                    double d = dx * dx + dy * dy + dz * dz;
                    if (d <= rSq && (!hollow || d > inner)) out.accept(center.add(dx, dy, dz));
                }
    }

    public static void cylinder(Vec3 base, double radius, int height, boolean hollow, Consumer<Vec3> out) {
        int r = (int) Math.ceil(radius);
        double rSq = radius * radius;
        double inner = (radius - 1) * (radius - 1);
        int hi = Math.max(1, height);
        for (int dy = 0; dy < hi; dy++)
            for (int dz = -r; dz <= r; dz++)
                for (int dx = -r; dx <= r; dx++) {
                    double d = dx * dx + dz * dz;
                    if (d <= rSq && (!hollow || d > inner)) out.accept(base.add(dx, dy, dz));
                }
    }

    public static void pyramid(Vec3 base, int size, boolean hollow, Consumer<Vec3> out) {
        for (int dy = 0; dy < size; dy++) {
            int extent = size - dy - 1;
            for (int dz = -extent; dz <= extent; dz++)
                for (int dx = -extent; dx <= extent; dx++) {
                    boolean edge = Math.abs(dx) == extent || Math.abs(dz) == extent || dy == 0 || dy == size - 1;
                    if (!hollow || edge) out.accept(base.add(dx, dy, dz));
                }
        }
    }

    public static List<Vec3> collect(Consumer<Consumer<Vec3>> shape) {
        List<Vec3> result = new ArrayList<>();
        shape.accept(result::add);
        return result;
    }
}
