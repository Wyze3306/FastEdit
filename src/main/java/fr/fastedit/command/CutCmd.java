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

public final class CutCmd extends Cmd {
    public CutCmd() { super("/cut", "Copy + clear the selection.", "//cut"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        require(session.hasSelection(), "no selection");
        Region r = session.region();
        Level level = session.level();
        Clipboard clip = Clipboard.ofRegion(r);
        clip.setOffset(new Vec3(
            player.getFloorX() - r.min().x(),
            player.getFloorY() - r.min().y(),
            player.getFloorZ() - r.min().z()));

        for (int y = 0; y < r.height(); y++)
            for (int z = 0; z < r.length(); z++)
                for (int x = 0; x < r.width(); x++) {
                    BlockState s = level.getBlockStateAt(r.min().x() + x, r.min().y() + y, r.min().z() + z);
                    clip.set(x, y, z, s);
                }
        session.setClipboard(clip);

        BlockState air = Blocks.air();
        EditEngine.get().submit(level,
            es -> Shapes.cuboid(r, v -> es.plan(v, air)),
            n -> player.sendMessage("§dFastEdit §7| cut §f" + n + "§7 blocks."),
            t -> player.sendMessage("§c[FastEdit] " + t.getMessage()),
            session.undo());
        return true;
    }
}
