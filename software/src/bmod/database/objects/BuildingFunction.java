package bmod.database.objects;

import java.util.Comparator;

import bmod.PredictionModel;
import bmod.database.DBWarningsList;
import bmod.database.DataNotAvailableException;
import bmod.database.DatabaseIntegrityException;
import bmod.plugin.generic.headless.SmartGridProvider;
import bmod.util.DateTime;


/**
 * Represents a function that can be applied to any given building.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public abstract class BuildingFunction<T extends BuildingFunction<T>> extends BuildingDependentRecord<T>
{
	public int m_rank; 

	public BuildingFunction(String tableName, String[] colNames,
			String[] colTypes, String[] indexes, Record<?>[] refs, long pKey, int rank)
	{
		super(tableName, colNames, colTypes, indexes, refs, pKey);
		
		m_rank = rank;
	}
	
	/**
	 * Returns the order in which this function will be evaluated, higher ranks
	 * mean they will be evaluated later.
	 * 
	 * @return
	 */
	public int getRank()
	{
		return m_rank;
	}
	
	/**
	 * Adds new wattage events to the building function.
	 * 
	 * @param m
	 * @param dw
	 */
	public abstract void addWattageEvents(PredictionModel m, DBWarningsList dw);
	
	
	/**
	 * Gets the feed value at the current time, if the feed couldn't be found,
	 * throws a DatabaseIntegrityException Caches feeds if possible
	 * 
	 * @param feed_id
	 * @throws DatabaseIntegrityException 
	 * @throws DataNotAvailableException 
	 */
	public static double getFeedValue(int feed_id, DateTime dt, PredictionModel m_model) throws DataNotAvailableException
	{
		// Do something fancy, we're going to cache all values over the whole
		// time frame, as these are most likely the ones we'll be getting
		// if the values aren't there, the penalty is a few ms, but much faster
		// than connecting one by one.
		if(m_model != null) {
			SmartGridProvider.cacheClosestFeedValueRange(m_model.getTimeRange(), feed_id);
		}
		
		
		return SmartGridProvider.getFeedValue(feed_id, dt);
	}
	
	public static Comparator<BuildingFunction<?>> getComparator(){
		return new Comparator<BuildingFunction<?>>()
		{
			@Override
			public int compare(BuildingFunction<?> first, BuildingFunction<?> second)
			{
				if(first.m_rank < second.m_rank)
					return -1;
				if(first.m_rank == second.m_rank)
					return 0;
				return 1;
			}
		};
	}
	
	public abstract BuildingFunction<?> createNewForBuilding(long buildingKey);
	
	
	@Override
	public String getUserEditableClass()
	{
		return BuildingFunction.class.getCanonicalName();
	}
}
