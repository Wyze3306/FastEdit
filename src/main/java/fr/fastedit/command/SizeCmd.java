package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.math.Region;
import fr.fastedit.session.Session;

public final class SizeCmd extends Cmd {
    public SizeCmd() { super("/size", "Show selection size and block count.", "//size"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        require(session.hasSelection(), "no selection — set pos1/pos2 first");
        Region r = session.region();
        player.sendMessage("§dFastEdit §7| §f" + r.width() + "×" + r.height() + "×" + r.length()
            + " §7= §f" + r.volume() + " §7blocks");
        return true;
    }
}
