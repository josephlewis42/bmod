package bmod.gui.widgets;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collection;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import bmod.database.Database;
import bmod.database.objects.Record;

/**
 * A combo box that holds CSVRecords and updates when the database version has
 * been upped and the user presses the button.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 * @param <T> The type of CSVRecord this combo box holds.
 */
public class CSVRecordJComboBox<T extends Record<T>> extends JComboBox<T>
{
	private static final long serialVersionUID = 2390819288176678114L;
	protected int m_lastVersion = Integer.MIN_VALUE;
	protected final T m_instance;
	
	/**
	 * Creates a new CSVRecordJComboBox.
	 * @param instance - The thing used to pass to the database to get new items.
	 */
	public CSVRecordJComboBox(T instance)
	{
		m_instance = instance;
		updateIfNeeded();
		
		addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent arg0) {
				updateIfNeeded();
			}
			@Override
			public void focusLost(FocusEvent arg0) {
				
			}
		});
	}
	
	public CSVRecordJComboBox(T record, long recordPkey)
	{
		this(record);
		setSelectedItemByPkey(recordPkey);
	}

	/**
	 * Updates the combo box if needed.
	 * @return True if the box was updated, False otherwise
	 */
	public boolean updateIfNeeded()
	{
		if(getItemCount() != 0 && m_lastVersion == Database.getDqm().getCommitNumber())
			return false;
		
		m_lastVersion = Database.getDqm().getCommitNumber();
		
		forceUpdate();
		return true;
	}
	
	/**
	 * Forces the ComboBox to update with new items.
	 * 
	 * @return True if the box was updated, False otherwise
	 */
	protected void forceUpdate()
	{
		setItems(m_instance.readAll());		
	}
	
	/**
	 * Sets the selected item by the toString method of the item.
	 */
	public void setSelectedItem(String toStringRepresentation)
	{
		for(int i = 0; i < getItemCount(); i++)
			if(getItemAt(i).toString().equals(toStringRepresentation))
				setSelectedItem(getItemAt(i));
	}
	
	/**
	 * Sets the selected item by the primary key given.
	 * @param pkey - The primary key of the item to set.
	 */
	public void setSelectedItemByPkey(long pkey)
	{
		for(int i = 0; i < getItemCount(); i++)
			if(getItemAt(i).getPrimaryKey() == pkey)
			{
				setSelectedItem(getItemAt(i));
				return;
			}
	}
	
	public void setItems(Collection<T> items) {
        super.setModel(new DefaultComboBoxModel<T>(new Vector<T>(items)));
    }

	@Override
    public T getSelectedItem() {
    	return super.getItemAt(super.getSelectedIndex());
    }
}
