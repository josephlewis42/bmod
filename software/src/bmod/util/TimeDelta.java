package bmod.util;

/**
 * The TimeDelta object is a difference of two times, inspired by Python's 
 * TimeDelta.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class TimeDelta implements Comparable<TimeDelta>
{
	private final long MS_PER_S = 1000;
	private final long MS_PER_MIN = MS_PER_S * 60;
	private final long MS_PER_HOUR = MS_PER_MIN * 60;
	private final long MS_PER_DAY = MS_PER_HOUR * 24;
	private final long m_ms;
	
	// The maximum possible TimeDelta
	public static final TimeDelta MAX_VALUE = new TimeDelta(0,0,0,0,Long.MAX_VALUE);
	// The minimum possible TimeDelta
	public static final TimeDelta MIN_VALUE = new TimeDelta(0,0,0,0,Long.MIN_VALUE);
	
	
	public TimeDelta(long days)
	{
		this(days, 0);
	}
	
	public TimeDelta(long days, long hours)
	{
		this(days, hours, 0);
	}
	
	public TimeDelta(long days, long hours, long minutes)
	{
		this(days, hours, minutes, 0L);
	}
	
	public TimeDelta(long days, long hours, long minutes, long seconds)
	{
		this(days, hours, minutes, seconds, 0L);
	}
	
	public TimeDelta(long days, long hours, long minutes, long seconds, long milliseconds)
	{
		m_ms = (days * MS_PER_DAY) +
				(hours * MS_PER_HOUR) + 
				(minutes * MS_PER_MIN) + 
				(seconds * MS_PER_S) + 
				milliseconds;
	}
	
	@Override
	public String toString()
	{
		long tmp = m_ms;
		long days = tmp / MS_PER_DAY;
		tmp %= MS_PER_DAY;
		long hours = tmp / MS_PER_HOUR;
		tmp %= MS_PER_HOUR;
		long minutes = tmp / MS_PER_MIN;
		tmp %= MS_PER_MIN;
		long seconds = tmp / MS_PER_S;
		tmp %= MS_PER_S;
		
		return String.format("TimeDelta of: %d days %02d:%02d:%02d.%04d", days, hours, minutes, seconds, tmp);
	}
	
	/**
	 * Returns the absoloute value of this timedelta.
	 * 
	 * @return
	 */
	public TimeDelta abs()
	{
		return new TimeDelta(0, 0, 0, 0, Math.abs(m_ms));
	}
	
	/**
	 * Returns the negative of this timedelta.
	 * @return
	 */
	public TimeDelta negate()
	{
		return new TimeDelta(0, 0, 0, 0, -m_ms);
	}
	
	/**
	 * Returns the number of milliseconds this TimeDelta represents.
	 * 
	 * @return
	 */
	public long toMilliseconds()
	{
		return m_ms;
	}
	
	public long toSeconds()
	{
		return m_ms / MS_PER_S;
	}

	@Override
	public int compareTo(TimeDelta arg)
	{
		return Long.compare(m_ms, arg.m_ms);
	}
}
