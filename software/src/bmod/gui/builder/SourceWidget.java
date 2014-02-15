package bmod.gui.builder;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import bmod.database.Database;
import bmod.database.objects.SimpleRecord;
import bmod.database.objects.Source;

public class SourceWidget extends GUIBuilderWidget
{
	private final JTextArea textArea = new JTextArea();
	private final JScrollPane scroller = new JScrollPane(textArea);
	private final Source fup;
	
	public SourceWidget(SimpleRecord<?> original)
	{
		super("Source");
		fup = Database.templateSource.getSourceFor(original);
		
		textArea.setText(fup.getSource());
		
		textArea.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent arg0)
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyReleased(KeyEvent arg0)
			{
				fup.setSource(textArea.getText());
				fup.update();				
			}

			@Override
			public void keyTyped(KeyEvent arg0)
			{
			
			}
		});
	}

	@Override
	public boolean isContentChanged()
	{
		return false;
	}

	@Override
	public Object getValue()
	{
		return textArea.getText();
	}

	@Override
	public Component getComponent()
	{
		return scroller;
	}
	
	@Override
	public boolean getMaxHeight()
	{
		return true;
	}

}
