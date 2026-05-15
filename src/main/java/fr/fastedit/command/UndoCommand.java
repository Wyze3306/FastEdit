package fr.fastedit.command;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.edit.UndoBuffer;
import fr.fastedit.session.Session;

import java.util.List;

public class UndoCommand extends FeCommand {
    public UndoCommand() { super("undo", "Undo the last edit (whole structure).");
        params(num("times", true));
    }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        int times = args.length > 0 ? Math.max(1, Integer.parseInt(args[0])) : 1;
        long total = 0;
        for (int i = 0; i < times; i++) {
            List<UndoBuffer.Entry> group = session.undo().popUndoGroup();
            if (group.isEmpty()) break;
            for (UndoBuffer.Entry e : group) {
                Level level = Server.getInstance().getLevelByName(e.levelName());
                if (level == null) continue;
                EditEngine.get().replay(level, e.changes(), false, null);
                total += e.changes().size();
            }
        }
        p.sendMessage("§dFastEdit §7| undoing §f" + total + "§7 blocks…");
        return true;
    }
}
