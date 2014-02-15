package bmod.plugin.generic.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import bmod.gui.GuiExtensionPoints;
import bmod.gui.widgets.VerticalPanel;
import bmod.plugin.loader.GenericGuiPluginLoader;

public class PluginManager extends GenericGuiPlugin
{
	public final JMenuItem MANAGE_ITEM = new JMenuItem("Enable/Disable Plugins");
	protected static GuiExtensionPoints m_environment;

	public PluginManager()
	{
		super("Plugin Manager", "A dialog that allows you to enable/disable plugins.");
	}

	@Override
	public void setup(final GuiExtensionPoints environment)
	{
		m_environment = environment;
		environment.addMenuItem("Help", new JMenuItem[]{MANAGE_ITEM});
		
		MANAGE_ITEM.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				environment.showWindow("Plugin Manager", new PluginManagerWindow());
			}});
		
	}

	@Override
	public void teardown()
	{
		// DO NOT EVER remove the plugin manager from the menu system, 
		// this could be disastrous!
	}
	
	public class PluginEntry extends JPanel implements ActionListener
	{

		private static final long serialVersionUID = 1L;
		private final GenericGuiPlugin m_item;
		private final JTextArea m_description = new JTextArea(0,30);
		private final JCheckBox m_enabled = new JCheckBox("Enabled", true);
		
		public PluginEntry(GenericGuiPlugin item)
		{
			m_item = item;
			m_enabled.setSelected(GenericGuiPluginLoader.isEnabled(item));
			
			setLayout(new BorderLayout());
			add(new JLabel("<html><h2>" + item.getName() + "</h2></html>"), BorderLayout.NORTH);
			
			m_description.setText(item.getDescription());
			m_description.setEditable(false);
			m_description.setWrapStyleWord(true);
			m_description.setLineWrap(true);
			add(m_description, BorderLayout.CENTER);
			add(m_enabled, BorderLayout.EAST);
			m_enabled.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent evt)
		{
			System.out.println("State changed");
			
			if(GenericGuiPluginLoader.isEnabled(m_item))
			{
				GenericGuiPluginLoader.disable(m_item);
			}
			else
			{
				GenericGuiPluginLoader.enable(m_item);
			}
		}
	}
	
	public class PluginManagerWindow extends JPanel
	{
		private static final long serialVersionUID = 1L;

		public PluginManagerWindow()
		{
			setMaximumSize(new Dimension(640, 480));
			setSize(640, 480);
			
			setLayout(new BorderLayout());
			VerticalPanel vp = new VerticalPanel();
			
			for(GenericGuiPlugin gp : GenericGuiPluginLoader.getAllPlugins())
			{
				vp.add(new PluginEntry(gp));
				vp.add(new JSeparator());
			}
			

			add(new JScrollPane(vp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
			setVisible(true);
		}
	}

}
