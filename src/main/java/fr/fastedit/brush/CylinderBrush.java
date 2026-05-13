package fr.fastedit.brush;

import fr.fastedit.block.Pattern;
import fr.fastedit.math.Vec3;
import fr.fastedit.shape.Shapes;

import java.util.function.Consumer;

public final class CylinderBrush extends Brush {

    private final int height;

    public CylinderBrush(Pattern pattern, double radius, int height) {
        super(pattern, radius);
        this.height = Math.max(1, height);
    }

    @Override public String kind() { return "cyl"; }

    @Override
    protected void shape(Vec3 center, Consumer<Vec3> out) {
        Shapes.cylinder(center, radius, height, false, out);
    }
}
