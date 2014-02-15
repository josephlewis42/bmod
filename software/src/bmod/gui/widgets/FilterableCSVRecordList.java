package bmod.gui.widgets;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bmod.database.Database;
import bmod.database.objects.Record;

/**
 * A Filterable list for a set of CSVRecords; automatically fetches the 
 * contents of each and appends them to the list.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class FilterableCSVRecordList extends JPanel
{
	private static final long serialVersionUID = 1L;
	private final Collection<Record<?>> m_masterRecords;
	private final JTextField m_filterEntry = new JTextField();
	private final JList<Record<?>> m_internalList = new JList<>();
	private int m_lastCheckpoint = -1;
	private final LinkedList<Record<?>> m_cachedRecords = new LinkedList<>();
	private Collection<Record<?>> m_currentFilters = new LinkedList<>();
	
	public FilterableCSVRecordList(Collection<Record<?>> masters)
	{
		setLayout(new BorderLayout());
		add(m_filterEntry, BorderLayout.PAGE_START);
		add(new JScrollPane(m_internalList), BorderLayout.CENTER);
		
		m_masterRecords = masters;
		
		doFilter();
		
		m_filterEntry.addKeyListener(new KeyAdapter(){
			@Override
			public void keyReleased(KeyEvent arg0)
			{
				doFilter();				
			}
		});
		
		// Alert anyone listening when we change records.
		m_internalList.addListSelectionListener(m_selectionListener);
	}
	
	private final ListSelectionListener m_selectionListener = new ListSelectionListener()
	{
		@Override
		public void valueChanged(ListSelectionEvent arg0)
		{
			List<Record<?>> selectedValues = m_internalList.getSelectedValuesList();
			
			for(RecordChangeListener rcl : m_recordChangeListeners)
			{
				switch(selectedValues.size())
				{
					case 0:
						rcl.allRecordsDeselected();
						break;
					case 1:
						rcl.recordChange(selectedValues.get(0));
						break;
					default:
						rcl.recordChange(selectedValues);
				}
			}
		}
	};
	

	
	private Collection<Record<?>> getAllRecords()
	{		
		if(m_lastCheckpoint == Database.getDqm().getCommitNumber())
		{
			return m_cachedRecords;
		}
		
		m_lastCheckpoint = Database.getDqm().getCommitNumber();
		m_cachedRecords.clear();
		
		for(Record<?> master : m_masterRecords)
		{
			m_cachedRecords.addAll(master.readFiltered(m_currentFilters));
		}
		
		Collections.sort(m_cachedRecords);
		
		return m_cachedRecords;
	}
	
	
	/**
	 * Filters every record available.
	 */
	public void doFilter()
	{
		List<Record<?>> currentSelected = m_internalList.getSelectedValuesList();
		List<Integer> ints = new LinkedList<Integer>();
		String text = m_filterEntry.getText().toUpperCase();
		
		DefaultListModel<Record<?>> dlm = new DefaultListModel<>();
		
		int i = 0;
		for(Record<?> record : getAllRecords())
		{
			if(record.toString().toUpperCase().contains(text))
			{
				dlm.addElement(record);
			}
			
			for(Record<?> selected : currentSelected)
			{
				if(record.equals(selected))
				{
					ints.add(i);
				}
			}
			
			i++;
		}
		
		m_internalList.setModel(dlm);
		m_internalList.setSelectedIndices(bmod.util.BmodCollection.collectionToIntList(ints));
	}

	public static abstract class RecordChangeListener{
		/**
		 * Called on single-select.
		 * @param r
		 */
		public abstract void recordChange(Record<?> r);
		
		/**
		 * Called on multi-select.
		 * @param recs
		 */
		public abstract void recordChange(List<Record<?>> recs);
		
		public abstract void allRecordsDeselected();
	}

	private final LinkedList<RecordChangeListener> m_recordChangeListeners = new LinkedList<>();
	public void addRecordChangeListener(RecordChangeListener listener)
	{
		m_recordChangeListeners.add(listener);
	}
	
	public void applyFilter(Collection<Record<?>> filters)
	{
		// check if we can be filtered before we actually do it to save on
		// computations
		for(Record<?> master : m_masterRecords)
		{
			if(master.canBeFilteredBy(filters) || m_currentFilters.size() != filters.size())
			{
				m_currentFilters = filters;
				m_lastCheckpoint = -1;
				
				doFilter();
				
				return;
			}
		}
	}
	
	/**
	 * Returns the user selected values.
	 * 
	 * @return
	 */
	public List<Record<?>> getSelectedValuesList()
	{
		return m_internalList.getSelectedValuesList();
	}
}
