package bmod.database.objects;

import java.util.Collection;

import bmod.database.DBWarningsList;
import bmod.database.DataNotAvailableException;
import bmod.database.Database;
import bmod.database.EditableDataFeed;
import bmod.gui.builder.DoubleWidget;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.LongWidget;
import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;


public class FixedValueDataFeed extends EditableDataFeed<FixedValueDataFeed>
{
	private final double m_value;
	
	public FixedValueDataFeed()
	{
		this(Database.getNewPrimaryKey(), 0.0);
	}
	
	public FixedValueDataFeed(long pKey, double value)
	{
		super("FixedValueDataFeed", 
				new String[]{"PrimaryKey", "Value"}, 
				new String[]{"BIGINT", "DOUBLE"}, 
				new Record<?>[]{null,null},
				pKey,
				"Fixed value data feed: " + value);
		
		m_value = value;
	}

	@Override
	public double getDataAtTime(DateTime t) throws DataNotAvailableException
	{
		return m_value;
	}

	@Override
	public void preCache(DateTimeRange precache)
	{
		// Nothing to do here.
	}

	@Override
	public FixedValueDataFeed getNew()
	{
		return new FixedValueDataFeed(Database.getNewPrimaryKey(), 0);
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		return new GUIBuilderPanel(
				toSQL(), 
				new LongWidget("Primary Key", getPrimaryKey(), false),
				new DoubleWidget("Value", m_value, true));
	}

	@Override
	protected FixedValueDataFeed getThis()
	{
		return this;
	}

	@Override
	protected String getId()
	{
		return "Fixed Value: " + m_value;
	}

	@Override
	public Object[] toSQL()
	{
		return new Object[]{getPrimaryKey(), m_value};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
	}

	@Override
	public FixedValueDataFeed fromSQL(Object[] parts)
			throws IllegalArgumentException
	{
		return new FixedValueDataFeed((Long)parts[0],(Double) parts[1]);
	}

	@Override
	public FixedValueDataFeed createNew(Collection<Record<?>> filterObjects)
			throws IllegalArgumentException
	{
		return new FixedValueDataFeed();
	}

}
