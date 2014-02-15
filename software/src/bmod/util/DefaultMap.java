package bmod.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A map that can optionally provide default values if needed. Threadsafe.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 * @param <K> - the key type to use for the map
 * @param <V> - the value type to use for the map.
 */
public class DefaultMap<K, V>
{	
	private final HashMap<K,V> m_underlying = new HashMap<K,V>();
	private final ValueGenerator<K, V> m_generator;
	
	
	public static abstract class ValueGenerator<K, V>
	{
		public abstract V getValue(K key);
	}
	
	/**
	 * Constructs a default map that returns null for any keys not found, much
	 * like a regular map!
	 */
	public DefaultMap()
	{
		this(new ValueGenerator<K, V>()
		{
			@Override
			public V getValue(K key)
			{
				return null;
			}
		});
	}
	
	/**
	 * Constructs a DefaultMap that returns the result of a function for every
	 * key not found.
	 */
	public DefaultMap(ValueGenerator<K, V> generator)
	{
		m_generator = generator;
	}
	
	public V get(K key)
	{
		synchronized(m_underlying)
		{
			V value = m_underlying.get(key);
			
			if(value == null)
			{
				value = m_generator.getValue(key);
				m_underlying.put(key, value);
			}
			return m_underlying.get(key);
		}
	}

	
	
	
	// EVERYTHING BELOW THIS LINE IS SIMPLY A CALL TO THE BASE MAP CLASS

	public void clear()
	{
		synchronized(m_underlying)
		{
			m_underlying.clear();
		}
	}

	public boolean containsKey(Object key)
	{
		synchronized(m_underlying)
		{
			return m_underlying.containsValue(key);
		}
	}

	public boolean containsValue(Object value)
	{
		synchronized(m_underlying)
		{
			return m_underlying.containsValue(value);
		}
	}

	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		synchronized(m_underlying)
		{
			return new TreeSet<java.util.Map.Entry<K, V>>(m_underlying.entrySet());
		}
	}

	public boolean isEmpty()
	{
		synchronized(m_underlying)
		{
			return m_underlying.isEmpty();
		}
	}

	public Set<K> keySet()
	{
		synchronized(m_underlying)
		{
			return new TreeSet<K>(m_underlying.keySet());
		}
	}

	public V put(K key, V value)
	{
		synchronized(m_underlying)
		{
			return m_underlying.put(key, value);
		}
	}

	public void putAll(Map<? extends K, ? extends V> arg0)
	{
		synchronized(m_underlying)
		{
			m_underlying.putAll(arg0);
		}
	}

	public V remove(Object key)
	{
		synchronized(m_underlying)
		{
			return m_underlying.remove(key);
		}
	}

	public int size()
	{
		synchronized(m_underlying)
		{
			return m_underlying.size();
		}
	}

	public Collection<V> values()
	{
		synchronized(m_underlying)
		{
			return new LinkedList<V>(m_underlying.values());
		}
	}

}
