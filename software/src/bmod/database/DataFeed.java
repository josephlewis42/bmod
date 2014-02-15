package bmod.database;

import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;

/**
 * An interface representing a general DataFeed.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 * 
 */
public interface DataFeed
{
	/**
	 * Gets the data at the given time.
	 * @param t
	 * @return
	 */
	public double getDataAtTime(DateTime t) throws DataNotAvailableException;
	
	/**
	 * Precaches the data for the given time. This is an optional component, but
	 * a good idea.
	 */
	public void preCache(DateTimeRange precache) throws DataNotAvailableException;
	
	/**
	 * Gets the human readable feed name.
	 */
	public String getFeedName();
}
