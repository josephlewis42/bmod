package bmod.gui.builder;

import java.awt.Component;

import bmod.gui.widgets.TimeChooser;

/**
 * A widget that allows the user to select a time, returns the number of 
 * Miliseconds since midnight as a long.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class TimeWidget extends GUIBuilderWidget
{
	private final TimeChooser m_chooser;
	//private final long m_originalTime;

	public TimeWidget(String widget_title, long currentTime)
	{
		super(widget_title);
		
		m_chooser = new TimeChooser(currentTime);
		//m_originalTime = currentTime;
		
	}

	@Override
	public boolean isContentChanged()
	{
		return true;
	}

	@Override
	public Object getValue()
	{
		System.err.println("Returned time" + m_chooser.getTime());
		return m_chooser.getTime();
	}

	@Override
	public Component getComponent()
	{
		return m_chooser;
	}
}
