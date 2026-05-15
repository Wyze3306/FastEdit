package fr.fastedit.clipboard;

import cn.nukkit.registry.Registries;

import java.util.Set;
import java.util.TreeSet;

/**
 * Cached view of the server's real Bedrock block-id registry. Built lazily on
 * first use (the registry is empty until the server has booted), then frozen.
 */
public final class BedrockIds {

    private BedrockIds() {}

    private static volatile Set<String> IDS;          // full "minecraft:x" ids
    private static volatile String[] NAMES;           // prefix-stripped, sorted

    private static void ensure() {
        if (IDS != null) return;
        synchronized (BedrockIds.class) {
            if (IDS != null) return;
            Set<String> ids;
            try {
                ids = new TreeSet<>(Registries.BLOCK.getKeySet());
            } catch (Throwable t) {
                return;                                // registry not ready yet
            }
            if (ids.isEmpty()) return;
            TreeSet<String> names = new TreeSet<>();
            for (String id : ids) names.add(id.startsWith("minecraft:") ? id.substring(10) : id);
            IDS = ids;
            NAMES = names.toArray(new String[0]);
        }
    }

    public static boolean has(String id) {
        ensure();
        return IDS != null && IDS.contains(id);
    }

    public static boolean ready() {
        ensure();
        return IDS != null;
    }

    /** All block names without the {@code minecraft:} prefix; empty if not ready. */
    public static String[] names() {
        ensure();
        return NAMES != null ? NAMES : new String[0];
    }
}
