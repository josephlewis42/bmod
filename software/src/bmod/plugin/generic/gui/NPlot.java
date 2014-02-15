package bmod.plugin.generic.gui;

import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ZoomableChart;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JLabel;

import bmod.DataSet;
import bmod.IconLoader;
import bmod.PredictionModel;
import bmod.gui.widgets.CollapsiblePanel;
import bmod.gui.widgets.DataFeedChooser;
import bmod.gui.widgets.DateTimeRangeChooser;
import bmod.gui.widgets.Dialogs;
import bmod.gui.widgets.VerticalPanel;
import bmod.plugin.baseclass.OutputWidget;
import bmod.util.DateTime;

/**
 * 
 * This is the second attempt at the plot output plugin, the first used a 
 * gargantuan libary for plotting. Hopefully this does a little better, as it 
 * uses a small webserver and the user's browser instead.
 * 
 */
public class NPlot extends OutputWidget
{
	private static final Color[] CHART_COLORS = new Color[]{Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.black, Color.DARK_GRAY};

	protected transient PredictionModel m_model;
	protected transient HashMap<DateTime, Double> m_csvPlot = null;
	protected final DataFeedChooser m_feedChooser = new DataFeedChooser();
	private final ZoomableChart chart = new ZoomableChart();
	private final LinkedList<ITrace2D> traces = new LinkedList<>();
	private final DateTimeRangeChooser feedLineTime = new DateTimeRangeChooser();
	private final JButton serverLineButton = new JButton("Single Feed");
	private final JButton simButton = new JButton("Plot Simulation Breakdown");

	
	public NPlot()
	{
		super("Plot", "A plotting mechanism for Bmod", IconLoader.getIcon("chart_curve.png"));
		outputWidgetPanel.setLayout(new BorderLayout());
		outputWidgetPanel.add(chart, BorderLayout.CENTER);
		
		outputWidgetPanel.add(new CollapsiblePanel("Plot Options",new VerticalPanel(
				new JLabel("Plot a Feed"),
				m_feedChooser,
				feedLineTime,
				serverLineButton,
				new JLabel("Plot Simulation"),
				simButton
				), true), BorderLayout.NORTH);
		
		

		
		serverLineButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				DataSet ds = new DataSet(feedLineTime.getRange(), m_feedChooser.getSelectedItem());
				plot(new DataSet[]{ds}, "Single Feed");

			}
		});
		
		
		simButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(m_model == null)
				{
					Dialogs.showInformationDialog("Missing Simulation", "Pleas run a simulation before plotting it.");
					return;
				}
				
				DataSet[] dsTemp = m_model.getRoomDataSets();
				plot(dsTemp, "Simulation Breakdown");

			}
		});
	}
	
	public void plot(DataSet[] sets, String title)
	{
		for(ITrace2D trace : traces)
		{
			chart.removeTrace(trace);
		}
		
		// Create a chart:  
	    int i = 0;
	    for(DataSet ds : sets)
	    {
	    	
	    	// Create an ITrace: 
		    ITrace2D trace = new Trace2DSimple(); 
		    // Add the trace to the chart. This has to be done before adding points (deadlock prevention): 
		    chart.addTrace(trace);   
		    trace.setName(ds.getTitle());
		    trace.setColor(CHART_COLORS[i % CHART_COLORS.length]);
		    
		    traces.add(trace);

		    // Plot the points, but only enough that we don't kill the display 
		    // system (1 per 5px).
	    	final int n = (ds.size() * 5) / chart.getWidth();
		    int place = 0;
		    for(Entry<DateTime, Double> entry : ds.entrySet())
		    {
		    	place++;
		    	if(n > 0 && place % n != 0)
		    	{
		    		continue;
		    	}
		    	
		    	trace.addPoint(entry.getKey().getTime(), entry.getValue());
		    }
		    i++;
	    }
	    
	    chart.zoomAll();
	}
		

	@Override
	public void generate(PredictionModel model)
	{
		m_model = model;
		
		plot(new DataSet[]{m_model.getEstimatedKWData()}, "Estimated Data");
	}
}
