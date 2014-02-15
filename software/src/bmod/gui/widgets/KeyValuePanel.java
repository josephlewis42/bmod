package bmod.gui.widgets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * A generic panel for general style layouts.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class KeyValuePanel extends JPanel
{

	private static final long serialVersionUID = 1L;
	int curr_y = 0;

	
	public KeyValuePanel()
	{
		// Set up the a grid bag, because those are pretty.
		GridBagLayout gbl = new GridBagLayout(); 
		// Full width for items, little for text.
		gbl.columnWeights = new double[]{0.0, 1.0};
		setLayout(gbl);					
	}
	
	/**
	 * Adds the given component to the form with the given title. All components
	 * will automatically get a weight of 1 when added through this method.
	 * 
	 * @param key - the title for the component.
	 * @param widget - the component to add.
	 */
	public void add(String key, JComponent widget)
	{
		add(key, widget, 1.0);
	}

	/**
	 * Adds the given component to the form with the given title, and the given
	 * y weight; the higher the y weight, the more of the available space the
	 * component takes up.
	 * 
	 * @param key - the title for the component.
	 * @param widget - the component to add
	 * @param weightY - the amount of vertical space this component should get
	 * relative to all the other y weights.
	 */
	public void add(String key, JComponent widget, double weightY)
	{
		if(widget == null)
		{
			return;
		}
		
		if(key == null)
		{
			key = "";
		}
		
		GridBagConstraints gbc_l = new GridBagConstraints();
		gbc_l.insets = new Insets(5, 5, 0, 0);
		gbc_l.gridx = 0;
		gbc_l.gridy = curr_y;
		gbc_l.anchor = GridBagConstraints.BELOW_BASELINE_TRAILING;

		
		add(new JLabel(key), gbc_l);
		
		
		
		GridBagConstraints gbc_w = new GridBagConstraints();
		gbc_w.fill = GridBagConstraints.BOTH;
		gbc_w.insets = new Insets(5, 5, 0, 5);
		gbc_w.gridx = 1;
		gbc_w.weighty = weightY;
		gbc_w.gridy = curr_y;
		gbc_w.anchor = GridBagConstraints.NORTH;
		
		add(widget, gbc_w);
		
		curr_y++;
	}
}
