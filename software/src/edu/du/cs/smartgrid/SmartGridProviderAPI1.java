package edu.du.cs.smartgrid;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SmartGridProviderAPI1
{
	private static final String CSV_NEWLINE_CHAR = "\n";
	private static final String CSV_DELIM_CHAR = ",";
	private static final int FEED_LIST_ID_INDEX = 0;
	private static final int FEED_LIST_TITLE_INDEX = 2;
	
	private final String API_KEY;
	private final String HOST;

	
	public SmartGridProviderAPI1(String apiKey)
	{
		this(apiKey, Common.DEFAULT_HOST);
	}
	
	public SmartGridProviderAPI1(String apiKey, String host)
	{
		API_KEY = apiKey;
		HOST = host;
	}
	
	
	
	/**
	 * Suggests the feed with the given id be compressed.
	 * 
	 * @param feed_id - the id of the feed to compress.
	 * @throws IOException 
	 */
	public void feedCompress(int feed_id) throws IOException
	{
		Common.fetch_url("http://" + HOST + "/feed/api/1/compress/" + feed_id + "/");
	}
	
	/**
	 * Returns a map of the feeds available. FeedId->FeedTitle format.
	 * 
	 * @return
	 * @throws IOException 
	 */
	public Map<Integer, String> feedList() throws IOException
	{
		HashMap<Integer, String> feeds = new HashMap<Integer, String>();
		
		String output = Common.fetch_url("http://" + HOST + "/feed/api/1/list_all/csv/?key=" + API_KEY);
		
		boolean firstline = true;
		for(String line : output.split(CSV_NEWLINE_CHAR))
		{
			if(firstline) 
			{
				firstline = false;
				continue;
			}
			
			try
			{
				String[] parts = line.split(CSV_DELIM_CHAR);
				
				feeds.put(Integer.parseInt(parts[FEED_LIST_ID_INDEX]), parts[FEED_LIST_TITLE_INDEX]);
				
			} catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		return feeds;
	}
	
	/**
	 * Returns the value of the feed at the given time, if no feed value is 
	 * close enough, null is returned.
	 * 
	 * @return
	 * @throws IOException 
	 */
	public Double feedValue(int feedId, int rangeMin, Date time) throws IOException
	{
		try
		{
			String date = Common._toISOTime(time);
			date = Common.urlEncode(date);

			return Double.parseDouble(Common.fetch_url("http://" + HOST + "/feed/api/1/closest/" + feedId + "/" + rangeMin + "/" + date + "/?key=" + API_KEY));
			
		}
		catch(NumberFormatException e)
		{
			return null;
		}
	}
	
	
	public FeedValueRange[] feedValues(int feedId, Date startTime, Date endTime) throws IOException
	{
		LinkedList<FeedValueRange> values = new LinkedList<FeedValueRange>();
		try
		{
			String start = Common.urlEncode(Common._toISOTime(startTime));
			String end = Common.urlEncode(Common._toISOTime(endTime));
	
			String csv = Common.fetch_url("http://" + HOST + "/feed/api/1/all/" + feedId + "/csv/" + start + "/" + end + "/?key=" + API_KEY);
			
			boolean firstline = true;
			for(String line : csv.split(CSV_NEWLINE_CHAR))
			{
				if(firstline)
				{
					firstline = false;
					continue;
				}
				
				try
				{
					values.add(new FeedValueRange(line, feedId));
				} catch (NumberFormatException ex){}
			}
			
		}catch(UnsupportedEncodingException ex)
		{
		}
		return values.toArray(new FeedValueRange[values.size()]);
		
	}
	
	/**
	 * 
	 * @param feedName
	 * @return
	 * @throws IOException 
	 */
	public int lookupFeed(String feedName) throws IllegalArgumentException, IOException
	{
		try
		{
			feedName = Common.urlEncode(feedName);
			return Integer.parseInt(Common.fetch_url("http://" + HOST + "/feed/api/1/lookup/" + feedName + "/?key=" + API_KEY));
			
		}
		catch(NumberFormatException e)
		{
			throw new IllegalArgumentException("The feed with the name " + feedName + " wasn't found.");
		}
	}
	
	public class FeedValueRange
	{
		public final Date startDate;
		public final Date endDate;
		public final int feedId;
		public final double value;
		
		public FeedValueRange(String line, int feedid)
		{
			String[] parts = line.split(CSV_DELIM_CHAR);
			
			startDate = Common._parseISOTime(parts[0]);
			endDate = Common._parseISOTime(parts[1]);
			value = Double.parseDouble(parts[2]);
			feedId = feedid;
		}
	}
}
