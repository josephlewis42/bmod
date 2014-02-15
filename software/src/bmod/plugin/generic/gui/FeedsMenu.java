package bmod.plugin.generic.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import bmod.IconLoader;
import bmod.gui.GuiExtensionPoints;
import bmod.plugin.generic.headless.SmartGridProvider;

/**
 * Displays the status of the feeds and the connection to the server.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class FeedsMenu extends GenericGuiPlugin implements ActionListener
{
	private final JMenuItem m_refreshButton = new JMenuItem("Refresh Feeds", IconLoader.REFRESH_ICON);
	private final JMenuItem[] m_menu = new JMenuItem[]{m_refreshButton};
	private GuiExtensionPoints m_environment;
	private static final String MENU_NAME = "Feeds";

	@Override
	public void setup(final GuiExtensionPoints environment)
	{
		m_environment = environment;
		m_environment.addMenuItem(MENU_NAME, m_menu);
	}

	@Override
	public void teardown()
	{
		if(m_environment != null)
		{
			m_environment.removeMenuItem(MENU_NAME, m_menu);
		}
	}

	
	public FeedsMenu()
	{
		super("Refresh Feeds Button", "Adds a button for refreshing feeds");
		
		m_refreshButton.addActionListener(this);
	}

	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(m_environment != null)
		{
			GuiExtensionPoints.showInfo("Refreshing feeds, please wait...");
			if(!SmartGridProvider.refreshFeeds())
			{
				m_environment.showError("Could not refresh feeds.");
			}
		}
	}
}
