package fr.fastedit.block;

import org.powernukkitx.block.BlockProperties;
import org.powernukkitx.block.BlockState;
import org.powernukkitx.block.property.type.BlockPropertyType;
import org.powernukkitx.registry.Registries;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class Blocks {

    private Blocks() {}

    public static final String AIR_ID         = "minecraft:air";
    public static final String WATER_ID       = "minecraft:water";
    public static final String PLACEHOLDER_ID = "minecraft:magenta_wool";

    /**
     * A parsed block token. {@code pinned} lists the property names the user
     * spelled out as {@code id[name=value]}: those are their choice, so
     * {@link BlockStates#reorient} must not overwrite them with the old block's.
     */
    public record Token(BlockState state, Set<String> pinned) {}

    public static BlockState air()         { return state(AIR_ID); }
    public static BlockState water()       { BlockState s = state(WATER_ID); return s == null ? air() : s; }
    public static BlockState placeholder() {
        BlockState s = state(PLACEHOLDER_ID);
        return s == null ? air() : s;
    }

    public static BlockState state(String token) {
        Token t = token(token);
        return t == null ? null : t.state();
    }

    /**
     * Parses {@code id} or {@code id[prop=value,prop=value]}.
     *
     * @return {@code null} when the id is unknown — a malformed {@code [state]}
     *         part throws instead, because silently falling back to the default
     *         state would place blocks the user did not ask for.
     */
    public static Token token(String input) {
        if (input == null || input.isBlank()) return null;
        String s = input.trim();
        String id = s;
        String states = null;

        int open = s.indexOf('[');
        if (open >= 0) {
            if (!s.endsWith("]")) throw new IllegalArgumentException("unclosed '[' in: " + s);
            id = s.substring(0, open).trim();
            states = s.substring(open + 1, s.length() - 1).trim();
        }

        BlockProperties props;
        try {
            props = Registries.BLOCK.getBlockProperties(normalize(id));
        } catch (IllegalArgumentException unknown) {
            return null;
        }
        if (props == null) return null;
        if (states == null || states.isEmpty()) return new Token(props.getDefaultState(), Set.of());

        List<BlockPropertyType.BlockPropertyValue<?, ?, ?>> values = new ArrayList<>();
        Set<String> pinned = new LinkedHashSet<>();
        for (String pair : states.split(",")) {
            String p = pair.trim();
            if (p.isEmpty()) continue;
            int eq = p.indexOf('=');
            if (eq <= 0) throw new IllegalArgumentException("expected prop=value, got '" + p + "'");
            String name = p.substring(0, eq).trim();
            BlockPropertyType<?> type = property(props, name);
            if (type == null)
                throw new IllegalArgumentException(id + " has no property '" + name + "' — it has "
                    + names(props));
            values.add(value(type, p.substring(eq + 1).trim()));
            pinned.add(type.getName());
        }
        if (values.isEmpty()) return new Token(props.getDefaultState(), Set.of());
        try {
            return new Token(
                props.getBlockState(values.toArray(new BlockPropertyType.BlockPropertyValue[0])),
                pinned);
        } catch (Exception e) {
            throw new IllegalArgumentException("no such block state: " + s);
        }
    }

    /** Property lookup that forgives a missing (or spurious) {@code minecraft:} prefix. */
    static BlockPropertyType<?> property(BlockProperties props, String name) {
        BlockPropertyType<?> exact = null, prefixed = null, bare = null;
        String withPrefix = "minecraft:" + name;
        String withoutPrefix = name.startsWith("minecraft:") ? name.substring(10) : name;
        for (BlockPropertyType<?> t : props.getPropertyTypeSet()) {
            if (t.getName().equals(name)) exact = t;
            else if (t.getName().equals(withPrefix)) prefixed = t;
            else if (t.getName().equals(withoutPrefix)) bare = t;
        }
        return exact != null ? exact : prefixed != null ? prefixed : bare;
    }

    private static BlockPropertyType.BlockPropertyValue<?, ?, ?> value(BlockPropertyType<?> type, String raw) {
        Object v = switch (type.getType()) {
            case BOOLEAN -> switch (raw.toLowerCase(Locale.ROOT)) {
                case "true", "1"  -> Boolean.TRUE;
                case "false", "0" -> Boolean.FALSE;
                default -> throw new IllegalArgumentException(
                    type.getName() + " is true/false, got '" + raw + "'");
            };
            case INT -> {
                try { yield Integer.valueOf(raw); }
                catch (NumberFormatException e) {
                    throw new IllegalArgumentException(type.getName() + " is a number, got '" + raw + "'");
                }
            }
            case ENUM -> raw;
        };
        try {
            return type.tryCreateValue(v);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "'" + raw + "' is not a valid " + type.getName() + " — expected " + valid(type));
        }
    }

    private static String names(BlockProperties props) {
        List<String> out = new ArrayList<>();
        for (BlockPropertyType<?> t : props.getPropertyTypeSet()) out.add(t.getName());
        return out.isEmpty() ? "no properties at all" : String.join(", ", out);
    }

    private static String valid(BlockPropertyType<?> type) {
        List<?> vals = type.getValidValues();
        if (vals.isEmpty()) return "?";
        if (vals.size() > 6) return vals.get(0) + ".." + vals.get(vals.size() - 1);
        StringBuilder sb = new StringBuilder();
        for (Object v : vals) {
            if (sb.length() > 0) sb.append('/');
            sb.append(v instanceof Enum<?> e ? e.name().toLowerCase(Locale.ROOT) : v);
        }
        return sb.toString();
    }

    public static String normalize(String token) {
        return token.contains(":") ? token : "minecraft:" + token;
    }
}
