package fr.fastedit;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandMap;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginManager;
import fr.fastedit.command.BrushCommand;
import fr.fastedit.command.CopyCommand;
import fr.fastedit.command.CutCommand;
import fr.fastedit.command.CylCommand;
import fr.fastedit.command.FlipCommand;
import fr.fastedit.command.InspectCommand;
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
import fr.fastedit.clipboard.UnknownBlocks;
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
        try {
            EditEngine.boot(this);
            UnknownBlocks.load();

            PluginManager pm = getServer().getPluginManager();
            safeRegister(pm, new WandListener());
            safeRegister(pm, new CommandAliasListener());

            CommandMap map = getServer().getCommandMap();
            register(map, new WandCommand());
            register(map, new Pos1Command());
            register(map, new Pos2Command());
            register(map, new SelCommand());
            register(map, new SizeCommand());
            register(map, new SetCommand());
            register(map, new ReplaceCommand());
            register(map, new WallsCommand());
            register(map, new SphereCommand());
            register(map, new CylCommand());
            register(map, new PyramidCommand());
            register(map, new CopyCommand());
            register(map, new CutCommand());
            register(map, new PasteCommand());
            register(map, new RotateCommand());
            register(map, new FlipCommand());
            register(map, new MoveCommand());
            register(map, new StackCommand());
            register(map, new UndoCommand());
            register(map, new RedoCommand());
            register(map, new SchemCommand());
            register(map, new BrushCommand());
            register(map, new MaskCommand());
            register(map, new InspectCommand());

            getLogger().info("§aFastEdit ready — //wand to start.");
        } catch (Throwable t) {
            getLogger().error("[FastEdit] startup failed: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            t.printStackTrace();
        }
    }

    private void safeRegister(PluginManager pm, cn.nukkit.event.Listener listener) {
        try { pm.registerEvents(listener, this); }
        catch (Throwable t) {
            getLogger().error("[FastEdit] listener " + listener.getClass().getSimpleName() + " failed: " + t.getMessage());
        }
    }

    private void register(CommandMap map, Command cmd) {
        try { map.register("fastedit", cmd); }
        catch (Throwable t) {
            getLogger().error("[FastEdit] command " + cmd.getName() + " failed to register: " + t.getMessage());
        }
    }
}
