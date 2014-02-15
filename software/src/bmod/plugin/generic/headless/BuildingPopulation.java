package bmod.plugin.generic.headless;

import java.util.Collection;

import bmod.GenericPlugin;
import bmod.database.DataFeed;
import bmod.database.DataNotAvailableException;
import bmod.database.Database;
import bmod.database.objects.Building;
import bmod.database.objects.BuildingActivity;
import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;

/**
 * Returns the population of the building at the given time.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class BuildingPopulation extends GenericPlugin implements DataFeed
{
	private Building m_bldg;
	
	public BuildingPopulation()
	{
		super("Building Population Data Feed", "Provides a data feed where you can find out the expected population of a building at a given time");
	}
	
	public BuildingPopulation(Building bldg)
	{
		this();
		m_bldg = bldg;
	}

	@Override
	public double getDataAtTime(DateTime t) throws DataNotAvailableException
	{
		double total = 0.0;
		
		for(BuildingActivity ba : Database.templateBuildingActivity.readBuildingDep(m_bldg.getPrimaryKey()))
			if(ba.happensWithin(t, t))
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
		return "Population of: " + m_bldg;
	}

	@Override
	public void appendAllPossibleDataFeeds(Collection<DataFeed> feeds)
	{
		for(Building buildingName : Database.templateBuilding.readAll())
			feeds.add(new BuildingPopulation(buildingName));
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
