package bmod.plugin.generic.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import bmod.ManifestParser;
import bmod.gui.GuiExtensionPoints;

/**
 * Shows information critical for debugging purposes.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class DebugInfoMenuItem extends GenericGuiPlugin implements ActionListener
{
	private GuiExtensionPoints m_environment = null;
	private final JMenuItem debugItem = new JMenuItem("Debug Information");
	private final JMenuItem[] menu = new JMenuItem[]{debugItem};
	
	public DebugInfoMenuItem()
	{
		super("Debugging Information Menu Item",
				"Adds a menu item that shows debugging information about the system");
		
		debugItem.addActionListener(this);
	}
	
	@Override
	public void setup(GuiExtensionPoints environment)
	{
		m_environment = environment;
		environment.addMenuItem("Help", menu);
	}

	@Override
	public void teardown()
	{
		if(m_environment != null)
		{
			m_environment.removeMenuItem("Help", menu);
		}
	}

	

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == debugItem)
		{
			m_environment.showWindow("Debugging Info", 
					ManifestParser.getPropertiesStartsWith());
		}
	}
}
