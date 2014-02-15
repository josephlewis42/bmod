package bmod.gui;

import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import bmod.database.objects.AnyRecord;
import bmod.database.objects.Record;
import bmod.gui.widgets.VerticalPanel;

/**
 * A simple filter for a DatabaseEditor.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class DatabaseEditorFilter extends VerticalPanel
{

	private static final long serialVersionUID = 1L;
	private final JComboBox<Record<?>> m_databaseEditor = new JComboBox<>();
	private final Record<?> m_instance;
	
	public DatabaseEditorFilter(Record<?> baseInstance)
	{
		m_instance = baseInstance;
		
		add(new JLabel(m_instance.getTableName()));
		add(m_databaseEditor);
			
		
		populateModel(new LinkedList<Record<?>>());
	}
	
	/**
	 * Populates the model based upon a list of filtered records.
	 */
	public void populateModel(Collection<Record<?>> filters)
	{
		// save state
		long currentRecordId = (getValue() == null)? Long.MIN_VALUE: getValue().getPrimaryKey();

		
		LinkedList<Record<?>> items = new LinkedList<>(m_instance.readFiltered(filters));
		items.add(new AnyRecord());
		
		Record<?>[] recordArr = items.toArray(new Record<?>[0]);
		
		Arrays.sort(recordArr);
		
		m_databaseEditor.setModel(new DefaultComboBoxModel<>(recordArr));
		
		// restore state
		if(currentRecordId != Long.MIN_VALUE)
		{
			for(int i = 0; i < recordArr.length; i++)
			{
				if(recordArr[i].getPrimaryKey() == currentRecordId)
				{
					m_databaseEditor.setSelectedIndex(i);
					break;
				}
			}
		}
	}
	
	/**
	 * Gets the currently selected item from the editor.
	 * 
	 * @return
	 */
	public Record<?> getValue()
	{
		return m_databaseEditor.getItemAt(m_databaseEditor.getSelectedIndex());
	}
	
	public void addFilterUpdateListener(ActionListener l)
	{
		m_databaseEditor.addActionListener(l);
	}
}
