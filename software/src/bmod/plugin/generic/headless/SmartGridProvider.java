package bmod.plugin.generic.headless;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.SwingWorker;

import org.apache.http.client.fluent.Request;

import bmod.Constants;
import bmod.DataSet;
import bmod.GenericPlugin;
import bmod.database.DataFeed;
import bmod.database.DataNotAvailableException;
import bmod.util.Bucket;
import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;
import bmod.util.SimpleCache;
import bmod.util.TimeDelta;
import bmod.util.TimeRange;
import edu.du.cs.smartgrid.FeedIdentifier;
import edu.du.cs.smartgrid.SmartGridProviderAPI1;
import edu.du.cs.smartgrid.SmartGridProviderAPI1.FeedValueRange;
import edu.du.cs.smartgrid.SmartGridProviderEventListener;

/**
 * A interface to the SmartGrid site APIs.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class SmartGridProvider extends GenericPlugin
{	
	private static FeedIdentifier[] currIds = new FeedIdentifier[0];
	private static final int SERVER_CHECK_TIMEOUT_MS = 5000;
	private static boolean m_isOnline = true;
	private static final SmartGridProviderAPI1 provider = new SmartGridProviderAPI1(Constants.FETCH_API_KEY, Constants.API_HOST);
	private static final LinkedList<SmartGridProviderEventListener> listeners = new LinkedList<>();
		
	public SmartGridProvider()
	{
		super("Smart Grid Provider", "Provides data feeds from the SmartGrid website.");
		if(isServerReachable())
		{
			refreshFeeds();
		}
	}
	
	/**
	 * Called when the connection went offline.
	 */
	private static void connectionWentOffline()
	{
		for(SmartGridProviderEventListener listener : listeners)
		{
			listener.onServerOffline();
		}
		
		m_isOnline = false;
		
		// start a thread to keep checking until the server is reached.
		SwingWorker<?, ?> worker = new SwingWorker<Object, Object>() 
		{
			@Override
			protected Object doInBackground() throws Exception
			{
				while(! isServerReachable()){}
				
				m_isOnline = true;
				
				refreshFeeds();
				
				// alert everyone we're online, after we take care of ourselves.
				for(SmartGridProviderEventListener listener : listeners)
				{
					listener.onServerConnected();
				}
				
				return null;
			}
		};

		worker.execute();
	}
	
	/**
	 * Updates the available feeds from the smartgrid website.
	 */
	public static boolean refreshFeeds()
	{
		if(!m_isOnline)
			return false;
			
		LinkedList<FeedIdentifier> lif = new LinkedList<FeedIdentifier>();
		
		try
		{
			for(Entry<Integer, String> entry : provider.feedList().entrySet())
				lif.add(new FeedIdentifier(entry.getKey(), entry.getValue()));
		} catch (IOException e)
		{
			connectionWentOffline();
			return false;
		}

		currIds = lif.toArray(new FeedIdentifier[lif.size()]);
		Arrays.sort(currIds);
		
		return true;
	}


	/**
	 * Tests a connection to the server.
	 * 
	 * @return True if server is reachable, false otherwise.
	 */
	private static boolean isServerReachable()
	{
		try
		{
			Request.Get("http://" + Constants.API_HOST + "/")
			.connectTimeout(SERVER_CHECK_TIMEOUT_MS)
			.socketTimeout(SERVER_CHECK_TIMEOUT_MS)
			.execute();
			
			m_isOnline = true;
			
			return true;
		}catch(Exception ex)
		{
			return false;
		}
	}
	
	private static final SimpleCache<Integer, DataSet> POINT_CACHE = new SimpleCache<>();
	private static final Bucket<Integer, TimeRange> FETCHED_RANGES = new Bucket<>();

	private static final TimeDelta FEED_ACCEPTABLE_RANGE = new TimeDelta(0,3);
	
	public static final DataSet getCachedPoints(int key)
	{
		return POINT_CACHE.get(key);
	}
	
	/**
	 * Gets the closest time range before the given time.
	 * 
	 * @param feedId
	 * @param feedTime
	 * @return
	 * @throws DataNotAvailableException
	 */
	public static Entry<DateTime, Double> getClosestBeforeValue(final int feedId, final DateTime feedTime) throws DataNotAvailableException
	{
		Entry<DateTime, Double> closest = null;
		
		for(Entry<DateTime, Double> point : POINT_CACHE.get(feedId).getPoints())
		{
			if(point.getKey().before(feedTime))
			{
				if(closest == null)
				{
					closest = point;
					continue;
				}
				
				if(point.getKey().after(closest.getKey()))
				{
					closest = point;
				}
			}			
		}
		
		if(closest != null)
		{
			return closest;
		}
	
		throw new DataNotAvailableException("No value before the given time.");
	}
	
	
	
	public static Entry<DateTime, Double> getClosestAfterValue(final int feedId, final DateTime feedTime) throws DataNotAvailableException
	{
		Entry<DateTime, Double> closest = null;
		
		for(Entry<DateTime, Double> point : POINT_CACHE.get(feedId).getPoints())
		{
			if(point.getKey().after(feedTime))
			{
				if(closest == null)
				{
					closest = point;
					continue;
				}
				
				if(point.getKey().before(closest.getKey()))
				{
					closest = point;
				}
			}			
		}
		
		if(closest != null)
		{
			return closest;
		}
	
		throw new DataNotAvailableException("No value before the given time.");
	}
	
	/**
	 * Gets the feed's value at the given time.
	 * @param feedId
	 * @param ISOTime
	 * @return
	 * @throws IOException
	 */
	public static double getFeedValue(final int feedId, final DateTime feedTime) throws DataNotAvailableException
	{
		if(!m_isOnline)
		{
			throw new DataNotAvailableException("Feeds are offline");
		}
		
		boolean found = false;
		for(TimeRange tmp : FETCHED_RANGES.get(feedId))
		{
			if(tmp.contains(feedTime))
			{
				found = true;
				break;
			}
		}
		
		if(!found)
		{
			throw new IllegalArgumentException("The time " + feedTime + " has not been cached already!");
		}
		
		TimeRange rng = new TimeRange(feedTime.plusTime(FEED_ACCEPTABLE_RANGE.negate()),
										feedTime.plusTime(FEED_ACCEPTABLE_RANGE));
		
		for(Entry<DateTime, Double> point : POINT_CACHE.get(feedId).getPoints())
		{
			if(rng.contains(point.getKey()))
			{
				return point.getValue();
			}
		}
		
		// Check to see if anyone has a better option.
		double finalValue = Double.NEGATIVE_INFINITY;
		for(SmartGridProviderEventListener e : listeners)
		{
			finalValue = e.onNoFeedValue(feedId, feedTime, finalValue);
		}
		
		if(finalValue != Double.NEGATIVE_INFINITY)
			return finalValue;
		
		throw new DataNotAvailableException("No value at given time");
	}

	public static double getFeedValue(FeedIdentifier f, DateTime time) throws DataNotAvailableException
	{
		return getFeedValue(f.getId(), time);
	}
	
	
	/**
	 * Fetches all of the compressed datum between two dates.
	 * 
	 * @param dtr - The range of dates to pre-cache
	 * @param feedId - The id of the feed to fetch.
	 * 
	 * @throws DataNotAvailableException 
	 */
	public static void cacheClosestFeedValueRange(DateTimeRange dtr, int feedId) throws DataNotAvailableException
	{
		for(SmartGridProviderEventListener listener : listeners)
		{
			listener.onCacheRequest(dtr.getStartTime(), dtr.getEndTime(), feedId);
		}
		
		if(! m_isOnline)
		{
			throw new DataNotAvailableException("Not connected.");
		}
		
		TimeRange givenRange = new TimeRange(dtr.getStartTime(), dtr.getEndTime());
		
		Set<TimeRange> ranges = FETCHED_RANGES.get(feedId);
		if(ranges != null)
		{
			for(TimeRange tmp : ranges)
			{
				if(tmp.equals(givenRange) || tmp.contains(givenRange))
				{
					return;
				}
			}
		}
		
		// We're going to fetch the range, so create it.
		FETCHED_RANGES.add(feedId, givenRange);
		
		
		DataSet cache = POINT_CACHE.get(feedId);
		if(cache == null)
		{
			cache = new DataSet("Feed: " + feedId);
			POINT_CACHE.put(feedId, cache);
		}
		
		
		
		// Fetch the ranges for that time period, for each range we get back,
		// we'll create individual points for it within the acceptable range,
		// that way we don't have to deal with checking with colliding ranges,
		// and searches will be fast without overlap.
		try
		{
			FeedValueRange[] values = provider.feedValues(feedId, dtr.getStartTime().toDate(), dtr.getEndTime().toDate());
			for(FeedValueRange fvr : values)
			{
				DateTimeRange valuePoints = new DateTimeRange(new DateTime(fvr.startDate), new DateTime(fvr.endDate), FEED_ACCEPTABLE_RANGE.toSeconds());
				
				for(DateTime point : valuePoints)
				{
					cache.addPoint(point, fvr.value);
				}
				
				if(fvr.startDate.getTime() == fvr.endDate.getTime())
				{
					cache.addPoint(new DateTime(fvr.startDate),  fvr.value);
				}
			}
		} catch(IOException ex)
		{
			connectionWentOffline();
			throw new DataNotAvailableException(ex);
		}
	}

	
	@Override
	public void appendAllPossibleDataFeeds(Collection<DataFeed> feeds)
	{
		for(DataFeed df : currIds)
			feeds.add(df);
	}
	
	/**
	 * Provides a simple way to listen for events that come from the SmartGridProvider.
	 * 
	 * @param listener
	 */
	public static void addProviderListener(SmartGridProviderEventListener listener)
	{
		listeners.add(listener);
	}

	public static void removeProviderListener(SmartGridProviderEventListener listener)
	{
		listeners.remove(listener);
	}

	@Override
	public void setupHeadless()
	{		
	}

	@Override
	public void teardown()
	{		
	}
}
