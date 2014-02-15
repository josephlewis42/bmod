package bmod.plugin.generic.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import bmod.DataSet;
import bmod.database.Database;
import bmod.database.objects.BuildingActivity;
import bmod.database.objects.Room;
import bmod.gui.GuiExtensionPoints;
import bmod.gui.widgets.CSVRecordJComboBox;
import bmod.gui.widgets.DataSetTable;
import bmod.gui.widgets.DateTimeRangeChooser;
import bmod.gui.widgets.VerticalPanel;
import bmod.util.DateTime;

/**
 * A plugin that reports Schedules
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class ScheduleReporter extends GenericGuiPlugin
{
	private final JMenuItem scheduleButton = new JMenuItem("Room Schedule");
	private final JMenuItem[] buttonGroup = new JMenuItem[]{scheduleButton};
	private GuiExtensionPoints m_environment;
	
	public ScheduleReporter()
	{
		super("Schedule Reporter", "Adds the ability to generate reports about schedules.");
		
		scheduleButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				m_environment.showWindow("Room Schedule", new ScheduleBrowser());
			}
		});
	}


	@Override
	public void setup(final GuiExtensionPoints environment)
	{
		m_environment = environment;
		m_environment.addMenuItem("Reports", buttonGroup);
	}

	@Override
	public void teardown()
	{		
		if(m_environment != null)
		{
			m_environment.removeMenuItem("Reports", buttonGroup);
		}
	}
	
	

	public class ScheduleBrowser extends VerticalPanel implements ActionListener
	{
		private static final long serialVersionUID = 1L;
		private final DateTimeRangeChooser m_chooser = new DateTimeRangeChooser();
		private final JButton m_submit = new JButton("Report");
		private final CSVRecordJComboBox<Room> m_roomChooser = new CSVRecordJComboBox<Room>(Database.templateRoom);
		private final DataSetTable m_table = new DataSetTable();
		public ScheduleBrowser() 
		{
			add(new JLabel("1) Choose a time and range."));
			add(m_chooser);
			
			add(new JLabel("2) Choose a room"));
			add(m_roomChooser);
			add(new JLabel("3) Generate Report"));
			add(m_submit);
			add(new JSeparator());
			add(m_table);
			
			m_submit.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			HashMap<String, DataSet> activityEnumeration = new HashMap<String, DataSet>();
			for(String s : BuildingActivity.readAllTypes())
			{
				activityEnumeration.put(s, new DataSet(s));
			}
						
			DateTime last = null;
			for(DateTime t : m_chooser.getRange())
			{
				if(last != null)
				{
					// set all to 0
					for(DataSet ds : activityEnumeration.values())
					{
						ds.addPoint(t, 0);
					}
					
					// set the actual on ones to 1
					for(BuildingActivity ba : BuildingActivity.readActivities(last, t, m_roomChooser.getSelectedItem().getPrimaryKey()))
					{
						DataSet ds = activityEnumeration.get(ba.getActivityType());
						ds.addPoint(t, 1);
					}
				}
				
				last = t;
			}
						
			m_table.setTables(activityEnumeration.values().toArray(new DataSet[0]), false, false);
		}
	}
}
