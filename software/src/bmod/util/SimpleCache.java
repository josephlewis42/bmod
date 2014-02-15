package bmod.util;

import java.util.HashMap;

/**
 * A cache that does not depend on the database.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class SimpleCache<K, V>
{
	private final HashMap<K, V> m_objects = new HashMap<K, V>();
	
	public void put(K key, V value)
	{
		m_objects.put(key, value);
	}
	
	public V get(K key)
	{
		return m_objects.get(key);
	}
	
	public boolean contains(K key)
	{
		return m_objects.containsKey(key);
	}
}
