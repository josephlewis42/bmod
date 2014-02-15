package bmod.gui.builder;

import java.awt.Component;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class IntWidget extends GUIBuilderWidget
{
	private final SpinnerNumberModel snm = new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
	private final JSpinner spin = new JSpinner(snm);
	private final int originalValue;
	
	public IntWidget(String name, int value)
	{
		this(name, value, true);
	}
	
	public IntWidget(String name, int value, boolean editable)
	{
		super(name);
		snm.setValue(value);
		spin.setEnabled(editable);
		originalValue = value;
	}

	@Override
	public boolean isContentChanged()
	{
		return !(originalValue == snm.getNumber().intValue());
	}

	@Override
	public Object getValue()
	{
		return snm.getNumber().intValue();
	}

	@Override
	public Component getComponent()
	{
		return spin;
	}
}
