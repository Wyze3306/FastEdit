package fr.fastedit.command;

import cn.nukkit.Player;
import cn.nukkit.block.BlockState;
import fr.fastedit.clipboard.Clipboard;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;

public final class PasteCmd extends Cmd {
    public PasteCmd() { super("/paste", "Paste your clipboard at your feet.", "//paste [-a]"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        Clipboard clip = session.clipboard();
        require(clip != null, "your clipboard is empty — //copy first");
        boolean pasteAir = args.length > 0 && args[0].equalsIgnoreCase("-a");

        Vec3 anchor = new Vec3(player.getFloorX(), player.getFloorY(), player.getFloorZ());
        EditEngine.get().submit(player.getLevel(),
            es -> {
                Vec3 off = clip.offset();
                for (int y = 0; y < clip.height(); y++)
                    for (int z = 0; z < clip.length(); z++)
                        for (int x = 0; x < clip.width(); x++) {
                            BlockState s = clip.get(x, y, z);
                            if (s == null) continue;
                            if (!pasteAir && "minecraft:air".equals(s.getIdentifier())) continue;
                            es.plan(new Vec3(
                                anchor.x() + x - off.x(),
                                anchor.y() + y - off.y(),
                                anchor.z() + z - off.z()), s);
                        }
            },
            n -> player.sendMessage("§dFastEdit §7| pasted §f" + n + "§7 blocks."),
            t -> player.sendMessage("§c[FastEdit] " + t.getMessage()),
            session.undo());
        return true;
    }
}
