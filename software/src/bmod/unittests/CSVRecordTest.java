package bmod.unittests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import bmod.database.objects.Record;
import bmod.plugin.loader.CSVRecordLoader;

public class CSVRecordTest
{

	@Test
	public void test()
	{
		for(Record<?> csvr : new CSVRecordLoader().getRecordPluginManagers())
		{
			System.err.println(csvr.getClass().getCanonicalName());
			assertTrue(csvr.getEditor() != null);
		}
	}

}
