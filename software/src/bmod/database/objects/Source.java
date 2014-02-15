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
public class Source extends SimpleRecord<Source>
{
	private static final String TABLE_NAME = "Source";
	private static final String[] COLUMN_NAMES = new String[]{"PrimaryKey","ForiegnKey","SourceTableName","SourceInfo"};
	private static final String[] COLUMN_TYPES = new String[]{"BIGINT", "BIGINT", "VARCHAR(100)", "VARCHAR(1000)"};
	private static final Record<?>[] COLUMN_REFERENCES = new Record<?>[]{null,null,null,null};

	private String m_sourceText;
	private final long m_foriegnKey;
	private final String m_srcTable;
	
	public Source()
	{
		this(Database.getNewPrimaryKey(), -1, "", "");
	}
	
	public Source(long pKey, long fKey, String srcTable, String source)
	{
		super(TABLE_NAME, 
				COLUMN_NAMES,
				COLUMN_TYPES,
				COLUMN_REFERENCES,
				pKey);
		
		m_sourceText = source;
		m_foriegnKey = fKey;
		m_srcTable = srcTable;
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		return null;
	}

	@Override
	protected Source getThis()
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
				m_sourceText};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
	}

	@Override
	public Source fromSQL(Object[] parts) throws IllegalArgumentException
	{
		return new Source(	(Long) parts[0], 
								(Long) parts[1], 
								(String) parts[2], 
								(String) parts[3]);
	}
	
	/**
	 * Gets the followup for the given CSVRecord.
	 * @param rec
	 * @return
	 */
	public Source getSourceFor(SimpleRecord<?> rec)
	{
		try
		{
			return readWhere().eq("SourceTableName", rec.getTableName()).eq("ForiegnKey", rec.getPrimaryKey()).one();
		} catch (DatabaseIntegrityException e)
		{
			// One does not exist, so create one and return it.
			Source fup = new Source(Database.getNewPrimaryKey(), 
					rec.getPrimaryKey(), 
					rec.getTableName(), 
					"");
			
			return fup.create();
		}
	}
	
	public void setSource(String val)
	{
		m_sourceText = val;
	}
	
	public String getSource()
	{
		return m_sourceText;
	}
	
	@Override
	public String getUserEditableClass()
	{
		return null;
	}

	@Override
	public Source createNew(Collection<Record<?>> filterObjects)
			throws IllegalArgumentException
	{
		return new Source();
	}
}
