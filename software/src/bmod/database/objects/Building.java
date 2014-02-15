package bmod.database.objects;

import java.util.Collection;

import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.LongWidget;
import bmod.gui.builder.TextFieldWidget;
import bmod.util.DateTime;

public class Building extends SimpleRecord<Building>
{
	private final String m_id;
	
	private static final String TABLE_NAME = "Buildings";
	private static final String[] COLUMN_NAMES = new String[]{"BuildingName","PrimaryKey"};
	private static final String[] COLUMN_TYPES = new String[]{"VARCHAR(100)","BIGINT"};
	private static final String[] COLUMN_INDEXES = new String[]{"BuildingName"};
	private static final Record<?>[] COLUMN_REFERENCES = new Record<?>[]{null,null};
	
	public Building()
	{
		this("Unnamed Building", Database.getNewPrimaryKey());
	}
	
	public Building(String id)
	{
		this(id, Database.getNewPrimaryKey());
	}
	
	public Building(String id, long pkey)
	{
		super(TABLE_NAME,
				COLUMN_NAMES, 
				COLUMN_TYPES, 
				COLUMN_INDEXES,
				COLUMN_REFERENCES,
				pkey);
		
		if(id == null){
			throw new IllegalArgumentException("'id' cannot be null.");
		}
		this.m_id = id;
	}
	
	
	/**
	 * Returns the name of this building.
	 */
	@Override
	public String getId()
	{
		return m_id;
	}

	@Override
	public Object[] toSQL() {
		return new Object[]{m_id, getPrimaryKey()};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
		if(Database.templateBuildingActivity.readBuildingDep(getPrimaryKey()).size() == 0)
			list.addWarning("Warning: " + this + " has no activities");
	}

	@Override
	public Building fromSQL(Object[] parts) throws IllegalArgumentException
	{
		if(parts == null ||  parts.length != getColNames().length)
			throw new IllegalArgumentException();
		
		return new Building((String) parts[0], (Long) parts[1]);
	}
	
	public static Building getBuilding(String building)
	{
		return new Building(building, new DateTime().getTime());
	}

	@Override
	protected Building getThis()
	{
		return this;
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		return new GUIBuilderPanel(toSQL(),
				new TextFieldWidget("Name", m_id, false),
				new LongWidget("Primary Key", getPrimaryKey(), false)
		);
	}

	
	
	// Convenience Methods
	
	/**
	 * Gets a building name by the given ID, or returns the ID if not found.
	 * 
	 * @param primaryKey - The key of the building to fetch the name for.
	 * @return The name of the building, or the ID if no building with the given
	 * key exists
	 */
	public static String getNameByPkey(long primaryKey)
	{
		try
		{
			return Database.templateBuilding.readPrimaryKey(primaryKey).toString();
		} catch (DatabaseIntegrityException e)
		{
			return "ID#" + primaryKey;
		}
	}

	/**
	 * Returns the (first) building object found with the given name.
	 * 
	 * @param building - The name of the building to fetch.
	 * @return A <Building> object.
	 * @throws DatabaseIntegrityException - If no such building exists.
	 */
	public static Building getBuildingByName(String building) throws DatabaseIntegrityException
	{
		return Database.templateBuilding.readWhere().eq("BuildingName", building).one();
	}

	@Override
	public String getUserEditableClass()
	{
		return this.getClass().getCanonicalName();
	}

	@Override
	public Building createNew(Collection<Record<?>> filterOsbjects)
	{
		return new Building();
	}
}
