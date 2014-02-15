package bmod.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import bmod.database.objects.Record;
import bmod.gui.FilterPanel.FilterUpdateListener;
import bmod.gui.widgets.ArticleLayoutPanel;
import bmod.gui.widgets.HorizontalPanel;

/**
 * Creates a dialog that will assist the user in creating a new CSVRecord.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class CSVRecordCreator extends JDialog
{
	private static final long serialVersionUID = 1L;
	private final FilterPanel filter;
	private final Record<?> m_template;
	private final JPanel m_internalContainer;
	private final JButton m_createButton = new JButton("Create");
	
	public CSVRecordCreator(Record<?> template)
	{
		setModal(true);
		//super("New "+template.getHumanReadableClassName());
		m_template = template;
		
		
		
		filter = new FilterPanel(m_template.getFilters());
		filter.addFilterUpdateListener(ful);
				
		
		m_internalContainer = new ArticleLayoutPanel(
				null,
				filter.getContainer(), 
				new HorizontalPanel(m_createButton));
		
		m_createButton.setEnabled(false);
		m_createButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				setVisible(false);
			}
		});
		
		if(m_template.getFilters().size() == 0)
		{
			return;
		}
		else
		{
			setContentPane(m_internalContainer);
			pack();
			setVisible(true);
		}
		
		
	}
	
	public final FilterUpdateListener ful = new FilterUpdateListener(){
		@Override
		public void filterUpdated(Collection<Record<?>> filter)
		{
			m_createButton.setEnabled(m_template.areReferencesSatisfied(filter));
		}
	};
	
	/**
	 * Returns the result of tihs record creator.
	 * @return The created item from this dialog or null if none was created.
	 */
	public Record<?> getValue()
	{
		Collection<Record<?>> refs = filter.getState();
		
		if(m_template.areReferencesSatisfied(refs))
		{
			return m_template.createNew(refs);
		} else {
			return  null;
		}
	}
	
}
