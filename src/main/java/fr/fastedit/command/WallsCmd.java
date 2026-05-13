package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.block.Pattern;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.session.Session;
import fr.fastedit.shape.Shapes;

public final class WallsCmd extends Cmd {
    public WallsCmd() { super("/walls", "Walls of the selection.", "//walls <pattern>"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        require(args.length >= 1, "usage: //walls <pattern>");
        require(session.hasSelection(), "no selection");
        Pattern pattern = Pattern.parse(args[0]);
        var region = session.region();
        EditEngine.get().submit(session.level(),
            es -> Shapes.walls(region, v -> es.plan(v, pattern.next(v))),
            n -> player.sendMessage("§dFastEdit §7| walls placed §f" + n + "§7 blocks."),
            t -> player.sendMessage("§c[FastEdit] " + t.getMessage()),
            session.undo());
        return true;
    }
}
