package bmod.gui.builder;

import java.awt.Component;

import javax.swing.JTextField;

/**
 * A text pane area.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class TextFieldWidget extends GUIBuilderWidget
{
	private final String m_originalContents;
	
	private final JTextField textArea = new JTextField();
	
	public TextFieldWidget(String widget_title, String contents)
	{
		this(widget_title, contents, true);
	}
	
	public TextFieldWidget(String widget_title, String contents, boolean enabled)
	{
		super(widget_title);
		m_originalContents = contents;
		textArea.setText(contents);
		textArea.setEnabled(enabled);
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
		return textArea;
	}
}
