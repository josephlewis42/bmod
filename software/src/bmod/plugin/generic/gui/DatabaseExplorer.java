package bmod.plugin.generic.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

import bmod.database.Database;
import bmod.database.DatabaseQueryMechanism;
import bmod.gui.GuiExtensionPoints;
import bmod.gui.widgets.NTable;

/**
 * A simple main menu for bmod.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class DatabaseExplorer extends GenericGuiPlugin
{
	public DatabaseExplorer()
	{
		super("Database Explorer", "Adds the ability to explore databases from Bmod.");
		
		exploreButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
			    new DatabaseBrowser(Database.getDqm().getConnection());
			}
		});
	}



	private final JMenuItem exploreButton = new JMenuItem("Database Explorer");
	private final JMenuItem[] explorerSlice = new JMenuItem[]{exploreButton};
	private final String MENU_LOCATION = "Database";
	private GuiExtensionPoints m_environment;

	@Override
	public void setup(final GuiExtensionPoints environment)
	{
		m_environment = environment;
		environment.addMenuItem(MENU_LOCATION, explorerSlice);
	}

	@Override
	public void teardown()
	{		
		if(m_environment != null)
		{
			m_environment.removeMenuItem(MENU_LOCATION, explorerSlice);
		}
	}
	
	

	public class DatabaseBrowser extends JFrame {

	  /**
		 * 
		 */
	private static final long serialVersionUID = 1L;
	protected Connection connection;
	  protected final JComboBox<String> catalogBox = new JComboBox<>();
	  protected final JComboBox<String> schemaBox = new JComboBox<>();
	  protected final JComboBox<String> tableBox = new JComboBox<>();

	 // protected JTable table = new JTable();
	  protected NTable table = new NTable(true, true, false, false);

	  public DatabaseBrowser(Connection conn) {
	    connection = conn;
	    Container pane = getContentPane();

	    populateCatalogBox();
	    populateSchemaBox();
	    populateTableBox();
	    
	    table.addToolbarEnd(new JLabel("Catalog"));
	    table.addToolbarEnd(catalogBox);
	    table.addToolbarEnd(new JLabel("Schema"));
	    table.addToolbarEnd(schemaBox);
	    table.addToolbarEnd(new JLabel("Table"));
	    table.addToolbarEnd(tableBox);



	    catalogBox.addItemListener(new ItemListener() {
	      @Override
		public void itemStateChanged(ItemEvent event) {
	        String newCatalog = (String) (catalogBox.getSelectedItem());
	        try {
	          connection.setCatalog(newCatalog);
	        } catch (Exception e) {
	        }
	        populateSchemaBox();
	        populateTableBox();
	        refreshTable();
	      }
	    });

	    schemaBox.addItemListener(new ItemListener() {
	      @Override
		public void itemStateChanged(ItemEvent event) {
	        populateTableBox();
	        refreshTable();
	      }
	    });

	    tableBox.addItemListener(new ItemListener() {
	      @Override
		public void itemStateChanged(ItemEvent event) {
	        refreshTable();
	      }
	    });
	    
	    pane.add(new JScrollPane(table), BorderLayout.CENTER);

	    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    setSize(640, 480);
	    
	    refreshTable();

	    setVisible(true);
	  }

	  protected void populateCatalogBox() {
	    try {
	      DatabaseMetaData dmd = connection.getMetaData();
	      ResultSet rset = dmd.getCatalogs();
	      Vector<String> values = new Vector<>();
	      while (rset.next()) {
	        values.addElement(rset.getString(1));
	      }
	      rset.close();
	      catalogBox.setModel(new DefaultComboBoxModel<>(values));
	      catalogBox.setSelectedItem(connection.getCatalog());
	      catalogBox.setEnabled(values.size() > 0);
	    } catch (Exception e) {
	      catalogBox.setEnabled(false);
	    }
	  }

	  protected void populateSchemaBox() {
	    try {
	      DatabaseMetaData dmd = connection.getMetaData();
	      ResultSet rset = dmd.getSchemas();
	      Vector<String> values = new Vector<>();
	      while (rset.next()) {
	        values.addElement(rset.getString(1));
	      }
	      rset.close();
	      schemaBox.setModel(new DefaultComboBoxModel<String>(values));
	      schemaBox.setEnabled(values.size() > 0);
	    } catch (Exception e) {
	      schemaBox.setEnabled(false);
	    }
	  }

	  protected void populateTableBox() {
	    try {
	      String[] types = { "TABLE" };
	      String catalog = connection.getCatalog();
	      String schema = (String) (schemaBox.getSelectedItem());
	      DatabaseMetaData dmd = connection.getMetaData();
	      ResultSet rset = dmd.getTables(catalog, schema, null, types);
	      Vector<String> values = new Vector<>();
	      while (rset.next()) {
	        values.addElement(rset.getString(3));
	      }
	      rset.close();
	      tableBox.setModel(new DefaultComboBoxModel<String>(values));
	      tableBox.setEnabled(values.size() > 0);
	    } catch (Exception e) {
	      tableBox.setEnabled(false);
	    }
	  }

	  protected void refreshTable() {
	    String schema = (schemaBox.isEnabled() ? schemaBox.getSelectedItem().toString() : null);
	    String tableName = (String) tableBox.getSelectedItem();
	    if (tableName == null) {
	      return;
	    }
	    String selectTable = (schema == null ? "" : schema + ".") + tableName;
	    if (selectTable.indexOf(' ') > 0) {
	      selectTable = "\"" + selectTable + "\"";
	    }
	    try {
	      Statement stmt = connection.createStatement();
	      ResultSet rset = stmt.executeQuery("SELECT * FROM \"" + tableName + "\" LIMIT 0, 100");
	      
	      Object[][] results = DatabaseQueryMechanism.dump(rset);
	      String[] header = DatabaseQueryMechanism.dumpHeader(rset);			
	      table.setTable(header, results);
	    } catch (Exception e) {
	    }
	  }
	}
}
