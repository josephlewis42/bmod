package bmod.database;

@SuppressWarnings("serial")
public class DatabaseIntegrityException extends Exception
{
	public DatabaseIntegrityException(String msg)
	{
		super(msg);
	}

	public DatabaseIntegrityException(Exception ex)
	{
		super(ex);
	}
}
