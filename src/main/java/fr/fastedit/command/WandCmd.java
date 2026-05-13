package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.listener.WandListener;
import fr.fastedit.session.Session;

public final class WandCmd extends Cmd {
    public WandCmd() { super("/wand", "Give the FastEdit wooden axe.", "//wand"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        player.getInventory().addItem(WandListener.makeWand());
        player.sendMessage("§dFastEdit §7| §fLeft-click §7= pos1, §fRight-click §7= pos2.");
        return true;
    }
}
