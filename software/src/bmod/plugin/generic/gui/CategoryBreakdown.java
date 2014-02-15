package bmod.plugin.generic.gui;

import java.awt.BorderLayout;

import bmod.PredictionModel;
import bmod.gui.widgets.DataSetTable;
import bmod.plugin.baseclass.OutputWidget;


public class CategoryBreakdown extends OutputWidget
{
	private final DataSetTable dst = new DataSetTable();

	public CategoryBreakdown()
	{
		super("Power By Category",
				"Displays power usage by category",
				null);
		outputWidgetPanel.setLayout(new BorderLayout());
		outputWidgetPanel.add(dst, BorderLayout.CENTER);
	}

	@Override
	public void generate(PredictionModel model)
	{
		dst.setTables(model.getCategoryDataSets(), true, true);
	}
}
