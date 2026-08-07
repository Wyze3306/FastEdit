package fr.fastedit.block;

import org.powernukkitx.block.BlockState;
import org.powernukkitx.level.Level;
import fr.fastedit.math.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface Mask {

    boolean matches(Level level, Vec3 pos);

    Mask ANY = (lvl, pos) -> true;

    static Mask parse(String input) {
        String s = input == null ? "*" : input.trim();
        if (s.isEmpty() || s.equals("*")) return ANY;

        if (s.startsWith("!")) {
            Mask inner = parse(s.substring(1));
            return (lvl, p) -> !inner.matches(lvl, p);
        }

        if (s.equalsIgnoreCase("#air") || s.equalsIgnoreCase("air"))
            return (lvl, p) -> idOf(lvl, p).equals("minecraft:air");

        if (s.equalsIgnoreCase("#solid"))
            return (lvl, p) -> !idOf(lvl, p).equals("minecraft:air");

        List<Spec> specs = new ArrayList<>();
        for (String tok : split(s)) {
            String t = tok.trim();
            if (t.isEmpty()) continue;
            specs.add(Spec.of(t));
        }
        if (specs.isEmpty()) return ANY;
        final List<Spec> snapshot = List.copyOf(specs);

        return (lvl, p) -> {
            BlockState st = stateOf(lvl, p);
            for (Spec spec : snapshot) if (spec.matches(st)) return true;
            return false;
        };
    }

    /**
     * A single mask term: a block id, optionally narrowed to the property
     * values written as {@code id[prop=value]} — so {@code //replace
     * oak_stairs[upside_down_bit=true] oak_slab} only touches the flipped ones.
     */
    record Spec(String id, Map<String, Object> required) {

        static Spec of(String token) {
            int open = token.indexOf('[');
            if (open < 0) return new Spec(Blocks.normalize(token), Map.of());

            Blocks.Token parsed = Blocks.token(token);
            if (parsed == null) throw new IllegalArgumentException("unknown block: " + token);
            Map<String, Object> required = new HashMap<>();
            for (var v : parsed.state().getBlockPropertyValues()) {
                if (parsed.pinned().contains(v.getPropertyType().getName()))
                    required.put(v.getPropertyType().getName(), v.getValue());
            }
            return new Spec(parsed.state().getIdentifier(), required);
        }

        boolean matches(BlockState state) {
            if (state == null) return id.equals("minecraft:air");
            if (!id.equals(state.getIdentifier())) return false;
            if (required.isEmpty()) return true;
            int hit = 0;
            for (var v : state.getBlockPropertyValues()) {
                Object want = required.get(v.getPropertyType().getName());
                if (want == null) continue;
                if (!want.equals(v.getValue())) return false;
                hit++;
            }
            return hit == required.size();
        }
    }

    /** Splits on top-level commas only, so {@code stairs[a=1,b=2]} stays one term. */
    private static List<String> split(String s) {
        List<String> out = new ArrayList<>();
        int depth = 0, start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') depth--;
            else if (c == ',' && depth == 0) { out.add(s.substring(start, i)); start = i + 1; }
        }
        out.add(s.substring(start));
        return out;
    }

    private static BlockState stateOf(Level level, Vec3 pos) {
        try {
            return level.getBlockStateAt(pos.x(), pos.y(), pos.z());
        } catch (Exception unreadable) {
            return null;
        }
    }

    private static String idOf(Level level, Vec3 pos) {
        BlockState st = stateOf(level, pos);
        return st == null ? "minecraft:air" : st.getIdentifier();
    }
}
