package bmod.database;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;


/**
 * A cache mechanism, keeps the specified number of objects in
 * memory with the given keys, after the map gets full, delete
 * the oldest objects.
 * 
 * Now supports versioning with a database so that if the
 * database changes, the cache will be invalidated.
 * 
 * @author jal
 *
 * @param <T> The type of the key to store.
 */
public class CRUDCache<T> {
	
	private static final LinkedList<CRUDCache<?>> ALL_CACHES = new LinkedList<CRUDCache<?>>();
	private final HashMap<String, T> m_map = new HashMap<String, T>();
	private final Queue<String> m_items = new LinkedList<String>();
	
	public static final int SMALL  = 10;
	public static final int MEDIUM = 50;
	public static final int LARGE = 100;
	public static final int INFINITY = Integer.MAX_VALUE;
	private static final Logger m_logger = Logger.getLogger("bmod.CRUDCache");
	private final int m_size;
	private int m_hits = 0;
	private int m_misses = 0;
	private int dbVersion = Integer.MIN_VALUE; // Make sure the version is always lower than current to start
	
	/**
	 * Constructs a cache of the MEDIUM size.
	 */
	public CRUDCache()
	{
		this(MEDIUM);
	}
	
	/**
	 * Constructs a cache with a given size.
	 * @param size - the number of keys:value pairs to store in the cache.
	 */
	public CRUDCache(int size)
	{
		m_size = size;
		
		synchronized(ALL_CACHES)
		{
			ALL_CACHES.add(this);
		}
	}
	
	/**
	 * Clears all known caches.
	 */
	public static void clearAllCaches()
	{
		synchronized(ALL_CACHES)
		{
			for(CRUDCache<?> c : ALL_CACHES)
				c.clear();
		}
	}
	
	
	/**
	 * Empties the entire cache and updates the database version.
	 */
	public void clear()
	{
		synchronized(m_map)
		{
			synchronized(m_items)
			{
				m_map.clear();
				m_items.clear();
			}
		}
	}
	
	/**
	 * Adds a value to the cache, with a key generated from the given objects.
	 * Order matters!
	 * 
	 * @param value
	 * @param keys
	 */
	public void add(T value, Object... keys)
	{
		clearIfNeeded();
		
		String key = getKey(keys);

		String toRemove = null;
		synchronized(m_items)
		{
			if(m_items.size() >= m_size)
				toRemove = m_items.poll();
			
			m_items.add(key);
		}
		
		
		// If we are storing too many things in the cache
		synchronized(m_map)
		{
			if(null != toRemove)
				m_map.remove(toRemove);
			
			m_map.put(key, value);
		}
	}
	
	/**
	 * Clears the cache if it is too old.
	 * @return true if the cache was cleared, false otherwise
	 */
	private boolean clearIfNeeded() {
		// Check version, if too old wipe us.
		int curr = Database.getDqm().getCommitNumber();
		if(dbVersion != curr)
		{
			m_logger.debug("Cache out of date, last version: "+dbVersion+" new version: "+curr+" clearing.");
			clear();
			dbVersion = curr;
			return true;
		}
		return false;
	}

	/**
	 * Gets a key from the given objects.
	 * 
	 * @param keys
	 * @return
	 */
	private String getKey(Object[] keys)
	{
		StringBuffer tmp = new StringBuffer();
		for(Object o : keys)
			tmp.append(o);
		
		return tmp.toString();
	}
	
	/**
	 * Gets an object from the database, returns null if no object found.
	 * 
	 * @param keys
	 * @return
	 */
	public T get(Object... keys)
	{
		// Check version, if too old wipe us.
		if(clearIfNeeded())
			return null;
		
		String key = getKey(keys);
		
		T myt = null;
		//synchronized(m_map){
			myt = m_map.get(key);
		//}
		
		if(myt != null)
			m_hits ++;
		else
			m_misses ++;
		
		m_logger.debug("Looking for: "+key+" hits: "+m_hits+" misses: "+m_misses);
		
		return myt;
	}
}
