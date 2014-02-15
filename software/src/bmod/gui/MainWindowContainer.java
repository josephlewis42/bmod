package bmod.gui;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.RootPaneContainer;

import org.apache.log4j.Logger;

import bmod.ExtensionPoints;
import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;
import bmod.gui.widgets.Splash;
import bmod.plugin.loader.GenericGuiPluginLoader;

/**
 * The MainWindowContainer provides an interface that can be embedded within
 * either a Swing Application, or Applet.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 * Originally part of CloneAnt
 * Copyright 2011, Apache License
 */
public class MainWindowContainer extends Container 
{
	private static final long serialVersionUID = 6680845837845480095L;

	private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
	private static final transient Logger m_logger = Logger.getLogger("MainWindowContainer");
	private final GenericGuiPluginLoader m_loader;
	
	/**
	 * Creates a new MainWindow whose parentFrame is given by parentFrame and
	 * whose game it controls is g.
	 * 
	 * @param parentFrame
	 *            - The owner of this window.
	 * @param g
	 *            - The game this frame displays.
	 * @throws DatabaseIntegrityException 
	 */
	public MainWindowContainer(RootPaneContainer parentFrame)
	{
		SwingSet splash = new SwingSet("Bmod Loading", new Splash(), JFrame.DISPOSE_ON_CLOSE, true);
		splash.setVisible(true);
		Splash.setPrefix("Version: " + ExtensionPoints.getBuildNumber() + " | ");
		
		Splash.setLabel("Loading Database");
		Database.getDqm();
		
		Splash.setLabel("Setting up extension points");
		GuiExtensionPoints points = new GuiExtensionPoints(parentFrame);
		
		Splash.setLabel("Loading Main Plugins");
		m_loader = new GenericGuiPluginLoader(points);
		
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(points.getMainMenu(), BorderLayout.PAGE_START);
		topPanel.add(points.getInlineDialog(), BorderLayout.CENTER);
		
		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.PAGE_START);
		add(tabbedPane, BorderLayout.CENTER);		
		
		tabbedPane.addTab("Model", new SimulationPanel());
		
		Splash.setLabel("Setting up database editor");
		tabbedPane.addTab("Database", new NewDatabaseEditor());
		
		Splash.setLabel("Setting up multiple regression");
		tabbedPane.addTab("Multiple Reg.", new MultipleRegression());

		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    @Override
			public void run() {

		    	m_logger.info("Shutting down the gui plugins");
		    	m_loader.teardown();
		    	
		    	m_logger.info("Shutting down database...");
		    	Database.getDqm().shutdown();
		    	m_logger.info("Bye");
		    }
		});
		
		splash.dispose();
	}
}
