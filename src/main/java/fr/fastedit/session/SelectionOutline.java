package fr.fastedit.session;

import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.level.Level;
import org.powernukkitx.level.ParticleEffect;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.plugin.Plugin;
import org.powernukkitx.utils.MolangVariableMap;
import fr.fastedit.math.Vec3;

/**
 * Draws each player's current selection as a white particle box (endrod),
 * WorldEdit-CUI style. Particles are sent only to the selection's owner, so
 * outlines are private and cost nothing for everyone else. With only pos1
 * set, the single block is outlined so the first corner is visible too.
 */
public final class SelectionOutline {

    private static final int PERIOD_TICKS = 10;
    /** Ideal spacing between points; stretched on huge selections. */
    private static final double STEP = 0.5;
    /** Hard cap on particles per player per refresh. */
    private static final int MAX_POINTS = 600;
    /** Beyond this distance the client would not render them anyway. */
    private static final double VIEW_DISTANCE = 64.0;

    private SelectionOutline() {}

    public static void boot(Plugin plugin) {
        plugin.getServer().getScheduler()
            .scheduleRepeatingTask(plugin, SelectionOutline::tick, PERIOD_TICKS);
    }

    private static void tick() {
        for (Player p : Server.getInstance().getOnlinePlayers().values()) {
            Session s = SessionManager.get().ofOrNull(p.getUniqueId());
            if (s == null || !s.outlineVisible()) continue;
            if (s.level() == null || s.level() != p.getLevel()) continue;
            Vec3 p1 = s.pos1(), p2 = s.pos2();
            if (p1 == null && p2 == null) continue;
            Vec3 a = p1 != null ? p1 : p2;
            Vec3 b = p2 != null ? p2 : p1;
            try { draw(p, a.min(b), a.max(b)); } catch (Throwable ignored) {}
        }
    }

    private static void draw(Player p, Vec3 min, Vec3 max) {
        // The outline wraps the selected blocks, hence max + 1 on every axis.
        double x0 = min.x(), y0 = min.y(), z0 = min.z();
        double x1 = max.x() + 1, y1 = max.y() + 1, z1 = max.z() + 1;

        double nx = clamp(p.getX(), x0, x1) - p.getX();
        double ny = clamp(p.getY(), y0, y1) - p.getY();
        double nz = clamp(p.getZ(), z0, z1) - p.getZ();
        if (nx * nx + ny * ny + nz * nz > VIEW_DISTANCE * VIEW_DISTANCE) return;

        double perimeter = 4 * ((x1 - x0) + (y1 - y0) + (z1 - z0));
        double step = Math.max(STEP, perimeter / MAX_POINTS);

        double[][] edges = {
            {x0, y0, z0, x1, y0, z0}, {x0, y1, z0, x1, y1, z0},
            {x0, y0, z1, x1, y0, z1}, {x0, y1, z1, x1, y1, z1},
            {x0, y0, z0, x0, y1, z0}, {x1, y0, z0, x1, y1, z0},
            {x0, y0, z1, x0, y1, z1}, {x1, y0, z1, x1, y1, z1},
            {x0, y0, z0, x0, y0, z1}, {x1, y0, z0, x1, y0, z1},
            {x0, y1, z0, x0, y1, z1}, {x1, y1, z0, x1, y1, z1},
        };
        for (double[] e : edges) line(p, e, step);
    }

    private static void line(Player p, double[] e, double step) {
        // Edges are axis-aligned, so the Manhattan distance is the length.
        double len = Math.abs(e[3] - e[0]) + Math.abs(e[4] - e[1]) + Math.abs(e[5] - e[2]);
        int n = Math.max(1, (int) Math.round(len / step));
        double r2 = VIEW_DISTANCE * VIEW_DISTANCE;
        Level lvl = p.getLevel();
        for (int i = 0; i <= n; i++) {
            double t = (double) i / n;
            double x = e[0] + (e[3] - e[0]) * t;
            double y = e[1] + (e[4] - e[1]) * t;
            double z = e[2] + (e[5] - e[2]) * t;
            double dx = x - p.getX(), dy = y - p.getY(), dz = z - p.getZ();
            if (dx * dx + dy * dy + dz * dz > r2) continue;
            lvl.addParticleEffect(new Vector3(x, y, z),
                ParticleEffect.ENDROD, -1L, (MolangVariableMap) null, p);
        }
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : Math.min(v, hi);
    }
}
