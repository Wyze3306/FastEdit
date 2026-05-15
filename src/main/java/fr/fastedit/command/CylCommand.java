package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.block.Pattern;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;
import fr.fastedit.shape.Shapes;

public class CylCommand extends FeCommand {
    public CylCommand() { super("cyl", "Cylinder at your feet.");
        params(block("pattern", false), dec("radius", false), num("height", false), enm("hollow", true, "hollow"));
    }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        require(args.length >= 3, "usage: //cyl <pattern> <radius> <height> [hollow]");
        Pattern pattern = Pattern.parse(args[0]);
        double radius = Double.parseDouble(args[1]);
        int height = Integer.parseInt(args[2]);
        boolean hollow = args.length > 3 && args[3].equalsIgnoreCase("hollow");
        Vec3 base = new Vec3(p.getFloorX(), p.getFloorY(), p.getFloorZ());
        EditEngine.get().submit(p.getLevel(),
            es -> Shapes.cylinder(base, radius, height, hollow, v -> es.plan(v, pattern.next(v))),
            n -> p.sendMessage("§dFastEdit §7| cyl §f" + n + "§7 blocks."),
            t -> p.sendMessage("§c[FastEdit] " + describe(t)),
            session.undo());
        return true;
    }
}
