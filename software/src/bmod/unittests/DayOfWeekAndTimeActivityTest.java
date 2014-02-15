package bmod.unittests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import bmod.buildingactivity.BuildingActivityInterface;
import bmod.buildingactivity.DayOfWeekAndTimeActivity;
import bmod.util.DateTime;

public class DayOfWeekAndTimeActivityTest
{
	String m_test = "mon=true;tue=true;wed=true;thu=true;fri=true;sat=false;sun=false;startTime=43200000;endTime=50400000;";

	BuildingActivityInterface a = new DayOfWeekAndTimeActivity(m_test);
	
	@Test
	public void testHappensWithinDateTimeDateTime()
	{
		assertFalse(a.happensWithin(new DateTime("2012-01-01 10:00:00"), new DateTime("2012-01-01 13:00:00"))); // sun
		

		assertTrue(a.happensWithin(new DateTime("2012-01-02 10:00:00"), new DateTime("2012-01-02 13:00:00"))); // mon
	}

	@Test
	public void testGetEditorPanel()
	{
		assertNotNull(a.getEditorPanel());
	}

	@Test
	public void testGetInterfaceTypeFromPanel()
	{
		System.out.println(a.getInterfaceTypeFromPanel(a.getEditorPanel()));
		assertTrue(a.getInterfaceTypeFromPanel(a.getEditorPanel()).equals(m_test));
	}

	@Test
	public void testGetHumanReadableName()
	{
		assertNotNull(a.getHumanReadableName());
		assertTrue(a.getHumanReadableName().length() > 0);
	}

	@Test
	public void testGetBoolProp()
	{
		assertTrue(a.getBoolProp("mon", false));
		assertFalse(a.getBoolProp("sun", true));
		
		assertFalse(a.getBoolProp("caturday", false));
		assertTrue(a.getBoolProp("caturday", true));
	}

	@Test
	public void testGetDoubleProp()
	{
		assertTrue(a.getDoubleProp("startTime",0) == 43200000);
		assertTrue(a.getDoubleProp("hi world",0) == 0);

	}

	@Test
	public void testGetProp()
	{
		assertTrue(a.getProp("mon", "false").equals("true"));
	}

	@Test
	public void testGetLongProp()
	{
		assertTrue(a.getLongProp("startTime", 0) == 43200000);
		assertTrue(a.getLongProp("endTime", 0) == 50400000);
	}

	@Test
	public void testBuildInterfaceString()
	{
		String built = "mon=true;tue=true;wed=true;thu=true;fri=true;sat=false;";
		
		assertTrue(BuildingActivityInterface.buildInterfaceString("mon","true","tue","true","wed","true","thu","true","fri","true","sat","false").equals(built));
		assertFalse(BuildingActivityInterface.buildInterfaceString("mon","true").equals(built));
		
		try
		{
			BuildingActivityInterface.buildInterfaceString("mon");
			fail("Should have thrown error");
		}catch(Exception ex)
		{
			System.err.println(""); // don't ignore an exception
		}

	}

	@Test
	public void testHappensWithinLongLong()
	{
		assertTrue(a.happensWithin(new DateTime("2012-01-02 10:00:00").getTime(), new DateTime("2012-01-02 13:00:00").getTime()));
	}
}
