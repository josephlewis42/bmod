package bmod.gui.widgets;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bmod.util.DateTime;

public class TimeChooser extends JPanel implements ChangeListener{

	private static final long serialVersionUID = -4973008107917989666L;
	private final SpinnerNumberModel hourModel = new SpinnerNumberModel(0, 0, 24, 1);
	private final SpinnerNumberModel minuteModel = new SpinnerNumberModel(0, 0, 59, 1);
	private final SpinnerNumberModel secondModel = new SpinnerNumberModel(0, 0, 59, 1);
	JSpinner hour, minute, second;
	
	public TimeChooser(){
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		hour = new JSpinner(hourModel);
		add(hour);
		hour.addChangeListener(this);
		hour.setToolTipText("Hours 0-23, type in 24 to set the time to 23:59:59");
		
		minute = new JSpinner(minuteModel);
		add(minute);
		minute.addChangeListener(this);
		minute.setToolTipText("Minutes, 0-59");
		
		second = new JSpinner(secondModel);
		add(second);
		second.addChangeListener(this);
		second.setToolTipText("Seconds, 0-59");
	}
	
	/**
	 * Create the panel.
	 */
	public TimeChooser(DateTime d) {
		this();
		setTime(d);
	}
	
	public void setTime(DateTime d)
	{
		hourModel.setValue(d.getHour());
		minuteModel.setValue(d.getMinute());
		secondModel.setValue(d.getSecond());
	}
	
	/**
	 * Sets the time based upon the number of miliseconds given
	 * @param msPastMidnight
	 */
	public TimeChooser(long msPastMidnight) {
		this();
		// Turn to seconds
		msPastMidnight = msPastMidnight / 1000;
		
		// set hours
		hourModel.setValue((int) (msPastMidnight / 3600));
		minuteModel.setValue((int) (msPastMidnight % 3600 / 60));
		secondModel.setValue((int) (msPastMidnight % 60));		
	}
	
	/**
	 * Returns the time since the beginning of the day in 
	 * milliseconds that this Chooser represents.
	 */
	public long getTime()
	{
		long time = 0;
		
		time += secondModel.getNumber().longValue() * 1000;
		time += minuteModel.getNumber().longValue() * 60 * 1000;
		time += hourModel.getNumber().longValue() * 60 * 60 * 1000;
		
		return time;
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		// a quick way of setting end of day, just set hours to 24
		if(hourModel.getNumber().longValue() == 24)
		{
			hourModel.setValue(23);
			minuteModel.setValue(59);
			secondModel.setValue(59);
		}
	}

}
