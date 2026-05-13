package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.block.Pattern;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;
import fr.fastedit.shape.Shapes;

public class PyramidCommand extends FeCommand {
    public PyramidCommand() { super("pyramid", "Pyramid at your feet."); }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        require(args.length >= 2, "usage: //pyramid <pattern> <size> [hollow]");
        Pattern pattern = Pattern.parse(args[0]);
        int size = Integer.parseInt(args[1]);
        boolean hollow = args.length > 2 && args[2].equalsIgnoreCase("hollow");
        Vec3 base = new Vec3(p.getFloorX(), p.getFloorY(), p.getFloorZ());
        EditEngine.get().submit(p.getLevel(),
            es -> Shapes.pyramid(base, size, hollow, v -> es.plan(v, pattern.next(v))),
            n -> p.sendMessage("§dFastEdit §7| pyramid §f" + n + "§7 blocks."),
            t -> p.sendMessage("§c[FastEdit] " + t.getMessage()),
            session.undo());
        return true;
    }
}
