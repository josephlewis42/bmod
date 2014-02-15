package bmod.plugin.loader;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import bmod.database.objects.BuildingFunction;
import bmod.plugin.PluginManager;

/**
 * Gets all of the user-editable data feeds.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class BuildingFunctionLoader extends PluginManager<BuildingFunction<?>>
{
	private static Collection<BuildingFunction<?>> ALL_BUILDING_FUNCTIONS;
	
	public BuildingFunctionLoader()
	{
		super("bmod/database/objects/");
	}
	
	/**
	 * Returns a list of all the building functions that exist.
	 * @return
	 */
	public synchronized Collection<BuildingFunction<?>> getFunctions()
	{
		if(ALL_BUILDING_FUNCTIONS != null)
			return ALL_BUILDING_FUNCTIONS;
		
		ALL_BUILDING_FUNCTIONS = new LinkedList<BuildingFunction<?>>();
		
		for(Object bfo : getPlugins())
		{
			try
			{
				BuildingFunction<?> bf = (BuildingFunction<?>) bfo;
				ALL_BUILDING_FUNCTIONS.add(bf);
			}catch(ClassCastException ex)
			{
				m_logger.debug("Couldn't cast: "+bfo+" to BuildingFunction: "+ex.getMessage());
			}
		}
		
		return ALL_BUILDING_FUNCTIONS;
	}
	
	/**
	 * Returns a list of functions that the user has created for the given 
	 * building.
	 * 
	 * @param buildingId
	 * @return
	 */
	public List<BuildingFunction<?>> getFunctionsForBuilding(long buildingId)
	{
		List<BuildingFunction<?>> bfs = new LinkedList<BuildingFunction<?>>();
		
		for(BuildingFunction<?> bf : getFunctions())
			bfs.addAll(bf.readBuildingDep(buildingId));
		
		return bfs;
	}
}
