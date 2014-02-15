package bmod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;


/**
 * Reads configuration files.
 * @author Joseph Lewis <joehms22@gmail.com>
 */
public class ConfigReader
{
	private static final Logger m_logger = Logger.getLogger("bmod.ConfigReader");
	static ConfigReader myCR;
	public Properties m_properties;
	private static final File SETTINGS_FILE_NAME = ExtensionPoints.getBmodDirectory("bmod_config.properties").toFile();
	
	private static synchronized ConfigReader getCR()
	{
		if(myCR == null)
			myCR = new ConfigReader();
		
		return myCR;
	}
	
	private ConfigReader()
	{		
		if(! SETTINGS_FILE_NAME.exists())
			try
			{
				try{
					SETTINGS_FILE_NAME.getParentFile().mkdirs();
				}catch(Exception e)
				{}
				
				SETTINGS_FILE_NAME.createNewFile();
			} catch (IOException e1)
			{
				m_logger.debug("Could not create the settings file.");
			}
		
		m_properties = new Properties();
		FileReader r = null;
		try
		{
			r = new FileReader(SETTINGS_FILE_NAME);
			m_properties.load(r);
		} catch (FileNotFoundException e)
		{
			m_logger.debug("Could not open the settings file");
		} catch (IOException e)
		{
			m_logger.debug("Could not read the settings file");
		} finally {
			try
			{
				if(r != null)
					r.close();
			} catch (IOException e)
			{
				m_logger.error("Error, couldn't close stream to: "+SETTINGS_FILE_NAME.getPath());
			}
		}
	}
	
	/**
	 * Gets the int at the given property.
	 * @param pname -- The name of the property to fetch.
	 * @param defaultValue -- The default value, if one doesn't exist in the file.
	 * @return
	 */
	public static final int getInt(String pname, int defaultValue)
	{
		ConfigReader cr = getCR();
		String p = cr.m_properties.getProperty(pname);
		
		try {
			return Integer.parseInt(p);
		} catch(Exception e) {
			put(pname, defaultValue);
			return defaultValue;
		}
	}
	
	/**
	 * Gets the long at the given property.
	 * @param pname -- The name of the property to fetch.
	 * @param defaultValue -- The default value, if one doesn't exist in the file.
	 * @return
	 */
	public static final long getLong(String pname, long defaultValue)
	{
		ConfigReader cr = getCR();
		String p = cr.m_properties.getProperty(pname);
		
		try {
			return Long.parseLong(p);
		} catch(Exception e) {
			put(pname, defaultValue);
			return defaultValue;
		}
	}
	
	
	/**
	 * Gets the string at the given property.
	 * @param pname -- The name of the property to fetch.
	 * @param defaultValue -- The default value, if one doesn't exist in the file.
	 * @return
	 */
	public static final String getString(String pname, String defaultValue)
	{
		ConfigReader cr = getCR();
		String p = cr.m_properties.getProperty(pname);
		
		if(p != null)
			return p;
		
		put(pname, defaultValue);
		return defaultValue;
	}
	
	/**
	 * Puts the given value in to the config file at the given name.
	 * @param pname
	 * @param pvalue
	 * @return 
	 */
	public static final void put(String pname, Object pvalue)
	{
		ConfigReader cr = getCR();
		cr.m_properties.put(pname, pvalue.toString());
		try
		{
			FileWriter f = null;
			try
			{
				f = new FileWriter(SETTINGS_FILE_NAME);
				cr.m_properties.store(f, "BMOD Config File");
			}
			finally
			{
				if(f != null)
					f.close();
			}
		} catch (IOException e)
		{
			m_logger.debug("Could not save the config file.");
		}
	}
	
	
	public static final String getAll()
	{
		StringBuilder sb = new StringBuilder();
		ConfigReader cr = getCR();
		for(Entry<Object, Object> objs : cr.m_properties.entrySet())
		{
			sb.append(objs.getKey());
			sb.append("\t");
			sb.append(objs.getValue());
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
