package bmod.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import bmod.util.SimpleCache;

/**
 * A plugin manager bootstraps any kind of plugin desired.
 * It searches the provided directory for java classes, 
 * loads them, and inits them. 
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 * @param <T> The type of the plugin classes to be loaded.
 */
public class PluginManager<T>
{
	public Logger m_logger = Logger.getLogger(this.getClass());
	protected final Collection<T> m_plugins;
	protected final ClassLoader m_loader;
	private static final SimpleCache<String, Class<?>> CLASS_CACHE = new SimpleCache<String, Class<?>>();
	
	/**
	 * @param path - The filesystem/JAR path to load from.
	 * @param b 
	 */
	@SuppressWarnings("unchecked")
	public PluginManager(String...paths)
	{		
		m_loader = getClass().getClassLoader();
		m_plugins = new LinkedList<T>();

		// Format the path in to a format we want, no leading / and a trailing /
		for(String path : paths)
		{
			if(! path.endsWith("/") && ! (path.length() == 0))
				path = path + "/";

			if(path.startsWith("/"))
				path = path.substring(1);

			String m_fsPath = path;
			String m_importPath = m_fsPath.replace('/', '.');
		
		
			for(String rsc : getResourceListing(path))
			{
	
				try
				{
					if(rsc.endsWith(".class"))
						rsc = rsc.substring(0, rsc.length() - 6);
					else
						continue; // Don't try to load non classes
										
					Class<?> c = CLASS_CACHE.get(rsc);
					if(c == null)
					{
						c = m_loader.loadClass(m_importPath + rsc.replace('/','.'));
						m_logger.debug("Loaded: " + m_importPath + rsc.replace('/','.'));
						CLASS_CACHE.put(rsc, c);
					}
	
					T instance = (T) c.newInstance();
	
					m_plugins.add(instance);
	
				} catch (NoClassDefFoundError | Exception e)
				{
					m_logger.info("Couldn't load class: " + rsc);
				}
			}
		}
		
		m_logger.debug("Loaded Plugins:");
		for(T tmp : m_plugins)
			m_logger.debug("\t" + tmp.getClass().getName());
	}

	/**
	 * Fetches all plugins of type T from a directory.
	 * 
	 * i.e. "bmod/wattagecalculator" would return the wattage calculator ones.
	 * 
	 * @param path
	 * @return
	 */
	public Collection<T> getPlugins()
	{
		return m_plugins;
	}


	/**
	 * List directory contents for a resource folder. Not recursive.
	 * Uses a "jarfiles.txt" list of files contained in the jar; if they are
	 * listed as .java files, these are changed to .class files.
	 * 
	 * This ensures that the whole thing works over JNLP
	 * 
	 * @author Joseph Lewis
	 * @return Just the name of each member item, not the full paths.
	 * in case of an error, returns an empty array.
	 * 
	 */
	private String[] getResourceListing(String path)
	{
		
		ClassLoader cldr = this.getClass().getClassLoader(); 
		java.net.URL rsc = cldr.getResource("jarfiles.txt");
		try
		{
			InputStream in = rsc.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = reader.readLine();
			LinkedList<String> wanted = new LinkedList<String>();
			for(String test : line.split(";"))
				if(test.startsWith(path))
				{
					test = test.replace(".java", ".class").substring(path.length());					
					if(! test.contains("/"))
						wanted.add(test);
				}
			
			return wanted.toArray(new String[wanted.size()]);
		} catch (IOException e1)
		{
			m_logger.error(e1.getMessage());
			return new String[0];
		}
	}
}
