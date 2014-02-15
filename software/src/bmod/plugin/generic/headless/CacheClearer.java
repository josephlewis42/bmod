package bmod.plugin.generic.headless;

import bmod.GenericPlugin;
import bmod.PredictionModel;
import bmod.database.CRUDCache;
import bmod.database.DBWarningsList;

public class CacheClearer extends GenericPlugin
{
	public CacheClearer()
	{
		super("Cache Clearer", "Clears the cache after the miner is done to reduce memory consumption.");
	}

	@Override
	public void minerEndHook(DBWarningsList wl, PredictionModel pm)
	{
		// Important! Kill all the caches after exit, it'll mean a slower
		// startup next time, but should take out about 1GB of memory usage
		CRUDCache.clearAllCaches();
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
