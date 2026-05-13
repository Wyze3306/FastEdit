package fr.fastedit.brush;

import cn.nukkit.Player;
import cn.nukkit.level.Level;
import fr.fastedit.block.Mask;
import fr.fastedit.block.Pattern;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.edit.EditSession;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;
import fr.fastedit.shape.Shapes;

import java.util.function.Consumer;

public abstract class Brush {

    protected final Pattern pattern;
    protected final double radius;
    protected Mask mask = Mask.ANY;

    protected Brush(Pattern pattern, double radius) {
        this.pattern = pattern;
        this.radius = Math.max(1, radius);
    }

    public Brush withMask(Mask m) { this.mask = m == null ? Mask.ANY : m; return this; }

    public abstract String kind();

    public void use(Player player, Session session, Level level, Vec3 hit) {
        EditSession es = new EditSession(level, 0);
        plan(es, level, hit);
        EditEngine.get().apply(es, session.undo(), null);
    }

    protected void plan(EditSession es, Level level, Vec3 hit) {
        Consumer<Vec3> sink = v -> {
            if (mask == Mask.ANY || mask.matches(level, v)) {
                es.plan(v, pattern.next(v));
            }
        };
        Shapes.collect(out -> shape(hit, out)).forEach(sink);
    }

    protected abstract void shape(Vec3 center, Consumer<Vec3> out);
}
