package bmod.gui;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bmod.database.objects.Record;
import bmod.gui.FilterPanel.FilterUpdateListener;
import bmod.plugin.loader.CSVRecordLoader;
import bmod.util.Bucket;

/**
 * A new style Database editor that filters all items at once, and doesn't have
 * issues with recursive updates.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class NewDatabaseEditor extends JPanel implements ChangeListener, FilterUpdateListener
{
	private static final long serialVersionUID = 1L;
	
	private final JTabbedPane m_tableEditors = new JTabbedPane();
	private Collection<Record<?>> m_currentFilters = new LinkedList<>(); // any currently applied filters.
	
	public NewDatabaseEditor()
	{
		setLayout(new BorderLayout());
		
		Set<Record<?>> m_filters = new TreeSet<>();
		Bucket<String, Record<?>> m_recordGroups = new Bucket<>();
		for(Record<?> tmp : new CSVRecordLoader().getPlugins())
		{
			String editGroup = tmp.getUserEditableClass();
			if( editGroup == null)
				continue;
			
			m_filters.addAll(tmp.getFilters());
			m_recordGroups.add(editGroup, tmp);
		}
		
		for(Entry<String, Set<Record<?>>> e : m_recordGroups.entrySet())
		{
			NewDatabaseEditorPanel tmp = new NewDatabaseEditorPanel(e.getValue());
			m_tableEditors.add(	Record.canonicalNameToHumanReadable(e.getKey()), tmp);
		}
		
		FilterPanel fp = new FilterPanel(new LinkedList<>(m_filters));
		fp.addFilterUpdateListener(this);
		
		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fp.getContainer(), m_tableEditors), BorderLayout.CENTER);
		
		m_tableEditors.addChangeListener(this);
	}
	
	/**
	 * Called when the filters are updated; updates the current tab with the 
	 * selected filters.
	 */
	@Override
	public void filterUpdated(Collection<Record<?>> filter)
	{
		m_currentFilters = filter;
		
		((NewDatabaseEditorPanel) m_tableEditors.getSelectedComponent()).setFilter(filter);
	}

	
	/**
	 * Updates the current tab with the current filters JIT.
	 */
	@Override
	public void stateChanged(ChangeEvent e)
	{
		((NewDatabaseEditorPanel) m_tableEditors.getSelectedComponent()).setFilter(m_currentFilters);
	}
}
