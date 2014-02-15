package bmod;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.apache.http.client.fluent.Request;

import bmod.database.DBWarningsList;
import bmod.database.DataFeed;
import bmod.database.objects.BuildingActivity;
import bmod.database.objects.Room;
import bmod.plugin.generic.gui.GenericGuiPlugin;
import bmod.plugin.loader.GenericGuiPluginLoader;
import bmod.plugin.loader.GenericHeadlessPluginLoader;
import bmod.util.DateTime;

/**
 * A class for extension points for this software.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public final class ExtensionPoints extends Constants
{
	private static PredictionModel currentModel = null;

	/**
	 * Returns the build identifier of this build or "TRUNK" if none is listed
	 * in the manifest.
	 */
	public static String getBuildNumber()
	{
		return new ManifestParser().getPropertyOrDefault("BuildNo","TRUNK");
	}
	
	/**
	 * Returns the current PredictionModel or null if none exists.
	 * 
	 * @return
	 */
	public static PredictionModel getCurrentPredictionModel()
	{
		return currentModel;
	}
	
	/**
	 * Sets the current PredictionModel to the given one.
	 * @param m
	 */
	public static void setCurrentPredictionModel(PredictionModel m)
	{
		if(m == currentModel)
		{
			return;
		}
				
		currentModel = m;
		
		// Tell anyone that is listening the model changed.
		for(GenericPlugin gp : getPlugins())
		{
			System.err.println("Updating: " + gp.getName() + " " + gp.getClass());
			gp.predictionModelChanged(currentModel);
		}
	}
	
	/**
	 * Reads a URL and returns the contents as a string.
	 * 
	 * @param URL
	 * @return the text of the page requested, or null if there was an error.
	 */
	public static String readURL(String URL)
	{
		try
		{
			return Request.Get(URL)
					.connectTimeout(HTTP_CONNECT_TIMEOUT_MS)
					.socketTimeout(HTTP_SOCKET_TIMEOUT_MS)
					.execute().returnContent().asString();
		} catch (Exception e)
		{
			return null;
		}
	}
	
	/**
	 * Reads a URL and returns the contents as a byte array.
	 * @param URL
	 * @return null for an error/timeout.
	 */
	public static byte[] readURLAsBytes(String URL)
	{
		try
		{
			return Request.Get(URL)
					.connectTimeout(HTTP_CONNECT_TIMEOUT_MS)
					.socketTimeout(HTTP_SOCKET_TIMEOUT_MS)
					.execute().returnContent().asBytes();
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * Returns a path to the current bmod file.
	 * 
	 * @return a file the represents the currently executing script, or 
	 * null if none was found.
	 */
	public static Path getcurrentJar()
	{
		try
		{
			return Paths.get(ExtensionPoints.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e)
		{
			return Paths.get("Bmod.jar");
		}
	}
	
	
	/**
	 * Returns a path to the building_modeler path that is being used; 
	 * automatically adapts to the user's system and bmod type.
	 * 
	 * @return A path to the building_modeler folder for which all settings are
	 * to be kept.
	 */
	public static Path getBmodDirectory()
	{
		Path internalPath = Paths.get(_PROJECT_DIRECTORY_NAME);
		if(internalPath.toFile().exists())
		{
			return internalPath;
		}
		
		Path realpath = Paths.get(System.getProperty("user.home"), _PROJECT_DIRECTORY_NAME);
		try
		{
			Files.createDirectories(realpath);
		} catch (IOException e)
		{
			System.err.println("Couldn't create parents");
		}
		
		return realpath;
	}
	
	/**
	 * Returns a Path to the given filename within the bmod settings directory.
	 * 
	 * @param filename
	 * @return
	 */
	public static Path getBmodDirectory(String filename)
	{
		Path realpath = Paths.get(getBmodDirectory().toString(), filename);
		return realpath;
	}
	
	/**
	 * Returns a list of all known data feeds.
	 * @return
	 */
	public static Collection<DataFeed> getAllDataFeeds()
	{
		ArrayList<DataFeed> feeds = new ArrayList<DataFeed>();
		
		for(GenericPlugin dfp : GenericHeadlessPluginLoader.getAllPlugins())
		{
			dfp.appendAllPossibleDataFeeds(feeds);
		}
		
		Collections.sort(feeds, new Comparator<DataFeed>(){
			@Override
			public int compare(DataFeed o1, DataFeed o2)
			{
				return o1.getFeedName().toLowerCase().compareTo(o2.getFeedName().toLowerCase());
			}});
		
		return feeds;
	}

	
	/**
	 * Returns a list of all Generic Plugins
	 * @return
	 */
	public static LinkedList<GenericPlugin> getHeadlessPlugins()
	{
		return GenericHeadlessPluginLoader.getAllPlugins();
	}
	
	/**
	 * Returns a list of all GenericGuiPlugins
	 * @return
	 */
	public static LinkedList<GenericGuiPlugin> getGuiPlugins()
	{
		return GenericGuiPluginLoader.getEnabledPlugins();
	}
	
	/**
	 * Returns a list of all plugins, cast to GenericPlugins
	 * @return
	 */
	public static LinkedList<GenericPlugin> getPlugins()
	{
		LinkedList<GenericPlugin> gps = new LinkedList<GenericPlugin>();
		
		gps.addAll(getGuiPlugins());
		gps.addAll(getHeadlessPlugins());
		
		return gps;
	}
	
	/**
	 * 
	 * 
	 * STUFF TAKEN FROM MinerPluginLoader.java
	 * 
	 * 
	 */
	
	public static int maxThreadsHook(int maxThreads)
	{
		for(GenericPlugin mp : getPlugins())
			maxThreads = mp.maxThreadsHook(maxThreads);
		
		return maxThreads;
	}

	public static void minerStartHook(DBWarningsList wl, PredictionModel pm)
	{
		for(GenericPlugin mp : getPlugins())
			mp.minerStartHook(wl, pm);
	}

	public static void minerEndHook(DBWarningsList wl, PredictionModel pm)
	{
		for(GenericPlugin mp : getPlugins())
			mp.minerEndHook(wl, pm);	
	}

	public static void minerThreadStartedHook(DBWarningsList wl, PredictionModel pm, Room rm)
	{
		for(GenericPlugin mp : getPlugins())
			mp.minerThreadStartedHook(wl, pm, rm);
	}

	public static void minerThreadEndedHook(DBWarningsList wl, PredictionModel pm, Room rm)
	{
		for(GenericPlugin mp : getPlugins())
			mp.minerThreadEndedHook(wl, pm, rm);
	}

	public static DateTime minerActivityStartTimeHook(DateTime start)
	{
		for(GenericPlugin mp : getPlugins())
			start = mp.minerActivityStartTimeHook(start);
		
		return start;
	}

	public static DateTime minerActivityEndTimeHook(DateTime end)
	{
		for(GenericPlugin mp : getPlugins())
			end = mp.minerActivityEndTimeHook(end);
		
		return end;
	}

	public static DateTime minerFunctionStartTimeHook(DateTime start)
	{
		for(GenericPlugin mp : getPlugins())
			start = mp.minerFunctionStartTimeHook(start);
		
		return start;
	}

	public static DateTime minerFunctionEndTimeHook(DateTime end)
	{
		for(GenericPlugin mp : getPlugins())
			end = mp.minerFunctionEndTimeHook(end);
		
		return end;
	}

	public static void minerActivityWattageEventReturnedHook(
			Collection<WattageEvent> events, BuildingActivity ba)
	{
		for(GenericPlugin mp : getPlugins())
			mp.minerActivityWattageEventReturnedHook(events, ba);
		
	}

	public static void minerFunctionWattageEventReturnedHook(
			Collection<WattageEvent> events, BuildingActivity ba)
	{
		for(GenericPlugin mp : getPlugins())
			mp.minerFunctionWattageEventReturnedHook(events, ba);		
	}
}
