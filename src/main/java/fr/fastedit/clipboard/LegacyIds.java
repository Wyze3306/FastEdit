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

        // extended coverage (base ids; orientation/variant handled in LegacyFlatten)
        put(26, "red_bed");              put(34, "piston_head");          put(36, "moving_piston");
        put(55, "redstone_wire");        put(59, "wheat");                put(69, "lever");
        put(70, "stone_pressure_plate"); put(72, "oak_pressure_plate");   put(75, "unlit_redstone_torch");
        put(76, "redstone_torch");       put(77, "stone_button");         put(83, "sugar_cane");
        put(84, "jukebox");              put(90, "nether_portal");        put(92, "cake");
        put(93, "repeater");             put(94, "repeater");             put(96, "oak_trapdoor");
        put(97, "infested_stone");       put(99, "brown_mushroom_block"); put(100, "red_mushroom_block");
        put(101, "iron_bars");           put(104, "pumpkin_stem");        put(105, "melon_stem");
        put(106, "vine");                put(107, "oak_fence_gate");      put(108, "brick_stairs");
        put(109, "stone_brick_stairs");  put(111, "lily_pad");            put(113, "nether_brick_fence");
        put(114, "nether_brick_stairs"); put(115, "nether_wart");         put(116, "enchanting_table");
        put(117, "brewing_stand");       put(118, "cauldron");            put(119, "end_portal");
        put(120, "end_portal_frame");    put(122, "dragon_egg");          put(123, "redstone_lamp");
        put(124, "lit_redstone_lamp");   put(125, "oak_double_slab");     put(126, "oak_slab");
        put(127, "cocoa");               put(128, "sandstone_stairs");    put(130, "ender_chest");
        put(131, "tripwire_hook");       put(132, "tripwire");            put(134, "spruce_stairs");
        put(135, "birch_stairs");        put(136, "jungle_stairs");       put(137, "command_block");
        put(138, "beacon");              put(139, "cobblestone_wall");    put(140, "flower_pot");
        put(141, "carrots");             put(142, "potatoes");            put(143, "oak_button");
        put(145, "anvil");               put(146, "trapped_chest");       put(147, "light_weighted_pressure_plate");
        put(148, "heavy_weighted_pressure_plate"); put(149, "comparator"); put(150, "comparator");
        put(151, "daylight_detector");   put(152, "redstone_block");      put(153, "quartz_ore");
        put(154, "hopper");              put(156, "quartz_stairs");       put(157, "activator_rail");
        put(158, "dropper");             put(161, "acacia_leaves");       put(162, "acacia_log");
        put(163, "acacia_stairs");       put(164, "dark_oak_stairs");     put(165, "slime_block");
        put(166, "barrier");             put(167, "iron_trapdoor");       put(175, "sunflower");
        put(176, "standing_banner");     put(177, "wall_banner");         put(178, "daylight_detector");
        put(180, "red_sandstone_stairs");put(181, "red_sandstone_double_slab"); put(182, "red_sandstone_slab");
        put(183, "spruce_fence_gate");   put(184, "birch_fence_gate");    put(185, "jungle_fence_gate");
        put(186, "dark_oak_fence_gate"); put(187, "acacia_fence_gate");   put(188, "spruce_fence");
        put(189, "birch_fence");         put(190, "jungle_fence");        put(191, "dark_oak_fence");
        put(192, "acacia_fence");        put(193, "spruce_door");         put(194, "birch_door");
        put(195, "jungle_door");         put(196, "acacia_door");         put(197, "dark_oak_door");
        put(198, "end_rod");             put(199, "chorus_plant");        put(200, "chorus_flower");
        put(201, "purpur_block");        put(202, "purpur_pillar");       put(203, "purpur_stairs");
        put(204, "purpur_double_slab");  put(205, "purpur_slab");         put(206, "end_brick");
        put(207, "beetroots");           put(208, "dirt_path");           put(209, "end_gateway");
        put(210, "repeating_command_block"); put(211, "chain_command_block"); put(212, "frosted_ice");
        put(213, "magma");               put(214, "nether_wart_block");   put(215, "red_nether_brick");
        put(216, "bone_block");          put(218, "observer");            put(255, "structure_block");
    }

    private static void put(int id, String suffix) { MAP.put(id, "minecraft:" + suffix); }

    static String lookup(int id) { return MAP.get(id); }
}
