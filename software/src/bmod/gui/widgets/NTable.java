package bmod.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


/**
 * This is a class for creating JTables, but provides much more 
 * built in functionality, such as sorting, enabling/disabling 
 * editing, csv export, etc.
 * 
 * @author Joseph Lewis
 *
 */
public class NTable extends JPanel implements TableModelListener
{
	// Action IDs
	public static final int TABLE_CHANGED = 0;
	
	private static final String NUMBER_REGEX = "-?\\d+(\\.\\d+)?";
	private static final long serialVersionUID = 1L;
	protected JTable m_table = new JTable();
	private String[] m_colHeader;
	protected JToolBar m_toolbar = new JToolBar();
	protected JButton m_csvExportButton = new JButton("Save...");
	protected JButton m_printButton = new JButton("Print...");
	protected transient TableRowSorter<TableModel> sorter;
	protected JLabel m_filterLabel = new JLabel("Filter:");
	protected JTextField m_filterBox = new JTextField(10);
	protected JButton m_filterButton = new JButton("Filter");
	
	protected boolean canEditTable;
	
	
	public NTable(boolean canExportCSV, boolean canPrint, boolean canFilter, boolean canEditTable)
	{
		this(canExportCSV, canPrint, canFilter, canEditTable, true);
	}
	
	/**
	 * Creates a new NTable with the given properties:
	 * 
	 * canExportCSV - should the table be able to be saved by the user?
	 * canPrint - should the user be able to print the table?
	 * canFilter - should the user be able to filter the table?
	 * canEditTable - should the user be able to edit the table?
	 */
	public NTable(boolean canExportCSV, boolean canPrint, boolean canFilter, boolean canEditTable, boolean canSortTable)
	{
		this.canEditTable = canEditTable;
		
		// Set up the panel layout
		setLayout(new BorderLayout());
		
		// Set up the toolbar
		m_toolbar.setFloatable(false);
		if(canExportCSV)
		{
			m_csvExportButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					exportTable();
				}
			});
			m_toolbar.add(m_csvExportButton);
		}
		
		if(canPrint)
		{
			m_printButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					try{
						m_table.print();
					} catch (PrinterException e)
					{}
				}
			});
			m_toolbar.add(m_printButton);
		}
		
		if(canFilter)
		{
			// Add filter label
			m_toolbar.add(m_filterLabel);
			m_toolbar.add(m_filterBox);
			m_toolbar.add(m_filterButton);
			
			// Set up filterer
			m_filterButton.addActionListener(new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent e) {
		        applyFilter();
		      }
		    });
			
			// Filter on enter/return key
			m_filterBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e){
					applyFilter();
				}
			});
		}
		
		add(m_toolbar, BorderLayout.PAGE_START);
		
		
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		m_table.getTableHeader().setReorderingAllowed(false);
		scrollPane.setViewportView(m_table);
		m_table.setFillsViewportHeight(true);
		if(canSortTable)
		{
			m_table.setAutoCreateRowSorter(true);
		}else{
			m_table.setRowSorter(null);
			m_table.setAutoCreateRowSorter(false);
		}
		m_table.setOpaque(true);
		m_table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		m_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		m_table.setColumnSelectionAllowed(true);
		m_table.setCellSelectionEnabled(true);

		m_actionListeners = new LinkedList<ActionListener>();
		
		// Make the table listen for excel editors
		new ExcelAdapter(m_table);
		
		// Set up the table
		setTable(new String[0], new String[0][0]);
	}
	
	/**
	 * Sets the table up from a column header and data.
	 * @param colHeader
	 * @param data
	 */
	public void setTable(String[] colHeader, Object[][] data)
	{
		m_colHeader = colHeader.clone();

		// Delete old contents
		Enumeration<TableColumn> tableCols = m_table.getColumnModel().getColumns();
		while(tableCols.hasMoreElements())
			m_table.removeColumn(tableCols.nextElement());
		
		
		m_table.setModel(new DefaultTableModel(data,colHeader){
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int rowIndex, int mColIndex) {
		        return canEditTable;
		    }
		});
		m_table.getModel().addTableModelListener(this);

		// reset the sorter.
		sorter = new TableRowSorter<TableModel>(m_table.getModel());
		m_table.setRowSorter(sorter);
		
		// re-apply the filter.
		applyFilter();
	}
	
	/**
	 * Shows a save dialog for the table, and outputs it when done,
	 * or does nothing if canceled.
	 * 
	 * @return true if the table was saved, false if it was not.
	 */
	public boolean exportTable()
	{
		// Show the dialog
		String path = Dialogs.showSaveDialog(new String[]{"Comma Separated Value Files","csv"}, "", false, true, false);
		
		// Save the table if needed.
		if(path != null && ! path.equals(""))
			return saveTable(path);
		else
			return false;
	}
	
	/**
	 * Saves the contents in csv format of the table at the given 
	 * location if possible.
	 * 
	 * @return true if the table was able to be saved, false 
	 * otherwise.
	 */
	public boolean saveTable(String loc)
	{
		FileWriter f;
		
		try {
			f = new FileWriter(loc);

		
			LinkedList<String> rows = new LinkedList<String>();
			
			// Generate header row.
			String thisrow = "";
			for(String s : m_colHeader)
			{
				if(!thisrow.equals(""))
					thisrow += ",";
				
				thisrow += '"' + s + '"';
			}
			
			rows.add(thisrow + "\n");
			
		
			int numrows = m_table.getModel().getRowCount();
			int numcols = m_colHeader.length;
		
			for(int i = 0; i < numrows; i++)
			{
				thisrow = "";
				for(int j = 0; j < numcols; j++)
				{
					String cell = (String) m_table.getModel().getValueAt(i, j);
					
					// delim cells that are not numbers with quotes
					if(! cell.matches(NUMBER_REGEX))
					{
						cell = '"' + cell + '"';
					}
					
					
					if(!thisrow.equals(""))
						thisrow += ",";
					
					thisrow += cell;
				}
				rows.add(thisrow+"\n");
			}
		
		// For row in rows, export if possible.
		for(String r : rows)
			f.write(r.toCharArray());
		
		f.close();
		
		} catch(IOException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Adds the given component to the start of the toolbar.
	 * @param c
	 */
	public void addToolbarStart(JComponent c)
	{
		m_toolbar.add(c,0);
	}

	/**
	 * Adds the given component to the end of the toolbar
	 * @param c
	 */
	public void addToolbarEnd(JComponent c)
	{
		m_toolbar.add(c);
	}
	
	/**
	 * Adds a column with the given header at the given location with the given
	 * value in each space.
	 */
	public void addColumn(int location, String title, String value)
	{
		
		// Get headers
		LinkedList<String> headers = new LinkedList<String>();
		LinkedList<String[]> values = new LinkedList<String[]>();
		
		for(String s : m_colHeader)
			headers.add(s);
		
		// Make sure location isn't far off.
		if(location < 0)
			location = 0;
		if(location > headers.size())
			location = headers.size();
		
		headers.add(location, title);
		
		for(String[] data : getData())
		{
			LinkedList<String> currRow = new LinkedList<String>();
			for(String s : data)
				currRow.add(s);
			currRow.add(location, value);
			values.add(currRow.toArray(new String[currRow.size()]));
		}
		
		// Now set the column.
		setTable(headers.toArray(new String[headers.size()]), 
				values.toArray(new String[values.size()][]));
	}
	
	/**
	 * Shows a dialog box for the column to delete.
	 */
	public void deleteColumnDialog()
	{
		if(m_colHeader.length == 0)
		{
			Dialogs.showInformationDialog("Info", "There are no columns to delete");
			return;
		}
		
		Object chosen = Dialogs.showOptionDialog("Delete Column?", "Select the column to delete", m_colHeader);
		
		int i = 0;
		for(String col : m_colHeader)
		{
			if(col == chosen)
			{
				deleteColumn(i);
				return;
			}
			i++;
		}		
	}
	
	/**
	 * Deletes the column at the given location.
	 * 
	 * @param location
	 * @return The values of the deleted column.
	 */
	public String[] deleteColumn(int location)
	{
		// Get headers
		LinkedList<String> headers = new LinkedList<String>();
		LinkedList<String[]> values = new LinkedList<String[]>();
		LinkedList<String> deletedValues = new LinkedList<String>();
		
		for(String s : m_colHeader)
			headers.add(s);
		
		// Make sure location isn't far off.
		if(location < 0)
			location = 0;
		if(location > headers.size())
			location = headers.size();
		
		headers.remove(location);
		
		for(String[] data : getData())
		{
			LinkedList<String> currRow = new LinkedList<String>();
			for(String s : data)
				currRow.add(s);
			deletedValues.add(currRow.remove(location));
			values.add(currRow.toArray(new String[currRow.size()]));
		}
		
		// Now set the column.
		setTable(headers.toArray(new String[headers.size()]), 
				values.toArray(new String[values.size()][]));
		
		return deletedValues.toArray(new String[deletedValues.size()]);
	}
	
	/**
	 * Deletes the columns with the given name.
	 * 
	 * @param name
	 * @return The values of the deleted column(s)
	 */
	public String[][] deleteColumnsByName(String name)
	{
		LinkedList<String[]> lls = new LinkedList<String[]>();
		
		for(int i = m_colHeader.length - 1; i >= 0 ; i--)
			if(m_colHeader[i].equals(name))
				lls.add(deleteColumn(i));
		
		return lls.toArray(new String[lls.size()][]);
	}
	
	/**
	 * Returns the number of rows in this table.
	 * @return
	 */
	public int getNumRows()
	{
		return 	m_table.getModel().getRowCount();
	}
	
	/**
	 * Gets the given column's contents.
	 * 
	 * @param location - index of the column to fetch.
	 * @return An array of the contents, index 0 is row 0...index n is row n
	 */
	public String[] getColumnContents(int location)
	{
		String[] contents = new String[getNumRows()];

		for(int i = 0; i < getNumRows(); i++)
			contents[i] = (String) m_table.getModel().getValueAt(i, location);
		
		return contents;
	}
	
	/**
	 * Checks to see if a column's contents are purely numeric.
	 * 
	 * @param colNum
	 * @return
	 */
	public boolean isColumnNumeric(int colNum)
	{
		for(int i = 0; i < getNumRows(); i++)
		{
			if(! ((String) m_table.getModel().getValueAt(i, colNum)).matches(NUMBER_REGEX))
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Gets the contents of the columns by name.
	 * 
	 * @param name - the name of the columns to fetch.
	 * @return An collection containing the values of the columns to fetch
	 * (left to right)
	 */
	public Collection<String[]> getContentsByName(String name)
	{
		LinkedList<String[]> lls = new LinkedList<String[]>();
		
		for(int i = 0; i < m_colHeader.length; i++)
			if(m_colHeader[i].contains(name))
				lls.add(getColumnContents(i));	
		
		return lls;
	}
	
	/**
	 * Returns a an array of row contents for the given table, each
	 * row contains individual cells.
	 */
	public String[][] getData()
	{
		int numrows = m_table.getModel().getRowCount();
		int numcols = m_colHeader.length;
		
		String[][] data = new String[numrows][numcols];
		for(int i = 0; i < numrows; i++)
			for(int j = 0; j < numcols; j++)
				data[i][j] = (String) m_table.getModel().getValueAt(i, j);
		
		return data;
	}
	
	/**
	 * Returns an array for the column names.
	 * 
	 * @return
	 */
	public String[] getColumnNames()
	{
		return m_colHeader.clone();
	}
	
	public void deleteSelectedRows()
	{
		int[] selectedRows = m_table.getSelectedRows();

		// Delete rows biggest to smallest so as not to mess up the indexes of all
		// of the larger rows.
		for(int k = selectedRows.length -1; k >= 0; k--)
		{
			// If there is a filter, actual indexes won't line up with 
			// the returned ones, this fixes that problem.
			int j = m_table.convertRowIndexToModel(selectedRows[k]); 
			
			DefaultTableModel dtm = (DefaultTableModel) m_table.getModel();
			dtm.removeRow(j);
		}
	}
	
	/**
	 * Force applies the filter given, and updates the filter
	 * text box to reflect it.
	 * 
	 * @param s
	 */
	public void applyFilter(String filter)
	{
        if (filter == null || filter.length() == 0) 
        {
    		m_filterBox.setText("");
    		sorter.setRowFilter(null);
        } else {
    		m_filterBox.setText(filter);
    		sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filter));
        }
	}
	
	/**
	 * Force applies the filter the user has entered, if any.
	 */
	public void applyFilter()
	{
		applyFilter(m_filterBox.getText());
	}
	
	/**
	 * Clears any user entered filter, showing all rows.
	 */
	public void clearFilter()
	{
		applyFilter("");
	}

	
    @Override
	public void tableChanged(TableModelEvent e) {
    	ActionEvent ae = new ActionEvent(this, TABLE_CHANGED, "TableChanged");
    	for(ActionListener l : m_actionListeners)
    		l.actionPerformed(ae);
    }
    
    private transient final LinkedList<ActionListener> m_actionListeners;
    /**
     * Action Listeners can be added for the following events:
     * Table Changed - when the table is modified by the user, denoted
     * by the id TABLE_CHANGED
     * 
     * @param l
     */
    public void addActionListener(ActionListener l)
    {
    	m_actionListeners.add(l);
    }
    
    /**
     * Returns true if the table has no columns.
     * 
     * @return
     */
    public boolean isTableEmpty()
    {
    	return m_colHeader.length == 0;
    }
}
