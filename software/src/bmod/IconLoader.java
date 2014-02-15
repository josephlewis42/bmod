package bmod;

import javax.swing.ImageIcon;

public class IconLoader
{	
	public static final ImageIcon OPEN_ICON = getIcon("folder.png");
	public static final ImageIcon ADD_ICON = getIcon("add.png");
	public static final ImageIcon HELP_ICON = getIcon("help.png");
	public static final ImageIcon PLUGIN = getIcon("plugin.png");
	public static final ImageIcon SPLASH = getIcon("bmod_splash.png");
	public static final ImageIcon COLLAPSED_ARROW = getIcon("arrow_collapsed.gif");
	public static final ImageIcon EXPANDED_ARROW = getIcon("arrow_expanded.gif");
	public static final ImageIcon SETTINGS = getIcon("settings.png");
	public static final ImageIcon BUG = getIcon("bug.png");
	public static final ImageIcon SAVE_ICON = getIcon("disk.png");
	public static final ImageIcon REFRESH_ICON = getIcon("arrow_refresh.png");
	public static final ImageIcon TRASH_ICON = getIcon("bin_empty.png");
	public static final ImageIcon COG_GO = getIcon("cog_go.png");
	
	/**
	 * Gets an ImageIcon based upon the path given. Returns a blank icon
	 * if an error occurred.
	 * 
	 * @param path the path to get the icon from.
	 * @return
	 */
	public static ImageIcon getIcon(String path)
	{
		ImageIcon tmp;
		try
		{
			ClassLoader cldr = new IconLoader().getClass().getClassLoader(); 
			java.net.URL imageURL = cldr.getResource(path);
			imageURL.getContent();
			tmp = new ImageIcon(imageURL);
		} catch(Exception ex)
		{
			ex.printStackTrace();
			tmp = new ImageIcon();
		}
		
		return tmp;
	}
}
