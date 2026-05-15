package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.block.Pattern;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.session.Session;
import fr.fastedit.shape.Shapes;

public class SetCommand extends FeCommand {
    public SetCommand() { super("set", "Fill the selection with a pattern.");
        params(block("pattern", false));
    }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        require(args.length >= 1, "usage: //set <pattern>");
        require(session.hasSelection(), "no selection — set pos1/pos2 first");
        Pattern pattern = Pattern.parse(args[0]);
        var region = session.region();
        EditEngine.get().submit(session.level(),
            es -> Shapes.cuboid(region, v -> es.plan(v, pattern.next(v))),
            n -> p.sendMessage("§dFastEdit §7| set §f" + n + "§7 blocks."),
            t -> p.sendMessage("§c[FastEdit] " + describe(t)),
            session.undo());
        return true;
    }
}
