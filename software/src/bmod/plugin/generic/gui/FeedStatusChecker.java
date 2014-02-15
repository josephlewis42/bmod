package bmod.plugin.generic.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import bmod.DataSet;
import bmod.PredictionModel;
import bmod.database.DBWarningsList;
import bmod.database.DataNotAvailableException;
import bmod.gui.GuiExtensionPoints;
import bmod.gui.SwingSet;
import bmod.gui.widgets.DataSetTable;
import bmod.plugin.generic.headless.SmartGridProvider;
import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;
import edu.du.cs.smartgrid.SmartGridProviderEventListener;

/**
 * Remembers all of the feeds requested for the prior model run, and 
 * produces a report about which feeds were online and which were offline.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class FeedStatusChecker extends GenericGuiPlugin implements SmartGridProviderEventListener 
{
	private DateTimeRange m_dtr;
	private final TreeSet<Integer> feeds = new TreeSet<Integer>();
	private final JMenuItem feedsReportbutton = new JMenuItem("Feeds Report");
	private final JMenuItem[] m_menu = new JMenuItem[]{feedsReportbutton};
	private static final String MENU_NAME = "Feeds";
	private GuiExtensionPoints m_env;
	
	public FeedStatusChecker()
	{
		super("Feed Status Checker", "Allows you to see which feeds were not available during a simulation");
	
		feedsReportbutton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				showReport();
			}
		});
	}

	@Override
	public void setup(GuiExtensionPoints environment)
	{
		m_env = environment;
		SmartGridProvider.addProviderListener(this);
		m_env.addMenuItem(MENU_NAME, m_menu);
	}
	
	public void showReport()
	{
		if(m_dtr == null)
		{
			m_env.showError("You must run a range before requesting a feed status report.");
			return;
		}
		
		LinkedList<DataSet> tableSets = new LinkedList<DataSet>();
		
		for(Integer i : feeds)
		{
			DataSet points = new DataSet("" + i);
			
			for(DateTime dt : m_dtr.getClone())
			{
				try
				{
					points.addPoint(dt, SmartGridProvider.getFeedValue(i, dt));
				}catch(DataNotAvailableException e)
				{
					points.addPoint(dt, -999);
				}
			}
						
			tableSets.add(points);
		}
		
		DataSetTable providerInfo = new DataSetTable(tableSets.toArray(new DataSet[0]),false, false);
		new SwingSet("Feed Status Report", providerInfo, JFrame.DISPOSE_ON_CLOSE, false);
	}

	@Override
	public void teardown()
	{
		if(m_env != null)
		{
			m_env.removeMenuItem(MENU_NAME, m_menu);
			SmartGridProvider.removeProviderListener(this);
		}
	}

	@Override
	public void onCacheRequest(DateTime start, DateTime end, int feedid)
	{
		feeds.add(feedid);
	}

	@Override
	public void onServerOffline()
	{
		
	}

	@Override
	public void onServerConnected()
	{
		
	}

	@Override
	public void minerStartHook(DBWarningsList wl, PredictionModel pm)
	{
		m_dtr = pm.getTimeRange();
		feeds.clear();
	}

	

	@Override
	public double onNoFeedValue(int feedId, DateTime feedTime, double finalValue)
	{
		return finalValue;
	}
}
