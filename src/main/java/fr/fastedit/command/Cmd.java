package fr.fastedit.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import fr.fastedit.session.Session;
import fr.fastedit.session.SessionManager;

/**
 * All FastEdit commands are player-only and gated behind {@code fastedit.use}.
 * The base class handles those checks so the subclasses stay tiny.
 */
public abstract class Cmd extends Command {

    protected Cmd(String name, String description, String usage) {
        super(name, description, usage);
        setPermission("fastedit.use");
    }

    @Override
    public final boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cFastEdit commands are player-only.");
            return true;
        }
        if (!testPermission(player)) return true;
        try {
            return run(player, SessionManager.get().of(player), args);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c[FastEdit] " + e.getMessage());
            return true;
        } catch (Throwable t) {
            player.sendMessage("§c[FastEdit] error: " + t.getMessage());
            t.printStackTrace();
            return true;
        }
    }

    protected abstract boolean run(Player player, Session session, String[] args);

    protected static void require(boolean cond, String message) {
        if (!cond) throw new IllegalArgumentException(message);
    }
}
