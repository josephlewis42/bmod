package bmod.plugin.generic.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

import bmod.IconLoader;
import bmod.gui.GuiExtensionPoints;
import bmod.gui.SwingSet;
import bmod.gui.widgets.HTMLPane;

/**
 * Provides links to the various OSI licenses that are used in this project.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class LicensesMenu extends GenericGuiPlugin implements ActionListener
{
	JMenuItem licenses = new JMenuItem("Open Source Licenses");
	JMenuItem apacheLicense = new JMenuItem("Apache License");
	JMenuItem hypersonicLicense = new JMenuItem("Hypersonic License");
	JMenuItem aboutBmod = new JMenuItem("~About BMOD", IconLoader.HELP_ICON); // ~ = go last in menu
	private final JMenuItem[] licensesSlice = new JMenuItem[]{
			aboutBmod,
			licenses,
			apacheLicense,
			hypersonicLicense};
	private final String LICENSES_MENU = "Help";
	private GuiExtensionPoints m_environment;
	
	@Override
	public void setup(GuiExtensionPoints environment)
	{				
		m_environment = environment;
		environment.addMenuItem(LICENSES_MENU, licensesSlice);
	}

	@Override
	public void teardown()
	{
		if(m_environment != null)
		{
			m_environment.removeMenuItem(LICENSES_MENU, licensesSlice);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String title = "", url = "";
		
		if(e.getSource() == aboutBmod)
		{
				title = "About Bmod";
				url = "bmod.html";
		}
			
		if(e.getSource() == apacheLicense)
		{
				title = "Apache License";
				url = "apache.txt";
		}
		
		if(e.getSource() == hypersonicLicense)
		{
			title = "Hypersonic License";
			url = "hypersonic_lic.txt";
		}
		
		new SwingSet(title, new JScrollPane(new HTMLPane(url)), JFrame.HIDE_ON_CLOSE, false).setSize(640,480);
		
	}

	public LicensesMenu()
	{
		super("Licenses Menu", "Allows you to view software licenses associated with Bmod");
		
		licenses.setEnabled(false);

		aboutBmod.addActionListener(this);
		apacheLicense.addActionListener(this);
		hypersonicLicense.addActionListener(this);
	}
}
