package bmod.plugin.baseclass;

import java.util.Collection;
import java.util.LinkedList;

import bmod.PredictionModel;
import bmod.WattageEvent;
import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.database.objects.BuildingActivity;
import bmod.database.objects.BuildingLoad;
import bmod.database.objects.DeviceType;
import bmod.database.objects.Room;
import bmod.util.DateTime;

/**
 * A base class that all plugins will extend.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 * @copyright University of Denver 2012
 *
 */
public class ActivityPlugin
{	
	/**
	 * Returns the number of watts used during a single activity during 
	 * the given time range for the given room.
	 * 
	 * @param ba - the activity
	 * @param start - the start time to check the activity at
	 * @param end - the end time to check the activity at
	 * @param room - the room the activity happens in
	 * @return number of watts consumed.
	 * @throws DatabaseIntegrityException 
	 */
	public Collection<WattageEvent> getWattsForActivity(PredictionModel model, DBWarningsList dbwl, BuildingActivity ba, DateTime segStart, DateTime segEnd, Room room) throws DatabaseIntegrityException 
	{
		LinkedList<WattageEvent> events = new LinkedList<WattageEvent>();
		
		for(BuildingLoad bl : Database.templateBuildingLoad.readLoadsForActivity(ba))
		{
			double dc = bl.getPercentUsage(ba.getActivityType());
			
			// Find the percentage of the time slot that is filled with the activity.
			double pct = ba.getPercentageTimeFilled(segStart, segEnd);
			if(ba.getActivityType().equals("Always") || ba.getActivityType().equals("Work Day"))
				pct = 1.0;
						
			DeviceType t = Database.templateDeviceType.readPrimaryKey(bl.getDeviceType());
			
			double wattsForActivity = 0;
			if(bl.isPopulationMultiplied())
				wattsForActivity += pct * (t.getWatts()* dc * bl.getDeviceQuantity() * ba.getPopulation());
			else
				wattsForActivity += pct * (t.getWatts() * dc * bl.getDeviceQuantity());
			
			events.add(new WattageEvent(segStart, wattsForActivity, ba, bl, t));
		}
		
		return events;
	}

	
	/**
	 * Returns the name of this ActivityPlugin.
	 * @return
	 */
	public String getName()
	{
		return getClass().getSimpleName();
	}
}
