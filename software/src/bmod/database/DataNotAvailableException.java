package bmod.database;

/**
 * An exception to throw when Data isn't available.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class DataNotAvailableException extends Exception
{
	private static final long serialVersionUID = 1771270057436186312L;

	public DataNotAvailableException(String msg)
	{
		super(msg);
	}

	public DataNotAvailableException(Exception ex)
	{
		super(ex);
	}
}
