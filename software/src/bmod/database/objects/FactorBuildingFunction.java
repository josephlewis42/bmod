package bmod.database.objects;

import java.util.Collection;
import java.util.LinkedList;

import bmod.PredictionModel;
import bmod.WattageEvent;
import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.gui.builder.DoubleWidget;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.IntWidget;
import bmod.gui.builder.LongWidget;
import bmod.util.DateTime;

public class FactorBuildingFunction extends BuildingFunction<FactorBuildingFunction>
{
	private static final long DEFAULT_PRIMARY_KEY = -1;
	private final long m_building;
	private final double m_factor;
	private final int m_order;
	
	private static final String TABLE_NAME = "FactorBuildingFunction";
	private static final String[] COLUMN_NAMES = new String[]{"PrimaryKey","BuildingID","Factor","Order"};
	private static final String[] COLUMN_TYPES = new String[]{"BIGINT","BIGINT","DOUBLE","INTEGER"};
	private static final String[] COLUMN_INDEXES = new String[]{"BuildingID"};
	private static final Record<?>[] COLUMN_REFERENCES = new Record<?>[]{null,Database.templateBuilding,null,null};
	
	public FactorBuildingFunction()
	{
		this(DEFAULT_PRIMARY_KEY, 0, 0.0, 0);
	}
	
	public FactorBuildingFunction(long building)
	{
		this(Database.getNewPrimaryKey(), building, 0, 0);
	}

	public FactorBuildingFunction(long pKey, long building, double factor, int order)
	{
		super(TABLE_NAME,
				COLUMN_NAMES,
				COLUMN_TYPES,
				COLUMN_INDEXES,
				COLUMN_REFERENCES,
				pKey,
				order);
		
		m_factor = factor;
		m_building = building;
		m_order = order;
	}

	@Override
	public void addWattageEvents(PredictionModel m, DBWarningsList dw)
	{
		LinkedList<WattageEvent> events = new LinkedList<WattageEvent>();
		
		for(DateTime t : m.getTimeRange()){
			double wattage = m.getTotalWattageAtTime(t);
			double factor = (wattage * m_factor);
			
			events.add(new WattageEvent(t, factor, this, new String[]{}, new String[]{"Factor"}));
		}
		
		m.addEvents(events);
	}

	@Override
	public BuildingFunction<?> createNewForBuilding(long buildingKey)
	{
		return new FactorBuildingFunction(buildingKey).create();
	}

	@Override
	public long getBuildingID()
	{
		return m_building;
	}

	@Override
	protected FactorBuildingFunction getThis()
	{
		return this;
	}

	@Override
	protected String getId()
	{
		if(getPrimaryKey() == DEFAULT_PRIMARY_KEY)
		{
			return "Add a percent to total usage.";
		}
		
		return "Add " + m_factor * 100 + "% to energy usage.";
	}

	@Override
	public Object[] toSQL()
	{
		return new Object[]{
			getPrimaryKey(),
			m_building,
			m_factor,
			m_order
		};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{		
	}

	@Override
	public FactorBuildingFunction fromSQL(Object[] parts) throws IllegalArgumentException
	{
		return new FactorBuildingFunction(
				(Long)parts[0], 
				(Long)parts[1], 
				(Double)parts[2], 
				(Integer)parts[3]);
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		return new GUIBuilderPanel(toSQL(), 
			new LongWidget("PrimaryKey", getPrimaryKey(), false),
			new LongWidget("Building ID", m_building, false),
			new DoubleWidget("Factor", m_factor, true),
			new IntWidget("Order", m_order));
	}

	@Override
	public FactorBuildingFunction createNew(Collection<Record<?>> filterObjects)
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
		return new FactorBuildingFunction(templateBuilding.getPrimaryKey());
	}

}
