package bmod;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import bmod.gui.widgets.NTable;

public class ManifestParser
{
	private static final Logger m_logger = Logger.getLogger("ManifestParser");
	private Manifest prop = new Manifest();
	public ManifestParser()
	{
		try
		{
		  URL res = ManifestParser.class.getResource(ManifestParser.class.getSimpleName() + ".class");
		    JarURLConnection conn = (JarURLConnection) res.openConnection();
		   	prop = conn.getManifest();
		} catch (ClassCastException | IOException e)
		{
			m_logger.info("Couldn't load manifest: " + e.getMessage());
		}

	}
	
	/**
	 * Gets the keys in the manifest file.
	 * 
	 * @return
	 */
	public Attributes getKeys()
	{
		return prop.getMainAttributes();
	}
	
	public Map<String, String> getManifestMap()
	{
		HashMap<String, String> output = new HashMap<String, String>();
		
		for(Entry<Object, Object> e : getKeys().entrySet())
			output.put(e.getKey().toString(), e.getValue().toString());
		
		return output;
	}
	
	public String getStrPropertiesStartingWith(String start)
	{
		StringBuilder total = new StringBuilder();
		
		for(Entry<Object, Object> e : getKeys().entrySet())
		{			
			if(e.getKey().toString().toLowerCase().startsWith(start.toLowerCase()))
			{
				total.append(e.getKey().toString());
				total.append("\t->\t");
				total.append(e.getValue().toString());
				total.append("\n");
			}
		}
		
		return total.toString();
	}
	
	public String getPropertyOrDefault(String propName, String defaultvalue)
	{
		try
		{
			String val = prop.getMainAttributes().getValue(propName);
			
			return (val == null)? defaultvalue:val;
		} catch(NullPointerException ex)
		{
			return defaultvalue;
		}
		
	}
	
	/**
	 * Normalizes key names to human readable names. 
	 * 
	 * e.g. TheLazyOSMac -> "The Lazy OS Mac"
	 */
	public static String normalizeKey(String key)
	{
		String output = "";
		char[] arr = key.toCharArray();
		
		for(int i = 0; i < arr.length; i++)
		{
			if(arr[i] == '-')
				arr[i] = ' ';
			
			if(i == 0 || i == arr.length - 1)
			{
				output += arr[i];
				continue;
			}
			
			if(Character.isUpperCase(arr[i]) && Character.isLowerCase(arr[i+1]) || 
					Character.isUpperCase(arr[i]) && Character.isLowerCase(arr[i-1]))
				output += " ";
			
			output += arr[i];
		}
		
		return output;
	}
	
	/**
	 * Returns a simple JPanel that just lists properties in the properties file.
	 * @param s
	 * @return
	 */
	public static JPanel getPropertiesStartsWith()
	{
		ManifestParser mp = new ManifestParser();
		NTable nt = new NTable(false, true, true, false);
		
		Map<String, String> props = mp.getManifestMap();
		
		String[] header = new String[]{"Property Name", "Property Value"};
		String[][] body = new String[props.keySet().size()][2];
		
		int i = 0;
		for(String key : props.keySet())
		{
			m_logger.info("Manifest: " + normalizeKey(key) + " -> " + props.get(key));
			body[i][0] = normalizeKey(key);
			body[i][1] = props.get(key);
			i++;
		}
		
		nt.setTable(header, body);
		nt.applyFilter("Build");
		return nt;
	}
	
}
