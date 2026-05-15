package fr.fastedit.clipboard;

import cn.nukkit.block.BlockProperties;
import cn.nukkit.block.BlockState;
import cn.nukkit.block.property.enums.MinecraftCardinalDirection;
import cn.nukkit.block.property.enums.MinecraftVerticalHalf;
import cn.nukkit.block.property.type.BlockPropertyType;
import cn.nukkit.math.BlockFace;
import cn.nukkit.registry.Registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Best-effort Java → Bedrock block-state translation for Sponge {@code .schem}
 * palettes (e.g. {@code minecraft:oak_stairs[facing=east,half=top]}).
 *
 * <p>Strictly additive: introspects the target Bedrock block's real property
 * set and only sets properties it can map with confidence. Anything unknown
 * (exotic blocks, {@code waterlogged}, redstone wiring, multipart shapes,
 * legacy {@code .schematic} metadata) falls back to the block's default
 * state — i.e. the previous behaviour, never worse.</p>
 *
 * <p>Mapped: pillar axis (logs/pillars), stairs (facing+half → weirdo/upside),
 * slab top/bottom, horizontal/6-way facing (chests, furnaces, dispensers,
 * observers…), open bit (doors/trapdoors/gates), door upper/hinge bits.</p>
 */
public final class JavaStates {

    private JavaStates() {}

    /**
     * Resolves a modern Java id (+ optional state map) to a Bedrock state:
     * applies the {@link fr.fastedit.block.BlockAliases} id mapping then this
     * translator, falling back to the un-aliased id. {@code null} if unknown.
     */
    public static BlockState resolve(String javaId, Map<String, String> jp) {
        String mapped = fr.fastedit.block.BlockAliases.translate(javaId);
        BlockState st = apply(mapped, jp);
        if (st == null && !mapped.equals(javaId)) st = apply(javaId, jp);
        return st;
    }

    /** @return a Bedrock {@link BlockState}, or {@code null} if the id is unknown. */
    public static BlockState apply(String bedrockId, Map<String, String> jp) {
        BlockProperties props;
        try {
            props = Registries.BLOCK.getBlockProperties(bedrockId);
        } catch (Exception e) {
            return null;
        }
        if (props == null) return null;
        BlockState def = props.getDefaultState();
        if (jp == null || jp.isEmpty()) return def;

        Map<String, BlockPropertyType<?>> byName = new HashMap<>();
        for (BlockPropertyType<?> t : props.getPropertyTypeSet()) byName.put(t.getName(), t);

        List<BlockPropertyType.BlockPropertyValue<?, ?, ?>> vals = new ArrayList<>();

        String axis = jp.get("axis");
        if (axis != null) {
            try { add(byName, vals, "pillar_axis", BlockFace.Axis.valueOf(axis.toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }

        String facing = jp.get("facing");
        if (facing != null) {
            facing = facing.toLowerCase();
            if (byName.containsKey("weirdo_direction")) {
                add(byName, vals, "weirdo_direction", weirdo(facing));
            } else if (byName.containsKey("minecraft:cardinal_direction")) {
                MinecraftCardinalDirection c = cardinal(facing);
                if (c != null) add(byName, vals, "minecraft:cardinal_direction", c);
            } else if (byName.containsKey("facing_direction")) {
                add(byName, vals, "facing_direction", facing6(facing));
            }
        }

        // Stairs/trapdoor vertical orientation.
        String half = jp.get("half");
        if (half != null) {
            if (byName.containsKey("upside_down_bit"))
                add(byName, vals, "upside_down_bit", "top".equalsIgnoreCase(half));
            if (byName.containsKey("upper_block_bit")) // doors: upper|lower
                add(byName, vals, "upper_block_bit", "upper".equalsIgnoreCase(half));
        }

        // Slabs.
        String type = jp.get("type");
        if (type != null && byName.containsKey("minecraft:vertical_half")) {
            if ("top".equalsIgnoreCase(type))
                add(byName, vals, "minecraft:vertical_half", MinecraftVerticalHalf.TOP);
            else if ("bottom".equalsIgnoreCase(type))
                add(byName, vals, "minecraft:vertical_half", MinecraftVerticalHalf.BOTTOM);
        }

        String open = jp.get("open");
        if (open != null && byName.containsKey("open_bit"))
            add(byName, vals, "open_bit", Boolean.parseBoolean(open));

        String hinge = jp.get("hinge");
        if (hinge != null && byName.containsKey("door_hinge_bit"))
            add(byName, vals, "door_hinge_bit", "right".equalsIgnoreCase(hinge));

        if (vals.isEmpty()) return def;
        try {
            return props.getBlockState(
                vals.toArray(new BlockPropertyType.BlockPropertyValue[0]));
        } catch (Exception e) {
            return def;
        }
    }

    private static void add(Map<String, BlockPropertyType<?>> byName,
                            List<BlockPropertyType.BlockPropertyValue<?, ?, ?>> out,
                            String name, Object value) {
        BlockPropertyType<?> t = byName.get(name);
        if (t == null) return;
        try {
            BlockPropertyType.BlockPropertyValue<?, ?, ?> v = t.tryCreateValue(value);
            if (v != null) out.add(v);
        } catch (Exception ignored) {}
    }

    /** Bedrock weirdo_direction: 0=east 1=west 2=south 3=north. */
    private static int weirdo(String facing) {
        return switch (facing) {
            case "west"  -> 1;
            case "south" -> 2;
            case "north" -> 3;
            default      -> 0; // east
        };
    }

    /** Bedrock facing_direction: 0=down 1=up 2=north 3=south 4=west 5=east. */
    private static int facing6(String facing) {
        return switch (facing) {
            case "down"  -> 0;
            case "up"    -> 1;
            case "north" -> 2;
            case "south" -> 3;
            case "west"  -> 4;
            default      -> 5; // east
        };
    }

    private static MinecraftCardinalDirection cardinal(String facing) {
        return switch (facing) {
            case "north" -> MinecraftCardinalDirection.NORTH;
            case "south" -> MinecraftCardinalDirection.SOUTH;
            case "west"  -> MinecraftCardinalDirection.WEST;
            case "east"  -> MinecraftCardinalDirection.EAST;
            default      -> null;
        };
    }
}
