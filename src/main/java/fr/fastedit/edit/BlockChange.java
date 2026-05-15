package fr.fastedit.edit;

import cn.nukkit.block.BlockState;
import fr.fastedit.math.Vec3;

public final class BlockChange {

    public final Vec3 pos;
    public BlockState target;
    public BlockState previous;
    /** Optional Bedrock layer-1 (waterlog) state; null = leave/clear layer 1. */
    public BlockState layer1;
    public BlockState prevLayer1;

    public BlockChange(Vec3 pos, BlockState target) {
        this.pos = pos;
        this.target = target;
    }

    public BlockChange(Vec3 pos, BlockState target, BlockState layer1) {
        this.pos = pos;
        this.target = target;
        this.layer1 = layer1;
    }
}
