package bmod.gui.widgets;


/**
 * A dialog that shows the progress of an event, modal, and can't be closed.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public interface ProgressDialog
{
	public void setMaximum(int max);
	
	public void setIndeterminate(boolean var);
	
	public void close();
	
	public void setProgress(int progress);
	
	public void incrementProgress(String message, int increment);
	
	public void setProgress(String message, int progress);

	public void setNote(String message);
}
