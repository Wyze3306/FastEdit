package fr.fastedit.listener;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.nbt.tag.CompoundTag;
import fr.fastedit.brush.Brush;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;
import fr.fastedit.session.SessionManager;

public final class WandListener implements Listener {

    public static final String WAND_ID = ItemID.WOODEN_AXE;
    public static final String WAND_TAG = "fastedit:wand";

    public static Item makeWand() {
        Item axe = Item.get(WAND_ID);
        axe.setCustomName("§dFastEdit §7Wand");
        CompoundTag tag = axe.hasCompoundTag() ? axe.getNamedTag() : new CompoundTag();
        tag.putByte(WAND_TAG, 1);
        axe.setNamedTag(tag);
        return axe;
    }

    public static boolean isWand(Item item) {
        if (item == null) return false;
        if (!WAND_ID.equals(item.getId())) return false;
        return item.hasCompoundTag() && item.getNamedTag().contains(WAND_TAG);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        Block block = event.getBlock();
        if (item == null) return;
        if (!player.hasPermission("fastedit.use")) return;

        Session session = SessionManager.get().of(player);

        if (isWand(item)) {
            if (block == null) return;
            Vec3 v = new Vec3(block.getFloorX(), block.getFloorY(), block.getFloorZ());
            switch (event.getAction()) {
                case LEFT_CLICK_BLOCK -> {
                    session.setPos1(player.getLevel(), v);
                    player.sendMessage("§dFastEdit §7| §fpos1 §7-> §a" + v);
                    event.setCancelled(true);
                }
                case RIGHT_CLICK_BLOCK -> {
                    session.setPos2(player.getLevel(), v);
                    player.sendMessage("§dFastEdit §7| §fpos2 §7-> §a" + v);
                    event.setCancelled(true);
                }
                default -> {}
            }
            return;
        }

        Brush brush = session.brush();
        if (brush == null) return;
        if (event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
            && event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_AIR) return;
        if (block == null) return;
        Vec3 v = new Vec3(block.getFloorX(), block.getFloorY(), block.getFloorZ());
        brush.use(player, session, player.getLevel(), v);
    }
}
