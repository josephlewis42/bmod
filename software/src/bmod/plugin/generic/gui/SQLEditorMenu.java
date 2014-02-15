package bmod.plugin.generic.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JMenuItem;

import bmod.database.Database;
import bmod.database.DatabaseQueryMechanism;
import bmod.gui.GuiExtensionPoints;
import bmod.gui.widgets.Dialogs;
import bmod.gui.widgets.NTable;

/**
 * Allows users to run SQL statements against the local database.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class SQLEditorMenu extends GenericGuiPlugin implements ActionListener
{
	private final JMenuItem editSQL = new JMenuItem("Run a SQL SELECT.");
	private final JMenuItem udiSQL = new JMenuItem("Run a SQL UPDATE/DROP/INSERT.");
	private static final String MENU_CATEGORY = "Database";
	private final JMenuItem[] menu_slice = new JMenuItem[]{editSQL, udiSQL};
	private GuiExtensionPoints m_environment;
	
	@Override
	public void setup(final GuiExtensionPoints environment)
	{
		m_environment = environment;
		environment.addMenuItem(MENU_CATEGORY, menu_slice);
	}

	@Override
	public void teardown()
	{
		if(m_environment != null)
		{
			m_environment.removeMenuItem(MENU_CATEGORY, menu_slice);
		}
	}

	public SQLEditorMenu()
	{
		super( "SQL Editor",
				"Allows you to directly edit the database underlying Bmod" +
				" for quick bulk changes.");
		
		udiSQL.addActionListener(this);
		editSQL.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent evt)
	{
		if(evt.getSource() == udiSQL)
		{
			String sql = Dialogs.showUserInputDialog("SQL Update/Delete/Insert", "Enter SQL:");
			
			if(sql == null)
			{
				return;
			}
			
			try
			{
				Dialogs.showInformationDialog("Output:", "" + Database.getDqm().executeSQLResult(sql));
			} catch (SQLException e)
			{
				Dialogs.showErrorDialog("ERROR:", e.getMessage());
			}
		}
		
		// code for editing the database.
		if(evt.getSource() == editSQL)
		{
			String sql = Dialogs.showUserInputDialog("SQL EDITOR", "Enter SQL:");
			
			if(sql == null)
				return;
			
			try
			{
				ResultSet s = Database.getDqm().executeSQLQuery(sql);
				
				Database.getDqm();
				Object[][] results = DatabaseQueryMechanism.dump(s);
				String[] header = DatabaseQueryMechanism.dumpHeader(s);
				NTable result = new NTable(false, true, false, false);
				
				result.setTable(header, results);
				
				m_environment.createWindow("Query Results",result);
				
			} catch (SQLException e)
			{
				Dialogs.showErrorDialog("ERROR:", e.getMessage());
			}
		}
	}
}
