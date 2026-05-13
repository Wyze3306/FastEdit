package fr.fastedit.block;

import cn.nukkit.block.BlockProperties;
import cn.nukkit.block.BlockState;
import cn.nukkit.registry.Registries;

public final class Blocks {

    private Blocks() {}

    public static final String AIR_ID = "minecraft:air";

    public static BlockState air() { return state(AIR_ID); }

    public static BlockState state(String token) {
        if (token == null || token.isBlank()) return null;
        BlockProperties props = Registries.BLOCK.getBlockProperties(normalize(token.trim()));
        return props == null ? null : props.getDefaultState();
    }

    public static String normalize(String token) {
        return token.contains(":") ? token : "minecraft:" + token;
    }
}
