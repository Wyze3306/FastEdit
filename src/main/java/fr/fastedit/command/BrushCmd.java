package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.block.Pattern;
import fr.fastedit.brush.Brush;
import fr.fastedit.brush.ClipboardBrush;
import fr.fastedit.brush.CubeBrush;
import fr.fastedit.brush.CylinderBrush;
import fr.fastedit.brush.SmoothBrush;
import fr.fastedit.brush.SphereBrush;
import fr.fastedit.session.Session;

public final class BrushCmd extends Cmd {
    public BrushCmd() {
        super("/brush", "Bind a brush to the held item.",
            "//brush <none|sphere|cube|cyl|smooth|clipboard> [pattern] [radius]");
    }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        require(args.length >= 1, "usage: //brush <none|sphere|cube|cyl|smooth|clipboard> [pattern] [radius]");
        String kind = args[0].toLowerCase();

        if (kind.equals("none") || kind.equals("off")) {
            session.setBrush(null);
            player.sendMessage("§dFastEdit §7| brush cleared.");
            return true;
        }

        Brush brush = switch (kind) {
            case "sphere" -> {
                require(args.length >= 3, "usage: //brush sphere <pattern> <radius>");
                yield new SphereBrush(Pattern.parse(args[1]), Double.parseDouble(args[2]));
            }
            case "cube"   -> {
                require(args.length >= 3, "usage: //brush cube <pattern> <radius>");
                yield new CubeBrush(Pattern.parse(args[1]), Double.parseDouble(args[2]));
            }
            case "cyl"    -> {
                require(args.length >= 4, "usage: //brush cyl <pattern> <radius> <height>");
                yield new CylinderBrush(Pattern.parse(args[1]), Double.parseDouble(args[2]), Integer.parseInt(args[3]));
            }
            case "smooth" -> {
                require(args.length >= 3, "usage: //brush smooth <fillPattern> <radius> [iterations]");
                int it = args.length > 3 ? Integer.parseInt(args[3]) : 2;
                yield new SmoothBrush(Pattern.parse(args[1]), Double.parseDouble(args[2]), it);
            }
            case "clipboard", "clip" -> {
                require(session.clipboard() != null, "your clipboard is empty");
                boolean pasteAir = args.length > 1 && args[1].equalsIgnoreCase("-a");
                yield new ClipboardBrush(session.clipboard(), pasteAir);
            }
            default -> throw new IllegalArgumentException("unknown brush: " + kind);
        };

        session.setBrush(brush);
        player.sendMessage("§dFastEdit §7| brush set: §f" + brush.kind()
            + "§7. Right-click a block to apply it.");
        return true;
    }
}
