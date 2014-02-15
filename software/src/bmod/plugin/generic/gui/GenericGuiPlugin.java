package bmod.plugin.generic.gui;

import bmod.GenericPlugin;
import bmod.gui.GuiExtensionPoints;

/**
 * A generic GUI plugin that can modify the environment and listen to signals.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public abstract class GenericGuiPlugin extends GenericPlugin
{
	public GenericGuiPlugin(String pluginName, String pluginDescription)
	{
		super(pluginName, pluginDescription);
	}
	
	public abstract void setup(GuiExtensionPoints environment);
	
	/**
	 * We're going to get something better than ExtensionPoints.
	 */
	@Override
	public void setupHeadless()
	{
	}
}
