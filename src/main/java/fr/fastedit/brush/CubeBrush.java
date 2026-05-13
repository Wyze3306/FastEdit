package fr.fastedit.brush;

import fr.fastedit.block.Pattern;
import fr.fastedit.math.Region;
import fr.fastedit.math.Vec3;
import fr.fastedit.shape.Shapes;

import java.util.function.Consumer;

public final class CubeBrush extends Brush {
    public CubeBrush(Pattern pattern, double radius) { super(pattern, radius); }

    @Override public String kind() { return "cube"; }

    @Override
    protected void shape(Vec3 center, Consumer<Vec3> out) {
        int r = (int) radius;
        Region box = new Region(center.add(-r, -r, -r), center.add(r, r, r));
        Shapes.cuboid(box, out);
    }
}
