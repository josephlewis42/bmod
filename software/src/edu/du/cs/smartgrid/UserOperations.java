package edu.du.cs.smartgrid;

import java.io.IOException;

/** Operations that the user does. **/
public class UserOperations
{
	private static final String DEFAULT_HOST = "smartgrid.cs.du.edu";
	private String user_key = "";
	private final String HOST;
	
	public UserOperations(String user_key)
	{
		this(user_key, null);
	}
	
	public UserOperations(String userKey, String host)
	{
		user_key = userKey;
		HOST = "http://" + ((host == null) ? DEFAULT_HOST : host);
	}
	
	
	/** Gets the key associated with the device name. If the device doesn't 
		exist, creates it.
	 * @throws IOException 
	**/	
	public String get_device_key(String device_name) throws IOException
	{
		device_name = Common.urlEncode(device_name);
		return Common.fetch_url(HOST + "/device/api/1/get_key/" + device_name + "/" + user_key + "/");
	}
		
		
	/** Enables the device with the given key if possible.
	 * @throws IOException **/
	public void enable_device(String device_key) throws IOException
	{
		Common.fetch_url(HOST + "/device/api/1/enable/"+device_key+"/"+user_key+"/");
	}
	
	/** Enables the device with the given key if possible.
	 * @throws IOException **/
	public void disable_device(String device_key) throws IOException
	{
		Common.fetch_url(HOST + "/device/api/1/disable/"+device_key+"/"+user_key+"/");
	}
	
	/** Creates a feed with the given name, if possible. 
	 * @throws IOException **/
	public String create_feed(String feed_name) throws IOException
	{
		return Common.fetch_url(HOST + "/feed/api/1/create/"+Common.urlEncode(feed_name.trim())+"/"+user_key+"/");
	}
	
	public int lookupFeed(String feed_name) throws IOException, IllegalArgumentException
	{
		try
		{
			return Integer.parseInt(Common.fetch_url(HOST + "/feed/api/1/lookup/"+Common.urlEncode(feed_name.trim())+"/?user="+user_key));
		}
		catch(NumberFormatException e)
		{
			throw new IllegalArgumentException("The feed with the name " + feed_name + " wasn't found.");
		}
	}
	
	/**
	 * Suggests the server delete a feed.
	 * 
	 * @param feedId - The id of the feed to delete.
	 * @throws IOException
	 */
	public void deleteFeed(int feedId) throws IOException
	{
		Common.fetch_url(HOST + "/feed/api/1/delete/"+feedId+"/"+user_key+"/");
	}
	
	public void setDescription(int feedId, String description) throws IOException
	{
		Common.fetch_url(HOST + "/feed/api/1/set_description/" + feedId + "/" + Common.urlEncode(user_key) + "/?description=" + Common.urlEncode(description));
	}
	
	public String getDescription(int feedId) throws IOException
	{
		return Common.fetch_url(HOST + "/feed/api/1/get_description/" + feedId + "/?key=" + Common.urlEncode(user_key) + "/");
	}
	
	/** Allows the feed with the given id, to be edited with the device with the given key. 
	 * @throws IOException **/
	public void add_device(int feed_id, String device_key) throws IOException
	{
		Common.fetch_url(HOST + "/device/api/1/add_feed/"+device_key+"/"+feed_id+"/"+user_key+"/");
	}
}
