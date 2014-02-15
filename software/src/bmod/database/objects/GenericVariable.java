package bmod.database.objects;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import bmod.database.DBWarningsList;

public abstract class GenericVariable<T extends SimpleRecord<T>> extends SimpleRecord<T>
{
	public static final char DOUBLE = 'd';
	public static final char STRING = 's';
	public static final char INTEGER = 'i';
	public static final char CHAR = 'c';
	
	private char m_type = INTEGER;
	private String m_value = "0";
	private String m_name = "NEW_VARIABLE";
	private String m_desc = "Description";

	public GenericVariable(String tableName, String[] colNames, String[] colTypes, String[] indexedKeys, Record<?>[] references,long pKey,
			String varName, char varType, String value, String varDesc)
	{
		super(tableName, colNames, colTypes, indexedKeys,references, pKey);
		
		m_name = varName;
		m_type = varType;
		m_value = value;
		m_desc = varDesc;
	}
	
	public Map<String, Character> getVariableTypesMap()
	{
		HashMap<String, Character> map = new HashMap<String, Character>();
		
		map.put("Integer",INTEGER);
		map.put("Decimal", DOUBLE);
		map.put("Text", STRING);
		map.put("Character", CHAR);
		
		return map;
		
	}
	
	public String getVariableName()
	{
		return m_name;
	}
	
	abstract public Object[] getExtraSQLVars();
	
	@Override
	public final Object[] toSQL()
	{
		LinkedList<Object> scl = new LinkedList<Object>();
		
		for(Object val : new Object[]{getPrimaryKey(), m_name, m_type, m_value, m_desc})
			scl.add(val);
		
		for(Object val : getExtraSQLVars())
			scl.add(val);
		
		return scl.toArray(new Object[scl.size()]);
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
		// Check to make sure type conversion works out.
		try
		{
			getValue();
		}catch(NumberFormatException ex)
		{
			list.addError("Variable \"" + this + " is not valid.");
		}

	}
	
	
	/**
	 * Gets the type of this stored variable as one of the 
	 * static final types contained within this class, i.e.
	 * DOUBLE, CHAR, INTEGER, STRING
	 */
	public final char getVariableType()
	{
		return m_type;
	}
	
	/**
	 * Returns the given value of this Variable in the type
	 * returned by getType(). You will need to cast this 
	 * variable to use it properly.
	 * 
	 * i.e. 
	 * 
	 * getType();
	 * > 'c'
	 * 
	 * c = (char) getValue();
	 * 
	 * @return
	 * @throws NumberFormatException - If the value cannot
	 * be converted to the set type.
	 */
	public final Object getValue() throws NumberFormatException
	{
		switch(getVariableType())
		{
			
			case INTEGER:
				return Integer.parseInt(m_value);
				
			case DOUBLE:
				return Double.parseDouble(m_value);
			
			case CHAR:
				if(m_value.length() == 0)
					throw new NumberFormatException("No value is set for the given char.");
				return m_value.charAt(0);
				
			case STRING:
				return m_value + "";
		}
		
		// This sould never happen
		throw new NumberFormatException("The type of the BuildingVariable isn't known!");
	}
	
	/**
	 * Gets the value of the int in this var, if available. If not, sets the int
	 * to the given value and returns it.
	 * 
	 * @param in
	 * @return
	 */
	public final int getOrSetInt(int in)
	{
		if(getValue() instanceof Integer)
			return (Integer) getValue();
		
		setIntValue(in);
		update();
		return in;
	}
	
	/**
	 * Gets the value of the double in this var, if available. If not, sets the int
	 * to the given value and returns it.
	 * 
	 * @param in
	 * @return
	 */
	public final double getOrSetDouble(double in)
	{
		if(getValue() instanceof Double)
			return (Double) getValue();
		
		setDoubleValue(in);
		update();
		return in;
	}
	
	/**
	 * Sets the value of this property to be the given int.
	 * @param in - the value to set
	 */
	public final void setIntValue(int in)
	{
		m_value = in + "";
		m_type = INTEGER;
	}
	
	/**
	 * Sets the value of this property to be the given double.
	 * @param in - the value to set
	 */
	public final void setDoubleValue(double in)
	{
		m_value = in + "";
		m_type = DOUBLE;
	}
	
	/**
	 * Sets the value of this property to be the given string.
	 * @param in - the value to set
	 */
	public final void setStringValue(String in)
	{
		m_value = in + ""; // copy the string
		m_type = STRING;
	}
	

	public final String getDescription()
	{
		return m_desc;
	}
}
