package bmod.gui.widgets;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.JComboBox;

import bmod.database.objects.BuildingActivity;

public class ActivityTypeComboBox extends JComboBox<String> implements FocusListener
{
	private static final long serialVersionUID = -6455968205679589965L;
	
	public ActivityTypeComboBox()
	{
		setEditable(true);
		
		for(Component c : getComponents())
			c.addFocusListener(this);
				
		reloadItems();
		
	}
	
	public void setText(String t)
	{
		for(int i = 0; i < getItemCount(); i++)
			if(getItemAt(i).equals(t)) {
				setSelectedItem(getItemAt(i));
				return;
			}
		
		addItem(t);
		setSelectedItem(t);
	}
	
	public String getText()
	{
		if(getSelectedItem() != null)
			return getSelectedItem().toString();
		return "";	
	}
	
	/**
	 * Reloads the DeviceTypeComboBox
	 */
	public void reloadItems()
	{
		String currItem = getText(); // Save the current item.

		removeAllItems();
		
		LinkedList<String> lls = new LinkedList<String>();
		
		for(String t: BuildingActivity.readAllTypes())
			lls.add(t);
		
				
		Collections.sort(lls);
		for(String t : lls)
			addItem(t);
		
		setText(currItem); // Set the current item back.
	}

	@Override
	public void focusGained(FocusEvent e) {
		reloadItems();
	}

	@Override
	public void focusLost(FocusEvent e) {
		
	}
}
