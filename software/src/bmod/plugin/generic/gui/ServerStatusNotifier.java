package bmod.plugin.generic.gui;

import bmod.gui.GuiExtensionPoints;
import bmod.plugin.generic.headless.SmartGridProvider;
import bmod.util.DateTime;
import edu.du.cs.smartgrid.SmartGridProviderEventListener;

/**
 * 
 * Displays a subtle alert when the server connection status changes, better
 * than calling GUI code from our old place in the actual provider.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class ServerStatusNotifier extends GenericGuiPlugin implements
		SmartGridProviderEventListener
{
	@Override
	public void onCacheRequest(DateTime start, DateTime end, int feedid)
	{

	}

	@Override
	public void onServerOffline()
	{
		GuiExtensionPoints.showInfo("The server is now offline...trying to reconnect.");
	}

	@Override
	public void onServerConnected()
	{
		GuiExtensionPoints.showInfo("The server is now online.");
	}

	@Override
	public void setup(GuiExtensionPoints environment)
	{
		SmartGridProvider.addProviderListener(this);
	}

	@Override
	public void teardown()
	{

	}
	
	@Override
	public double onNoFeedValue(int feedId, DateTime feedTime, double finalValue)
	{
		return finalValue;
	}

	public ServerStatusNotifier()
	{
		super( "Server Status Notifier",
				"A background process that notifys you when the server cannot" +
				" be reached or when it comes back online.");
	}
}
