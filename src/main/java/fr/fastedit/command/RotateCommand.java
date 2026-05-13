package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.clipboard.Clipboard;
import fr.fastedit.session.Session;

public class RotateCommand extends FeCommand {
    public RotateCommand() { super("rotate", "Rotate the clipboard."); }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        require(args.length >= 1, "usage: //rotate <90|180|270>");
        Clipboard clip = session.clipboard();
        require(clip != null, "your clipboard is empty");
        int deg = Integer.parseInt(args[0]);
        require(deg == 90 || deg == 180 || deg == 270, "only 90, 180 or 270");
        int steps = deg / 90;
        Clipboard out = clip;
        for (int i = 0; i < steps; i++) out = out.rotated90();
        session.setClipboard(out);
        p.sendMessage("§dFastEdit §7| clipboard rotated §f" + deg + "°");
        return true;
    }
}
