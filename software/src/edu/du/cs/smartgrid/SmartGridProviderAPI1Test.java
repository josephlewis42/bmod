package edu.du.cs.smartgrid;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

import edu.du.cs.smartgrid.SmartGridProviderAPI1.FeedValueRange;

public class SmartGridProviderAPI1Test
{
	SmartGridProviderAPI1 sg = new SmartGridProviderAPI1("ecb52298-88c5-4b04-bd97-680cbd34102f", "testsmartgrid.cs.du.edu");

	@Test
	public void testFeedList() throws IOException
	{
		Map<Integer, String> feeds = sg.feedList();
		
		assertTrue(feeds.size() > 0);
		
		for(Integer key : feeds.keySet())
			System.err.println(key + "->" + feeds.get(key));
	}
	
	@Test
	public void test_parseISOTime()
	{
		Date d = Common._parseISOTime("2012-01-02T03:04:05Z");
		assertTrue(d.getTime() == 1325473445L * 1000);
	}

	@Test
	public void testFeedValue() throws IOException
	{
		assertTrue(sg.feedValue(145, 180, Common._parseISOTime("2012-11-26T02:00:00+07:00")) == 0.0);
	}

	@Test
	public void testFeedValues() throws IOException
	{
		Common.DEBUGGING = true;
		FeedValueRange[] fvr = sg.feedValues(145, Common._parseISOTime("2012-11-26T00:00:00+07:00"), Common._parseISOTime("2012-11-26T05:00:00+07:00"));
		
		assertTrue(fvr.length > 0);
	}

	@Test
	public void testLookupFeed() throws IOException
	{
		Common.DEBUGGING = true;
		
		Map<Integer, String> feeds = sg.feedList();
		
		for(Integer key : feeds.keySet())
			assertTrue(sg.lookupFeed(feeds.get(key)) == key);
	}
}
