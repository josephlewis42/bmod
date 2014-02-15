package bmod.plugin.generic.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.swing.JMenuItem;

import bmod.ConfigReader;
import bmod.IconLoader;
import bmod.ManifestParser;
import bmod.gui.GuiExtensionPoints;

/**
 * Provides a quick way for reporting bugs.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class ReportBugMenuItem extends GenericGuiPlugin
{
	private static final String htmlFilePath = "http://smartgrid.cs.du.edu/bugs/newticket/"; // path to your new file
	private final JMenuItem item = new JMenuItem(" Report A Bug"); // prefaced with space to put first in menu
	private final JMenuItem[] itemGroup = new JMenuItem[]{item};
	private GuiExtensionPoints m_environment;

	@Override
	public void setup(final GuiExtensionPoints environment)
	{
		m_environment = environment;
		m_environment.addMenuItem("Help", itemGroup);
	}

	@Override
	public void teardown()
	{
		if(m_environment != null)
		{
			m_environment.removeMenuItem("Help", itemGroup);
		}
	}
	
	
	public static String getSystemInfo()
	{
		StringBuilder sb = new StringBuilder();
		
		String[] sysProps = new String[]{"java.home",
				"java.version",
				"os.arch",
				"os.name",
				"os.version",
				"user.dir"};
		
		for(String prop : sysProps)
		{
			sb.append(prop);
			sb.append(":\t");
			try
			{
				sb.append(System.getProperty(prop));
			}catch(Exception ex)
			{
				sb.append("N/A");
			}
			
			sb.append("\n");
		}
		return sb.toString();
	}

	public ReportBugMenuItem()
	{
		super( "Report A Bug Button","A menu item that helps you report bugs");
		
		// Grab the whole environment, and post it.
				String defaultInfo = new ManifestParser().getStrPropertiesStartingWith("Build");
				defaultInfo = "What did you do?\n\nWhat happened?\n\nWhat did you expect to happen?\n\n\n\n---SYSTEM INFO, DO NOT DELETE BELOW THIS LINE---\n\n" + defaultInfo;
				defaultInfo = defaultInfo + getSystemInfo() + ConfigReader.getAll();
				
				try
				{
					defaultInfo = URLEncoder.encode(defaultInfo, "ISO-8859-1");
				} catch (UnsupportedEncodingException e1)
				{
					defaultInfo = "";
				}
						
				final String url = htmlFilePath + "?component=Bmod+Java+Client&description=" + defaultInfo;
				
				
				item.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						try
						{
							java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
						} catch (IOException e)
						{
							m_environment.showError("Could not open: " + url);
						}				
					}
					
				});
				
				item.setIcon(IconLoader.BUG);
	}
}
