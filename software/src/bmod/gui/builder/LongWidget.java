package bmod.gui.builder;

import java.awt.Component;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class LongWidget extends GUIBuilderWidget
{
	private final JSpinner component = new JSpinner(new SpinnerNumberModel());
	private final long orig_value;
	
	public LongWidget(String title, long value, boolean editable)
	{
		super(title);
		orig_value = value;
		component.setEnabled(editable);
		component.setValue(value);
	}

	@Override
	public boolean isContentChanged()
	{

		return orig_value != ((Number) component.getValue()).longValue();
	}

	@Override
	public Object getValue()
	{
		return  ((Number) component.getValue()).longValue();
	}

	@Override
	public Component getComponent()
	{
		return component;
	}

}
