package bmod.unittests;

import static org.junit.Assert.*;

import org.junit.Test;

import bmod.gui.widgets.TimeChooser;
import bmod.util.DateTime;

public class TimeChooserTest
{
	TimeChooser t = new TimeChooser();
	DateTime now = new DateTime();
	TimeChooser k = new TimeChooser(now);

	@Test
	public void testGetTime()
	{
		assertTrue(t.getTime() == 0);
		
		// Don't bother with miliseconds
		assertTrue(k.getTime() / 1000 == now.getTimeOfDay() / 1000);
	}

}
