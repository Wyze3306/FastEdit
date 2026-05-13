package fr.fastedit.command;

import cn.nukkit.Player;
import fr.fastedit.math.Vec3;
import fr.fastedit.session.Session;

public class Pos2Command extends FeCommand {
    public Pos2Command() { super("pos2", "Set position 2 to where you stand."); }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        Vec3 v = new Vec3(p.getFloorX(), p.getFloorY(), p.getFloorZ());
        session.setPos2(p.getLevel(), v);
        p.sendMessage("§dFastEdit §7| §fpos2 §7-> §a" + v);
        return true;
    }
}
