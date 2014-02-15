package bmod.plugin.baseclass;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import bmod.PredictionModel;
import bmod.gui.GuiExtensionPoints;
import bmod.plugin.generic.gui.GenericGuiPlugin;

public abstract class OutputWidget extends GenericGuiPlugin implements ChangeListener
{
	private final String m_title;
	protected final Logger m_logger = Logger.getLogger(this.getClass());
	private final ImageIcon m_icon;
	private PredictionModel m_model;
	private boolean generated = false;
	protected final JPanel outputWidgetPanel = new JPanel();
	
	public OutputWidget(String title, String description, ImageIcon icon)
	{
		super(title, description);
		m_title = title;
		m_icon = icon;
	}

	public abstract void generate(PredictionModel model);

	@Override
	public void setup(GuiExtensionPoints environment)
	{
		GuiExtensionPoints.addOutputWidget(m_title, m_icon, outputWidgetPanel);
		GuiExtensionPoints.getOutputWidgetPane().addChangeListener(this);
	}

	@Override
	public void teardown()
	{
		GuiExtensionPoints.removeOutputWidget(outputWidgetPanel);
		GuiExtensionPoints.getOutputWidgetPane().removeChangeListener(this);
	}
	

	@Override
	public void predictionModelChanged(PredictionModel model)
	{
		m_model = model;
		generated = false;
		updateIfNeeded();
	}

	@Override
	public void stateChanged(ChangeEvent evt)
	{
		updateIfNeeded();
	}
	
	private void updateIfNeeded()
	{
		if(generated || m_model == null)
		{
			return;
		}
		
		if(GuiExtensionPoints.getOutputWidgetPane().getSelectedComponent() == outputWidgetPanel)
		{
			generate(m_model);
			generated = true;
		}
	}
}