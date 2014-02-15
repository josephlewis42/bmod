package bmod.gui.widgets;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import bmod.IconLoader;
import bmod.database.objects.Record;
import bmod.database.objects.MultiMap;


/**
 * Provides a simple GUI interface to maps between two objects.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 * @param <E>
 * @param <T>
 */
@SuppressWarnings("serial")
public class MultiMapWidget<E extends Record<E>, T extends Record<T>> extends JPanel implements ActionListener
{
	private final MultiMap<E, T> m_multiMap;
	private E m_selectedItem;
	private final JList<T> potential = new JList<>();
	private final JList<T> selected = new JList<>();
	private final JScrollPane potentialScroll = new JScrollPane(potential);
	private final JScrollPane selectedScroll = new JScrollPane(selected);
	private final JButton selectAll = new JButton(">>");
	private final JButton selectSome = new JButton(">");
	private final JButton deselectAll = new JButton("<<");
	private final JButton deselectSome = new JButton("<");
	private final JPanel potentialPanel = new JPanel();
	private final JPanel selectedPanel = new JPanel();
	private final JSplitPane split = new JSplitPane();
	private final JToolBar potentialToolBar = new JToolBar();
	private final JToolBar selectedToolBar = new JToolBar();


	/**
	 * Creates a new MultiMapWidget.
	 * 
	 * @param selectedItem
	 * @param map
	 */
	public MultiMapWidget(E selectedItem, MultiMap<E, T> map)
	{
		if(null == selectedItem || null == map)
			throw new NullPointerException("Map or SelectedItem = null! Map:" + map);
		
		m_multiMap = map;
		m_selectedItem = selectedItem;
		
		
		// Setup the panel.
		setLayout(new BorderLayout());
		add(split);
		split.setLeftComponent(potentialPanel);
		potentialPanel.setLayout(new BorderLayout());
		potentialPanel.add(potentialScroll, BorderLayout.CENTER);
		
		potentialToolBar.setFloatable(false);
		potentialToolBar.add(selectAll);
		potentialToolBar.add(selectSome);
		potentialPanel.add(potentialToolBar, BorderLayout.PAGE_START);
		
		split.setRightComponent(selectedPanel);
		selectedPanel.setLayout(new BorderLayout());
		selectedPanel.add(selectedScroll, BorderLayout.CENTER);
		selectedToolBar.setFloatable(false);
		selectedToolBar.add(deselectSome);
		selectedToolBar.add(deselectAll);
		selectedPanel.add(selectedToolBar, BorderLayout.PAGE_START);
		
		// Link buttons
		selectAll.addActionListener(this);
		selectSome.addActionListener(this);
		deselectAll.addActionListener(this);
		deselectSome.addActionListener(this);
		
		// Refresh all items.
		refreshPanel();
	}
	
	/**
	 * Refreshes the panel for the chosen items.
	 */
	public void refreshPanel()
	{
		// Get all potential items.
		T[] potentialItems = m_multiMap.possibleConnectionsFrom(m_selectedItem.getPrimaryKey());
		T[] selectedItems = m_multiMap.connectionsFrom(m_selectedItem.getPrimaryKey());
		
		// Remove selected items from potential items
		selected.setModel(new DefaultComboBoxModel<>(selectedItems));
		
		LinkedList<T> unselectedPotential = new LinkedList<T>();
		for(T tmp : potentialItems)
		{
			boolean found = false;
			
			for(T chal : selectedItems)
				if(chal.getPrimaryKey() == tmp.getPrimaryKey())
					found = true;
			
			if(! found)
				unselectedPotential.add(tmp);
		}
		
		potential.setModel(new DefaultComboBoxModel<T>(new Vector<T>(unselectedPotential)));

	}
	
	/**
	 * Selects the chosen items.
	 */
	public void select()
	{
		for(T tmp : potential.getSelectedValuesList())
			m_multiMap.addLink(m_selectedItem.getPrimaryKey(), tmp.getPrimaryKey());
	}
	
	/**
	 * De-selects the chosen items
	 */
	public void deselect()
	{
		for(T tmp : selected.getSelectedValuesList())
			m_multiMap.deleteFrom(m_selectedItem.getPrimaryKey(), tmp.getPrimaryKey());
	}
	
	/**
	 * Selects all possible items.
	 */
	public void selectAll()
	{
		for(T tmp : m_multiMap.possibleConnectionsFrom(m_selectedItem.getPrimaryKey()))
			m_multiMap.addLink(m_selectedItem.getPrimaryKey(), tmp.getPrimaryKey());
	}
	
	/**
	 * De-selects all possible items.
	 */
	public void deselectAll()
	{
		for(T tmp : m_multiMap.connectionsFrom(m_selectedItem.getPrimaryKey()))
			m_multiMap.deleteFrom(m_selectedItem.getPrimaryKey(), tmp.getPrimaryKey());
	}

	@Override
	public void actionPerformed(ActionEvent evt)
	{
		if(evt.getSource() == selectAll)
			selectAll();
		
		if(evt.getSource() == selectSome)
			select();
		
		if(evt.getSource() == deselectAll)
			deselectAll();
		
		if(evt.getSource() == deselectSome)
			deselect();
		
		refreshPanel();
	}
	
	/**
	 * Returns the main toolbar for this widget so you can add items.
	 * 
	 * @return
	 */
	public JToolBar getToolBar()
	{
		return potentialToolBar;
	}
	
	/**
	 * Adds a "create" button to the widget and returns it.
	 * @return
	 */
	public JButton appendAddButton()
	{
		JButton createButton = new JButton("", IconLoader.ADD_ICON);
		createButton.setToolTipText("Make a new one");
		getToolBar().add(createButton, 0);
		
		return createButton;
	}

	/**
	 * Sets the item that is the source now.
	 * @param l
	 */
	public void setSource(E l)
	{
		m_selectedItem = l;
		refreshPanel();
	}
}
