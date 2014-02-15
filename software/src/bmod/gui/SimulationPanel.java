package bmod.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import bmod.ConfigReader;
import bmod.ExtensionPoints;
import bmod.ForkMiner;
import bmod.IconLoader;
import bmod.PredictionModel;
import bmod.database.DBWarningsList;
import bmod.database.Database;
import bmod.database.objects.Building;
import bmod.gui.widgets.CSVRecordJComboBox;
import bmod.gui.widgets.DatabaseLoadOutput;
import bmod.gui.widgets.DateTimeRangeChooser;
import bmod.gui.widgets.Dialogs;
import bmod.gui.widgets.JProgressDialog;
import bmod.gui.widgets.ProgressDialog;
import bmod.gui.widgets.VerticalPanel;
import bmod.util.DateTime;
import bmod.util.TimeDelta;

/**
 * The main panel that you can run simulations from.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class SimulationPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private final DateTimeRangeChooser timeChooser = new DateTimeRangeChooser();
	protected CSVRecordJComboBox<Building> buildingChooser;
	private final JButton generateData = new JButton("Run Simulation", IconLoader.COG_GO);
	private static final DatabaseLoadOutput modelLog = new DatabaseLoadOutput();
	
	private static final TimeDelta LONG_DATE_WARNING_TIME_DIFF = new TimeDelta(180);


	private static final transient Logger m_logger = Logger.getLogger("SimulationPanel");

	public SimulationPanel()
	{
		buildingChooser = new CSVRecordJComboBox<Building>(new Building());

		setLayout(new BorderLayout(0,0));
		// Lay out the top pane
		
		JToolBar topBar = new JToolBar();
		topBar.add(new JLabel("Building "));
		topBar.add(buildingChooser);
		topBar.add(Box.createHorizontalGlue());
		topBar.add(generateData);
		topBar.setFloatable(false);
		
		add(new VerticalPanel(topBar,
				timeChooser), BorderLayout.NORTH);
		generateData.addActionListener(this);
		
		
		add(GuiExtensionPoints.getOutputWidgetPane(), BorderLayout.CENTER);		
		GuiExtensionPoints.getOutputWidgetPane().addTab("Log", null, modelLog, null);
		
		
		// Try re-registering values in components 
		long sd = ConfigReader.getLong("StartTime", Long.MAX_VALUE);
		long ed = ConfigReader.getLong("EndTime", Long.MAX_VALUE);
		
		DateTime start, end;
		
		if(sd != Long.MAX_VALUE)
			start = new DateTime(sd);
		else
			start = new DateTime();
		
		if(ed != Long.MAX_VALUE)
			end = new DateTime(ed);
		else
			end = new DateTime();
		
		timeChooser.setTimes(start, end);

		int inc = ConfigReader.getInt("Interval", -1);
		if(inc != -1)
			timeChooser.setStep(inc);
		
		String bldg = ConfigReader.getString("Building", "");
		if(!bldg.equals(""))
			buildingChooser.setSelectedItem(bldg.trim());
	}
	
	/**
	 * Handles all of the actions for the MainWindow.
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{	
		if(e.getSource() != generateData)
			return;
		
		// Create the starting time by taking the date chooser's time, setting it to midnight, 
		// then adding the time from the time chooser.
		final Building selectedBuilding = buildingChooser.getSelectedItem();
		
		final DateTime startDT = timeChooser.getRange().getStartTime();
		final DateTime endDT = timeChooser.getRange().getEndTime();
		final long interval = timeChooser.getRange().getStep();
		
		// Ensure we warn the user of long dates.
		if(startDT.plusTime(LONG_DATE_WARNING_TIME_DIFF).before(endDT) &&
			! Dialogs.showYesNoQuestionDialog("Large Date Warning", 
												"Running a simulation this long is going to be very slow, would you like to continue?"))
		{
			return;
		}
		
		
		
		final ProgressDialog monitor = new JProgressDialog("", "", 100);
		monitor.setNote("Generating model...");
		
		GuiExtensionPoints.showWaitCursor();

		@SuppressWarnings("rawtypes")
		SwingWorker worker = new SwingWorker() 
		{
			@Override
			protected Object doInBackground() throws Exception
			{
						
					monitor.setMaximum(3);
					monitor.setNote("Saving Database");
					Database.getDqm().save();
					monitor.setProgress(1);
					monitor.setNote("Clearing Database");
					Database.getDqm().updateCommitNumber();
					monitor.setProgress(2);
				
					monitor.setNote("Doing integrity check.");
					//Skip this for now, as it was just slowing everything down.
					if(initDatabase(selectedBuilding.getPrimaryKey()))
					{
						
						if(! Dialogs.showYesNoQuestionDialog("Database Error", "Errors were detected in your database configuration, \n they should be fixed before continuing.\n"+
												"Should I try to run anyway?"))
							{
								monitor.close();
								return null;
							}
					}
																						
					monitor.setNote("Setting up the prediction model...");
					PredictionModel predictionModel = new PredictionModel(startDT, endDT, interval, selectedBuilding.getPrimaryKey());
					
					monitor.setNote("Starting the miner...");
					ForkMiner m = new ForkMiner(predictionModel, monitor);	
					monitor.setProgress(0);
					
					monitor.setNote("Generating logging...");
					
					modelLog.clearAll();
					modelLog.append(m.getRuntimeErrors());
					
					if(m.getRuntimeErrors().hasErrors())
					{
						Dialogs.showWarningDialog("Database Error", "Errors were found while running the model\nthe data generated may be incorrect."+
								"You may view them in the Model Log tab");
					}						
					
					monitor.setNote("Updating displays.");
					
					ExtensionPoints.setCurrentPredictionModel(predictionModel);
					monitor.close();

					return null;
				}
			
				@Override
				public void done()
				{
					GuiExtensionPoints.hideWaitCursor();
				}
			};
			
			worker.execute();
	}
	
	
	/**
	 * Sets up the database and checks it for errors.
	 */
	protected boolean initDatabase(long buildingId)
	{
		DBWarningsList dwl = Database.getDqm().getIntegrityCheck(buildingId);
		
		if(modelLog != null)
			modelLog.append(dwl);
		
		
		m_logger.debug("Finishing init db");
		return dwl.hasErrors();
	}
}
