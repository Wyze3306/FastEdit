package fr.fastedit.math;

public record Vec3(int x, int y, int z) {

    public static Vec3 of(int x, int y, int z) { return new Vec3(x, y, z); }

    public Vec3 add(int dx, int dy, int dz) { return new Vec3(x + dx, y + dy, z + dz); }
    public Vec3 add(Vec3 v)                 { return new Vec3(x + v.x, y + v.y, z + v.z); }
    public Vec3 sub(Vec3 v)                 { return new Vec3(x - v.x, y - v.y, z - v.z); }
    public Vec3 mul(int s)                  { return new Vec3(x * s, y * s, z * s); }

    public Vec3 min(Vec3 v) { return new Vec3(Math.min(x, v.x), Math.min(y, v.y), Math.min(z, v.z)); }
    public Vec3 max(Vec3 v) { return new Vec3(Math.max(x, v.x), Math.max(y, v.y), Math.max(z, v.z)); }

    @Override public String toString() { return "(" + x + "," + y + "," + z + ")"; }
}
