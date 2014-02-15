package bmod.gui;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * Copyright 2011 Joseph Lewis <joehms22@gmail.com>
 * Apache License
 * Originally part of CloneAnt
 */

public class SwingSet extends JFrame
{
	private static final long serialVersionUID = -4568445609586612020L;
	
	/**
	 * Create the GUI and show it.
	 */
	public SwingSet()
	{
		// Set System look and feel.
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e)
		{
		}

		// Create and set up the window.
		setTitle("Building Modeler");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        

		// Create and set up the content pane.
		setContentPane( new MainWindowContainer(this));
		
		// Display the window.
		pack();
		setVisible(true);
	    setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

	}
	
	public SwingSet(String title, JComponent content, int onClose, boolean undecorated)
	{
		setUndecorated(undecorated);
		
		// Set System look and feel.
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e)
		{
		}

		// Create and set up the window.
		setTitle(title);
		setDefaultCloseOperation(onClose);

		// Create and set up the content pane.
		setContentPane(content);
		
		
		
		// Make sure we don't grow beyond the bounds of the screen
		setMaximumSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
		
		// Display the window.
		pack();
		setLocationRelativeTo(null); 
		setVisible(true);
	}
}
