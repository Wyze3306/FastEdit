package fr.fastedit.command;

import cn.nukkit.Player;
import cn.nukkit.block.BlockState;
import cn.nukkit.level.Level;
import fr.fastedit.block.Blocks;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.math.Region;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;
import fr.fastedit.shape.Shapes;

public final class MoveCmd extends Cmd {
    public MoveCmd() { super("/move", "Move selection along a direction.", "//move <amount> [direction]"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        require(args.length >= 1, "usage: //move <amount> [up|down|north|south|east|west|me]");
        require(session.hasSelection(), "no selection");
        int amount = Integer.parseInt(args[0]);
        String dir = args.length > 1 ? args[1].toLowerCase() : "me";

        Vec3 step = direction(player, dir).mul(amount);
        Region r = session.region();
        Level level = session.level();

        BlockState[] snapshot = new BlockState[(int) r.volume()];
        int idx = 0;
        for (int y = 0; y < r.height(); y++)
            for (int z = 0; z < r.length(); z++)
                for (int x = 0; x < r.width(); x++)
                    snapshot[idx++] = level.getBlockStateAt(r.min().x() + x, r.min().y() + y, r.min().z() + z);

        BlockState air = Blocks.air();
        EditEngine.get().submit(level,
            es -> {
                Shapes.cuboid(r, v -> es.plan(v, air));
                int i = 0;
                for (int y = 0; y < r.height(); y++)
                    for (int z = 0; z < r.length(); z++)
                        for (int x = 0; x < r.width(); x++) {
                            BlockState s = snapshot[i++];
                            if (s == null) continue;
                            es.plan(new Vec3(
                                r.min().x() + x + step.x(),
                                r.min().y() + y + step.y(),
                                r.min().z() + z + step.z()), s);
                        }
            },
            n -> player.sendMessage("§dFastEdit §7| moved §f" + n + "§7 blocks."),
            t -> player.sendMessage("§c[FastEdit] " + t.getMessage()),
            session.undo());

        session.setPos1(level, r.min().add(step));
        session.setPos2(level, r.max().add(step));
        return true;
    }

    static Vec3 direction(Player player, String dir) {
        return switch (dir) {
            case "up", "u"          -> new Vec3(0, 1, 0);
            case "down", "d"        -> new Vec3(0, -1, 0);
            case "north", "n"       -> new Vec3(0, 0, -1);
            case "south", "s"       -> new Vec3(0, 0, 1);
            case "east", "e"        -> new Vec3(1, 0, 0);
            case "west", "w"        -> new Vec3(-1, 0, 0);
            case "me", "forward", "f" -> facing(player);
            default -> throw new IllegalArgumentException("unknown direction: " + dir);
        };
    }

    private static Vec3 facing(Player player) {
        double yaw = Math.toRadians(player.getYaw());
        int dx = (int) Math.round(-Math.sin(yaw));
        int dz = (int) Math.round( Math.cos(yaw));
        if (dx == 0 && dz == 0) return new Vec3(0, 1, 0);
        return new Vec3(Integer.signum(dx), 0, Integer.signum(dz));
    }
}
