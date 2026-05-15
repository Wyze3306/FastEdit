package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.block.Pattern;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;
import fr.fastedit.shape.Shapes;

public class SphereCommand extends FeCommand {
    public SphereCommand() { super("sphere", "Sphere at your feet."); }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        require(args.length >= 2, "usage: //sphere <pattern> <radius> [hollow]");
        Pattern pattern = Pattern.parse(args[0]);
        double radius = Double.parseDouble(args[1]);
        boolean hollow = args.length > 2 && args[2].equalsIgnoreCase("hollow");
        Vec3 center = new Vec3(p.getFloorX(), p.getFloorY(), p.getFloorZ());
        EditEngine.get().submit(p.getLevel(),
            es -> Shapes.sphere(center, radius, hollow, v -> es.plan(v, pattern.next(v))),
            n -> p.sendMessage("§dFastEdit §7| sphere §f" + n + "§7 blocks."),
            t -> p.sendMessage("§c[FastEdit] " + describe(t)),
            session.undo());
        return true;
    }
}
