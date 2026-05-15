package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.listener.WandListener;
import fr.fastedit.session.Session;

public class WandCommand extends FeCommand {
    public WandCommand() { super("wand", "Give the FastEdit wooden axe.");
        params();
    }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        p.getInventory().addItem(WandListener.makeWand());
        p.sendMessage("§dFastEdit §7| §fbreak §7= pos1, §fright-click §7= pos2.");
        return true;
    }
}
