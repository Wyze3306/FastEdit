package fr.fastedit.edit;

/**
 * The arithmetic behind {@link TerrainSmoother}, over a plain ground/air grid.
 *
 * <p>Every column of the window becomes a <em>signed depth field</em>: {@code +1}
 * on the outermost ground block growing inwards, {@code -1} on the air touching
 * it growing outwards. Blurring that field across the 3×3 neighbouring columns
 * and thresholding it back to ground/air is what rounds a surface off — spikes
 * lose the vote, pits get filled, and flat ground or a straight wall does not
 * move at all. Runs that leave the window count as going on forever, so the
 * window edge is never mistaken for a surface.
 *
 * <p>Modelled on EasyEdit's {@code SmoothTask} (github.com/platz1de/EasyEdit),
 * which clamps the air side of the field to a flat {@code -1} while letting the
 * ground side grow — that drags material sideways out of any wall taller than
 * the brush. The two sides are symmetric here.
 */
final class SmoothField {

    /** A run that leaves the window: it keeps going, so its edge is not a surface. */
    private static final int OPEN = 1 << 20;

    private final int w, d, h;
    private final boolean[] ground;
    private final int[] before;
    private final int[] after;

    SmoothField(int w, int d, int h, boolean[] ground, int passes) {
        this.w = w;
        this.d = d;
        this.h = h;
        this.ground = ground;
        this.before = new int[ground.length];
        this.after = new int[ground.length];

        profile(ground, before);

        float[] field = new float[before.length];
        for (int i = 0; i < before.length; i++) field[i] = before[i];
        for (int p = 0; p < Math.max(1, passes); p++) field = blur(field);

        boolean[] smoothed = new boolean[field.length];
        for (int i = 0; i < field.length; i++) smoothed[i] = field[i] > 0;
        profile(smoothed, after);
    }

    int column(int cx, int cz) { return (cz * w + cx) * h; }

    int before(int base, int cy) { return before[base + cy]; }
    int after(int base, int cy)  { return after[base + cy]; }

    /** True where the smoothed profile disagrees with the world as it stands. */
    boolean changes(int base, int cy) { return before[base + cy] != after[base + cy]; }

    /** Signed distance to the nearest ground/air boundary, column by column. */
    private void profile(boolean[] solid, int[] out) {
        for (int cz = 0; cz < d; cz++)
            for (int cx = 0; cx < w; cx++) {
                int base = column(cx, cz);
                int start = 0;
                while (start < h) {
                    boolean run = solid[base + start];
                    int end = start;
                    while (end + 1 < h && solid[base + end + 1] == run) end++;
                    for (int y = start; y <= end; y++) {
                        int below = start == 0     ? OPEN : y - start;
                        int above = end   == h - 1 ? OPEN : end - y;
                        int depth = 1 + Math.min(Math.min(below, above), h);
                        out[base + y] = run ? depth : -depth;
                    }
                    start = end + 1;
                }
            }
    }

    private float[] blur(float[] in) {
        float[] out = new float[in.length];
        for (int cz = 0; cz < d; cz++)
            for (int cx = 0; cx < w; cx++) {
                int base = column(cx, cz);
                for (int cy = 0; cy < h; cy++) {
                    float sum = 0;
                    int n = 0;
                    for (int oz = -1; oz <= 1; oz++) {
                        int nz = cz + oz;
                        if (nz < 0 || nz >= d) continue;
                        for (int ox = -1; ox <= 1; ox++) {
                            int nx = cx + ox;
                            if (nx < 0 || nx >= w) continue;
                            sum += in[column(nx, nz) + cy];
                            n++;
                        }
                    }
                    out[base + cy] = sum / n;
                }
            }
        return out;
    }

    /**
     * Which row of this column the block at {@code cy} should be taken from.
     * The new ground run is mapped onto the old one proportionally, from the
     * surface down, so a cut that moved by three blocks still comes out
     * grass / dirt / stone in that order.
     *
     * @return a row in the window, or {@code -1} when the column has no ground
     *         left to copy and the caller has to look sideways
     */
    int source(int base, int cy) {
        int dir = after[base + cy] < after[base + clamp(cy + 1)] ? 1 : -1;

        int nAnchor = climb(after, base, cy, dir);
        int nSurface = clamp(nAnchor - dir * (after[base + nAnchor] - 1));

        int oAnchor = climb(before, base, cy, dir);
        if (before[base + oAnchor] <= 0) return -1;
        int oSurface = clamp(oAnchor - dir * (before[base + oAnchor] - 1));

        int anchor = dir > 0 ? Math.max(nAnchor, oAnchor) : Math.min(nAnchor, oAnchor);
        int spanNew = dir * (anchor - nSurface);
        int spanOld = dir * (anchor - oSurface);
        int distNew = dir * (anchor - cy);

        double along = spanNew <= 0 ? 0 : Math.min(1, Math.max(0, distNew / (double) spanNew));
        int src = clamp(anchor - dir * (int) Math.round(along * Math.max(0, spanOld)));
        // The two runs can share an anchor, which lands the read past the end
        // of the old one; the core of the old run is the honest answer there.
        return before[base + src] > 0 ? src : oAnchor;
    }

    /** Walks towards the core of a run: as far as the depth keeps rising. */
    private int climb(int[] field, int base, int from, int dir) {
        int at = from;
        while (true) {
            int next = at + dir;
            if (next < 0 || next >= h) return at;
            if (field[base + next] <= field[base + at]) return at;
            at = next;
        }
    }

    private int clamp(int cy) { return Math.min(h - 1, Math.max(0, cy)); }
}
