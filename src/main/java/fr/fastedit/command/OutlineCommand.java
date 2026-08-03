package fr.fastedit.command;

import org.powernukkitx.Player;
import fr.fastedit.session.Session;

public class OutlineCommand extends FeCommand {
    public OutlineCommand() { super("outline", "Toggle the white selection outline.");
        params(enm("state", true, "on", "off"));
    }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        boolean visible;
        if (args.length > 0 && (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off"))) {
            visible = args[0].equalsIgnoreCase("on");
        } else {
            visible = !session.outlineVisible();
        }
        session.setOutlineVisible(visible);
        p.sendMessage("§dFastEdit §7| selection outline " + (visible ? "§aon" : "§coff") + "§7.");
        return true;
    }
}
