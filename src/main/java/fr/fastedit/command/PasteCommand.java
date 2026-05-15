package fr.fastedit.command;

import cn.nukkit.Player;
import cn.nukkit.block.BlockState;
import fr.fastedit.clipboard.Clipboard;
import fr.fastedit.clipboard.UnknownBlocks;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;

public class PasteCommand extends FeCommand {
    public PasteCommand() { super("paste", "Paste your clipboard at your feet."); }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        Clipboard clip = session.clipboard();
        require(clip != null, "your clipboard is empty — //copy first");
        boolean skipAir = args.length > 0
            && (args[0].equalsIgnoreCase("-noair") || args[0].equalsIgnoreCase("--skip-air"));
        Vec3 anchor = new Vec3(p.getFloorX(), p.getFloorY(), p.getFloorZ());
        String worldName = p.getLevel().getName();

        final int W = clip.width(), H = clip.height(), L = clip.length();
        final long volume = (long) W * H * L;
        final Vec3 off = clip.offset();
        final long[] cursor = {0};

        // Streamed in bounded segments so a clipboard of any size cannot OOM.
        EditEngine.Segmenter seg = (es, max) -> {
            int planned = 0, scanned = 0, scanCap = max * 8;
            long i = cursor[0];
            while (i < volume && planned < max && scanned < scanCap) {
                int x = (int) (i % W);
                int z = (int) ((i / W) % L);
                int y = (int) (i / ((long) W * L));
                i++;
                scanned++;
                BlockState s = clip.get(x, y, z);
                if (s == null) continue;
                if (skipAir && "minecraft:air".equals(s.getIdentifier())) continue;
                int wx = anchor.x() + x - off.x();
                int wy = anchor.y() + y - off.y();
                int wz = anchor.z() + z - off.z();
                es.plan(new Vec3(wx, wy, wz), s);
                String orig = clip.original(x, y, z);
                if (orig != null) UnknownBlocks.record(worldName, wx, wy, wz, orig);
                planned++;
            }
            cursor[0] = i;
            return i < volume;
        };

        EditEngine.get().submitStreaming(p.getLevel(), seg,
            n -> {
                UnknownBlocks.flush();
                int u = clip.unknownCount();
                String suffix = u > 0 ? " §7(§e" + u + " placeholders§7)" : "";
                p.sendMessage("§dFastEdit §7| pasted §f" + n + "§7 blocks." + suffix);
            },
            t -> { UnknownBlocks.flush(); p.sendMessage("§c[FastEdit] " + describe(t)); },
            session.undo());
        return true;
    }
}
