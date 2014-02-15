package bmod.database.objects;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import bmod.PredictionModel;
import bmod.WattageEvent;
import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.gui.builder.DoubleWidget;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.GUIBuilderWidget;
import bmod.gui.builder.IntWidget;
import bmod.gui.builder.LongWidget;
import bmod.gui.builder.SimpleWrapperWidget;
import bmod.gui.builder.TimeWidget;
import bmod.gui.widgets.MultiMapWidget;
import bmod.util.DateTime;

public class SimpleLightingFunction extends
		BuildingFunction<SimpleLightingFunction>
{
	private static final String TABLE_NAME = "SimpleLightingFunction20121102";
	private static final String[] COLUMN_NAMES = new String[]{"PrimaryKey","Rank","BuildingID","ProportionOn","OffTime"};
	private static final String[] COLUMN_TYPES = new String[]{"BIGINT","INTEGER","BIGINT","DOUBLE","BIGINT"};
	private static final Record<?>[] COLUMN_REFERENCES = new Record<?>[]{null,null,Database.templateBuilding,null,null};
	private static final String[] INDEXES = new String[]{};
	public static final SimpleLightingFunction TEMPLATE = new SimpleLightingFunction();
	private final long m_buildingID, m_offTime; // Off time is milliseconds since midnight
	private final double m_proportionOn;
	
	private static final String LEFT_ON_STRING = "Items Left On";
	private static final String[] EVENT_ZONES = new String[]{};


	
	public SimpleLightingFunction()
	{
		this(Database.getNewPrimaryKey(), 0, 0, 0.0, 0);
	}
	
	public SimpleLightingFunction(long buildingId)
	{
		this(Database.getNewPrimaryKey(), 0, buildingId, 0.0, 0);
	}


	public SimpleLightingFunction(long pKey, int rank, long buildingID, double proportionOn, long offTime)
	{
		super(TABLE_NAME, COLUMN_NAMES, COLUMN_TYPES, INDEXES, COLUMN_REFERENCES, pKey, rank);
		m_buildingID = buildingID;
		m_proportionOn = proportionOn;
		m_offTime = offTime;
	}

	@Override
	public void addWattageEvents(PredictionModel m, DBWarningsList dw)
	{
		// Get all potential loads.
		HashMap<Long, Double> potentialLoads = new HashMap<Long, Double>();
		HashMap<Long, Boolean> roomOn = new HashMap<Long, Boolean>();
		LinkedList<WattageEvent> events = new LinkedList<WattageEvent>();
		
		for(long r : m.getRoomIDs())
		{
			double contributingRoomWattages = 0.0;
			
			// Get the wattages of each of the potential loads
			for(BuildingLoad bl : Database.templateBuildingLoad.readRoomDep(r))
			{
				try
				{
					DeviceType dt = Database.templateDeviceType.readPrimaryKey(bl.getDeviceType());
					
					if(SimpleRecord.hasOverlap(dt.getCategories(), getCategories()))
						contributingRoomWattages += bl.getDeviceQuantity() * dt.getWatts();

				} catch (DatabaseIntegrityException e)
				{
					dw.addError("Could not find the device type ID#" + bl.getDeviceType() + "referenced in " + bl);
				}
			}
			
			//dw.addInfo("Potential loads for " + r + " is " + contributingRoomWattages);
			
			potentialLoads.put(r, contributingRoomWattages);
			roomOn.put(r, false);
		}
		
		for(DateTime t : m.getTimeRange())
		{
			// If we're past the shut off point, turn off everything.
			if(t.getTimeOfDay() >= m_offTime)
			{
				for(long room : roomOn.keySet())
					roomOn.put(room, false);
				continue;
			}
			
			for(long room : m.getRoomIDs())
			{
				// check to see if there are activities
				if(Database.templateBuildingActivity.hasNonFunctions(room, t, t))//readNonFunctions(room, t, t).size() > 0)
					roomOn.put(room, true); // turn on the room
				else
					if(roomOn.get(room) != null && roomOn.get(room) == true)
						events.add(new WattageEvent(t, potentialLoads.get(room) * m_proportionOn, this, EVENT_ZONES, new String[]{LEFT_ON_STRING, LEFT_ON_STRING + " in: " + Room.getNameByPkey(room)}));
			}
		}
		
		m.addEvents(events);
	}

	@Override
	public BuildingFunction<?> createNewForBuilding(long buildingKey)
	{
		return new SimpleLightingFunction(buildingKey).create();
	}

	@Override
	public long getBuildingID()
	{
		return m_buildingID;
	}

	@Override
	protected SimpleLightingFunction getThis()
	{
		return this;
	}

	@Override
	protected String getId()
	{
		return "Lighting Left On";
	}

	@Override
	public Object[] toSQL()
	{
		return new Object[]{
				getPrimaryKey(),
				getRank(),
				m_buildingID,
				m_proportionOn,
				m_offTime
		};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
		if(m_proportionOn == 0)
			list.addWarning("The " + getId() + " function in " + Building.getNameByPkey(m_buildingID) + " has a porportion on of 0");
		
		if(m_offTime == 0)
			list.addWarning("The " + getId() + " function in " + Building.getNameByPkey(m_buildingID) + " has an off time of midnight, meaning no lights will ever be turned on.");
	}

	@Override
	public SimpleLightingFunction fromSQL(Object[] parts)
			throws IllegalArgumentException
	{
		return new SimpleLightingFunction(
				(Long) parts[0], 
				(Integer) parts[1], 
				(Long) parts[2], 
				(Double) parts[3], 
				(Long) parts[4]);
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		final MultiMapWidget<SimpleLightingFunction, DeviceTypeCategory> m_map = new MultiMapWidget<SimpleLightingFunction, DeviceTypeCategory>(this, new SimpleLightingFunctionToCategoryMap());
		m_map.setSource(this);
		
		return new GUIBuilderPanel(toSQL(), 
				new GUIBuilderWidget[]{new SimpleWrapperWidget("Categories", m_map, true, false)},
				new GUIBuilderWidget[]{
					new LongWidget("Primary Key", getPrimaryKey(), false),
					new IntWidget("Order", m_rank, true),
					new LongWidget("Building ID", m_buildingID, false),
					new DoubleWidget("Proportion On", m_proportionOn, true),
					new TimeWidget("Time to turn all off", m_offTime)
				}
				);
	}
	
	public DeviceTypeCategory[] getCategories()
	{
		return new SimpleLightingFunctionToCategoryMap().connectionsFrom(getPrimaryKey());
	}
	
	@Override
	public SimpleLightingFunction createNew(Collection<Record<?>> filterObjects)
	{
		// Make sure we have enough objects
		if (filterObjects.size() != 1)
		{
			throw new IllegalArgumentException("Cannot instantiate instance without proper arguments.");
		}
		
		// Declare our objects
		Building templateBuilding = null;
		
		// Instantiate all of our declared objects
		for (Record<?> tmp : filterObjects)
		{
			if (tmp instanceof Building)
			{
				templateBuilding = (Building) tmp;
			}
		}
		
		// Make sure we got all of our objects.
		if (templateBuilding == null)
		{
			throw new IllegalArgumentException("Cannot instantiate instance without proper arguments.");
		}
		
		// Return the new item.
		return new SimpleLightingFunction(templateBuilding.getPrimaryKey());
	}

}
