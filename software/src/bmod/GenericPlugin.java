package bmod;

import java.util.Collection;

import bmod.database.DBWarningsList;
import bmod.database.DataFeed;
import bmod.database.objects.BuildingActivity;
import bmod.database.objects.Room;
import bmod.util.DateTime;


/**
 * An interface that is used for loading plugins in bmod.plugin.generic.headless
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public abstract class GenericPlugin
{
	private final String m_name, m_description;
	
	public GenericPlugin(String pluginName, String pluginDescription)
	{
		m_name = pluginName;
		m_description = pluginDescription;
	}
	
	
	/**
	 * Set up the plugin using this method.
	 */
	public abstract void setupHeadless();
	
	/**
	 * Undo everything done in setup in this method, teardown does not
	 * necessarialy mean the application is shutting down, but rather that the
	 * user has chosen to close it.
	 */
	public abstract void teardown();
	
	/**
	 * Returns a short human-readable name for this plugin.
	 * 
	 * @return the name provided in the constructor.
	 */
	public final String getName()
	{
		return m_name;
	}
	
	/**
	 * Returns a short human-readable description for this plugin.
	 * 
	 * @return the description provided in the constructor
	 */
	public final String getDescription()
	{
		return m_description;
	}


	/**
	 * If you are a DataFeed provider, add all of the feeds you want to provide
	 * to the given list.
	 */
	public void appendAllPossibleDataFeeds(Collection<DataFeed> list)
	{
	}
	
	
	/**
	 * Modifies the number of threads the miner is to use.
	 * 
	 * @param maxThreads - the number of threads calculated so far to be used.
	 * @return The number of threads to use, if you don't care simply return
	 * the same number received.
	 */
	public int maxThreadsHook(int maxThreads)
	{
		return maxThreads;
	}

	/**
	 * Called when the simulation is started.
	 * 
	 * @param wl - the warning list the simulation is using
	 * @param pm - the prediction model the simulation is using
	 */
	public void minerStartHook(DBWarningsList wl, PredictionModel pm)
	{
	}

	/**
	 * Called when the simulation is finished.
	 * 
	 * @param wl - the warning list the simulation is using
	 * @param pm - the prediction model the simulation is using.
	 */
	public void minerEndHook(DBWarningsList wl, PredictionModel pm)
	{
	}

	/**
	 * Called when the simulator starts simulating a room.
	 * 
	 * @param wl - the warning list the simulation is using
	 * @param pm - the prediction model the simulation is using
	 * @param rm - the room this thread looks up values for
	 */
	public void minerThreadStartedHook(DBWarningsList wl, PredictionModel pm, Room rm)
	{
	}

	/**
	 * Called when the simulator finishes simulating a room.
	 * 
	 * @param wl - the warning list the simulation is using
	 * @param pm - the prediction model the simulation is using
	 * @param rm - the room this thread looks up values for
	 */
	public void minerThreadEndedHook(DBWarningsList wl, PredictionModel pm, Room rm)
	{
	}

	public DateTime minerActivityStartTimeHook(DateTime start)
	{
		return start;
	}

	public DateTime minerActivityEndTimeHook(DateTime end)
	{
		return end;
	}

	public DateTime minerFunctionStartTimeHook(DateTime start)
	{
		return start;
	}

	public DateTime minerFunctionEndTimeHook(DateTime end)
	{
		return end;
	}

	public void minerActivityWattageEventReturnedHook(
			Collection<WattageEvent> events, BuildingActivity ba)
	{		
	}

	public void minerFunctionWattageEventReturnedHook(
			Collection<WattageEvent> events, BuildingActivity ba)
	{
	}
	
	/**
	 * Called when the current prediction model has changed.
	 * @param newModel - the new model that the current one is being replaced 
	 * with.
	 */
	public void predictionModelChanged(PredictionModel newModel)
	{
	}
	
	@Override
	public String toString()
	{
		return m_name + " (" + getClass().getCanonicalName() + ")";
	}

}
