package bmod.plugin.generic.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

import bmod.DataSet;
import bmod.ExtensionPoints;
import bmod.database.DataFeed;
import bmod.gui.GuiExtensionPoints;
import bmod.gui.MultipleRegression;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.SimpleWrapperWidget;
import bmod.gui.widgets.ArticleLayoutPanel;
import bmod.gui.widgets.DataSetTable;
import bmod.gui.widgets.DateTimeRangeChooser;
import bmod.gui.widgets.EasyMultiSelectWidget;

/**
 * Allows the user to directly fetch a given number of feeds from the feed 
 * stores.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class FeedValueFetcherMenuItem extends GenericGuiPlugin implements ActionListener
{
	//private final DataFeedList m_bsChooser = new DataFeedList();
	private final EasyMultiSelectWidget<DataFeed> m_bsChooser;
	private final JButton m_fetchButton = new JButton("Fetch");
	private final JButton m_multipleRegressionButton = new JButton("Run Multiple Regression");
	private final DateTimeRangeChooser m_timeChooser = new DateTimeRangeChooser();
	private final DataSetTable m_dataOutput = new DataSetTable();
	private final JMenuItem m_selectorItem = new JMenuItem("Interactive Feed Viewer");
	private final JMenuItem[] m_menu = new JMenuItem[]{m_selectorItem};
	private GuiExtensionPoints m_environment = null;
	private final ArticleLayoutPanel m_panel;
	
	public FeedValueFetcherMenuItem()
	{
		super("Feed Value Fetcher", "Looks up feed values from the SmartGridDataStore for you");
		m_bsChooser = new EasyMultiSelectWidget<DataFeed>(ExtensionPoints.getAllDataFeeds().toArray(new DataFeed[0]));
		
		m_panel = new ArticleLayoutPanel(
					new GUIBuilderPanel(
						new SimpleWrapperWidget("Feed to fetch", new JScrollPane(m_bsChooser)),
						new SimpleWrapperWidget("Times to fetch", m_timeChooser),
						new SimpleWrapperWidget("", m_fetchButton),
						new SimpleWrapperWidget("", m_dataOutput, true, false)
						),
					m_dataOutput,
					null);
		
		m_fetchButton.addActionListener(this);
		m_selectorItem.addActionListener(this);
		m_dataOutput.getUnderlyingNTable().addToolbarEnd(m_multipleRegressionButton);
		
		m_multipleRegressionButton.addActionListener(this);
	}
	

	@Override
	public void setup(GuiExtensionPoints environment)
	{
		m_environment = environment;
		environment.addMenuItem("Feeds", m_menu);
	}

	@Override
	public void teardown()
	{
		if(m_environment != null)
		{
			m_environment.removeMenuItem("Feeds", m_menu);
		}
	}

	@Override
	public void actionPerformed(ActionEvent evt)
	{
		if(evt.getSource() == m_selectorItem)
		{
			m_environment.showDialog("Feed Fetcher", m_panel);
		}
		
		if(evt.getSource() == m_fetchButton)
		{
			m_fetchButton.setText("Working...");
			m_fetchButton.setEnabled(false);
			LinkedList<DataSet> dataSets = new LinkedList<DataSet>();
			
			for(DataFeed tmp : m_bsChooser.getSelectedValuesList())
			{
				try
				{
					dataSets.add(new DataSet(m_timeChooser.getRange(), tmp));
				} catch(Exception e)
				{
					// can't fetch the data set.
				}
			}
			
			m_dataOutput.setTables(dataSets.toArray(new DataSet[0]),false, false);
			
			m_fetchButton.setEnabled(true);
			m_fetchButton.setText("Fetch");
		}
		
		if(evt.getSource() == m_multipleRegressionButton)
		{
			MultipleRegression mr = new MultipleRegression(m_dataOutput.getUnderlyingNTable());
			m_environment.showDialog("Multiple Regression", mr);
		}
	}
}
