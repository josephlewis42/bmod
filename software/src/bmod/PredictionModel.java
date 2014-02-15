package bmod;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.database.objects.Building;
import bmod.database.objects.Room;
import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;

/**
 * Gathers a bunch of wattage events and is able to run stats on them.
 * 
 * @author Joseph Lewis
 */
public class PredictionModel
{
	private static final String SERIALIZATION_VERSION = "1.0";
	//private final List<WattageEvent> events = Collections.synchronizedList(new LinkedList<WattageEvent>());
	private final WattageEventCollection events = new WattageEventCollection();
	private Collection<Long> m_rooms;	
	private final DataSet m_totalWattages = new DataSet("Total Wattage");
	private final HashMap<String, DataSet> m_notices = new HashMap<String, DataSet>();
	
	private DateTime m_start;
	private DateTime m_end;
	private long m_interval; // Secs between generation times.
	private Collection<Room> m_toGenerateRooms;
	private boolean m_hasChanged = true;
	private long m_building; // the id of the building this model represents.
	

	public PredictionModel()
	{
	}
	
	/**
	 * @param start
	 * @param end
	 * @param interval
	 * @param buildingKey - The primary key of the building to generate a model for
	 */
	public PredictionModel(DateTime start, DateTime end, long interval, long buildingKey)
	{
		m_start = start;
		m_end = end;
		m_interval = interval;
		m_toGenerateRooms = Database.templateRoom.readBuildingDep(buildingKey);
		m_building = buildingKey;
	}
	
	public long getBuildingId()
	{
		return m_building;
	}
	
	public Collection<Room> getRoomsToGenerateFor()
	{
		return m_toGenerateRooms;
	}
	
	
	public void addEvents(Collection<WattageEvent> e)
	{
		m_hasChanged = true;
		
		synchronized(events)
		{
			events.addAll(e);
		}
	}
	
	
	private static final String PREDICTION_MODEL_ELEMENT_NAME = "prediction_model";
	private static final String VERSION_ATTRIBUTE_NAME = "version";
	private static final String START_DATE_ATTRIBUTE_NAME = "start_date";
	private static final String END_DATE_ATTRIBUTE_NAME = "end_date";
	private static final String INTERVAL_ATTRIBUTE_NAME = "interval";
	private static final String WATTAGE_EVENT_NODE_NAME = "event";
	private static final String BUILDING_ATTRIBUTE_NAME = "building";
	
	/**
	 * Converts this prediction model to an XML document.
	 * @return - this simulation as an XML file.
	 * @throws ParserConfigurationException - if we can't make a new documentbuilder.
	 */
	private Document predictionModelToXML() throws ParserConfigurationException
	{
		// Setup factories
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// Create default document information.
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(PREDICTION_MODEL_ELEMENT_NAME);
		rootElement.setAttribute(VERSION_ATTRIBUTE_NAME, SERIALIZATION_VERSION);
		rootElement.setAttribute(START_DATE_ATTRIBUTE_NAME, m_start.toISODate());
		rootElement.setAttribute(END_DATE_ATTRIBUTE_NAME, m_end.toISODate());
		rootElement.setAttribute(INTERVAL_ATTRIBUTE_NAME, "" + m_interval);
		rootElement.setAttribute(BUILDING_ATTRIBUTE_NAME, "" + m_building);
		doc.appendChild(rootElement);
		
		
		for(WattageEvent w : events)
		{
			rootElement.appendChild(w.setupNode(doc.createElement(WATTAGE_EVENT_NODE_NAME)));
		}
		
		return doc;
	}
	
	public void compressSerialize(Path p) throws IOException
	{		
		try
		{
			Document doc = predictionModelToXML();
			
			// Convert to String.
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

			BufferedWriter w = Files.newBufferedWriter(p, Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			transformer.transform(new DOMSource(doc), new StreamResult(w));
			w.close();
			
		} catch (ParserConfigurationException | TransformerException e)
		{
			e.printStackTrace();
		}
	}
	
	public static PredictionModel deserialize(Path path)
	{
		final PredictionModel tmpModel = new PredictionModel();
		final LinkedList<WattageEvent> events = new LinkedList<WattageEvent>();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			javax.xml.parsers.SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {

				@Override
				public void startElement(String uri, String localName, String qName, 
						Attributes attributes) throws SAXException {
					
					switch(qName)
					{
						case PREDICTION_MODEL_ELEMENT_NAME:
							try
							{
								//String ver = attributes.getValue(VERSION_ATTRIBUTE_NAME);
								tmpModel.m_start = new DateTime(attributes.getValue(START_DATE_ATTRIBUTE_NAME));
								tmpModel.m_end = new DateTime(attributes.getValue(END_DATE_ATTRIBUTE_NAME));
								tmpModel.m_interval = Integer.parseInt(attributes.getValue(INTERVAL_ATTRIBUTE_NAME));
								tmpModel.m_building = Long.parseLong(attributes.getValue(BUILDING_ATTRIBUTE_NAME));

							}catch(Exception e)
							{
								return;
							}
							break;

						case WATTAGE_EVENT_NODE_NAME:
							WattageEvent tmp = WattageEvent.deserialize(attributes);
							if(tmp != null)
							{
								events.add(tmp);
							}
							break;
					}

				}

				@Override
				public void endElement(String uri, String localName, String qName) throws SAXException {}

				@Override
				public void characters(char ch[], int start, int length) throws SAXException {}

			};

			saxParser.parse(path.toFile(), handler);

		} catch (Exception e) {
			e.printStackTrace();
		}
		if(tmpModel != null)
		{
			tmpModel.addEvents(events);
		}
		return tmpModel;
	}

		
	private void process_events()
	{
		synchronized(events)
		{
			if(! m_hasChanged)
			{
				return;
			}
			
			m_hasChanged = false;
			
			m_rooms = new HashSet<Long>();			
			m_totalWattages.clear();
		
			for(WattageEvent e : events)
			{
				if(e == null)
				{
					continue;
				}
				
				m_rooms.add(e.getRoom());
				m_totalWattages.incrementPoint(e.getStartTime(), e.getWattage());
			}
		}
	}
	
	/**
	 * Gets the header rows for the output CSV
	 * @return
	 */
	public String[] getHeaderRow()
	{
		process_events();
		String wattageHeader = WattageEvent.getRowHeaderCSV();
		wattageHeader += ",Total Watts at Time";
		return wattageHeader.split(",");
	}
	
	/**
	 * Gets the data rows for the output CSV
	 * @return
	 */
	public Object[][] getDataRows()
	{
		process_events();

		synchronized(events)
		{
			Object[][] data = new Object[events.size()][];
			
			int index = 0;
			for(WattageEvent e : events)
			{
				data[index] = (e.toString() + "," + getTotalWattageAtTime(e.getStartTime())).split(","); 
				index++;
			}
			
			return data;
		}
	}
	
	/**
	 * Returns the buildings for which this model has events.
	 * 
	 * @return
	 */
	public Building getBuilding()
	{		
		try
		{
			return Database.templateBuilding.readPrimaryKey(m_building);
		} 
		catch (DatabaseIntegrityException e)
		{
			return Database.templateBuilding; // should never happen.
		}
	}
	
	/**
	 * Returns the rooms for which this model has events.
	 * 
	 * @return
	 */
	public long[] getRoomIDs()
	{
		process_events();
		
		long[] room_ids = new long[m_rooms.size()];
		
		int i = 0;
		for(long rmid : m_rooms)
		{
			room_ids[i] = rmid;
			i++;
		}
		
		return room_ids;
	}
	
	
	/**
	 * Returns the interval between generations for this model in seconds.
	 */
	public long getInterval()
	{
		return m_interval;
	}
	
	/**
	 * Returns the start time this model was generating for.
	 * @return
	 */
	public DateTime getStartTime()
	{
		return m_start.clone();
	}

	/**
	 * Returns the end time this model was generating for.
	 * @return
	 */
	public DateTime getEndTime()
	{
		return m_end.clone();
	}
	
	/**
	 * Returns a copy of the time range this model used to generate for.
	 * @return
	 */
	public DateTimeRange getTimeRange()
	{
		return new DateTimeRange(m_start, m_end, m_interval);
	}
	
	/**
	 * Returns the total Wattage in kW that the WattageEvents
	 * record for a given time.
	 * 
	 * @throws NumberFormatException if the wattage isn't found.
	 */
	public double getTotalWattageAtTime(DateTime time) throws NumberFormatException
	{
		process_events();
		
		return m_totalWattages.getValue(time);
	}
	
	/**
	 * Adds a "notice", a specific point of data tied to a particular time and 
	 * an arbitrary string.
	 * 
	 * @param noticeCategory - The "category" for the notice, any string.
	 * @param time - The time the notice appears at.
	 * @param value - The value of the notice.
	 */
	public void addNotice(String noticeCategory, DateTime time, double value)
	{
		synchronized(m_notices)
		{
			DataSet ds = m_notices.get(noticeCategory);
			if(ds != null)
			{
				ds.addPoint(time, value);
				return;
			}
			
			ds = new DataSet(noticeCategory);
			ds.addPoint(time, value);
			
			m_notices.put(noticeCategory, ds);
		}
	}
	
	public DataSet[] getNotices()
	{
		synchronized(m_notices)
		{
			return m_notices.values().toArray(new DataSet[m_notices.size()]);
		}
	}
	
	public DataSet[] getZoneDataSets()
	{
		return convertAllEventsToDataSet(new ModelTotalBucket.BucketFunction()
		{
			@Override
			public String[] getIndexes(WattageEvent e)
			{
				return e.getZones();
			}
		});
	}
	
	public DataSet[] getCategoryDataSets()
	{
		return convertAllEventsToDataSet(new ModelTotalBucket.BucketFunction()
		{
			@Override
			public String[] getIndexes(WattageEvent e)
			{
				return e.getCategories();
			}
		});
	}
	
	public DataSet[] getActivityDataSets()
	{
		return convertAllEventsToDataSet(new ModelTotalBucket.BucketFunction()
		{
			@Override
			public String[] getIndexes(WattageEvent e)
			{
				return new String[]{e.getActivityType()};
			}
		});
	}
	
	public DataSet[] getRoomDataSets()
	{
		ModelTotalBucket roomDataSets = new ModelTotalBucket(new ModelTotalBucket.BucketFunction()
		{
			@Override
			public String[] getIndexes(WattageEvent e)
			{
				return new String[]{Room.getNameByPkey(e.getRoom())};
			}
		});
		
		synchronized(events)
		{
			roomDataSets.addAll(events);
			return roomDataSets.getDataSets();
		}
	}
	
	/**
	 * Sorts through all events finding those that match the given function and
	 * returns them.
	 * 
	 * @param fun
	 * @return
	 */
	private DataSet[] convertAllEventsToDataSet(ModelTotalBucket.BucketFunction fun)
	{
		ModelTotalBucket categoryBucket = new ModelTotalBucket(fun);
		
		synchronized(events)
		{
			categoryBucket.addAll(events);
			return categoryBucket.getDataSets();
		}
	}
	
	/**
	 * Returns a DataSet representing the total demand over the given time 
	 * period.
	 */
	public DataSet getEstimatedKWData()
	{
		process_events();
		
		// Scales all Wattages in to kW
		return m_totalWattages.scale("Estimated kW", .001);
	}
	
	/**
	 * Returns a DataSet representing the total demand over the given time 
	 * period in watts
	 */
	public DataSet getEstimatedWData()
	{
		process_events();
		
		// Scales all Wattages in to kW
		return m_totalWattages.scale("Total Watts", 1);
	}
}
