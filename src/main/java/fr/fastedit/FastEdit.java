package fr.fastedit;

import cn.nukkit.command.CommandMap;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginManager;
import fr.fastedit.command.BrushCommand;
import fr.fastedit.command.CopyCommand;
import fr.fastedit.command.CutCommand;
import fr.fastedit.command.CylCommand;
import fr.fastedit.command.FlipCommand;
import fr.fastedit.command.MaskCommand;
import fr.fastedit.command.MoveCommand;
import fr.fastedit.command.PasteCommand;
import fr.fastedit.command.Pos1Command;
import fr.fastedit.command.Pos2Command;
import fr.fastedit.command.PyramidCommand;
import fr.fastedit.command.RedoCommand;
import fr.fastedit.command.ReplaceCommand;
import fr.fastedit.command.RotateCommand;
import fr.fastedit.command.SchemCommand;
import fr.fastedit.command.SelCommand;
import fr.fastedit.command.SetCommand;
import fr.fastedit.command.SizeCommand;
import fr.fastedit.command.SphereCommand;
import fr.fastedit.command.StackCommand;
import fr.fastedit.command.UndoCommand;
import fr.fastedit.command.WallsCommand;
import fr.fastedit.command.WandCommand;
import fr.fastedit.edit.EditEngine;
import fr.fastedit.listener.CommandAliasListener;
import fr.fastedit.listener.WandListener;

public class FastEdit extends PluginBase {

    private static FastEdit INSTANCE;
    public static FastEdit get() { return INSTANCE; }

    @Override
    public void onLoad() { INSTANCE = this; }

    @Override
    public void onEnable() {
        EditEngine.boot(this);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new WandListener(), this);
        pm.registerEvents(new CommandAliasListener(), this);

        CommandMap map = getServer().getCommandMap();
        map.register("fastedit", new WandCommand());
        map.register("fastedit", new Pos1Command());
        map.register("fastedit", new Pos2Command());
        map.register("fastedit", new SelCommand());
        map.register("fastedit", new SizeCommand());
        map.register("fastedit", new SetCommand());
        map.register("fastedit", new ReplaceCommand());
        map.register("fastedit", new WallsCommand());
        map.register("fastedit", new SphereCommand());
        map.register("fastedit", new CylCommand());
        map.register("fastedit", new PyramidCommand());
        map.register("fastedit", new CopyCommand());
        map.register("fastedit", new CutCommand());
        map.register("fastedit", new PasteCommand());
        map.register("fastedit", new RotateCommand());
        map.register("fastedit", new FlipCommand());
        map.register("fastedit", new MoveCommand());
        map.register("fastedit", new StackCommand());
        map.register("fastedit", new UndoCommand());
        map.register("fastedit", new RedoCommand());
        map.register("fastedit", new SchemCommand());
        map.register("fastedit", new BrushCommand());
        map.register("fastedit", new MaskCommand());

        getLogger().info("§aFastEdit ready — //wand to start.");
    }
}
