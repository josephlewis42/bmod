package edu.du.cs.smartgrid;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import bmod.database.DataNotAvailableException;
import bmod.plugin.generic.headless.SmartGridProvider;
import bmod.util.DateTime;

public class SmartGridProviderTest
{

	@Test
	public void testGetFeedValueIntString()
	{
		try
		{
			SmartGridProvider.getFeedValue(746, new DateTime("2012-06-04 00:00:00"));
			assertTrue(true);
		} catch (DataNotAvailableException e)
		{
			e.printStackTrace();
			assertTrue(false);
		}
		
	}

	@Test
	public void testCacheClosestFeedValueRange()
	{
		try
		{
			SmartGridProvider.cacheClosestFeedValueRange(DateTime.range(new DateTime("2012-07-01 00:00:00"), 
						new DateTime("2012-07-30 00:00:00"), 900), 746);
		} catch (DataNotAvailableException e)
		{
		}
		
	}
}
