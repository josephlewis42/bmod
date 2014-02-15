package bmod.plugin.generic.headless;

import bmod.ConfigReader;
import bmod.GenericPlugin;
import bmod.PredictionModel;
import bmod.database.DBWarningsList;

/**
 * Writes the config file that will be read from next time Bmod starts so 
 * settings don't have to be added *every* time.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class ConfigWriter extends GenericPlugin
{
	public ConfigWriter()
	{
		super("Config Writer", 
				"Writes the current simulation start and end dates to a file " +
				"so next time Bmod is started it will remember them for you.");
	}

	@Override
	public void minerStartHook(DBWarningsList wl, PredictionModel pm)
	{
		try
		{			
			ConfigReader.put("StartTime", pm.getStartTime().getTime());
			ConfigReader.put("EndTime", pm.getEndTime().getTime());
			ConfigReader.put("Interval", pm.getInterval());		
			ConfigReader.put("Building", pm.getBuilding().toString());
		}
		catch(Exception e)
		{
		}
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
