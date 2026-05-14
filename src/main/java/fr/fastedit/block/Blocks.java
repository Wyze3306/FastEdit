package fr.fastedit.block;

import cn.nukkit.block.BlockProperties;
import cn.nukkit.block.BlockState;
import cn.nukkit.registry.Registries;

public final class Blocks {

    private Blocks() {}

    public static final String AIR_ID         = "minecraft:air";
    public static final String PLACEHOLDER_ID = "minecraft:magenta_wool";

    public static BlockState air()         { return state(AIR_ID); }
    public static BlockState placeholder() {
        BlockState s = state(PLACEHOLDER_ID);
        return s == null ? air() : s;
    }

    public static BlockState state(String token) {
        if (token == null || token.isBlank()) return null;
        String id = normalize(token.trim());
        try {
            BlockProperties props = Registries.BLOCK.getBlockProperties(id);
            return props == null ? null : props.getDefaultState();
        } catch (IllegalArgumentException unknown) {
            return null;
        }
    }

    public static String normalize(String token) {
        return token.contains(":") ? token : "minecraft:" + token;
    }
}
