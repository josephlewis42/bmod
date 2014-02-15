package bmod.plugin.generic.headless;

import bmod.GenericPlugin;
import bmod.PredictionModel;
import bmod.database.DBWarningsList;
import bmod.util.DateTime;

/**
 * Times how long the miner takes.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class Timer extends GenericPlugin
{
	public Timer()
	{
		super("Timer", "Calculates how long it takes for an entire simulation" +
				" to run and reports it.");
	}

	long startTime;
	
	@Override
	public void minerStartHook(DBWarningsList wl, PredictionModel pm)
	{
		DateTime start = new DateTime();
		startTime = start.getTime();
		wl.addInfo("Started Miner: " + start);
	}

	@Override
	public void minerEndHook(DBWarningsList wl, PredictionModel pm)
	{
		DateTime end = new DateTime();
		
		wl.addInfo("Ended Miner: " + end);
		wl.addInfo("Total Miner Time (S): " + ((end.getTime() - startTime) / 1000.0));
	}

	@Override
	public void setupHeadless()
	{		
	}

	@Override
	public void teardown()
	{		
	}
}
