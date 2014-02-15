package bmod.buildingactivity;

import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import bmod.util.DateTime;

public abstract class BuildingActivityInterface
{	
	private final String m_optionString;
	protected final Logger m_logger = Logger.getLogger(this.getClass());
	
	BuildingActivityInterface(String optionString)
	{
		m_optionString = optionString;
	}
	
	private HashMap<String, String> getOptMap()
	{
		HashMap<String,String> m_hm = new HashMap<String,String>();
		for(String option : m_optionString.split(";"))
		{
			if(option.contains("="))
			{
				String[] splits = option.split("=", 2);
				m_hm.put(splits[0], splits[1]);
			}
		}
		
		return m_hm;
	}
	
	private String getProp(String name)
	{
		return getOptMap().get(name);
	}
	
	public boolean getBoolProp(String propname, boolean def)
	{
		try
		{
			String s = getProp(propname);
			if(s == null)
				return def;
			return Boolean.parseBoolean(getProp(propname));
		} catch(Exception ex)
		{
			return def;
		}
	}
	
	public double getDoubleProp(String propname, double def)
	{
		try
		{
			return Double.parseDouble(getProp(propname));
		} catch(Exception ex)
		{
			return def;
		}
	}
	
	public String getProp(String propname, String def)
	{
		try
		{
			String s = getProp(propname);
			if(null != s)
				return s;
			return def;
		} catch(Exception ex)
		{
			return def;
		}
	}
	
	public long getLongProp(String propname, long def)
	{
		String testLong = getProp(propname);
		if(testLong != null)
		{
			try
			{
				return Long.parseLong(testLong);
			} catch(Exception ex)
			{
			}
		}
		
		return def;
	}

	
	
	/**
	 * An interface string stores properties.
	 * 
	 * name=value;name2=value2;
	 * @param strings
	 * @return
	 */
	public static String buildInterfaceString(String...strings)
	{
		if(strings.length % 2 != 0)
			throw new IllegalArgumentException("Odd number of strings not accepted for buildInterfaceString: "+Arrays.toString(strings));
		
		StringBuffer buf = new StringBuffer();
		boolean isfirst = true;
		for(String s : strings)
		{
			s = s.replace('=', ' ');
			
			if(isfirst)
			{
				buf.append(s + "=");
				isfirst = false;
			}
			else
			{
				buf.append(s + ";");
				isfirst = true;
			}
		}
		
		return buf.toString();
	}
	
	public abstract boolean happensWithin(DateTime start, DateTime end);
	
	public boolean happensWithin(long start, long end)
	{
		return happensWithin(new DateTime(start),new DateTime(end));
	}
	
	public abstract int getInterfaceId();
	
	public abstract JPanel getEditorPanel();
	
	public abstract String getInterfaceTypeFromPanel(JPanel m_panel);
	
	public abstract String getHumanReadableName();
	
	/**
	 * Returns the properties string this activity was set up with.
	 * 
	 * @return
	 */
	public String getPropertiesString()
	{
		return m_optionString;
	}
	
	public abstract String getHumanString();
	
	@Override
	public boolean equals(Object oth)
	{
		
		if(!(oth instanceof BuildingActivityInterface))
			return false;
			
		HashMap<String, String> m_hm = getOptMap();
		BuildingActivityInterface other = (BuildingActivityInterface) oth;
		HashMap<String, String> o_hm = other.getOptMap();
		try
		{
			for(String key : m_hm.keySet())
			{
				if(!o_hm.get(key).equals(m_hm.get(key)))
				{
					return false;
				}
			}
			return true;
		} catch(NullPointerException ex) // Other hm doesn't have one of the keys we do.
		{
			return false;
		}
	}

	public abstract double getPercentageTimeFilled(DateTime start, DateTime end);
}