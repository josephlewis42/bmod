package bmod.plugin.loader;

import java.util.LinkedList;

import bmod.ExtensionPoints;
import bmod.gui.GuiExtensionPoints;
import bmod.plugin.PluginManager;
import bmod.plugin.generic.gui.GenericGuiPlugin;

/**
 * This loader handles all GUI plugins that really don't have another home.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class GenericGuiPluginLoader extends PluginManager<GenericGuiPlugin>
{
	private final static LinkedList<GenericGuiPlugin> m_enabled = new LinkedList<GenericGuiPlugin>();
	private final static LinkedList<GenericGuiPlugin> m_disabled = new LinkedList<GenericGuiPlugin>();

	private final GenericHeadlessPluginLoader m_genericPlugins;
	private static GuiExtensionPoints m_extensionPoints = null;
	public GenericGuiPluginLoader(GuiExtensionPoints pts)
	{
		super("bmod/plugin/generic/gui");
		
		if(m_extensionPoints == null)
		{
			m_extensionPoints = pts;
		}
		
			
		for(GenericGuiPlugin tmp : getPlugins())
		{
			try
			{
				tmp.setup(pts);
				m_enabled.add(tmp);
				
			}catch(ClassCastException ex)
			{
				m_logger.debug("Couldn't cast: " + tmp + " to GenericGuiPlugin: " + ex.getMessage());
			}
		}
		
		m_genericPlugins = new GenericHeadlessPluginLoader();
	}
	
	/**
	 * Called before shutdown, so all widgets have a chance to die with dignity.
	 */
	public void teardown()
	{
		ExtensionPoints.setCurrentPredictionModel(null);
		for(GenericGuiPlugin tmp : m_enabled)
		{
			m_logger.info("Tearing Down: " + tmp);
			tmp.teardown();
		}
		
		m_genericPlugins.teardown();
	}
	
	public static void enable(GenericGuiPlugin tmp)
	{
		System.out.println("Enabling: " + tmp);
		if(m_disabled.contains(tmp))
		{
			m_disabled.remove(tmp);
			m_enabled.add(tmp);
			
			tmp.setup(m_extensionPoints);
		}
	}
	
	public static void disable(GenericGuiPlugin tmp)
	{
		System.out.println("Disabling: " + tmp);
		if(m_enabled.contains(tmp))
		{
			m_enabled.remove(tmp);
			m_disabled.add(tmp);
			
			tmp.teardown();
		}
	}
	
	/**
	 * Tests if a plugin is enabled or not.
	 * @param test
	 * @return
	 */
	public static boolean isEnabled(GenericGuiPlugin test)
	{
		return m_enabled.contains(test);
	}

	/**
	 * Returns all GenericGuiPlugins
	 * @return
	 */
	public static LinkedList<GenericGuiPlugin> getAllPlugins()
	{
		LinkedList<GenericGuiPlugin> all = new LinkedList<>();
		all.addAll(m_disabled);
		all.addAll(m_enabled);
		
		return all;
	}
	
	public static LinkedList<GenericGuiPlugin> getEnabledPlugins()
	{
		return new LinkedList<GenericGuiPlugin>(m_enabled);
	}
	
	
}
