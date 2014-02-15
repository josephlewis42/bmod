package bmod.plugin.loader;

import java.util.HashMap;

import bmod.plugin.PluginManager;
import bmod.plugin.baseclass.ActivityPlugin;

public class WattageCalculator extends PluginManager<ActivityPlugin>
{
	private final HashMap<String, ActivityPlugin> pluginMap = new HashMap<String, ActivityPlugin>();
	
	public WattageCalculator()
	{
		super("bmod/plugin/wattagecalculator");
		
		for(ActivityPlugin p : getPlugins()){
			pluginMap.put(p.getName(), p);
		}
	}
	
	
	/**
	 * Returns a plugin with the given name, or null if it doesn't exist.
	 * 
	 * @param name - the name of the plugin
	 * @return The plugin with the given name, or null
	 * @throws IllegalArgumentException
	 */
	public ActivityPlugin getPluginByName(String name)
	{
		return pluginMap.get(name);
	}
}
