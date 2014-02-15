package bmod.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import bmod.database.objects.Record;
import bmod.util.DateTime;

/**
 * Generates SQL queries using records. Cloned shamelessly from Django.
 * 
 * To generate a query on an object, something like this would happen.
 * 
 * Person.readWhere().eq("FIRST_NAME","Joe").lt("AGE",35).all();
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 */
public class QueryGenerator<T extends Record<T>>
{
	private final T m_instance;
	
	private final LinkedList<Object> m_params = new LinkedList<Object>();
	private final LinkedList<String> m_types = new LinkedList<String>();
	private final LinkedList<String> m_fields = new LinkedList<String>();
	private final LinkedList<String> m_operators = new LinkedList<String>();
	private boolean m_delete = false;
	
	public QueryGenerator(T instance)
	{
		m_instance = instance;
	}
	
	private QueryGenerator<T> addToStack(String field, String type, Object value, String operator)
	{
		m_types.add(type);
		m_fields.add(field);
		m_params.add(value);
		m_operators.add(operator);
		
		return this;
	}
	
	/**
	 * Returns a raw SQL statement without params introduced yet.
	 */
	public String getRawStatement(int limit)
	{
		StringBuilder query = new StringBuilder();
		
		if(m_delete)
		{
			query.append("DELETE FROM \"");
		}else{
			query.append("SELECT * FROM \"");
		}
		query.append(m_instance.getTableName());
		query.append("\"");

		
		for(int i = 0; i < m_params.size(); i++)
		{
			if(i == 0)
				query.append(" WHERE ");
			else
				query.append(" AND ");
			
			query.append("\"" + m_fields.get(i) + "\" " + m_operators.get(i) + " ? ");
		}
		
		if(! m_delete && limit > 0)
			query.append(" LIMIT " + limit);
		
		return query.toString();
	}
	
	public void checkIntegrity() throws IllegalArgumentException
	{
		String[] cols = m_instance.getColNames();
		String[] types = m_instance.getSQLColTypes();
		
		StringBuilder toReturn = new StringBuilder("");
				
		for(int field = 0; field < m_fields.size(); field++)
		{
			boolean found = false;
			
			for(int i = 0; i < cols.length; i++)
			{
				if(cols[i].equals(m_fields.get(field)) &&
				   types[i].startsWith(m_types.get(field)))
				   found = true;
			}
			
			if(found == false)
				toReturn.append("Field: " + m_fields.get(field) + " with type: " + m_types.get(field) + " not found. ");
		}
		
		if(!toReturn.toString().equals(""))
			throw new IllegalArgumentException(toReturn.toString());
	}
	
	public String getUUID()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getRawStatement(-1));
		for(Object param : m_params)
		{
			sb.append("|");
			sb.append(param);
		}
		return sb.toString();
	}
	
	
	public PreparedStatement getQuery(int limit)
	{

		checkIntegrity();
		
		try
		{
			PreparedStatement stmt = Database.getDqm().getPreparedStatement(getRawStatement(limit));

			// Set all of the params
			for(int i = 1; i <= m_params.size(); i++)
			{
				Object param = m_params.get(i - 1);
				if(param instanceof Double)
					stmt.setDouble(i, (Double) param);
				else if(param instanceof String)
					stmt.setString(i, (String) param);
				else if(param instanceof Integer)
					stmt.setInt(i, (Integer) param);
				else if(param instanceof Boolean)
					stmt.setBoolean(i, (Boolean) param);
				else if(param instanceof Long)
					stmt.setLong(i, (Long) param);
				else if(param instanceof Character)
					stmt.setString(i, param + "");
				else if(param instanceof DateTime)
					stmt.setString(i, ((DateTime) param).toISODate());
				else
					stmt.setObject(i, m_params.get(i - 1));
			}
			return stmt;
		}catch(SQLException ex)
		{
			throw new IllegalArgumentException(ex);
		}
	}
	
	
	/**
	 * Returns the first result of a query, if the query resulted in no objects
	 * a DatabaseIntegrityException is thrown.
	 * 
	 * @return
	 * @throws DatabaseIntegrityException -- if no objects are found
	 */
	private static CRUDCache<Record<?>> oneCache = new CRUDCache<Record<?>>();
	
	public T one() throws DatabaseIntegrityException
	{
		String uuid = getUUID();
		@SuppressWarnings("unchecked")
		T tmp = (T) oneCache.get(uuid);
		
		if(tmp != null)
			return tmp;
				
		for(T tmp2 : Database.getDqm().readTable(getQuery(1), m_instance, uuid))
		{
			oneCache.add(tmp2, uuid);
			return tmp2;
		}
		
		throw new DatabaseIntegrityException("No object found! " + uuid);
	}
	
	public void delete()
	{
		m_delete = true;
		try
		{
			getQuery(-1).execute();
			Database.getDqm().updateCommitNumber();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public Collection<T> none()
	{
		return new LinkedList<T>();
	}
	
	public Collection<T> all()
	{
		return Database.getDqm().readTable(getQuery(-1), m_instance, getUUID());
	}
	
	public QueryGenerator<T> eq(String field, double toEq)
	{
		return addToStack(field, "DOUBLE", toEq, "=");
	}
	
	public QueryGenerator<T> eq(String field, long toEq)
	{
		return addToStack(field, "BIGINT", toEq, "=");
	}
	
	public QueryGenerator<T> eq(String field, String toEq)
	{
		return addToStack(field, "VARCHAR", toEq, "=");
	}
	
	public QueryGenerator<T> eq(String field, boolean toEq)
	{
		return addToStack(field, "BOOLEAN", toEq, "=");
	}
	
	public QueryGenerator<T> eq(String field, int toEq)
	{
		return addToStack(field, "INT", toEq, "=");
	}
	
	public QueryGenerator<T> eq(String field, char toEq)
	{
		return addToStack(field, "CHAR", toEq, "=");
	}
	
	public QueryGenerator<T> eq(String field, DateTime toEq)
	{
		return addToStack(field, "DATETIME", toEq, "=");
	}
	
	public QueryGenerator<T> lt(String field, double tolt)
	{
		return addToStack(field, "DOUBLE", tolt, "<");
	}
	
	public QueryGenerator<T> lt(String field, long tolt)
	{
		return addToStack(field, "BIGINT", tolt, "<");
	}
	
	public QueryGenerator<T> lt(String field, String tolt)
	{
		return addToStack(field, "VARCHAR", tolt, "<");
	}
	
	public QueryGenerator<T> lt(String field, boolean tolt)
	{
		return addToStack(field, "BOOLEAN", tolt, "<");
	}
	
	public QueryGenerator<T> lt(String field, int tolt)
	{
		return addToStack(field, "INT", tolt, "<");
	}
	
	public QueryGenerator<T> lt(String field, char tolt)
	{
		return addToStack(field, "CHAR", tolt, "<");
	}
	
	public QueryGenerator<T> lt(String field, DateTime toEq)
	{
		return addToStack(field, "DATETIME", toEq, "<");
	}
	
	public QueryGenerator<T> gt(String field, double togt)
	{
		return addToStack(field, "DOUBLE", togt, ">");
	}
	
	public QueryGenerator<T> gt(String field, long togt)
	{
		return addToStack(field, "BIGINT", togt, ">");
	}
	
	public QueryGenerator<T> gt(String field, String togt)
	{
		return addToStack(field, "VARCHAR", togt, ">");
	}
	
	public QueryGenerator<T> gt(String field, boolean togt)
	{
		return addToStack(field, "BOOLEAN", togt, ">");
	}
	
	public QueryGenerator<T> gt(String field, int togt)
	{
		return addToStack(field, "INT", togt, ">");
	}
	
	public QueryGenerator<T> gt(String field, char togt)
	{
		return addToStack(field, "CHAR", togt, ">");
	}
	
	public QueryGenerator<T> gt(String field, DateTime toEq)
	{
		return addToStack(field, "DATETIME", toEq, ">");
	}
	
	public QueryGenerator<T> neq(String field, double toneq)
	{
		return addToStack(field, "DOUBLE", toneq, "<>");
	}
	
	public QueryGenerator<T> neq(String field, long toneq)
	{
		return addToStack(field, "BIGINT", toneq, "<>");
	}
	
	public QueryGenerator<T> neq(String field, String toneq)
	{
		return addToStack(field, "VARCHAR", toneq, "<>");
	}
	
	public QueryGenerator<T> neq(String field, boolean toneq)
	{
		return addToStack(field, "BOOLEAN", toneq, "<>");
	}
	
	public QueryGenerator<T> neq(String field, int toneq)
	{
		return addToStack(field, "INT", toneq, "<>");
	}
	
	public QueryGenerator<T> neq(String field, char toneq)
	{
		return addToStack(field, "CHAR", toneq, "<>");
	}
	
	public QueryGenerator<T> neq(String field, DateTime toEq)
	{
		return addToStack(field, "DATETIME", toEq, "<>");
	}
}
