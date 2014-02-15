package bmod.gui.builder;

import java.awt.Component;

import bmod.gui.widgets.DecimalSpinner;

public class DoubleWidget extends GUIBuilderWidget
{
	private final DecimalSpinner component;
	private final double orig_value;
	
	public DoubleWidget(String title, double value, boolean editable)
	{
		super(title);
		orig_value = value;
		component = new DecimalSpinner(value, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, .01, 2);
		component.setEnabled(editable);
		component.setValue(value);
	}

	@Override
	public boolean isContentChanged()
	{
		return orig_value != component.getDouble();
	}

	@Override
	public Object getValue()
	{
		return component.getDouble();
	}

	@Override
	public Component getComponent()
	{
		return component;
	}
}
