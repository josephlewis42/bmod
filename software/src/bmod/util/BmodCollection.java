package bmod.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BmodCollection
{
	
	/**
	 * Returns a set of the toString method called on all the objects.
	 * 
	 * @param objects - the object to get the Set from.
	 * @return A set containing the values of the toString operation on all of
	 * the given objects.
	 */
	public static Set<String> stringSet(Collection<?> objects)
	{
		HashSet<String> output = new HashSet<String>();
		
		for(Object o : objects)
			output.add(o.toString());
		
		return output;
	}
	
	public static <T extends Comparable<? super T>> List<T> sortedSet(Set<T> input)
	{
		LinkedList<T> list = new LinkedList<T>(input);
		Collections.sort(list);
		
		return list;
	}
	
	public static <T> List<T> sortedSet(Set<T> input, Comparator<? super T> comp)
	{
		LinkedList<T> list = new LinkedList<T>(input);
		Collections.sort(list, comp);
		return list;
	}
	
	/**
	 * Performs a diff on the two collections.
	 * @param first
	 * @param second
	 * @return
	 */
	public static String diffCollections(Collection<?> first, Collection<?> second)
	{
		Set<String> a = stringSet(first);
		Set<String> b = stringSet(second);
		
		Set<String> all = new HashSet<String>();
		all.addAll(a);
		all.addAll(b);
		
		StringBuilder output = new StringBuilder();
		for(String s : sortedSet(all))
		{
			boolean inA = a.contains(s);
			boolean inB = b.contains(s);
			
			if(inA && inB)
				output.append("  ");
			else if(inA && !inB)
				output.append("+ ");
			else
				output.append("- ");
			
			output.append(s);
			output.append("\n");
		}
		
		return output.toString();
	}
	
	public static final int[] collectionToIntList(Collection<Integer> ints)
	{
		int[] output = new int[ints.size()];
		
		int i = 0;
		for(Integer tmp : ints)
		{
			output[i] = tmp.intValue();
		}
		
		return output;
	}
	
	/**
	 * Sorts a map by value rather than by key and returns a sorted entry set.
	 * 
	 * @param myMap
	 * @return
	 */
	public static <K,V extends Comparable<V>> List<Entry<K,V>> valueSortMap(Map<K,V> myMap)
	{
		LinkedList<Entry<K,V>> lle = new LinkedList<Entry<K, V>>(myMap.entrySet());
		
		Collections.sort(lle, new Comparator<Entry<K,V>>(){
	
			@Override
			public int compare(Entry<K,V> arg0, Entry<K,V> arg1)
			{
				return arg0.getValue().compareTo(arg1.getValue());
			}
			
		});
		
		return lle;
	}
	
	/**
	 * Compares two sets of objects, if any object's string in newSelection
	 * matches one in oldSelection, the index of the object in newSelection is
	 * added to an array and returned.
	 * 
	 * This is generally useful in finding indicies of objects in lists that 
	 * have been updated.
	 * 
	 * @param oldSelection
	 * @param newSelection
	 * @return
	 */
	public static int[] sharedIndicies(Collection<?> oldSelection, Collection<?> newSelection)
	{
		Set<String> oldSelectionStrings = BmodCollection.stringSet(oldSelection);
		ArrayList<Integer> indicies = new ArrayList<Integer>();
		
		int i = 0;
		for(Object o : newSelection)
		{
			if(oldSelectionStrings.contains(o.toString()))
			{
				indicies.add(i);
			}
			i++;
		}
		
		int[] returnIndicies = new int[indicies.size()];
		
		for(i = 0; i < returnIndicies.length; i++)
		{
			returnIndicies[i] = indicies.get(i);
		}
		
		return returnIndicies;
	}
}
