package bmod.database.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.database.QueryGenerator;
import bmod.gui.builder.GUIBuilderPanel;

public abstract class Record<T extends Record<T>> implements Comparable<Record<?>>
{
	
	// SQL types
	public static final String BIGINT = "BIGINT";
	public static final String INT = "INT";
	public static final String BOOL = "BOOLEAN";
	public static final String VARCHAR_100 = "VARCHAR(100)";
	public static final String VARCHAR_1000 = "VARCHAR(1000)";
	public static final String VARCHAR_5000 = "VARCHAR(5000)";
	
	/**
	 * Deletes the current record from the current data store.
	 * @throws DatabaseIntegrityException
	 */
	public final void delete()
	{
		new QueryGenerator<T>(getThis()).eq(getPrimaryKeyFieldName(), getPrimaryKey()).delete();
	}
	
	/**
	 * Updates the current record in the current data store. Returns the caller.
	 * 
	 * @throws DatabaseIntegrityException
	 */
	public final T update()
	{
		delete();
		create();
		return getThis();
	}
	
	/**
	 * Adds the current record to the current data store.
	 * 
	 * @return the record you created
	 * @throws DatabaseIntegrityException
	 */
	public final T create()
	{
		Database.getDqm().createRecord(this);
		return getThis();
	}
	
	/**
	 * Reads all of the records of this type from the current data store.
	 * @return A collection of all items of this type.
	 * @throws DatabaseIntegrityException
	 */
	public Collection<T> readAll()
	{
		return readWhere().all();
	}
	
	/**
	 * Reads a series of items with filters applied.
	 * 
	 * @param filters - instances of objects whose primary keys are meant to 
	 * constrain the results.
	 * 
	 * @return
	 */
	public Collection<T> readFiltered(Collection<Record<?>> filters)
	{
		QueryGenerator<T> qg = readWhere();
		
		String[] colNames = getColNames();
		Record<?>[] colConnections = getReferences();
		
		for(Record<?> filter : filters)
		{
			for(int i = 0; i < colNames.length; i++)
			{
				if(	colConnections[i] != null && 
					colConnections[i].getTableName().equals(filter.getTableName()))
				{
					qg = qg.eq(colNames[i], filter.getPrimaryKey());
				}
			}
		}
		
		return qg.all();
	}
	
	
	/**
	 * Reads the record of this type where the primary key is the one given from
	 * the current data store. If no object exists with the given primary key
	 * an error is thrown.
	 * 
	 * @return The item with the given primary key.
	 * @throws DatabaseIntegrityException
	 */
	public final T readPrimaryKey(long pKey) throws DatabaseIntegrityException
	{
		return readWhere().eq(getPrimaryKeyFieldName(), pKey).one();
	}
	
	/**
	 * Returns an instance of the current class, so the super class can do
	 * database reading magic without reverting to nasty Java spaghetti things.
	 */
	protected abstract T getThis();
	
	public QueryGenerator<T> readWhere()
	{
		return new QueryGenerator<T>(getThis());
	}
	
	public QueryGenerator<T> where()
	{
		return new QueryGenerator<T>(getThis());
	}
	
	
	/**
	 * Asserts that both records are of the same class and they have the same
	 * primary key.
	 * 
	 * @param oth - the record to test this one against.
	 * @return
	 */
	public final boolean equals(Record<?> oth)
	{
		if(oth == null)
			return false;
		
		if(! (oth.getClass().getCanonicalName().equals(getThis().getClass().getCanonicalName())))
			return false;
		
		return oth.getPrimaryKey() == getPrimaryKey();
	}
	
	@Override
	public int hashCode(){
		return (int) getPrimaryKey();
	}
	
	public abstract void updateTable();
	
	
	/**
	 * Reads a set of records from the given list of primary keys for the 
	 * items.
	 * 
	 * Ignores keys that can't be found.
	 * 
	 * @param items - A list of items to get.
	 * @return items matching the primary keys asked for; not gauranteed to 
	 * contain all items. Duplicates will also be omitted.
	 */
	public Collection<T> readPrimaryKeys(Collection<Long> items)
	{
		LinkedList<Long> done = new LinkedList<Long>();
		LinkedList<T> output = new LinkedList<T>();
		
		for(Long item : items)
		{
			if(done.contains(item))
				continue;
			
			try
			{
				output.add(readPrimaryKey(item));
			} catch (DatabaseIntegrityException e)
			{
			}
		}
		
		return output;
	}
	
	public Collection<T> readPrimaryKeys(long[] items)
	{
		ArrayList<Long> itemslist = new ArrayList<Long>(items.length);
		return readPrimaryKeys(itemslist);
	}
	
	/**
	 * Returns a human readable ID for the record, not gauranteed to be unique.
	 * e.g. for buildings, this could be the building name.
	 * @return
	 */
	protected abstract String getId();
	
	/**
	 * Gets the names of the columns expected in the SQL database.
	 */
	public abstract String[] getColNames();
	
	/**
	 * Gets the types of the columns found by getColNames() as SQL database
	 * types.
	 */
	public abstract String[] getSQLColTypes();
	
	/**
	 * Returns a list of objects for a SQL table to 
	 * look up, do not return anything other than
	 * built-in types here, String, Long, Double,
	 * Float, Boolean, etc. Only Strings will be
	 * quoted when they are inserted in to SQL so
	 * if something needs to be quoted, it needs to be
	 * in the object list as a string.
	 * @return
	 */
	public abstract Object[] toSQL();
	
	public abstract void reportIntegrityErrors(DBWarningsList list);
	
	@Override
	public String toString()
	{
		if(getPrimaryKey() == Database.TEMPLATE_PRIMARY_KEY)
		{
			return "[ " + getHumanReadableClassName() + " ]";
		}
		
		return getId();
	}
	
	/**
	 * Returns the primary key for this object.
	 * 
	 * @return The unique key representing this object.
	 */
	public abstract long getPrimaryKey();
	
	/**
	 * Returns the SQL column name of the primary key.
	 * @return
	 */
	public abstract String getPrimaryKeyFieldName();

	
	/**
	 * Comparable if the toStrings match, case insensitive.
	 */
	@Override
	public int compareTo(Record<?> o)
	{
		if(this.getPrimaryKey() == Database.TEMPLATE_PRIMARY_KEY)
		{
			return -1;
		}
		
		return toString().toUpperCase().compareTo(o.toString().toUpperCase());
	}
	

	
	/**
	 * Returns the name of the table that should represent this
	 * record.
	 * 
	 * @return
	 */
	abstract public String getTableName();
	
	/**
	 * Returns a table definition for a sql table:
	 * i.e.
	 * CREATE TABLE blah ("hello world" VARCHAR(40), "nothing" INT)
	 */
	public final String getSQLTableDefn()
	{
		String[] colnames = getColNames();
		String[] coltypes = getSQLColTypes();
		String defn = null;
		
		for(int i = 0; i < colnames.length; i++)
		{
			if(defn == null)
				defn = "CREATE TABLE \""+getTableName()+"\" (";
			else
				defn += ", ";
			defn += "\"" + colnames[i] + "\" "+coltypes[i];
			if(colnames[i].equals(getPrimaryKeyFieldName()))
				defn += " PRIMARY KEY";
		}
		return defn + ")";
	}
	
	/**
	 * The factory for the Record.
	 * 
	 * @param parts - A list of Objects coming from SQL
	 * @return A new T representing the deserialized object from the parts.
	 * @throws IllegalArgumentException - If one of the parts is wrong.
	 */
	public abstract T fromSQL(Object[] parts) throws IllegalArgumentException;
	
	public abstract String[] getIndex();
	
	/**
	 * Returns the index definition for the table, or null if there isn't one
	 * defined.
	 */
	public String getIndexDefn()
	{
		if(getIndex() == null || getIndex().length == 0)
			return null; // nothing to create
		
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE INDEX \"");
		
		for(String in : getIndex())
			sb.append(in);
		
		sb.append("\" ON \"");
		sb.append(getTableName());
		sb.append("\" (");
		
		boolean first = true;
		for(String col : getIndex())
		{
			if(first)
				first = false;
			else
				sb.append(", ");
			
			sb.append("\"");
			sb.append(col);
			sb.append("\"");
		}
		
		sb.append(")");
		
		return sb.toString();
	}
	
	public abstract GUIBuilderPanel getEditor();

	/**
	 * Returns the objects that this records of this type
	 * can be filtered by. i.e. those that they contain
	 * references to, will not contain null records.
	 * 
	 * @return
	 */
	public final List<Record<?>> getFilters()
	{
		LinkedList<Record<?>> filters = new LinkedList<>();
		
		for(Record<?> r : getReferences())
		{
			if(r != null)
				filters.add(r);
		}
		
		return filters;
	}
	
	/**
	 * Checks whether or not this record type can be filtered by the given
	 * filters.
	 * 
	 * @param filters - the filters to compare against.
	 * @return True if this record type can be, False if it can not be
	 */
	public final boolean canBeFilteredBy(Collection<Record<?>> filters)
	{
		HashSet<String> refstr = new HashSet<>();
		for(Record<?> othRef : filters)
		{
			if(othRef.getPrimaryKey() != Database.TEMPLATE_PRIMARY_KEY)
			{
				refstr.add(othRef.getTableName());
			}
		}
		
		for(Record<?> myRef : getFilters())
		{
			if(refstr.contains(myRef.getTableName()))
			{
				return true;
			}
		}
		
		return false;
	}

	
	/**
	 * Returns the remote tables this object references.
	 * @return
	 */
	public abstract Record<?>[] getReferences();
	
	/**
	 * Checks to see if all of the references for this record can be satisfied
	 * by the records given (i.e. you can create a new object from just these).
	 * 
	 * @param refs
	 * @return
	 */
	public final boolean areReferencesSatisfied(final Collection<Record<?>> refs)
	{
		HashSet<String> refstr = new HashSet<>();
		for(Record<?> othRef : refs)
		{
			refstr.add(othRef.getTableName());
		}
		
		for(Record<?> myRef : getFilters())
		{
			if(!refstr.contains(myRef.getTableName()))
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Sorts the collection based upon the references required by each record.
	 */
	public static final void sortByReferenceDependencies(List<Record<?>> refs)
	{
		LinkedList<Record<?>> toPlace = new LinkedList<>(refs);
		refs.clear();
		
		while(toPlace.size() > 0)
		{
			boolean set = false;
			for(Record<?> tmp : toPlace)
			{
				if(tmp.areReferencesSatisfied(refs))
				{
					set = true;
					refs.add(tmp);
				}
			}
			
			if(set == false)
			{
				refs.add(toPlace.pop());
			}
			
			toPlace.removeAll(refs);
		}
	}
	
	
		
	/**
	 * Returns the group for which this CSV Record should be shown in,
	 * or NULL if it should not be shown.
	 * 
	 * @return
	 */
	public abstract String getUserEditableClass();

	/**
	 * Creates a new instance of this type, given instances of all the filters
	 * listed in getFilters()
	 * 
	 * @param filterObjects - the default objects to create this record with.
	 * @return a new instance of this record, will not be saved yet.
	 * @throws IllegalArgumentException - if the filterObjects are not complete.
	 */
	public abstract T createNew(Collection<Record<?>> filterObjects) throws IllegalArgumentException;
	
	
	/**
	 * Returns a human version of the user editable class name.
	 * @return
	 */
	public final String getHumanReadableUserEditableClassName()
	{
		return canonicalNameToHumanReadable(getUserEditableClass());
	}
	
	
	/**
	 * Returns a more human way of representing this class.
	 * @return
	 */
	public final String getHumanReadableClassName()
	{
		return canonicalNameToHumanReadable(getClass().getCanonicalName());
	}
	
	public final static String canonicalNameToHumanReadable(String canonicalName)
	{
		String[] parts = canonicalName.split("\\.");
		String lastPart = parts[parts.length - 1];
		String newStr = "";
		
		for (char c : lastPart.toCharArray())
		{
			if(Character.isUpperCase(c))
			{
				newStr += " ";
			}
			
			newStr += c;
		}
		
		return newStr;
	}
}
