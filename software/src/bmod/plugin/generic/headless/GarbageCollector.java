package bmod.plugin.generic.headless;

import bmod.GenericPlugin;
import bmod.PredictionModel;
import bmod.database.DBWarningsList;

/**
 * Asks the system for GC when we know it'll be needed.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class GarbageCollector extends GenericPlugin
{
	public GarbageCollector()
	{
		super("Garbage Collector", 
				"Tells Java to do a garbage collection after a simulation.");
	}

	@Override
	public void minerEndHook(DBWarningsList wl, PredictionModel pm)
	{
		System.gc();
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
