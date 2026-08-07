package fr.fastedit.brush;

import org.powernukkitx.level.Level;
import fr.fastedit.block.Blocks;
import fr.fastedit.edit.EditSession;
import fr.fastedit.edit.TerrainSmoother;
import fr.fastedit.math.Vec3;

import java.util.function.Consumer;

/**
 * Rounds the terrain off inside a sphere, keeping the ground's own materials.
 * The work is in {@link TerrainSmoother}; this only carries the settings.
 */
public class SmoothBrush extends Brush {

    /** Beyond this the window would be tens of millions of blocks to snapshot. */
    public static final int MAX_RADIUS = 32;
    /** Blur passes over the depth field: 1 is a nudge, 4 melts a cliff into a slope. */
    public static final int MAX_PASSES = 6;
    public static final int DEFAULT_PASSES = 2;

    private final int passes;

    public SmoothBrush(double radius, int passes) {
        super((lvl, v) -> Blocks.air(), Math.min(radius, MAX_RADIUS));
        this.passes = passes <= 0 ? DEFAULT_PASSES : Math.min(passes, MAX_PASSES);
    }

    @Override public String kind() { return "smooth"; }

    @Override protected void shape(Vec3 center, Consumer<Vec3> out) {}

    @Override
    protected void plan(EditSession es, Level level, Vec3 hit) {
        TerrainSmoother.plan(es, level, hit, radius, passes);
    }
}
