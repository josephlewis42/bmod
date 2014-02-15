package bmod.gui.widgets;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import bmod.buildingactivity.BuildingActivityInterface;
import bmod.buildingactivity.BuildingActivityInterfaceFactory;
import bmod.buildingactivity.SingleTimeEvent;
import bmod.gui.builder.GUIBuilderWidget;

/**
 * Provides an interface for choosing an activity dialog and 
 * 
 * @author Joseph Lewis <joehms22@gmail.com> 2012-01-24
 *
 */
public class ActivityInterfaceEditor
{
	private static final Logger m_logger = Logger.getLogger("ActivityInterfacePanel");
	private final JComboBox<String> m_interfaceCombo = new JComboBox<>();
	private final JScrollPane m_interfaceScroller = new JScrollPane();
	private transient BuildingActivityInterface m_interface = new SingleTimeEvent("");
	private String m_optionString = "";
	private JPanel m_optionPanel = m_interface.getEditorPanel();

	/**
	 * Create the panel.
	 */
	public ActivityInterfaceEditor()
	{		
		m_interfaceCombo.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				onChange();
			}
		});
		
		setupCombo();
		
		m_interfaceScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	}
	
	public void init(int id, String options)
	{
		m_optionString = options;
		try
		{
			m_interface = BuildingActivityInterfaceFactory.getInstance(id, options);
			m_optionPanel = m_interface.getEditorPanel();
			m_interfaceCombo.setSelectedItem(m_interface.getHumanReadableName());
			m_interfaceScroller.setViewportView(m_optionPanel);
		} catch (IllegalArgumentException ex) {
			m_logger.error("Activity interface couldn't be created",ex);
		}
	}
	
	public void onChange()
	{
		try
		{
			int i = BuildingActivityInterfaceFactory.getNames().get(m_interfaceCombo.getSelectedItem().toString());

			init(i, m_optionString);
		} catch (Exception ex)
		{
			m_logger.error("The chosen string could not be found, something went horribly wrong!",ex);
		}
	}
	
	private void setupCombo()
	{
		for(String s : BuildingActivityInterfaceFactory.getNames().keySet())
		{
			m_interfaceCombo.addItem(s);
		}
	}

	
	public GUIBuilderWidget getInterfaceIdWidget()
	{
		return new GUIBuilderWidget("Activity Type")
		{
			@Override
			public boolean isContentChanged()
			{
				return true;
			}

			@Override
			public Object getValue()
			{
				return  m_interface.getInterfaceId();
			}

			@Override
			public Component getComponent()
			{
				return m_interfaceCombo;
			}
			
		};
	}
	
	public GUIBuilderWidget getEditorWidget()
	{
		return new GUIBuilderWidget("Activity Type")
		{
			@Override
			public boolean isContentChanged()
			{
				return true;
			}

			@Override
			public Object getValue()
			{
				return m_interface.getInterfaceTypeFromPanel(m_optionPanel);
			}

			@Override
			public Component getComponent()
			{
				return m_interfaceScroller;
			}
			
			@Override
			public boolean getMaxHeight()
			{
				return true;
			}
		};
	}
}
