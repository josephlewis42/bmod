package bmod.database.objects;

/**
 * A map between two objects. All elements are backwards and forwards.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public interface GenericMultiMap<E extends Record<E>>
{	
	/**
	 * Fetches all of the E's that are associated with the given T's primary key
	 * 
	 * @param src - the pkey of a t to fetch from.
	 * @return
	 */
	public E[] connectionsFrom(Record<?> srcInst);
	
	/**
	 * Returns all possible connections from the given source node.
	 * 
	 * @param src
	 * @return
	 */
	public E[] possibleConnectionsFrom(Record<?> srcInst);
	
	/**
	 * Deletes any links from the given source to the given destination.
	 * 
	 * @param src
	 * @param dest
	 */
	public void deleteFrom(Record<?> srcInst, long dest);
	
	/**
	 * Adds a link from the given node to the given destination, if it doesn't
	 * already exist.
	 * 
	 * @param src
	 * @param dest
	 */
	public void addLink(Record<?> srcInst, long dest);
}
