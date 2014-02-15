package bmod.plugin.generic.gui;

import java.awt.BorderLayout;

import bmod.PredictionModel;
import bmod.gui.widgets.DataSetTable;
import bmod.plugin.baseclass.OutputWidget;


public class ZoneBreakdown extends OutputWidget
{
	private final DataSetTable dst = new DataSetTable();

	public ZoneBreakdown()
	{
		super("Power By Zone","A report that breaks energy usage down by zone.", null);
		outputWidgetPanel.setLayout(new BorderLayout());
		outputWidgetPanel.add(dst, BorderLayout.CENTER);
	}

	@Override
	public void generate(PredictionModel model)
	{
		dst.setTables(model.getZoneDataSets(), true, true);
	}
}
