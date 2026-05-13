package fr.fastedit.block;

import cn.nukkit.block.BlockState;
import cn.nukkit.level.Level;
import fr.fastedit.math.Vec3;

import java.util.HashSet;
import java.util.Set;

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

        Set<String> ids = new HashSet<>();
        for (String tok : s.split(",")) {
            String t = tok.trim();
            if (t.isEmpty()) continue;
            ids.add(Blocks.normalize(t));
        }
        if (ids.isEmpty()) return ANY;

        return (lvl, p) -> ids.contains(idOf(lvl, p));
    }

    private static String idOf(Level level, Vec3 pos) {
        BlockState st = level.getBlockStateAt(pos.x(), pos.y(), pos.z());
        return st == null ? "minecraft:air" : st.getIdentifier();
    }
}
