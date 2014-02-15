package bmod.buildingactivity;

import javax.swing.JPanel;

import bmod.util.DateTime;

public class Always extends BuildingActivityInterface
{
	public Always(String optionString)
	{
		super(optionString);
	}

	@Override
	public boolean happensWithin(DateTime start, DateTime end)
	{
		return true;
	}

	@Override
	public int getInterfaceId()
	{
		return "Always".hashCode();
	}

	@Override
	public JPanel getEditorPanel()
	{
		return new JPanel();
	}

	@Override
	public String getInterfaceTypeFromPanel(JPanel m_panel)
	{
		return "";
	}

	@Override
	public String getHumanReadableName()
	{
		return "Always on activity.";
	}

	@Override
	public String getHumanString()
	{
		return "Always on";
	}

	@Override
	public double getPercentageTimeFilled(DateTime start, DateTime end)
	{
		return 1;
	}

}
