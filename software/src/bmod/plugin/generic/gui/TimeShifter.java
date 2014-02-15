package bmod.plugin.generic.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bmod.PredictionModel;
import bmod.WattageEvent;
import bmod.database.DBWarningsList;
import bmod.database.objects.BuildingActivity;
import bmod.gui.GuiExtensionPoints;
import bmod.util.DateTime;
import bmod.util.TimeDelta;

/**
 * Displays options for shifting the time.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class TimeShifter extends GenericGuiPlugin implements ActionListener, ChangeListener
{
	private static boolean isActive = false;
	private static long shiftingAmount = 0;
	
	private final JMenuItem m_menuItem = new JMenuItem("Shift Feed Time");
	private final JMenuItem[] m_menu = new JMenuItem[]{m_menuItem};
	
	private final JCheckBox m_checkbox = new JCheckBox("Enable time shifting", isActive);
	private final JSpinner m_spinner = new JSpinner();
	private GuiExtensionPoints m_environment;
	
	private final JPanel m_internalPane = new JPanel();
	
	public TimeShifter()
	{
		super("Time Shifter", 
				"Instead of looking for activities at the current time, shifts" +
				" to looking for them in the future or past by n seconds. " +
				"Useful if you want the current feeds, but a new schedule.");
		
		m_internalPane.setLayout(new BoxLayout(m_internalPane, BoxLayout.PAGE_AXIS));
		
		m_internalPane.add(m_checkbox);
		m_internalPane.add(new JLabel("Amount of time to shift looking for activities in seconds:"));
		m_internalPane.add(m_spinner);
		
		m_checkbox.addChangeListener(this);
		m_spinner.addChangeListener(this);
		m_menuItem.addActionListener(this);
	}
	
	@Override
	public void stateChanged(ChangeEvent evt)
	{
		if(evt.getSource() == m_spinner)
		{
			shiftingAmount = (Integer) m_spinner.getValue();
		}
		
		if(evt.getSource() == m_checkbox)
		{
			isActive = m_checkbox.isSelected();
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if(arg0.getSource() == m_menuItem)
		{
			m_environment.showDialog("Time Shifter", m_internalPane);
		}
	}

	@Override
	public void setup(GuiExtensionPoints environment)
	{
		m_environment = environment;
		environment.addMenuItem("Simulation", m_menu);
	}

	@Override
	public void teardown()
	{
		m_environment.removeMenuItem("Simulation", m_menu);
	}
		
	@Override
	public void minerStartHook(DBWarningsList wl, PredictionModel pm)
	{
		// setup from plugin
		
		if(isActive)
			wl.addWarning("Delorian plugin is active! Fetching activities: "+
					shiftingAmount +
					"in the future and shifting the values they produce back.");
	}
	
	@Override
	public DateTime minerActivityStartTimeHook(DateTime start)
	{
		if(isActive)
			return start.plusTime(new TimeDelta(0,0,shiftingAmount));
		return start;
	}

	@Override
	public DateTime minerActivityEndTimeHook(DateTime end)
	{
		if(isActive)
			return end.plusTime(new TimeDelta(0,0,shiftingAmount));
		return end;
	}
	
	@Override
	public void minerActivityWattageEventReturnedHook(
			Collection<WattageEvent> events, BuildingActivity ba)
	{
		if(! isActive)
			return;

		for(WattageEvent w : events)
			w.setStartTime(w.getStartTime().plusTime(new TimeDelta(0,0,- shiftingAmount)));
	}
}
