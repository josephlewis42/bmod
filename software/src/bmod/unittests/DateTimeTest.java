package bmod.unittests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;

public class DateTimeTest {

	@Test
	public void testIsLeapYearBasic()
	{
		assertTrue(DateTime.isLeapYear(1996));
	}
	
	@Test
	public void testIsLeapYearOdd()
	{
		assertFalse(DateTime.isLeapYear(1997));
	}
	
	@Test
	public void testIsLeapYearEven()
	{
		assertFalse(DateTime.isLeapYear(1998));
	}

	@Test
	public void testIsLeapYear_100()
	{
		assertFalse(DateTime.isLeapYear(2100));
	}
	
	@Test
	public void testIsLeapYear_400()
	{
		assertTrue(DateTime.isLeapYear(2000));
	}

	@Test
	public void testIsValidDay_TooBig()
	{
		try
		{
			new DateTime("2011-13-22 00:00:00");
			assertTrue("Date didn't fail!", false);
		}catch(Exception ex){
		}
	}
	
	@Test
	public void testEquality()
	{
		assertTrue((new DateTime("2011-10-10 10:10:10")).equals(new DateTime("2011-10-10 10:10:10")));
		assertFalse((new DateTime("2011-10-10 10:10:11")).equals(new DateTime("2011-10-10 10:10:10")));

		assertTrue((new DateTime("2011-10-10 10:10:10")).compareTo(new DateTime("2011-10-10 10:10:10")) == 0);
		assertTrue((new DateTime("2011-10-10 10:10:10")).compareTo(new DateTime("2011-10-10 10:10:11")) < 0);
		assertTrue((new DateTime("2011-10-10 10:10:10")).compareTo(new DateTime("2011-10-10 10:10:09")) > 0);

		assertFalse((new DateTime("2011-10-10 10:10:10")).compareTo(new DateTime("2011-10-10 10:10:10")) != 0);
		assertFalse((new DateTime("2011-10-10 10:10:10")).compareTo(new DateTime("2011-10-10 10:10:11")) > 0);
		assertFalse((new DateTime("2011-10-10 10:10:10")).compareTo(new DateTime("2011-10-10 10:10:09")) < 0);
	}
	
	@Test
	public void testDecimalTime()
	{
		assertTrue(Math.abs(new DateTime("2011-10-10 10:10:10").getDecimalTime() - 10.169444444444444) < .0000001);
		assertTrue(Math.abs(new DateTime("2011-10-10 23:10:10").getDecimalTime() - 23.169444444444444) < .0000001);
	}
	
	@Test
	public void testMywebTime()
	{
		assertTrue(new DateTime("05-JUN-12 10:00AM", DateTime.MYWEB_FORMAT).getTime() == 
					new DateTime("2012-06-05 10:00:00").getTime());
		
		assertTrue(new DateTime("26-MAR-12 6:00PM", DateTime.MYWEB_FORMAT) != null);
	}
	
	@Test
	public void testDateTime()
	{
		int iterations = 0;
		for(@SuppressWarnings("unused") DateTime t : DateTime.range(new DateTime("2011-10-10 10:00:00"), new DateTime("2011-10-10 11:00:00"), 900))
			iterations++;
			
		assertTrue(iterations == 4);
		
	}
	
	@Test
	public void testMiddleTime()
	{
		DateTimeRange dtr = new DateTimeRange(new DateTime(0), new DateTime(500), 5);
		
		assertTrue(dtr.getTotalSteps() == 100);
		assertTrue(dtr.getHalfwayStep().getTime() == 250);
		
		DateTimeRange dtr2 = new DateTimeRange(new DateTime(1), new DateTime(500), 5);
		assertTrue(dtr2.getTotalSteps() == 99);
		assertTrue(dtr2.getHalfwayStep().getTime() == 246);

	}
	
}
