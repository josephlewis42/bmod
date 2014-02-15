package bmod.database.objects;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.gui.builder.DoubleWidget;
import bmod.gui.builder.FollowupWidget;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.GUIBuilderWidget;
import bmod.gui.builder.LongWidget;
import bmod.gui.builder.SimpleWrapperWidget;
import bmod.gui.builder.SourceWidget;
import bmod.gui.builder.TextAreaWidget;
import bmod.gui.builder.TextFieldWidget;
import bmod.gui.widgets.Dialogs;
import bmod.gui.widgets.MultiMapWidget;
import bmod.math.calculator.BmodVariableStore;
import bmod.math.calculator.Eval;
import bmod.math.calculator.MalformedExpression;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class DeviceType extends SimpleRecord<DeviceType>
{
	private final String m_id;
	private final String m_wattsExpression;
	private final String m_wattsOffExpression;
	private final String m_totalWattsExpression;
	private final double m_dutyCycle;
	private final String m_notes;
	private double m_watts = Double.MIN_VALUE; // Indicates getWatts never called, or invalid
	private static Logger m_logger = Logger.getLogger("DeviceType");

	private static final String   TABLE_NAME   = "Devices";
	private static final String[] COLUMN_NAMES = new String[]{"DeviceType","Watts","Notes","DutyCycle","OffWatts","PrimaryKey"};
	private static final String[] COLUMN_TYPES = new String[]{"VARCHAR(100)","VARCHAR(1000)","VARCHAR(5000)","DOUBLE","VARCHAR(1000)","BIGINT"};
	private static final String[] PRIMARY_KEYS = new String[]{"DeviceType"};
	private static final Record<?>[] COLUMN_REFERENCES = new Record<?>[]{null,null,null,null,null,null};

	/**
	 * @param name
	 * @param watts - a base64 encoded string for the function to calculate the watts used by the device.
	 * @param notes
	 */
	public DeviceType(String name, String watts, String notes, double dutyCycle, String offWatts, long pkey)
	{
		super(TABLE_NAME, COLUMN_NAMES, COLUMN_TYPES, PRIMARY_KEYS, COLUMN_REFERENCES, pkey);
		
		if(name == null)
			throw new IllegalArgumentException("'id' cannot be null.");
		
		m_id = name;
		m_dutyCycle = dutyCycle;
		
		// Try decoding the Base64:
		m_wattsExpression = getWattsFromB64(watts);
		m_wattsOffExpression = getWattsFromB64(offWatts);
				
		m_notes = notes;
		m_totalWattsExpression = "((("+m_wattsExpression+") * "+m_dutyCycle + ") + (("+m_wattsOffExpression+") * (1 - "+m_dutyCycle+")))";
	}
	
	public DeviceType()
	{
		this("","","",0,"",Long.MIN_VALUE);
	}
	
	
	private static HashMap<String, String> wattsCache = new HashMap<String, String>();
	/**
	 * Turns a base64 expression or int in to plain-text
	 * 
	 * @param watts
	 * @return
	 */
	private String getWattsFromB64(String watts)
	{
		String output = wattsCache.get(watts);
		if(null != output)
			return output;
		
		try
		{
			output = Integer.parseInt(watts)+ "";
		}
		catch(Exception e)
		{
			try
			{
				output = new String(Base64.decode(watts));
			} catch (Base64DecodingException ex)
			{
				// Should work if reading from CSV file directly (you can do js in, but it won't be put out.
			}
		}
		
		wattsCache.put(watts, output);
		
		return output;
	}

	@Override
	public String getId() { return m_id; }
	public String getWattsExpr() { return m_wattsExpression; }
	public String getWattsOffExpr() { return m_wattsOffExpression; }
	public String getTotalWattsExpr() { return m_totalWattsExpression; }
	
	
	/**
	 * Returns true if the expression for this DeviceType's total wattage
	 * doesn't use external variables, device types, or JavaScript functions.
	 */
	public boolean isPureMath()
	{
		return getTotalWattsExpr().indexOf('$') == -1;
	}
	
	
	public String getNotes() { return m_notes; }
	@Override
	public int hashCode() { return toString().hashCode(); }
	public double getDutyCycle() { return m_dutyCycle;}
	
	/**
	 * Calculates the average number of watts used for this device
	 * at a given moment.
	 * 
	 * @return
	 */
	private static final BmodVariableStore m_bms = new BmodVariableStore();
	public double getWatts()
	{
		if(m_watts == Double.MIN_VALUE)
		{
			try
			{
				//m_watts = Calculator.getWatts(m_totalWattsExpression);
				m_watts = Eval.eval(m_totalWattsExpression,m_bms);
				//m_watts = (c.getInt(m_wattsExpression) * m_dutyCycle) + (c.getInt(m_wattsOffExpression) * (1 - m_dutyCycle));
			} catch (MalformedExpression | NumberFormatException e)
			{
				m_watts = Double.NEGATIVE_INFINITY;
			}
		}
		
		//if(m_watts == Double.MIN_VALUE)
		//	throw new IllegalArgumentException("Cannot get the watts if the device is not valid!");
		
		return m_watts;
	}
	
	public boolean isValid()
	{
		return getWatts() != Double.NEGATIVE_INFINITY;
	}

	@Override
	public boolean equals(Object o)
	{
		if(o instanceof DeviceType){
			DeviceType that = (DeviceType)o;
			return m_id.equals(that.m_id);
		}
		return false;
	}


	@Override
	public Object[] toSQL() {
		return new Object[]{m_id, Base64.encode(m_wattsExpression.getBytes()), m_notes, m_dutyCycle, Base64.encode(m_wattsOffExpression.getBytes()), getPrimaryKey()};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{
		if(getNotes().equals(""))
			list.addWarning("Device Type \""+getId()+"\" has no notes.");
			
		
		try
		{
			if( !Eval.isErrorFree(m_wattsOffExpression, m_bms) || !Eval.isErrorFree(m_wattsExpression, m_bms))
				list.addError("The function for device type \""+getId()+"\" is not valid.");
		} catch (Exception e)
		{
			list.addError("The calculator could not be created" + e.getMessage());
			m_logger.error("The calculator could not be created", e);
		}
		
		if(! isValid())
			list.addError("The wattage calculation is not valid.");
	}

	

	@Override
	public DeviceType fromSQL(Object[] parts) throws IllegalArgumentException
	{
		return new DeviceType((String)parts[0], (String)parts[1], (String) parts[2], (Double)parts[3], (String)parts[4], (Long)parts[5]);
	}

	@Override
	protected DeviceType getThis()
	{
		return this;
	}

	@Override
	public void updateTable()
	{
	}
	
	
	/**
	 * Gets a simple editor for the DataFeed.
	 * @return
	 */
	@Override
	public GUIBuilderPanel getEditor()
	{
		
		final MultiMapWidget<DeviceType, DeviceTypeCategory> m_map = new MultiMapWidget<DeviceType, DeviceTypeCategory>(this, new DeviceTypeToCategoryMap());
		m_map.appendAddButton().addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				String s = Dialogs.showUserInputDialog("New Category", "What would you like to name the new category?");
				if(s == null)
					return;
				
				DeviceTypeCategory z = new DeviceTypeCategory(s);
				z.create();
				
				m_map.refreshPanel();
			}
		});
		
		m_map.setSource(this);
		
		return new GUIBuilderPanel(toSQL(),
				new GUIBuilderWidget[]{
					new FollowupWidget(this),
					new SimpleWrapperWidget("Categories", m_map, true, false),
					new SourceWidget(this),
					new DoubleWidget("Calculated Watts", getWatts(), false)
				},
				new int[]{1,2,5,4,3,0},
				new TextFieldWidget("Name", getId(), true),
				new TextFieldWidget("Watts While On", getWattsExpr()),
				new TextAreaWidget("Notes", getNotes()),
				new DoubleWidget("Duty Cycle", getDutyCycle(), true),
				new TextFieldWidget("Watts While Off", getWattsOffExpr()),
				new LongWidget("Primary Key", getPrimaryKey(), false));
	}

	public DeviceTypeCategory[] getCategories()
	{
		return new DeviceTypeToCategoryMap().connectionsFrom(getPrimaryKey());
	}
	
	/**
	 * Finds a DeviceType with the given name.
	 * 
	 * @param deviceTypeName - The name of the type to fetch.
	 * @return The DeviceType with the given name
	 * @throws DatabaseIntegrityException - If no such DeviceType exists.
	 */
	public static DeviceType readDeviceType(String deviceTypeName) 
			throws DatabaseIntegrityException
	{
		return Database.templateDeviceType.readWhere()
				.eq("DeviceType", deviceTypeName).one();
	}
	
	@Override
	public String getUserEditableClass()
	{
		return this.getClass().getCanonicalName();
	}

	@Override
	public DeviceType createNew(Collection<Record<?>> filterObjects)
			throws IllegalArgumentException
	{
		return new DeviceType("New Device Type","","",0.0,"",Database.getNewPrimaryKey());
	}
}
