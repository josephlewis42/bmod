package bmod.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import bmod.IconLoader;
import bmod.database.objects.Record;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.widgets.ArticleLayoutPanel;
import bmod.gui.widgets.Dialogs;
import bmod.gui.widgets.FilterableCSVRecordList;
import bmod.gui.widgets.FilterableCSVRecordList.RecordChangeListener;
import bmod.gui.widgets.HorizontalPanel;
import bmod.gui.widgets.VerticalPanel;

/**
 * This editor panel is highly generic, and updates at every filter event.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 * @param <T>
 */
public class NewDatabaseEditorPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private final Collection<Record<?>> m_masterRecords;

	private final CardLayout m_cardLayout = new CardLayout();
	private final JPanel m_cards = new JPanel(m_cardLayout);
	private final JLabel m_flipperText = new JLabel("Select an item to edit; select multiple to perform group actions.");
	private final FilterableCSVRecordList recs ;
	private GUIBuilderPanel m_currentEditor;
	protected final JPanel m_editorWrapper;
	private final JButton m_newRecordButton = new JButton("New");
	
	private static final String m_flipperTextId = "FLIPPER_TEXT";
	private static final String m_editorId = "EDITOR_ID";
	private static final String MULTI_EDITOR_ID = "MULTI_EDITOR_ID";
	private final JComboBox<Record<?>> createList;
	
	private final JButton m_saveButton = new JButton("Save", IconLoader.SAVE_ICON);
	private final JButton m_deleteButton = new JButton("Delete", IconLoader.TRASH_ICON);
	
	private final ActionListener m_deleteListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(!Dialogs.showYesNoQuestionDialog("Delete?", "This will delete the selected record(s), continue?"))
			{
				return;
			}
			
			for(Record<?> r: recs.getSelectedValuesList())
			{
				r.delete();
			}
			
			recs.doFilter();
		}
	};
	
	private final ActionListener m_saveListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			List<Record<?>> selected = recs.getSelectedValuesList();
			if(selected.size() != 1)
			{
				throw new IllegalArgumentException("You may not save more than one record from a form.");
			}
			
			Record<?> currentRecord = selected.get(0);
			
			currentRecord.fromSQL(m_currentEditor.getResult()).update();

			recs.doFilter();
		}
	};
	
	private final ActionListener m_newListener = new ActionListener()
	{
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			CSVRecordCreator c = new CSVRecordCreator(createList.getItemAt(createList.getSelectedIndex()));
			Record<?> value = c.getValue();
			if(value == null)
			{
				Dialogs.showErrorDialog("Cancelled", "No record created");
				return;
			}
			
			value.create();
			
			recs.doFilter();
		}
	};
	
	public NewDatabaseEditorPanel(Collection<Record<?>> filter)
	{
		m_masterRecords = filter;
		createList = new JComboBox<>(new Vector<>(filter));
		
		m_editorWrapper = new ArticleLayoutPanel(
								null,
								null,
								new HorizontalPanel(m_saveButton, m_deleteButton));
		
		setLayout(new BorderLayout());
		
		recs = new FilterableCSVRecordList(m_masterRecords);
		
		
		HorizontalPanel menuPanel = new HorizontalPanel();
		if(filter.size() > 1)
		{
			menuPanel.add(createList);
		}
		menuPanel.add(m_newRecordButton);
		JPanel leftPanel = new ArticleLayoutPanel(
								null,
								recs, 
								menuPanel);
		
		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, m_cards));
		
		m_cards.add(m_flipperText, m_flipperTextId);
		m_cards.add(new JScrollPane(m_editorWrapper), m_editorId);
		m_cards.add(getMultiSelectPanel(), MULTI_EDITOR_ID);
		m_cardLayout.show(m_cards, m_flipperTextId);	
		
		recs.addRecordChangeListener(new RecordChangeListener(){
			@Override
			public void recordChange(Record<?> r)
			{
				if (m_currentEditor != null)
				{
					m_editorWrapper.remove(m_currentEditor);
				}
				
				m_currentEditor = r.getEditor();
				if (m_currentEditor != null)
				{
					m_editorWrapper.add(m_currentEditor, BorderLayout.CENTER);
					m_editorWrapper.revalidate();
					m_cardLayout.show(m_cards, m_editorId);
				}
			}

			@Override
			public void recordChange(List<Record<?>> recs)
			{
				m_cardLayout.show(m_cards, MULTI_EDITOR_ID);
			}

			@Override
			public void allRecordsDeselected()
			{
				m_cardLayout.show(m_cards, m_flipperTextId);
			}
		});
		
		m_deleteButton.addActionListener(m_deleteListener);
		m_saveButton.addActionListener(m_saveListener);
		m_newRecordButton.addActionListener(m_newListener);
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	private JPanel getMultiSelectPanel()
	{
		JButton deleteManyButton = new JButton("Delete All", IconLoader.TRASH_ICON);
		deleteManyButton.addActionListener(m_deleteListener);
		
		return new VerticalPanel(
					new JLabel("<html><h1>Multiple Items Selected</h1></html>"),
					deleteManyButton);
	}
	
	public void setFilter(Collection<Record<?>> filters)
	{
		recs.applyFilter(filters);
	}
}
