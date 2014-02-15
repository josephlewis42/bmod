package bmod.database.objects;

import java.util.Arrays;
import java.util.Collection;

import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.gui.builder.CSVRecordWidget;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.LongWidget;
import bmod.gui.builder.TextFieldWidget;

/**
 * Represents a location on campus, with a building name and room name, this is a
 * multiton so there is only one instance of Room per location.
 * 
 * @author Joseph Lewis &lt;joehms22@gmail.com&gt;
 */
public class Room extends BuildingDependentRecord<Room>
{
	private static final String TABLE_NAME = "Rooms";
	private static final String[] COLUMN_NAMES = new String[]{"PrimaryKey","RoomName","BuildingID"};
	private static final String[] COLUMN_TYPES = new String[]{"BIGINT","VARCHAR(100)","BIGINT"};
	private static final String[] COLUMN_INDEXES = new String[]{"RoomName", "BuildingID"};
	private static final Record<?>[] COLUMN_REFERENCES = new Record<?>[]{null,null,Database.templateBuilding};
	
	private final String m_room;
	private final long m_buildingID;
	
	/**
	 * @return The human readable name of this room i.e. "125"
	 */
	public String getRoomName()
	{
		return m_room;
	}
	
	
	public Room(long bldgId, String roomName)
	{
		this(bldgId, roomName, Database.getNewPrimaryKey());
	}
	
	
	public Room(long bldgId, String roomName, long pkey)
	{
		super(TABLE_NAME,
				COLUMN_NAMES,
				COLUMN_TYPES, 
				COLUMN_INDEXES,
				COLUMN_REFERENCES,
				pkey);
		
		m_buildingID = bldgId;
		m_room = roomName;
	}
	
	
	public Room()
	{
		this(Database.ALL_BUILDINGS_ID, "", -1);
	}
	


	/**
	 * Creates a "pretty" string representing this room, suitable for showing
	 * users; formatted &lt;building_name&gt; &lt;room_name&gt; i.e. "Olin 125"
	 * @return The pretty string.
	 */
	@Override
	public String getId()
	{
		return Building.getNameByPkey(m_buildingID) + " " + m_room;
	}

	@Override
	public Object[] toSQL()
	{
		return new Object[]{getPrimaryKey(), m_room, m_buildingID};
	}

	
	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
		// If building does not exist, delete it.
		if(! doesBuildingExist())
		{
			delete();
			return;
		}
				
		if(Database.templateBuildingLoad.readRoomDep(getPrimaryKey()).size() == 0)
			list.addWarning("The room: " + this + " has no loads.");
	}

	@Override
	public Room fromSQL(Object[] parts) throws IllegalArgumentException
	{
		if(parts == null || parts.length > getColNames().length)
			throw new IllegalArgumentException("Parts doesn't work!" + Arrays.toString(parts));
		
		try
		{			
			return new Room((Long) parts[2], (String) parts[1], (Long) parts[0]);
		} catch(Exception e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public long getBuildingID()
	{
		return m_buildingID;
	}


	@Override
	protected Room getThis()
	{
		return this;
	}

	
	/**
	 * Gets the room with the given name, or if it doesn't exist, creates it.
	 * 
	 * @param bldgId - The building ID to create the room for.
	 * @param roomName - The name of the room to search for in the given building.
	 * @return A Room, found or minted.
	 * @throws DatabaseIntegrityException 
	 */
	public Room getOrCreateRoom(long bldgId, String roomName) throws DatabaseIntegrityException
	{
		for(Room r : readBuildingDep(bldgId))
			if(r.getRoomName().equals(roomName))
				return r;
		
		Room r = new Room(bldgId, roomName);
		r.create();
		return r;
	}
	
	/**
	 * Gets the name of the room + building if possible in a human readable 
	 * format: "Building Room" if building can't be found, simply returns "Room"
	 * and if room doesn't have a name, returns the primary key.
	 * 
	 * @return
	 */
	public static String getHumanReadableName(long roomId)
	{
		String roomname = "" + roomId;
		
		try {
			Room r = Database.templateRoom.readPrimaryKey(roomId);
			roomname = r.getId();
			
		} catch(DatabaseIntegrityException ex){}
		
		return roomname;
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		return new GUIBuilderPanel(toSQL(),
					new LongWidget("Primary Key", getPrimaryKey(), false),
					new TextFieldWidget("Name", m_room, true),
					new CSVRecordWidget("Building", m_buildingID, new Building(), false)
				);
	}
	
	/**
	 * Gets a room name by the given ID, or returns the ID if not found.
	 * 
	 * @param primaryKey - The key of the building to fetch the name for.
	 * @return The name of the building, or the ID if no building with the given
	 * key exists
	 */
	public static String getNameByPkey(long primaryKey)
	{
		try
		{
			return Database.templateRoom.readPrimaryKey(primaryKey).getRoomName();
		} catch (DatabaseIntegrityException e)
		{
			return "ID#" + primaryKey;
		}
	}
	
	@Override
	public String getUserEditableClass()
	{
		return this.getClass().getCanonicalName();
	}
	
	@Override
	public Room createNew(Collection<Record<?>> filterObjects)
	{
		// Make sure we have enough objects
		if (filterObjects.size() != 1)
		{
			throw new IllegalArgumentException("Cannot instantiate instance without proper arguments.");
		}
		
		// Declare our objects
		Building templateBuilding = null;
		
		// Instantiate all of our declared objects
		for (Record<?> tmp : filterObjects)
		{
			if (tmp instanceof Building)
			{
				templateBuilding = (Building) tmp;
			}
		}
		
		// Make sure we got all of our objects.
		if (templateBuilding == null)
		{
			throw new IllegalArgumentException("Cannot instantiate instance without proper arguments.");
		}
		
		// Return the new item.
		return new Room(templateBuilding.getPrimaryKey(), "New Room");
	}
}