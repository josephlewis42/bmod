package bmod.gui;

import java.awt.Cursor;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.RootPaneContainer;

import bmod.gui.widgets.InlineDialog;
import bmod.util.BmodCollection;
import bmod.util.Bucket;

/**
 * A list of extension points for the GUI.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class GuiExtensionPoints
{	
	private static final InlineDialog m_dialog = new InlineDialog();
	private static final Bucket<String, JMenuItem[]> menuItems = new Bucket<>();
	private static RootPaneContainer m_rootPane;
	private static final JTabbedPane m_outputWidgets = new JTabbedPane();
	private static final JMenuBar m_mainMenu = new JMenuBar();

	public GuiExtensionPoints(RootPaneContainer parent)
	{
		m_rootPane = parent;
	}
	
	/**
	 * Shows the wait cursor over the entire project.
	 */
	public static void showWaitCursor()
	{
		if(m_rootPane == null)
			return;
		
		m_rootPane.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    m_rootPane.getGlassPane().setVisible(true);
	}
	
	/**
	 * Shows the wait cursor over the entire project.
	 */
	public static void hideWaitCursor()
	{
		if(m_rootPane == null)
			return;
		
		m_rootPane.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	    m_rootPane.getGlassPane().setVisible(false);
	}
	
	/**
	 * Generates a main menu bar from the given menu items.
	 * 
	 * @return
	 */
	public JMenuBar getMainMenu()
	{
		
		return m_mainMenu;
	}
	
	/**
	 * Creates a menu item at the specified path, preface the
	 * first item in your list with spaces to go
	 * first in a menu, and ~s to go last, both of these characters will be 
	 * removed.
	 * 
	 * @param path
	 * @param menuItem
	 */
	public void addMenuItem(String path, JMenuItem[] menuItem)
	{
		for(JMenuItem item : menuItem)
		{
			item.setName(item.getText());
			item.setText(item.getText().replaceAll("^[ ~]+", ""));
		}
		
		menuItems.add(path, menuItem);
		menuChanged();
	}
	
	private void menuChanged()
	{
		m_mainMenu.removeAll();
		for(String menuName : BmodCollection.sortedSet(menuItems.keySet()))
		{
			JMenu tmpMenu = new JMenu(menuName);
			
			Set<JMenuItem[]> items = menuItems.get(menuName);
			
			// Sort the menu items by name.
			List<JMenuItem[]> sortedItems = bmod.util.BmodCollection.sortedSet(items, new Comparator<JMenuItem[]>()
			{
				@Override
				public int compare(JMenuItem[] o1, JMenuItem[] o2)
				{
					if(o1.length == 0 || o2.length == 0)
						return 0;
					
					return o1[0].getName().compareTo(o2[0].getName());
				}
			});
			
			boolean first = true;
			for(JMenuItem[] tmpItems : sortedItems)
			{
				if(first)
				{
					first = false;
				}
				else
				{
					tmpMenu.addSeparator();
				}
				
				for(JMenuItem tmpItem : tmpItems)
				{
					tmpMenu.add(tmpItem);
				}
			}
			
			if(! first) // only add menus with at least one item.
			{
				m_mainMenu.add(tmpMenu);
			}
		}	
	}
	
	/**
	 * Shows an error to the user
	 * @param message
	 */
	public void showError(String message)
	{
		m_dialog.showError(message);
	}
	
	/**
	 * Shows a warning to the user
	 * @param message
	 */
	public static void showWarning(String message)
	{
		m_dialog.showWarning(message);
	}
	
	/**
	 * Shows information to the user.
	 * @param message
	 */
	public static void showInfo(String message)
	{
		m_dialog.showInfo(message);
	}
	
	
	/**
	 * Returns the Inline dialog for this gui.
	 * @return
	 */
	public InlineDialog getInlineDialog()
	{
		return m_dialog;
	}

	public void createWindow(String title, JPanel contents)
	{
		new SwingSet(title, contents, JFrame.HIDE_ON_CLOSE, false);
	}
	
	/**
	 * Adds a panel to the output tabbed pane.
	 * 
	 * @param title - the title of the widget to add.
	 * @param widget - the widget to add.
	 */
	public static void addOutputWidget(String title, Icon icon, JPanel widget)
	{
		m_outputWidgets.addTab(title, icon, widget);
	}
	
	/**
	 * Removes a widget from the output tabbed pane.
	 * 
	 * @param widget
	 */
	public static void removeOutputWidget(JPanel widget)
	{
		m_outputWidgets.remove(widget);
	}
	
	/**
	 * Returns the tabbed pane for the output widgets.
	 * 
	 * @return 
	 * @return
	 */
	public static JTabbedPane getOutputWidgetPane()
	{
		return m_outputWidgets;
	}
	
	/**
	 * Sets up a new window for the application that contains the given 
	 * component.
	 * 
	 * @param comp - the component to add.
	 */
	public void showWindow(String title, JComponent comp)
	{
		new SwingSet(title, comp, JFrame.DISPOSE_ON_CLOSE, false);
	}

	/**
	 * Removes a menu item from the menu system.
	 * @param menuName
	 * @param menu
	 */
	public void removeMenuItem(String menuName, JMenuItem[] menu)
	{
		menuItems.get(menuName).remove(menu);
		menuChanged();
	}

	/**
	 * Shows a dialog in the system.
	 * @param title - the title for the dialog
	 * @param content - the content pane for the dialog
	 */
	public void showDialog(String title, JPanel content)
	{
		JDialog dlg = new JDialog();
		dlg.setModal(true);
		dlg.setContentPane(content);
		dlg.setTitle(title);
		dlg.pack();
		dlg.setVisible(true);
	}
}
