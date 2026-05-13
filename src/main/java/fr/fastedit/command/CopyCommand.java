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
                    BlockState s = level.getBlockStateAt(r.min().x() + x, r.min().y() + y, r.min().z() + z);
                    clip.set(x, y, z, s);
                }

        session.setClipboard(clip);
        p.sendMessage("§dFastEdit §7| copied §f" + r.volume() + "§7 blocks.");
        return true;
    }
}
