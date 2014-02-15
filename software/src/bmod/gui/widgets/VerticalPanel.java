package bmod.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * A simple panel that has a vertical layout and shrinks all items to be 
 * vertical.
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class VerticalPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private final JPanel m_internalPanel = new JPanel();

	public VerticalPanel(JComponent...comps)
	{
		setLayout(new BorderLayout());
		super.add(m_internalPanel, BorderLayout.PAGE_START);
		
		
		m_internalPanel.setLayout(new BoxLayout(m_internalPanel, BoxLayout.Y_AXIS));
		
		for(JComponent tmp : comps)
		{
			m_internalPanel.add(tmp);
		}
	}
	
	@Override
	public Component add(Component comp)
	{
		return m_internalPanel.add(comp);
	}
	
	@Override
	public Component add(Component comp, int i)
	{
		return m_internalPanel.add(comp, i);
	}
	
	@Override
	public Component add(String title, Component comp)
	{
		return m_internalPanel.add(title, comp);
	}
}
