package bmod.plugin.generic.headless;

import java.util.Collection;

import bmod.GenericPlugin;
import bmod.database.DataFeed;
import bmod.database.DataNotAvailableException;
import bmod.database.Database;
import bmod.database.objects.BuildingActivity;
import bmod.database.objects.Room;
import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;

/**
 * Returns the population of the room at the given time.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class RoomPopulation extends GenericPlugin implements DataFeed
{
	private Room m_room;
	
	public RoomPopulation()
	{
		super("Room Population Data Feed", 
				"Provides a data feed showing the predicted population of a room.");
	}
	
	public RoomPopulation(Room room)
	{
		this();
		m_room = room;
	}

	@Override
	public double getDataAtTime(DateTime t) throws DataNotAvailableException
	{
		double total = 0.0;
		
		for(BuildingActivity ba : BuildingActivity.readActivities(t, t, m_room.getPrimaryKey()))
			total += ba.getPopulation();
		
		return total;
	}


	@Override
	public void preCache(DateTimeRange precache)
			throws DataNotAvailableException
	{
		// do nothing for now.
	}

	@Override
	public String getFeedName()
	{
		return "Room Population of: " + m_room;
	}

	@Override
	public void appendAllPossibleDataFeeds(Collection<DataFeed> feeds)
	{		
		for(Room roomName : Database.templateRoom.readAll())
			feeds.add(new RoomPopulation(roomName));
	}
	
	@Override
	public String toString()
	{
		return getFeedName();
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
