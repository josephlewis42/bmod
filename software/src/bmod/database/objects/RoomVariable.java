package bmod.database.objects;

import java.util.Collection;

import bmod.database.Database;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.LongWidget;
import bmod.gui.builder.SelectionWidget;
import bmod.gui.builder.TextAreaWidget;
import bmod.gui.builder.TextFieldWidget;


public class RoomVariable extends GenericVariable<RoomVariable>
{
	private final long m_room;
	
	private static final String tableName = "RoomVariables";
	private static final String[] COLUMN_NAMES = new String[]{"PrimaryKey", "VariableName", "VariableType", "VariableValue", "Description", "RoomID"};
	private static final String[] COLUMN_TYPES = new String[]{"BIGINT","VARCHAR(500)","CHAR","VARCHAR(1000)","VARCHAR(1000)", "BIGINT"};
	private static final String[] COLUMN_INDEXES = new String[]{};
	private static final Record<?>[] COLUMN_REFERENCES = new Record<?>[]{null,null,null,null,null,Database.templateRoom};

	
	public RoomVariable()
	{
		this(Database.TEMPLATE_PRIMARY_KEY, "", STRING, "", "", -1);
	}
	
	public RoomVariable(long pKey, String varName, char varType, String value, String desc, long roomId)
	{
		super(tableName, 
				COLUMN_NAMES,
				COLUMN_TYPES, 
				COLUMN_INDEXES,
				COLUMN_REFERENCES,
				pKey,
				varName,
				varType,
				value,
				desc);
		
		m_room = roomId;
	}
	
	public RoomVariable(String varName, char varType, String value, String desc, long roomId)
	{
		this(Database.getNewPrimaryKey(), varName, varType, value, desc, roomId);
	}
	
	/**
	 * Creates a BuildingVariable with null as a value, with the given
	 * name and in the given building.
	 * 
	 * @param varName - the name to give the variable.
	 * @param rm - the room to put the variable in.
	 */
	public RoomVariable(String varName, long rm)
	{
		this(varName, STRING, "", "", rm);
	}

	@Override
	public Object[] getExtraSQLVars()
	{
		return new Object[]{m_room};
	}

	@Override
	public String getId()
	{
		return getVariableName() + " in " + Room.getHumanReadableName(m_room);
	}
	
	public static final long getPrimaryKey(String varname, long roomid)
	{
		return (varname + " in " + roomid).hashCode();
	}
	
	/**
	 * Returns the key for the room this variable is in.
	 * @return
	 */
	public long getRoomKey()
	{
		return m_room;
	}

	@Override
	public RoomVariable fromSQL(Object[] parts) throws IllegalArgumentException
	{
		if(parts == null || parts.length != getColNames().length)
			throw new IllegalArgumentException();
		
		return new RoomVariable((Long) parts[0], (String) parts[1], ((String) parts[2]).charAt(0), (String) parts[3], (String) parts[4], (Long) parts[5]);

	}

	@Override
	protected RoomVariable getThis()
	{
		return this;
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		return new GUIBuilderPanel(toSQL(),
				new LongWidget("Primary Key", getPrimaryKey(), false),
				new TextFieldWidget("Name", getVariableName()),
				new SelectionWidget("Type", getVariableTypesMap(), getVariableType()),
				new TextFieldWidget("Value", getValue().toString()),
				new TextAreaWidget("Description", getDescription()),
				null);
	}
	
	@Override
	public String getUserEditableClass()
	{
		return this.getClass().getCanonicalName();
	}
	
	@Override
	public RoomVariable createNew(Collection<Record<?>> filterObjects)
	{
		// Make sure we have enough objects
		if (filterObjects.size() != 1)
		{
			throw new IllegalArgumentException("Cannot instantiate instance without proper arguments.");
		}
		
		// Declare our objects
		Room templateRoom = null;
		
		// Instantiate all of our declared objects
		for (Record<?> tmp : filterObjects)
		{
			if (tmp instanceof Room)
			{
				templateRoom = (Room) tmp;
			}
		}
		
		// Make sure we got all of our objects.
		if (templateRoom == null)
		{
			throw new IllegalArgumentException("Cannot instantiate instance without proper arguments.");
		}
		
		// Return the new item.
		return new RoomVariable(Database.getNewPrimaryKey(), "New Variable", STRING, "", "", templateRoom.getPrimaryKey());
	}
}
