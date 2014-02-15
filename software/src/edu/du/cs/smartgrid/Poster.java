package edu.du.cs.smartgrid;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;

import bmod.util.DateTime;

/**
 * Posts data to the site.
 * 
 * call add_data(...) to add all your data, then post_data() to post it.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class Poster
{		
	private static final int MAX_UPLOADS_PER_POST = 2000;
	private final String HOST;
	private final HashMap<String, String> feed_id_lookup = new HashMap<String, String>();
	
	
	private String device_key = "";
	private UserOperations userOperations = null;
	private final LinkedList<Double> vals = new LinkedList<Double>();
	private final LinkedList<DateTime> dates = new LinkedList<DateTime>();
	private final LinkedList<String> feed_names = new LinkedList<String>();
	
	
	/**
	 * Creates a new Poster with the given device_key.
	 * @param device_key - the key used to connect to the API
	 * @param operations - A UserOperations or null, if not null, used to
	 * create feeds that don't already exist, and such. 
	 * @param host - The hostname/ip address of the place to submit to, or null
	 * for the default.
	 */
	public Poster(String device_key, UserOperations operations, String host)
	{
		this.device_key = device_key;
		this.userOperations = operations;
		
		HOST = "http://" + ((host == null)? Common.DEFAULT_HOST : host);
	}
	
	/**
	 * Checks to see if this Poster has enough data to do an efficient submit.
	 * 
	 * @return
	 */
	public boolean shouldSubmit()
	{
		return vals.size() >= MAX_UPLOADS_PER_POST;
	}
	
	public void add_data(DateTime date, double val, String feedname)
	{
		vals.push(val);
		dates.push(date);
		feed_names.push(feedname);
	}
	
	private String feed_id_from_name(String name) throws IOException
	{
		name = name.trim();
		String id = feed_id_lookup.get(name);
		if(id != null)
			return id;
		
		String result = Common.fetch_url(HOST + "/feed/api1/lookup/" + Common.urlEncode(name) + "/");		

		if(! result.equals(""))
		{
			if(userOperations != null)
				userOperations.add_device(Integer.parseInt(result), device_key);
			
			feed_id_lookup.put(name, result);
			return result;
		}
		
		if(userOperations == null)
			throw new IllegalArgumentException("Can't find feed, and can't create one.");
		
		
		result = userOperations.create_feed(name);
		userOperations.add_device(Integer.parseInt(result), device_key);

		feed_id_lookup.put(name, result);
		return result;		
	}
	
	/**
	 * Posts all of the data to the site.
	 * 
	 * @param data
	 * @throws IOException 
	 */
	public void post_data() throws IOException
	{
		while(vals.size() > 0)
		{
			LinkedList<BasicNameValuePair> form = new LinkedList<BasicNameValuePair>();
			
			int i = 1;
			while(i <= MAX_UPLOADS_PER_POST && vals.size() != 0)
			{
				BasicNameValuePair d = new BasicNameValuePair("d" + i, dates.pop().toISODate());
				BasicNameValuePair v = new BasicNameValuePair("v" + i, vals.pop() + "");
				BasicNameValuePair f = new BasicNameValuePair("f" + i, feed_id_from_name(feed_names.pop()));

				form.add(d);
				form.add(v);
				form.add(f);
				i++;
			}
			
			if(Common.DEBUGGING)
				System.err.println("Posting " + (form.size() / 3.0) + " data points. ");
			
			Request r = Request.Post(HOST + "/feed/api/1/submit/" + device_key + "/")
					.connectTimeout(Common.TIMEOUT_MS * 5)
			    	.socketTimeout(Common.TIMEOUT_MS * 5)
			    	.bodyForm(form);
			try
			{
				r.execute().returnContent().asString();
			} catch (ClientProtocolException e)
			{
				System.err.println(e.getMessage());
				e.printStackTrace();
			} catch (IOException e)
			{
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
