package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.FastEdit;
import fr.fastedit.clipboard.Clipboard;
import fr.fastedit.clipboard.Schematic;
import fr.fastedit.session.Session;

import java.io.File;

public final class SchemCmd extends Cmd {
    public SchemCmd() { super("/schem", "Schematic save/load/list.", "//schem <save|load|list> [name]"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        require(args.length >= 1, "usage: //schem <save|load|list> [name]");
        if (!player.hasPermission("fastedit.schematic")) {
            player.sendMessage("§c[FastEdit] you lack the schematic permission.");
            return true;
        }
        File dir = new File(FastEdit.get().getDataFolder(), "schematics");
        dir.mkdirs();

        switch (args[0].toLowerCase()) {
            case "save" -> {
                require(args.length >= 2, "usage: //schem save <name>");
                require(session.clipboard() != null, "your clipboard is empty");
                File f = new File(dir, sanitize(args[1]) + ".fschem");
                try {
                    Schematic.writeFile(f, session.clipboard());
                    player.sendMessage("§dFastEdit §7| saved §f" + f.getName());
                } catch (Exception e) {
                    player.sendMessage("§c[FastEdit] save failed: " + e.getMessage());
                }
            }
            case "load" -> {
                require(args.length >= 2, "usage: //schem load <name>");
                File f = new File(dir, sanitize(args[1]) + ".fschem");
                require(f.exists(), "no such schematic: " + f.getName());
                try {
                    Clipboard c = Schematic.readFile(f);
                    session.setClipboard(c);
                    player.sendMessage("§dFastEdit §7| loaded §f" + f.getName()
                        + " §7(" + c.width() + "×" + c.height() + "×" + c.length() + ")");
                } catch (Exception e) {
                    player.sendMessage("§c[FastEdit] load failed: " + e.getMessage());
                }
            }
            case "list" -> {
                File[] files = dir.listFiles((d, name) -> name.endsWith(".fschem"));
                if (files == null || files.length == 0) {
                    player.sendMessage("§dFastEdit §7| no schematics yet.");
                    return true;
                }
                StringBuilder sb = new StringBuilder("§dFastEdit §7| schematics:");
                for (File f : files) sb.append("\n §7- §f").append(f.getName().replace(".fschem", ""));
                player.sendMessage(sb.toString());
            }
            default -> throw new IllegalArgumentException("unknown sub-command: " + args[0]);
        }
        return true;
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }
}
