package bmod;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import bmod.buildingactivity.Function;
import bmod.database.DBWarningsList;
import bmod.database.DatabaseIntegrityException;
import bmod.database.objects.BuildingActivity;
import bmod.database.objects.BuildingFunction;
import bmod.database.objects.Room;
import bmod.gui.widgets.ProgressDialog;
import bmod.plugin.baseclass.ActivityPlugin;
import bmod.plugin.loader.BuildingFunctionLoader;
import bmod.plugin.loader.WattageCalculator;
import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;

/**
 * The miner takes raw CRUD statements from a DatabaseQueryMechanism
 * and converts them in to human usable data by performing operations
 * on them. This is the tertiary layer of the Database system.
 *  
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class ForkMiner extends RecursiveAction
{
	private static final DBWarningsList WARNINGS_LIST = new DBWarningsList();
    private static final BuildingFunctionLoader BUILDING_FUNCTION_LOADER = new BuildingFunctionLoader();
	private static final long serialVersionUID = 1L;
    private static final WattageCalculator m_wattageCalculator = new WattageCalculator();
    private final List<WattageEvent> m_events = new LinkedList<>();
    
	private final DateTimeRange m_dtr;
	private final LinkedList<Room> m_rooms;
	private final PredictionModel m_predictionModel;
	private final ProgressDialog m_monitor;
	
	// Miners behave like trees, spawning more as needed if both m_left and
	// m_right are null the miner has events.
	private ForkMiner left = null;
	private ForkMiner right = null;
	private Room m_room = null;
	
	public ForkMiner(PredictionModel pm, ProgressDialog monitor)
	{
		if(pm == null || monitor == null)
			throw new NullPointerException("Prediction model or monitor is null!");
		
		
		m_dtr = pm.getTimeRange();
		m_rooms = new LinkedList<>(pm.getRoomsToGenerateFor());
		m_predictionModel = pm;
		m_monitor = monitor;
		
		WARNINGS_LIST.clear();
		
		// HOOKS
		ExtensionPoints.minerStartHook(WARNINGS_LIST, pm);
		
		int maxthreads = Runtime.getRuntime().availableProcessors();
		maxthreads = ExtensionPoints.maxThreadsHook(maxthreads);
		maxthreads = (maxthreads < 1)? 1 : maxthreads; // Sanity check
		WARNINGS_LIST.addInfo("Used " + maxthreads + " threads to do computation.");
		
		m_monitor.setMaximum(m_rooms.size() + 1);
		m_monitor.setProgress(0);

		// Do the recursion
		ForkJoinPool pool = new ForkJoinPool(maxthreads);
		pool.invoke(this);
		
		m_monitor.setMaximum(m_rooms.size() + 1);
		m_monitor.setProgress(0);
		
		calculateBuildingFunctions();
		
		// CALL HOOKS
		ExtensionPoints.minerEndHook(WARNINGS_LIST, m_predictionModel);
	}
	
	private void calculateBuildingFunctions()
	{
		// Do the functions
		m_monitor.setProgress("Executing building functions", 1);
		
		// Now calculate the building wide functions that need to be run.
		List<BuildingFunction<?>> funs = BUILDING_FUNCTION_LOADER.getFunctionsForBuilding(m_predictionModel.getBuildingId());
		
		m_monitor.setMaximum(funs.size());
		
		Collections.sort(funs, BuildingFunction.getComparator());
		
		for(int i = 0; i < funs.size(); i++)
		{
			m_monitor.setProgress(funs.get(i).toString(), i);
			funs.get(i).addWattageEvents(m_predictionModel, WARNINGS_LIST);
		}
	}
	
	private ForkMiner(DateTimeRange dtr, Collection<Room> rooms, PredictionModel pm, ProgressDialog monitor)
	{
		m_dtr = dtr;
		m_rooms = new LinkedList<>(rooms);
		m_predictionModel = pm;
		m_monitor = monitor;
	}

	@Override
	protected void compute()
	{
		int roomsSize = m_rooms.size();
		if(roomsSize == 0) return; // Make sure we're computing for some rooms.
		
		
		// Then split in to rooms
		if(roomsSize > 1) 
		{
			left = new ForkMiner(m_dtr.getClone(), m_rooms.subList(0, roomsSize / 2), m_predictionModel, m_monitor);
			right = new ForkMiner(m_dtr.getClone(), m_rooms.subList(roomsSize / 2, roomsSize), m_predictionModel, m_monitor);
			invokeAll(left, right);
			
			return;
		}
		else
		{
			m_room = m_rooms.getFirst();
			// We must be here to perform calculations.
			process(m_dtr, m_room);
			
			// Report our events
			m_predictionModel.addEvents(m_events);
			m_events.clear();
		}
	}
	
	/**
	 * Starts the calculation. For every time within the given times
	 * a point of data should be added to "m_data" i.e.
	 * <table>
	 * <tr><td>Time</td><td>kW</td></tr>
	 * <tr><td>2012-01-09 11:22:33</td><td>22</td></tr>
	 * <tr><td>2012-01-09 11:22:35</td><td>25</td></tr>
	 * <tr><td>2012-01-09 11:22:37</td><td>42</td></tr>
	 * </table>
	 */
	public void process(DateTimeRange m_times, Room m_room)
	{
		m_monitor.incrementProgress("Calculating " + m_room, 1);
		
		// CALL HOOKS
		ExtensionPoints.minerThreadStartedHook(WARNINGS_LIST, m_predictionModel, m_room);

		// Loop over each time and generate rows for the csv writer.
		DateTime last = null;
		for(DateTime date : m_times) 
		{
			if(last != null)
			{
				getWatts(m_room, last, date);
			}
			
			last = date;
		}
		
		
		// CALL HOOKS
		ExtensionPoints.minerThreadEndedHook(WARNINGS_LIST, m_predictionModel, m_room);
    	
		System.err.println("Finished calculating: " + m_room);
	}
	
	
	/**
	 * Gets the number of watts used by a room during a certain time period
	 * by calling getWattsForActivity for each activity that happens in
	 * the given time period.
	 * 
	 * Includes support for fetching the "Always" and "Work Day" activities.
	 * 
	 * @param room - the room
	 * @param start - the start time to check
	 * @param end - the end time to check
	 * @param m_events 
	 */
	private void getWatts(Room room, DateTime start, DateTime end)
	{
		LinkedList<BuildingActivity> activities = new LinkedList<BuildingActivity>();
		LinkedList<BuildingActivity> functions = new LinkedList<BuildingActivity>();
		
		DateTime activityStart = ExtensionPoints.minerActivityStartTimeHook(start);
		DateTime activityEnd = ExtensionPoints.minerActivityEndTimeHook(end);
		DateTime functionStart = ExtensionPoints.minerFunctionStartTimeHook(start);
		DateTime functionEnd = ExtensionPoints.minerFunctionEndTimeHook(end);
		
		
		// Set up functions and building activities.
		for(BuildingActivity ba : BuildingActivity.readActivities(activityStart, activityEnd, room.getPrimaryKey()))
		{
			if(ba.getActivityInterface() instanceof Function)
			{
				functions.add(ba);
			}
			else
			{
				activities.add(ba);
			}
		}
		
		// Add in "ALWAYS"
		activities.add(new BuildingActivity(room));
		
		// DO ACTIVITIES
		for(BuildingActivity ba : activities)
		{
			ActivityPlugin p = new ActivityPlugin(); // The default ActivityPlugin
			try
			{
				Collection<WattageEvent> evts = p.getWattsForActivity(m_predictionModel, WARNINGS_LIST, ba, activityStart, activityEnd, room);
				ExtensionPoints.minerActivityWattageEventReturnedHook(evts, ba);
				m_events.addAll(evts);
			}
			catch(DatabaseIntegrityException ex)
			{
				WARNINGS_LIST.addError(ex.getMessage());
			}
		}
		
		
		// DO FUNCTIONS
		for(BuildingActivity ba : functions)
		{
			Function f = (Function) ba.getActivityInterface();
			ActivityPlugin plugin = m_wattageCalculator.getPluginByName(f.getFunctString());
			
			if(plugin == null) 
			{
				WARNINGS_LIST.addError("A function handler couldn't be found for the function: " + ba);
				continue;
			}
			
			try
			{
				Collection<WattageEvent> evts = plugin.getWattsForActivity(m_predictionModel, WARNINGS_LIST, ba, functionStart, functionEnd, room);
				ExtensionPoints.minerFunctionWattageEventReturnedHook(evts, ba);
				m_events.addAll(evts);
			}
			catch(DatabaseIntegrityException ex)
			{
				WARNINGS_LIST.addError(ex.getMessage());
			}
		}
	}
	
	public DBWarningsList getRuntimeErrors()
	{
		return WARNINGS_LIST;
	}
}
