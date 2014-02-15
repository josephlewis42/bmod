package bmod.plugin.generic.gui;

import java.awt.BorderLayout;

import bmod.DataSet;
import bmod.PredictionModel;
import bmod.gui.widgets.DataSetTable;
import bmod.plugin.baseclass.OutputWidget;

/**
 * Provides a nicer output than just dumping a CSV somewhere.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class kWAtTime extends OutputWidget
{
	private final DataSetTable dst = new DataSetTable();

	public kWAtTime()
	{
		super("kW At Time",
				"A report that breaks down electricity usage at a given time.",
				null);
		
		outputWidgetPanel.setLayout(new BorderLayout());
		outputWidgetPanel.add(dst, BorderLayout.CENTER);
	}

	@Override
	public void generate(PredictionModel model)
	{
		DataSet[] rooms = model.getRoomDataSets();
		DataSet[] dataSets = new DataSet[rooms.length + 2];
		
		for(int i = 0; i < rooms.length; i++)
		{
			dataSets[i] = rooms[i];
		}
		
		dataSets[rooms.length] = new DataSet("");
		dataSets[rooms.length + 1] = model.getEstimatedKWData();
		
		dst.setTables(dataSets, false, false, true);
	}
}
