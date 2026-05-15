package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.block.Pattern;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.session.Session;
import fr.fastedit.shape.Shapes;

public class WallsCommand extends FeCommand {
    public WallsCommand() { super("walls", "Walls of the selection."); }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        require(args.length >= 1, "usage: //walls <pattern>");
        require(session.hasSelection(), "no selection");
        Pattern pattern = Pattern.parse(args[0]);
        var region = session.region();
        EditEngine.get().submit(session.level(),
            es -> Shapes.walls(region, v -> es.plan(v, pattern.next(v))),
            n -> p.sendMessage("§dFastEdit §7| walls §f" + n + "§7 blocks."),
            t -> p.sendMessage("§c[FastEdit] " + describe(t)),
            session.undo());
        return true;
    }
}
