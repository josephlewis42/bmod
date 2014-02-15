package edu.du.cs.smartgrid;

import bmod.util.DateTime;

public interface SmartGridProviderEventListener
{
	/**
	 * Called when the software requests.
	 * 
	 * @param start
	 * @param end
	 * @param feedid
	 */
	public void onCacheRequest(DateTime start, DateTime end, int feedid);
	
	/**
	 * Called when the Internet connection to the server goes down.
	 */
	public void onServerOffline();
	
	/**
	 * Called when the Internet connection to the server goes up.
	 */
	public void onServerConnected();
	
	/**
	 * Called when a feed is requested, but no value is found for the feed at
	 * the given time. If you choose to do nothing, simply return finalValue.
	 * 
	 * @param feedId
	 * @param feedTime
	 * @param finalValue
	 * @return
	 */
	public double onNoFeedValue(int feedId, DateTime feedTime, double finalValue);
}
