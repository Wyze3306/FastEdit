package fr.fastedit.clipboard;

import java.util.HashMap;
import java.util.Map;

final class LegacyIds {

    private static final Map<Integer, String> MAP = new HashMap<>();

    static {
        put(0, "air");                   put(1, "stone");                 put(2, "grass_block");
        put(3, "dirt");                  put(4, "cobblestone");           put(5, "oak_planks");
        put(6, "oak_sapling");           put(7, "bedrock");               put(8, "water");
        put(9, "water");                 put(10, "lava");                 put(11, "lava");
        put(12, "sand");                 put(13, "gravel");               put(14, "gold_ore");
        put(15, "iron_ore");             put(16, "coal_ore");             put(17, "oak_log");
        put(18, "oak_leaves");           put(19, "sponge");               put(20, "glass");
        put(21, "lapis_ore");            put(22, "lapis_block");          put(23, "dispenser");
        put(24, "sandstone");            put(25, "noteblock");            put(27, "powered_rail");
        put(28, "detector_rail");        put(29, "sticky_piston");        put(30, "cobweb");
        put(31, "tall_grass");           put(32, "deadbush");             put(33, "piston");
        put(35, "white_wool");           put(37, "yellow_flower");        put(38, "red_flower");
        put(39, "brown_mushroom");       put(40, "red_mushroom");         put(41, "gold_block");
        put(42, "iron_block");           put(43, "stone_slab");           put(44, "stone_slab");
        put(45, "brick_block");          put(46, "tnt");                  put(47, "bookshelf");
        put(48, "mossy_cobblestone");    put(49, "obsidian");             put(50, "torch");
        put(51, "fire");                 put(52, "mob_spawner");          put(53, "oak_stairs");
        put(54, "chest");                put(56, "diamond_ore");          put(57, "diamond_block");
        put(58, "crafting_table");       put(60, "farmland");             put(61, "furnace");
        put(62, "lit_furnace");          put(63, "standing_sign");        put(64, "wooden_door");
        put(65, "ladder");               put(66, "rail");                 put(67, "cobblestone_stairs");
        put(68, "wall_sign");            put(73, "redstone_ore");         put(74, "lit_redstone_ore");
        put(78, "snow_layer");           put(79, "ice");                  put(80, "snow");
        put(81, "cactus");               put(82, "clay");                 put(85, "fence");
        put(86, "pumpkin");              put(87, "netherrack");           put(88, "soul_sand");
        put(89, "glowstone");            put(91, "lit_pumpkin");          put(98, "stonebrick");
        put(102, "glass_pane");          put(103, "melon_block");         put(110, "mycelium");
        put(112, "nether_brick");        put(121, "end_stone");           put(129, "emerald_ore");
        put(133, "emerald_block");       put(155, "quartz_block");        put(159, "white_glazed_terracotta");
        put(168, "prismarine");          put(169, "sea_lantern");         put(170, "hay_block");
        put(172, "hardened_clay");       put(173, "coal_block");          put(174, "packed_ice");
        put(179, "red_sandstone");       put(251, "concrete");            put(252, "concrete_powder");
    }

    private static void put(int id, String suffix) { MAP.put(id, "minecraft:" + suffix); }

    static String lookup(int id) { return MAP.get(id); }
}
