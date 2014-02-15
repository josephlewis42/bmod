package bmod;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import bmod.database.DataFeed;
import bmod.database.DataNotAvailableException;
import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;

/**
 * Represents a set of points consisting of Times and values that can be shown
 * in a table or plotted.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class DataSet
{
	private static final String UNLABELED_FEED = "Unlabeled Feed";
	private final TreeMap<DateTime, Double> m_points = new TreeMap<DateTime, Double>();
	private final String m_name;
	private double m_sum = 0;
	
	public DataSet(String columnName)
	{
		m_name = columnName;
		clear();
	}
	
	public DataSet(DateTimeRange dtr, DataFeed f)
	{
		this((f.getFeedName() != null)? f.getFeedName():UNLABELED_FEED);
		try
		{
			f.preCache(dtr.getClone());
		} catch (DataNotAvailableException e1)
		{
			e1.printStackTrace();
		}
		
		for(DateTime dt : dtr)
			try
			{
				addPoint(dt, f.getDataAtTime(dt));
			} catch (DataNotAvailableException e)
			{
				System.err.println("No Data at Time: " + dt + " for " + f.getFeedName());
			}
	}
	
	/**
	 * Creates a new data set with the same keys as this one, but with 
	 * each value multiplied by the given scale. The new dataset will
	 * be given the supplied name.
	 * 
	 * @param newName - the name to give the new data set, null for the same name.
	 * @param magnitude - the magnitude to scale the old data.
	 * @return
	 */
	public DataSet scale(String newName, double magnitude)
	{
		String finalName = (newName == null)? m_name : newName;
		DataSet scale = new DataSet(finalName);
		
		for(Entry<DateTime, Double> entry : getPoints())
		{
			scale.addPoint(entry.getKey(), entry.getValue() * magnitude);
		}
		
		return scale;
	}
	
	public void addPoint(DateTime t, double v)
	{
		if(Double.isNaN(v))
			v = 0.0;
		
		m_points.put(t, v);
		m_sum += v;
	}
	
	/**
	 * Adds a point, if it already exists, then add the value to the existing
	 * value as well.
	 */
	public void incrementPoint(DateTime t, double v)
	{
		if(Double.isNaN(v))
			v = 0.0;
		
		Double pt = m_points.get(t);
		if(pt != null)
		{
			
			
			m_points.put(t, pt + v);
			m_sum += v;
			
			return;
		}
		
		m_points.put(t,  v);
		m_sum += v;
	}
	
	public Set<Entry<DateTime, Double>> getPoints()
	{
		return m_points.entrySet();
	}
	
	/**
	 * Gets the value for the item at the given time, 0 if none.
	 * @param dt
	 * @return
	 */
	public double getValue(DateTime dt)
	{
		return getValue(dt, 0);
	}
	
	public double getValue(DateTime dt, double defaultValue)
	{
		Double d = m_points.get(dt);
		
		if(d != null)
			return d;
		
		return defaultValue;
	}
	
	/**
	 * Gets the title of this column/plot.
	 * 
	 * @return
	 */
	public String getTitle()
	{
		return m_name;
	}
	
	/**
	 * Gets the sum of all the items in this column.
	 * 
	 * @return
	 */
	public double getSum()
	{
		return m_sum;
	}
	
	/**
	 * Returns all of the DateTimes that have points in this DataSet
	 * 
	 * @return
	 */
	public Set<DateTime> keySet()
	{
		return m_points.keySet();
	}

	public Set<Entry<DateTime, Double>> entrySet()
	{
		return m_points.entrySet();
	}

	public void clear()
	{
		m_points.clear();
		m_sum = 0;
	}

	/**
	 * Counts the number of keys that are in this DataSet.
	 * @return
	 */
	public int size()
	{
		return m_points.size();
	}
	
	/**
	 * Returns a new DataSet with all of the points for this one summed up
	 * to a point occurring at midnight on their respective days.
	 * 
	 * @return A DataSet with all points summed up to their midnight values.
	 */
	public DataSet dailySums()
	{
		DataSet ds = new DataSet(m_name + " Daily Sum");
		
		for(Entry<DateTime, Double> ent : entrySet())
		{
			ds.incrementPoint(ent.getKey().toMidnight(), ent.getValue());
		}
		
		return ds;
	}
}
