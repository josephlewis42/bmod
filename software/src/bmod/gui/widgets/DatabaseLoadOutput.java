package bmod.gui.widgets;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import bmod.database.DBWarningsList;
import bmod.gui.GuiExtensionPoints;

public class DatabaseLoadOutput extends JPanel 
{
	private static final long serialVersionUID = -2788865903248071652L;
	private final JEditorPane m_output;
	private final JScrollPane m_scroller;
	private final JTextField m_searchBox = new JTextField();
	
	private transient DBWarningsList m_warningsList = new DBWarningsList();
	
	/**
	 * Create the panel.
	 */
	public DatabaseLoadOutput() {
		setLayout(new BorderLayout(0, 0));
		add(m_searchBox, BorderLayout.NORTH);
		
		m_scroller = new JScrollPane();
		add(m_scroller, BorderLayout.CENTER);
		m_output = new JEditorPane();
        m_output.setContentType("text/html");
		m_scroller.setViewportView(m_output);
		clearAll();
		
		m_searchBox.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent arg0)
			{
			}

			@Override
			public void keyReleased(KeyEvent arg0)
			{
				reloadDisplayPane();				
			}

			@Override
			public void keyTyped(KeyEvent arg0)
			{
			}
		});
	}
	
	public void clearAll()
	{
		m_warningsList.clear();
		m_searchBox.setText("");
		reloadDisplayPane();
	}
	
	protected void reloadDisplayPane()
	{
		StringBuilder m_currText = new StringBuilder();
		String filter = m_searchBox.getText().toLowerCase();
		
		m_currText.append("<html><body>");
		
		if(m_warningsList.getInfos().size() + 
				m_warningsList.getErrors().size() + 
				m_warningsList.getWarnings().size() == 0)
			m_currText.append("<p>Errors found while running the model will be put here.</p>");
		
		// Do INFOs
		m_currText.append("<span style=\'color:#009999\'>");
		
		for(String s : m_warningsList.getInfos())
		{
			s = "Info: " + s.replace("<", "&lt;").replace(">","&gt;").replace("\n", "<br>") + "<br>";
			
			if(s.toLowerCase().contains(filter))
				m_currText.append(s);
		}	
		m_currText.append("</span>");
		
		// Do Warnings
		m_currText.append("<span style='color:#cc6600'>");
		for(String s : m_warningsList.getWarnings())
		{
			s = "Warning: " + s.replace("<", "&lt;").replace(">","&gt;").replace("\n", "<br>") + "<br>";

			if(s.toLowerCase().contains(filter))
				m_currText.append(s);
		}
				
		// Do Errors
		m_currText.append("</span><span style=\'color:#990000\'>");
	
		for(String s : m_warningsList.getErrors())
		{
			
			s = "Error: " + s.replace("<", "&lt;").replace(">","&gt;").replace("\n", "<br>") + "<br>";
			if(s.toLowerCase().contains(filter))
				m_currText.append(s);
		}
				
		m_currText.append("</span></body></html>");
				
		m_output.setText(m_currText.toString());
	}

	public void append(DBWarningsList dwl)
	{
		m_warningsList.addAll(dwl);
		GuiExtensionPoints.showInfo(m_warningsList.getSummary());
		reloadDisplayPane();
	}
}
