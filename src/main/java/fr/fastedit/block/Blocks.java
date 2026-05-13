package fr.fastedit.block;

import cn.nukkit.block.BlockProperties;
import cn.nukkit.block.BlockState;
import cn.nukkit.registry.Registries;

public final class Blocks {

    private Blocks() {}

    public static final String AIR_ID = "minecraft:air";

    public static BlockState air() { return state(AIR_ID); }

    /**
     * Resolve a block identifier into its default state.
     * Accepts "stone" (defaulted to minecraft:stone), "minecraft:stone", or
     * any namespaced custom id registered on the server.
     */
    public static BlockState state(String token) {
        if (token == null || token.isBlank()) return null;
        String id = normalize(token.trim());
        BlockProperties props = Registries.BLOCK.getBlockProperties(id);
        return props == null ? null : props.getDefaultState();
    }

    public static String normalize(String token) {
        return token.contains(":") ? token : "minecraft:" + token;
    }
}
