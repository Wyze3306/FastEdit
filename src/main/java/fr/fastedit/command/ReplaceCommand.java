package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.block.Mask;
import fr.fastedit.block.Pattern;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.session.Session;
import fr.fastedit.shape.Shapes;

public class ReplaceCommand extends FeCommand {
    public ReplaceCommand() { super("replace", "Replace blocks matching a mask."); }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        require(args.length >= 2, "usage: //replace <mask> <pattern>");
        require(session.hasSelection(), "no selection");
        Mask mask = Mask.parse(args[0]);
        Pattern pattern = Pattern.parse(args[1]);
        var region = session.region();
        EditEngine.get().submit(session.level(),
            es -> {
                es.setFilter(mask::matches);
                Shapes.cuboid(region, v -> es.plan(v, pattern.next(v)));
            },
            n -> p.sendMessage("§dFastEdit §7| replaced §f" + n + "§7 blocks."),
            t -> p.sendMessage("§c[FastEdit] " + describe(t)),
            session.undo());
        return true;
    }
}
