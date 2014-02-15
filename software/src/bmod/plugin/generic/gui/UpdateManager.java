package bmod.plugin.generic.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import bmod.ExtensionPoints;
import bmod.gui.GuiExtensionPoints;

/**
 * Checks for updates with smartgrid.cs.du.edu and downloads them if applicable.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class UpdateManager extends GenericGuiPlugin implements Runnable
{
	private static final String COULD_NOT_FIND_UPDATES = "Could not find updates";
	private static final String RESTART_ERROR = "Could not restart for updates.";
	private static final String WRITE_ERROR = "Could not apply updates.";
	private static final String DOWNLOAD_FAIL = "Could not download updates.";
	private static final String UPDATED_STRING = "No Updates Available";
	private static final String RESTART_FOR_UPDATES = "Restart to apply updates";
	private static final String UPDATES_READY_MESSAGE = "Updates are ready to install, go to Help > " + RESTART_FOR_UPDATES + " to apply";
	private static final String VERSION_PREFIX = "Version: ";
	private static final String VERSION_PATH = "/static/bmod_app/version";
	private static final String UPDATE_EXEC_PATH = "/static/bmod_app/Bmod.jar";
	private static final String UPDATE_SUFFIX = "new";
	private static final long TIME_BETWEEN_UPDATE_CHECKS_MS = 10 * 60 * 1000; // ten minutes
	private final JMenuItem updatesButton = new JMenuItem(UPDATED_STRING);
	private final JMenuItem versionItem = new JMenuItem();
	private final JMenuItem[] buttonGroup = new JMenuItem[]{updatesButton,  versionItem};
	private boolean checkingForUpdates = true;
	private static final Logger m_logger = Logger.getLogger("Update Manager");

	@Override
	public void setup(GuiExtensionPoints environment)
	{
		versionItem.setEnabled(false);
		versionItem.setText(VERSION_PREFIX + getLocalVersion());
		environment.addMenuItem("Help", buttonGroup);
		
		updatesButton.setEnabled(false);
		updatesButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				restart();
			}
		});
		
		// background check for updates.
		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
	}

	@Override
	public void teardown()
	{
		replaceJar();
	}
	
	/**
	 * Gets the version of the current bmod on the server.
	 * 
	 * @return
	 */
	private static int getServerVersion()
	{
		String version = ExtensionPoints.readURL("http://" + ExtensionPoints.API_HOST + VERSION_PATH);

		if(version != null)
		{
			try
			{
				return Integer.parseInt(version.trim());
			} catch(NumberFormatException ex)
			{
				ex.printStackTrace();
			}
		}
		
		return -1;
	}
	
	private static int getLocalVersion()
	{
		try
		{
			return Integer.parseInt(ExtensionPoints.getBuildNumber().trim());
		} catch(NumberFormatException ex)
		{
		}
		
		return Integer.MAX_VALUE;
	}
	

	private static Path getNewVersionFile()
	{
		Path currentJar = ExtensionPoints.getcurrentJar();
		return Paths.get(currentJar.toString() + UPDATE_SUFFIX);
	}
	
	public void checkForUpdates()
	{		
		int serverVersion = getServerVersion();
		
		if(serverVersion < 0)
			return;
		
		if(serverVersion > getLocalVersion())
		{
			m_logger.info("Updates available");
			byte[] newversion = ExtensionPoints.readURLAsBytes("http://" + ExtensionPoints.API_HOST + UPDATE_EXEC_PATH);
			
			if(newversion == null)
			{
				GuiExtensionPoints.showWarning(DOWNLOAD_FAIL);
				m_logger.warn(COULD_NOT_FIND_UPDATES);
				return;
			}
			
			
			try
			{
				Files.write(getNewVersionFile(), newversion, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			} catch (IOException ex)
			{
				GuiExtensionPoints.showWarning(WRITE_ERROR);
				return;
			}
			
			updatesButton.setText(RESTART_FOR_UPDATES);
			GuiExtensionPoints.showInfo(UPDATES_READY_MESSAGE);
			checkingForUpdates = false;
			updatesButton.setEnabled(true);
		}
	}
	
	private void replaceJar()
	{
		Path currentJar = ExtensionPoints.getcurrentJar();
		
		if(!currentJar.getFileName().toString().endsWith(".jar"))
		{
			currentJar = Paths.get("Bmod.jar");
		}
		
		Path newFile = getNewVersionFile();
		
		try
		{
			Files.move(newFile, currentJar, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e1)
		{
			GuiExtensionPoints.showWarning(WRITE_ERROR);
		}
	}

	
	private void restart()
	{
		final String javaBin = Paths.get(System.getProperty("java.home"), "bin", "java").toString();

		replaceJar();


		// Build command: java -jar application.jar
		final ArrayList<String> command = new ArrayList<String>();
		command.add(javaBin);
		command.add("-jar");
		command.add(ExtensionPoints.getcurrentJar().toString());

		final ProcessBuilder builder = new ProcessBuilder(command);
		try
		{
			builder.start();
			System.exit(0);
		} catch (IOException e)
		{
			GuiExtensionPoints.showWarning(RESTART_ERROR);
		}
	}

	@Override
	public void run()
	{
		while(checkingForUpdates)
		{
			checkForUpdates();
			try
			{
				Thread.sleep(TIME_BETWEEN_UPDATE_CHECKS_MS);
			} catch (InterruptedException e)
			{
			}
		}
	}

	public UpdateManager()
	{
		super( "Updater",
				"A background process that will silently check for updates, and" +
				" apply them if found so you always have the newest Bmod without" +
				" having to remember to do anything!");
	}

}
