package bmod.gui.widgets;

import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import bmod.util.DateTime;
import bmod.util.TimeDelta;

import com.michaelbaranov.microba.calendar.DatePicker;

/**
 * Chooses a date and time.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class DateTimeChooser extends JPanel
{
	private static final long serialVersionUID = 5576897325378507755L;
	protected DatePicker m_dateChooser = new DatePicker();
	protected TimeChooser m_timeChooser = new TimeChooser();
	
	/**
	 * Sets the date and time with the default values.
	 */
	public DateTimeChooser()
	{
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		add(m_dateChooser);
		add(m_timeChooser);
	}
	
	/**
	 * Sets the date and time from the one provided.
	 * 
	 * @param d - The date to set.
	 */
	public DateTimeChooser(DateTime d)
	{
		this();
		setDateTime(d);
	}
	
	public void setDateTime(DateTime d)
	{
		try
		{
			m_dateChooser.setDate(d.toDate());
		} catch (PropertyVetoException e)
		{
		}
		m_timeChooser.setTime(d);
	}
	
	/**
	 * Returns the DateTime that the user has chosen.
	 * @return
	 */
	public DateTime getDateTime()
	{
		TimeDelta diff = new TimeDelta(0, 0, 0, 0, m_timeChooser.getTime());
		return new DateTime((new DateTime(m_dateChooser.getDate())).toMidnight().plusTime(diff).getTime());
	}
	
	public void addActionListener(ActionListener l)
	{
		m_dateChooser.addActionListener(l);
	}

}
