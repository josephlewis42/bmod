package bmod.plugin.generic.headless;

import java.util.Collection;

import bmod.GenericPlugin;
import bmod.database.DataFeed;
import bmod.database.DataNotAvailableException;
import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;

/**
 * A feed for getting the actual time of day.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class TimeOfDay extends GenericPlugin implements DataFeed
{
	public TimeOfDay()
	{
		super("Time of Day Data Feed", "Provides a data feed consisting of the time of day.");
	}

	@Override
	public void appendAllPossibleDataFeeds(Collection<DataFeed> list)
	{
		list.add(this);
	}

	@Override
	public double getDataAtTime(DateTime t) throws DataNotAvailableException
	{
		return t.getMinute();
	}

	@Override
	public void preCache(DateTimeRange precache)
			throws DataNotAvailableException
	{	// would not help to pre-cache.
	}

	@Override
	public String getFeedName()
	{
		return "Time: Minutes Since Midnight";
	}
	
	@Override
	public String toString()
	{
		return getFeedName();
	}

	@Override
	public void setupHeadless()
	{		
	}

	@Override
	public void teardown()
	{		
	}
}
