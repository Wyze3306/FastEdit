package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.FastEdit;
import fr.fastedit.clipboard.Clipboard;
import fr.fastedit.clipboard.SchematicIO;
import fr.fastedit.session.Session;

import java.util.List;

public class SchemCommand extends FeCommand {
    public SchemCommand() { super("schem", "Schematic save/load/list (.mcstructure/.schem/.schematic).");
        params(enm("action", false, "save","load","list"), txt("name", true));
    }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        require(args.length >= 1, "usage: //schem <save|load|list> [name]");
        switch (args[0].toLowerCase()) {
            case "save" -> {
                require(args.length >= 2, "usage: //schem save <name>");
                require(session.clipboard() != null, "your clipboard is empty — //copy first");
                String name = sanitize(args[1]);
                SchematicIO.save(session.clipboard(), name);
                p.sendMessage("§dFastEdit §7| saved §f" + name + ".mcstructure");
            }
            case "load" -> {
                require(args.length >= 2, "usage: //schem load <name>");
                String name = sanitize(args[1]);
                require(SchematicIO.exists(name), "no such schematic: " + name);
                Clipboard c;
                try { c = SchematicIO.load(name); }
                catch (Exception e) {
                    FastEdit.get().getLogger().error("[FastEdit] schem load '" + name + "' failed", e);
                    throw new IllegalArgumentException("load failed: " + describe(e));
                }
                require(c != null, "failed to load: " + name);
                session.setClipboard(c);
                p.sendMessage("§dFastEdit §7| loaded §f" + name
                    + " §7(" + c.width() + "×" + c.height() + "×" + c.length() + ")");
            }
            case "list" -> {
                List<String> all = SchematicIO.list();
                if (all.isEmpty()) {
                    p.sendMessage("§dFastEdit §7| no schematics in §f" + SchematicIO.structureDir().getPath());
                    return true;
                }
                StringBuilder sb = new StringBuilder("§dFastEdit §7| §f" + all.size() + " schematic(s):");
                for (String n : all) sb.append("\n §7- §f").append(n);
                p.sendMessage(sb.toString());
            }
            default -> throw new IllegalArgumentException("unknown sub-command: " + args[0]);
        }
        return true;
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_./:-]", "_");
    }
}
