package fr.fastedit.brush;

import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
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
        return isShovel(item) && item.hasCompoundTag()
            && item.getNamedTag().contains(NBT_KEY);
    }

    public static Brush fromItem(Item item, Session session) {
        if (!hasBrush(item)) return null;
        return fromNbt(item.getNamedTag().getCompound(NBT_KEY), session);
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
            case "smooth" -> new SmoothBrush(
                Pattern.parse(tag.getString("pattern")),
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
        CompoundTag tag = item.hasCompoundTag() ? item.getNamedTag() : new CompoundTag();
        tag.putCompound(NBT_KEY, brushData);
        item.setNamedTag(tag);
    }

    public static void clearOnItem(Item item) {
        if (!item.hasCompoundTag()) return;
        CompoundTag tag = item.getNamedTag();
        tag.remove(NBT_KEY);
        item.setNamedTag(tag);
    }
}
