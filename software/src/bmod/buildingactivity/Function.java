package bmod.buildingactivity;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bmod.plugin.baseclass.ActivityPlugin;
import bmod.plugin.loader.WattageCalculator;
import bmod.util.DateTime;

public class Function extends BuildingActivityInterface
{
	
	protected String m_functString, m_propertiesString;
	protected static final WattageCalculator m_wattageCalculator = new WattageCalculator();
	
	public Function(String optionString)
	{
		super(optionString);

		m_functString = getProp("function", "ActivityPlugin");
		try
		{
			m_propertiesString = getProp("properties", "");
			
		} catch (Exception e)
		{
			m_logger.error(e.getMessage(),e);
			m_propertiesString = "";
		}
	}

	@Override
	public boolean happensWithin(DateTime start, DateTime end)
	{
		// Always happens, unless the function says otherwise, but we don't 
		// care about that right now.
		return true;
	}

	@Override
	public int getInterfaceId()
	{
		return "Function".hashCode();
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public FunctionEditorPanel getEditorPanel()
	{
		return new FunctionEditorPanel();
	}

	@Override
	public String getInterfaceTypeFromPanel(JPanel m_panel)
	{
		if(m_panel instanceof FunctionEditorPanel)
		{
			FunctionEditorPanel mp = (FunctionEditorPanel) m_panel;
			return buildInterfaceString(
					"function", mp.getFunctName()
			);
		}
		throw new IllegalArgumentException("The given panel does not represent this activity");
	}

	@Override
	public String getHumanReadableName()
	{
		return "Function Defined";
	}

	@Override
	public String getHumanString()
	{
		return "Function: " + m_functString + " Props: "+m_propertiesString;
	}
	
	public String getFunctString()
	{
		return m_functString;
	}
	
	/**
	 * Returns the function's properties.
	 * @return
	 */
	public String getProperties()
	{
		return m_propertiesString;
	}
	
	public class FunctionEditorPanel extends JPanel 
	{
		private final JComboBox<String> chooser;
		
		private static final long serialVersionUID = 1L;
		public FunctionEditorPanel()
		{
			setLayout(new GridBagLayout());
			
			JLabel lblNewLabel_1 = new JLabel("Function");
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 1;
			c.gridheight = 1;
			add(lblNewLabel_1, c);
			
			Collection<ActivityPlugin> plugins = m_wattageCalculator.getPlugins();
			String[] a = new String[plugins.size()];
			
			int i = 0;
			for(ActivityPlugin p : plugins)
			{
				a[i] = p.getName();
				i++;
			}
			
			chooser = new JComboBox<String>(a);
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 1;
			c.gridy = 0;
			add(chooser, c);
			
			// do for "this" to not break reverse compatibility.
			chooser.setSelectedItem(m_functString);
		}
		
		public void updateForFunction(Function f)
		{
			chooser.setSelectedItem(f.m_functString);
		}

		public String getFunctName()
		{
			return chooser.getSelectedItem().toString();
		}
		
		public String getInterfaceType()
		{
				return buildInterfaceString(
						"function", getFunctName()
				);
		}
	}

	@Override
	public double getPercentageTimeFilled(DateTime start, DateTime end)
	{
		return 1;
	}
}
