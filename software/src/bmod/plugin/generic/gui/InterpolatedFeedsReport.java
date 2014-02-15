package bmod.plugin.generic.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import bmod.gui.GuiExtensionPoints;
import bmod.gui.widgets.TextPaginator;
import bmod.plugin.generic.headless.FeedPatchBot;

/**
 * A plugin that reports on interpolated feed status.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class InterpolatedFeedsReport extends GenericGuiPlugin
{

	private final JMenuItem feedsReportButton = new JMenuItem("Interpolated Feeds Report");
	private final JMenuItem[] itemGroup = new JMenuItem[]{feedsReportButton};
	private GuiExtensionPoints m_environment;

	
	public InterpolatedFeedsReport()
	{
		super("Interpolated Feeds Reporter", "Provides the ability to run reports on interpolated feeds.");
		
		feedsReportButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				String reportText = FeedPatchBot.getInterpolatedFeedsReport();
						
				if(reportText.length() == 0)
				{
					reportText = "Congratulations, no feeds were interpolated!";
				}
				
				
				m_environment.createWindow("Feed Interpolation Report", new TextPaginator(reportText));
			}
			
		});
	}

	@Override
	public void setup(final GuiExtensionPoints environment)
	{
		m_environment = environment;
		m_environment.addMenuItem("Reports", itemGroup);
	}

	@Override
	public void teardown()
	{
		if(m_environment != null)
		{
			m_environment.removeMenuItem("Reports", itemGroup);
		}
	}
	
}
