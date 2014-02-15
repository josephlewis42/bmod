package bmod.plugin.generic.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import bmod.database.Database;
import bmod.gui.GuiExtensionPoints;
import bmod.gui.widgets.Dialogs;
import bmod.gui.widgets.JProgressDialog;
import bmod.gui.widgets.ProgressDialog;

/**
 * Allows the user to refresh the database from the menu.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class DatabaseRefreshMenu extends GenericGuiPlugin
{
	private final JMenuItem resetDB = new JMenuItem("Reset Database from Web");
	private final JMenuItem cleanupDB = new JMenuItem("Clean up the Database");
	private final JMenuItem[] menu = new JMenuItem[]{resetDB, cleanupDB};
	private final String MENU_LOCATION  = "Database";
	private GuiExtensionPoints m_environment;
	
	public DatabaseRefreshMenu()
	{
		super("Refresh/Cleanup Database", "Adds buttons to reset the database from the web or clean it up.");
		
		
		resetDB.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(Dialogs.showYesNoQuestionDialog("Confirm", "<html>This will "+
						"<b>delete all your work</b> are you sure you want to "+
						"continue?</html>"))
				{
					ProgressDialog pd = new JProgressDialog("Updating DB", "Fetching Database",100);
					pd.setIndeterminate(true);
					pd.setProgress("Fetching...",1);
					Database.getDqm().resetDB();
					pd.close();
				}
			}
		});
		
		cleanupDB.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				int num = Database.getDqm().deleteUnusedTables();
				
				GuiExtensionPoints.showInfo("Deleted " + num + " unused tables.");
			}
		});
	}

	@Override
	public void setup(final GuiExtensionPoints environment)
	{
		m_environment = environment;
		m_environment.addMenuItem(MENU_LOCATION, menu);
	}

	@Override
	public void teardown()
	{
		if(m_environment != null)
		{
			m_environment.removeMenuItem(MENU_LOCATION, menu);
		}
	}
}
