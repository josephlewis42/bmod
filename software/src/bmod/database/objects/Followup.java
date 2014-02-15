package bmod.database.objects;

import java.util.Collection;

import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.gui.builder.GUIBuilderPanel;

/**
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class Followup extends SimpleRecord<Followup>
{
	private boolean m_followup;
	private final long m_foriegnKey;
	private final String m_srcTable;
	private static final Record<?>[] COLUMN_REFERENCES = new Record<?>[]{null,null,null,null};

	
	public Followup()
	{
		this(Database.getNewPrimaryKey(), -1, "", false);
	}
	
	public Followup(long pKey, long fKey, String srcTable, boolean followup)
	{
		super("Followup", 
				new String[]{"PrimaryKey","ForiegnKey","SourceTableName","ShouldFollowup"},
				new String[]{"BIGINT", "BIGINT", "VARCHAR(100)", "BOOLEAN"},
				COLUMN_REFERENCES,
				pKey);
		
		m_followup = followup;
		m_foriegnKey = fKey;
		m_srcTable = srcTable;
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Followup getThis()
	{
		return this;
	}

	@Override
	protected String getId()
	{
		return "Followup #" + getPrimaryKey() + " for #" + m_foriegnKey + " on " + m_srcTable;
	}

	@Override
	public Object[] toSQL()
	{
		return new Object[]{getPrimaryKey(),
				m_foriegnKey,
				m_srcTable,
				m_followup};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
	}

	@Override
	public Followup fromSQL(Object[] parts) throws IllegalArgumentException
	{
		return new Followup(	(Long) parts[0], 
								(Long) parts[1], 
								(String) parts[2], 
								(Boolean) parts[3]);
	}
	
	/**
	 * Gets the followup for the given CSVRecord.
	 * @param rec
	 * @return
	 */
	public Followup getFollowupFor(Record<?> rec)
	{
		try
		{
			return readWhere().eq("SourceTableName", rec.getTableName()).eq("ForiegnKey", rec.getPrimaryKey()).one();
		} catch (DatabaseIntegrityException e)
		{
			// One does not exist, so create one and return it.
			Followup fup = new Followup(Database.getNewPrimaryKey(), 
					rec.getPrimaryKey(), 
					rec.getTableName(), 
					false);
			
			return fup.create();
		}
	}
	
	public void setFollowup(boolean val)
	{
		m_followup = val;
	}
	
	public boolean getFollowup()
	{
		return m_followup;
	}
	
	@Override
	public String getUserEditableClass()
	{
		return null;
	}

	@Override
	public Followup createNew(Collection<Record<?>> filterObjects)
			throws IllegalArgumentException
	{
		return new Followup();
	}
}
