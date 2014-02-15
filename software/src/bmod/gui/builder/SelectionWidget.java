package bmod.gui.builder;

import java.awt.Component;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComboBox;

public class SelectionWidget extends GUIBuilderWidget
{
	private final JComboBox<?> m_component;
	private final Map<?, ?> m_options;
	private final Object m_original;
	

	public SelectionWidget(String title, Map<?, ?> map, Object currentValue)
	{
		super(title);
		
		m_component = new JComboBox<>(map.keySet().toArray());
		m_options = map;
		m_original = currentValue;
		
		for(Entry<?,?> ent : m_options.entrySet())
			if(ent.getValue().equals(currentValue))
				m_component.setSelectedItem(ent.getKey());
		
	}

	@Override
	public boolean isContentChanged()
	{
		return ! m_original.equals(m_component.getSelectedItem());
	}

	@Override
	public Object getValue()
	{
		return m_options.get(m_component.getSelectedItem());
	}

	@Override
	public Component getComponent()
	{
		return m_component;
	}

}
