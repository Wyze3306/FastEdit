package fr.fastedit.block;

import org.powernukkitx.block.BlockState;
import org.powernukkitx.level.Level;
import fr.fastedit.math.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@FunctionalInterface
public interface Pattern {

    /**
     * @param level the world being written to, used to read the block that is
     *              about to be replaced so its orientation can be carried over
     *              (see {@link BlockStates}); {@code null} skips that.
     */
    BlockState next(Level level, Vec3 at);

    static Pattern single(BlockState state) {
        if (state == null) throw new IllegalArgumentException("null state");
        return (lvl, v) -> state;
    }

    static Pattern parse(String input) {
        if (input == null) throw new IllegalArgumentException("empty pattern");
        String s = input.trim();
        if (s.isEmpty()) throw new IllegalArgumentException("empty pattern");

        List<String> parts = split(s);
        if (parts.size() == 1) {
            Entry only = parseToken(parts.get(0).trim());
            return only::at;
        }

        List<Entry> entries = new ArrayList<>(parts.size());
        double totalWeight = 0;
        for (String p : parts) {
            Entry e = parseToken(p.trim());
            entries.add(e);
            totalWeight += e.weight();
        }
        final double total = totalWeight;
        final List<Entry> snapshot = List.copyOf(entries);

        return (lvl, at) -> {
            double r = ThreadLocalRandom.current().nextDouble(total);
            for (Entry e : snapshot) {
                r -= e.weight();
                if (r <= 0) return e.at(lvl, at);
            }
            return snapshot.get(snapshot.size() - 1).at(lvl, at);
        };
    }

    /**
     * One term of a pattern. {@code orientable} is precomputed so plain blocks
     * ({@code stone}, {@code dirt}…) never pay for a world read per block.
     */
    record Entry(BlockState state, Set<String> pinned, double weight, boolean orientable) {

        BlockState at(Level level, Vec3 v) {
            if (!orientable || level == null) return state;
            BlockState current;
            try {
                current = level.getBlockStateAt(v.x(), v.y(), v.z());
            } catch (Exception unreadable) {
                return state;
            }
            return BlockStates.reorient(current, state, pinned);
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

    private static Entry parseToken(String s) {
        double weight = 1;
        String id = s;
        int bracket = s.indexOf('[');
        int pct = s.indexOf('%');
        if (pct > 0 && (bracket < 0 || pct < bracket)) {
            weight = Double.parseDouble(s.substring(0, pct));
            id = s.substring(pct + 1);
        }
        Blocks.Token token = Blocks.token(id);
        if (token == null) throw new IllegalArgumentException("unknown block: " + id);
        return new Entry(token.state(), token.pinned(), weight,
            BlockStates.isOrientable(token.state(), token.pinned()));
    }
}
