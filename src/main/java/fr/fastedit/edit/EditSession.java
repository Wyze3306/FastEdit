package fr.fastedit.edit;

import cn.nukkit.block.BlockState;
import cn.nukkit.level.Level;
import fr.fastedit.block.Pattern;
import fr.fastedit.math.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class EditSession {

    private final Level level;
    private final List<BlockChange> changes;
    private BiPredicate<Level, Vec3> filter;

    public EditSession(Level level, int estimatedSize) {
        this.level = level;
        this.changes = new ArrayList<>(Math.min(Math.max(estimatedSize, 64), 1 << 16));
    }

    public Level level() { return level; }
    public List<BlockChange> changes() { return changes; }
    public int size() { return changes.size(); }
    public BiPredicate<Level, Vec3> filter() { return filter; }
    public void setFilter(BiPredicate<Level, Vec3> filter) { this.filter = filter; }

    public void plan(Vec3 pos, BlockState target) {
        if (target == null) return;
        changes.add(new BlockChange(pos, target));
    }

    public void planAll(Iterable<Vec3> positions, Pattern pattern) {
        for (Vec3 v : positions) plan(v, pattern.next(v));
    }
}
