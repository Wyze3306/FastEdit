package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;

public final class Pos2Cmd extends Cmd {
    public Pos2Cmd() { super("/pos2", "Set position 2 to where you stand.", "//pos2"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        Vec3 v = new Vec3(player.getFloorX(), player.getFloorY(), player.getFloorZ());
        session.setPos2(player.getLevel(), v);
        player.sendMessage("§dFastEdit §7| §fpos2 §7-> §a" + v);
        return true;
    }
}
