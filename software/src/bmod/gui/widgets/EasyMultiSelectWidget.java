package bmod.gui.widgets;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

/**
 * A simple multi-select widget that will work on any object type.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 * @param <T>
 */
public class EasyMultiSelectWidget<T> extends JPanel implements ActionListener
{
	private static final long serialVersionUID = -4680371789684224811L;
	private final JList<T> potential;
	private final JList<T> selected = new JList<>();
	private final JButton select = new JButton("Select >");
	private final JButton deselect = new JButton(" < Deselect");
	private final JButton selectAll = new JButton(">>");
	private final JButton deselectAll = new JButton("<<");
	private final JToolBar selectToolBar = new JToolBar();
	private final JToolBar deselectToolBar = new JToolBar();
	
	public EasyMultiSelectWidget(T[] allPossible)
	{
		potential = new JList<>(allPossible);
		setLayout(new BorderLayout());
		
		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
				new ArticleLayoutPanel(selectToolBar, new JScrollPane(potential), null),
				new ArticleLayoutPanel(deselectToolBar, new JScrollPane(selected), null)),
			BorderLayout.CENTER);
		
		selectToolBar.add(selectAll);
		selectToolBar.add(select);
		
		deselectToolBar.add(deselect);
		deselectToolBar.add(deselectAll);
		
		selectAll.addActionListener(this);
		deselectAll.addActionListener(this);
		deselect.addActionListener(this);
		select.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent evt)
	{
		LinkedList<T> selected_items = getAllValues(selected);
		LinkedList<T> unselected_items = getAllValues(potential);
		
		if(evt.getSource() == selectAll)
		{
			selected_items.addAll(unselected_items);
			unselected_items.clear();
		}
		
		if(evt.getSource() == deselectAll)
		{
			unselected_items.addAll(selected_items);
			selected_items.clear();
		}
		
		if(evt.getSource() == select)
		{
			selected_items.addAll(potential.getSelectedValuesList());
			unselected_items.removeAll(potential.getSelectedValuesList());
		}
		
		if(evt.getSource() == deselect)
		{
			selected_items.removeAll(selected.getSelectedValuesList());
			unselected_items.addAll(selected.getSelectedValuesList());
		}
				
		potential.setModel(new DefaultComboBoxModel<>(new Vector<T>(unselected_items)));
		selected.setModel(new DefaultComboBoxModel<>(new Vector<T>(selected_items)));
	}
	
	public List<T> getSelectedValuesList()
	{
		return getAllValues(selected);
	}
	
	/**
	 * Extracts the values from a JList in to a LinkedList.
	 * 
	 * @param list - the JList with the values to extract
	 */
	private LinkedList<T> getAllValues(JList<T> list)
	{
		LinkedList<T> contents = new LinkedList<>();
		
		for(int i = 0; i < list.getModel().getSize(); i++)
		{
			contents.add(list.getModel().getElementAt(i));
		}
		
		return contents;
	}
}
