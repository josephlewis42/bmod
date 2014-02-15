package bmod.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import bmod.IconLoader;

/**
 * A user-triggerable collapsable panel.
 */
public class CollapsiblePanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private final JComponent m_component;
	private boolean m_isCollapsed;
	private final JButton m_collapseButton;
	
	/**
	 * 
	 * @param title - title for the panel
	 * @param component - component to fit in the panel
	 * @param collapsed - initially collapsed?
	 */
     public CollapsiblePanel(String title, JComponent component, boolean collapsed)
     {
    	 setLayout(new BorderLayout());
    	 
    	 m_collapseButton = new JButton(title);
    	 add(m_collapseButton, BorderLayout.NORTH);
    	 
    	 m_collapseButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				toggleCollapsed();
			}
		});
    	 
    	 m_component = component;
    	 m_isCollapsed = collapsed;
    	 
    	 drawComponent();
     }
     
     public void toggleCollapsed()
     {
    	 m_isCollapsed = !m_isCollapsed;    	
    	 drawComponent();
     }
     
     private void drawComponent()
     {
    	 if(m_isCollapsed)
    	 {
    		 remove(m_component);
    		 m_collapseButton.setIcon(IconLoader.COLLAPSED_ARROW);
    	 }
    	 else
    	 {
    		 add(m_component, BorderLayout.CENTER);
    		 m_collapseButton.setIcon(IconLoader.EXPANDED_ARROW);
    	 }
    	 validate();
    	 repaint();
    	
    	 // Go all the way to the top parent and repaint from there, keeps
    	 // things from messing up when put in scroll panels.
    	 Container parent = getParent();
    	 while(parent != null && parent.getParent() != null)
    		 parent = parent.getParent();
    	 
    	if(parent != null)
    	{
	    	parent.validate();
	    	parent.repaint();
    	}
     }
}