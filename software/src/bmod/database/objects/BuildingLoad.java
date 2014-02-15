package bmod.database.objects;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import bmod.SerializableHashMap;
import bmod.database.CRUDCache;
import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.gui.builder.BooleanWidget;
import bmod.gui.builder.CSVRecordWidget;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.GUIBuilderWidget;
import bmod.gui.builder.IntWidget;
import bmod.gui.builder.PercentUsageOverrideWidget;
import bmod.gui.builder.SimpleWrapperWidget;
import bmod.gui.builder.TextAreaWidget;
import bmod.gui.builder.TextFieldWidget;
import bmod.gui.widgets.Dialogs;
import bmod.gui.widgets.MultiMapWidget;

public class BuildingLoad extends RoomDepRecord<BuildingLoad>
{
	private final String m_deviceName;
	private final long m_deviceType;
	private final boolean m_populationMultiply;
	private final String m_notes;
	private final long m_buildingName;
	private final long m_room; 
	private final int m_deviceQuantity;
	private final SerializableHashMap<String, Double> m_overrides;

	private static final String TABLE_NAME = "building_loads20120916";
	private static final String[] COLUMN_NAMES = new String[]{"RoomID","NumDevices","DeviceName","DeviceType","MultiplyPopulation","Notes","BuildingID","PrimaryKey","PercentUsageOverrides"};
	private static final String[] COLUMN_TYPES = new String[]{BIGINT, INT, VARCHAR_100, BIGINT, BOOL, VARCHAR_5000, BIGINT, BIGINT, VARCHAR_5000};
	private static final String[] COLUMN_INDEXES = new String[]{"RoomID", "BuildingID"};
	private static final Record<?>[] COLUMN_REFERENCES = new Record<?>[]{Database.templateRoom, null, null, Database.templateDeviceType, null, null, Database.templateBuilding, null, null};
	
	
	public BuildingLoad(long roomName, int numItems, String deviceName, long deviceType, boolean populationMultiply, String notes, long buildingName, long primaryKey, String pctUsageOverride)
	{
		super(TABLE_NAME,
				COLUMN_NAMES,
				COLUMN_TYPES, 
				COLUMN_INDEXES,
				COLUMN_REFERENCES,
				primaryKey);
		
		if( deviceName == null){
			throw new IllegalArgumentException();
		}

		m_deviceName = deviceName;
		m_deviceType = deviceType;
		m_populationMultiply = populationMultiply;
		m_notes = notes;
		m_buildingName = buildingName;
		m_deviceQuantity = numItems;
		
		
		m_room = roomName;
				
		if(pctUsageOverride == null)
			pctUsageOverride = "";
		
		if(!pctUsageOverride.equals(""))
			try
			{
				m_overrides = new SerializableHashMap<String, Double>(pctUsageOverride);
			} catch (IOException e)
			{
				throw new IllegalArgumentException("Couldn't parse pctUsageOverride! "+e.getMessage());
			} catch (ClassNotFoundException e)
			{
				throw new IllegalArgumentException("Couldn't parse pctUsageOverride!"+e.getMessage());
			}
		else
			m_overrides = new SerializableHashMap<String, Double>();
	}

	public BuildingLoad()
	{
		this(0L, 0, "", 0L, false, "", 0L, Database.TEMPLATE_PRIMARY_KEY, "");
	}
	
	public BuildingLoad(Building building, Room room, DeviceType deviceType)
	{
		this(room.getPrimaryKey(), 
				0,
				"New Device",
				deviceType.getPrimaryKey(),
				false,
				"",
				building.getPrimaryKey(),
				Database.getNewPrimaryKey(),
				"");
	}


	@Override
	public String getId() { return m_deviceName; } //m_id; }
	public String getDeviceName() { return m_deviceName; }
	public long getDeviceType() { return m_deviceType; }
	public int getDeviceQuantity() 
	{
		return m_deviceQuantity;
	}
	
	public boolean isPopulationMultiplied() { return m_populationMultiply;}
	public String getNotes() { return m_notes; }
	
	public long getRoom()
	{
		return m_room;
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public Object[] toSQL() {
		return new Object[]{
				m_room, 
				m_deviceQuantity,
				m_deviceName, 
				m_deviceType, 
				m_populationMultiply, 
				m_notes, 
				m_buildingName,
				getPrimaryKey(),
				m_overrides.safeSerialize("")
		};
	}


	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
		if(! doesBuildingExist() || ! doesRoomExist()) {
			delete();
			return;
		}
		
		if(m_deviceQuantity <= 0)
			list.addWarning("The load: " + this + "has 0 devices.");
	}

	public SerializableHashMap<String, Double> getPercentUsageOverrides()
	{
		return m_overrides;
	}

	public boolean hasOverride(String activityType)
	{
		return m_overrides.get(activityType) != null;
	}

	public double getOverride(String activityType)
	{
		return m_overrides.get(activityType);
	}

	@Override
	public BuildingLoad fromSQL(Object[] parts) throws IllegalArgumentException
	{
		return new BuildingLoad(
				(Long) parts[0], 
				(Integer) parts[1],
				(String) parts[2],
				(Long) parts[3], 
				(Boolean) parts[4], 
				(String) parts[5], 
				(Long) parts[6],
				(Long) parts[7],
				(String) parts[8]);
	}

	@Override
	public long getBuildingID()
	{
		return m_buildingName;
	}

	@Override
	protected BuildingLoad getThis()
	{
		return this;
	}
	
	/**
	 * Gets the PercentUsage associated with this BuildingLoad
	 * @return
	 */
	public double getPercentUsage(String activityType)
	{
		if(hasOverride(activityType))
		{
			return getOverride(activityType);
		}
		
		try
		{
			return DeviceTypeDutyCycle.readDeviceTypeDutyCycle(getBuildingID(), getDeviceType(), activityType).getDutyCycle();
		} catch (DatabaseIntegrityException e)
		{
			return 0;
		}
	}
	
	
	public static CRUDCache<LinkedList<BuildingLoad>> LOADS_FOR_ACTIVITY_CACHE = new CRUDCache<>();
	/**
	 * Reads the loads that are associated with the given activity that also 
	 * have duty cycles/overridden duty cycles.
	 * 
	 * @param a
	 * @return
	 */
	public Collection<BuildingLoad> readLoadsForActivity(BuildingActivity a)
	{
		LinkedList<BuildingLoad> tmp = LOADS_FOR_ACTIVITY_CACHE.get(a.getPrimaryKey());
		if(tmp != null)
		{
			return tmp;
		}
		
		LinkedList<BuildingLoad> llbl = new LinkedList<BuildingLoad>();
		for(BuildingLoad bl : readWhere().eq("RoomID", a.getRoomPrimaryKey()).all())
		{
			if(bl.getPercentUsage(a.getActivityType()) > 0)
			{
				llbl.add(bl);
			}
		}
		
		LOADS_FOR_ACTIVITY_CACHE.add(llbl, a.getPrimaryKey());
		return llbl;
	}
	
	/**
	 * Fetches the zones associated with this Load and returns them.
	 * 
	 * @return
	 */
	public Zone[] getZones()
	{
		 return new LoadToZoneMap().connectionsFrom(getPrimaryKey());
	}

	@Override
	public GUIBuilderPanel getEditor()
	{

		
		final MultiMapWidget<BuildingLoad, Zone> m_map = new MultiMapWidget<BuildingLoad, Zone>(this, new LoadToZoneMap());
		m_map.appendAddButton().addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				String s = Dialogs.showUserInputDialog("New Zone", "What would you like to name the new zone?");
				if(s == null)
					return;
				
				Zone z = new Zone(s);
				z.create();
				
				m_map.refreshPanel();
			}
		});
		
		m_map.setSource(this);
		
		return new GUIBuilderPanel(toSQL(),
					new GUIBuilderWidget[]{new SimpleWrapperWidget("Zones", m_map,true, false)},
					new CSVRecordWidget("Room", m_room, new Room(), false),
					new IntWidget("Number of Devices", m_deviceQuantity),
					new TextFieldWidget("Device Name", m_deviceName),
					new CSVRecordWidget("DeviceType", m_deviceType, new DeviceType()),
					new BooleanWidget("Multiply Population", m_populationMultiply, true),
					new TextAreaWidget("Notes", m_notes),
					null,
					null,
					new PercentUsageOverrideWidget("Percent Usage Override", m_overrides)
				);
	}
	
	@Override
	public void updateTable()
	{
	}

	@Override
	public long getRoomID()
	{
		return m_room;
	}
	
	@Override
	public String getUserEditableClass()
	{
		return this.getClass().getCanonicalName();
	}
	
	@Override
	public BuildingLoad createNew(Collection<Record<?>> filterObjects)
	{
		// Declare our objects
		Room templateRoom = null;
		Building templateBuilding = null;
		DeviceType templateDeviceType = null;
		
		// Instantiate all of our declared objects
		for (Record<?> tmp : filterObjects)
		{
			if (tmp instanceof Room)
			{
				templateRoom = (Room) tmp;
			}
			
			if (tmp instanceof Building)
			{
				templateBuilding = (Building) tmp;
			}
			
			if (tmp instanceof DeviceType)
			{
				templateDeviceType = (DeviceType) tmp;
			}
		}
		
		// Make sure we got all of our objects.
		if (templateBuilding == null)
		{
			throw new IllegalArgumentException("Cannot instantiate instance without proper arguments.");
		}
		
		// Return the new item.
		return new BuildingLoad(templateBuilding, templateRoom, templateDeviceType);
	}
	
}
