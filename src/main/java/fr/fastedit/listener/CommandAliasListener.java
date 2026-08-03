package fr.fastedit.listener;

import org.powernukkitx.Server;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerCommandPreprocessEvent;

public class CommandAliasListener implements Listener {

    @EventHandler
    public void onPreprocess(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage();
        if (msg == null || msg.length() < 3) return;
        if (msg.charAt(0) != '/' || msg.charAt(1) != '/') return;
        Server.getInstance().executeCommand(event.getPlayer(), msg.substring(1));
        event.setCancelled();
    }
}
