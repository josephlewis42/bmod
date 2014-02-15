package bmod.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Provides basic pagination over a text area.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class TextPaginator extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private static final int LINES_PER_PAGE = 100;
	
	public JButton next = new JButton("Next");
	public JButton prev = new JButton("Prev");
	public JLabel currPageLabel = new JLabel();
	public ArrayList<String> chunks = new ArrayList<String>();
	public final JTextArea shownText = new JTextArea();
	public int currIndex = 0;
	
	public TextPaginator(String longtext)
	{
		setText(longtext);
		
		
		next.addActionListener(this);
		prev.addActionListener(this);
		
		
		// Layout functions
		setLayout(new BorderLayout());
		add(new JScrollPane(shownText), BorderLayout.CENTER);
		add(new HorizontalPanel(prev, currPageLabel,  next), BorderLayout.PAGE_END);
	}
	
	public void setText(String text)
	{
		String[] currChunks = text.split("\n");
		
		StringBuilder currLine = null;
		for(int i = 0; i < currChunks.length; i++)
		{
			if(i % LINES_PER_PAGE == 0)
			{
				if(currLine != null)
				{
					chunks.add(currLine.toString());
				}
				currLine = new StringBuilder();
			}
			
			currLine.append(currChunks[i]);
			currLine.append("\n");
		}

		changePage();
	}

	@Override
	public void actionPerformed(ActionEvent evt)
	{
		if(evt.getSource() == next)
		{
			currIndex++;
		}
		
		if(evt.getSource() == prev)
		{
			currIndex--;
		}
		
		changePage();
	}
	
	public void changePage()
	{
		if(chunks.size() != 0)
		{
			shownText.setText(chunks.get(currIndex));
			shownText.setForeground(Color.black);
		}
		else
		{
			shownText.setText("[This page intentionally left blank]");
			shownText.setForeground(Color.gray);
		}
		
		currPageLabel.setText(String.format("Page %d of %d", currIndex + 1, chunks.size()));
		
		updateButtons();
	}
	
	public void updateButtons()
	{
		
		if(currIndex == chunks.size() - 1 || chunks.size() == 0)
		{
			next.setEnabled(false);
		}
		else
		{
			next.setEnabled(true);
		}
		
		if(currIndex == 0)
		{
			prev.setEnabled(false);
		}
		else
		{
			prev.setEnabled(true);
		}
	}
	
}
