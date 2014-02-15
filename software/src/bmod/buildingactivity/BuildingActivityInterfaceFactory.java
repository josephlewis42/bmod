package bmod.buildingactivity;

import java.util.HashMap;

import bmod.database.CRUDCache;

public class BuildingActivityInterfaceFactory
{
	
	private static final CRUDCache<BuildingActivityInterface> cachedInterfaces = new CRUDCache<BuildingActivityInterface>();
	/**
	 * Constructs a new BuildingActivityInterface for the type with the given 
	 * id with the default options "options".
	 * 
	 * @throws Throws an IllegalArgumentException if the id is invalid.
	 * @param id - the id of the interface to get.
	 * @param options - the options parameter for the interface to use.
	 * @return
	 */
	public static BuildingActivityInterface getInstance(int id, String options) throws IllegalArgumentException
	{
		BuildingActivityInterface ret = cachedInterfaces.get(id, options);
		
		if(null != ret)
			return ret;
		
		ret = forceGetInstance(id, options);
		
		cachedInterfaces.add(ret, id, options);
		
		return ret;
	}
	
	private static final int REPEATING_EVENT_ID = new RepeatingEvent("").getInterfaceId();
	private static final int DAY_OF_WEEK_AND_TIME_ID = new DayOfWeekAndTimeActivity("").getInterfaceId();
	private static final int SINGLE_TIME_ID = new SingleTimeEvent("").getInterfaceId();
	private static final int FUNCTION_ID = new Function("").getInterfaceId();
	private static final int ALWAYS_ID = new Always("").getInterfaceId();
	
	
	public static BuildingActivityInterface forceGetInstance(int id, String options) throws IllegalArgumentException
	{
		if(REPEATING_EVENT_ID == id)
			return new RepeatingEvent(options);
		
		if(DAY_OF_WEEK_AND_TIME_ID == id)
			return new DayOfWeekAndTimeActivity(options);
		
		// Special case for SingleTimeEvents id=0
		if(SINGLE_TIME_ID == id || id == 0)
			return new SingleTimeEvent(options);
		
		if(FUNCTION_ID == id)
			return new Function(options);
		
		if(ALWAYS_ID == id)
			return new Always(options);
		
		throw new IllegalArgumentException("No interface could be found with the given id: "+id);
	}
	
	/**
	 * Returns a list of the <human readable string, id> of all the available
	 * ActivityInterfaces.
	 */
	public static HashMap<String,Integer> getNames()
	{
		HashMap<String,Integer> namesMap = new HashMap<String,Integer>();
		
		BuildingActivityInterface[] bai = new BuildingActivityInterface[]{
												new RepeatingEvent(""),
												new DayOfWeekAndTimeActivity(""),
												new SingleTimeEvent(""),
												new Function(""),
												new Always("")
												};
		
		for(BuildingActivityInterface b : bai)
		{
			namesMap.put(b.getHumanReadableName(), b.getInterfaceId());
		}
		return namesMap;
	}
}
