package bmod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import bmod.database.CRUDCache;

/**
 * A HashMap that serializes in to a base64 representation.
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 * @param <E>
 * @param <T>
 * @deprecated This class should not be used from here on in, instead things
 * should be done using the database; previously it was needed for CSVs.
 */
@Deprecated
public class SerializableHashMap<E extends Serializable,T extends Serializable> extends HashMap<E,T>
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates an empty serializable hash map.
	 */
	public SerializableHashMap()
	{
		
	}
	
	/**
	 * Creates a new map from a pre-serialized representation.
	 * @param init
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public SerializableHashMap(String init) throws IOException, ClassNotFoundException
	{
		SerializableHashMap<E,T> et = deserialize(init);
		
		putAll(et);
	}
	
	 private void writeObject(ObjectOutputStream out) throws IOException
	 {
		 // Write number of objects to follow
		 out.writeInt(size());
		 
		 // Write Key:Value pairs.
		 for(java.util.Map.Entry<E, T> k : entrySet())
		 {
			 out.writeObject(k.getKey());
			 out.writeObject(k.getValue());
		 }
	 }
	 
	 
	 private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	 {
		 // Read number of objects to follow
		 int num = in.readInt();
		 
		 // Read key value pairs.
		 
		 for(int i = 0; i < num; i++)
		 {
			 @SuppressWarnings("unchecked")
			 E key = (E) in.readObject();
			 @SuppressWarnings("unchecked")
			 T val = (T) in.readObject();
			 
			 put(key, val);
		 }	 
	}
	 
	 @Override
	public String toString()
	 {
		 StringBuilder output = new StringBuilder();
		 output.append("SerializableHashMap\n");
		 
		 for(java.util.Map.Entry<E, T> k : entrySet())
		 {
			 output.append("\t" + k.getKey() + " -> " + k.getValue() + "\n");
		 }
		 return output.toString();
	 }
	 
	 /**
	  * Creates a serialized representation of this object.
	  * 
	  * @return
	  * @throws IOException
	  */
	 public String serialize() throws IOException
	 {
	    ObjectOutput out;
	    ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
	    out = new ObjectOutputStream(bos) ;
	    out.writeObject(this);
	    out.close();

	    // Get the bytes of the serialized object
	    byte[] buf = bos.toByteArray();
	    
	    return new sun.misc.BASE64Encoder().encode(buf);

	 }
	 
	 /**
	  * Same as serialize, but returns the given string if serialization fails.
	  */
	 public String safeSerialize(String given)
	 {
		 try
		{
			return serialize();
		} catch (IOException e)
		{
			return given;
		}
	 }
	 
	 /**
	  * Returns a deserialized representation of an object.
	  * @param s
	  * @return
	  * @throws IOException
	  * @throws ClassNotFoundException
	  */
	 private static final CRUDCache<SerializableHashMap<?,?>> deserializeCache = new CRUDCache<SerializableHashMap<?,?>>();
	 @SuppressWarnings("unchecked")
	 private SerializableHashMap<E,T> deserialize(String s) throws IOException, ClassNotFoundException
	 {
		SerializableHashMap<?,?> shm = deserializeCache.get(s);
		if(shm != null)
			return (SerializableHashMap<E, T>) shm;
		
		byte[] buf = new sun.misc.BASE64Decoder().decodeBuffer(s);

		ByteArrayInputStream bosin = new ByteArrayInputStream(buf);
		ObjectInput in = new ObjectInputStream(bosin);
		shm = (SerializableHashMap<?, ?>) in.readObject();
		deserializeCache.add(shm, s);
		
		return (SerializableHashMap<E, T>) shm;
	 }
}
