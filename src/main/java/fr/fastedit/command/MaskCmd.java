package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.block.Mask;
import fr.fastedit.brush.Brush;
import fr.fastedit.session.Session;

public final class MaskCmd extends Cmd {
    public MaskCmd() { super("/mask", "Set a mask for the current brush.", "//mask <mask|none>"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        Brush brush = session.brush();
        require(brush != null, "no active brush — //brush <kind> ... first");
        if (args.length == 0 || args[0].equalsIgnoreCase("none")) {
            brush.withMask(Mask.ANY);
            player.sendMessage("§dFastEdit §7| brush mask cleared.");
        } else {
            brush.withMask(Mask.parse(args[0]));
            player.sendMessage("§dFastEdit §7| brush mask set: §f" + args[0]);
        }
        return true;
    }
}
