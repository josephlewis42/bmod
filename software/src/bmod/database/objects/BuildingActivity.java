package bmod.database.objects;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import bmod.buildingactivity.BuildingActivityInterface;
import bmod.buildingactivity.BuildingActivityInterfaceFactory;
import bmod.buildingactivity.Function;
import bmod.database.CRUDCache;
import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.IntWidget;
import bmod.gui.builder.LongWidget;
import bmod.gui.builder.TextFieldWidget;
import bmod.gui.widgets.ActivityInterfaceEditor;
import bmod.util.DateTime;

public class BuildingActivity extends RoomDepRecord<BuildingActivity>
{
	private static final Logger m_logger = Logger.getLogger("bmod.BuildingActivity");
	private static final String TABLE_NAME = "building_activities";
	private static final String[] COLUMN_NAMES = new String[]{"RoomID","Activity Name","ActivityType","Population","BuildingID","Zone","ActivityInterfaceId","ActivityInterfaceProperties","PrimaryKey"};
	private static final String[] COLUMN_TYPES = new String[]{"BIGINT","VARCHAR(100)","VARCHAR(100)","INT","BIGINT","VARCHAR(100)","INT","VARCHAR(1000)","BIGINT"};
	private static final String[] COLUMN_INDEXES = new String[]{"RoomID", "ActivityInterfaceId"};
	private static final Record<?>[] COLUMN_REFERENCES = new Record<?>[]{Database.templateRoom,null,null,null,Database.templateBuilding,null,null,null,null};
	private final String m_activityName;
	private final String m_activityType;
	private final int m_population;
	private final long m_buildingPKey;
	private final long m_roomPKey;
	private final BuildingActivityInterface m_buildingActivityInterface;
	

	public BuildingActivity(long roomName, String activityName, String activityType, int population, long buildingName, String zoneName, int interfaceId, String activityInterfaceProperties, long pkey)
	{
		super(TABLE_NAME,
				COLUMN_NAMES,
				COLUMN_TYPES,
				COLUMN_INDEXES,
				COLUMN_REFERENCES,
				pkey);
		
		if(activityName == null || activityType == null || zoneName == null){
			throw new IllegalArgumentException();
		}
		m_buildingPKey = buildingName;
		m_roomPKey = roomName;
		m_activityName = activityName;
		m_activityType = activityType;
		m_population = population;
		
		BuildingActivityInterface tmp = null;
		try
		{
			tmp = BuildingActivityInterfaceFactory.getInstance(interfaceId, activityInterfaceProperties);
		} catch(IllegalArgumentException ex)
		{
			m_logger.error("The interface "+interfaceId+" wasn't found");
		}
		m_buildingActivityInterface = tmp;
	}
	
	/**
	 * Generates an activity that happens for a given room with a given activityName and Type
	 * 
	 * @param loc - The room to generate for
	 * @param activityNameType - A string to use as both activity name and activityType
	 */
	public BuildingActivity(Room loc, String activityNameType)
	{
		this(
				loc.getPrimaryKey(),
				activityNameType,
				activityNameType,
				0,
				loc.getBuildingID(),
				"Default",
				0,
				"",
				Database.getNewPrimaryKey());
	}
	
	/**
	 * Generates an activity that "Always" happens for a given room.
	 * 
	 * @param loc - The room to generate for.
	 */
	public BuildingActivity(Room loc)
	{
		this(loc, "Always");
	}
	
	/**
	 * Just used for fetching Record information.
	 */
	public BuildingActivity() {
		//this(-1, "", "", 0, -1, "", -1, "", Database.getNewPrimaryKey());
		super(TABLE_NAME,
				COLUMN_NAMES,
				COLUMN_TYPES,
				COLUMN_INDEXES,
				COLUMN_REFERENCES,
				Database.TEMPLATE_PRIMARY_KEY);
		
		m_activityName = "";
		m_activityType = "";
		m_population = 0;
		m_buildingPKey = -1;
		m_roomPKey = -1;
		
		m_buildingActivityInterface = null;		
	}

	@Override
	public String getId() { 
	
		if(m_buildingActivityInterface == null)
			return Room.getHumanReadableName(m_roomPKey) + " " + m_activityName + " (" + m_activityType + ") Pop: " + m_population + "ID: " + new DateTime().getTime();
		else
			return Room.getHumanReadableName(m_roomPKey) + " " + m_activityName + " (" + m_activityType + ") Pop: " + m_population + "ID: " + m_buildingActivityInterface.getHumanString();
	}
	public String getActivityName() { return m_activityName; }
	public String getActivityType() { return m_activityType; }
	public int getPopulation() { return m_population; }
	public BuildingActivityInterface getActivityInterface() { return m_buildingActivityInterface;}
	public long getRoomPrimaryKey() { return m_roomPKey;};
	
	
	/**
	 * Returns true if this event occurs within the time interval given
	 * false otherwise.
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public boolean happensWithin(DateTime start, DateTime end)
	{
		return m_buildingActivityInterface.happensWithin(start, end);
	}

	@Override
	public Object[] toSQL() {
		return new Object[]{
				m_roomPKey, 
				m_activityName,
				m_activityType,
				m_population,
				m_buildingPKey,
				"", // Zone names are no longer used in here, TODO delete them.
				m_buildingActivityInterface.getInterfaceId() + "",
				m_buildingActivityInterface.getPropertiesString(),
				getPrimaryKey()
				};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
		
		// If building does not exist, delete it.
		if(! doesBuildingExist() || ! doesRoomExist()) {
			delete();
			return;
		}
		
		
		// Check that this activity isn't similar to another one.
		for(BuildingActivity a : where().eq("RoomID", m_roomPKey).eq("ActivityType", m_activityType).all())
		{
			if(a.toString().equals(toString()) && a.getPrimaryKey() != getPrimaryKey())
			{
				list.addError("The activity: " + this.getPrimaryKey() + "was very similar to: " + a.getPrimaryKey() + " deleting");
				delete();
				return;
			}
			else 
			{
				if(a.getActivityInterface().equals(getActivityInterface()) && !a.getId().equals(getId()))
					list.addWarning("The activity \"" + getActivityName() + "\" is similar to the activity: \""+a.getActivityName()+"\" their loads may be consolidated upon running.");
			}
		}
			
		if(getPopulation() == 0)
			list.addWarning("The Activity " + getId() + " has a population of 0");	
	}

	
	/**
	 * True if the thing that determines the times for this is a function.
	 */
	public boolean isFunction()
	{
		return getActivityInterface() instanceof Function;
	}
	
	public boolean isSimilar(BuildingActivity other)
	{
		return other.m_buildingPKey == m_buildingPKey &&
				other.m_roomPKey == m_roomPKey &&
				other.m_activityType.equals(m_activityType) && 
				other.getActivityInterface().equals(getActivityInterface()) && 
				!other.getId().equals(getId());
	}

	public double getPercentageTimeFilled(DateTime start, DateTime end)
	{
		return m_buildingActivityInterface.getPercentageTimeFilled(start, end);
	}

	@Override
	public BuildingActivity fromSQL(Object[] parts)
			throws IllegalArgumentException
	{
		return new BuildingActivity((Long) parts[0], 
									(String) parts[1], 
									(String) parts[2], 
									(Integer)parts[3], 
									(Long) parts[4], 
									(String) parts[5], 
									(Integer) parts[6], 
									(String)parts[7], 
									(Long) parts[8]);
	}

	@Override
	public long getBuildingID()
	{
		return m_buildingPKey;
	}

	@Override
	protected BuildingActivity getThis()
	{
		return this;
	}
	
	
	/**
	 * Returns a list of all the strings for the ActivityType field that the
	 * user has entered.
	 * 
	 * @return An array of all the ActivityTypes
	 */
	public static Collection<String> readAllTypes()
	{
		HashSet<String> allActivities = new HashSet<String>();
		for(BuildingActivity tmp : Database.templateBuildingActivity.readAll())
			allActivities.add(tmp.getActivityType());

		return allActivities;
	}
	
	

	@Override
	public GUIBuilderPanel getEditor()
	{
		ActivityInterfaceEditor m_editor = new ActivityInterfaceEditor();
		m_editor.init(m_buildingActivityInterface.getInterfaceId(), 
				m_buildingActivityInterface.getPropertiesString());
		
		return new GUIBuilderPanel(toSQL(),
				new LongWidget("Room Id", m_roomPKey, false),
				new TextFieldWidget("Activity Name", m_activityName),
				new TextFieldWidget("Activity Type", m_activityType),
				new IntWidget("Population", m_population),
				null, // Don't show building chooser
				null, // Don't show the zone widget, as it no longer is needed.
				m_editor.getInterfaceIdWidget(),
				m_editor.getEditorWidget(),
				null
				);
	}

	public boolean isAlways()
	{
		return getActivityName().equals("Always");
	}
	
	/**
	 * Reads the functions in the given room.
	 * 
	 * @param roomPrimaryKey - the pkey of the room to return the functions for.
	 * @return The functions in that room.
	 */
	public Collection<BuildingActivity> readFunctions(long roomID)
	{
		return readWhere()
				.eq("RoomID", roomID)
				.eq("ActivityInterfaceId", new Function("").getInterfaceId())
				.all();
	}

	/**
	 * Reads all activities that are not functions.
	 * 
	 * @param roomID
	 * @return
	 */
	public Collection<BuildingActivity> readNonFunctions(long roomID)
	{
		return readWhere()
				.eq("RoomID", roomID)
				.neq("ActivityInterfaceId", new Function("").getInterfaceId())
				.all();
	}
	
	public Collection<BuildingActivity> readNonFunctions(long roomID, DateTime start, DateTime end)
	{
		LinkedList<BuildingActivity> bas = new LinkedList<BuildingActivity>();
		
		for(BuildingActivity ba : readNonFunctions(roomID))
			if(ba.happensWithin(start, end))
				bas.add(ba);
		
		return bas;
	}
	
	public boolean hasNonFunctions(long roomID, DateTime start, DateTime end)
	{
		try
		{
			readWhere().eq("RoomID", roomID)
			.neq("ActivityInterfaceId", new Function("").getInterfaceId())
			.one();
		} catch(DatabaseIntegrityException ex)
		{
			return false;
		}
		
		return true;
	}
	
	private final static CRUDCache<Collection<BuildingActivity>> roomActivityCache = new CRUDCache<Collection<BuildingActivity>>();
	
	/**
	 * Reads all activities and functions in a room that occur within the two times.
	 * 
	 * @param start - the time to start looking for activities.
	 * @param end - the time to end looking for activities.
	 * @param rmPkey - the primary key of the room to look in.
	 * @return
	 */
	public static Collection<BuildingActivity> readActivities(DateTime start, DateTime end, long rmPkey) {
		
		Collection<BuildingActivity> activitiesInRoom = roomActivityCache.get(rmPkey);
		if(null == activitiesInRoom)
		{
			activitiesInRoom = Database.templateBuildingActivity.readWhere().eq("RoomID", rmPkey).all();
			roomActivityCache.add(activitiesInRoom, rmPkey);
		}
		
		LinkedList<BuildingActivity> bal = new LinkedList<BuildingActivity>();
		for(BuildingActivity tmp : activitiesInRoom)
		{
			if(tmp.happensWithin(start, end))
			{
				bal.add(tmp);
			}
		}
			
		return bal;
	}

	@Override
	public long getRoomID()
	{
		return m_roomPKey;
	}
	
	@Override
	public String getUserEditableClass()
	{
		return this.getClass().getCanonicalName();
	}

	@Override
	public BuildingActivity createNew(Collection<Record<?>> filterObjects)
	{
		
		// Declare our objects
		Room templateRoom = null;
		
		// Instantiate all of our declared objects
		for (Record<?> tmp : filterObjects)
		{
			if (tmp instanceof Room)
			{
				templateRoom = (Room) tmp;
			}
		}
		
		// Make sure we got all of our objects.
		if (templateRoom == null )
		{
			throw new IllegalArgumentException("Cannot instantiate instance without proper arguments.");
		}
		
		// Return the new item.
		return new BuildingActivity(templateRoom);
	}
}
