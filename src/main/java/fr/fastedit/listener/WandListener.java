package fr.fastedit.listener;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockState;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.ItemWoodenAxe;
import fr.fastedit.brush.Brush;
import fr.fastedit.brush.Brushes;
import fr.fastedit.clipboard.UnknownBlocks;
import fr.fastedit.command.InspectCommand;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;
import fr.fastedit.session.SessionManager;

public class WandListener implements Listener {

    private static final int BRUSH_REACH = 256;
    private static final long BRUSH_COOLDOWN_MS = 500;

    public static Item makeWand() {
        Item axe = Item.get(ItemID.WOODEN_AXE);
        axe.setCustomName("§dFastEdit §7Wand");
        return axe;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getItem() instanceof ItemWoodenAxe) event.setCancelled(true);
        if (InspectCommand.isInspector(event.getItem())) event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Item item = event.getItem();
        if (item == null) return;
        Session session = SessionManager.get().of(p);

        if (InspectCommand.isInspector(item)) {
            handleInspector(p, event);
            return;
        }

        if (item instanceof ItemWoodenAxe) {
            Block b = event.getBlock();
            if (b == null) return;
            Vec3 v = new Vec3(b.getFloorX(), b.getFloorY(), b.getFloorZ());
            switch (event.getAction()) {
                case LEFT_CLICK_BLOCK -> {
                    event.setCancelled(true);
                    if (v.equals(session.pos1())) return;
                    session.setPos1(p.getLevel(), v);
                    p.sendMessage("§dFastEdit §7| §fpos1 §7-> §a" + v);
                }
                case RIGHT_CLICK_BLOCK -> {
                    event.setCancelled(true);
                    if (v.equals(session.pos2())) return;
                    session.setPos2(p.getLevel(), v);
                    p.sendMessage("§dFastEdit §7| §fpos2 §7-> §a" + v);
                }
                default -> {}
            }
            return;
        }

        if (!Brushes.hasBrush(item)) return;
        var action = event.getAction();
        if (action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
            && action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR) return;

        long now = System.currentTimeMillis();
        if (now - session.lastBrushUseMs() < BRUSH_COOLDOWN_MS) return;
        session.setLastBrushUseMs(now);

        Block target = event.getBlock();
        if (target == null || action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            target = p.getTargetBlock(BRUSH_REACH);
        }
        if (target == null) {
            p.sendMessage("§c[FastEdit] no block in sight.");
            return;
        }

        try {
            Brush brush = Brushes.fromItem(item, session);
            brush.use(p, session, p.getLevel(), new Vec3(target.getFloorX(), target.getFloorY(), target.getFloorZ()));
        } catch (IllegalArgumentException e) {
            p.sendMessage("§c[FastEdit] " + e.getMessage());
        }
    }

    private void handleInspector(Player p, PlayerInteractEvent event) {
        var action = event.getAction();
        if (action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
            && action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR
            && action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) return;
        event.setCancelled(true);

        Block target = event.getBlock();
        if (target == null || action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            target = p.getTargetBlock(64);
        }
        if (target == null) {
            p.sendMessage("§c[FastEdit] no block in sight.");
            return;
        }
        int x = target.getFloorX(), y = target.getFloorY(), z = target.getFloorZ();
        BlockState st = p.getLevel().getBlockStateAt(x, y, z);
        String id = st == null ? "minecraft:air" : st.getIdentifier();
        String orig = UnknownBlocks.lookup(p.getLevel().getName(), x, y, z);

        StringBuilder sb = new StringBuilder("§dFastEdit §7| §f").append(x).append(", ").append(y).append(", ").append(z)
            .append("\n §7current: §f").append(id);
        if (orig != null) sb.append("\n §7original (Java): §e").append(orig);
        p.sendMessage(sb.toString());
    }
}
