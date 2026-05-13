package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.clipboard.Clipboard;
import fr.fastedit.session.Session;

public final class RotateCmd extends Cmd {
    public RotateCmd() { super("/rotate", "Rotate the clipboard.", "//rotate <90|180|270>"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        require(args.length >= 1, "usage: //rotate <90|180|270>");
        Clipboard clip = session.clipboard();
        require(clip != null, "your clipboard is empty");
        int degrees = Integer.parseInt(args[0]);
        require(degrees == 90 || degrees == 180 || degrees == 270, "only 90, 180 or 270");

        int steps = degrees / 90;
        Clipboard rotated = clip;
        for (int i = 0; i < steps; i++) rotated = rotated.rotated90();
        session.setClipboard(rotated);
        player.sendMessage("§dFastEdit §7| clipboard rotated §f" + degrees + "°");
        return true;
    }
}
