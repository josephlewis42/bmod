package bmod.unittests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import bmod.database.Database;
import bmod.database.DatabaseIntegrityException;

public class DatabaseTest
{

	@Test
	public void testGetDqm()
	{
		assertTrue(Database.getDqm() != null);
	}

	@Test
	public void testGetNewPrimaryKey()
	{
		assertTrue(Database.getNewPrimaryKey() < Database.getNewPrimaryKey());
	}

	@Test
	public void testHandleCriticalError()
	{
		DatabaseIntegrityException ex = new DatabaseIntegrityException("Test Fail");
		Database.handleCriticalError(ex, false);
		
		assertTrue(true);
	}

}
