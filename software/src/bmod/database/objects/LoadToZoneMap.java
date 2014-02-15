package bmod.database.objects;

import java.util.Collection;
import java.util.LinkedList;

import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.gui.builder.CSVRecordWidget;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.LongWidget;

public class LoadToZoneMap extends SimpleRecord<LoadToZoneMap> implements MultiMap<BuildingLoad, Zone>
{
	private static final Record<?>[] COLUMN_REFERENCES = new Record<?>[]{null, Database.templateBuildingLoad, Database.templateZone};
	
	private final long m_loadPrimaryKey;
	private final long m_zonePrimaryKey;
	
	public LoadToZoneMap()
	{
		this(Database.getNewPrimaryKey(), 0L, 0L);
	}
	
	public LoadToZoneMap(long src, long dest)
	{
		this(Database.getNewPrimaryKey(), src, dest);
	}
	
	public LoadToZoneMap(long pkey, long lpk, long zpk)
	{
		super("LoadToZoneMap",
				new String[]{"PrimaryKey", "LoadPrimaryKey", "ZonePrimaryKey"},
				new String[]{"BIGINT","BIGINT","BIGINT"},
				COLUMN_REFERENCES,
				pkey);
		m_loadPrimaryKey = lpk;
		m_zonePrimaryKey = zpk;
	}

	@Override
	public LoadToZoneMap fromSQL(Object[] parts) 
			throws IllegalArgumentException
	{
		return new LoadToZoneMap((Long)parts[0], (Long)parts[1],(Long)parts[2]);
	}

	@Override
	protected LoadToZoneMap getThis()
	{
		return this;
	}

	@Override
	public void updateTable()
	{
	}

	@Override
	public String getId()
	{
		return m_loadPrimaryKey + " -> " + m_zonePrimaryKey;
	}


	@Override
	public Object[] toSQL()
	{
		return new Object[]{getPrimaryKey(), m_loadPrimaryKey, m_zonePrimaryKey};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
		// If zone or load doesn't exist, delete this mapping.	
		try
		{
			Database.templateBuildingLoad.readPrimaryKey(m_loadPrimaryKey);
		} catch (DatabaseIntegrityException e)
		{
			delete();
		}
		
		try
		{
			Database.templateZone.readPrimaryKey(m_zonePrimaryKey);
		} catch (DatabaseIntegrityException e)
		{
			delete();
		}
	}

	@Override
	public Zone[] connectionsFrom(long src)
	{
		LinkedList<Long> zones = new LinkedList<Long>();
		
		for(LoadToZoneMap tmp : readWhere().eq("LoadPrimaryKey", src).all())
			zones.add(tmp.m_zonePrimaryKey);
		
		Collection<Zone> toRet = new Zone().readPrimaryKeys(zones);
		return toRet.toArray(new Zone[toRet.size()]);
	}

	@Override
	public Zone[] possibleConnectionsFrom(long src)
	{
		// All zones possible from everywhere.
		Collection<Zone> toRet = Database.templateZone.readAll();
		return toRet.toArray(new Zone[toRet.size()]);
	}

	@Override
	public BuildingLoad[] connectionsTo(long dest)
	{
		LinkedList<Long> loads = new LinkedList<Long>();
		
		for(LoadToZoneMap tmp : readWhere().eq("ZonePrimaryKey", dest).all())
			loads.add(tmp.m_loadPrimaryKey);
		
		Collection<BuildingLoad> toRet = Database.templateBuildingLoad.readPrimaryKeys(loads);
		return toRet.toArray(new BuildingLoad[toRet.size()]);
	}

	@Override
	public void deleteFrom(long src, long dest)
	{
		for(LoadToZoneMap lzm : readWhere().eq("LoadPrimaryKey", src).eq("ZonePrimaryKey", dest).all())
			lzm.delete();
	}

	@Override
	public void addLink(long src, long dest)
	{
		// Try to fetch an existing link, if it exists, do nothing; otherwise
		// catch the error and make a new one.
		try
		{
			readWhere().eq("LoadPrimaryKey", src).eq("ZonePrimaryKey", dest).one();
		} catch (DatabaseIntegrityException e)
		{
			new LoadToZoneMap(src, dest).create();
		}
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		return new GUIBuilderPanel(toSQL(),
				new LongWidget("Primary Key", getPrimaryKey(), false),
				new CSVRecordWidget("Load", m_loadPrimaryKey, Database.templateBuildingLoad),
				new CSVRecordWidget("Zone", m_zonePrimaryKey, Database.templateZone)
		);
	}
	
	@Override
	public String getUserEditableClass()
	{
		return null;
	}

	@Override
	public LoadToZoneMap createNew(Collection<Record<?>> filterObjects)
			throws IllegalArgumentException
	{
		return new LoadToZoneMap();
	}
}