package bmod.gui.widgets;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import bmod.IconLoader;

/**
 * Creates a splash screen to be shown by the interface.
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class Splash extends JPanel
{
	private static final long serialVersionUID = 1L;
	private static JLabel jl = new JLabel("Loading...");
	private static String m_prefix = "";
	
	public Splash()
	{
		setLayout(new BorderLayout());
		add(new JLabel(IconLoader.SPLASH), BorderLayout.CENTER);
		add(jl, BorderLayout.PAGE_END);
	}
	
	public static void setLabel(String label)
	{
		jl.setText(m_prefix + label);
	}
	
	public static void setPrefix(String prefix)
	{
		m_prefix = prefix;
	}
}
