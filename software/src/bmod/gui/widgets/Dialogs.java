package bmod.gui.widgets;
/**
 * Dialogs provides an easy way to access Java swing dialogs in a static 
 * context.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 * @version December 17, 2010
 * @license GNU GPL V 2 or higher.
 * Changelog:
 * July 30, 2009 Original
 * October 27, 2010 Made all methods static.
 * December 17, 2010 General cleanup.
 * March 07, 2012 Added a dialog for option selection.
 */

import java.awt.Toolkit;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class Dialogs
{
	
	/**
	 * Shows a generic file save dialog.
	 * @param extensions - the extension filters to accept.
	 * @param allfiles - whether or not to accept an all files option
	 * @return
	 */
	public static Path showFileSaveDialog(FileFilter[] filters, boolean allFiles)
	{
		JFileChooser fileChooser = new JFileChooser();
		
		for(FileFilter ff : filters)
		{
			fileChooser.addChoosableFileFilter(ff);
			fileChooser.setFileFilter(ff);
		}

		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setAcceptAllFileFilterUsed(allFiles); 

		//Get the user option
		JFrame jf = new JFrame();
		int input = fileChooser.showSaveDialog(jf);

		//If the user approves  a file get that filepath and send it
		if(input == JFileChooser.APPROVE_OPTION)
		{
			File file = fileChooser.getSelectedFile();
			if(file.exists())
			{
				if(! Dialogs.showYesNoQuestionDialog("Exists", "The file you chose already exists, it will be overwritten, is this okay?"))
				{
					return null;
				}
			}
			
			return file.toPath();
		}
		else
		{
			return null;
		}
	}
	
	
	/**
	 * Shows a generic file open dialog.
	 * @param extensions - the extension filters to accept.
	 * @param allfiles - whether or not to accept an all files option
	 * @return
	 */
	public static Path showFileOpenDialog(FileFilter[] filters, boolean allFiles)
	{
		JFileChooser fileChooser = new JFileChooser();
		
		for(FileFilter ff : filters)
		{
			fileChooser.addChoosableFileFilter(ff);
			fileChooser.setFileFilter(ff);
		}
		

		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setAcceptAllFileFilterUsed(allFiles); 

		//Get the user option
		JFrame jf = new JFrame();
		int input = fileChooser.showOpenDialog(jf);

		//If the user approves  a file get that filepath and send it
		if(input == JFileChooser.APPROVE_OPTION)
		{   
			File file = fileChooser.getSelectedFile();
			if(!file.exists())
			{
				return null;
			}
			
			return file.toPath();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Shows an open dialog that has an array of file extension options.
	 * 
	 * @param extensions (Each Element is An Acceptable extension, first is description)
	 * Example: ["images","jpg","png"]
	 * @param currentLocation The location to open the dialog to.  Blank is home folder.
	 * @param folderSelect  Should entire folders be selectable?
	 * @param folderShow  Should folders be shown? (to navigate, usually true)
	 * @param allFiles  Can the user choose any file (true) or just the ones you allowed
	 * in extensions?
	 * 
	 * @return The path of the file the user selected, null if user quit.
	 */
	public static String showSaveDialog(final String[] extensions, 
			String currentLocation, 
			boolean folderSelect, 
			final boolean folderShow, 
			boolean allFiles)
	{
		//JFileChooser fc = new JFileChooser();
		JFileChooser fileChooser = new JFileChooser();  //Starts the main file chooser and readies it
		
		fileChooser.setFileFilter(new FileFilter() 
		{
			@Override
			public boolean accept(File f) 
			{
				//For each extension accept the files with it
				//I is set at 1 becasue the first element 0 is the Description
				for(int i = 1; i < extensions.length; i++)
					if(f.getName().endsWith(extensions[i]))
						return true;

				if(f.isDirectory() && folderShow) 
					return true;

				return false;
			}

			@Override
			public String getDescription() 
			{ 
				return extensions[0];
			}
		}
		);

		if(folderSelect)
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		fileChooser.setAcceptAllFileFilterUsed(allFiles); 

		//Get the user option
		JFrame jf = new JFrame();
		int input = fileChooser.showSaveDialog(jf);
		String filePath = null;
		//If the user approves  a file get that filepath and send it
		if(input == JFileChooser.APPROVE_OPTION)
		{   
			File file = fileChooser.getSelectedFile();
			if(file.exists())
			{
				if(! Dialogs.showYesNoQuestionDialog("Exists", "The file you chose already exists, it will be overwritten, is this okay?"))
					return null;
			}
			try 
			{
				filePath = file.getPath();
			}
			catch (Exception ex)
			{
				showErrorDialog("Save Error","Save Error:\nDid you move or delete\nthe file you are trying to open?\n"+ex);
			}
		}else{  //If the user canceled/quit
			filePath = null;
		}

		return filePath;
	}

	/**
	 * Shows an open dialog that has an array of file extension options.
	 * 
	 * @param extensions (Each Element is An Acceptable extension, first is description)
	 * Example: ["images","jpg","png"]
	 * @param currentLocation The location to open the dialog to.  Blank is home folder.
	 * @param folderSelect  Should entire folders be selectable?
	 * @param folderShow  Should folders be shown? (to navigate, usually true)
	 * @param allFiles  Can the user choose any file (true) or just the ones you allowed
	 * in extensions?
	 * 
	 * @return The path of the file the user selected, null if user quit.
	 */
	public static String showOpenDialog (final String[] extensions, 
			String currentLocation, 
			boolean folderSelect, 
			final boolean folderShow, 
			boolean allFiles)
	{
		//JFileChooser fc = new JFileChooser();
		JFileChooser fileChooser = new JFileChooser();//Starts the main file chooser and readies it
		fileChooser.addChoosableFileFilter(new FileFilter() 
		{
			@Override
			public boolean accept(File f) 
			{
				//For each extension accept the files with it
				//I is set at 1 becasue the first element 0 is the Description
				for(int i = 1; i < extensions.length; i++)
					if(f.getName().endsWith(extensions[i]))
						return true;

				if(f.isDirectory() && folderShow)
					return true;

				return false;
			}

			@Override
			public String getDescription() 
			{ 
				return extensions[0];
			}
		}
		);

		if(folderSelect)
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		fileChooser.setAcceptAllFileFilterUsed(allFiles); 

		//Get the user option
		JFrame jf = new JFrame();
		int input = fileChooser.showOpenDialog(jf);
		String filePath = null;
		//If the user approves  a file get that filepath and send it
		if(input == JFileChooser.APPROVE_OPTION)
		{   
			File file = fileChooser.getSelectedFile();
			try 
			{
				filePath = file.getPath();
			}
			catch (Exception ex)
			{
				showErrorDialog("Open Error","Open Error:\nDid you move or delete\nthe file you are trying to open?\n"+ex);
			}
		}else{
			filePath = null;
		}

		return filePath;
	}

	/**
	 * Shows a quit dialog and returns the option
	 *
	 * @return     option (yes no cancel)
	 */
	public static int showQuitDialog()
	{
		Toolkit.getDefaultToolkit().beep();
		JFrame jf = new JFrame();
		int n = JOptionPane.showConfirmDialog(jf, "Do you want to save before you quit?", "Quit?", JOptionPane.YES_NO_CANCEL_OPTION);
		return n;
	}

	/**
	 * Error Dialog - alerts the user of an error
	 *
	 * @param title The title for the dialog window.
	 * @param body The prompt of the window.
	 */
	public static void showErrorDialog(String title, String body)
	{
		JFrame jf = new JFrame();
		JOptionPane.showMessageDialog(jf, body, title, 
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Warning Dialog - alerts the user of an warning
	 *
	 * @param title The title for the dialog window.
	 * @param body The prompt of the window.
	 */
	public static void showWarningDialog(String title, String body)
	{
		JFrame jf = new JFrame();
		JOptionPane.showMessageDialog(jf, body, title, 
				JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Information Dialog - shows Information to the user
	 *
	 * @param title The title for the dialog window.
	 * @param body The prompt of the window.
	 */
	public static void showInformationDialog(String title, String body)
	{
		JFrame jf = new JFrame();
		JOptionPane.showMessageDialog(jf, body, title, 
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Yes/No Dialog - Requests the user to say yes or no.
	 *
	 * @param title The title for the dialog window.
	 * @param body The prompt of the window.
	 * 
	 * @return True if the user pressed yes, False on anything else.
	 */
	public static boolean showYesNoQuestionDialog(String title, String body)
	{
		JFrame jf = new JFrame();
		int n = JOptionPane.showConfirmDialog(jf, body, title, 
				JOptionPane.YES_NO_OPTION);
		return n == JOptionPane.YES_OPTION;
	}

	/**
	 * Show the dialog for user input.
	 * 
	 * @param title The title for the dialog window.
	 * @param body The prompt of the window.
	 * @return The text the user entered.
	 */
	public static String showUserInputDialog(String title, String body)
	{
		JFrame jf = new JFrame();
		return JOptionPane.showInputDialog(jf, body, title, 
				JOptionPane.PLAIN_MESSAGE);
	}
	
	/**
	 * Show the dialog for an option box with options.
	 * Returns the chosen object, or null if none was chosen.
	 */
	public static Object showOptionDialog(String title, String body, Object[] options)
	{
		JFrame jf = new JFrame();
		
		
		// Sort the options
		Arrays.sort(options, new Comparator<Object>(){
			@Override
			public int compare(Object arg0, Object arg1)
			{
				return arg0.toString().compareTo(arg1.toString());
			}
		});
		
		return JOptionPane.showInputDialog(
		                    jf,
		                    body,
		                    title,
		                    JOptionPane.PLAIN_MESSAGE,
		                    null,
		                    options,
		                    options[0]);
	}
}
