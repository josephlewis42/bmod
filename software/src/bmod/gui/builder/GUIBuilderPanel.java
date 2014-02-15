package bmod.gui.builder;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Builds a GUI based upon SQL types. Act very much like Django's admin,
 * although, could be extended to build web based GUIs, etc. Beans anyone?
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class GUIBuilderPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private final Object[] initial;
	private GUIBuilderWidget[] widgetset;
	
	/**
	 * Builds a JPanel with a GUI editor for no object, defaults to all empty
	 * strings. This isn't very useful, unless you just want to do layout.
	 */
	public GUIBuilderPanel(GUIBuilderWidget...widgets)
	{
		this(new String[widgets.length], widgets);
	}
	
	/**
	 * Builds a JPanel with a GUI editor for the given object.
	 * 
	 * @param buildable
	 * @return
	 */
	public GUIBuilderPanel(Object[] initial, GUIBuilderWidget...widgets)
	{
		this(initial, new GUIBuilderWidget[0], widgets);
	}
	
	public void setupWidgets()
	{			
		// Set up the a grid bag, because those are pretty.
		GridBagLayout gbl = new GridBagLayout(); 
		// Full width for items, little for text.
		gbl.columnWeights = new double[]{0.0, 1.0};
		setLayout(gbl);
		
		int curr_y = 0;
		
		
		for(GUIBuilderWidget widget : widgetset)
			if(widget != null)
			{
				GridBagConstraints gbc_l = new GridBagConstraints();
				gbc_l.insets = new Insets(5, 5, 0, 0);
				gbc_l.gridx = 0;
				gbc_l.gridy = curr_y;
				//gbc_l.anchor = GridBagConstraints.NORTH;
				gbc_l.anchor = GridBagConstraints.BELOW_BASELINE_TRAILING;

				if(widget.getFullWidth())
				{
					curr_y++;
					gbc_l.gridwidth = 2;
					gbc_l.anchor = GridBagConstraints.BELOW_BASELINE_LEADING;
				}
				
				
				add(new JLabel(widget.getTitle()), gbc_l);
				
				GridBagConstraints gbc_w = new GridBagConstraints();
				gbc_w.fill = GridBagConstraints.BOTH;
				gbc_w.insets = new Insets(5, 5, 0, 5);
				if(widget.getFullWidth())
				{
					gbc_w.gridwidth = 2;
					gbc_w.gridx = 0;
				} else {
					gbc_w.gridx = 1;
				}
				if(widget.getMaxHeight())
					gbc_w.weighty = 1.0;
				
				gbc_w.gridy = curr_y;
				gbc_w.anchor = GridBagConstraints.NORTH;
								
				add(widget.getComponent(), gbc_w);

				
				curr_y++;
			}
	}
	
	/**
	 * Builds a JPanel with a GUI editor for the given object, but adds the 
	 * widgets to the panel in a different order than they'll be output.
	 * 
	 * e.g. you expect the getResult to return a [String, Integer, Long], but
	 * want your user to see the long, then the int then the string, you could 
	 * do:
	 * GUIBuilderPanel(new Object[]{"", 11, 1L}, 
	 * 					new int[]{2,1,0}, 
	 * 					StringWidget(...), 
	 * 					IntWidget(...), 
	 *					LongWidget(...)); 
	 * 
	 * 
	 * @param initial
	 * @param widget_output_order
	 * @param widgets
	 */
	public GUIBuilderPanel(Object[] initial, int[] widget_order, GUIBuilderWidget...widgets)
	{				
		if(initial.length != widgets.length || widgets.length != widget_order.length)
			throw new IllegalArgumentException("initial length must match widget length!");
		
		this.initial = initial;
		
		// Re-make the widgetset
		BuilderPanelTuple[] bpt = new BuilderPanelTuple[widgets.length];
		for(int i = 0; i < bpt.length; i++)
			bpt[i] = new BuilderPanelTuple(widget_order[i], widgets[i]);
		
		Arrays.sort(bpt);
		
		GUIBuilderWidget[] output_ordered_widgets = new GUIBuilderWidget[widgets.length];
		for(int i = 0; i < bpt.length; i++)
			output_ordered_widgets[i] = bpt[i].widget;
		
		widgetset = output_ordered_widgets;
		
		setupWidgets();
		
		// Set the widgetset to what it should be.
		widgetset = widgets;

	}
	
	public GUIBuilderPanel(Object[] initial, GUIBuilderWidget[] extraWidgets, GUIBuilderWidget...widgets)
	{
		
		this(initial, extraWidgets, new int[0], widgets);
	}
	
	public GUIBuilderPanel(Object[] initial, GUIBuilderWidget[] extraWidgets, int[] widgetOrder, GUIBuilderWidget...widgets)
	{
		// If widget order is empty, we will construct the default one.
		if(widgetOrder.length == 0)
		{
			widgetOrder = new int[widgets.length];
			
			for(int i = 0; i < widgetOrder.length; i++)
				widgetOrder[i] = i;
		}
		
		if(initial.length != widgets.length || widgets.length != widgetOrder.length)
			throw new IllegalArgumentException("initial length must match widget length!");
		
		this.initial = initial;
		
		// Re-make the widgetset
		BuilderPanelTuple[] bpt = new BuilderPanelTuple[widgets.length];
		for(int i = 0; i < bpt.length; i++)
			bpt[i] = new BuilderPanelTuple(widgetOrder[i], widgets[i]);
		
		Arrays.sort(bpt);
		
		GUIBuilderWidget[] output_ordered_widgets = new GUIBuilderWidget[widgets.length + extraWidgets.length];
		for(int i = 0; i < bpt.length; i++)
			output_ordered_widgets[i] = bpt[i].widget;
		
		for(int i = 0; i <  extraWidgets.length; i++)
			output_ordered_widgets[i + bpt.length] = extraWidgets[i];
		
		widgetset = output_ordered_widgets;
		
		setupWidgets();
		
		// Set the widgetset to what it should be.
		widgetset = widgets;
	}
	
	private class BuilderPanelTuple implements Comparable<BuilderPanelTuple>
	{
		public int order;
		public GUIBuilderWidget widget;
		
		public BuilderPanelTuple(int order, GUIBuilderWidget widget)
		{
			this.order = order;
			this.widget = widget;
		}

		@Override
		public int compareTo(BuilderPanelTuple o)
		{
			if(this.order < o.order)
				return -1;
			if(this.order > o.order)
				return 1;
			return 0;
		}
	}
	
	/**
	 * Get the value of the GUIBuilderPanel
	 * @return
	 */
	public Object[] getResult()
	{
		Object[] result = new Object[widgetset.length];
		
		for(int i = 0; i < initial.length; i++)
			if(widgetset[i] != null && widgetset[i].isContentChanged())
			{
				result[i] = widgetset[i].getValue();
			}else{
				result[i] = initial[i];
			}
				
		return result;
	}
}
