package bmod.database.objects;

import java.util.Collection;

import bmod.database.DBWarningsList;
import bmod.database.DataNotAvailableException;
import bmod.database.Database;
import bmod.database.EditableDataFeed;
import bmod.gui.builder.CSVRecordWidget;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.LongWidget;
import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;
import bmod.util.TimeDelta;

public class ZoneInUseDataFeed extends EditableDataFeed<ZoneInUseDataFeed>
{
	final long m_zonePrimaryKey;
	final long m_buildingPrimaryKey;
	final long m_timeShiftMin;

	public ZoneInUseDataFeed()
	{
		this(Database.getNewPrimaryKey(), -1, -1, 0);
	}
	
	public ZoneInUseDataFeed(long pKey, long zonePkey, long buildingPkey, long timeshift)
	{
		super("ZoneInUseDataFeed", 
			new String[]{"PrimaryKey", "ZonePrimaryKey", "BuildingPrimaryKey", "TimeShiftMin"}, 
			new String[]{"BIGINT", "BIGINT", "BIGINT","BIGINT"},
			new Record<?>[]{null, Database.templateZone, Database.templateBuilding, null},
			pKey,
			"Zone in use data feed for zone id: " + zonePkey);	
	
		m_zonePrimaryKey = zonePkey;
		m_buildingPrimaryKey = buildingPkey;
		m_timeShiftMin = timeshift;
	}

	@Override
	public double getDataAtTime(DateTime t) throws DataNotAvailableException
	{
		t = t.plusTime(new TimeDelta(0,0,m_timeShiftMin));
		
		// Lookup building activities at time t
		for(BuildingActivity ba : Database.templateBuildingActivity.readBuildingDep(m_buildingPrimaryKey))
		{
			if(ba.isFunction() || ! ba.happensWithin(t, t))
				continue;
			
			// Lookup loads those use
			for(BuildingLoad l : Database.templateBuildingLoad.readLoadsForActivity(ba))
				for(Zone z : l.getZones())
					if(z.getPrimaryKey() == m_zonePrimaryKey)
						return 1;				
		}
		
		return 0;
	}

	@Override
	public void preCache(DateTimeRange precache)
	{
		// Nothing to do here, for now at least.
	}

	@Override
	public ZoneInUseDataFeed getNew()
	{
		return new ZoneInUseDataFeed();
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		return new GUIBuilderPanel(toSQL(), 
				new LongWidget("Primary Key", getPrimaryKey(), false),
				new CSVRecordWidget("Zone", m_zonePrimaryKey, new Zone()),
				new CSVRecordWidget("Building", m_buildingPrimaryKey, new Building()),
				new LongWidget("Time Shift (in min)", m_timeShiftMin, true));
	}

	@Override
	protected ZoneInUseDataFeed getThis()
	{
		return this;
	}

	@Override
	protected String getId()
	{
		return "Zone In Use: " + m_zonePrimaryKey;
	}

	@Override
	public Object[] toSQL()
	{
		return new Object[]{getPrimaryKey(), m_zonePrimaryKey};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
	}

	@Override
	public ZoneInUseDataFeed fromSQL(Object[] parts)
			throws IllegalArgumentException
	{
		return new ZoneInUseDataFeed((Long) parts[0], (Long) parts[1], (Long) parts[2], (Long) parts[3]);
	}

	@Override
	public ZoneInUseDataFeed createNew(Collection<Record<?>> filterObjects)
			throws IllegalArgumentException
	{
		return new ZoneInUseDataFeed();
	}
}