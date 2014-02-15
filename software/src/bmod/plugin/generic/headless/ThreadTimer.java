package bmod.plugin.generic.headless;

import java.util.HashMap;

import bmod.GenericPlugin;
import bmod.PredictionModel;
import bmod.database.DBWarningsList;
import bmod.database.objects.Room;
import bmod.util.DateTime;

/**
 * Times how long the miner takes.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class ThreadTimer extends GenericPlugin
{
	
	public ThreadTimer()
	{
		super("Thread Timer", "Records the amount of time each room takes to " +
				"process in the simulation, and reports it to you.");
	}

	private static final HashMap<Room, Long> startTimes = new HashMap<Room, Long>();
	private static final HashMap<Room, Double> totalTimes = new HashMap<Room, Double>();
	
	@Override
	public void minerStartHook(DBWarningsList wl, PredictionModel pm)
	{
		startTimes.clear();
		totalTimes.clear();
	}
	
	@Override
	public void minerThreadStartedHook(DBWarningsList wl, PredictionModel pm, Room rm)
	{
		startTimes.put(rm,  new DateTime().getTime());
	}
	
	@Override
	public void minerThreadEndedHook(DBWarningsList wl, PredictionModel pm, Room rm)
	{
		totalTimes.put(rm,  (new DateTime().getTime() - startTimes.get(rm)) / 1000.0);
	}

	@Override
	public void minerEndHook(DBWarningsList wl, PredictionModel pm)
	{
		StringBuilder sb = new StringBuilder("Total time taken per room: ");
		for(Room key : totalTimes.keySet())
		{
			sb.append("\n&nbsp;&nbsp;&nbsp;&nbsp;");
			sb.append(key);
			sb.append(" -> ");
			sb.append(totalTimes.get(key));
			sb.append(" seconds");
		}
		wl.addInfo(sb.toString());
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
