package fr.fastedit.listener;

import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;

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
