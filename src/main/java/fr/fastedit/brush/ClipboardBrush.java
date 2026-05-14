package fr.fastedit.brush;

import cn.nukkit.block.BlockState;
import cn.nukkit.level.Level;
import fr.fastedit.block.Blocks;
import fr.fastedit.clipboard.Clipboard;
import fr.fastedit.edit.EditSession;
import fr.fastedit.math.Vec3;

import java.util.function.Consumer;

public final class ClipboardBrush extends Brush {

    private final Clipboard clipboard;
    private final boolean skipAir;

    public ClipboardBrush(Clipboard clipboard, boolean skipAir) {
        super(v -> Blocks.air(), 1);
        this.clipboard = clipboard;
        this.skipAir = skipAir;
    }

    @Override public String kind() { return "clipboard"; }

    @Override protected void shape(Vec3 center, Consumer<Vec3> out) {}

    @Override
    protected void plan(EditSession es, Level level, Vec3 hit) {
        Vec3 off = clipboard.offset();
        for (int y = 0; y < clipboard.height(); y++)
            for (int z = 0; z < clipboard.length(); z++)
                for (int x = 0; x < clipboard.width(); x++) {
                    BlockState s = clipboard.get(x, y, z);
                    if (s == null) continue;
                    if (skipAir && "minecraft:air".equals(s.getIdentifier())) continue;
                    es.plan(new Vec3(hit.x() + x - off.x(), hit.y() + y - off.y(), hit.z() + z - off.z()), s);
                }
    }
}
