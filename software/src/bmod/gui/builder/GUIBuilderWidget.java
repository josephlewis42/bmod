package bmod.gui.builder;

import java.awt.Component;
import java.awt.event.FocusListener;

public abstract class GUIBuilderWidget
{
	private final String m_title;
	
	public GUIBuilderWidget(String widget_title)
	{
		m_title = widget_title;
	}
	
	/**
	 * Gets the title of the widget.
	 * 
	 * @return The title of the widget.
	 */
	public String getTitle()
	{
		return m_title;
	}
	
	/**
	 * @return True if the content changed from the original.
	 */
	public abstract boolean isContentChanged();
	
	/**
	 * Gets the value of the widget.
	 */
	public abstract Object getValue();
	
	/**
	 * Adds a focus listener to the GUIBuilderWidget
	 * @param l - the focus listener to add.
	 */
	public void addFocusListener(FocusListener l)
	{
		getComponent().addFocusListener(l);
	}
	
	/**
	 * Gets the given component.
	 * @return
	 */
	public abstract Component getComponent();
	
	/**
	 * Tells the widget builder to put this widget as full width.
	 * DEFAULT: False override for true
	 */
	public boolean getFullWidth()
	{
		return false;
	}
	
	/**
	 * Tells the gui builder to put this widget as maximum height.
	 * DEFAULT: False override for true
	 */
	public boolean getMaxHeight()
	{
		return false;
	}
}
