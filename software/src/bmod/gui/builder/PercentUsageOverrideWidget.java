package bmod.gui.builder;

import java.awt.Component;

import bmod.SerializableHashMap;
import bmod.gui.widgets.PercentUsageOverrideEditor;

public class PercentUsageOverrideWidget extends GUIBuilderWidget
{
	private final PercentUsageOverrideEditor puoe = new PercentUsageOverrideEditor();
	private final SerializableHashMap<String, Double> m_overrides;
	private final String original;
	
	public PercentUsageOverrideWidget(String label, SerializableHashMap<String, Double> overrides)
	{
		super(label);
		puoe.setMap(overrides);
		m_overrides = overrides;
		original = m_overrides.safeSerialize("");
		
	}


	@Override
	public boolean isContentChanged()
	{
		return ! m_overrides.safeSerialize("").equals(original);
	}

	@Override
	public Object getValue()
	{
		return m_overrides.safeSerialize("");
	}

	@Override
	public Component getComponent()
	{
		return puoe;
	}
	
	@Override
	public boolean getMaxHeight()
	{
		return true;
	}

}
