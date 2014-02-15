package bmod.gui.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.swing.JButton;
import javax.swing.JEditorPane;


/**
 * A JEditorPane pre-set up for saving, setting HTML and printing.
 * 
 * @author Joseph Lewis joehms22@gmail.com
 */
public class HTMLPane extends JEditorPane
{
	private static final long serialVersionUID = 193002090810163576L;
	private final JButton m_printButton = new JButton("Print");
	private final JButton m_saveButton = new JButton("Save As...");
	
	/**
	 * Creates a new HTMLPane with no text.
	 */
	public HTMLPane()
	{
		 setContentType("text/html");
		 setEditable(false);
		 
		/** Set up the button listeners **/
		m_printButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				printFile();
			}
		});
		
		m_saveButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				saveFile();
			}
		});
	}
	
	public HTMLPane(String URI)
	{
		this();
		
		String s = readResourceContents(URI);
		
		if(!s.contains("<html>"))
		{
			setText(s);
		}
		else
		{
			setHTML(s);
		}
	}
	
	/**
	 * Sets the HTML of this pane, should start with <html><body> and end with </body></html>
	 * @param s - The html string.
	 */
	public void setHTML(String s)
	{	
		super.setText(s);
	}
	
	/**
	 * Sets the contents of this pane as text, will automatically be wrapped in HTML 
	 * and BODY tags.
	 */
	@Override
	public void setText(String s)
	{
		setHTML("<html><body>" + s + "</body></html>");
	}
	
	/**
	 * @return A button linked with the print action.
	 */
	public JButton getPrintButton()
	{
		return m_printButton;
	}
	
	/**
	 * @return A button linked with the save action.
	 */
	public JButton getSaveButton()
	{
		return m_saveButton;
	}
	
	/**
	 * Shows a print dialog, and prints if the user requests it.
	 */
	public void printFile()
	{
		try
		{
			print();
		} catch (PrinterException e)
		{
			Dialogs.showErrorDialog("Print error", e.getMessage());
		}
	}
	
	/**
	 * Shows a save dialog, and saves if the user requests it.
	 * 
	 * @return true on file saved, false if not
	 */
	public boolean saveFile()
	{
		String path = Dialogs.showSaveDialog(new String[]{"HTML File", "html"}, "", false, true, true);
		
		if(path == null)
			return false;
		
		FileWriter w = null;
		
		try
		{
			w = new FileWriter(path);
			w.write(getText());
			w.flush();
			w.close();
			
			return true;
		} catch (IOException e1)
		{
			if(w != null)
				try
				{
					w.close();
				} catch (IOException e2)
				{
					Dialogs.showErrorDialog("File Error", e2.getMessage());
				}
		}
		
		return false;
	}
	
	public String readResourceContents(String filename)
	{
		ClassLoader cl = this.getClass().getClassLoader();
		
		try(	InputStream f = cl.getResourceAsStream(filename);
				InputStreamReader isr = new InputStreamReader( f, Charset.forName( "UTF-8"));
				BufferedReader in = new BufferedReader(isr))
		{
			StringBuilder sb = new StringBuilder();
			
			String line = in.readLine();
			while(line != null)
			{
				sb.append(line);
				line = in.readLine();
			}
			
			return sb.toString();
		} catch (IOException e)
		{
			return "Error: file not found: " + filename;
		}
	}
	
}
