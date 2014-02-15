package bmod.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Bucket<K, V> implements Map<K, Set<V>>
{
	private final HashMap<K,Set<V>> m_map = new HashMap<K,Set<V>>();
	
	@Override
	public Set<K> keySet()
	{
		return m_map.keySet();
	}
	
	public Set<V> valueSet(K key)
	{
		return m_map.get(key);
	}
	
	/**
	 * Adds a single item to the bucket, creating a bucket if needed.
	 * @param key
	 * @param value
	 */
	public void add(K key, V value)
	{
		Set<V> values = m_map.get(key);
		if(values != null)
		{
			values.add(value);
			return;
		}
		
		values = new HashSet<V>();
		values.add(value);
		m_map.put(key, values);
	}
	
	/**
	 * Adds a single item to multiple keys.
	 * 
	 * @param keys
	 * @param value
	 */
	public void add(List<K> keys, V value)
	{
		for(K tmp : keys)
			add(tmp, value);
	}
	public void add(K[] keys, V value)
	{
		for(K tmp : keys)
			add(tmp, value);
	}
	
	@Override
	public void clear()
	{
		m_map.clear();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return m_map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return m_map.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<K, Set<V>>> entrySet()
	{
		return m_map.entrySet();
	}

	@Override
	public Set<V> get(Object key)
	{
		return m_map.get(key);
	}

	@Override
	public boolean isEmpty()
	{
		return m_map.isEmpty();
	}

	@Override
	public Set<V> put(K key, Set<V> value)
	{
		return m_map.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends Set<V>> m)
	{
		m_map.putAll(m);
	}

	@Override
	public Set<V> remove(Object key)
	{
		return m_map.remove(key);
	}

	@Override
	public int size()
	{
		return m_map.size();
	}

	@Override
	public Collection<Set<V>> values()
	{
		return m_map.values();
	}
	
	@Override
	public String toString()
	{
		StringBuilder output = new StringBuilder();
		output.append("Bucket: ");
		for(K key : keySet())
		{
			output.append(key + ": " + get(key).size() + ", ");
		}
		
		return output.toString();
	}
}
