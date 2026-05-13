package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;

public class Pos1Command extends FeCommand {
    public Pos1Command() { super("pos1", "Set position 1 to where you stand."); }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        Vec3 v = new Vec3(p.getFloorX(), p.getFloorY(), p.getFloorZ());
        session.setPos1(p.getLevel(), v);
        p.sendMessage("§dFastEdit §7| §fpos1 §7-> §a" + v);
        return true;
    }
}
