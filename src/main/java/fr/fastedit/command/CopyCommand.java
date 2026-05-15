package fr.fastedit.command;

import cn.nukkit.Player;
import cn.nukkit.block.BlockState;
import cn.nukkit.level.Level;
import fr.fastedit.clipboard.Clipboard;
import fr.fastedit.math.Region;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;

public class CopyCommand extends FeCommand {
    public CopyCommand() { super("copy", "Copy the selection to your clipboard."); }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        require(session.hasSelection(), "no selection — set pos1/pos2 first");
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
        p.sendMessage("§dFastEdit §7| copied §f" + r.volume() + "§7 blocks.");
        return true;
    }
}
