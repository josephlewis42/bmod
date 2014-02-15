package bmod.plugin.wattagecalculator;

import java.util.Collection;
import java.util.LinkedList;

import bmod.PredictionModel;
import bmod.WattageEvent;
import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.database.objects.BuildingActivity;
import bmod.database.objects.Room;
import bmod.plugin.baseclass.ActivityPlugin;
import bmod.util.DateTime;

/**
 * A generic plugin that all functions should override; allows simple creation
 * of functions.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public abstract class Plugin extends ActivityPlugin
{
	LinkedList<WattageEvent> events = new LinkedList<WattageEvent>();
	protected BuildingActivity activity = null;
	protected DateTime startTime = DateTime.FIRST_DAY;
	protected DateTime endTime = DateTime.LAST_DAY;
	protected Room room = null;
	protected long buildingID = Database.ALL_BUILDINGS_ID;
	protected PredictionModel m_model;
	protected DBWarningsList warnings_list;
	
	
	public boolean isOnAtSameTime(Plugin p) throws DatabaseIntegrityException
	{
		activity = p.activity;
		startTime = p.startTime;
		endTime = p.endTime;
		room = p.room;
		buildingID = p.buildingID;
		
		getWatts();
		
		for(WattageEvent e : events)
			if(e.getWattage() > 0)
				return true;
		
		return false;
	}
	
	@Override
	public final Collection<WattageEvent> getWattsForActivity(PredictionModel model, DBWarningsList dbwl, BuildingActivity ba, DateTime segStart, DateTime segEnd, Room room) throws DatabaseIntegrityException 
	{
		m_model = model;
		warnings_list = dbwl;
		
		events = new LinkedList<WattageEvent>();
		
		activity = ba;
		startTime = segStart;
		endTime = segEnd;
		this.room = room;
		buildingID = room.getBuildingID();
		
		getWatts();
		
		return events;
	}
	
	/**
	 * You must override this method for any plugin, it adds watts using the addWatts() methods
	 * and returns.
	 * @throws DatabaseIntegrityException 
	 */
	public abstract void getWatts() throws DatabaseIntegrityException;
	
	/**
	 * Shows the user there was an error encountered while trying to generate these watts.
	 * 
	 * @param error_messgae The message to send to the output logger.
	 */
	public void throwError(String error_message)
	{
		warnings_list.addError(this.getClass().getSimpleName() + ": " + error_message);
	}
	
	/**
	 * Shows the user a piece of information.
	 * @param info_message The message to send to the output logger.
	 */
	public void logInfo(String info_message)
	{
		warnings_list.addInfo(this.getClass().getSimpleName() + ": " + info_message);
	}
	
	/**
	 * Adds watts to be reported for this session, and notes for the device.
	 * @param watts - Number of watts to add.
	 * @param device_notes - A string
	 */
	public void addWatts(double watts, String[] zones, String[] categories)
	{
		events.add(new WattageEvent(startTime,watts, activity, zones, categories));
	}
}
