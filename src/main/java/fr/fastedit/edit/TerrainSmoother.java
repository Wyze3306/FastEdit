package fr.fastedit.edit;

import org.powernukkitx.block.BlockState;
import org.powernukkitx.level.Level;
import fr.fastedit.block.Blocks;
import fr.fastedit.block.Terrain;
import fr.fastedit.math.Vec3;

/**
 * Terrain smoothing, modelled on EasyEdit's {@code SmoothTask}
 * (github.com/platz1de/EasyEdit).
 *
 * <p>The old brush averaged one height per column and then re-poured the whole
 * column from a pattern: overhangs and caves were flattened, everything it
 * touched came out as brush material regardless of what the ground was made of,
 * and it rewrote its whole cylinder even where the terrain was already smooth.
 * This one instead
 *
 * <ol>
 *   <li>reads a window of the world into a ground/air grid and hands it to
 *       {@link SmoothField}, which decides the new shape;</li>
 *   <li>only writes where that shape actually differs from the world;</li>
 *   <li>takes each new block from the same column at the depth it maps to, so
 *       grass stays on top of dirt on top of stone, and lets water and lava
 *       close over a cut instead of leaving a dry hole in a lake.</li>
 * </ol>
 *
 * <p>Trees, plants and liquids are transparent to the height field (see
 * {@link Terrain}), so the brush follows the land and not the forest on it.
 */
public final class TerrainSmoother {

    /** Extra rows kept above and below the brush so column runs are seen whole. */
    private static final int VERTICAL_PAD = 3;

    private final int minX, minY, minZ;
    private final int w, h, d;
    private final BlockState[] blocks;
    private final boolean[] ground;
    private final SmoothField field;

    public static void plan(EditSession es, Level level, Vec3 hit, double radius, int passes) {
        new TerrainSmoother(level, hit, radius, Math.max(1, passes)).emit(es, hit, radius);
    }

    private TerrainSmoother(Level level, Vec3 hit, double radius, int passes) {
        int r = (int) Math.ceil(radius);
        // Each blur pass reaches one column further out; keep the sphere that
        // far clear of the window edge so every column it touches was blurred
        // with a full neighbourhood behind it.
        int pad = 1 + passes;

        this.minX = hit.x() - r - pad;
        this.minZ = hit.z() - r - pad;
        this.w = 2 * (r + pad) + 1;
        this.d = w;

        int lo = Math.max(level.getMinHeight(), hit.y() - r - VERTICAL_PAD);
        int hi = Math.min(level.getMaxHeight(), hit.y() + r + VERTICAL_PAD);
        this.minY = lo;
        this.h = Math.max(1, hi - lo + 1);

        int cells = w * d * h;
        this.blocks = new BlockState[cells];
        this.ground = new boolean[cells];
        snapshot(level);

        this.field = new SmoothField(w, d, h, ground, passes);
    }

    private void snapshot(Level level) {
        for (int cz = 0; cz < d; cz++)
            for (int cx = 0; cx < w; cx++) {
                int base = field(cx, cz);
                for (int cy = 0; cy < h; cy++) {
                    BlockState s = level.getBlockStateAt(minX + cx, minY + cy, minZ + cz);
                    blocks[base + cy] = s;
                    ground[base + cy] = Terrain.isGround(s);
                }
            }
    }

    /** Column offset — same layout {@link SmoothField} uses, needed before it exists. */
    private int field(int cx, int cz) { return (cz * w + cx) * h; }

    private void emit(EditSession es, Vec3 hit, double radius) {
        int r = (int) Math.ceil(radius);
        double rSq = radius * radius;
        BlockState air = Blocks.air();

        for (int dz = -r; dz <= r; dz++)
            for (int dx = -r; dx <= r; dx++) {
                int cx = hit.x() + dx - minX;
                int cz = hit.z() + dz - minZ;
                int base = field.column(cx, cz);
                for (int dy = -r; dy <= r; dy++) {
                    if (dx * dx + dy * dy + dz * dz > rSq) continue;
                    int cy = hit.y() + dy - minY;
                    if (cy < 0 || cy >= h) continue;
                    if (!field.changes(base, cy)) continue;

                    BlockState target;
                    if (field.after(base, cy) > 0) {
                        int src = field.source(base, cy);
                        target = src < 0 ? borrow(cx, cz, cy) : blocks[base + src];
                    } else {
                        // Only actual ground is carved away: a flower or a tree
                        // standing in the air above is left where it is.
                        target = ground[base + cy] ? clear(cx, cz, cy, air) : null;
                    }
                    if (target == null || target == blocks[base + cy]) continue;
                    es.plan(new Vec3(minX + cx, minY + cy, minZ + cz), target);
                }
            }
    }

    /** Air, unless the block sits in a lake — then the liquid closes over it. */
    private BlockState clear(int cx, int cz, int cy, BlockState air) {
        BlockState above = cy + 1 < h ? blocks[field.column(cx, cz) + cy + 1] : null;
        if (Terrain.isLiquid(above)) return above;
        BlockState side = around(cx, cz, cy, true);
        return side != null ? side : air;
    }

    /** Nothing left in this column to copy — take the material from next door. */
    private BlockState borrow(int cx, int cz, int cy) {
        return around(cx, cz, cy, false);
    }

    private BlockState around(int cx, int cz, int cy, boolean liquid) {
        for (int oz = -1; oz <= 1; oz++)
            for (int ox = -1; ox <= 1; ox++) {
                if (ox == 0 && oz == 0) continue;
                int nx = cx + ox, nz = cz + oz;
                if (nx < 0 || nx >= w || nz < 0 || nz >= d) continue;
                int at = field.column(nx, nz) + cy;
                if (liquid ? Terrain.isLiquid(blocks[at]) : ground[at]) return blocks[at];
            }
        return null;
    }
}
