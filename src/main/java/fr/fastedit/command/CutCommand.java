package fr.fastedit.command;

import cn.nukkit.Player;
import cn.nukkit.block.BlockState;
import cn.nukkit.level.Level;
import fr.fastedit.block.Blocks;
import fr.fastedit.clipboard.Clipboard;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.math.Region;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;
import fr.fastedit.shape.Shapes;

public class CutCommand extends FeCommand {
    public CutCommand() { super("cut", "Copy + clear the selection."); }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        require(session.hasSelection(), "no selection");
        Region r = session.region();
        Level level = session.level();
        Clipboard clip = Clipboard.ofRegion(r);
        clip.setOffset(new Vec3(
            p.getFloorX() - r.min().x(),
            p.getFloorY() - r.min().y(),
            p.getFloorZ() - r.min().z()));

        for (int y = 0; y < r.height(); y++)
            for (int z = 0; z < r.length(); z++)
                for (int x = 0; x < r.width(); x++) {
                    int wx = r.min().x() + x, wy = r.min().y() + y, wz = r.min().z() + z;
                    BlockState s = level.getBlockStateAt(wx, wy, wz);
                    clip.set(x, y, z, s);
                    BlockState l1 = level.getBlockStateAt(wx, wy, wz, 1);
                    if (l1 != null && "minecraft:water".equals(l1.getIdentifier()))
                        clip.setLiquid(x, y, z, l1);
                }
        session.setClipboard(clip);

        BlockState air = Blocks.air();
        EditEngine.get().submit(level,
            es -> Shapes.cuboid(r, v -> es.plan(v, air)),
            n -> p.sendMessage("§dFastEdit §7| cut §f" + n + "§7 blocks."),
            t -> p.sendMessage("§c[FastEdit] " + describe(t)),
            session.undo());
        return true;
    }
}
