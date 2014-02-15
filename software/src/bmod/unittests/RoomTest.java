package bmod.unittests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import bmod.database.objects.Building;
import bmod.database.objects.Room;

public class RoomTest {

	@Test
	public void testEqualsLocation() {
		Building a = Building.getBuilding("olin");
		Building b = Building.getBuilding("sturm");
		Building aa = Building.getBuilding("olin");
		assertFalse(new Room(a.getPrimaryKey(), "123").equals( new Room(b.getPrimaryKey(), "123")));
		assertTrue(new Room(a.getPrimaryKey(), "123").equals( new Room(a.getPrimaryKey(), "123")));
		assertFalse(new Room(a.getPrimaryKey(), "123").equals( new Room(a.getPrimaryKey(), "1234")));
		assertTrue(new Room(a.getPrimaryKey(), "123").equals( new Room(aa.getPrimaryKey(), "123")));
		assertFalse(new Room(a.getPrimaryKey(), "123").equals( new Room(aa.getPrimaryKey(), "1234")));
		
	}

	@Test
	public void testToString() {
		Building a = Building.getBuilding("olin");
		Building b = Building.getBuilding("sturm");
		assertFalse(new Room(a.getPrimaryKey(), "123").equals( new Room(b.getPrimaryKey(), "123")));
		assertTrue(new Room(a.getPrimaryKey(), "123").equals( new Room(a.getPrimaryKey(), "123")));
	}

}
