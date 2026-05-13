package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.block.Pattern;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;
import fr.fastedit.shape.Shapes;

public final class CylCmd extends Cmd {
    public CylCmd() { super("/cyl", "Cylinder at your feet.", "//cyl <pattern> <radius> <height> [hollow]"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        require(args.length >= 3, "usage: //cyl <pattern> <radius> <height> [hollow]");
        Pattern pattern = Pattern.parse(args[0]);
        double radius  = Double.parseDouble(args[1]);
        int    height  = Integer.parseInt(args[2]);
        boolean hollow = args.length > 3 && args[3].equalsIgnoreCase("hollow");

        Vec3 base = new Vec3(player.getFloorX(), player.getFloorY(), player.getFloorZ());
        EditEngine.get().submit(player.getLevel(),
            es -> Shapes.cylinder(base, radius, height, hollow, v -> es.plan(v, pattern.next(v))),
            n -> player.sendMessage("§dFastEdit §7| cyl placed §f" + n + "§7 blocks."),
            t -> player.sendMessage("§c[FastEdit] " + t.getMessage()),
            session.undo());
        return true;
    }
}
