package bmod.util;

public class TimeRange
{
	private final DateTime m_start;
	private final DateTime m_end;
	
	/**
	 * Creates a new TimeRange with the given points.
	 * 
	 * @param start - The start time of the range.
	 * @param end - The end time of the range.
	 */
	public TimeRange(DateTime start, DateTime end)
	{
		if(start.after(end))
			throw new IllegalArgumentException("Start cannot be after end!");
		
		m_start = start;
		m_end = end;
	}
	
	/**
	 * Determines if the given date time is within this TimeRange, or if it 
	 * is equal to one of the edges.
	 * 
	 * @param other
	 * @return
	 */
	public boolean contains(DateTime other)
	{
		return (other.after(m_start) && other.before(m_end)) || (other.equals(m_start)) || (other.equals(m_end));
	}
	
	public boolean containsNonInclusive(DateTime other)
	{
		return (other.after(m_start) && other.before(m_end));
	}
	
	/**
	 * Determines if one time range overlaps the other.
	 * 
	 * @param other
	 * @return
	 */
	public boolean overlap(TimeRange other)
	{
		return contains(other.m_start) || contains(other.m_end); 
	}
	
	/**
	 * If two TimeRanges overlap, this finds the largest area they occupy, and
	 * creates a new TimeRange with that area.
	 * 
	 * @param other - The other TimeRange to extend this one with.
	 * @return a new TimeRange with the earlier start time and later end time 
	 * of this and the given TimeRange.
	 * @throws IllegalArgumentException - if the two time ranges do not overlap.
	 */
	public TimeRange union(TimeRange other)
	{
		if(overlap(other) == false)
			throw new IllegalArgumentException("You cannot join non-overlapping time ranges!");
		
		DateTime newStart = (other.m_start.before(m_start))? other.m_start : m_start;
		DateTime newEnd = (other.m_end.after(m_end))? other.m_end : m_end;
		
		return new TimeRange(newStart, newEnd);
	}
	
	/**
	 * Returns true if the other TimeRange contains this one.
	 * 
	 * @param other
	 * @return
	 */
	public boolean contains(TimeRange other)
	{
		return contains(other.m_start) && contains(other.m_end); 
	}
	
	/**
	 * Returns the non-overlapping portion of this TimeRange with the given 
	 * TimeRange.
	 * 
	 * @param other
	 * @return
	 */
	public TimeRange compliment(TimeRange other)
	{
		if(overlap(other) == false)
			return new TimeRange(m_start, m_end);
		
		if(containsNonInclusive(other.m_start) && containsNonInclusive(other.m_end))
			throw new IllegalArgumentException("cannot take the compliment of a range that is in this one.");
		
		if(m_start.before(other.m_start))
		{
			return new TimeRange(m_start, other.m_start);
		}
		if(m_end.after(other.m_end))	
		{
			return new TimeRange(other.m_end, m_end);
		}
		
		return null;
	}
	
	
	@Override
	public String toString()
	{
		return "[Time Range: " + m_start.toISODate() + " to " + m_end.toISODate() + "]";
	}
	
	
	public boolean equals(TimeRange other)
	{
		return m_start.equals(other.m_start) && m_end.equals(other.m_end);
	}

	public boolean before(DateTime time)
	{
		return m_end.before(time);
	}
	
	public boolean after(DateTime time)
	{
		return m_start.after(time);
	}
	
	/**
	 * Returns the number of milliseconds the end time for this range is before
	 * the given time.
	 * 
	 * @param time - the time to judge before or after for.
	 * @return
	 */
	public long amountBefore(DateTime time)
	{
		return time.getTime() - m_end.getTime();
	}

	/**
	 * Returns the number of milliseconds the start time for this range is 
	 * after the given time.
	 * 
	 * @param time - the time to judge before or after for.
	 * @return
	 */
	public long amountAfter(DateTime time)
	{
		return m_start.getTime() - time.getTime();
	}
}