package bmod.plugin.generic.headless;

import java.util.Calendar;
import java.util.Collection;

import bmod.Constants;
import bmod.GenericPlugin;
import bmod.database.DataFeed;
import bmod.database.DataNotAvailableException;
import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;

import com.luckycatlabs.sunrisesunset.Location;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

/**
 * Provides feeds for sunrise, sunset, and amount of daylight in a day.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class Sunrise extends GenericPlugin implements DataFeed
{
	private static final String SUNRISE_STATE = "Time: Sunrise (Minutes past Midnight)";
	private static final String SUNSET_STATE = "Time: Sunset (Minutes past Midnight)";
	private static final String DIFF_STATE = "Time: Amount of Daylight (Minutes)";
	private static final Location LOCATION = new Location(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE);
	private static final SunriseSunsetCalculator CALCULATOR = new SunriseSunsetCalculator(LOCATION, Constants.DEFAULT_TIME_ZONE);
	private static final int MS_PER_MIN = 1000 * 60;
	
	private final String my_state;
	
	public Sunrise()
	{
		this(SUNRISE_STATE);
	}
	
	private Sunrise(String stateName)
	{
		super("Sunrise Data Feed", "Provides information as to when sunrise/sunset will be.");
		my_state = stateName;
	}
	
	
	@Override
	public void appendAllPossibleDataFeeds(Collection<DataFeed> feeds)
	{
		feeds.add(new Sunrise(SUNRISE_STATE));
		feeds.add(new Sunrise(SUNSET_STATE));
		feeds.add(new Sunrise(DIFF_STATE));
	}

	@Override
	public double getDataAtTime(DateTime t) throws DataNotAvailableException
	{
		Calendar time = t.toCalendar();
		
		DateTime sunrise = new DateTime(CALCULATOR.getCivilSunriseCalendarForDate(time).getTime());
		DateTime sunset = new DateTime(CALCULATOR.getCivilSunsetCalendarForDate(time).getTime());
		
		switch(my_state)
		{
			case SUNRISE_STATE:
				return sunrise.getTimeOfDay() / MS_PER_MIN;
			case SUNSET_STATE:
				return sunset.getTimeOfDay() / MS_PER_MIN;
			default:
				return (sunset.getTimeOfDay() - sunrise.getTimeOfDay()) / MS_PER_MIN;
		}
	}

	@Override
	public void preCache(DateTimeRange precache)
			throws DataNotAvailableException
	{
		// Don't worry about pre-caching.
	}

	@Override
	public String getFeedName()
	{
		return my_state;
	}
	
	@Override
	public String toString()
	{
		return getFeedName();
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
