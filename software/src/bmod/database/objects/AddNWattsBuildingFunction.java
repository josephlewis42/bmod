package bmod.database.objects;

import java.util.Collection;
import java.util.LinkedList;

import bmod.PredictionModel;
import bmod.WattageEvent;
import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.IntWidget;
import bmod.gui.builder.LongWidget;
import bmod.util.DateTime;

public class AddNWattsBuildingFunction extends BuildingFunction<AddNWattsBuildingFunction>
{
	private static final long DEFAULT_PRIMARY_KEY = -1;
	private static final String TABLE_NAME = "NWattsBuildingFunction";
	private static final String[] COLUMN_NAMES = new String[]{"PrimaryKey","BuildingID","Watts","Order"};
	private static final String[] COLUMN_TYPES = new String[]{"BIGINT","BIGINT","BIGINT","INTEGER"};
	private static final String[] COLUMN_INDEXES = new String[]{"BuildingID"};
	private static final Record<?>[] COLUMN_REFERENCES = new Record<?>[]{null, Database.templateBuilding, null, null};
	private final long m_building;
	private final long m_watts;
	private final int m_order;
	
	public AddNWattsBuildingFunction()
	{
		this(DEFAULT_PRIMARY_KEY, 0, 0, 0);
	}
	
	public AddNWattsBuildingFunction(long building)
	{
		this(Database.getNewPrimaryKey(), building, 0, 0);
	}

	public AddNWattsBuildingFunction(long pKey, long building, long watts, int order)
	{
		super(TABLE_NAME,
				COLUMN_NAMES, 
				COLUMN_TYPES, 
				COLUMN_INDEXES, 
				COLUMN_REFERENCES,
				pKey, 
				order);
		
		m_watts = watts;
		m_building = building;
		m_order = order;
	}

	@Override
	public void addWattageEvents(PredictionModel m, DBWarningsList dw)
	{
		LinkedList<WattageEvent> events = new LinkedList<WattageEvent>();
		for(DateTime t : m.getTimeRange()){
			events.add(new WattageEvent(t, m_watts, this, new String[]{}, new String[]{"Baseload"}));
		}		
		
		m.addEvents(events);
	}

	@Override
	public BuildingFunction<?> createNewForBuilding(long buildingKey)
	{
		return new AddNWattsBuildingFunction(buildingKey).create();
	}

	@Override
	public long getBuildingID()
	{
		return m_building;
	}

	@Override
	protected AddNWattsBuildingFunction getThis()
	{
		return this;
	}

	@Override
	protected String getId()
	{
		if(getPrimaryKey() == DEFAULT_PRIMARY_KEY)
		{
			return "Add N watts to baseload.";
		}
		
		return "Add " + m_watts + " watts to baseload.";
	}

	@Override
	public Object[] toSQL()
	{
		return new Object[]{
			getPrimaryKey(),
			m_building,
			m_watts,
			m_order
		};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{		
	}

	@Override
	public AddNWattsBuildingFunction fromSQL(Object[] parts) throws IllegalArgumentException
	{
		return new AddNWattsBuildingFunction(
				(Long)parts[0], 
				(Long)parts[1], 
				(Long)parts[2], 
				(Integer)parts[3]);
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		return new GUIBuilderPanel(toSQL(), 
				new LongWidget("PrimaryKey", getPrimaryKey(), false),
				new LongWidget("Building ID", m_building, false),
				new LongWidget("Watts", m_watts, true),
				new IntWidget("Order", m_order));
		
	}
	
	@Override
	public AddNWattsBuildingFunction createNew(Collection<Record<?>> filterObjects)
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
		return new AddNWattsBuildingFunction(templateBuilding.getPrimaryKey());
	}
}
