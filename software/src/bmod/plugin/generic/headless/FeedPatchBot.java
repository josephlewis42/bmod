package bmod.plugin.generic.headless;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import bmod.GenericPlugin;
import bmod.database.DataNotAvailableException;
import bmod.util.DateTime;
import bmod.util.TimeDelta;
import edu.du.cs.smartgrid.SmartGridProviderEventListener;

/**
 * A bot that looks for gaps in the data we get from the server, and patches
 * it up with generated values.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 */
public class FeedPatchBot extends GenericPlugin implements SmartGridProviderEventListener
{
	private int numNoValues;
	private final TimeDelta MULTIDAY_INTERPOLATION_POINT = new TimeDelta(0,3);
	// must include 0 to get present time.
	private final int[] PATCH_DAY_OFFSETS = new int[]{0, -1, 1, -2, 2, 3, -3};
	private static final Set<String> interpolatedFeeds = new TreeSet<String>();
	
	public FeedPatchBot()
	{
		super("Feed Patch Bot", "A small bot that patches up missing values in data feeds.");
	}

	@Override
	public void setupHeadless()
	{
		SmartGridProvider.addProviderListener(this);
		System.err.println("Loading FeedPatchBot");
	}

	@Override
	public void teardown()
	{
		SmartGridProvider.removeProviderListener(this);
		System.err.println("Feed No Value Lookups " + numNoValues);
	}
	
	@Override
	public void onCacheRequest(DateTime start, DateTime end, int feedid)
	{
	}

	@Override
	public void onServerOffline()
	{
	}

	@Override
	public void onServerConnected()
	{
	}
	
	/**
	 * Generates a report on the interpolated feeds and erases the current
	 * values.
	 * 
	 * @return
	 */
	public static String getInterpolatedFeedsReport()
	{
		synchronized(interpolatedFeeds)
		{
			StringBuilder sb = new StringBuilder();
			for(String tmp : interpolatedFeeds)
			{
				sb.append(tmp);
				sb.append("\n");
			}
			
			interpolatedFeeds.clear();
			
			return sb.toString();
		}
	}
	
	private final class LinearEquation
	{
		private final double m_m;
		private final double m_b;
		
		public LinearEquation(double m, double b)
		{
			m_m = m;
			m_b = b;
		}
		
		public LinearEquation(double x1, double y1, double x2, double y2)
		{
			m_m = (y2 - y1) / (x2 - x1);
			m_b = y1 - (m_m * x1);
		}
		
		public double getYIntercept()
		{
			return m_b;
		}
		
		public double getValue(double x)
		{
			return (m_m * x) + m_b;
		}
	}
	
	private final class ClosestValue implements Comparable<ClosestValue>
	{
		private Entry<DateTime, Double> before = null, after = null;
		private final DateTime feedTime, original;
		private final int feedId;
		
		public ClosestValue(int feed_id, DateTime originalFeedTime, TimeDelta offset)
		{
			original = originalFeedTime;
			feedTime = originalFeedTime.plusTime(offset);
			feedId = feed_id;
			
			try
			{
				before = SmartGridProvider.getClosestBeforeValue(feedId, feedTime);
				after = SmartGridProvider.getClosestAfterValue(feedId, feedTime);
			} 
			catch (DataNotAvailableException e)
			{
				// don't worry about that here
			}
		}
		
		public TimeDelta getClosestPointsDifference()
		{
			if(before != null && after != null)
			{
				return after.getKey().subtract(before.getKey());
			}
			
			return TimeDelta.MAX_VALUE;
		}
		
		public double getLinearInterpolation(double default_value)
		{
			if(before != null && after != null)
			{
				return new LinearEquation(before.getKey().getTime(),
										  before.getValue(),
										  after.getKey().getTime(),
										  after.getValue())
											.getValue(feedTime.getTime());
			}
			
			if(before != null)
			{
				return before.getValue();
			}
			
			if(after != null)
			{
				return after.getValue();
			}
			
			return default_value;
		}

		@Override
		public int compareTo(ClosestValue o)
		{
			return getClosestPointsDifference().compareTo(o.getClosestPointsDifference());
		}
		
		@Override
		public String toString()
		{
			
			if(original.getTime() == feedTime.getTime())
			{
				return String.format("Interpolated feed within %s", MULTIDAY_INTERPOLATION_POINT);
			}
			
			return String.format("Set feed %d to %f. Original Time: %s Shifted Time: %s", 
					feedId, 
					getLinearInterpolation(-999), 
					original, 
					feedTime);
		}
	}

	@Override
	public double onNoFeedValue(int feedId, DateTime feedTime, double finalValue)
	{
		numNoValues++;			
		
		ArrayList<ClosestValue> cva = new ArrayList<ClosestValue>(PATCH_DAY_OFFSETS.length);
		
		
		for(int offsetDays : PATCH_DAY_OFFSETS)
		{
			ClosestValue present = new ClosestValue(feedId, feedTime, new TimeDelta(offsetDays));
			
			// If the given day has close enough points just return it's value.
			if(present.getClosestPointsDifference().compareTo(MULTIDAY_INTERPOLATION_POINT) <= 0)
			{
				synchronized(interpolatedFeeds)
				{
					interpolatedFeeds.add(present.toString());
				}
				return present.getLinearInterpolation(finalValue);
			}
			
			cva.add(present);
		}
		
		Collections.sort(cva);
		
		synchronized(interpolatedFeeds)
		{
			interpolatedFeeds.add(cva.get(0).toString());
		}
		return cva.get(0).getLinearInterpolation(finalValue);
		
	}
	
	/** Buggy old code, just in case Jon needs it again.
	 * 
	private final int[] PATCH_DAY_OFFSETS = new int[]{-1,-2,-3};

	@Override
	public double onNoFeedValue(int feedId, DateTime feedTime, double finalValue)
	{
		numNoValues++;			
		
		ArrayList<ClosestValue> cva = new ArrayList<ClosestValue>(PATCH_DAY_OFFSETS.length);
		
		ClosestValue present = new ClosestValue(feedId, feedTime, new TimeDelta(0));

		if(present.getClosestPointsDifference().compareTo(MULTIDAY_INTERPOLATION_POINT) <= 0)
		{
			synchronized(interpolatedFeeds)
			{
				interpolatedFeeds.add(present.toString());
			}
			return present.getLinearInterpolation(finalValue);
		}
		
		for(int offsetDays : PATCH_DAY_OFFSETS)
		{
			present = new ClosestValue(feedId, feedTime, new TimeDelta(offsetDays));
			
			cva.add(present);
		}
		
		Collections.sort(cva);
		
		synchronized(interpolatedFeeds)
		{
			interpolatedFeeds.add(cva.get(0).toString());
		}
		return cva.get(0).getLinearInterpolation(finalValue);
	}**/
}
