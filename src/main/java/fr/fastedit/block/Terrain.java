package fr.fastedit.block;

import org.powernukkitx.block.Block;
import org.powernukkitx.block.BlockState;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * What counts as <em>ground</em> when reshaping terrain.
 *
 * <p>Mirrors EasyEdit's {@code terrain-ignored-blocks}: liquids, plants, leaves
 * and tree trunks are see-through for the height field, so a smooth pass follows
 * the land instead of the forest standing on it. Everything else falls back to
 * the server's own {@code isSolid()}, cached per block id.
 */
public final class Terrain {

    private Terrain() {}

    private static final Map<String, Boolean> GROUND = new ConcurrentHashMap<>();

    /** Solid, but part of what grows on the ground rather than the ground itself. */
    private static final Set<String> IGNORED = Set.of(
        "leaves", "leaves2", "log", "log2", "wood",
        "snow_layer", "cactus", "bamboo", "bamboo_sapling",
        "brown_mushroom_block", "red_mushroom_block", "mushroom_stem",
        "chorus_plant", "chorus_flower", "beehive", "bee_nest");

    private static final String[] IGNORED_SUFFIX =
        {"_leaves", "_log", "_wood", "_stem", "_hyphae", "_roots"};

    public static boolean isAir(BlockState state) {
        return state == null || Blocks.AIR_ID.equals(state.getIdentifier());
    }

    public static boolean isLiquid(BlockState state) {
        if (state == null) return false;
        return switch (state.getIdentifier()) {
            case "minecraft:water", "minecraft:flowing_water",
                 "minecraft:lava",  "minecraft:flowing_lava" -> true;
            default -> false;
        };
    }

    public static boolean isGround(BlockState state) {
        if (state == null) return false;
        Boolean known = GROUND.get(state.getIdentifier());
        if (known != null) return known;
        boolean ground = compute(state);
        GROUND.put(state.getIdentifier(), ground);
        return ground;
    }

    private static boolean compute(BlockState state) {
        String id = state.getIdentifier();
        String name = id.startsWith("minecraft:") ? id.substring(10) : id;
        if (IGNORED.contains(name)) return false;
        for (String suffix : IGNORED_SUFFIX) if (name.endsWith(suffix)) return false;
        try {
            // Covers air, liquids and every flowable (grass, flowers, torches…).
            return Block.get(state).isSolid();
        } catch (Throwable unknownToTheServer) {
            return true;
        }
    }
}
