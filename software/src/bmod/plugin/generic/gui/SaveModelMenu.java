package bmod.plugin.generic.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JMenuItem;
import javax.swing.filechooser.FileNameExtensionFilter;

import bmod.ExtensionPoints;
import bmod.IconLoader;
import bmod.PredictionModel;
import bmod.gui.GuiExtensionPoints;
import bmod.gui.widgets.Dialogs;
import bmod.gui.widgets.JProgressDialog;
import bmod.gui.widgets.ProgressDialog;

public class SaveModelMenu extends GenericGuiPlugin
{
	
	private final JMenuItem saveButton = new JMenuItem("Save Simulation", IconLoader.SAVE_ICON);
	private final JMenuItem loadButton = new JMenuItem("Load Simulation", IconLoader.OPEN_ICON);
	private final JMenuItem loadRecent = new JMenuItem("Load Last Simulation");
	private final JMenuItem[] buttonGroup = new JMenuItem[]{saveButton, loadButton, loadRecent};
	private static final String BUTTON_GROUP_NAME = "Bmod";
	private static final Path AUTOSAVE_FILE = ExtensionPoints.getBmodDirectory("autosave.bsimx");
		
	PredictionModel m_model = null;
	boolean currentSaved = true;
	private GuiExtensionPoints m_environment = null;
	
	private static final FileNameExtensionFilter[] FILE_FILTERS = new FileNameExtensionFilter[]{
		new FileNameExtensionFilter("Compressed BMOD Simulation", "bsimz"),
		new FileNameExtensionFilter("BMOD Simulation", "bsimx")
	};
	

	
	@Override
	public void setup(final GuiExtensionPoints environment)
	{
		m_environment = environment;
		m_environment.addMenuItem(BUTTON_GROUP_NAME, buttonGroup);
		
		loadRecent.setEnabled(Files.exists(AUTOSAVE_FILE));
	}
	
	
	public void updateModel(Path path)
	{
		ProgressDialog d = new JProgressDialog("Updating", "Updating widgets for old run", 3);
		PredictionModel m = PredictionModel.deserialize(path);
		d.setProgress(1);
		
		if(m != null)
		{
			
			// Set this here, so we already have an instance of our own
			// model, so if we open another, we know this won't be "destoryed"
			m_model = m;
			
			ExtensionPoints.setCurrentPredictionModel(m);
		}
		d.close();
		
		if(m == null)
		{
			Dialogs.showErrorDialog("Error Opening Saved File", "The file you chose is corrupt or made by a newer version of bmod");
		}
	}

	@Override
	public void teardown()
	{
		if(m_environment != null)
		{
			m_environment.removeMenuItem(BUTTON_GROUP_NAME, buttonGroup);
		}
	}


	public SaveModelMenu()
	{
		super("Save Model Menu","A menu that allows you to save/reload simulations");
		
		
		loadRecent.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				updateModel(AUTOSAVE_FILE);
			}
			
		});
			
		
		saveButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(m_model == null)
				{
					GuiExtensionPoints.showInfo("Please run a model before trying to save it.");
					return;
				}
				
				Path path = Dialogs.showFileSaveDialog(FILE_FILTERS, true);
				
				if(path == null)
				{
					return;
				}
				
				try
				{
					m_model.compressSerialize(path);
				} catch (IOException ex)
				{
					m_environment.showError("The chosen file could not be created, the simulation was not saved.");
				}
				
				currentSaved = true;
			}
		});
			
		loadButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(! currentSaved)
				{
					if(! Dialogs.showYesNoQuestionDialog("Current Work Not Saved", "Your current simulation is not saved and will be destroyed if you open another.\nDo you want to continue?"))
					{
						return;
					}
				}
				
				
				Path path = Dialogs.showFileOpenDialog(FILE_FILTERS, true);
				
				if(path != null)
				{
					updateModel(path);
				}
			}
		});
	}
	
	@Override
	public void predictionModelChanged(PredictionModel model)
	{
		if(model == null || model == m_model)
		{
			return;
		}
		
		if(model != m_model)
		{
			currentSaved = false;
		}
		
		try
		{
			model.compressSerialize(AUTOSAVE_FILE);
		} catch(IOException ex)
		{
			m_environment.showError("Could not auto-save model.");
		}
		
		m_model = model;
		
		loadRecent.setEnabled(Files.exists(AUTOSAVE_FILE));
	}
}
