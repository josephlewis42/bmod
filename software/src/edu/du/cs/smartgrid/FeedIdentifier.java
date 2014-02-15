package edu.du.cs.smartgrid;

import bmod.database.DataFeed;
import bmod.database.DataNotAvailableException;
import bmod.plugin.generic.headless.SmartGridProvider;
import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;

public class FeedIdentifier implements Comparable<FeedIdentifier>, DataFeed
{
	private final int id;
	private final String title;
	
	/**
	 * Feed identifiers provide access to feeds from the Smartgrid website.
	 * 
	 * @param csvrow_input
	 */
	public FeedIdentifier(Integer id, String title)
	{		
		this.id = id;
		this.title = title;
	}

	/**
	 * AUTO GENERATED GETTERS AND SETTERS
	 */
	
	
	public int getId()
	{
		return id;
	}

	public String getTitle()
	{
		return title;
	}
	
	@Override
	public int compareTo(FeedIdentifier oth)
	{
		return getTitle().toUpperCase().compareTo(oth.getTitle().toUpperCase());
	}

	@Override
	public double getDataAtTime(DateTime t) throws DataNotAvailableException
	{
		return SmartGridProvider.getFeedValue(this, t);
	}

	@Override
	public void preCache(DateTimeRange precache)
			throws DataNotAvailableException
	{
		SmartGridProvider.cacheClosestFeedValueRange(precache, id);
	}

	@Override
	public String getFeedName()
	{
		return title;
	}
	
	@Override
	public String toString()
	{
		return getFeedName();
	}

}
