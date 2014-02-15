package bmod.gui.builder;

import java.util.HashMap;

import javax.swing.JFrame;

import bmod.database.objects.Room;
import bmod.gui.SwingSet;

public class GUIBuilderPanelTest
{
	public static void main(String[] args)
	{
		HashMap<Object,Object> map = new HashMap<Object,Object>();
		map.put("Hello", 'h');
		map.put("world", 'w');
				
		GUIBuilderPanel gbp = new GUIBuilderPanel(new Object[]{13L,13L,113,4.0,.5,"","", false, false,null},
				new LongWidget("PrimaryKey",10,false),
				new LongWidget("Value",11,true),
				new CSVRecordWidget("Room", 113, new Room()),
				new DoubleWidget("Percent", .04, true),
				new DoubleWidget("Fixed", .05, false),
				new TextAreaWidget("Text Area", "Hello, world\n\n\n\n\n\n\n\n\nhi"),
				new TextAreaWidget("Disabled Text Area", "Hello, world\n\n\n\n\n\n\n\n\nhi", false),
				new BooleanWidget("Boolean", true, false),
				new BooleanWidget("Boolean Editable", false, true),
				new SelectionWidget("Selection",map,'h'));
		
		new SwingSet("GUI BUILDER TEST", gbp, JFrame.EXIT_ON_CLOSE, false);
		
	}
}
