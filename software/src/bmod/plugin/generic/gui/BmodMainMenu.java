package bmod.plugin.generic.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import bmod.gui.GuiExtensionPoints;

/**
 * A simple main menu for bmod.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class BmodMainMenu extends GenericGuiPlugin
{
	private final JMenuItem quitButton = new JMenuItem("~Quit"); // ~ makes go last in menu
	private final JMenuItem[] quit_slice = new JMenuItem[]{quitButton};
	private final String MENU_NAME = "Bmod";
	private GuiExtensionPoints m_environment;
	
	public BmodMainMenu()
	{
		super("Quit Button", "Adds a quit button to the bmod menu.");
		
		quitButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_DOWN_MASK ));
		quitButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				System.exit(0);
			}
		});
	}

	@Override
	public void setup(GuiExtensionPoints environment)
	{
		m_environment = environment;
		environment.addMenuItem(MENU_NAME, quit_slice);
	}

	@Override
	public void teardown()
	{
		if(m_environment != null)
		{
			m_environment.removeMenuItem(MENU_NAME, quit_slice);
		}
	}
}
