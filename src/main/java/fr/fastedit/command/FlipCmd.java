package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.clipboard.Clipboard;
import fr.fastedit.session.Session;

public final class FlipCmd extends Cmd {
    public FlipCmd() { super("/flip", "Flip the clipboard.", "//flip <x|y|z>"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        require(args.length >= 1, "usage: //flip <x|y|z>");
        Clipboard clip = session.clipboard();
        require(clip != null, "your clipboard is empty");
        char axis = Character.toLowerCase(args[0].charAt(0));
        require(axis == 'x' || axis == 'y' || axis == 'z', "axis must be x, y or z");
        session.setClipboard(clip.flipped(axis));
        player.sendMessage("§dFastEdit §7| clipboard flipped §f" + axis);
        return true;
    }
}
