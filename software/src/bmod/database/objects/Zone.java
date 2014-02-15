package bmod.database.objects;

import java.util.Collection;

import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.LongWidget;
import bmod.gui.builder.TextFieldWidget;

/**
 * Represents a zone.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class Zone extends SimpleRecord<Zone>
{
	private static final String TABLE_NAME = "Zone";
	private static final String[] COLUMN_NAMES = new String[]{"PrimaryKey", "ZoneName"};
	private static final String[] COLUMN_TYPES = new String[]{"BIGINT", "VARCHAR(100)"};
	private static final String[] COLUMN_INDEXES = new String[]{};
	private static final Record<?>[] COLUMN_REFERENCES = new Record[]{null, null};
	private final String m_zoneName;
	
	public Zone()
	{
		this(Database.getNewPrimaryKey(), "New Zone");
	}
	
	public Zone(String zoneName)
	{
		this(Database.getNewPrimaryKey(), zoneName);
	}
	
	public Zone(long pKey, String zoneName)
	{
		super(TABLE_NAME, 
				COLUMN_NAMES,
				COLUMN_TYPES,
				COLUMN_INDEXES,
				COLUMN_REFERENCES,
				pKey);

		m_zoneName = zoneName;
	}

	@Override
	public Zone fromSQL(Object[] parts) throws IllegalArgumentException
	{
		return new Zone((Long) parts[0], (String) parts[1]);
	}

	@Override
	protected Zone getThis()
	{
		return this;
	}

	@Override
	public String getId()
	{
		return m_zoneName;
	}
	@Override
	public Object[] toSQL()
	{
		return new Object[]{getPrimaryKey(), m_zoneName};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
		//TODO warn on similar zone names? May be confusing...
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		return new GUIBuilderPanel(toSQL(),
					new LongWidget("Primary Key", getPrimaryKey(), false),
					new TextFieldWidget("Zone Name", m_zoneName)
				);
	}
	
	@Override
	public String getUserEditableClass()
	{
		return getClass().getCanonicalName();
	}

	@Override
	public Zone createNew(Collection<Record<?>> filterObjects)
			throws IllegalArgumentException
	{
		return new Zone();
	}
}
