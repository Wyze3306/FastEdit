package fr.fastedit.listener;

import org.powernukkitx.Player;
import org.powernukkitx.block.Block;
import org.powernukkitx.block.BlockState;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.block.BlockBreakEvent;
import org.powernukkitx.event.player.PlayerInteractEvent;
import org.powernukkitx.item.Item;
import org.powernukkitx.item.ItemID;
import org.powernukkitx.item.ItemWoodenAxe;
import org.powernukkitx.math.BlockFace;
import fr.fastedit.FastEdit;
import fr.fastedit.brush.Brush;
import fr.fastedit.brush.Brushes;
import fr.fastedit.clipboard.UnknownBlocks;
import fr.fastedit.command.ExpandCommand;
import fr.fastedit.command.ExpandRodCommand;
import fr.fastedit.command.InspectCommand;
import fr.fastedit.math.Region;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;
import fr.fastedit.session.SessionManager;

public class WandListener implements Listener {

    private static final int BRUSH_REACH = 256;
    private static final long BRUSH_COOLDOWN_MS = 500;
    private static final long TOOL_COOLDOWN_MS = 300;

    private static boolean allowed(Player p) {
        return p != null && p.hasPermission(FastEdit.PERMISSION);
    }

    // Bedrock re-fires the same interact many times per click; swallow repeats
    // of the same action within the window (a different action passes at once).
    private boolean spam(Session s, PlayerInteractEvent.Action action) {
        long now = System.currentTimeMillis();
        String a = action.name();
        if (a.equals(s.lastToolAction()) && now - s.lastToolUseMs() < TOOL_COOLDOWN_MS) return true;
        s.setLastTool(now, a);
        return false;
    }

    public static Item makeWand() {
        Item axe = Item.get(ItemID.WOODEN_AXE);
        axe.setCustomName("§dFastEdit §7Wand");
        return axe;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        // Non-ops keep vanilla behaviour: a wooden axe is just a wooden axe.
        if (!allowed(event.getPlayer())) return;
        if (event.getItem() instanceof ItemWoodenAxe) event.setCancelled(true);
        if (InspectCommand.isInspector(event.getItem())) event.setCancelled(true);
        if (ExpandRodCommand.isRod(event.getItem())) event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        // The tools carry the same power as the commands, so they take the
        // same operator gate — otherwise a handed-out brush would bypass it.
        if (!allowed(p)) return;
        Item item = event.getItem();
        if (item == null) return;
        Session session = SessionManager.get().of(p);

        if (InspectCommand.isInspector(item)) {
            handleInspector(p, session, event);
            return;
        }

        if (ExpandRodCommand.isRod(item)) {
            handleExpandRod(p, session, event);
            return;
        }

        if (item instanceof ItemWoodenAxe) {
            var act = event.getAction();
            if (act != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK
                && act != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
            event.setCancelled(true);
            if (spam(session, act)) return;
            Block b = event.getBlock();
            if (b == null) return;
            Vec3 v = new Vec3(b.getFloorX(), b.getFloorY(), b.getFloorZ());
            if (act == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
                if (v.equals(session.pos1())) return;
                session.setPos1(p.getLevel(), v);
                p.sendMessage("§dFastEdit §7| §fpos1 §7-> §a" + v);
            } else {
                if (v.equals(session.pos2())) return;
                session.setPos2(p.getLevel(), v);
                p.sendMessage("§dFastEdit §7| §fpos2 §7-> §a" + v);
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

    private void handleExpandRod(Player p, Session session, PlayerInteractEvent event) {
        var action = event.getAction();
        if (action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK
            && action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        event.setCancelled(true);
        if (spam(session, action)) return;

        BlockFace face = event.getFace();
        Vec3 dir = face != null
            ? new Vec3(face.getXOffset(), face.getYOffset(), face.getZOffset())
            : ExpandCommand.lookStep(p);
        if (dir.x() == 0 && dir.y() == 0 && dir.z() == 0) {
            p.sendMessage("§c[FastEdit] can't tell which way to expand.");
            return;
        }

        // No selection needed: the clicked block seeds a 1-block region, then
        // it grows 1 toward the clicked face — same as with an existing one.
        Region base;
        if (session.hasSelection()) {
            base = session.region();
        } else {
            Block b = event.getBlock();
            if (b == null) { p.sendMessage("§c[FastEdit] no block in sight."); return; }
            Vec3 c = new Vec3(b.getFloorX(), b.getFloorY(), b.getFloorZ());
            base = new Region(c, c);
        }

        Region nr = ExpandCommand.expand(base, dir, 1);
        session.setPos1(p.getLevel(), nr.min());
        session.setPos2(p.getLevel(), nr.max());
        p.sendMessage("§dFastEdit §7| expand §a+1 §7" + ExpandCommand.dirName(dir)
            + " §7| §f" + nr.min() + " §7-> §f" + nr.max());
    }

    private void handleInspector(Player p, Session session, PlayerInteractEvent event) {
        var action = event.getAction();
        if (action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
            && action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR
            && action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) return;
        event.setCancelled(true);
        if (spam(session, action)) return;

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
