package bmod.database.objects;

import java.util.Collection;
import java.util.LinkedList;

import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.gui.builder.CSVRecordWidget;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.LongWidget;

public class SimpleLightingFunctionToCategoryMap extends SimpleRecord<SimpleLightingFunctionToCategoryMap> implements MultiMap<SimpleLightingFunction, DeviceTypeCategory>
{
	private final long m_srcPrimaryKey;
	private final long m_destPrimaryKey;
	
	private static final String SRC_TITLE = "SimpleLightingFunction";
	private static final String DEST_TITLE = "Category";
	private static final String TABLE_NAME = SRC_TITLE + "To" + DEST_TITLE + "Map";
	private static final SimpleLightingFunction TEMPLATE_SRC = SimpleLightingFunction.TEMPLATE;
	private static final DeviceTypeCategory TEMPLATE_DEST = Database.templateDeviceTypeCategory;

	private static final String[] COLUMN_NAMES = new String[]{"PrimaryKey", "SrcPrimaryKey", "DestPrimaryKey"};
	private static final String[] COLUMN_TYPES = new String[]{"BIGINT","BIGINT","BIGINT"};
	private static final String[] COLUMN_INDEXES = new String[]{"SrcPrimaryKey", "DestPrimaryKey"};
	private static final Record<?>[] COLUMN_REFERENCES = new Record<?>[]{null, SimpleLightingFunction.TEMPLATE, Database.templateDeviceTypeCategory};
	
	public SimpleLightingFunctionToCategoryMap()
	{
		this(Database.getNewPrimaryKey(), 0L, 0L);
	}
	
	public SimpleLightingFunctionToCategoryMap(long src, long dest)
	{
		this(Database.getNewPrimaryKey(), src, dest);
	}
	
	public SimpleLightingFunctionToCategoryMap(long pkey, long lpk, long zpk)
	{
		super(TABLE_NAME,
				COLUMN_NAMES,
				COLUMN_TYPES,
				COLUMN_INDEXES,
				COLUMN_REFERENCES,
				pkey);
		m_srcPrimaryKey = lpk;
		m_destPrimaryKey = zpk;
	}

	@Override
	public SimpleLightingFunctionToCategoryMap fromSQL(Object[] parts) 
			throws IllegalArgumentException
	{
		return new SimpleLightingFunctionToCategoryMap((Long)parts[0], (Long)parts[1],(Long)parts[2]);
	}

	@Override
	protected SimpleLightingFunctionToCategoryMap getThis()
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
		return m_srcPrimaryKey + " -> " + m_destPrimaryKey;
	}


	@Override
	public Object[] toSQL()
	{
		return new Object[]{getPrimaryKey(), m_srcPrimaryKey, m_destPrimaryKey};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
		// If zone or load doesn't exist, delete this mapping.	
		try
		{
			TEMPLATE_SRC.readPrimaryKey(m_srcPrimaryKey);
		} catch (DatabaseIntegrityException e)
		{
			delete();
		}
		
		try
		{
			TEMPLATE_DEST.readPrimaryKey(m_destPrimaryKey);
		} catch (DatabaseIntegrityException e)
		{
			delete();
		}
	}

	@Override
	public DeviceTypeCategory[] connectionsFrom(long src)
	{
		LinkedList<Long> zones = new LinkedList<Long>();
		
		for(SimpleLightingFunctionToCategoryMap tmp : readWhere().eq("SrcPrimaryKey", src).all())
			zones.add(tmp.m_destPrimaryKey);
		
		Collection<DeviceTypeCategory> toRet = TEMPLATE_DEST.readPrimaryKeys(zones);
		return toRet.toArray(new DeviceTypeCategory[toRet.size()]);
	}

	@Override
	public DeviceTypeCategory[] possibleConnectionsFrom(long src)
	{
		// All zones possible from everywhere.
		Collection<DeviceTypeCategory> toRet = TEMPLATE_DEST.readAll();
		return toRet.toArray(new DeviceTypeCategory[toRet.size()]);
	}

	@Override
	public SimpleLightingFunction[] connectionsTo(long dest)
	{
		LinkedList<Long> loads = new LinkedList<Long>();
		
		for(SimpleLightingFunctionToCategoryMap tmp : readWhere().eq("DestPrimaryKey", dest).all())
			loads.add(tmp.m_srcPrimaryKey);
		
		Collection<SimpleLightingFunction> toRet = TEMPLATE_SRC.readPrimaryKeys(loads);
		return toRet.toArray(new SimpleLightingFunction[toRet.size()]);
	}

	@Override
	public void deleteFrom(long src, long dest)
	{
		for(SimpleLightingFunctionToCategoryMap lzm : readWhere().eq("SrcPrimaryKey", src).eq("DestPrimaryKey", dest).all())
			lzm.delete();
	}

	@Override
	public void addLink(long src, long dest)
	{
		// Try to fetch an existing link, if it exists, do nothing; otherwise
		// catch the error and make a new one.
		try
		{
			readWhere().eq("SrcPrimaryKey", src).eq("DestPrimaryKey", dest).one();
		} catch (DatabaseIntegrityException e)
		{
			new SimpleLightingFunctionToCategoryMap(src, dest).create();
		}
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		return new GUIBuilderPanel(toSQL(),
				new LongWidget("Primary Key", getPrimaryKey(), false),
				new CSVRecordWidget(SRC_TITLE, m_srcPrimaryKey, TEMPLATE_SRC),
				new CSVRecordWidget(DEST_TITLE, m_destPrimaryKey, TEMPLATE_DEST)
		);
	}
	
	@Override
	public String getUserEditableClass()
	{
		return null;
	}

	@Override
	public SimpleLightingFunctionToCategoryMap createNew(
			Collection<Record<?>> filterObjects)
			throws IllegalArgumentException
	{
		return new SimpleLightingFunctionToCategoryMap();
	}
}