package bmod.math.calculator;


import bmod.database.DatabaseIntegrityException;
import bmod.database.objects.DeviceType;
import bmod.database.objects.DeviceTypeVariable;

public class BmodVariableStore implements VariableStore
{
	private static final String VARIABLE_BEGINNING = ".var(";
	private static final String WATTS_BEGINNING = ".watts(";
		
	@Override
	public double getValue(String variable) throws MalformedExpression
	{
		if(variable.startsWith(VARIABLE_BEGINNING))
		{
			String[] comps = variable.substring(VARIABLE_BEGINNING.length()).split(",");
			if(comps.length != 2)
				throw new MalformedExpression("The var function takes 2 args "+
							"name (String), and default (number) but" + 
							comps.length + 
							" were provided.");
			comps[1] = comps[1].substring(0,comps[1].length() - 1); // cut off the )
			comps[0] = comps[0].substring(1, comps[0].length() - 1); // cut off the quotes
			
			return var(comps[0], Double.parseDouble(comps[1]));
		}
		
		if(variable.startsWith(WATTS_BEGINNING))
		{
			// cut off quotes, prefix, and suffix
			String comps = variable.substring(WATTS_BEGINNING.length() + 1, variable.length() - 2);
			return watts(comps);
		}
		
		throw new MalformedExpression(variable + " is not a known variable or function.");
	}
	
	
	/**
	 * Returns the number of watts for the given deviceType.
	 * 
	 * @param deviceType
	 * @return the number of watts for the DeviceType with the given name.
	 * 
	 */
	public double watts(String deviceType) throws MalformedExpression
	{
		try
		{
			DeviceType t = DeviceType.readDeviceType(deviceType);
			if(t.isPureMath())
				return t.getWatts();
		} catch (DatabaseIntegrityException e)
		{
		}
		
		throw new MalformedExpression("Can't recusivly nest device types" +
				"that use another math within an expression: " + 
				deviceType);
		
	}
	
	public double var(String varname, double default_val)
	{
		return DeviceTypeVariable.getVariableValueOrDefault(varname, default_val);
	}

}
