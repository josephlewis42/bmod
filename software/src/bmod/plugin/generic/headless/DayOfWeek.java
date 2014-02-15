package bmod.plugin.generic.headless;

import java.util.Collection;

import bmod.GenericPlugin;
import bmod.database.DataFeed;
import bmod.database.DataNotAvailableException;
import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;

/**
 * A simple wrapper for the day of week.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class DayOfWeek extends GenericPlugin implements DataFeed
{
	public DayOfWeek()
	{
		super("Day of Week Feed",
				"Creates a data feed showing the day of the weeek.");
	}

	@Override
	public void appendAllPossibleDataFeeds(Collection<DataFeed> list)
	{
		list.add(this);
	}

	@Override
	public double getDataAtTime(DateTime t) throws DataNotAvailableException
	{
		return t.getDay();
	}

	@Override
	public void preCache(DateTimeRange precache)
			throws DataNotAvailableException
	{
		// no benefit in pre-caching.
	}

	@Override
	public String getFeedName()
	{
		return "Time: Day of Week";
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
