package fr.fastedit.command;

import org.powernukkitx.Player;
import org.powernukkitx.item.Item;
import org.powernukkitx.nbt.tag.CompoundTag;
import fr.fastedit.block.Pattern;
import fr.fastedit.brush.Brushes;
import fr.fastedit.brush.SmoothBrush;
import fr.fastedit.session.Session;

public class BrushCommand extends FeCommand {
    public BrushCommand() { super("brush", "Bind a brush to the held shovel.");
        params(enm("kind", false, "none","sphere","cube","cyl","smooth","clipboard"), block("pattern", true), dec("radius", true));
        // //brush smooth takes no pattern, so its radius lands where the block
        // enum is — without this the client rejects the number as an argument.
        overload("smooth", enm("kind", false, "smooth"), dec("radius", true), num("passes", true));
    }

    private static boolean isNumber(String s) {
        try { Double.parseDouble(s); return true; }
        catch (NumberFormatException e) { return false; }
    }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        require(args.length >= 1, "usage: //brush <sphere|cube|cyl> <pattern> <radius> "
            + "| //brush smooth <radius> [passes] | //brush clipboard [-noair] | //brush none");
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
                // The brush keeps the ground's own blocks now, so there is no
                // pattern any more — one is still accepted and dropped so the
                // old "//brush smooth stone 5" out of habit doesn't error out.
                int at = args.length > 1 && isNumber(args[1]) ? 1 : 2;
                require(args.length > at, "usage: //brush smooth <radius> [passes]");
                double radius = Double.parseDouble(args[at]);
                require(radius >= 1 && radius <= SmoothBrush.MAX_RADIUS,
                    "smooth radius must be between 1 and " + SmoothBrush.MAX_RADIUS);
                data.putString("kind", "smooth");
                data.putDouble("radius", radius);
                data.putInt("iterations", args.length > at + 1
                    ? Integer.parseInt(args[at + 1]) : SmoothBrush.DEFAULT_PASSES);
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
