package bmod.buildingactivity;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import bmod.gui.widgets.KeyValuePanel;
import bmod.gui.widgets.TimeChooser;
import bmod.gui.widgets.VerticalPanel;
import bmod.util.DateTime;
import bmod.util.TimeDelta;

import com.michaelbaranov.microba.calendar.DatePicker;

public class RepeatingEvent extends BuildingActivityInterface
{
	private final static Logger m_logger = Logger.getLogger("RepeatingEvent");

	protected boolean m_mon = false;
	protected boolean m_tue = false;
	protected boolean m_wed = false;
	protected boolean m_thu = false;
	protected boolean m_fri = false;
	protected boolean m_sat = false;
	protected boolean m_sun = false;
	protected DateTime m_startTime;
	protected DateTime m_endTime;
	
	
	public RepeatingEvent(String daysOfWeekString, DateTime start, DateTime end)
	{
		this(buildInterfaceString(
				"mon",daysOfWeekString.contains("M") + "",
				"tue", daysOfWeekString.contains("T") + "",
				"wed", daysOfWeekString.contains("W") + "",
				"thu", daysOfWeekString.contains("R") + "",
				"fri", daysOfWeekString.contains("F") + "",
				"sat", daysOfWeekString.contains("S") + "",
				"sun", daysOfWeekString.contains("U") + "",
				"startTime", start.getTime() + "",
				"endTime", end.getTime() + ""));
	}

	public RepeatingEvent(String optionString)
	{
		super(optionString);

		m_mon = getBoolProp("mon", true);
		m_tue = getBoolProp("tue", true);
		m_wed = getBoolProp("wed", true);
		m_thu = getBoolProp("thu", true);
		m_fri = getBoolProp("fri", true);
		m_sat = getBoolProp("sat", true);
		m_sun = getBoolProp("sun", true);

		m_startTime = new DateTime(getLongProp("startTime", 0));
		m_endTime = new DateTime(getLongProp("endTime", 0));
	}

	@Override
	public boolean happensWithin(DateTime start, DateTime end)
	{
		return (start.getTimeOfDay() <  m_endTime.getTimeOfDay() && 
				end.getTimeOfDay() > m_startTime.getTimeOfDay() &&
				start.before(m_endTime) &&
				end.after(m_startTime) &&
				(
					(start.getDay() == 1 && m_sun) || 
					(start.getDay() == 2 && m_mon) || 
					(start.getDay() == 3 && m_tue) || 
					(start.getDay() == 4 && m_wed) || 
					(start.getDay() == 5 && m_thu) || 
					(start.getDay() == 6 && m_fri) ||
					(start.getDay() == 7 && m_sat)
				));
	}

	@Override
	public JPanel getEditorPanel()
	{
		return new MEditorPanel();
	}

	@Override
	public String getInterfaceTypeFromPanel(JPanel m_panel)
	{
		if(m_panel instanceof MEditorPanel)
		{
			MEditorPanel mp = (MEditorPanel) m_panel;
			return buildInterfaceString(
					"mon", mp.mondaySelected() + "",
					"tue", mp.tuesdaySelected() + "",
					"wed", mp.wednesdaySelected() + "",
					"thu", mp.thursdaySelected() + "",
					"fri", mp.fridaySelected() + "",
					"sat", mp.saturdaySelected() + "",
					"sun", mp.sundaySelected() + "",
					"startTime", mp.getStartTime() + "",
					"endTime", mp.getEndTime() + "");
		}
		throw new IllegalArgumentException("The given panel does not represent this activity");
	}

	@Override
	public String getHumanReadableName()
	{
		return "Weekday and Time over Dates";
	}

	private class MEditorPanel extends KeyValuePanel 
	{
		private final JCheckBox mon, tue, wed, thu, fri, sat, sun;
		private final TimeChooser startTimeChooser, endTimeChooser;
		private static final long serialVersionUID = 1L;
		private final DatePicker beginDatePicker, endDatePicker;

		public MEditorPanel()
		{			

			JPanel panel = new VerticalPanel();
			mon = new JCheckBox("Mon");
			mon.setSelected(m_mon);
			panel.add(mon);
		
			tue = new JCheckBox("Tue");
			tue.setSelected(m_tue);
			panel.add(tue);
		
			wed = new JCheckBox("Wed");
			wed.setSelected(m_wed);
			panel.add(wed);
		
			thu = new JCheckBox("Thu");
			thu.setSelected(m_thu);
			panel.add(thu);
		
			fri = new JCheckBox("Fri");
			fri.setSelected(m_fri);
			panel.add(fri);
		
			sat = new JCheckBox("Sat");
			sat.setSelected(m_sat);
			panel.add(sat);

			sun = new JCheckBox("Sun");
			sun.setSelected(m_sun);
			panel.add(sun);
			add("Days of Week", panel, 2.0);

		
			beginDatePicker = new DatePicker(m_startTime.toDate());
			add("Start Date", beginDatePicker);
		
		
			endDatePicker = new DatePicker(m_endTime.toDate());
			add("End Date", endDatePicker);
			
			startTimeChooser = new TimeChooser(m_startTime);
			add("Start Time", startTimeChooser);

			endTimeChooser = new TimeChooser(m_endTime);
			add("End Time", endTimeChooser);
			
			
		}

		public boolean mondaySelected(){
			return mon.isSelected();
		}

		public boolean tuesdaySelected(){
			return tue.isSelected();
		}

		public boolean wednesdaySelected(){
			return wed.isSelected();
		}

		public boolean thursdaySelected(){
			return thu.isSelected();
		}

		public boolean fridaySelected(){
			return fri.isSelected();
		}

		public boolean saturdaySelected(){
			return sat.isSelected();
		}

		public boolean sundaySelected(){
			return sun.isSelected();
		}

		public long getStartTime()
		{
			TimeDelta diff = new TimeDelta(0,0,0,0,startTimeChooser.getTime());
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
		return "RepeatingEvent".hashCode();
	}
	
	@Override
	public String getHumanString()
	{
		String output = "";
		
		if(m_mon)
			output += "M";

		if(m_tue)
			output += "T";
		
		if(m_wed)
			output += "W";
		
		if(m_thu)
			output += "R";
		
		if(m_fri)
			output += "F";
		
		if(m_sat)
			output += "S";
		
		if(m_sun)
			output += "U";
		
		output += " " + m_startTime.toISOTime() + " - " + m_endTime.toISOTime() + " between " + m_startTime.toISODate().substring(0, 10) + " and " + m_endTime.toISODate().substring(0, 10);
		return output;
	}

	@Override
	public double getPercentageTimeFilled(DateTime start, DateTime end)
	{
		if(!happensWithin(start,end))
			return 0;
		
		// Find the greater start time
		long totalTime = end.getTime() - start.getTime();
		
		long s, e;
		if(start.getTimeOfDay() > m_startTime.getTimeOfDay())
			s = start.getTimeOfDay();
		else
			s = m_startTime.getTimeOfDay();
		
		// Find the smaller end time
		if(end.getTimeOfDay() < m_endTime.getTimeOfDay())
			e = end.getTimeOfDay();
		else
			e = m_endTime.getTimeOfDay();
	
		m_logger.debug("Percent time filled is: "+(((double) e - (double) s) / totalTime));
		return (((double) e - (double) s) / totalTime);
		
	}
}
