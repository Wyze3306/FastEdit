package fr.fastedit.clipboard;

import cn.nukkit.block.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * Legacy 1.12 {@code (id, data)} → modern Java id + state, then Bedrock via
 * {@link JavaStates#resolve}. Covers the block families that actually carry
 * orientation/variant/colour in old {@code .schematic} files (stairs, logs,
 * slabs, wool/glass/clay/concrete/carpet colours, planks/leaves species,
 * doors/trapdoors, furnaces/chests/dispensers facing). Anything not handled
 * here falls through to {@link LegacyIds} (base id, default state) and finally
 * to a placeholder — never a hard failure.
 */
final class LegacyFlatten {

    private LegacyFlatten() {}

    private static final String[] COLOR = {
        "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
        "gray", "light_gray", "cyan", "purple", "blue", "brown", "green",
        "red", "black"
    };
    private static final String[] WOOD = {
        "oak", "spruce", "birch", "jungle", "acacia", "dark_oak"
    };
    /** Legacy stair data low 2 bits → Java facing. */
    private static final String[] STAIR_FACING = { "east", "west", "south", "north" };
    /** Legacy horizontal facing (2..5) → Java facing. */
    private static final String[] FACE_2_5 = { null, null, "north", "south", "west", "east" };

    /** @return Bedrock state, or {@code null} to let the caller fall back. */
    static BlockState translate(int id, int data) {
        int d = data & 0xFF;
        switch (id) {
            // ---- colours -------------------------------------------------
            case 35:  return res(COLOR[d & 15] + "_wool");
            case 95:  return res(COLOR[d & 15] + "_stained_glass");
            case 160: return res(COLOR[d & 15] + "_stained_glass_pane");
            case 159: return res(COLOR[d & 15] + "_terracotta");
            case 171: return res(COLOR[d & 15] + "_carpet");
            case 251: return res(COLOR[d & 15] + "_concrete");
            case 252: return res(COLOR[d & 15] + "_concrete_powder");

            // ---- wood species -------------------------------------------
            case 5:   return res(WOOD[Math.min(d & 7, 5)] + "_planks");
            case 6:   return res(WOOD[Math.min(d & 7, 5)] + "_sapling");
            case 17:  return logState(WOOD[d & 3], d);
            case 162: return logState((d & 1) == 0 ? "acacia" : "dark_oak", d);
            case 18:  return res(WOOD[d & 3] + "_leaves");
            case 161: return res(((d & 1) == 0 ? "acacia" : "dark_oak") + "_leaves");

            // ---- stairs (data: 0-3 facing, +4 = upside/top) --------------
            case 53:  return stair("oak_stairs", d);
            case 67:  return stair("cobblestone_stairs", d);
            case 108: return stair("brick_stairs", d);
            case 109: return stair("stone_brick_stairs", d);
            case 114: return stair("nether_brick_stairs", d);
            case 128: return stair("sandstone_stairs", d);
            case 134: return stair("spruce_stairs", d);
            case 135: return stair("birch_stairs", d);
            case 136: return stair("jungle_stairs", d);
            case 156: return stair("quartz_stairs", d);
            case 163: return stair("acacia_stairs", d);
            case 164: return stair("dark_oak_stairs", d);
            case 180: return stair("red_sandstone_stairs", d);
            case 203: return stair("purpur_stairs", d);

            // ---- slabs (bit 8 = top half) -------------------------------
            case 44:  return slab(stoneSlab(d & 7), d);
            case 126: return slab(WOOD[Math.min(d & 7, 5)] + "_slab", d);
            case 182: return slab("red_sandstone_slab", d);
            case 205: return slab("purpur_slab", d);

            // ---- doors (64 oak, 71 iron, 193-197 species) ---------------
            case 64:  return door("oak_door", d);
            case 71:  return door("iron_door", d);
            case 193: return door("spruce_door", d);
            case 194: return door("birch_door", d);
            case 195: return door("jungle_door", d);
            case 196: return door("acacia_door", d);
            case 197: return door("dark_oak_door", d);

            // ---- trapdoors ----------------------------------------------
            case 96:  return trapdoor("oak_trapdoor", d);
            case 167: return trapdoor("iron_trapdoor", d);

            // ---- horizontal-facing blocks (data 2-5) --------------------
            case 23:  return facing("dispenser", d);
            case 158: return facing("dropper", d);
            case 54:  return facing("chest", d);
            case 146: return facing("trapped_chest", d);
            case 130: return facing("ender_chest", d);
            case 61:  return facing("furnace", d);
            case 62:  return facing("furnace", d);          // lit furnace → furnace
            case 86:  return facingHoriz("carved_pumpkin", d);
            case 91:  return facingHoriz("jack_o_lantern", d);

            default:  return null;
        }
    }

    // ---- helpers ----------------------------------------------------------

    private static BlockState res(String javaId) {
        return JavaStates.resolve("minecraft:" + javaId, Map.of());
    }

    private static BlockState logState(String species, int d) {
        Map<String, String> p = new HashMap<>();
        int axis = (d >> 2) & 3;
        p.put("axis", axis == 1 ? "x" : axis == 2 ? "z" : "y");
        return JavaStates.resolve("minecraft:" + species + "_log", p);
    }

    private static BlockState stair(String javaId, int d) {
        Map<String, String> p = new HashMap<>();
        p.put("facing", STAIR_FACING[d & 3]);
        p.put("half", (d & 4) != 0 ? "top" : "bottom");
        return JavaStates.resolve("minecraft:" + javaId, p);
    }

    private static BlockState slab(String javaId, int d) {
        Map<String, String> p = new HashMap<>();
        p.put("type", (d & 8) != 0 ? "top" : "bottom");
        return JavaStates.resolve("minecraft:" + javaId, p);
    }

    private static BlockState door(String javaId, int d) {
        Map<String, String> p = new HashMap<>();
        if ((d & 8) != 0) {                       // upper half
            p.put("half", "upper");
            p.put("hinge", (d & 1) != 0 ? "right" : "left");
        } else {                                  // lower half
            p.put("half", "lower");
            p.put("open", String.valueOf((d & 4) != 0));
        }
        return JavaStates.resolve("minecraft:" + javaId, p);
    }

    private static BlockState trapdoor(String javaId, int d) {
        Map<String, String> p = new HashMap<>();
        p.put("half", (d & 8) != 0 ? "top" : "bottom");
        p.put("open", String.valueOf((d & 4) != 0));
        return JavaStates.resolve("minecraft:" + javaId, p);
    }

    private static BlockState facing(String javaId, int d) {
        String f = (d >= 2 && d <= 5) ? FACE_2_5[d] : null;
        Map<String, String> p = f == null ? Map.of() : Map.of("facing", f);
        return JavaStates.resolve("minecraft:" + javaId, p);
    }

    private static BlockState facingHoriz(String javaId, int d) {
        // legacy pumpkin: 0=south 1=west 2=north 3=east
        String[] f = { "south", "west", "north", "east" };
        return JavaStates.resolve("minecraft:" + javaId, Map.of("facing", f[d & 3]));
    }

    private static String stoneSlab(int v) {
        return switch (v) {
            case 1  -> "sandstone_slab";
            case 2  -> "oak_slab";
            case 3  -> "cobblestone_slab";
            case 4  -> "brick_slab";
            case 5  -> "stone_brick_slab";
            case 6  -> "nether_brick_slab";
            case 7  -> "quartz_slab";
            default -> "smooth_stone_slab";
        };
    }
}
