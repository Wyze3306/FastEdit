package fr.fastedit.block;

import java.util.HashMap;
import java.util.Map;

public final class BlockAliases {

    private static final Map<String, String> JAVA_TO_BEDROCK = new HashMap<>();

    static {
        put("stonebrick",             "stone_bricks");
        put("terracotta",             "hardened_clay");
        put("stained_hardened_clay",  "hardened_clay");
        put("wool",                   "white_wool");
        put("planks",                 "oak_planks");
        put("log",                    "oak_log");
        put("log2",                   "acacia_log");
        put("leaves",                 "oak_leaves");
        put("leaves2",                "acacia_leaves");
        put("wooden_slab",            "oak_slab");
        put("double_wooden_slab",     "oak_double_slab");
        put("wooden_door",            "oak_door");
        put("wooden_button",          "oak_button");
        put("wooden_pressure_plate",  "oak_pressure_plate");
        put("wooden_trapdoor",        "oak_trapdoor");
        put("wooden_stairs",          "oak_stairs");
        put("fence",                  "oak_fence");
        put("fence_gate",             "oak_fence_gate");
        put("concrete",               "white_concrete");
        put("concrete_powder",        "white_concrete_powder");
        put("carpet",                 "white_carpet");
        put("stained_glass",          "white_stained_glass");
        put("stained_glass_pane",     "white_stained_glass_pane");
        put("bed",                    "white_bed");
        put("banner",                 "standing_banner");
        put("standing_banner",        "standing_banner");
        put("wall_banner",            "wall_banner");
        put("grass",                  "grass_block");
        put("grass_path",             "dirt_path");
        put("tallgrass",              "tall_grass");
        put("yellow_flower",          "dandelion");
        put("red_flower",             "poppy");
        put("waterlily",              "lily_pad");
        put("double_plant",           "tall_grass");
        put("snow_layer",             "snow_layer");
        put("ice",                    "ice");
        put("netherrack",             "netherrack");
        put("nether_brick",           "nether_brick");
        put("end_bricks",             "end_brick");
        put("end_brick",              "end_brick");
        put("end_brick_stairs",       "end_brick_stairs");
        put("purpur_block",           "purpur_block");
        put("redstone_lamp",          "redstone_lamp");
        put("lit_redstone_lamp",      "lit_redstone_lamp");
        put("snow",                   "snow_layer");
        put("snow_block",             "snow");
        put("mob_spawner",            "mob_spawner");
        put("monster_egg",            "infested_stone");
        put("hardened_glass",         "glass");
        put("hardened_glass_pane",    "glass_pane");
        put("cobble_wall",            "cobblestone_wall");
        put("cobblestone_wall",       "cobblestone_wall");
        put("frame",                  "frame");
        put("flowing_water",          "water");
        put("flowing_lava",           "lava");
        put("lit_furnace",            "furnace");
        put("lit_pumpkin",            "lit_pumpkin");
        put("noteblock",              "noteblock");
        put("daylight_detector_inverted", "daylight_detector");
        put("dispenser",              "dispenser");
        put("dropper",                "dropper");
        put("trapped_chest",          "trapped_chest");
        put("powered_repeater",       "repeater");
        put("unpowered_repeater",     "repeater");
        put("powered_comparator",     "comparator");
        put("unpowered_comparator",   "comparator");
        put("redstone_torch",         "redstone_torch");
        put("unlit_redstone_torch",   "unlit_redstone_torch");
        put("unpowered_torch",        "unlit_redstone_torch");
        put("redstone_wire",          "redstone_wire");
        put("piston_head",            "piston_arm_collision");
        put("sticky_piston_head",     "sticky_piston_arm_collision");

        // Modern Java → Bedrock identifier renames (id differs, not just state).
        put("rooted_dirt",            "dirt_with_roots");
        put("dirt_path",              "grass_path");
        put("lily_pad",               "waterlily");
        put("cobweb",                 "web");
        put("dead_bush",              "deadbush");
        put("melon",                  "melon_block");
        put("nether_quartz_ore",      "quartz_ore");
        put("magma_block",            "magma");
        put("note_block",             "noteblock");
        put("slime_block",            "slime");
        put("sugar_cane",             "reeds");
        put("beetroots",              "beetroot");
        put("powered_rail",           "golden_rail");
        put("tripwire",               "trip_wire");
        put("cobblestone_stairs",     "stone_stairs");
        put("light",                  "light_block");
        put("small_dripleaf",         "small_dripleaf_block");
        put("big_dripleaf_stem",      "big_dripleaf");
        put("waxed_copper_block",     "waxed_copper");
        put("spawner",                "mob_spawner");
        put("monster_spawner",        "mob_spawner");
        put("frogspawn",              "frog_spawn");
        // PNX 2.0 keeps split powered/unpowered redstone ids — override the
        // earlier collapsing aliases so the lit/unlit state is preserved.
        put("repeater",               "unpowered_repeater");
        put("comparator",             "unpowered_comparator");
        put("powered_comparator",     "powered_comparator");
        put("unpowered_comparator",   "unpowered_comparator");
        put("powered_repeater",       "powered_repeater");
        put("unpowered_repeater",     "unpowered_repeater");
    }

    private static void put(String javaName, String bedrockName) {
        JAVA_TO_BEDROCK.put("minecraft:" + javaName, "minecraft:" + bedrockName);
    }

    public static String translate(String javaId) {
        String mapped = JAVA_TO_BEDROCK.get(javaId);
        return mapped != null ? mapped : javaId;
    }
}
