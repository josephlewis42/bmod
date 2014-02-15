package bmod.database.objects;

import java.util.Collection;

import bmod.database.CRUDCache;
import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.gui.builder.ActivityTypeWidget;
import bmod.gui.builder.CSVRecordWidget;
import bmod.gui.builder.DoubleWidget;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.LongWidget;

/**
 * Represents a duty cycle for a particular device and activity type.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class DeviceTypeDutyCycle extends BuildingDependentRecord<DeviceTypeDutyCycle>
{
	private static final String TABLE_NAME = "DeviceTypeDutyCycles20120916";
	private static final String[] COLUMN_NAMES = new String[]{"PrimaryKey", "DeviceTypeID","DutyCycle","ActivityType","BuildingID"};
	private static final String[] COLUMN_TYPES = new String[]{"BIGINT", "BIGINT","DOUBLE","VARCHAR(100)","BIGINT"};
	private static final String[] COLUMN_INDEXES = new String[]{"BuildingID", "DeviceTypeID", "ActivityType"};
	private static final Record<?>[] COLUMN_REFERENCES = new Record<?>[]{null,Database.templateDeviceType,null,null,Database.templateBuilding};
	private final long m_deviceType;
	private final double m_dutyCycle;
	private final String m_activityType;
	private final long m_buildingID;
	
	public DeviceTypeDutyCycle()
	{
		this(Database.getNewPrimaryKey());
	}
	
	public DeviceTypeDutyCycle(long pkey, long devType, double dutyCycle, String activitytype, long bldgid)
	{
		super(TABLE_NAME,
				COLUMN_NAMES,
				COLUMN_TYPES, 
				COLUMN_INDEXES,
				COLUMN_REFERENCES,
				pkey);
		
		m_activityType = activitytype;
		m_buildingID = bldgid;
		m_deviceType = devType;
		m_dutyCycle = dutyCycle;
	}
	
	public DeviceTypeDutyCycle(long devicetype, double dc)
	{
		this(Database.getNewPrimaryKey(), devicetype, dc, "", 1);

	}
	
	public DeviceTypeDutyCycle(long primaryKey)
	{
		this(primaryKey, 0, 0.0, "", 0);
	}
	
	public DeviceTypeDutyCycle(Building building, DeviceType devicetype)
	{
		this(Database.getNewPrimaryKey(), devicetype.getPrimaryKey(),0.0,"",building.getPrimaryKey());
	}

	@Override
	public String getId()
	{
		String devTypeName = "" + m_deviceType;
		try
		{
			devTypeName = Database.templateDeviceType.readPrimaryKey(m_deviceType).getId();
		} catch (DatabaseIntegrityException e)
		{
		}
		return devTypeName + " " + m_activityType + " (id:"+getPrimaryKey()+")";
	}

	@Override
	public Object[] toSQL()
	{	
		return new Object[]{
			getPrimaryKey(),
			m_deviceType,
			m_dutyCycle,
			m_activityType,
			m_buildingID
		};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
		// If building does not exist, delete it.
		if(! doesBuildingExist())
		{
			delete();
			return;
		}
	}
	
	public long getDeviceType()
	{
		return m_deviceType;
	}

	public double getDutyCycle()
	{
		return m_dutyCycle;
	}

	public String getActivityType()
	{
		return m_activityType;
	}

	@Override
	public long getBuildingID()
	{
		return m_buildingID;
	}
	
	
	
	@Override
	public DeviceTypeDutyCycle fromSQL(Object[] parts) throws IllegalArgumentException
	{
		return new DeviceTypeDutyCycle(
				(Long) parts[0],
				(Long) parts[1],
				(Double) parts[2],
				(String) parts[3],
				(Long) parts[4]
				);
	}

	@Override
	protected DeviceTypeDutyCycle getThis()
	{
		return this;
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		return new GUIBuilderPanel(toSQL(),
					new LongWidget("Primary Key", getPrimaryKey(), false),
					new CSVRecordWidget("Device Type", m_deviceType, new DeviceType()),
					new DoubleWidget("Duty Cycle", m_dutyCycle, true),
					new ActivityTypeWidget("Activity Type", m_activityType),
					new CSVRecordWidget("Building", m_buildingID, new Building())
		);
	}
	
	@Override
	public void updateTable()
	{
	}
	
	
	public static CRUDCache<DeviceTypeDutyCycle> READ_DEVICE_TYPE_DUTY_CYCLE_CACHE = new CRUDCache<>();
	public static DeviceTypeDutyCycle readDeviceTypeDutyCycle(long buildingName,
			long deviceName, String activityType)
			throws DatabaseIntegrityException
	{
		DeviceTypeDutyCycle tmp = READ_DEVICE_TYPE_DUTY_CYCLE_CACHE.get(buildingName, deviceName, activityType);
		if(tmp != null)
		{
			return tmp;
		}
		
		 tmp = Database.templateDeviceTypeDutyCycle.readWhere()
				.eq("BuildingID", buildingName)
				.eq("DeviceTypeID", deviceName)
				.eq("ActivityType", activityType)
				.one();
		 
		 READ_DEVICE_TYPE_DUTY_CYCLE_CACHE.add(tmp, buildingName, deviceName, activityType);
		 
		 return tmp;
	}
	
	@Override
	public String getUserEditableClass()
	{
		return this.getClass().getCanonicalName();
	}
	
	@Override
	public DeviceTypeDutyCycle createNew(Collection<Record<?>> filterObjects)
	{
		
		// Declare our objects
		Building templateBuilding = null;
		DeviceType templateDeviceType = null;
		
		// Instantiate all of our declared objects
		for (Record<?> tmp : filterObjects)
		{
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
		return new DeviceTypeDutyCycle(templateBuilding, templateDeviceType);
	}
}
