package bmod;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;

import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.database.objects.BuildingActivity;
import bmod.database.objects.BuildingFunction;
import bmod.database.objects.BuildingLoad;
import bmod.database.objects.DeviceType;
import bmod.database.objects.Room;
import bmod.util.DateTime;
import bmod.util.SimpleCache;

public class WattageEvent
{
	private static final SimpleCache<String,String> m_stringCache = new SimpleCache<String,String>();
	private static final String ARR_TO_STRING_SEPERATOR = "|";
	private static final String FUNCTION_STRING = "Function";
	private static final String EMPTY_STRING = "";
	private static final String ROOM_ATTR_KEY = "room";
	private static final String TIME_ATTR_KEY = "time";
	private static final String ACTIVITY_ATTR_KEY = "activity";
	private static final String DEVICE_TYPE_ATTR_KEY = "device";
	private static final String DUTY_CYCLE_ATTR_KEY = "dutycycle";
	private static final String WATTAGE_ATTR_KEY = "watts";
	private static final String PERCENT_ON_ATTR_KEY = "pct_on";
	private static final String MULTIPLY_BY_POP_KEY = "pop_mult";
	private static final String POPULATION_ATTR_KEY = "pop";
	private static final String ZONES_ATTR_KEY = "zones";
	private static final String ACTIVITY_TYPE_ATTR_KEY = "act_type";
	private static final String CATEGORIES_ATTR_KEY = "categories";
	
	private final long m_room;
	private DateTime m_startTime;
	private final String m_buildingActivity;
	private final String m_deviceType;
	private final double m_dutyCycle;
	private final double m_wattage;
	private final double m_percentTimeSpanOn;
	private final boolean m_multiplyByPop;
	private final int m_population;
	private final String[] m_zones;
	private final String m_activityType;
	private final String[] m_categories;
	
	private static final String[] arrToStringArr(Object[] objects)
	{
		String[] output = new String[objects.length];
		
		for(int i = 0; i < objects.length; i++)
			output[i] = objects[i].toString();
		
		return output;
	}
	
	private static final String arrToString(String[] objects)
	{
		StringBuilder output = new StringBuilder();
		
		for(String tmp : objects)
		{
			output.append(tmp);
			output.append(ARR_TO_STRING_SEPERATOR);
		}
		
		return output.toString();
	}
	
	private static final String[] stringToArr(String objects)
	{
		return objects.split(ARR_TO_STRING_SEPERATOR);
	}
	
	public WattageEvent(DateTime startTime, 
			double numWatts, 
			BuildingActivity ba, 
			BuildingLoad bl, 
			DeviceType dt)
	{
		m_startTime = startTime;
		m_wattage = numWatts;
		
		m_room = bl.getRoom();
		m_percentTimeSpanOn = bl.getPercentUsage(ba.getActivityType());
		m_multiplyByPop = bl.isPopulationMultiplied();
		m_zones = internalizeArrString(arrToStringArr(bl.getZones()));
		
		m_buildingActivity = internalizeString(ba.getActivityName());
		m_population = ba.getPopulation();
		m_activityType = internalizeString(ba.getActivityType());
		
		m_deviceType = internalizeString(dt.getId());
		m_dutyCycle = dt.getDutyCycle();
		m_categories = internalizeArrString(arrToStringArr(dt.getCategories()));
	}
	
	public WattageEvent(DateTime startTime, double numWatts,
			BuildingActivity ba, String[] zones, String[] categories)
	{
		m_startTime = startTime;
		m_wattage = numWatts;
		
		m_room = ba.getRoomPrimaryKey();
		m_percentTimeSpanOn = 1;
		m_multiplyByPop = false;
		m_zones = internalizeArrString(zones);
		
		m_buildingActivity = internalizeString(ba.getActivityName());
		m_population = ba.getPopulation();
		m_activityType = internalizeString(ba.getActivityType());
		
		m_deviceType = FUNCTION_STRING;
		m_dutyCycle = 1;
		m_categories = internalizeArrString(categories);
	}
	
	public WattageEvent(DateTime startTime, 
			double numWatts, BuildingFunction<?> bf,
			 String[] zones, String[] categories)
	{
		m_startTime = startTime;
		m_wattage = numWatts;
		
		m_room = 0;
		m_percentTimeSpanOn = 1;
		m_multiplyByPop = false;
		m_zones = internalizeArrString(zones);
		
		m_buildingActivity = internalizeString(bf.toString());
		m_population = 0;
		m_activityType = EMPTY_STRING;
		
		m_deviceType = FUNCTION_STRING;
		m_dutyCycle = 1;
		m_categories = internalizeArrString(categories);
	}
	
	
	public WattageEvent(long room, 
			DateTime start, 
			String ba, 
			String dt, 
			double dc,
			double watts,
			double pct_on,
			boolean pop_mult,
			int pop,
			String[] zones,
			String actType,
			String[] categories)
	{
		m_room = room;
		m_startTime = start;
		m_buildingActivity = ba;
		m_deviceType = dt;
		m_dutyCycle = dc;
		m_wattage = watts;
		m_percentTimeSpanOn = pct_on;
		m_multiplyByPop = pop_mult;
		m_population = pop;
		m_zones = zones;
		m_activityType = actType;
		m_categories = categories;
	}
	
	public WattageEvent()
	{
		this(0,new DateTime(),"","",0.0,0.0,0.0,false,0,new String[]{}, "", new String[]{});
	}
	
	
	private String internalizeString(String str)
	{
		String tmp = m_stringCache.get(str);
		
		if(tmp != null)
			return tmp;
		
		m_stringCache.put(str, str);
		return str;
	}
	
	private String[] internalizeArrString(String[] strs)
	{
		for(int i = 0; i < strs.length; i++)
		{
			strs[i] = internalizeString(strs[i]);
		}
		
		return strs;
	}

	public String getStrZones()
	{
		return arrToString(m_zones);
	}

	public String getRowCSV()
	{
		Room rm;
		try
		{
			rm = Database.templateRoom.readPrimaryKey(m_room);
			return m_startTime + "," + 
			Database.templateBuilding.readPrimaryKey(rm.getBuildingID()) + "," +
			rm.getRoomName() + "," + 
			m_buildingActivity + "," + 
			m_deviceType + "," +
			m_percentTimeSpanOn + "," +
			m_multiplyByPop + "," +
			m_population + "," +
			m_dutyCycle + "," +
			m_wattage + "," + 
			getStrZones();
		} catch (DatabaseIntegrityException e)
		{
			return m_startTime + "," + 
					"" + "," +
					"" + "," + 
					m_buildingActivity + "," + 
					m_deviceType + "," +
					m_percentTimeSpanOn + "," +
					m_multiplyByPop + "," +
					m_population + "," +
					m_dutyCycle + "," +
					m_wattage + "," + 
					getStrZones();
		}
		
	}
	
	
	public static String getRowHeaderCSV()
	{
		return "Start Time,Building,Room,Activity,Device Type,Percent TimeSpan On,Population Multiply,Population,Duty Cycle,Wattage,Zones";
	}


	public long getRoom()
	{
		return m_room;
	}



	public DateTime getStartTime()
	{
		return m_startTime;
	}
	
	public void setStartTime(DateTime time)
	{
		m_startTime = time;
	}



	public String getBuildingActivity()
	{
		return m_buildingActivity;
	}



	public double getPercentOn()
	{
		return m_percentTimeSpanOn;
	}



	public String getDeviceType()
	{
		return m_deviceType;
	}



	public double getDutyCycle()
	{
		return m_dutyCycle;
	}



	public double getWattage()
	{
		return m_wattage;
	}
	
	public String[] getZones()
	{
		return m_zones;
	}
	
	public String[] getCategories()
	{
		return m_categories;
	}
	
	public String getActivityType()
	{
		return m_activityType;
	}
	
	@Override
	public String toString()
	{
		return getRowCSV();
	}

	public int getPopulation()
	{
		return m_population;
	}
	
	public boolean equals(WattageEvent other)
	{
		return toString().equals(other.toString());
	}
	
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}
	
	
	public Node setupNode(Element element)
	{
		element.setAttribute(ROOM_ATTR_KEY, ""+m_room);
		element.setAttribute(TIME_ATTR_KEY, m_startTime.toISODate());
		element.setAttribute(ACTIVITY_ATTR_KEY, m_buildingActivity);
		element.setAttribute(DEVICE_TYPE_ATTR_KEY, m_deviceType);
		element.setAttribute(DUTY_CYCLE_ATTR_KEY, m_dutyCycle + "");
		element.setAttribute(WATTAGE_ATTR_KEY, m_wattage + "");
		element.setAttribute(PERCENT_ON_ATTR_KEY, m_percentTimeSpanOn + "");
		element.setAttribute(MULTIPLY_BY_POP_KEY, m_multiplyByPop + "");
		element.setAttribute(POPULATION_ATTR_KEY, m_population + "");
		element.setAttribute(ZONES_ATTR_KEY, arrToString(m_zones));
		element.setAttribute(ACTIVITY_TYPE_ATTR_KEY, m_activityType);
		element.setAttribute(CATEGORIES_ATTR_KEY, arrToString(m_categories));
		
		return element;
	}

	public static WattageEvent deserialize(Attributes attributes)
	{
		try
		{
			return new WattageEvent(
					Long.parseLong(			attributes.getValue(ROOM_ATTR_KEY)),
					new DateTime(			attributes.getValue(TIME_ATTR_KEY)),
											attributes.getValue(ACTIVITY_ATTR_KEY),
											attributes.getValue(DEVICE_TYPE_ATTR_KEY),
					Double.parseDouble(		attributes.getValue(DUTY_CYCLE_ATTR_KEY)),
					Double.parseDouble(		attributes.getValue(WATTAGE_ATTR_KEY)),
					Double.parseDouble(		attributes.getValue(PERCENT_ON_ATTR_KEY)),
					Boolean.parseBoolean(	attributes.getValue(MULTIPLY_BY_POP_KEY)),
					Integer.parseInt(		attributes.getValue(POPULATION_ATTR_KEY)),
					stringToArr(			attributes.getValue(ZONES_ATTR_KEY)),
											attributes.getValue(ACTIVITY_TYPE_ATTR_KEY),
					stringToArr(			attributes.getValue(CATEGORIES_ATTR_KEY)));
		} catch(Exception ex)
		{
			return null;
		}
	}

}
