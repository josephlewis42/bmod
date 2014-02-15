package bmod.database;

import bmod.database.objects.Record;
import bmod.database.objects.SimpleRecord;
import bmod.gui.builder.GUIBuilderPanel;

/**
 * A class representing the number of others that are user-editable data feeds.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 * @param <T> 
 */
public abstract class EditableDataFeed<T extends EditableDataFeed<T>> extends
		SimpleRecord<T> implements DataFeed
{
	private final String m_feedName;
	
	public EditableDataFeed(String tableName, String[] colNames, String[] colTypes, Record<?>[] references, long pKey, String feedName)
	{
		super(tableName, colNames, colTypes,references, pKey);
		m_feedName = feedName;
	}
	
	/**
	 * Gets a new one of these data feeds.
	 * @return
	 */
	public abstract T getNew();
	
	
	/**
	 * Gets a simple editor for the DataFeed.
	 * @return
	 */
	@Override
	public abstract GUIBuilderPanel getEditor();
	
	@Override
	public String getFeedName()
	{
		return m_feedName;
	}
	
	@Override
	public String getUserEditableClass()
	{
		return EditableDataFeed.class.getCanonicalName();
	}
}
