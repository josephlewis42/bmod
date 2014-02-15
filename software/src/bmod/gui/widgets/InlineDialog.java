package bmod.gui.widgets;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/**
 * An inline dialog that can be used to display information/warnings about
 * what is going on.
 * 
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class InlineDialog extends JPanel
{
	private static final long serialVersionUID = 1L;

	JLabel m_text = new JLabel();
	
	private static final int DIALOG_TIMEOUT_MS = 5000;
	private static final Color INFO_COLOR = new Color(0xe8,0xf2,0xfe);
	private static final Color ERROR_COLOR = new Color(0xff, 0xa1, 0x96);
	private static final Color WARN_COLOR = new Color(0xd5, 0x96, 0xff);
	
	
	public InlineDialog()
	{
		add(m_text);
		hideWidget();
	}
	
	public void showInfo(String message)
	{
		showDialog(message, INFO_COLOR);
	}
	
	public void showWarning(String message)
	{
		showDialog(message, WARN_COLOR);
	}
	
	public void showError(String message)
	{
		showDialog(message, ERROR_COLOR);
	}
	
	private void showDialog(String message, Color c)
	{
		m_text.setText(message);
		setBackground(c);
		showWidget();
		
		
		(new  HideWidgetTimeout()).execute();
	}
	
	private void hideWidget()
	{
		setVisible(false);
		if(getParent() != null)
			getParent().revalidate();
	}
	
	class HideWidgetTimeout extends SwingWorker<Object, Object> {

		@Override
		protected Object doInBackground() throws Exception
		{
			Thread.sleep(DIALOG_TIMEOUT_MS);
			hideWidget();
			return null;
		}
	  }
	
	private void showWidget()
	{
		setVisible(true);
		if(getParent() != null)
			getParent().revalidate();
	}
}
