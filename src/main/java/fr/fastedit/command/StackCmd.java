package fr.fastedit.command;

import cn.nukkit.Player;
import cn.nukkit.block.BlockState;
import cn.nukkit.level.Level;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.math.Region;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;

public final class StackCmd extends Cmd {
    public StackCmd() { super("/stack", "Stack the selection along a direction.", "//stack <count> [direction]"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        require(args.length >= 1, "usage: //stack <count> [direction]");
        require(session.hasSelection(), "no selection");
        int count = Integer.parseInt(args[0]);
        require(count > 0, "count must be > 0");
        String dirName = args.length > 1 ? args[1].toLowerCase() : "me";
        Vec3 dir = MoveCmd.direction(player, dirName);

        Region r = session.region();
        Level level = session.level();
        int w = r.width(), h = r.height(), l = r.length();

        BlockState[] snapshot = new BlockState[w * h * l];
        int idx = 0;
        for (int y = 0; y < h; y++)
            for (int z = 0; z < l; z++)
                for (int x = 0; x < w; x++)
                    snapshot[idx++] = level.getBlockStateAt(r.min().x() + x, r.min().y() + y, r.min().z() + z);

        Vec3 step = new Vec3(dir.x() * w, dir.y() * h, dir.z() * l);

        EditEngine.get().submit(level,
            es -> {
                for (int rep = 1; rep <= count; rep++) {
                    int ox = step.x() * rep, oy = step.y() * rep, oz = step.z() * rep;
                    int i = 0;
                    for (int y = 0; y < h; y++)
                        for (int z = 0; z < l; z++)
                            for (int x = 0; x < w; x++) {
                                BlockState s = snapshot[i++];
                                if (s == null || "minecraft:air".equals(s.getIdentifier())) continue;
                                es.plan(new Vec3(r.min().x() + x + ox,
                                                 r.min().y() + y + oy,
                                                 r.min().z() + z + oz), s);
                            }
                }
            },
            n -> player.sendMessage("§dFastEdit §7| stacked §f" + n + "§7 blocks."),
            t -> player.sendMessage("§c[FastEdit] " + t.getMessage()),
            session.undo());
        return true;
    }
}
