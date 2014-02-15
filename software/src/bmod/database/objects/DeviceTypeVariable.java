package bmod.database.objects;

import java.util.Collection;

import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.gui.builder.DoubleWidget;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.LongWidget;
import bmod.gui.builder.TextFieldWidget;

/**
 * Represents a single variable.
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class DeviceTypeVariable extends SimpleRecord<DeviceTypeVariable>
{
	private static final String TABLE_NAME = "DeviceTypeVariables";
	private static final String[] COLUMN_NAMES = new String[]{"PrimaryKey","Key","Value"};
	private static final String[] COLUMN_TYPES = new String[]{"BIGINT","VARCHAR(100)","DOUBLE"};
	private static final String[] COLUMN_INDEXES = new String[]{"Key"};
	private static final Record<?>[] COLUMN_REFERENCES = new Record<?>[]{null,null,null};


	private final String key;
	private final double value;
	
	public DeviceTypeVariable()
	{
		this(Database.TEMPLATE_PRIMARY_KEY, "", 0.0);
	}
	
	public DeviceTypeVariable(String k, double val)
	{
		this(Database.getNewPrimaryKey(), k, val);
	}
	
	public DeviceTypeVariable(long p, String k, double val)
	{
		super(TABLE_NAME,
				COLUMN_NAMES,
				COLUMN_TYPES,
				COLUMN_INDEXES,
				COLUMN_REFERENCES,
				p);
		
		// Replace whitespace with underscores, replace all non words, and capitalize everything
		key = k.replaceAll("\\s","_").toUpperCase().replaceAll("\\W", "");
		value = val;
	}

	@Override
	public String getId()
	{
		return key;
	}

	@Override
	public Object[] toSQL()
	{
		return new Object[]{getPrimaryKey(), key, value};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
		
	}
	
	public String getKey()
	{
		return key;
	}
	
	public double getValue()
	{
		return value;
	}

	@Override
	public DeviceTypeVariable fromSQL(Object[] parts)
			throws IllegalArgumentException
	{
		return new DeviceTypeVariable((Long) parts[0], (String) parts[1], (Double) parts[2]);
	}

	@Override
	protected DeviceTypeVariable getThis()
	{
		return this;
	}
	
	/**
	 * Gets a simple editor for the DataFeed.
	 * @return
	 */
	@Override
	public GUIBuilderPanel getEditor()
	{
		return new GUIBuilderPanel(toSQL(),
				new LongWidget("Primary Key", getPrimaryKey(), false),
				new TextFieldWidget("Variable Name", key, true),
				new DoubleWidget("Value", value, true));
	}
	
	private static final DeviceTypeVariable instance = new DeviceTypeVariable();
	public static double getVariableValueOrDefault(String name, double default_value)
	{
		try
		{
			return instance.readWhere().eq("Key", name).one().getValue();
		} catch (DatabaseIntegrityException e)
		{
		}
		return default_value;
	}
	
	@Override
	public String getUserEditableClass()
	{
		return this.getClass().getCanonicalName();
	}

	@Override
	public DeviceTypeVariable createNew(Collection<Record<?>> filterObjects)
			throws IllegalArgumentException
	{
		return new DeviceTypeVariable("New Variable", 0.0);
	}
}
