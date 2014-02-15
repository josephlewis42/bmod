package bmod.database;

import java.util.Collection;
import java.util.HashSet;

/**
 * Compiles a list of warnings, etc from the database.
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class DBWarningsList
{
	private final HashSet<String> infos = new HashSet<String>();
	private final HashSet<String> errors = new HashSet<String>();
	private final HashSet<String> warnings = new HashSet<String>();
	
	public void addWarning(String s)
	{
		synchronized(warnings)
		{
			warnings.add(s);
		}
	}
	
	public void addError(String s)
	{
		synchronized(errors)
		{
			errors.add(s);
		}
	}
	
	public void addInfo(String s)
	{
		synchronized(infos)
		{
			infos.add(s);
		}
	}
	
	/** Adds all elements of another DBWarningsList to this one. **/
	public void addAll(DBWarningsList dbwl)
	{
		synchronized(warnings)
		{
			synchronized(dbwl.warnings)
			{
				warnings.addAll(dbwl.warnings);
			}		
		}
		
		synchronized(errors)
		{
			synchronized(dbwl.errors)
			{
				errors.addAll(dbwl.errors);
			}
		}
		
		synchronized(infos)
		{
			synchronized(dbwl.infos)
			{
				infos.addAll(dbwl.infos);
			}
		}
	}
	
	/** Empties this DBWarningsList **/
	public void clear()
	{
		synchronized(warnings)
		{
			warnings.clear();
		}
		
		synchronized(errors)
		{
			errors.clear();
		}
		
		synchronized(infos)
		{
			infos.clear();
		}
	}
	
	public Collection<String> getInfos()
	{
		synchronized(infos)
		{
			return infos;
		}
	}
	
	public Collection<String> getErrors()
	{
		synchronized(errors)
		{
			return errors;
		}
	}
	
	public Collection<String> getWarnings()
	{
		synchronized(warnings)
		{
			return warnings;
		}
	}

	public boolean hasErrors()
	{
		synchronized(errors)
		{
			return errors.size() > 0;
		}
	}
	
	private void appendList(StringBuilder tmp, Collection<String> toappend, String prefix)
	{
		for(String t : toappend)
		{
			tmp.append(prefix);
			tmp.append(t);
			tmp.append("\n");
		}
	}
	
	@Override
	public synchronized String toString()
	{
		StringBuilder tmp = new StringBuilder();
		
		appendList(tmp, infos, "Info: ");
		appendList(tmp, warnings, "Warning: ");
		appendList(tmp, errors, "Error: ");
		
		return tmp.toString();
	}
	
	public String getSummary()
	{
		return "Found " + infos.size() + " infos, " 
						+ warnings.size() + " warnings, " 
						+ errors.size() + " errors.";
	}
}
