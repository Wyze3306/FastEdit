package fr.fastedit.math;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class Region implements Iterable<Vec3> {

    private final Vec3 min;
    private final Vec3 max;

    public Region(Vec3 a, Vec3 b) {
        this.min = a.min(b);
        this.max = a.max(b);
    }

    public Vec3 min() { return min; }
    public Vec3 max() { return max; }

    public int width()  { return max.x() - min.x() + 1; }
    public int height() { return max.y() - min.y() + 1; }
    public int length() { return max.z() - min.z() + 1; }

    public long volume() { return (long) width() * height() * length(); }

    public boolean contains(int x, int y, int z) {
        return x >= min.x() && x <= max.x()
            && y >= min.y() && y <= max.y()
            && z >= min.z() && z <= max.z();
    }

    public Region shifted(int dx, int dy, int dz) {
        return new Region(min.add(dx, dy, dz), max.add(dx, dy, dz));
    }

    @Override
    public Iterator<Vec3> iterator() {
        return new Iterator<>() {
            int x = min.x(), y = min.y(), z = min.z();
            boolean done = false;

            @Override public boolean hasNext() { return !done; }

            @Override
            public Vec3 next() {
                if (done) throw new NoSuchElementException();
                Vec3 v = new Vec3(x, y, z);
                if (++x > max.x()) {
                    x = min.x();
                    if (++z > max.z()) {
                        z = min.z();
                        if (++y > max.y()) done = true;
                    }
                }
                return v;
            }
        };
    }
}
