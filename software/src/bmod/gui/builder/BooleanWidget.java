package bmod.gui.builder;

import java.awt.Component;

import javax.swing.JCheckBox;

public class BooleanWidget extends GUIBuilderWidget
{
	private final JCheckBox checkbox = new JCheckBox();
	private final boolean original;
	
	public BooleanWidget(String title, boolean value, boolean editable)
	{
		super(title);
		
		checkbox.setSelected(value);
		checkbox.setEnabled(editable);
		original = value;
	}

	@Override
	public boolean isContentChanged()
	{
		return ! (checkbox.isSelected() == original);
	}

	@Override
	public Object getValue()
	{
		return checkbox.isSelected();
	}

	@Override
	public Component getComponent()
	{
		return checkbox;
	}
}
