package fr.fastedit;

import cn.nukkit.command.Command;
import cn.nukkit.plugin.PluginBase;
import fr.fastedit.command.BrushCmd;
import fr.fastedit.command.CopyCmd;
import fr.fastedit.command.CutCmd;
import fr.fastedit.command.CylCmd;
import fr.fastedit.command.FlipCmd;
import fr.fastedit.command.MaskCmd;
import fr.fastedit.command.MoveCmd;
import fr.fastedit.command.PasteCmd;
import fr.fastedit.command.Pos1Cmd;
import fr.fastedit.command.Pos2Cmd;
import fr.fastedit.command.PyramidCmd;
import fr.fastedit.command.RedoCmd;
import fr.fastedit.command.ReplaceCmd;
import fr.fastedit.command.RotateCmd;
import fr.fastedit.command.SchemCmd;
import fr.fastedit.command.SelCmd;
import fr.fastedit.command.SetCmd;
import fr.fastedit.command.SizeCmd;
import fr.fastedit.command.SphereCmd;
import fr.fastedit.command.StackCmd;
import fr.fastedit.command.UndoCmd;
import fr.fastedit.command.WallsCmd;
import fr.fastedit.command.WandCmd;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.listener.WandListener;

public final class FastEdit extends PluginBase {

    private static FastEdit instance;
    public static FastEdit get() { return instance; }

    @Override
    public void onLoad() { instance = this; }

    @Override
    public void onEnable() {
        EditEngine.boot(this);
        getServer().getPluginManager().registerEvents(new WandListener(), this);
        registerCommands(
            new WandCmd(),
            new Pos1Cmd(),
            new Pos2Cmd(),
            new SelCmd(),
            new SizeCmd(),
            new SetCmd(),
            new ReplaceCmd(),
            new WallsCmd(),
            new SphereCmd(),
            new CylCmd(),
            new PyramidCmd(),
            new CopyCmd(),
            new CutCmd(),
            new PasteCmd(),
            new RotateCmd(),
            new FlipCmd(),
            new MoveCmd(),
            new StackCmd(),
            new UndoCmd(),
            new RedoCmd(),
            new SchemCmd(),
            new BrushCmd(),
            new MaskCmd()
        );
        getLogger().info("§aFastEdit ready — try //wand.");
    }

    private void registerCommands(Command... commands) {
        var map = getServer().getCommandMap();
        for (Command c : commands) map.register("fastedit", c);
    }
}
