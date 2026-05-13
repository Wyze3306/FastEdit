package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;

public final class Pos1Cmd extends Cmd {
    public Pos1Cmd() { super("/pos1", "Set position 1 to where you stand.", "//pos1"); }

    @Override
    protected boolean run(Player player, Session session, String[] args) {
        Vec3 v = new Vec3(player.getFloorX(), player.getFloorY(), player.getFloorZ());
        session.setPos1(player.getLevel(), v);
        player.sendMessage("§dFastEdit §7| §fpos1 §7-> §a" + v);
        return true;
    }
}
