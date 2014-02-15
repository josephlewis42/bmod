package bmod.database.objects;

import java.util.Collection;

import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.LongWidget;
import bmod.gui.builder.TextFieldWidget;

/**
 * Represents a Category device types can be placed in.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class DeviceTypeCategory extends SimpleRecord<DeviceTypeCategory>
{
	private static final String TABLE_NAME = "DeviceTypeCategory";
	private static final String[] COLUMN_NAMES = new String[]{"PrimaryKey", "CategoryName"};
	private static final String[] COLUMN_TYPES = new String[]{"BIGINT", "VARCHAR(100)"};
	private static final String[] COLUMN_INDEXES = new String[]{};
	private static final Record<?>[] COLUMN_REFERENCES = new Record<?>[]{null,null};
	private String m_categoryName;
	
	
	public DeviceTypeCategory()
	{
		this(Database.getNewPrimaryKey(), "");
	}
	
	public DeviceTypeCategory(String categoryName)
	{
		this(Database.getNewPrimaryKey(), categoryName);
	}
	
	public DeviceTypeCategory(long pKey, String categoryName)
	{
		super(TABLE_NAME, 
				COLUMN_NAMES,
				COLUMN_TYPES,
				COLUMN_INDEXES,
				COLUMN_REFERENCES,
				pKey);

		m_categoryName = categoryName;
	}

	@Override
	public DeviceTypeCategory fromSQL(Object[] parts) throws IllegalArgumentException
	{
		return new DeviceTypeCategory((Long) parts[0], (String) parts[1]);
	}

	@Override
	protected DeviceTypeCategory getThis()
	{
		return this;
	}

	@Override
	public String getId()
	{
		return m_categoryName;
	}
	@Override
	public Object[] toSQL()
	{
		return new Object[]{getPrimaryKey(), m_categoryName};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
		//TODO warn on similar category names? May be confusing...
	}
	
	/**
	 * Sets the category name to be what was requested.
	 */
	public void setCategoryName(String s)
	{
		m_categoryName = s;
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		return new GUIBuilderPanel(toSQL(),
					new LongWidget("Primary Key", getPrimaryKey(), false),
					new TextFieldWidget("Category Name", m_categoryName)
				);
	}
	
	@Override
	public String getUserEditableClass()
	{
		return null;
	}

	@Override
	public DeviceTypeCategory createNew(Collection<Record<?>> filterObjects)
			throws IllegalArgumentException
	{
		return new DeviceTypeCategory();
	}
}
