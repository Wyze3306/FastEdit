package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.block.Mask;
import fr.fastedit.block.Pattern;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.session.Session;
import fr.fastedit.shape.Shapes;

public final class ReplaceCmd extends Cmd {
    public ReplaceCmd() { super("/replace", "Replace blocks matching a mask.", "//replace <mask> <pattern>"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        require(args.length >= 2, "usage: //replace <mask> <pattern>");
        require(session.hasSelection(), "no selection — set pos1/pos2 first");
        Mask mask = Mask.parse(args[0]);
        Pattern pattern = Pattern.parse(args[1]);
        var region = session.region();
        var level = session.level();

        EditEngine.get().submit(level,
            es -> {
                es.setFilter(mask::matches);
                Shapes.cuboid(region, v -> es.plan(v, pattern.next(v)));
            },
            n -> player.sendMessage("§dFastEdit §7| replaced §f" + n + "§7 blocks."),
            t -> player.sendMessage("§c[FastEdit] " + t.getMessage()),
            session.undo());
        return true;
    }
}
