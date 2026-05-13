package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.block.Pattern;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;
import fr.fastedit.shape.Shapes;

public final class SphereCmd extends Cmd {
    public SphereCmd() { super("/sphere", "Sphere at your feet.", "//sphere <pattern> <radius> [hollow]"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        require(args.length >= 2, "usage: //sphere <pattern> <radius> [hollow]");
        Pattern pattern = Pattern.parse(args[0]);
        double radius = Double.parseDouble(args[1]);
        boolean hollow = args.length > 2 && args[2].equalsIgnoreCase("hollow");

        Vec3 center = new Vec3(player.getFloorX(), player.getFloorY(), player.getFloorZ());
        EditEngine.get().submit(player.getLevel(),
            es -> Shapes.sphere(center, radius, hollow, v -> es.plan(v, pattern.next(v))),
            n -> player.sendMessage("§dFastEdit §7| sphere placed §f" + n + "§7 blocks."),
            t -> player.sendMessage("§c[FastEdit] " + t.getMessage()),
            session.undo());
        return true;
    }
}
