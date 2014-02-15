package bmod.plugin.generic.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import bmod.buildingactivity.BuildingActivityInterface;
import bmod.buildingactivity.RepeatingEvent;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.database.objects.Building;
import bmod.database.objects.BuildingActivity;
import bmod.database.objects.Room;
import bmod.gui.GuiExtensionPoints;
import bmod.gui.SwingSet;
import bmod.gui.widgets.JProgressDialog;
import bmod.util.DateTime;

/**
 * Provides an interface for the user to manually update schedules based upon
 * public information DU publishes about their courses.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class CourseFetcherMenuItem extends GenericGuiPlugin implements ActionListener
{

	private static final String COURSE_SEARCH_URL = "http://myweb.du.edu/mdb/pducrs.p_duSrchCrsOff";
	private static final String HISTORICAL_DATE_SELECT_URL = "http://myweb.du.edu/mdb/pducrs.p_duSlctCrsOff";
	private static final String TOPIC_SELECT_URL = "http://myweb.du.edu/mdb/pducrs.p_duSlctSubj";

	public static final int ACT_FIELD = 12;
	public static final int ACTIVITY_TYPE_FIELD = 4;
	public static final int BLDG_AND_ROOM_FIELD = 9;
	public static final int CAP_FIELD = 11;
	public static final int CLASS_FIELD = 1;
	public static final int CRN_FIELD = 0;
	public static final int DATES_FIELD = 5;
	public static final int DAYS_OF_WEEK_FIELD = 7;
	public static final int HOURS_FIELD = 8;
	public static final int INSTRUCTOR_FIELD = 10;
	
	
	private static JPanel m_blank = new JPanel();
	private static JButton m_fetchButton = new JButton("Fetch Courses");
	private static JLabel m_fetchVal = new JLabel("Select Term to Search For:");
	private static Logger m_logger = Logger.getLogger("CourseFetcher");
	private static JPanel m_searchPanel = new JPanel();
	private static JComboBox<String> m_termSelector = new JComboBox<>();
	public static final int NAME_FIELD = 3;
	
	// Number of table columns that are used to represent a valid class.
	public static final int NUM_COLS_PER_VALID_CLASS = 13;
	public static final int SECT_FIELD = 2;
	public static final int TIMES_FIELD = 6;
	private final JMenuItem fetchCourseItem = new JMenuItem("Fetch Course Details");
	private GuiExtensionPoints m_environment = null;
	private final JMenuItem[] menu = new JMenuItem[]{fetchCourseItem};
	
	
	public CourseFetcherMenuItem()
	{

		super("Course Fetcher", "Updates course information using publicly available databases from DU.");
		fetchCourseItem.addActionListener(this);
		
		/* Set up the search panel */
		m_searchPanel.setLayout(new GridLayout(0, 2));
		m_searchPanel.add(m_fetchVal);
		
		// Do in background to avoid hangups.
		@SuppressWarnings("rawtypes")
		SwingWorker worker = new SwingWorker() 
		{
			@Override
			protected Object doInBackground() throws Exception
			{
				try
				{
					Document doc = Jsoup.connect(HISTORICAL_DATE_SELECT_URL).get();
					//System.out.println(doc);
					Elements terms = doc.select("select[name=p_term] > option");
					//System.out.println(terms);
					for(Element e : terms)
						m_termSelector.addItem(e.attr("value") + " -> " + e.text());
						
				} catch(Exception ex)
				{
					m_logger.error("", ex);
				}
				
				return null;
			}
		};
		
		worker.execute();
		
		m_searchPanel.add(m_termSelector);
		
		m_searchPanel.add(m_blank);
		m_searchPanel.add(m_fetchButton);
		
		m_fetchButton.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == fetchCourseItem)
		{
			new SwingSet("Fetch Courses", m_searchPanel, JFrame.DISPOSE_ON_CLOSE, false);
		}
		
		if(e.getSource() == m_fetchButton)
		{
			try
			{
				String selected_term = m_termSelector.getSelectedItem().toString();
				selected_term = selected_term.substring(0, selected_term.indexOf(' '));
				
				Document doc = Jsoup.connect(TOPIC_SELECT_URL).data("p_term",selected_term,"p_coll","ALL").post();
				final Elements terms = doc.select("select[name=p_subj] > option");
				final JProgressDialog progress = new JProgressDialog("Fetching Courses Progress","Fetching courses",terms.size());
				SwingWorker<?, ?> worker = new SwingWorker<Object, Object>(){

					@Override
					protected Object doInBackground() throws Exception
					{
						for(Element elem : terms)
						{
							progress.incrementProgress("Fetching Courses For:" + elem.attr("value"), 1);
							fetchCourses(elem.attr("value"));
						}
						
						progress.close();
						return null;
					}
				};
				
				worker.execute();
				
			} catch(Exception ex)
			{
				m_logger.error("", ex);
			}
		}
	}

	
	private void fetchCourses(String subjectPost)
	{
		try
		{
			String selected_term = m_termSelector.getSelectedItem().toString();
			selected_term = selected_term.substring(0, selected_term.indexOf(' '));
			
			Document doc = Jsoup.connect(COURSE_SEARCH_URL).data("p_subj",subjectPost).post();
			
			processPage(doc);
							
		} catch(Exception ex)
		{
			m_logger.error("", ex);
		}
	}

		
	private Building getOrCreateBuilding(String bldgname)
	{
		try
		{
			for(Building b : Database.templateBuilding.readAll())
				if(b.getId().toUpperCase().equals(bldgname.toUpperCase()))
					return b;
			
			throw new DatabaseIntegrityException("hi");
		} catch(DatabaseIntegrityException ex)
		{
			return new Building(bldgname).create();
		}
	}
	
	private void processClass(String[] course) throws DatabaseIntegrityException
	{
		
		// CRN, CLASS, SECT, NAME, TYPE, DATES, TIMES, DAYS OF WEEK, HOURS ROOM, INSTRUCTOR, CAP, ACT
		//[4584, LGST 2570, 1, Contracts for Business, Lecture, 05-JUN-12 to 05-JUN-12, 10:00AM-11:50AM, T, 4, DCB 130, Holt, Paula A., 30, 32]
		
		String activityType = course[ACTIVITY_TYPE_FIELD];
		String activityName = course[NAME_FIELD] + " Section: " + course[SECT_FIELD];
		int activityPopulation = Integer.parseInt(course[ACT_FIELD]);
		String activityRoom = course[BLDG_AND_ROOM_FIELD];
		
		if(course[BLDG_AND_ROOM_FIELD] == null)
			course[BLDG_AND_ROOM_FIELD] = "NOWHERE";
		
		String[] bldg_room = course[BLDG_AND_ROOM_FIELD].split(" ");
		String bldgname = "";
		String rmname = "";
		
		switch(bldg_room.length)
		{
			case 1:
				bldgname = rmname = bldg_room[0];
				break;
			case 2:
				bldgname = bldg_room[0];
				rmname = bldg_room[1];
				break;
			default:
				m_logger.error("Building and room don't work: " + activityRoom);
				return;
		}
		
		Building bldg = getOrCreateBuilding(bldgname);
		Room room = Database.templateRoom.getOrCreateRoom(bldg.getPrimaryKey(), rmname);
		
		
		if(course[TIMES_FIELD] == null)
			course[TIMES_FIELD] = "12:00AM-12:00AM";
		
		String startTime = course[TIMES_FIELD].split("-")[0];
		String endTime = course[TIMES_FIELD].split("-")[1];
		
		// A course needs dates, if nothing else.
		if(course[DATES_FIELD] == null)
			return;
		
		DateTime startDate = new DateTime(course[DATES_FIELD].split(" to ")[0].trim() + " " + startTime.trim(), DateTime.MYWEB_FORMAT);
		DateTime endDate = new DateTime(course[DATES_FIELD].split(" to ")[1].trim() + " " + endTime.trim(), DateTime.MYWEB_FORMAT);
		
		if(course[DAYS_OF_WEEK_FIELD] == null)
			course[DAYS_OF_WEEK_FIELD] = "";
		
		BuildingActivityInterface bai = new RepeatingEvent(course[DAYS_OF_WEEK_FIELD], 
											startDate,
											endDate);
		
		
		BuildingActivity m_activity = new BuildingActivity(room.getPrimaryKey(), 
															activityName, 
															activityType, 
															activityPopulation, 
															bldg.getPrimaryKey(), 
															"", // No zone
															bai.getInterfaceId(), 
															bai.getPropertiesString(), 
															Database.getNewPrimaryKey());
		
		System.out.println(m_activity.getId());
		m_activity.create();
	}
	
	private void processPage(Document doc)
	{
		Elements rows = doc.select("tr");
		String[] last_row = new String[NUM_COLS_PER_VALID_CLASS];
		for(Element e : rows)
		{
			Elements tds = e.select("td");
			
			if(tds.size() != NUM_COLS_PER_VALID_CLASS)
				continue;
			
			String[] curr_row = new String[NUM_COLS_PER_VALID_CLASS];
			
			for(int i = 0; i < NUM_COLS_PER_VALID_CLASS; i++)
			{
				curr_row[i] = tds.get(i).text();
				
				// Use this because Java's inability to remove non-breaking 
				// spaces through String.trim().
				if(curr_row[i].replaceAll("\\p{javaSpaceChar}", "").length() == 0)
					curr_row[i] = last_row[i]; // Prev row should have things like names for labs.
			}
			
			try
			{
				processClass(curr_row);	
				
			}catch(Exception ex)
			{
				m_logger.error(Arrays.toString(last_row));
				m_logger.error(Arrays.toString(curr_row));
				m_logger.error("", ex);
			}
			
			
			last_row = curr_row;
		}
	}
	
	@Override
	public void setup(GuiExtensionPoints environment)
	{
		m_environment = environment;
		environment.addMenuItem("Database", menu);
	}
		
	@Override
	public void teardown()
	{
		m_environment.removeMenuItem("Database", menu);
	}

}
