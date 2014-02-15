package bmod.plugin.generic.headless;

import java.util.Collection;

import bmod.GenericPlugin;
import bmod.PredictionModel;
import bmod.WattageEvent;
import bmod.database.DBWarningsList;
import bmod.database.objects.BuildingActivity;

/**
 * Warns of activities that lack loads.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class NoLoadWarning extends GenericPlugin
{
	public NoLoadWarning()
	{
		super("No Load Warning", 
				"Warns of activities without loads during a simulation");
	}

	private static DBWarningsList warningList;
	
	@Override
	public void minerStartHook(DBWarningsList wl, PredictionModel pm)
	{
		warningList = wl;
	}
	
	@Override
	public void minerActivityWattageEventReturnedHook(
			Collection<WattageEvent> events, BuildingActivity ba)
	{
		if(events.size() == 0 && ! ba.isAlways())
			warningList.addWarning("The activity: " + ba + " has no loads.");
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
