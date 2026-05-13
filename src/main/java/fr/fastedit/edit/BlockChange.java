package fr.fastedit.edit;

import cn.nukkit.block.BlockState;
import fr.fastedit.math.Vec3;

public final class BlockChange {

    public final Vec3 pos;
    public BlockState target;
    public BlockState previous;

    public BlockChange(Vec3 pos, BlockState target) {
        this.pos = pos;
        this.target = target;
    }
}
