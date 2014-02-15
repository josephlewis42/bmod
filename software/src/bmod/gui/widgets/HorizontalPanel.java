package bmod.gui.widgets;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * A simple panel that has a horizontal layout.
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class HorizontalPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	public HorizontalPanel(JComponent...comps)
	{
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		for(JComponent tmp : comps)
		{
			add(tmp);
		}
	}
}
