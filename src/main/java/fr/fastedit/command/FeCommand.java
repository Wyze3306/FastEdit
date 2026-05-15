package fr.fastedit.command;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import fr.fastedit.FastEdit;
import fr.fastedit.session.Session;
import fr.fastedit.session.SessionManager;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class FeCommand extends PluginCommand<FastEdit> {

    protected FeCommand(String name, String description) {
        super(name, FastEdit.get());
        setDescription(description);
    }

    @Override
    public final boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cFastEdit commands are player-only.");
            return true;
        }
        try {
            return run(p, SessionManager.get().of(p), args);
        } catch (IllegalArgumentException e) {
            p.sendMessage("§c[FastEdit] " + describe(e));
            return true;
        } catch (Throwable t) {
            p.sendMessage("§c[FastEdit] error: " + describe(t));
            FastEdit.get().getLogger().error("[FastEdit] command '" + label + "' failed", t);
            return true;
        }
    }

    protected abstract boolean run(Player player, Session session, String[] args);

    protected static void require(boolean cond, String msg) {
        if (!cond) throw new IllegalArgumentException(msg);
    }

    // These only feed the Bedrock client's argument autocomplete; parsing
    // stays the permissive WorldEdit-style String[] handling in each run().
    protected final void params(CommandParameter... p) {
        Map<String, CommandParameter[]> m = new LinkedHashMap<>();
        m.put("default", p);
        setCommandParameters(m);
    }

    protected static CommandParameter txt(String name, boolean optional) {
        return CommandParameter.newType(name, optional, CommandParamType.STRING);
    }
    protected static CommandParameter num(String name, boolean optional) {
        return CommandParameter.newType(name, optional, CommandParamType.INT);
    }
    protected static CommandParameter dec(String name, boolean optional) {
        return CommandParameter.newType(name, optional, CommandParamType.FLOAT);
    }
    protected static CommandParameter enm(String name, boolean optional, String... values) {
        return CommandParameter.newEnum(name, optional, values);
    }
    /** Every Bedrock block id as a suggestion list; falls back to free text. */
    protected static CommandParameter block(String name, boolean optional) {
        String[] ids = fr.fastedit.clipboard.BedrockIds.names();
        return ids.length == 0
            ? txt(name, optional)
            : CommandParameter.newEnum(name, optional, ids);
    }

    /** Never returns null — falls back to the exception class name when there is no message. */
    protected static String describe(Throwable t) {
        if (t == null) return "unknown error";
        String msg = t.getMessage();
        if (msg != null && !msg.isBlank()) return msg;
        Throwable cause = t.getCause();
        if (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank())
            return t.getClass().getSimpleName() + " (" + cause.getMessage() + ")";
        return t.getClass().getSimpleName();
    }
}
