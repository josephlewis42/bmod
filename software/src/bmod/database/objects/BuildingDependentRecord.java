package bmod.database.objects;

import java.util.Collection;

import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;

/**
 * A common class for DatabaseObjects that can be selected by a Building's ID
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 * @param <T> The extended class.
 */
public abstract class BuildingDependentRecord<T extends BuildingDependentRecord<T>> extends SimpleRecord<T>
{
	private int buildingLoc = -1;
	
	public BuildingDependentRecord(String tableName, String[] colNames, String[] colTypes, String[] indexes, Record<?>[] references, long pKey)
	{
		super(tableName, colNames, colTypes, indexes, references, pKey);
		
		buildingLoc = -1;
		
		for(int i = 0; i < colNames.length; i++)
		{
			if(colNames[i].equals("BuildingID") && getSQLColTypes()[i].equals("BIGINT"))
			{
				buildingLoc = i;
			}
		}
		
		if(buildingLoc == -1)
		{
			throw new IllegalArgumentException("BuildingDepCSVRecords must have a BuildingID field with a BIGINT type!");
		}
	}


	public abstract long getBuildingID();
	
	/**
	 * Returns the building represented by the BuildingID
	 * @return
	 * @throws DatabaseIntegrityException - if the building wasn't found.
	 */
	protected Building getBuilding() throws DatabaseIntegrityException
	{
		return Database.templateBuilding.readPrimaryKey(getBuildingID());
	}
	
	public Collection<T> readBuildingDep(long buildingID)
	{
		return readWhere().eq("BuildingID", buildingID).all();
	}
	
	protected boolean doesBuildingExist()
	{
		try
		{
			Database.templateBuilding.readPrimaryKey(getBuildingID());
			return true;
		} catch (DatabaseIntegrityException e)
		{
			return false;
		}
	}
}
