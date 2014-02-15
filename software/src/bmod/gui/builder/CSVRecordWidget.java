package bmod.gui.builder;

import java.awt.Component;

import bmod.database.objects.Record;
import bmod.gui.widgets.CSVRecordJComboBox;

public class CSVRecordWidget extends GUIBuilderWidget
{
	final CSVRecordJComboBox<?> combobox;
	final long origPkey;
	
	public <T extends Record<T>> CSVRecordWidget(String title, long recordPkey, T record)
	{
		this(title, recordPkey, record, true);
	}
	
	public <T extends Record<T>> CSVRecordWidget(String title, long recordPkey, T record, boolean editable)
	{
		super(title);
		combobox = new CSVRecordJComboBox<T>(record, recordPkey);
		origPkey = recordPkey;
		combobox.setEnabled(editable);
	}
	


	@Override
	public boolean isContentChanged()
	{
		return combobox.getSelectedItem().getPrimaryKey() != origPkey;
	}

	@Override
	public Object getValue()
	{
		return combobox.getSelectedItem().getPrimaryKey();
	}

	@Override
	public Component getComponent()
	{
		return combobox;
	}
}
