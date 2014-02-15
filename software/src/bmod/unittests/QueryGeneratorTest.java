package bmod.unittests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import bmod.database.Database;

public class QueryGeneratorTest
{

	@Test
	public void testGetRawStatement()
	{
		Database.getDqm();
		assertTrue(Database.templateBuilding.readWhere().getRawStatement(-1).equals("SELECT * FROM \"Buildings\""));
	}

	@Test
	public void testNone()
	{
		assertTrue(Database.templateBuilding.readWhere().eq("BuildingName", "Olin").none().size() == 0);
	}

	@Test
	public void testAll()
	{
		assertTrue(Database.templateBuilding.readWhere().all().size() > 0);
	}

	@Test
	public void testEqStringDouble()
	{
		Database.templateBuilding.readWhere().eq("PrimaryKey", 1.0).all();
		fail("Can't assign primary key a double");
	}

	@Test
	public void testGtStringLong()
	{
		assertTrue(Database.templateBuilding.readWhere().gt("PrimaryKey", Long.MIN_VALUE).all().size() > 0);
	}

	@Test
	public void testEqStringString()
	{
		Database.templateBuilding.readWhere().eq("PrimaryKey", "").all();
		fail("Can't assign primary key a string");	
	}

	@Test
	public void testEqStringBoolean()
	{
		Database.templateBuilding.readWhere().eq("PrimaryKey", true).all();
		fail("Can't assign primary key a bool");	
	}

	@Test
	public void testEqStringInt()
	{
		Database.templateBuilding.readWhere().eq("PrimaryKey", 12).all();
		fail("Can't assign primary key an int");	
	}

	@Test
	public void testEqStringChar()
	{
		Database.templateBuilding.readWhere().eq("PrimaryKey", 'c').all();
		fail("Can't assign primary key a char");	
	}

}
