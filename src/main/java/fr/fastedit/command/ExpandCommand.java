package fr.fastedit.command;

import cn.nukkit.Player;
import cn.nukkit.level.Level;
import fr.fastedit.math.Region;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;

public class ExpandCommand extends FeCommand {
    public ExpandCommand() { super("expand", "Grow the selection.");
        params(txt("amount", false),
               enm("direction", true, "me","north","south","east","west","up","down"));
    }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        require(args.length >= 1, "usage: //expand <amount> [reverse] [dir[,dir...]]  |  //expand vert");
        require(session.hasSelection(), "no selection — set pos1/pos2 first");
        Level level = session.level();
        Region r = session.region();

        if (args[0].equalsIgnoreCase("vert") || args[0].equalsIgnoreCase("vertical")) {
            int minY = level.getMinHeight(), maxY = level.getMaxHeight();
            session.setPos1(level, new Vec3(r.min().x(), minY, r.min().z()));
            session.setPos2(level, new Vec3(r.max().x(), maxY, r.max().z()));
            p.sendMessage("§dFastEdit §7| expanded vertically §f" + minY + " §7-> §f" + maxY);
            return true;
        }

        int amount = parseInt(args[0], "amount");
        int reverse = 0;
        String dirArg;
        if (args.length >= 2 && isInt(args[1])) {
            reverse = parseInt(args[1], "reverse-amount");
            dirArg = args.length >= 3 ? args[2] : null;
        } else {
            dirArg = args.length >= 2 ? args[1] : null;
        }

        Region nr = r;
        for (String tok : (dirArg == null ? "me" : dirArg).split(",")) {
            Vec3 dir = parseDir(p, tok.trim());
            nr = expand(nr, dir, amount);
            if (reverse != 0) nr = expand(nr, dir.mul(-1), reverse);
        }

        session.setPos1(level, nr.min());
        session.setPos2(level, nr.max());
        p.sendMessage("§dFastEdit §7| §f" + nr.min() + " §7-> §f" + nr.max()
            + " §7(" + nr.width() + "×" + nr.height() + "×" + nr.length() + ")");
        return true;
    }

    public static Region expand(Region r, Vec3 dir, int amount) {
        int x0 = r.min().x(), y0 = r.min().y(), z0 = r.min().z();
        int x1 = r.max().x(), y1 = r.max().y(), z1 = r.max().z();
        if (dir.x() > 0) x1 += amount; else if (dir.x() < 0) x0 -= amount;
        if (dir.y() > 0) y1 += amount; else if (dir.y() < 0) y0 -= amount;
        if (dir.z() > 0) z1 += amount; else if (dir.z() < 0) z0 -= amount;
        return new Region(new Vec3(x0, y0, z0), new Vec3(x1, y1, z1));
    }

    static Vec3 parseDir(Player p, String name) {
        return switch (name.toLowerCase()) {
            case "up", "u"             -> new Vec3(0, 1, 0);
            case "down", "d"           -> new Vec3(0, -1, 0);
            case "north", "n"          -> new Vec3(0, 0, -1);
            case "south", "s"          -> new Vec3(0, 0, 1);
            case "east", "e"           -> new Vec3(1, 0, 0);
            case "west", "w"           -> new Vec3(-1, 0, 0);
            case "me", "forward", "f"  -> lookStep(p);
            case "back", "b"           -> lookStep(p).mul(-1);
            default -> throw new IllegalArgumentException("unknown direction: " + name);
        };
    }

    public static Vec3 lookStep(Player p) {
        double yaw = Math.toRadians(p.getYaw());
        double pitch = Math.toRadians(p.getPitch());
        double dx = -Math.sin(yaw) * Math.cos(pitch);
        double dy = -Math.sin(pitch);
        double dz =  Math.cos(yaw) * Math.cos(pitch);
        double ax = Math.abs(dx), ay = Math.abs(dy), az = Math.abs(dz);
        if (ay >= ax && ay >= az) return new Vec3(0, dy < 0 ? -1 : 1, 0);
        if (ax >= az)             return new Vec3(dx < 0 ? -1 : 1, 0, 0);
        return new Vec3(0, 0, dz < 0 ? -1 : 1);
    }

    public static String dirName(Vec3 d) {
        if (d.y() > 0) return "up";
        if (d.y() < 0) return "down";
        if (d.z() < 0) return "north";
        if (d.z() > 0) return "south";
        if (d.x() > 0) return "east";
        if (d.x() < 0) return "west";
        return "?";
    }

    private static boolean isInt(String s) {
        if (s == null || s.isEmpty()) return false;
        int i = (s.charAt(0) == '-' || s.charAt(0) == '+') ? 1 : 0;
        if (i == s.length()) return false;
        for (; i < s.length(); i++) if (!Character.isDigit(s.charAt(i))) return false;
        return true;
    }

    private static int parseInt(String s, String what) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("invalid " + what + ": " + s); }
    }
}
