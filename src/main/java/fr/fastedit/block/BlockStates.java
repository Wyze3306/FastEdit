package fr.fastedit.block;

import org.powernukkitx.block.BlockProperties;
import org.powernukkitx.block.BlockState;
import org.powernukkitx.block.property.CommonPropertyMap;
import org.powernukkitx.block.property.enums.MinecraftCardinalDirection;
import org.powernukkitx.block.property.enums.MinecraftVerticalHalf;
import org.powernukkitx.block.property.enums.TorchFacingDirection;
import org.powernukkitx.block.property.type.BlockPropertyType;
import org.powernukkitx.math.BlockFace;
import org.powernukkitx.registry.Registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keeps a block's orientation when its type changes.
 *
 * <p>A pattern token like {@code stone_brick_stairs} resolves to the block's
 * <em>default</em> state — east-facing, right side up. Writing that straight
 * over an existing staircase flattens every step into the same direction, and
 * the same happens to slabs (top/bottom), logs (axis), trapdoors, doors, walls…
 * So before writing, the old block's properties are replayed onto the new one:
 * first every property the two types share by name, then the facing / vertical
 * half families across the different names Bedrock gives them (stairs carry
 * {@code weirdo_direction}, slabs {@code minecraft:vertical_half}, trapdoors
 * {@code direction}, pistons {@code facing_direction}…).
 */
public final class BlockStates {

    private BlockStates() {}

    /**
     * @param source  the block being replaced (may be null)
     * @param target  the state the pattern asked for
     * @param pinned  property names the user wrote by hand — never overwritten
     * @return {@code target} re-oriented like {@code source}, or {@code target} unchanged
     */
    public static BlockState reorient(BlockState source, BlockState target, Set<String> pinned) {
        if (source == null || target == null || source == target) return target;

        BlockProperties props = propertiesOf(target.getIdentifier());
        if (props == null) return target;

        Map<String, BlockPropertyType<?>> want = new HashMap<>();
        for (BlockPropertyType<?> t : props.getPropertyTypeSet()) {
            if (pinned == null || !pinned.contains(t.getName())) want.put(t.getName(), t);
        }
        if (want.isEmpty()) return target;

        List<BlockPropertyType.BlockPropertyValue<?, ?, ?>> out = new ArrayList<>();

        // Same name on both sides: straight copy. Covers stairs→stairs,
        // slabs→slabs, logs→logs and every other same-family replace.
        for (var v : source.getBlockPropertyValues()) {
            BlockPropertyType<?> t = want.get(v.getPropertyType().getName());
            if (t == null) continue;
            var copied = copy(t, v);
            if (copied != null) { out.add(copied); want.remove(t.getName()); }
        }

        // Different names for the same idea: go through a canonical facing/half/axis.
        if (!want.isEmpty()) {
            BlockFace facing = facingOf(source);
            Boolean top = topHalfOf(source);
            BlockFace.Axis axis = axisOf(source);
            if (facing != null || top != null || axis != null) {
                for (BlockPropertyType<?> t : want.values()) {
                    var derived = derive(t, target.getIdentifier(), facing, top, axis);
                    if (derived != null) out.add(derived);
                }
            }
        }

        if (out.isEmpty()) return target;
        try {
            return target.setPropertyValues(props,
                out.toArray(new BlockPropertyType.BlockPropertyValue[0]));
        } catch (Exception e) {
            return target;
        }
    }

    /** True when the type carries anything worth re-orienting (so callers can skip a world read). */
    public static boolean isOrientable(BlockState state, Set<String> pinned) {
        if (state == null) return false;
        BlockProperties props = propertiesOf(state.getIdentifier());
        if (props == null) return false;
        for (BlockPropertyType<?> t : props.getPropertyTypeSet()) {
            if (pinned == null || !pinned.contains(t.getName())) return true;
        }
        return false;
    }

    /** {@code minecraft:oak_stairs[weirdo_direction=2,upside_down_bit=1]} — the form patterns take. */
    public static String describe(BlockState state) {
        if (state == null) return "minecraft:air";
        var values = state.getBlockPropertyValues();
        if (values.isEmpty()) return state.getIdentifier();
        StringBuilder sb = new StringBuilder(state.getIdentifier()).append('[');
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(values.get(i).getPropertyType().getName())
              .append('=').append(values.get(i).getSerializedValue());
        }
        return sb.append(']').toString();
    }

    private static BlockProperties propertiesOf(String id) {
        try {
            return Registries.BLOCK.getBlockProperties(id);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Same-name copy. Two blocks can share a property name with incompatible
     * values ({@code rail_direction} is 0-9 on curved rails but 0-5 on powered
     * ones), so an unusable value is dropped rather than forced.
     */
    private static BlockPropertyType.BlockPropertyValue<?, ?, ?> copy(
            BlockPropertyType<?> type, BlockPropertyType.BlockPropertyValue<?, ?, ?> from) {
        try { return type.tryCreateValue(from.getValue()); } catch (Exception ignored) {}
        try { return type.tryCreateValue(from.getSerializedValue()); } catch (Exception ignored) {}
        return null;
    }

    // ---------------------------------------------------------------- reading

    /** The facing a state carries, whichever of Bedrock's six spellings it uses. */
    public static BlockFace facingOf(BlockState state) {
        for (var v : state.getBlockPropertyValues()) {
            Object raw = v.getValue();
            BlockFace face = switch (v.getPropertyType().getName()) {
                case "weirdo_direction" ->
                    raw instanceof Integer i ? CommonPropertyMap.EWSN_DIRECTION.inverse().get(i) : null;
                case "direction" ->
                    raw instanceof Integer i ? fromDirection(i, state.getIdentifier()) : null;
                case "facing_direction" ->
                    raw instanceof Integer i && i >= 0 && i <= 5 ? BlockFace.fromIndex(i) : null;
                case "minecraft:cardinal_direction" ->
                    raw instanceof MinecraftCardinalDirection c ? CommonPropertyMap.CARDINAL_BLOCKFACE.get(c) : null;
                case "minecraft:block_face", "minecraft:facing_direction" ->
                    raw instanceof BlockFace f ? f : null;
                case "torch_facing_direction" ->
                    raw instanceof TorchFacingDirection t ? t.getTorchDirection() : null;
                default -> null;
            };
            if (face != null) return face;
        }
        return null;
    }

    /** {@code true} when the block sits in the upper half of its cube (top slab, upside-down stairs). */
    public static Boolean topHalfOf(BlockState state) {
        for (var v : state.getBlockPropertyValues()) {
            Object raw = v.getValue();
            switch (v.getPropertyType().getName()) {
                case "upside_down_bit", "top_slot_bit" -> {
                    if (raw instanceof Boolean b) return b;
                }
                case "minecraft:vertical_half" -> {
                    if (raw instanceof MinecraftVerticalHalf h) return h == MinecraftVerticalHalf.TOP;
                }
                default -> {}
            }
        }
        return null;
    }

    private static BlockFace.Axis axisOf(BlockState state) {
        for (var v : state.getBlockPropertyValues()) {
            if (v.getPropertyType().getName().equals("pillar_axis") && v.getValue() instanceof BlockFace.Axis a)
                return a;
        }
        return null;
    }

    // ---------------------------------------------------------------- writing

    private static BlockPropertyType.BlockPropertyValue<?, ?, ?> derive(
            BlockPropertyType<?> type, String targetId,
            BlockFace facing, Boolean top, BlockFace.Axis axis) {
        boolean horizontal = facing != null && facing.getAxis().isHorizontal();
        Object raw = switch (type.getName()) {
            case "weirdo_direction" -> horizontal ? CommonPropertyMap.EWSN_DIRECTION.get(facing) : null;
            case "direction"        -> horizontal ? toDirection(facing, targetId) : null;
            case "facing_direction" -> facing == null ? null : facing.getIndex();
            case "minecraft:cardinal_direction" ->
                horizontal ? CommonPropertyMap.CARDINAL_BLOCKFACE.inverse().get(facing) : null;
            case "minecraft:block_face", "minecraft:facing_direction" -> facing;
            case "torch_facing_direction" ->
                facing == null ? null : TorchFacingDirection.getByTorchDirection(facing);
            case "upside_down_bit", "top_slot_bit" -> top;
            case "minecraft:vertical_half" ->
                top == null ? null : (top ? MinecraftVerticalHalf.TOP : MinecraftVerticalHalf.BOTTOM);
            case "pillar_axis" -> axis != null ? axis : (facing != null ? facing.getAxis() : null);
            default -> null;
        };
        if (raw == null) return null;
        try { return type.tryCreateValue(raw); } catch (Exception e) { return null; }
    }

    /**
     * The 4-way {@code direction} int has two conventions: trapdoors use
     * EWSN (E0 W1 S2 N3), everything else that carries the name (beds, bells,
     * cocoa, grindstones, looms, tripwire hooks…) uses
     * {@link BlockFace#getHorizontalIndex()} (S0 W1 N2 E3).
     */
    private static boolean trapdoorLike(String id) {
        return id.endsWith("_trapdoor") || id.equals("minecraft:trapdoor");
    }

    private static int toDirection(BlockFace facing, String id) {
        return trapdoorLike(id)
            ? CommonPropertyMap.EWSN_DIRECTION.get(facing)
            : facing.getHorizontalIndex();
    }

    private static BlockFace fromDirection(int value, String id) {
        if (value < 0 || value > 3) return null;
        return trapdoorLike(id)
            ? CommonPropertyMap.EWSN_DIRECTION.inverse().get(value)
            : BlockFace.fromHorizontalIndex(value);
    }
}
