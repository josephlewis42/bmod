package bmod.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ProgressMonitor;

import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.GUIBuilderWidget;
import bmod.gui.builder.SimpleWrapperWidget;
import bmod.gui.widgets.DateTimeRangeChooser;
import bmod.gui.widgets.Dialogs;
import bmod.gui.widgets.HTMLPane;
import bmod.gui.widgets.NTable;
import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;

public class MultipleRegression extends JPanel
{
	private static final long serialVersionUID = 6540099110755009303L;
	private final JScrollPane jsp = new JScrollPane();
	private final NTable tblContents = new NTable(false, false, false, true, false);
	private final HTMLPane m_output = new HTMLPane();
	
	public MultipleRegression()
	{
		setLayout(new BorderLayout());
		add(jsp, BorderLayout.CENTER);
		
		
		GUIBuilderPanel internalPanel = new GUIBuilderPanel(new GUIBuilderWidget[]{
				new SimpleWrapperWidget("To Start, choose a range of times, and press \"Populate\"", 
						generateStartPanel(),false, true),
				new SimpleWrapperWidget("Next, cut and paste in data for the given dates and times. The last row is the regressor.", 
						generateEditorPanel(), true, true),
				new SimpleWrapperWidget("Finally, press \"Generate\" to do the math.", 
						getOutputPanel(), true, true)
		});
		
		jsp.setViewportView(internalPanel);

	}
	
	/**
	 * Performs a multiple regression on the data from the given NTable.
	 * 
	 * @param currentData
	 */
	public MultipleRegression(NTable currentData)
	{
		this();
		
		Object regressand = Dialogs.showOptionDialog("Select Regressand", 
							"Select the regressand you want to use", 
							currentData.getColumnNames());
		
		String[] newHeaders = currentData.getColumnNames();
		for(int i = 0; i < newHeaders.length; i++)
		{
			if(regressand.equals(newHeaders[i]))
			{
				newHeaders[i] = "Regressand: " + newHeaders[i];
			}
			else
			{
				if(currentData.isColumnNumeric(i))
				{
					newHeaders[i] = "Regressor: " + newHeaders[i];
				}
			}
		}
		
		tblContents.setTable(newHeaders, currentData.getData());
		process();
	}
	
	
	private JPanel generateStartPanel()
	{
		JPanel pnlStart = new JPanel();
		final DateTimeRangeChooser csrStart = new DateTimeRangeChooser();
		JButton btnPopulate = new JButton("Populate");
		
		btnPopulate.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(!tblContents.isTableEmpty())
					if(!Dialogs.showYesNoQuestionDialog("Warning", "This will erase all data currently in the table, continue?"))
						return;
				
				setupTableForRange(csrStart.getRange());				
			}
		});
		
		
		pnlStart.setLayout(new BoxLayout(pnlStart, BoxLayout.PAGE_AXIS));
		
		pnlStart.add(csrStart);
		pnlStart.add(btnPopulate);
		
		return pnlStart;
	}
	
	private JPanel generateEditorPanel()
	{
		JPanel pnlEditor = new JPanel();
		JButton btnAddCol = new JButton("Add Column");
		JButton btnDelCol = new JButton("Delete Column");
		// Use global editor.
		
		btnAddCol.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(tblContents.isTableEmpty())
					setupTableForRange(null);
				
				// Add column to the right of the date.
				tblContents.addColumn(1, "Regressor", "");
			}
		});
		
		btnDelCol.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				tblContents.deleteColumnDialog();
			}
		});
		
		tblContents.addToolbarEnd(btnAddCol);
		tblContents.addToolbarEnd(btnDelCol);
		
		pnlEditor.setLayout(new BoxLayout(pnlEditor, BoxLayout.PAGE_AXIS));
		
		pnlEditor.add(tblContents);
		
		return pnlEditor;
	}
	
	private JPanel getOutputPanel()
	{
		JPanel pnlOutput = new JPanel();
		JButton btnGenerate = new JButton("Generate");
		btnGenerate.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				process();
			}
			
		});
		
		pnlOutput.setLayout(new BoxLayout(pnlOutput, BoxLayout.PAGE_AXIS));
		
		pnlOutput.add(btnGenerate);
		pnlOutput.add(new JScrollPane(m_output));

		return pnlOutput;
	}


	protected void setupTableForRange(DateTimeRange range)
	{
		int numRows = 0;
		String[] header;

		if(null == range)
		{
			header = new String[]{"Regressand"};
			numRows = 1000;
		}else{
			for(@SuppressWarnings("unused") DateTime t : range)
				numRows++;
			
			header = new String[]{"Time","Regressor", "Regressand"};
		}
		
		String[][] output = new String[numRows][header.length];
		
		if(null != range)
		{
			int i = 0;
			for(DateTime t : range.getClone())
			{
				output[i][0] = t.toISODate();
				i++;
			}
		}
		
		tblContents.setTable(header, output);
	}
	
	
	/**
	 * Processes the table.
	 */
	private void process()
	{
		ProgressMonitor dlgProgress = new ProgressMonitor(this, "Doing Multiple Regression", "Fetching Regressor", 0, 3);
		Collection<String[]> regressors = tblContents.getContentsByName("Regressor");

		dlgProgress.setNote("Fetching Regressand");
		dlgProgress.setProgress(1);
		Collection<String[]> regressand = tblContents.getContentsByName("Regressand");
		dlgProgress.setNote("Calculating Regression");
		dlgProgress.setProgress(2);
		
		// DO MAGIC HERE
		LinkedList<String[]> all = new LinkedList<String[]>();
		for(String[] reg : regressors)
			all.add(reg);
		for(String[] reg : regressand)
			all.add(reg);
		
		try
		{
			double[][] inputs = toDoubleArray(all);
			
		
			MultipleRegressionCalculator calc = new MultipleRegressionCalculator(inputs);
		
		
			dlgProgress.setNote("Generating Output");
			dlgProgress.setProgress(3);
			m_output.setText(calc.getOutput());
		
		} catch(Exception ex)
		{
			dlgProgress.setProgress(4);
			Dialogs.showErrorDialog("Number Error", ex.getMessage());
			ex.printStackTrace();
		}	
		dlgProgress.setProgress(4);

		
	}
	
	/**
	 * Converts the lls to a double array suitable for parsing
	 * Linked List<String[]>
	 * =
	 * + cv, cv, cv, cv, cv
	 * + cv, cv, cv, cv, cv
	 * =
	 * 
	 * to Double[][]
	 * =
	 * + cv, cv
	 * + cv, cv
	 * + cv, cv
	 * + cv, cv
	 * + cv, cv
	 * =
	 * 
	 * @param lls
	 * @return
	 */
	private double[][] toDoubleArray(LinkedList<String[]> lls)
	{
		if(lls.size() < 2)
			throw new NumberFormatException("Must have at least two columns");
		
		
		// Because we have the option of having extra rows, count the max number
		// we will take.
		int maxElements = 0;
		for(maxElements = 0; maxElements < lls.get(0).length; maxElements++)
			try{
				Double.parseDouble(lls.get(0)[maxElements]);
			} catch(Exception ex){
				break;
			}
				
		double[][] output = new double[maxElements][lls.size()];

		
		for(int col = 0; col < lls.size(); col++)
			for(int row = 0; row < maxElements; row++)
				try
				{
					output[row][col] = Double.parseDouble(lls.get(col)[row]);
				} catch(Exception ex)
				{
					String val = "regressor #" + (col + 1);
					if(col == lls.size() - 1)
						val = "regressand";
					ex.printStackTrace();
					throw new NumberFormatException("Illegal value at " + val + " row:" + row);
				}
		
		return output;
	}
}
