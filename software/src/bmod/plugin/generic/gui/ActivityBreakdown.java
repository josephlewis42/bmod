package bmod.plugin.generic.gui;

import java.awt.BorderLayout;

import bmod.PredictionModel;
import bmod.gui.widgets.DataSetTable;
import bmod.plugin.baseclass.OutputWidget;


public class ActivityBreakdown extends OutputWidget
{
	private final DataSetTable dst = new DataSetTable();

	public ActivityBreakdown()
	{
		super("Power By Activity",
				"An output widget that breaks down power usage by activity type.",
				null);
		outputWidgetPanel.setLayout(new BorderLayout());
		outputWidgetPanel.add(dst, BorderLayout.CENTER);
	}

	@Override
	public void generate(PredictionModel model)
	{
		dst.setTables(model.getActivityDataSets(), true, true);
	}
}
