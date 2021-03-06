package be.seeseemelk.mockbukkit;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

public class TestPlugin extends JavaPlugin implements Listener
{
	public boolean onEnableExecuted = false;
	public CommandSender commandSender;
	public Command command;
	public String commandLabel;
	public String[] commandArguments;
	public boolean commandReturns;
	public boolean unannotatedPlayerInteractEventExecuted = false;
	public boolean annotatedPlayerInteractEventExecuted = false;
	public boolean annotatedBlockBreakEventExecuted = false;

	public TestPlugin()
	{
		super();
	}

	protected TestPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file)
	{
		super(loader, description, dataFolder, file);
	}
	
	@Override
	public void onEnable()
	{
		onEnableExecuted = true;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		this.commandSender = sender;
		this.command = command;
		this.commandLabel = label;
		this.commandArguments = args;
		return commandReturns;
	}
	
	public void unannotatedEventHandler(PlayerInteractEvent event)
	{
		unannotatedPlayerInteractEventExecuted = true;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		annotatedPlayerInteractEventExecuted = true;
	}
	
	@EventHandler
	public void onBlockEvent(BlockBreakEvent event)
	{
		annotatedBlockBreakEventExecuted = true;
	}
}
