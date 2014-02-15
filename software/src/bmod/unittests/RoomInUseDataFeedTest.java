package bmod.unittests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import bmod.database.DataNotAvailableException;
import bmod.database.objects.RoomInUseDataFeed;
import bmod.util.DateTime;

public class RoomInUseDataFeedTest
{
	RoomInUseDataFeed template = new RoomInUseDataFeed();

	@Test
	public void testFromSQLObjectArray()
	{
		RoomInUseDataFeed r = new RoomInUseDataFeed(1800, 1800);
		RoomInUseDataFeed c = template.fromSQL(new Object[]{1800L, 1800L});
		assertTrue(r.equals(c));
	}

	@Test
	public void testGetDataAtTime()
	{
		long olin_103_id = 103;
		DateTime on = new DateTime("2011-09-12 13:00:01");
		DateTime off = new DateTime("2011-09-12 16:00:01");
		RoomInUseDataFeed r = new RoomInUseDataFeed(olin_103_id);
		
		try
		{
			System.out.println(r.getDataAtTime(on));
			assertTrue(r.getDataAtTime(on) == 1.0);
			assertTrue(r.getDataAtTime(off) == 0.0);
		} catch (DataNotAvailableException e)
		{
			fail("Exception encountered.");
			e.printStackTrace();
		}
		
	}
}
