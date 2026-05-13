package fr.fastedit.brush;

import fr.fastedit.block.Pattern;
import fr.fastedit.math.Vec3;
import fr.fastedit.shape.Shapes;

import java.util.function.Consumer;

public final class SphereBrush extends Brush {
    public SphereBrush(Pattern pattern, double radius) { super(pattern, radius); }

    @Override public String kind() { return "sphere"; }

    @Override
    protected void shape(Vec3 center, Consumer<Vec3> out) {
        Shapes.sphere(center, radius, false, out);
    }
}
