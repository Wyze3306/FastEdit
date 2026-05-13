package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.math.Region;
import fr.fastedit.session.Session;

public class SizeCommand extends FeCommand {
    public SizeCommand() { super("size", "Show selection size and block count."); }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        require(session.hasSelection(), "no selection — set pos1/pos2 first");
        Region r = session.region();
        p.sendMessage("§dFastEdit §7| §f" + r.width() + "×" + r.height() + "×" + r.length()
            + " §7= §f" + r.volume() + " §7blocks");
        return true;
    }
}
