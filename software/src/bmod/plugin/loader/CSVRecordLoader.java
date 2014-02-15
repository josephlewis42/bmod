package bmod.plugin.loader;

import java.util.Collection;
import java.util.LinkedList;

import bmod.database.objects.Record;
import bmod.plugin.PluginManager;

/**
 * Loads CSVRecords that can be used in the database.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class CSVRecordLoader extends PluginManager<Record<?>>
{
	public CSVRecordLoader()
	{
		super("bmod/database/objects/");
		
		m_logger.debug("Found CSVRecord Plugins:");		
		for(Record<?> r : getRecordPluginManagers())
			m_logger.debug("\t\t" + r.getTableName());
	}
	
	public Record<?>[] getRecordPluginManagers()
	{
		Collection<Record<?>> spm = new LinkedList<Record<?>>();
		
		for(Object pmo : getPlugins())
		{
			try{
				Record<?> pm = (Record<?>) pmo; 
				
				spm.add(pm);
			}catch(ClassCastException ex)
			{
				m_logger.debug("Couldn't cast: "+pmo+" to CSVRecord: "+ex.getMessage());
			}
		}
		
		return spm.toArray(new Record<?>[spm.size()]);
	}
	
}
