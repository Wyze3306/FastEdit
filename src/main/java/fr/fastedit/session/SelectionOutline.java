package fr.fastedit.session;

import fr.fastedit.math.Vec3;
import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.level.Level;
import org.powernukkitx.math.Vector3;
import org.powernukkitx.network.primitiveshape.BoxShape;
import org.powernukkitx.network.primitiveshape.PrimitiveShapes;
import org.powernukkitx.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Displays each player's selection as a private Bedrock primitive box.
 *
 * <p>The primitive stays client-side until the selection changes or is hidden,
 * so an unchanged outline does not generate any recurring network traffic.
 * With only one corner set, the corresponding single block is outlined.</p>
 */
public final class SelectionOutline {

    private static final int PERIOD_TICKS = 10;
    private static final float VIEW_DISTANCE = 64.0f;
    private static final Map<UUID, OutlineState> OUTLINES = new HashMap<>();

    private SelectionOutline() {}

    public static void boot(Plugin plugin) {
        plugin.getServer().getScheduler()
            .scheduleRepeatingTask(plugin, SelectionOutline::tick, PERIOD_TICKS);
    }

    /** Removes FastEdit's shapes when the plugin is disabled or reloaded. */
    public static void shutdown() {
        for (Player player : Server.getInstance().getOnlinePlayers().values()) {
            remove(player);
        }
        OUTLINES.clear();
    }

    private static void tick() {
        Set<UUID> online = new HashSet<>();
        for (Player player : Server.getInstance().getOnlinePlayers().values()) {
            UUID playerId = player.getUniqueId();
            online.add(playerId);

            Session session = SessionManager.get().ofOrNull(playerId);
            if (!canDisplay(player, session)) {
                remove(player);
                continue;
            }

            Vec3 pos1 = session.pos1();
            Vec3 pos2 = session.pos2();
            Vec3 a = pos1 != null ? pos1 : pos2;
            Vec3 b = pos2 != null ? pos2 : pos1;
            Vec3 min = a.min(b);
            Vec3 max = a.max(b);

            OutlineState current = OUTLINES.get(playerId);
            if (current != null
                && current.level() == session.level()
                && current.min().equals(min)
                && current.max().equals(max)) {
                continue;
            }

            try {
                BoxShape box = box(session.level(), min, max);
                int networkId;
                if (current == null) {
                    networkId = box.showTo(player);
                } else {
                    networkId = current.networkId();
                    box.updateFor(player, networkId);
                }
                OUTLINES.put(playerId, new OutlineState(networkId, session.level(), min, max));
            } catch (Throwable ignored) {
                // A malformed or unsupported primitive must not stop the scheduler.
            }
        }

        // A disconnected client has already discarded its primitives. Forget its
        // network id so a reconnect creates a fresh shape for the new connection.
        OUTLINES.keySet().removeIf(playerId -> !online.contains(playerId));
    }

    private static boolean canDisplay(Player player, Session session) {
        if (session == null || !session.outlineVisible()) return false;
        if (session.level() == null || session.level() != player.getLevel()) return false;
        return session.pos1() != null || session.pos2() != null;
    }

    private static BoxShape box(Level level, Vec3 min, Vec3 max) {
        double width = max.x() - min.x() + 1.0;
        double height = max.y() - min.y() + 1.0;
        double length = max.z() - min.z() + 1.0;

        // Primitive boxes are centered on their location. Their bound is the
        // full size, so this wraps the complete volume of every selected block.
        Vector3 center = new Vector3(
            min.x() + width / 2.0,
            min.y() + height / 2.0,
            min.z() + length / 2.0);
        Vector3 bound = new Vector3(width, height, length);

        return PrimitiveShapes.box(center, bound)
            .color(255, 255, 255, 255)
            .maxRenderDistance(VIEW_DISTANCE)
            .dimension(level.getDimension());
    }

    private static void remove(Player player) {
        OutlineState state = OUTLINES.remove(player.getUniqueId());
        if (state == null) return;
        try {
            PrimitiveShapes.remove(player, state.networkId());
        } catch (Throwable ignored) {
            // The player may be in the process of disconnecting.
        }
    }

    private record OutlineState(int networkId, Level level, Vec3 min, Vec3 max) {}
}
