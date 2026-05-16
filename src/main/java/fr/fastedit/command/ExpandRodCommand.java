package fr.fastedit.command;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.nbt.tag.CompoundTag;
import fr.fastedit.session.Session;

public class ExpandRodCommand extends FeCommand {

    public static final String ROD_TAG = "fastedit_expandrod";

    public ExpandRodCommand() { super("expandrod", "Give the FastEdit blaze-rod selection expander.");
        params();
    }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        p.getInventory().addItem(makeRod());
        p.sendMessage("§dFastEdit §7| click a block face to grow the selection §f1 §7block that way.");
        return true;
    }

    public static Item makeRod() {
        Item rod = Item.get(ItemID.BLAZE_ROD);
        rod.setCustomName("§dFastEdit §7Expand Rod");
        CompoundTag tag = rod.hasCompoundTag() ? rod.getNamedTag() : new CompoundTag();
        tag.putByte(ROD_TAG, 1);
        rod.setNamedTag(tag);
        return rod;
    }

    public static boolean isRod(Item item) {
        if (item == null) return false;
        if (!ItemID.BLAZE_ROD.equals(item.getId())) return false;
        return item.hasCompoundTag() && item.getNamedTag().contains(ROD_TAG);
    }
}
