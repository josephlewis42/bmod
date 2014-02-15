package bmod.gui.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JToolBar;

import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;

/**
 * A widget representing a range of DateTimes.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class DateTimeRangeChooser extends JToolBar
{
	private static final long serialVersionUID = -8435652785913668654L;
	
	private final JLabel fromLabel = new JLabel("From ");
	private final JLabel toLabel = new JLabel("To ");
	private final JLabel stepLabel = new JLabel("Step (sec) ");
	private final DateTimeChooser fromTime = new DateTimeChooser();
	private final DateTimeChooser toTime = new DateTimeChooser();
	private final JSpinner stepSpinner = new JSpinner();

	public DateTimeRangeChooser()
	{
		//setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setFloatable(false);
		add(fromLabel);
		add(fromTime);
		addSeparator();
		add(toLabel);
		add(toTime);
		addSeparator();
		add(stepLabel);
		add(stepSpinner);
		stepSpinner.setValue(3600);
		
		fromTime.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(fromTime.getDateTime().after(toTime.getDateTime()))
					toTime.setDateTime(fromTime.getDateTime());
			}
		});
		
		toTime.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(toTime.getDateTime().before(fromTime.getDateTime()))
					fromTime.setDateTime(toTime.getDateTime());
			}
		});
	}
	
	/**
	 * Sets the times shown in this widget.
	 * 
	 * @param from - the time that this widget generates from.
	 * @param to - the time that this widget generates to.
	 */
	public void setTimes(DateTime from, DateTime to)
	{
		fromTime.setDateTime(from);
		toTime.setDateTime(to);
	}
	
	/**
	 * Sets the step (in seconds) that this range will represent.
	 * 
	 * @param step - the step to set (in seconds)
	 */
	public void setStep(long step)
	{
		stepSpinner.setValue(step);
	}
	
	/**
	 * Get the range represented by this chooser.
	 * 
	 * @return A new DateTimeRange.
	 */
	public DateTimeRange getRange()
	{
		try
		{
			return DateTime.range(fromTime.getDateTime(), toTime.getDateTime(), (Integer) stepSpinner.getValue());
		}catch(ClassCastException ex)
		{
			return DateTime.range(fromTime.getDateTime(), toTime.getDateTime(), (Long) stepSpinner.getValue());
		}
	}
}
