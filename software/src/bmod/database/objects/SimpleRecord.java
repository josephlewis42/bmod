package bmod.database.objects;



/**
 * A record that simplifies the creation of CSVRecords.
 * 
 * PrimaryKey is always "PrimaryKey"
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 * @param <T>
 */
public abstract class SimpleRecord<T extends SimpleRecord<T>> extends Record<T>
{
	private final String m_tableName;
	private final long m_primaryKey;
	private final String[] m_columnNames;
	private final String[] m_columnTypes;
	private final String m_primaryKeyField;
	private final static String DEFAULT_PRIMARY_KEY_FIELD = "PrimaryKey";
	private final String[] m_indexes;
	private final Record<?>[] m_references;
	
	public SimpleRecord(String tableName, String[] colNames, String[] colTypes, Record<?>[] references, long pKey)
	{
		this(tableName, colNames, colTypes, new String[0], pKey, references, DEFAULT_PRIMARY_KEY_FIELD);
	}
	
	public SimpleRecord(String tableName, String[] colNames, String[] colTypes, String[] indexedKeys, Record<?>[] references, long pKey)
	{
		this(tableName, colNames, colTypes, indexedKeys, pKey, references, DEFAULT_PRIMARY_KEY_FIELD);
	}

	public SimpleRecord(String tableName, String[] colNames, String[] colTypes,
			String[] indexes, long pKey, Record<?>[] references, String pKeyField)
	{
		m_tableName = tableName;
		m_primaryKey = pKey;
		m_columnNames = colNames;
		m_columnTypes = colTypes;
		m_primaryKeyField = pKeyField;
		m_indexes = indexes;
		m_references = references;
	}

	@Override
	public final String[] getColNames()
	{
		return m_columnNames;
	}

	@Override
	public final String[] getSQLColTypes()
	{
		return m_columnTypes;
	}

	@Override
	public final long getPrimaryKey()
	{
		return m_primaryKey;
	}

	@Override
	public final String getPrimaryKeyFieldName()
	{
		return m_primaryKeyField;
	}

	@Override
	public final String getTableName()
	{
		return m_tableName;
	}
	
	@Override
	public void updateTable()
	{
		
	}
	
	
	@Override
	public String[] getIndex()
	{
		return m_indexes;
	}
	
	public static <T extends SimpleRecord<T>> boolean hasOverlap(T[] first, T[] second)
	{
		for(T orig : first)
			for(T sec : second)
				if(orig.getPrimaryKey() == sec.getPrimaryKey())
					return true;
		
		return false;
	}
	
	@Override
	public Record<?>[] getReferences()
	{
		return m_references;
	}
}
