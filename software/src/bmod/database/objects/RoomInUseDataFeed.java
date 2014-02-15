package bmod.database.objects;

import java.util.Collection;

import bmod.database.DBWarningsList;
import bmod.database.DataNotAvailableException;
import bmod.database.Database;
import bmod.database.EditableDataFeed;
import bmod.gui.builder.CSVRecordWidget;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.LongWidget;
import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;

/**
 * A DataFeed that represents whether or not a room is on at a given time period
 * outputs a simple boolean value.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class RoomInUseDataFeed extends EditableDataFeed<RoomInUseDataFeed>
{
	private final long m_roomPrimaryKey;
	
	public RoomInUseDataFeed()
	{
		this(-1);
	}
	
	public RoomInUseDataFeed(long roomPKey)
	{
		this(Database.getNewPrimaryKey(), roomPKey);
	}
	
	public RoomInUseDataFeed(long pKey, long roomPKey)
	{
		super("RoomInUseDataFeed", 
				new String[]{"PrimaryKey", "RoomPrimaryKey"}, 
				new String[]{"BIGINT", "BIGINT"}, 
				new Record<?>[]{null,Database.templateRoom},
				pKey,
				"RoomInUseDataFeed");
		
		m_roomPrimaryKey = roomPKey;
	}
	

	@Override
	protected RoomInUseDataFeed getThis()
	{
		return this;
	}

	@Override
	public String getId()
	{
		return "Room In Use: " + Room.getHumanReadableName(m_roomPrimaryKey);
	}

	@Override
	public Object[] toSQL()
	{
		return new Object[]{getPrimaryKey(), m_roomPrimaryKey};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
	}

	@Override
	public RoomInUseDataFeed fromSQL(Object[] parts) throws IllegalArgumentException
	{
		return new RoomInUseDataFeed((Long) parts[0], (Long) parts[1]);
	}
	
	@Override
	public double getDataAtTime(DateTime t) throws DataNotAvailableException
	{
		// Check to see if the room is in use.
		if(BuildingActivity.readActivities(t, t, m_roomPrimaryKey).size() > 0)
			return 1.0;
		
		return 0.0;
	}

	@Override
	public void preCache(DateTimeRange precache)
	{
		// Don't worry about this for now, if things become super slow, we can 
		// add it in.
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		return new GUIBuilderPanel(
					toSQL(), 
					new LongWidget("Primary Key", getPrimaryKey(), false),
					new CSVRecordWidget("Room",m_roomPrimaryKey, new Room()));
	}

	@Override
	public RoomInUseDataFeed getNew()
	{
		return new RoomInUseDataFeed();
	}
	
	@Override
	public RoomInUseDataFeed createNew(Collection<Record<?>> filterObjects)
	{
		// Make sure we have enough objects
		if (filterObjects.size() != 1)
		{
			throw new IllegalArgumentException("Cannot instantiate instance without proper arguments.");
		}
		
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
		if (templateRoom == null)
		{
			throw new IllegalArgumentException("Cannot instantiate instance without proper arguments.");
		}
		
		// Return the new item.
		return new RoomInUseDataFeed(templateRoom.getPrimaryKey());
	}
}
