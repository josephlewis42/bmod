package bmod;

import bmod.util.DefaultMap;
import bmod.util.DefaultMap.ValueGenerator;

/**
 * The ModelTotalBucket creates DataSets from WattageEvents using a function
 * that decides where to put each event.
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 * @param <K>
 */
public class ModelTotalBucket
{
	private final DefaultMap<String, DataSet> m_default;
	private final BucketFunction m_sorter;
	
	public ModelTotalBucket(BucketFunction bf)
	{
		m_default = new DefaultMap<>(
						new ValueGenerator<String, DataSet>()
						{
							@Override
							public DataSet getValue(String key)
							{
								return new DataSet(key.toString());
							}
						});
		m_sorter = bf;
	}
	
	
	public static abstract class BucketFunction
	{
		/**
		 * Returns the indexes for the given WattageEvent. 
		 * Returns null on no index.
		 * @param e
		 * @return
		 */
		public abstract String[] getIndexes(WattageEvent e);
	}
	
	
	public void add(WattageEvent e)
	{
		for(String index : m_sorter.getIndexes(e))
		{
			m_default.get(index).incrementPoint(e.getStartTime(), e.getWattage());
		}
	}
	
	public void addAll(Iterable<WattageEvent> events)
	{
		for(WattageEvent e : events)
		{
			add(e);
		}
	}
	
	public void clear()
	{
		m_default.clear();
	}
	
	public DataSet[] getDataSets()
	{
		return m_default.values().toArray(new DataSet[m_default.size()]);
	}
}
