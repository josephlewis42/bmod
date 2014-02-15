package bmod.database.objects;

import java.util.Collection;

import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;


public abstract class RoomDepRecord<T extends RoomDepRecord<T>> extends BuildingDependentRecord<T>
{
	
	private int roomLoc = -1;

	public RoomDepRecord(String tableName, String[] colNames,
			String[] colTypes, String[] strings, Record<?>[] references, long primaryKey)
	{
		super(tableName, colNames, colTypes, strings, references, primaryKey);
		
		roomLoc = -1;
		
		for(int i = 0; i < colNames.length; i++)
		{
			if(colNames[i].equals("RoomID") && getSQLColTypes()[i].equals("BIGINT"))
			{
				roomLoc = i;
			}
		}
		
		if(roomLoc == -1)
		{
			throw new IllegalArgumentException("RoomDepCSVRecords must have a RoomID field with a BIGINT type!");
		}
	}

	public abstract long getRoomID();
	
	/**
	 * Returns the room represented by the Room
	 * @return
	 * @throws DatabaseIntegrityException - if the building wasn't found.
	 */
	protected Room getRoomObject() throws DatabaseIntegrityException
	{
		return Database.templateRoom.readPrimaryKey(getRoomID());
	}
	
	public Collection<T> readRoomDep(long roomID)
	{
		return readWhere().eq("RoomID", roomID).all();
	}
	
	protected boolean doesRoomExist()
	{
		try
		{
			Database.templateRoom.readPrimaryKey(getRoomID());
			return true;
		} catch (DatabaseIntegrityException e)
		{
			return false;
		}
	}
}
