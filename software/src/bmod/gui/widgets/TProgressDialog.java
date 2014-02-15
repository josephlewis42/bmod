package bmod.gui.widgets;

public class TProgressDialog implements ProgressDialog
{
	private static final int BAR_LENGTH = 30;
	private int m_max = 0;
	private String m_note = "";
	private int m_progress = 0;
	private String m_title = "";
	
	public TProgressDialog(String title, String message, int maxProgress)
	{
		m_title = title;
		m_note = message;
		m_max = maxProgress;
	}

	@Override
	public void setMaximum(int max)
	{
		m_max = max;
	}

	@Override
	public void setIndeterminate(boolean var)
	{
		
	}

	@Override
	public void close()
	{
		
	}

	@Override
	public void setProgress(int progress)
	{
		if(m_progress > m_max)
			return;
		m_progress = progress;
		
		System.out.print(m_title);
		System.out.print(" [");
		
		double progressPercent = progress * 1.0 / m_max;
		for(int i = 0; i < BAR_LENGTH; i++)
		{
			if(i < progressPercent * BAR_LENGTH)
				System.out.print("=");
			else
				System.out.print(" ");
		}
		
		String pct = String.format("%2.0f", progressPercent * 100);
		System.out.println("] " + pct + "% " + m_note);
	}
	
	@Override
	public void incrementProgress(String message, int increment)
	{
		setProgress(message, m_progress + increment);
	}

	@Override
	public void setProgress(String message, int progress)
	{
		m_note = message;
		setProgress(progress);
	}

	@Override
	public void setNote(String message)
	{
		m_note = message;
		setProgress(m_progress);
	}
}
