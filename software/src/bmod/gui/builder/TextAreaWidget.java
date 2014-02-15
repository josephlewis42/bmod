package bmod.gui.builder;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * A text editor area.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class TextAreaWidget extends GUIBuilderWidget
{
	final String m_originalContents;
	
	private final JTextArea textArea = new JTextArea();
	private final JScrollPane scroller = new JScrollPane(textArea);
	
	public TextAreaWidget(String widget_title, String contents)
	{
		this(widget_title, contents, true);
	}
	
	public TextAreaWidget(String widget_title, String contents, boolean enabled)
	{
		super(widget_title);
		m_originalContents = contents;
		textArea.setText(contents);
		textArea.setEnabled(enabled);
		
		textArea.setLineWrap(true);
	}

	@Override
	public boolean isContentChanged()
	{
		return ! textArea.getText().equals(m_originalContents);
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
