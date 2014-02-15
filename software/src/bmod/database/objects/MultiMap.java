package bmod.database.objects;

/**
 * A map between two objects. All elements are backwards and forwards.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public interface MultiMap<SRC extends Record<SRC>, E extends Record<E>>
{
	/**
	 * Fetches all of the E's that are associated with the given T's primary key
	 * 
	 * @param src - the pkey of a t to fetch from.
	 * @return
	 */
	public E[] connectionsFrom(long src);
	
	/**
	 * Returns all possible connections from the given source node.
	 * 
	 * @param src
	 * @return
	 */
	public E[] possibleConnectionsFrom(long src);
	
	/**
	 * Fetches all of the T's that are associated with the given E's primary key
	 * 
	 * @param dest - the pkey of an E to fetch to.
	 * @return
	 */
	public SRC[] connectionsTo(long dest);
	
	/**
	 * Deletes any links from the given source to the given destination.
	 * 
	 * @param src
	 * @param dest
	 */
	public void deleteFrom(long src, long dest);
	
	/**
	 * Adds a link from the given node to the given destination, if it doesn't
	 * already exist.
	 * 
	 * @param src
	 * @param dest
	 */
	public void addLink(long src, long dest);
}
