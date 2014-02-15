package bmod.util;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;


/** Utility class representing a date / time.  The basic representation is '2011-11-20 12:34:11'.
 * @author mjr, jal
 */
public final class DateTime implements Comparable<DateTime>, Cloneable
{
	private static final HashMap<String,Long> parsedCache = new HashMap<String,Long>(4096);
	private final static SimpleDateFormat m_iso_time_format = new SimpleDateFormat("HH:mm:ss");
	private final static SimpleDateFormat m_iso_day_format = new SimpleDateFormat("yyyy-MM-dd");
	private final static SimpleDateFormat m_iso_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final static SimpleDateFormat m_simple_format = new SimpleDateFormat("yyyyMMddHHmmss");

	private final static SimpleDateFormat m_excel_format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
	private final static SimpleDateFormat m_excel_format2 = new SimpleDateFormat("M/d/yyyy H:mm");
	private final static SimpleDateFormat m_usa_format = new SimpleDateFormat("M/d/yyyy h:mm a");
	
	// This format only used when called specifically.
	public final static SimpleDateFormat MYWEB_FORMAT = new SimpleDateFormat("dd-MMM-yy h:mmaaa");
	
	private static final Logger m_logger = Logger.getLogger("bmod.DateTime");
	private final long m_time;

	public final static DateTime FIRST_DAY = new DateTime(Long.MIN_VALUE);
	public final static DateTime LAST_DAY = new DateTime(Long.MAX_VALUE);


	public DateTime(String dateTime)
	{
		if(null == dateTime){
			throw new IllegalArgumentException("invalid date: null");
		}
		
		Long test = parsedCache.get(dateTime);
		if(test != null)
			m_time = test;
		else
		{
			Date tmp = parseDate(dateTime);
			if(tmp == null)
				throw new IllegalArgumentException("invalid date: null");
			m_time = tmp.getTime();
			parsedCache.put(dateTime, m_time);
		}
	}
	
	/**
	 * Parse a datetime with the given format (for speed, rather than robustness.)
	 * @param dateTime
	 * @param fmt
	 */
	public DateTime(String dateTime, SimpleDateFormat fmt)
	{
		if(null == dateTime || null == fmt){
			throw new IllegalArgumentException("Invalid date or format.");
		}
		
		Long test = parsedCache.get(dateTime);
		if(test != null)
			m_time = test;
		else
		{
			Date tmp = parseDate(dateTime, fmt);
			if(tmp == null)
				throw new IllegalArgumentException("invalid date: null");
			m_time = tmp.getTime();
			parsedCache.put(dateTime, m_time);
		}
	}

	public DateTime(Date d){
		m_time = d.getTime();
	}

	public DateTime(long l)
	{
		m_time = l;
	}

	/**
	 * Constructs a date time with the current time.
	 */
	public DateTime()
	{
		this(new Date());
	}
	
	/**
	 * Parses a date with a parser, returns null if no date matched,
	 * or throws an IllegalArgumentException if the date is parsed
	 * but doesn't un-parse to the same date (i.e. a date will parse
	 * if it is Feb 30, 2009, but will un parse as Mar 2, 2009)
	 */
	protected static Date parseDate(String dateString, SimpleDateFormat fmt)
	{
		Date tmpdate = null;
		try {
			tmpdate = fmt.parse(dateString);
			//System.out.println(fmt.format(tmpdate).toUpperCase());
			//System.out.println(dateString);
			if(! fmt.format(tmpdate).toUpperCase().equals(dateString.toUpperCase()))
				throw new IllegalArgumentException("invalid date: " + dateString);
			
			return tmpdate;
		} catch (ParseException e) {
		}
		return null;
	}
	

	/**
	 * Returns a Date based upon the given string if its format 
	 * can be matched.
	 * 
	 * @param dateString
	 * @return
	 */
	public static Date parseDate(String dateString)
	{
		try {
			return new Date(Long.parseLong(dateString));
		} catch(Exception ex){}

		Date tmpdate;
		synchronized(m_iso_format)
		{
			tmpdate = parseDate(dateString, m_iso_format);
			if(tmpdate != null)
				return tmpdate;
		}

		synchronized(m_excel_format)
		{
			tmpdate = parseDate(dateString, m_excel_format);
			if(tmpdate != null)
				return tmpdate;
		}

		synchronized(m_usa_format)
		{
			tmpdate = parseDate(dateString, m_usa_format);
			if(tmpdate != null)
				return tmpdate;
		}

		synchronized(m_excel_format2)
		{
			tmpdate = parseDate(dateString, m_excel_format2);
			if(tmpdate != null)
				return tmpdate;
		}

		return null;
	}

	public static boolean isLeapYear(int year)
	{
		return (year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0));
	}

	/**
	 * Returns a list of DateTimes from start to end such that
	 * each is interval seconds further than the previous one.
	 * The returned list includes start and the closest interval after 
	 * end.
	 * 
	 * Returns an empty list if start is after end.
	 * 
	 * @param start - the first time.
	 * @param end  - the time to stop working at.
	 * @param step - the number of seconds to step each date by.
	 * @return A list of dates
	 */
	public static DateTimeRange range(DateTime start, DateTime end, long step){
		m_logger.debug("Generating a range of dates between "+start + " - "+end+" by increments of "+step +" seconds.");

		return new DateTimeRange(start, end, step);
	}

	/**
	 * Converts this day to an Excel liked format.
	 */
	public synchronized String toExcelDate()
	{
		synchronized(m_excel_format)
		{
			return m_excel_format.format(toDate());		
		}
	}
	
	public synchronized String toSimpleDate()
	{
		synchronized(m_simple_format)
		{
			return m_simple_format.format(toDate());
		}
	}

	@Override
	public String toString()
	{
		if(m_time == Long.MAX_VALUE)
			return "INFINITY";
		if(m_time == Long.MIN_VALUE)
			return "-INFINITY";

		return toExcelDate();
	}

	
	private static final HashMap<Long, String> ISODateCache = new HashMap<Long, String>();
	/**
	 * Converts this day to an iso format.
	 */
	public String toISODate()
	{
		String date = ISODateCache.get(m_time);
		if(date != null)
			return date;
		
		synchronized(m_iso_format)
		{
			date =  m_iso_format.format(toDate());
			ISODateCache.put(m_time, date);
			return date;
		}
	}


	/**
	 * Gets the day of the week for this date.
	 * 
	 * Returns an String representing the day of the week.
	 * 
	 * Monday for mon
	 * Tuesday for tue
	 * Wednesday for wed
	 * Thursday for thu
	 * Friday for fri
	 * Saturday for sat
	 * Sunday for sun
	 */
	public synchronized String getDayOfWeek()
	{
		return (new SimpleDateFormat("EEEE")).format(toDate());
	}

	/**
	 * Returns a new DateTime that is this Date with time 00:00:00
	 */
	public synchronized DateTime toMidnight() 
	{
		String ndt = toISODate().substring(0, 11).concat("00:00:00");
		return new DateTime(ndt);
	}

	/**
	 * Emulate Date stuff:
	 */
	public boolean before(DateTime other)
	{
		return m_time < other.m_time;
	}

	public boolean after(DateTime other)
	{
		return m_time > other.m_time;
	}

	@Override
	public boolean equals(Object other)
	{
		if(other instanceof DateTime)
			return m_time == ((DateTime)other).m_time;
		return false;
	}

	public long getTime()
	{
		return m_time;
	}
	
	@Override
	public int hashCode()
	{
		return toDate().hashCode();
	}
	
	private int getCalendarProperty(int field)
	{
		Calendar c = new GregorianCalendar();
		c.setTime(toDate());
		return c.get(field);
	}
	
	/**
	 * Returns the month of the year this is.
	 * 0 for Jan 11 for Dec
	 */
	public int getMonth()
	{
		return getCalendarProperty(Calendar.MONTH);
	}
	
	/**
	 * Gets the day of the month this is. 1-31
	 * @return
	 */
	public int getDayOfMonth()
	{
		return getCalendarProperty(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Returns an hour of the day between 0 and 23
	 * @return
	 */
	public int getHour()
	{
		return getCalendarProperty(Calendar.HOUR_OF_DAY);
	}

	/**
	 * Returns the day of week that represents this DateTime
	 * Sunday = 1; Monday = 2...Saturday = 7
	 */
	public int getDay()
	{
		return getCalendarProperty(Calendar.DAY_OF_WEEK);
	}


	/**
	 * Represents a range of date times.
	 * 
	 * @author joseph
	 *
	 */
	public static class DateTimeRange implements Iterable<DateTime>, Iterator<DateTime>{

		private final DateTime m_end;
		private final long m_step;
		private DateTime m_last;
		private final DateTime m_start;
		
		public DateTime getStartTime()
		{
			return m_start;
		}
		
		public DateTime getEndTime()
		{
			return m_end;
		}
		
		public long getStep()
		{
			return m_step;
		}
		
		/**
		 * Gets a clone of this DateTimeRange that will start its iteration 
		 * where this one started, rather than where this one is.
		 * 
		 * @return
		 */
		public DateTimeRange getClone()
		{
			return new DateTimeRange(m_start, m_end, m_step);
		}

		/**
		 * 
		 * @param start
		 * @param end
		 * @param step - in seconds
		 */
		public DateTimeRange(DateTime start, DateTime end, long step)
		{
			m_end = end;
			m_step = step;
			m_last = start;
			m_start = start;
		}

		@Override
		public Iterator<DateTime> iterator() {
			return this;
		}

		@Override
		public boolean hasNext() {
			return m_last.before(m_end);
		}

		@Override
		public DateTime next() {
			DateTime next = new DateTime(new Date(m_last.getTime() + (1000L * m_step)));
			m_last = next;
			return next;
		}

		@Override
		public void remove() {

		}
		
		@Override
		public String toString()
		{
			return "Date Time Range between: " + m_start + " - " + m_end + " step: " + m_step;
		}
		
		public DateTime[] toArray()
		{
			LinkedList<DateTime> lldt = new LinkedList<DateTime>();
			
			for(DateTime t : this)
				lldt.add(t);
			
			return lldt.toArray(new DateTime[lldt.size()]);
		}

		public long getRangeLength()
		{
			return m_end.getTime() - m_start.getTime();
		}
		
		public long getTotalSteps()
		{
			return getRangeLength() / m_step;
		}
		
		public DateTime getHalfwayStep()
		{
			return new DateTime(((getTotalSteps() / 2) * m_step) + m_start.getTime());
		}

		public DateTimeRange firstHalf()
		{
			return new DateTimeRange(m_start.clone(), getHalfwayStep(), m_step);
		}
		public DateTimeRange secondHalf()
		{
			return new DateTimeRange(getHalfwayStep(), m_end.clone(), m_step);
		}

		public Set<DateTime> toSet()
		{
			TreeSet<DateTime> out = new TreeSet<>();
			
			for(DateTime t : this)
			{
				out.add(t);
			}
			
			return out;
		}
	}


	@Override
	public int compareTo(DateTime other) {
		if(before(other))
			return -1;
		if(after(other))
			return 1;
		return 0;
	}

	public Date toDate()
	{
		return new Date(m_time);
	}
	
	public Calendar toCalendar()
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(m_time);
		return c;
	}

	private static SimpleCache<Long,Long> cachedTimeOfDay = new SimpleCache<Long,Long>();
	/**
	 * Returns the number of miliseconds since midnight on a given day.
	 * 
	 * @return
	 */
	public synchronized long getTimeOfDay()
	{
		Long toret = cachedTimeOfDay.get(m_time);
		if(toret != null)
			return toret;
		
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(m_time);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		toret = (m_time - c.getTimeInMillis());
		
		cachedTimeOfDay.put(m_time, toret);
		
		return toret;
	}

	public int getMinute()
	{
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(m_time);

		return c.get(Calendar.MINUTE);
	}
	
	public int getSecond()
	{
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(m_time);

		return c.get(Calendar.SECOND);
	}

	
	public DateTime plusTime(TimeDelta d)
	{
		return new DateTime(m_time + d.toMilliseconds());
	}

	@Override
	public DateTime clone()
	{
		return new DateTime(m_time);
	}
	
	
	public String format(String fmt)
	{
		SimpleDateFormat f = new SimpleDateFormat(fmt);
	
		Date tmpdate = new Date(m_time);
		
		return f.format(tmpdate);
	}

	public boolean isSameDayAndMonth(DateTime endTime)
	{
		return endTime.getMonth() == getMonth() && endTime.getDayOfMonth() == getDayOfMonth();
	}

	/**
	 * Gets the year of the given date.
	 */
	public int getYear()
	{
		return getCalendarProperty(Calendar.YEAR);
	}

	public double getDecimalTime()
	{
		return getHour() + (((60 * getMinute()) + getSecond()) / 3600.0);
	}

	/**
	 * Converts to an ISO Time format HH:MM:SS
	 * @return
	 */
	public String toISOTime()
	{
		synchronized(m_iso_time_format)
		{
			return m_iso_time_format.format(toDate());	
		}
	}
	
	/**
	 * Converts to an ISO Day format, YYYY-MM-DD
	 * @return
	 */
	public String toISODay()
	{
		synchronized(m_iso_day_format)
		{
			return m_iso_day_format.format(toDate());
		}
	}
	
	public static DateTime findMax(DateTime[] list)
	{
		if(list == null || list.length == 0)
		{
			return null;
		}
		
		if(list.length == 1)
		{
			return list[0];
		}
		
		DateTime greatest = list[0];
		for(DateTime t : list)
		{
			if(t.m_time > greatest.m_time)
			{
				greatest = t;
			}
		}
		return greatest;
	}
	
	public static DateTime findMin(DateTime[] list)
	{
		if(list == null || list.length == 0)
		{
			return null;
		}
		
		if(list.length == 1)
		{
			return list[0];
		}
		
		DateTime smallest = list[0];
		for(DateTime t : list)
		{
			if(t.m_time < smallest.m_time)
			{
				smallest = t;
			}
		}
		
		return smallest;
	}

	/**
	 * Returns the difference between this time and another.
	 * 
	 * @param key
	 * @return
	 */
	public TimeDelta subtract(DateTime key)
	{
		return new TimeDelta(0,0,0,0,m_time - key.m_time);
	}
	
	/**
	 * Converts the hours and minutes to fractions of an hour, e.g. 23.25 
	 * for 11:15pm.
	 */
	public String toFractionHours()
	{
		if(getDecimalTime() < 10)
			return "0" + String.format("%02.2f", getDecimalTime());
		else
			return String.format("%02.2f", getDecimalTime());
	}
}
