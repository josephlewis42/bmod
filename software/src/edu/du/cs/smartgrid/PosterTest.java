package edu.du.cs.smartgrid;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import bmod.util.DateTime;

public class PosterTest
{
	private static final String FEED_NAME = "__SMARTGRID_POSTER_TEST__";
	String userKey = "798d5e4f-0da4-497e-8c70-e6dffab2f4eb";
	String deviceKey = "83134d01-ea43-4201-b1df-4c95161a2c4d";
	UserOperations uo = new UserOperations(userKey, Common.TEST_HOST);
	Poster p = new Poster(deviceKey, uo, Common.TEST_HOST);
	
	@Test
	public void testAdd_data()
	{
		Common.DEBUGGING = true;
		try
		{
			int feed = uo.lookupFeed(FEED_NAME);
			uo.deleteFeed(feed);
		} catch (Exception ex)
		{
			
		}
		
		for(DateTime t : DateTime.range(new DateTime("2012-01-01 00:00:00"), new DateTime("2012-01-10 00:00:00"), 900))
		{
			p.add_data(t, t.getDecimalTime(), FEED_NAME);
		}
		try
		{
			p.post_data();
		} catch (IOException e)
		{
			fail("Couldn't post.");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try
		{
			int feed = uo.lookupFeed(FEED_NAME);
			System.err.println(feed);
			uo.setDescription(feed, "Hello\n\nWorld/Day");
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Test
	public void testPost_data()
	{
		
	}
}
