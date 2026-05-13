package fr.fastedit.block;

import cn.nukkit.block.BlockState;
import fr.fastedit.math.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@FunctionalInterface
public interface Pattern {

    BlockState next(Vec3 at);

    static Pattern single(BlockState state) {
        if (state == null) throw new IllegalArgumentException("null state");
        return v -> state;
    }

    static Pattern parse(String input) {
        if (input == null) throw new IllegalArgumentException("empty pattern");
        String s = input.trim();
        if (s.isEmpty()) throw new IllegalArgumentException("empty pattern");

        if (!s.contains(",")) return single(parseToken(s).state);

        String[] parts = s.split(",");
        List<Token> tokens = new ArrayList<>(parts.length);
        double totalWeight = 0;
        for (String p : parts) {
            Token t = parseToken(p.trim());
            tokens.add(t);
            totalWeight += t.weight;
        }
        final double total = totalWeight;
        final List<Token> snapshot = List.copyOf(tokens);

        return at -> {
            double r = ThreadLocalRandom.current().nextDouble(total);
            for (Token t : snapshot) {
                r -= t.weight;
                if (r <= 0) return t.state;
            }
            return snapshot.get(snapshot.size() - 1).state;
        };
    }

    record Token(BlockState state, double weight) {}

    private static Token parseToken(String s) {
        double weight = 1;
        String id = s;
        int pct = s.indexOf('%');
        if (pct > 0) {
            weight = Double.parseDouble(s.substring(0, pct));
            id = s.substring(pct + 1);
        }
        BlockState st = Blocks.state(id);
        if (st == null) throw new IllegalArgumentException("unknown block: " + id);
        return new Token(st, weight);
    }
}
