package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.math.Region;
import fr.fastedit.session.Session;

public final class SelCmd extends Cmd {
    public SelCmd() { super("/sel", "Show or clear the current selection.", "//sel [clear]"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("clear")) {
            session.clearSelection();
            player.sendMessage("§dFastEdit §7| selection cleared.");
            return true;
        }
        if (!session.hasSelection()) {
            player.sendMessage("§dFastEdit §7| no selection. Use §f//wand§7, §f//pos1§7, §f//pos2§7.");
            return true;
        }
        Region r = session.region();
        player.sendMessage("§dFastEdit §7| §f" + r.min() + " §7-> §f" + r.max()
            + " §7(" + r.width() + "×" + r.height() + "×" + r.length() + " = §f" + r.volume() + "§7)");
        return true;
    }
}
