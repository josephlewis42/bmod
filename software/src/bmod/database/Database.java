package bmod.database;

import java.awt.Frame;

import org.apache.log4j.Logger;

import bmod.database.objects.Building;
import bmod.database.objects.BuildingActivity;
import bmod.database.objects.BuildingLoad;
import bmod.database.objects.DeviceType;
import bmod.database.objects.DeviceTypeCategory;
import bmod.database.objects.DeviceTypeDutyCycle;
import bmod.database.objects.DeviceTypeVariable;
import bmod.database.objects.Followup;
import bmod.database.objects.Room;
import bmod.database.objects.RoomVariable;
import bmod.database.objects.Source;
import bmod.database.objects.Zone;
import bmod.gui.widgets.Dialogs;

/**
 * Handles the database information for the building modeler, specifically how
 * each method connects to the database.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 * 
 * 
 * TODO provide a migration CLI script and config params.
 *
 */
public class Database
{
	public static final Building templateBuilding = new Building();
	public static final Room templateRoom = new Room();
	public static final BuildingActivity templateBuildingActivity = new BuildingActivity();
	public static final DeviceType templateDeviceType = new DeviceType();
	public static final DeviceTypeVariable templateDeviceTypeVariable = new DeviceTypeVariable();
	public static final DeviceTypeDutyCycle templateDeviceTypeDutyCycle = new DeviceTypeDutyCycle();
	public static final BuildingLoad templateBuildingLoad = new BuildingLoad();
	public static final RoomVariable templateRoomVariable = new RoomVariable();
	public static final Zone templateZone = new Zone();
	public static final DeviceTypeCategory templateDeviceTypeCategory = new DeviceTypeCategory();
	public static final Followup templateFollowup = new Followup();
	public static final Source templateSource = new Source();

	private static volatile DatabaseQueryMechanism m_mech = null;
	
	public static DatabaseQueryMechanism getDqm()
	{
		if(m_mech == null)
		{
			m_mech = new DatabaseQueryMechanism();
			m_mech.updateDatabase();
		}
		
		return m_mech;
	}
	
	/**
	 * ID used for any data feeds that don't have a building.
	 */
	public static final long ALL_BUILDINGS_ID = 0L;
	public static final long TEMPLATE_PRIMARY_KEY = Long.MIN_VALUE;
	private static long lastTime = Long.MIN_VALUE;

	/**
	 * Generates a new primary key for a database.
	 * @return
	 */
	public static synchronized final long getNewPrimaryKey()
	{
		if(lastTime == Long.MIN_VALUE)
			lastTime = System.currentTimeMillis();
		
		lastTime++;
		return lastTime;
	}
	
	/**
	 * Safely shuts down the database connections, shows the user what is going 
	 * on, logs the error, and shuts down the program.
	 * 
	 * @param ex - The critical error to report.
	 */
	public static final void handleCriticalError(DatabaseIntegrityException ex)
	{
		handleCriticalError(ex, true);
	}
	
	public static final void handleCriticalError(DatabaseIntegrityException ex, boolean shutdownProgram)
	{
		// Shutdown the database

		if(m_mech != null)
			m_mech.shutdown();
		
		// Log the error
		Logger l = Logger.getLogger("CRITICAL ERROR");
		l.error("CRITICAL ERROR", ex);
		
		// Close all JFrames
		for(Frame f : Frame.getFrames())
			f.dispose();
		
		Dialogs.showErrorDialog("CRITICAL ERROR", "A critical error has been detected with the database.\nThe program will attempt to recover upon next startup.\nThe program will now close.:\n"+ex.getMessage());
		
		// Close the program.
		if(shutdownProgram)
			System.exit(2);
	}
}
