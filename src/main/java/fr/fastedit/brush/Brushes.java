package fr.fastedit.brush;

import org.powernukkitx.item.Item;
import org.powernukkitx.nbt.tag.CompoundTag;
import fr.fastedit.block.Mask;
import fr.fastedit.block.Pattern;
import fr.fastedit.session.Session;

public final class Brushes {

    public static final String NBT_KEY = "fastedit_brush";

    private Brushes() {}

    public static boolean isShovel(Item item) {
        if (item == null) return false;
        String id = item.getId();
        return id != null && id.endsWith("_shovel");
    }

    public static boolean hasBrush(Item item) {
        return isShovel(item) && item.hasNbt()
            && item.getNbt().contains(NBT_KEY);
    }

    public static Brush fromItem(Item item, Session session) {
        if (!hasBrush(item)) return null;
        return fromNbt(item.getNbt().getCompound(NBT_KEY), session);
    }

    public static Brush fromNbt(CompoundTag tag, Session session) {
        String kind = tag.getString("kind");
        Brush brush = switch (kind) {
            case "sphere" -> new SphereBrush(
                Pattern.parse(tag.getString("pattern")),
                tag.getDouble("radius"));
            case "cube" -> new CubeBrush(
                Pattern.parse(tag.getString("pattern")),
                tag.getDouble("radius"));
            case "cyl" -> new CylinderBrush(
                Pattern.parse(tag.getString("pattern")),
                tag.getDouble("radius"),
                tag.getInt("height"));
            // "iterations" predates the rewrite and is kept so shovels bound by
            // an older build still work; a missing value reads as 0 → the default.
            case "smooth" -> new SmoothBrush(
                tag.getDouble("radius"),
                tag.getInt("iterations"));
            case "clipboard" -> {
                if (session.clipboard() == null)
                    throw new IllegalArgumentException("clipboard brush requires a clipboard — //copy first");
                yield new ClipboardBrush(session.clipboard(), tag.getByte("skipAir") == 1);
            }
            default -> throw new IllegalArgumentException("unknown brush: " + kind);
        };
        if (tag.containsString("mask")) brush.withMask(Mask.parse(tag.getString("mask")));
        return brush;
    }

    public static void writeToItem(Item item, CompoundTag brushData) {
        CompoundTag tag = item.hasNbt() ? item.getNbt() : new CompoundTag();
        tag.putCompound(NBT_KEY, brushData);
        item.setNbt(tag);
    }

    public static void clearOnItem(Item item) {
        if (!item.hasNbt()) return;
        CompoundTag tag = item.getNbt();
        tag.remove(NBT_KEY);
        item.setNbt(tag);
    }
}
