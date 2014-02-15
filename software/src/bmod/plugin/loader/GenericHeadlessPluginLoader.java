package bmod.plugin.loader;

import java.util.LinkedList;

import bmod.GenericPlugin;
import bmod.plugin.PluginManager;

/**
 * Responsible for loading headless plugins, the kind that don't interact with
 * the GUI but may interact with the underlying logic of the system.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 */
public class GenericHeadlessPluginLoader extends PluginManager<GenericPlugin>
{	
	private static final LinkedList<GenericPlugin> m_loaded = new LinkedList<GenericPlugin>();

	public GenericHeadlessPluginLoader()
	{
		super("bmod/plugin/generic/headless");
		
		for(GenericPlugin tmp : getPlugins())
		{
			try
			{
				tmp.setupHeadless();
				m_loaded.add(tmp);
				
			}catch(ClassCastException ex)
			{
				m_logger.debug("Couldn't cast: " + tmp + " to GenericPlugin: " + ex.getMessage());
			}
		}
	}

	public void teardown()
	{
		for(GenericPlugin tmp : m_loaded)
		{
			m_logger.info("Tearing Down: " + tmp);
			tmp.teardown();
		}
	}
	
	/**
	 * @return a list of all loaded GenericPlugins.
	 */
	public static LinkedList<GenericPlugin> getAllPlugins()
	{
		return m_loaded;
	}
	
}
