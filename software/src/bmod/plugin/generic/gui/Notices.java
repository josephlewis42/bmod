package bmod.plugin.generic.gui;

import java.awt.BorderLayout;

import bmod.IconLoader;
import bmod.PredictionModel;
import bmod.gui.widgets.DataSetTable;
import bmod.plugin.baseclass.OutputWidget;


public class Notices extends OutputWidget
{
	private final DataSetTable dst = new DataSetTable();

	public Notices()
	{
		super("Notices", 
				"A table that shows any notices that came from Bmod during a " +
						"model run",
						IconLoader.HELP_ICON);
		outputWidgetPanel.setLayout(new BorderLayout());
		outputWidgetPanel.add(dst, BorderLayout.CENTER);
	}

	@Override
	public void generate(PredictionModel model)
	{
		dst.setTables(model.getNotices(), false, false);
	}
}
