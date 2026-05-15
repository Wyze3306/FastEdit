package fr.fastedit.command;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import fr.fastedit.block.Pattern;
import fr.fastedit.brush.Brushes;
import fr.fastedit.session.Session;

public class BrushCommand extends FeCommand {
    public BrushCommand() { super("brush", "Bind a brush to the held shovel.");
        params(enm("kind", false, "none","sphere","cube","cyl","smooth","clipboard"), block("pattern", true), dec("radius", true));
    }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        require(args.length >= 1, "usage: //brush <none|sphere|cube|cyl|smooth|clipboard> [pattern] [radius]");
        Item held = p.getInventory().getItemInMainHand();
        require(Brushes.isShovel(held), "hold a shovel to bind a brush.");

        String kind = args[0].toLowerCase();
        if (kind.equals("none") || kind.equals("off")) {
            Brushes.clearOnItem(held);
            p.getInventory().setItemInMainHand(held);
            p.sendMessage("§dFastEdit §7| brush cleared on §f" + held.getId() + "§7.");
            return true;
        }

        CompoundTag data = new CompoundTag();
        switch (kind) {
            case "sphere", "cube" -> {
                require(args.length >= 3, "usage: //brush " + kind + " <pattern> <radius>");
                Pattern.parse(args[1]);
                data.putString("kind", kind);
                data.putString("pattern", args[1]);
                data.putDouble("radius", Double.parseDouble(args[2]));
            }
            case "cyl" -> {
                require(args.length >= 4, "usage: //brush cyl <pattern> <radius> <height>");
                Pattern.parse(args[1]);
                data.putString("kind", "cyl");
                data.putString("pattern", args[1]);
                data.putDouble("radius", Double.parseDouble(args[2]));
                data.putInt("height", Integer.parseInt(args[3]));
            }
            case "smooth" -> {
                require(args.length >= 3, "usage: //brush smooth <fillPattern> <radius> [iterations]");
                Pattern.parse(args[1]);
                data.putString("kind", "smooth");
                data.putString("pattern", args[1]);
                data.putDouble("radius", Double.parseDouble(args[2]));
                data.putInt("iterations", args.length > 3 ? Integer.parseInt(args[3]) : 2);
            }
            case "clipboard", "clip" -> {
                require(session.clipboard() != null, "your clipboard is empty");
                data.putString("kind", "clipboard");
                boolean skipAir = args.length > 1
                    && (args[1].equalsIgnoreCase("-noair") || args[1].equalsIgnoreCase("--skip-air"));
                data.putByte("skipAir", skipAir ? 1 : 0);
            }
            default -> throw new IllegalArgumentException("unknown brush: " + kind);
        }

        Brushes.writeToItem(held, data);
        p.getInventory().setItemInMainHand(held);
        p.sendMessage("§dFastEdit §7| brush §f" + kind + " §7bound to your shovel. Right-click anywhere to apply.");
        return true;
    }
}
