package bmod.plugin.generic.gui;

import java.awt.BorderLayout;

import bmod.IconLoader;
import bmod.PredictionModel;
import bmod.gui.widgets.NTable;
import bmod.plugin.baseclass.OutputWidget;

/**
 * Provides a nicer output than just dumping a CSV somewhere.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class DetailTable extends OutputWidget
{
	private final NTable m_output = new NTable(true, true, true, false);
	
	public DetailTable()
	{
		super("Detailed Table",
				"Shows a detailed table of where every bit of energy use comes from.",
				IconLoader.getIcon("table.png"));
		
		outputWidgetPanel.setLayout(new BorderLayout(0, 0));
		outputWidgetPanel.add(m_output, BorderLayout.CENTER);
	}
	
	@Override
	public void generate(PredictionModel model)
	{
		// Create a table of data
		// Generate a header
		String[] header = model.getHeaderRow();
		// Generate the data
		Object[][] data = model.getDataRows();
		
		m_output.setTable(header,data);
	}
}
