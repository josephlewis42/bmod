package bmod.gui.builder;

import java.awt.Component;

import bmod.gui.widgets.ActivityTypeComboBox;

/**
 * Handles editing ActivityTypes. Returns a String.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class ActivityTypeWidget extends GUIBuilderWidget
{
	private final ActivityTypeComboBox actcb = new ActivityTypeComboBox();
	private final String origText;
	
	public ActivityTypeWidget(String widgetName, String activityType)
	{
		super(widgetName);
		actcb.setText(activityType);
		origText = activityType;
	}

	@Override
	public boolean isContentChanged()
	{
		return ! origText.equals(actcb.getText());
	}

	@Override
	public Object getValue()
	{
		return actcb.getText();
	}

	@Override
	public Component getComponent()
	{
		return actcb;
	}
}
