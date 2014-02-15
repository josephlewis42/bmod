/** MJR: Top-level entry point for the bmod software system
 *  JAL: When program is run as an Applet, the gui/Applet.java
 *  file will be used instead, no params will be generated/used.
 */

import java.io.IOException;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import bmod.Constants;
import bmod.ExtensionPoints;
import bmod.ForkMiner;
import bmod.PredictionModel;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.database.objects.Building;
import bmod.gui.SwingSet;
import bmod.gui.widgets.TProgressDialog;
import bmod.plugin.loader.GenericHeadlessPluginLoader;
import bmod.util.DateTime;
import edu.du.cs.smartgrid.Poster;
import edu.du.cs.smartgrid.UserOperations;

public class Bmod
{
	private final static String USAGE = "java bmod <params>\njava bmod iconics [options]";
	private final static PatternLayout LOGGER_LAYOUT = new PatternLayout("%d{ABSOLUTE} %c %-5p: %m %n");
	
	public static void main(String[] args)
	{
		setupLogging();
		
		// If we want the Gui, run it...
	    if(args.length == 0)
	    {
	    	runGui();
	    	return;
	    }
	
		// create the Options
		Options options = new Options();
		options.addOption("s", "start", true, "an ISO 8601 date string the program starts generating from: i.e. '1776-07-04 12:00:00'");
		options.addOption("e", "end", true, "an ISO 8601 date string the program stops generating data at");
		options.addOption("i", "interval", true, "the interval to generate at in seconds [default: 900]" );
		options.addOption("b", "building", true, "the name of the building to generate data for");
		options.addOption("d", true, "The device key to use for posting.");
		options.addOption("u", true, "The optional user key to use for posting.");
		options.addOption("host", true, "The hostname to fetch from and post to.");
		
		if(args[0].equals("-h") || args[0].equals("--help"))
		{
			printHelp("", options);
			return;
		}
		
		try 
		{
		    // parse the command line arguments
		    CommandLine line = new PosixParser().parse( options, args );
		   
		    if(!line.hasOption('s') || !line.hasOption('e') || !line.hasOption('b'))
		    {
		    	printHelp("Must have args: s,e,b", options);
		    	return;
		    }
		    
		    
		    Database.getDqm().updateDatabase(); // update the database to be the most recent version.
			new GenericHeadlessPluginLoader();  // Dynamically load plugins that manipulate output.
		    
		    DateTime startTime 	= new DateTime(line.getOptionValue('s'));
		    DateTime endTime 	= new DateTime(line.getOptionValue('e'));
		    int interval 		= Integer.parseInt(line.getOptionValue('i', "900"));
		    String userKey		= line.getOptionValue('u', null);
		    final String host 		= line.getOptionValue("host", Constants.API_HOST);
		    final String building	= line.getOptionValue('b');
		    
			// check the building
		    Building m_building;
			try
			{
				m_building = Building.getBuildingByName(line.getOptionValue('b'));
			} 
			catch(DatabaseIntegrityException ex)
			{
				System.out.println(String.format("ERROR: invalid building name: %s, choose from:\n", building));
				
				for(Building tmp : Database.templateBuilding.readAll())
				{
					System.out.print(tmp.getId());
					System.out.print(", ");
				}
				printHelp("", options);
				return;
			}
			
			
			// Do calculations!
			PredictionModel mod = new PredictionModel(startTime, endTime, interval, m_building.getPrimaryKey());
			ForkMiner m = new ForkMiner(mod, new TProgressDialog("Miner Progress", "", 0));
			
			System.err.println(m.getRuntimeErrors().toString());
			
			// Now post the data, or just show it as a CSV
			if(!line.hasOption('d'))
			{
				System.out.println("Date,Value");
				for(Entry<DateTime, Double> entry : mod.getEstimatedKWData().entrySet())
				{
					System.out.println(entry.getKey().toISODate() + "," + entry.getValue());
				}
			}
			else
			{
				UserOperations uo = (userKey != null) ? new UserOperations(userKey, host) : null;
			    Poster post = new Poster(line.getOptionValue('d'), uo, host);
				final String feedID = building + " Prediction";
				
				for(Entry<DateTime, Double> entry : mod.getEstimatedKWData().entrySet())
				{
					post.add_data(entry.getKey(), entry.getValue(), feedID);
					System.out.println(entry.getKey().toISODate() + " -> " + entry.getValue());
				}
				
				post.post_data();
			}
			
		} catch (Exception e) 
		{
			Logger.getLogger("").error(e.getMessage(),e);
			printHelp("", options);
		}
	}
	
	private static void printHelp(String s, Options o)
	{
		if(s != null && !s.equals(""))
		{
			System.out.println("Error: " + s);
		}
		
		new HelpFormatter().printHelp(USAGE, o);
	}
	
	/**
	 * Sets up logging in the appropriate file for the software.
	 */
	private static void setupLogging()
	{
		try
		{
			String bmod_directory =  ExtensionPoints.getBmodDirectory("log.txt").toString();
			
			FileAppender appender = new FileAppender(LOGGER_LAYOUT, bmod_directory, false);    
			Logger.getRootLogger().addAppender(appender);
			Logger.getRootLogger().setLevel(Level.INFO);
		}
		catch(IOException ex)
		{
			System.err.println("Could not set up logging file.");
		}
	}
	
	/**
	 * Runs the GUI for the user
	 */
	private static void runGui()
	{
		System.out.println("Starting the GUI environment, run with --help to get command line options.");
    	new SwingSet();
	}
}