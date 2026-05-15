package fr.fastedit.command;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.tag.CompoundTag;
import fr.fastedit.block.Mask;
import fr.fastedit.brush.Brushes;
import fr.fastedit.session.Session;

public class MaskCommand extends FeCommand {
    public MaskCommand() { super("mask", "Set the mask for the brush on the held shovel.");
        params(txt("mask", true));
    }

    @Override
    protected boolean run(Player p, Session session, String[] args) {
        Item held = p.getInventory().getItemInMainHand();
        require(Brushes.hasBrush(held), "hold a shovel with a brush — //brush ... first");

        CompoundTag tag = held.getNamedTag();
        CompoundTag brush = tag.getCompound(Brushes.NBT_KEY);

        if (args.length == 0 || args[0].equalsIgnoreCase("none")) {
            brush.remove("mask");
            p.sendMessage("§dFastEdit §7| brush mask cleared.");
        } else {
            Mask.parse(args[0]);
            brush.putString("mask", args[0]);
            p.sendMessage("§dFastEdit §7| brush mask set: §f" + args[0]);
        }
        tag.putCompound(Brushes.NBT_KEY, brush);
        held.setNamedTag(tag);
        p.getInventory().setItemInMainHand(held);
        return true;
    }
}
