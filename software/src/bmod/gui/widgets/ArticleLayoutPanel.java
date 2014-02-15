package bmod.gui.widgets;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * A panel that can be used for common BorderLayout schemes.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class ArticleLayoutPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	public ArticleLayoutPanel()
	{
		setLayout(new BorderLayout());
	}
	
	public ArticleLayoutPanel(JComponent header, JComponent content, JComponent footer)
	{
		this();
		
		if(header != null)
		{
			add(header, BorderLayout.PAGE_START);
		}
		
		if(content != null)
		{
			add(content, BorderLayout.CENTER);
		}
		
		if(footer != null)
		{
			add(footer, BorderLayout.PAGE_END);
		}
	}
	
	public ArticleLayoutPanel(JComponent header, JComponent content, JComponent footer, JComponent sidebar)
	{
		this(header, content, footer);
		
		if(sidebar != null)
		{
			add(sidebar, BorderLayout.LINE_START);
		}
	}
}
