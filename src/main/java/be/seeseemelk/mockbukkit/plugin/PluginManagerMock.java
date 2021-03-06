package be.seeseemelk.mockbukkit.plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginCommandUtils;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.JavaPluginUtils;

import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;

public class PluginManagerMock implements PluginManager
{
	private final ServerMock server;
	private final List<Plugin> plugins = new ArrayList<>();
	private final JavaPluginLoader loader;
	private final List<PluginCommand> commands = new ArrayList<>();
	private final Map<Plugin, Listener> eventListeners = new HashMap<>();

	@SuppressWarnings("deprecation")
	public PluginManagerMock(ServerMock server)
	{
		this.server = server;
		loader = new JavaPluginLoader(this.server);
	}

	@Override
	public Plugin getPlugin(String name)
	{
		for (Plugin plugin : plugins)
		{
			if (name.equals(plugin.getName()))
			{
				return plugin;
			}
		}
		return null;
	}

	@Override
	public Plugin[] getPlugins()
	{
		return plugins.toArray(new Plugin[plugins.size()]);
	}

	/**
	 * Get a collection of all available commands.
	 * 
	 * @return A collection of all available commands.
	 */
	public Collection<PluginCommand> getCommands()
	{
		return Collections.unmodifiableList(commands);
	}

	/**
	 * Load a plugin from a class. It will use the system resource
	 * {@code plugin.yml} as the resource file.
	 * 
	 * @param description The {@link PluginDescriptionFile} that contains information about the plugin.
	 * @param class1 The plugin to load.
	 * @return The loaded plugin.
	 */
	public JavaPlugin loadPlugin(Class<? extends JavaPlugin> class1, PluginDescriptionFile description)
	{
		try
		{
			Constructor<? extends JavaPlugin> plugin = class1.getDeclaredConstructor(JavaPluginLoader.class,
					PluginDescriptionFile.class, File.class, File.class);
			plugin.setAccessible(true);
			JavaPlugin obj = plugin.newInstance(loader, description, null, null);
			plugins.add(obj);
			addCommandsFrom(obj);
			obj.onLoad();
			return obj;
		}
		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException e)
		{
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e.getTargetException());
		}
	}
	
	/**
	 * Load a plugin from a class. It will use the system resource
	 * {@code plugin.yml} as the resource file.
	 * 
	 * @param class1 The plugin to load.
	 * @return The loaded plugin.
	 */
	public JavaPlugin loadPlugin(Class<? extends JavaPlugin> class1)
	{
		try
		{
			return loadPlugin(class1, new PluginDescriptionFile(ClassLoader.getSystemResourceAsStream("plugin.yml")));
		}
		catch (InvalidDescriptionException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void callEvent(Event event) throws IllegalStateException
	{
		for (Listener listener : eventListeners.values())
		{
			for (Method method : listener.getClass().getMethods())
			{
				if (method.isAnnotationPresent(EventHandler.class) && method.getParameterCount() == 1
						&& method.getParameters()[0].getType().isInstance(event))
				{
					try
					{
						method.invoke(listener, event);
					}
					catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
					{
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	@Override
	public void registerEvents(Listener listener, Plugin plugin)
	{
		eventListeners.put(plugin, listener);
	}

	@Override
	public void enablePlugin(Plugin plugin)
	{
		if (plugin instanceof JavaPlugin)
		{
			JavaPluginUtils.setEnabled((JavaPlugin) plugin, true);
		}
		else
		{
			throw new IllegalArgumentException("Not a JavaPlugin");
		}
	}

	/**
	 * Add commands from a certain plugin to the internal list of commands.
	 * 
	 * @param plugin The plugin from which to read commands.
	 */
	@SuppressWarnings("unchecked")
	protected void addCommandsFrom(JavaPlugin plugin)
	{
		Map<String, Map<String, Object>> commands = plugin.getDescription().getCommands();
		if (commands != null)
		{
			for (Entry<String, Map<String, Object>> entry : commands.entrySet())
			{
				PluginCommand command = PluginCommandUtils.createPluginCommand(entry.getKey(), plugin);
				for (Entry<String, Object> section : entry.getValue().entrySet())
				{
					switch (section.getKey())
					{
						case "description":
							command.setDescription((String) section.getValue());
							break;
						case "aliases":
							List<String> aliases = new ArrayList<>();
							if (section.getValue() instanceof String)
							{
								aliases.add((String) section.getValue());
							}
							else
							{
								aliases.addAll((List<String>) section.getValue());
							}
							command.setAliases(aliases);
							break;
						case "permission":
							command.setPermission((String) section.getValue());
							break;
						case "permission-message":
							command.setPermissionMessage((String) section.getValue());
							break;
						case "usage":
							command.setUsage((String) section.getValue());
							break;
						default:
							throw new UnsupportedOperationException("Unknown section " + section.getKey());
					}
				}
				this.commands.add(command);
			}
		}
	}

	@Override
	public void registerInterface(Class<? extends PluginLoader> loader) throws IllegalArgumentException
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public boolean isPluginEnabled(String name)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public boolean isPluginEnabled(Plugin plugin)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public Plugin loadPlugin(File file)
			throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public Plugin[] loadPlugins(File directory)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public void disablePlugins()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public void clearPlugins()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority,
			EventExecutor executor, Plugin plugin)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority,
			EventExecutor executor, Plugin plugin, boolean ignoreCancelled)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public void disablePlugin(Plugin plugin)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public Permission getPermission(String name)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public void addPermission(Permission perm)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public void removePermission(Permission perm)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public void removePermission(String name)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public Set<Permission> getDefaultPermissions(boolean op)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public void recalculatePermissionDefaults(Permission perm)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public void subscribeToPermission(String permission, Permissible permissible)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public void unsubscribeFromPermission(String permission, Permissible permissible)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public Set<Permissible> getPermissionSubscriptions(String permission)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public void subscribeToDefaultPerms(boolean op, Permissible permissible)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public void unsubscribeFromDefaultPerms(boolean op, Permissible permissible)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public Set<Permissible> getDefaultPermSubscriptions(boolean op)
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public Set<Permission> getPermissions()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

	@Override
	public boolean useTimings()
	{
		// TODO Auto-generated method stub
		throw new UnimplementedOperationException();
	}

}
