package bmod.gui.builder;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bmod.database.Database;
import bmod.database.objects.Record;
import bmod.database.objects.Followup;

public class FollowupWidget extends GUIBuilderWidget
{
	private final JCheckBox jcb = new JCheckBox();
	private final Followup fup;
	
	public FollowupWidget(Record<?> original)
	{
		super("Needs Followup?");
		fup = Database.templateFollowup.getFollowupFor(original);
		
		jcb.setSelected(fup.getFollowup());
		
		jcb.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent arg0)
			{
				fup.setFollowup(jcb.isSelected());
				fup.update();
			}
		});
	}

	@Override
	public boolean isContentChanged()
	{
		return false;
	}

	@Override
	public Object getValue()
	{
		return jcb.isSelected();
	}

	@Override
	public Component getComponent()
	{
		return jcb;
	}

}
