package fr.fastedit.command;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.edit.UndoBuffer;
import fr.fastedit.session.Session;

public final class UndoCmd extends Cmd {
    public UndoCmd() { super("/undo", "Undo the last edit.", "//undo [count]"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        int times = args.length > 0 ? Math.max(1, Integer.parseInt(args[0])) : 1;
        int total = 0;
        for (int i = 0; i < times; i++) {
            UndoBuffer.Entry e = session.undo().popUndo();
            if (e == null) break;
            Level level = Server.getInstance().getLevelByName(e.levelName());
            if (level == null) continue;
            EditEngine.get().replay(level, e.changes(), false, null);
            total += e.changes().size();
        }
        player.sendMessage("§dFastEdit §7| undone §f" + total + "§7 blocks.");
        return true;
    }
}
