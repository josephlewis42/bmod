package bmod.unittests;

import static org.junit.Assert.assertTrue;

import javax.swing.JFrame;

import org.junit.Test;

import bmod.gui.SwingSet;
import bmod.gui.widgets.DateTimeRangeChooser;
import bmod.util.DateTime;

public class DateTimeRangeChooserTest
{

	@Test
	public void test()
	{
		DateTimeRangeChooser c = new DateTimeRangeChooser();
		
		DateTime st = new DateTime("2012-06-04 00:00:00");
		DateTime et = new DateTime("2012-07-04 00:00:00");
		
		long range = 60L;
		
		c.setTimes(st, et);
		c.setStep(range);
		
		assertTrue(c.getRange().getStartTime().equals(st));
		assertTrue(c.getRange().getEndTime().equals(et));
		assertTrue(c.getRange().getStep() == range);
		
	}
	
	public static void main(String[] args)
	{

		DateTimeRangeChooser c = new DateTimeRangeChooser();
		
		DateTime st = new DateTime("2012-06-04 00:00:00");
		DateTime et = new DateTime("2012-07-04 00:00:00");
				
		c.setTimes(st, et);
		c.setStep(60);
		
		new SwingSet("Chooser", c, JFrame.EXIT_ON_CLOSE, false);
	}
}
