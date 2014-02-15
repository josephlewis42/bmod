package bmod.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TimeRangeTest
{
	DateTime jan1 = new DateTime("2012-01-01 00:00:00");
	DateTime jan2 = new DateTime("2012-01-02 00:00:00");
	DateTime jan3 = new DateTime("2012-01-03 00:00:00");
	DateTime jan4 = new DateTime("2012-01-04 00:00:00");

	@Test
	public void testTimeRange()
	{
		try
		{
			new TimeRange(jan2, jan1);
			assertTrue(false);
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		new TimeRange(jan1, jan2);
		assertTrue(true);
	}

	@Test
	public void testContains()
	{
		TimeRange jan1to2 = new TimeRange(jan1, jan2);
		TimeRange jan2to3 = new TimeRange(jan2, jan3);
		TimeRange jan1to4 = new TimeRange(jan1, jan4);
		
		assertTrue(jan1to2.contains(jan2));
		assertFalse(jan1to2.contains(jan3));
		assertTrue(jan1to2.contains(jan1));
		
		assertTrue(jan1to4.contains(jan2to3));
		assertTrue(jan1to4.contains(jan1to2));

		
	}

	@Test
	public void testOverlap()
	{
		TimeRange jan1to2 = new TimeRange(jan1, jan2);
		TimeRange jan2to3 = new TimeRange(jan2, jan3);
		TimeRange jan3to4 = new TimeRange(jan3, jan4);
		TimeRange jan1to4 = new TimeRange(jan1, jan4);
		
		assertTrue(jan1to2.overlap(jan2to3));
		assertTrue(jan1to4.overlap(jan3to4));
		assertFalse(jan1to2.overlap(jan3to4));
	}

	@Test
	public void testUnion()
	{
		TimeRange jan1to2 = new TimeRange(jan1, jan2);
		TimeRange jan2to3 = new TimeRange(jan2, jan3);
		TimeRange jan3to4 = new TimeRange(jan3, jan4);
		TimeRange jan1to4 = new TimeRange(jan1, jan4);

		TimeRange tmp = jan1to2.union(jan2to3).union(jan3to4);
		
		assertTrue(tmp.equals(jan1to4));
	}

	@Test
	public void testCompliment()
	{
		TimeRange jan1to2 = new TimeRange(jan1, jan2);
		TimeRange jan1to4 = new TimeRange(jan1, jan4);
		
		TimeRange comp = jan1to4.compliment(jan1to2);
		
		assertTrue(comp.equals(new TimeRange(jan2, jan4)));
	}

	@Test
	public void testToString()
	{
		TimeRange jan1to2 = new TimeRange(jan1, jan2);
		assertTrue(jan1to2.toString().equals("2012-01-01 00:00:00 to 2012-01-02 00:00:00"));
	}

}
