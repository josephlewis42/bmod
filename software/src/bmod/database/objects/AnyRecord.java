package bmod.database.objects;

import java.util.Collection;

import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.gui.builder.GUIBuilderPanel;

/**
 * A basic class that can be used to match anything in filters.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class AnyRecord extends SimpleRecord<AnyRecord>
{
	public static final String DEFAULT_STRING = "--------------------";
	public final String toStringValue;

	public AnyRecord()
	{
		this(DEFAULT_STRING);
	}
	
	public AnyRecord(String name)
	{
		super("AnyRecord", new String[]{"PKey"}, new String[]{"BIGINT"}, new Record<?>[]{null}, Database.TEMPLATE_PRIMARY_KEY);
		
		toStringValue = name;
	}
	
	@Override
	public String toString()
	{
		return toStringValue;
	}

	@Override
	protected AnyRecord getThis()
	{
		return this;
	}

	@Override
	protected String getId()
	{
		return toStringValue;
	}

	@Override
	public Object[] toSQL()
	{
		return null;
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{		
	}

	@Override
	public AnyRecord fromSQL(Object[] parts) throws IllegalArgumentException
	{
		return new AnyRecord();
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		return null;
	}

	@Override
	public String getUserEditableClass()
	{
		return null;
	}

	@Override
	public AnyRecord createNew(Collection<Record<?>> filterObjects)
			throws IllegalArgumentException
	{
		return new AnyRecord();
	}
}
