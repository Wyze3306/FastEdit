package fr.fastedit.command;

import org.powernukkitx.Player;
import org.powernukkitx.item.Item;
import org.powernukkitx.item.ItemID;
import org.powernukkitx.nbt.tag.CompoundTag;
import fr.fastedit.session.Session;

public class InspectCommand extends FeCommand {

    public static final String INSPECTOR_TAG = "fastedit_inspector";

    public InspectCommand() { super("inspect", "Give the FastEdit block-info stick.");
        params();
    }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        p.getInventory().addItem(makeInspector());
        p.sendMessage("§dFastEdit §7| right-click any block to see its ID (and original Java ID if it was a placeholder).");
        return true;
    }

    public static Item makeInspector() {
        Item stick = Item.get(ItemID.STICK);
        stick.setCustomName("§dFastEdit §7Inspector");
        CompoundTag tag = stick.hasNbt() ? stick.getNbt() : new CompoundTag();
        tag.putByte(INSPECTOR_TAG, 1);
        stick.setNbt(tag);
        return stick;
    }

    public static boolean isInspector(Item item) {
        if (item == null) return false;
        if (!ItemID.STICK.equals(item.getId())) return false;
        return item.hasNbt() && item.getNbt().contains(INSPECTOR_TAG);
    }
}
