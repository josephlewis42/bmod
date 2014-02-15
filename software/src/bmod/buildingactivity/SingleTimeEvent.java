package bmod.buildingactivity;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import bmod.gui.widgets.TimeChooser;
import bmod.util.DateTime;
import bmod.util.TimeDelta;

import com.michaelbaranov.microba.calendar.DatePicker;

public class SingleTimeEvent extends BuildingActivityInterface
{
	private final static Logger m_logger = Logger.getLogger("SingleTimeEvent");
	private final DateTime m_startTime;
	private final DateTime m_endTime;

	public SingleTimeEvent(String optionString)
	{
		super(optionString);

		m_startTime = new DateTime(getLongProp("startTime", 0));
		m_endTime = new DateTime(getLongProp("endTime", 0));
	}

	@Override
	public boolean happensWithin(DateTime start, DateTime end)
	{
		return (start.before(m_endTime) && end.after(m_startTime));
	}

	@Override
	public JPanel getEditorPanel()
	{
		return new mEditorPanel();
	}

	@Override
	public String getInterfaceTypeFromPanel(JPanel m_panel)
	{
		if(m_panel instanceof mEditorPanel)
		{
			mEditorPanel mp = (mEditorPanel) m_panel;
			return buildInterfaceString(
					"startTime", mp.getStartTime() + "",
					"endTime", mp.getEndTime() + "");
		}
		throw new IllegalArgumentException("The given panel does not represent this activity");
	}

	@Override
	public String getHumanReadableName()
	{
		return "Single Time Event";
	}

	private class mEditorPanel extends JPanel 
	{
		private final TimeChooser startTimeChooser, endTimeChooser;
		private static final long serialVersionUID = 1L;
		private final DatePicker beginDatePicker, endDatePicker;

		public mEditorPanel()
		{
			setLayout(new GridLayout(0, 2, 0, 0));
			
			JLabel lblNewLabel = new JLabel("Start Date");
			add(lblNewLabel);
		
			beginDatePicker = new DatePicker(m_startTime.toDate());
			add(beginDatePicker);
		
			JLabel lblEndDate = new JLabel("End Date");
			add(lblEndDate);
		
			endDatePicker = new DatePicker(m_endTime.toDate());
			add(endDatePicker);
			
			add(new JLabel("Start Time"));
			startTimeChooser = new TimeChooser(m_startTime);
			add(startTimeChooser);

			add(new JLabel("End Time"));
			endTimeChooser = new TimeChooser(m_endTime);
			add(endTimeChooser);
		}


		public long getStartTime()
		{
			TimeDelta diff = new TimeDelta(0, 0, 0, 0, startTimeChooser.getTime());
			return (new DateTime(beginDatePicker.getDate())).toMidnight().plusTime(diff).getTime();
		}

		public long getEndTime()
		{
			TimeDelta diff = new TimeDelta(0, 0, 0, 0, endTimeChooser.getTime());
			return (new DateTime(endDatePicker.getDate())).toMidnight().plusTime(diff).getTime();
		}
	}

	@Override
	public int getInterfaceId()
	{
		return "SingleTimeEvent".hashCode();
	}
	
	@Override
	public String getHumanString()
	{
		return "From: " + m_startTime + " to: " + m_endTime;
	}

	@Override
	public double getPercentageTimeFilled(DateTime start, DateTime end)
	{
		if(!happensWithin(start,end))
			return 0;
		
		// Find the greater start time
		long totalTime = end.getTime() - start.getTime();
		
		long s, e;
		if(start.after( m_startTime ))
			s = start.getTime();
		else
			s = m_startTime.getTime();
		
		// Find the smaller end time
		if(end.before(m_endTime))
			e = end.getTime();
		else
			e = m_endTime.getTime(); 
		
		m_logger.debug("Percent time filled is: "+(((double) e - (double) s) / totalTime));
		return (((double) e - (double) s) / totalTime);
	}
}
