package bmod.gui.widgets;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

/**
 * A dialog that shows the progress of an event, modal, and can't be closed.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class JProgressDialog extends JDialog implements Runnable, ProgressDialog
{
	private static final long serialVersionUID = -4614322449764019546L;
	private final JLabel m_label = new JLabel();
	private final JProgressBar m_progress = new JProgressBar();
	private final JPanel m_panel = new JPanel();
	private int m_maxProgress;
	
	
	public JProgressDialog(String title, String message, int maxProgress)
	{
		setTitle(title);
		m_panel.setLayout(new BoxLayout(m_panel, BoxLayout.PAGE_AXIS));
		
		m_panel.add(m_label);
		m_panel.add(m_progress);
		setMaximum(maxProgress);
		if(message == null || message.trim().equals(""))
			m_label.setText(" ");
		else
			m_label.setText(message);
		setSize(400, 100);
		
		add(m_panel);
		
		setModal(true);
		setResizable(false);
		//setUndecorated(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setLocationRelativeTo(null);
				
		new Thread(this).start();
	}
	
	@Override
	public void setMaximum(int max)
	{
		m_maxProgress = max;
		m_progress.setMaximum(max);
	}
	
	@Override
	public void setIndeterminate(boolean var)
	{
		m_progress.setIndeterminate(var);
	}
	
	@Override
	public void close()
	{
		setVisible(false);
	}
	
	@Override
	public void setProgress(int progress)
	{
		m_progress.setValue(progress);
	
		if(progress > m_maxProgress)
			close();
	}
	
	@Override
	public void setProgress(String message, int progress)
	{
		setNote(message);
		setProgress(progress);
	}

	@Override
	public void setNote(String message)
	{
		m_label.setText(message);
	}

	@Override
	public void run()
	{
		setVisible(true);
	}
	
	@Override
	public void incrementProgress(String message, int increment)
	{
		setProgress(message, m_progress.getValue() + increment);
	}
	
}
