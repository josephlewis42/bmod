package bmod.plugin.generic.headless;

import bmod.GenericPlugin;
import bmod.PredictionModel;
import bmod.database.DBWarningsList;
import bmod.database.Database;

/**
 * Saves the database before the miner is run.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class SaveDatabase extends GenericPlugin
{
	public SaveDatabase()
	{
		super("Save Database", "Saves all of your work before a simulation to" +
				"make sure it isn't lost if the program crashes.");
	}

	@Override
	public void minerStartHook(DBWarningsList wl, PredictionModel pm)
	{
		Database.getDqm().save();
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
