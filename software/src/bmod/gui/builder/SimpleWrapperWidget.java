package bmod.gui.builder;

import java.awt.Component;

/**
 * Provides a sipmle wrapper for sticking Components in the wrapper. getValue
 * always returns null, and isContentChanged is always false.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class SimpleWrapperWidget extends GUIBuilderWidget
{
	private final Component m_component;
	private boolean m_fullHeight = false;
	private boolean m_fullWidth = false;
	
	public SimpleWrapperWidget(String name, Component c)
	{
		super(name);
		this.m_component = c;
	}
	
	public SimpleWrapperWidget(String name, Component c, boolean maxH, boolean maxW)
	{
		this(name, c);
		
		m_fullHeight = maxH;
		m_fullWidth = maxW;
	}

	/**
	 * Always returns false.
	 */
	@Override
	public boolean isContentChanged()
	{
		return false;
	}

	/**
	 * Always returns null.
	 */
	@Override
	public Object getValue()
	{
		return null;
	}

	@Override
	public Component getComponent()
	{
		return m_component;
	}
	
	@Override
	public boolean getFullWidth()
	{
		return m_fullWidth;
	}
	

	@Override
	public boolean getMaxHeight()
	{
		return m_fullHeight;
	} 
}
