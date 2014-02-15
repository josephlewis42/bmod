package bmod.gui.widgets;


import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import bmod.ExtensionPoints;
import bmod.database.DataFeed;


public class DataFeedChooser extends JComboBox<DataFeed>
{
	private static final long serialVersionUID = 1L;

	public DataFeedChooser()
	{
		addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent arg0) {
				update();
			}
			@Override
			public void focusLost(FocusEvent arg0) {
				
			}
		});
	}
	
	protected void update()
	{
		setModel(
				new DefaultComboBoxModel<DataFeed>(
						new Vector<DataFeed>(ExtensionPoints.getAllDataFeeds())));
	}
	
	@Override
	public DataFeed getSelectedItem()
	{
		return (DataFeed) super.getSelectedItem();
	}
}
