package bmod.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import bmod.database.Database;
import bmod.database.objects.Record;
import bmod.gui.widgets.VerticalPanel;

/**
 * A panel for a set of filters. It will dynamically update the filters, and
 * can report the selected filters.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class FilterPanel implements ActionListener
{
	private final JPanel m_filterPanel = new VerticalPanel();
	private final LinkedList<DatabaseEditorFilter> m_filters = new LinkedList<>();
	private final LinkedList<FilterUpdateListener> listeners = new LinkedList<>();

	public FilterPanel(List<Record<?>> filtersToDisplay)
	{
		Record.sortByReferenceDependencies(filtersToDisplay);
		
		for(Record<?> filter : filtersToDisplay)
		{
			DatabaseEditorFilter tmp = new DatabaseEditorFilter(filter);
			m_filterPanel.add(tmp);
			m_filters.add(tmp);
			
			tmp.addFilterUpdateListener(this);
		}
	}
	
	/**
	 * Returns a list of all currently selected states in this panel.
	 * 
	 * @return
	 */
	public Collection<Record<?>> getState()
	{
		Collection<Record<?>> selected = new LinkedList<>();
		
		for(DatabaseEditorFilter tmp : m_filters)
		{
			Record<?> filter = tmp.getValue();
			
			if(filter != null)
			{
				selected.add(filter);
			}
		}
		
		return selected;
	}
	
	/**
	 * Updates the state down the line as we get more and more filters.
	 */
	public void updateState()
	{
		Collection<Record<?>> selected = new LinkedList<>();
		
		for(DatabaseEditorFilter tmp : m_filters)
		{
			tmp.populateModel(selected); // refresh with current.

			Record<?> filter = tmp.getValue();
			
			if(filter != null && filter.getPrimaryKey() != Database.TEMPLATE_PRIMARY_KEY)
			{
				selected.add(filter);
			}
		}
		
		for(FilterUpdateListener listener : listeners)
		{
			listener.filterUpdated(selected);
		}
	}
	
	
	/**
	 * Fetches the container that contains the filters referenced by this 
	 * object.
	 * 
	 * @return
	 */
	public JPanel getContainer()
	{
		JPanel tmp = new JPanel(new BorderLayout());
		tmp.add(m_filterPanel, BorderLayout.NORTH);
		return tmp;
	}
	
	
	public static interface FilterUpdateListener{
		public void filterUpdated(Collection<Record<?>> filter);
	}
	
	
	/**
	 * Adds a listener that will be called when the filter changes.
	 * 
	 * @param listener - the listener to add
	 */
	public void addFilterUpdateListener(FilterUpdateListener listener)
	{
		listeners.add(listener);
	}
	
	/**
	 * Removes an existing listener from the list of listeners to be called when
	 * a filter changes.
	 * 
	 * @param listener - the lisetenr to remove
	 */
	public void removeFilterUpdateListener(FilterUpdateListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Called when one of the child filters is updated.
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		updateState();		
	}
}
