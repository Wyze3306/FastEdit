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
        put("snow",                   "snow");
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
    }

    private static void put(String javaName, String bedrockName) {
        JAVA_TO_BEDROCK.put("minecraft:" + javaName, "minecraft:" + bedrockName);
    }

    public static String translate(String javaId) {
        String mapped = JAVA_TO_BEDROCK.get(javaId);
        return mapped != null ? mapped : javaId;
    }
}
