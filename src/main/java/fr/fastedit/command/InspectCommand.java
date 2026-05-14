package fr.fastedit.command;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.nbt.tag.CompoundTag;
import fr.fastedit.session.Session;

public class InspectCommand extends FeCommand {

    public static final String INSPECTOR_TAG = "fastedit_inspector";

    public InspectCommand() { super("inspect", "Give the FastEdit block-info stick."); }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        p.getInventory().addItem(makeInspector());
        p.sendMessage("§dFastEdit §7| right-click any block to see its ID (and original Java ID if it was a placeholder).");
        return true;
    }

    public static Item makeInspector() {
        Item stick = Item.get(ItemID.STICK);
        stick.setCustomName("§dFastEdit §7Inspector");
        CompoundTag tag = stick.hasCompoundTag() ? stick.getNamedTag() : new CompoundTag();
        tag.putByte(INSPECTOR_TAG, 1);
        stick.setNamedTag(tag);
        return stick;
    }

    public static boolean isInspector(Item item) {
        if (item == null) return false;
        if (!ItemID.STICK.equals(item.getId())) return false;
        return item.hasCompoundTag() && item.getNamedTag().contains(INSPECTOR_TAG);
    }
}
