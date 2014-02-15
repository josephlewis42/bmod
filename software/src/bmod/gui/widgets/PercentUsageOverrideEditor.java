package bmod.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map.Entry;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;

import bmod.SerializableHashMap;

public class PercentUsageOverrideEditor extends JPanel
{

	private static final long serialVersionUID = 1L;
	private DefaultTableModel m_model;
	private JTable m_table;
	private final JToolBar m_toolbar = new JToolBar();
	private final Logger m_logger = Logger.getLogger(this.getClass());

	/**
	 * Create the panel.
	 */
	public PercentUsageOverrideEditor()
	{		
		setLayout(new BorderLayout());
		
		// Build an easily editable model
		m_model = new DefaultTableModel() {  
			private static final long serialVersionUID = 1L;
			Class<?>[] types = new Class [] {  
	            //COL. TYPES ARE HERE!!!  
	            java.lang.String.class, java.lang.Double.class  
	        };  
	          
	        @Override  
	        public Class<?> getColumnClass(int columnIndex) {  
	            return types [columnIndex];  
	        }
	    };  
	    
		m_table = new JTable(m_model);
		
		// create the editor
		JButton deleteRowsButton = new JButton("Delete Selected Rows");
		deleteRowsButton.addActionListener(new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent e) {
		        deleteSelectedRows();
		      }
		});
		m_toolbar.add(deleteRowsButton);
		
		JButton newRowButton = new JButton("Add Row");
		newRowButton.addActionListener(new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent e) {
		        addRow();
		      }
		});
		m_toolbar.add(newRowButton);
		
		add(m_toolbar, BorderLayout.PAGE_END);

		// Do the main table
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		m_table.getTableHeader().setReorderingAllowed(false);
		scrollPane.setViewportView(m_table);
		m_table.setFillsViewportHeight(true);
		m_table.setAutoCreateRowSorter(true);
		m_table.setOpaque(true);
				
		// Create a couple of columns
		m_model.addColumn("Activity Type");
		m_model.addColumn("Percent Usage");
		
		m_table.getColumnModel().getColumn(0).setCellEditor(new ActivityTypeEditor());
		//m_table.getColumnModel().getColumn(1).setCellEditor(new PercentUsageEditor());
	}
	
	public SerializableHashMap<String,Double> getValue()
	{
		SerializableHashMap<String, Double> values = new SerializableHashMap<String,Double>();	
		
		for(int i = 0; i < m_model.getRowCount(); i++)
			values.put((String)m_model.getValueAt(i, 0), (Double) m_model.getValueAt(i, 1));
		
		return values;
	}
	
	private void deleteSelectedRows()
	{
		int[] selectedRows = m_table.getSelectedRows();

		// Delete rows biggest to smallest so as not to mess up the indexes of all
		// of the larger rows.
		for(int k = selectedRows.length -1; k >= 0; k--)
		{
			// If there is a filter, actual indexes won't line up with 
			// the returned ones, this fixes that problem.
			int j = m_table.convertRowIndexToModel(selectedRows[k]); 
			
			m_model.removeRow(j);
		}
	}
	
	private void addRow()
	{
		m_model.addRow(new Object[]{"",0});
	}
	
	public void setMap(SerializableHashMap<String, Double> map)
	{
		while (m_model.getRowCount()>0){
			m_model.removeRow(0);
		}
		
		for(Entry<String, Double> rm : map.entrySet())
			m_model.addRow(new Object[]{rm.getKey(), rm.getValue()});
	}
	
	public String getText()
	{
		try
		{
			return getValue().serialize();
		} catch (IOException e)
		{
			m_logger.error(e.getMessage(), e);
			return "";
		}
	}
	
	private class ActivityTypeEditor extends AbstractCellEditor implements TableCellEditor {
		private static final long serialVersionUID = 1L;
		// This is the component that will handle the editing of the cell value
		ActivityTypeComboBox component = new ActivityTypeComboBox();

	    // This method is called when a cell value is edited by the user.
	    @Override
		public Component getTableCellEditorComponent(JTable table, Object value,
	            boolean isSelected, int rowIndex, int vColIndex) {
	        // 'value' is value contained in the cell located at (rowIndex, vColIndex)

	        if (isSelected) {
	            // cell (and perhaps other cells) are selected
	        }

	        // Configure the component with the specified value
	        component.setText((String)value);

	        // Return the configured component
	        return component;
	    }

	    // This method is called when editing is completed.
	    // It must return the new value to be stored in the cell.
	    @Override
		public Object getCellEditorValue() {
	        return component.getText();
	    }
	}
}
