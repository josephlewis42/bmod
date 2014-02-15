package bmod.plugin.generic.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import bmod.IconLoader;
import bmod.PredictionModel;
import bmod.gui.widgets.DecimalSpinner;
import bmod.gui.widgets.HTMLPane;
import bmod.plugin.baseclass.OutputWidget;
import bmod.util.DateTime;
import bmod.util.TimeDelta;


public class InvestmentCalculations extends OutputWidget
{
	
	private final DecimalSpinner m_productLifeYears = new DecimalSpinner(0,0,300,1,0);
	private final DecimalSpinner m_initalCostSpinner = new DecimalSpinner(0,0,10000000,.01,2);
	private final DecimalSpinner m_returnSpinner = new DecimalSpinner(0,0,1.0,.01,2);
	private final DecimalSpinner m_MaximumPaybackPeriodSpinner = new DecimalSpinner(0,0,500,1,0);

	
	private final JToolBar m_toolbar = new JToolBar();
	private final HTMLPane m_output = new HTMLPane();
	private final JButton m_clear_baseline = new JButton("Clear Baseline");
	private static final String INFO_TEXT = "The next generated run will be set as the baseline.<br>" +
											  "Future runs will be calculated against it, until you clear it.<br>" +
											  "This plugin needs at least one year of data to be run in order to work.";
	boolean baselineSet = false;
	double lbsCO2Baseline;
	double lbsSO2Baseline;
	double lbsNOxBaseline;
	double lbsHGBaseline;
	double lbsCH4Baseline;
	double lbsN2OBaseline;
	DateTime lastStart;
	DateTime lastEnd;
	double[] billTotals;

	

	public InvestmentCalculations()
	{
		super("Investment Calculations", 
				"A calculator that allows you to see how much money a simulation is saving.",
				IconLoader.getIcon("calendar.png"));
		
		outputWidgetPanel.setLayout(new BorderLayout(0, 0));
		JScrollPane m_scroller = new JScrollPane();
		outputWidgetPanel.add(m_scroller, BorderLayout.CENTER);
		
		JPanel header = new JPanel(new BorderLayout(0,0));
		JPanel gridit = new JPanel(new GridLayout(0,4));
		
		outputWidgetPanel.add(header, BorderLayout.PAGE_START);
		header.add(m_toolbar, BorderLayout.PAGE_START);
		header.add(gridit, BorderLayout.CENTER);
		
		
		m_toolbar.add(m_output.getPrintButton());
		m_toolbar.add(m_output.getSaveButton());
		m_toolbar.add(m_clear_baseline);
		
		m_clear_baseline.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				baselineSet = false;
				m_output.setText(INFO_TEXT);
			}
		});
		
		
		gridit.add(new JLabel("Product Life (Years)"));
		
		gridit.add(m_productLifeYears);

		gridit.add(new JLabel("Initial Cost ($)"));
		gridit.add(m_initalCostSpinner);
		gridit.add(new JLabel("Minimum Rate of Return"));
		
		gridit.add(m_returnSpinner);
		gridit.add(new JLabel("Maximum Payback Period (Years)"));
		gridit.add(m_MaximumPaybackPeriodSpinner);
		
		
        m_output.setContentType("text/html");
		m_scroller.setViewportView(m_output);
		m_output.setText(INFO_TEXT);
	}
	
	@Override
	public void generate(PredictionModel model)
	{
		// Start time must be at least a year after end time.
		if(! model.getStartTime().plusTime(new TimeDelta(365)).before(model.getEndTime()))
		{
			return;
		}
		
		BillDisplay bd = new BillDisplay(1);
		bd.generate(model);
		
		if(! baselineSet)
		{
			try
			{
				lbsCO2Baseline = bd.getLbsCO2Year(); //(Double)	ipc.get("lbsCO2Year");
				lbsSO2Baseline = bd.getLbsSO2Year();
				lbsNOxBaseline = bd.getLbsNOxYear();
				lbsHGBaseline  = bd.getLbsHgYear();
				lbsCH4Baseline = bd.getLbsCH4Year();
				lbsN2OBaseline = bd.getLbsN2OYear();
				billTotals 	   = bd.getFirstYearBills();
			}
			catch(Exception e)
			{
				m_logger.error(e.getMessage(),e);
				throw new IllegalArgumentException("The needed params could not be found, perhaps you are missing a plugin.");
			}
			lastStart = model.getStartTime();
			lastEnd = model.getEndTime();
		
			m_output.setText("Baseline is ready, make your changes now, and re-generate the model to see effects.");
			baselineSet = true;
		}
		else
		{
			if(! model.getStartTime().equals(lastStart) ||
				! model.getEndTime().equals(lastEnd))
				{
					m_output.setText("The date params have changed! Clear the baseline, or re set the start and end times to:"
							+ "Start: " + lastStart + " end: " + lastEnd);
					return;
				}
			generateBaselineOutput(bd);
		}
	}
	
	

	/**
	 * @param args
	 */
	public void generateBaselineOutput(BillDisplay bd) 
	{
		double NPV;
		
		//Savings is just $Baseline run - $ modified Run for all the bills in that year
		double lbsCO2SavedLifetime =  lbsCO2Baseline - bd.getLbsCO2Year();
		double lbsSO2SavedLifetime =  lbsSO2Baseline - bd.getLbsSO2Year();
		double lbsNOxSavedLifetime =  lbsNOxBaseline - bd.getLbsNOxYear();
		double lbsHGSavedLifetime  =  lbsHGBaseline - bd.getLbsHgYear();
		double lbsCH4SavedLifetime =  lbsCH4Baseline - bd.getLbsCH4Year();
		double lbsN2OSavedLifetime =  lbsN2OBaseline - bd.getLbsN2OYear();
		double[] secondBills 	   = bd.getFirstYearBills();
		//Sum these from the emissions report.  Baseline Emissions - modified emissions.
		
		final double productLifetime = m_productLifeYears.getDouble();
		
		
		double[] Savings = new double[billTotals.length];
		for(int i = 0; i < secondBills.length; i++)
			Savings[i] = billTotals[i] - secondBills[i];
		
		System.out.println(Arrays.toString(billTotals));
		System.out.println(Arrays.toString(secondBills));
		System.out.println(Arrays.toString(Savings));
		
		double yearlySavings = 0;
		for(int i = 0; i < billTotals.length; i++)
			yearlySavings += Savings[i];
		
		System.out.println(yearlySavings);
		
		
		double NPVZero;
		double InternalRateOfReturn;
		
		
		
		//User Inputs
		double InitialCost = m_initalCostSpinner.getDouble();
		double MinimumRateofReturn = m_returnSpinner.getDouble();
		double MaximumPaybackPeriod = m_MaximumPaybackPeriodSpinner.getDouble();
		//End User Inputs
		
		
		
		InternalRateOfReturn = 999999;
		NPVZero = 99999;
		double PaybackPeriodDec = 0;
		double SumofCashflows = -InitialCost;
		for(double i = 0; i <= 2000; i++)
		{
			if (NPVZero >= 0)
			{
				InternalRateOfReturn = 0 + i / 1000;
				NPVZero = 0;
				for(int Year = 0; Year <= productLifetime; Year++)
				{
					if(Year == 0)
						NPVZero = (-InitialCost + yearlySavings) / (Math.pow((1 + InternalRateOfReturn),Year)) + NPVZero;
					else
						NPVZero = yearlySavings / (Math.pow((1 + InternalRateOfReturn),Year)) + NPVZero;
				}
			}
		}
		boolean PaybackPeriodFlag = true;
		double PaybackPeriod;
		PaybackPeriod = 100;
		NPV = 0;
			for(int Year = 0; Year <= productLifetime; Year++)
			{
				if(Year == 0)
					NPV = (-InitialCost + yearlySavings) / (Math.pow((1 + MinimumRateofReturn),Year)) + NPV;
				else
					NPV = yearlySavings / (Math.pow((1 + MinimumRateofReturn),Year)) + NPV;
				SumofCashflows = yearlySavings + SumofCashflows;
				if (SumofCashflows >= 0 && PaybackPeriodFlag)
				{
					PaybackPeriodDec = 1 - SumofCashflows / yearlySavings;
					PaybackPeriod = Year - 1 + PaybackPeriodDec;
					PaybackPeriodFlag = false;
				}
			}
			
			String document = "";
			
			document += generateRow("Financial Report", "");
			
			
			document += generateRow("Initial Cost", String.format("$ %.2f", InitialCost));
			document += generateRow("Product Lifetime", m_productLifeYears.getDouble() + " Years");
			document += generateRow("Maximum Allowed Payback Period", String.format("%.2f Years", MaximumPaybackPeriod));
			document += generateRow("Minimum Rate of Return", Math.round(MinimumRateofReturn*10000)/100.0 + " %");
			document += generateRow("Predicted Payback Period", String.format("%.2f Years", PaybackPeriod));
			document += generateRow("PredictedInternal Rate Of Return", Math.round(InternalRateOfReturn*10000)/100.0, " %");
			document += generateRow("Predicted Net Present Value", String.format("$ %.2f", NPV));
			document += generateRow("Yearly savings", yearlySavings);

			if (InternalRateOfReturn >= MinimumRateofReturn && PaybackPeriod <= MaximumPaybackPeriod && NPV >= 0)
				document += generateRow("Is Measure Reccomended?", "Yes");
			else
				document += generateRow("Is Measure Reccomended?", "No");
			
			document += generateRow("","");
			document += generateRow("","");
			document += generateRow("Emissions Report (Yearly Savings)");
			document += generateRow("","");
			document += generateRow("CO2 Saved", lbsCO2SavedLifetime + " lbs/yr");
			document += generateRow("SO2 Saved", lbsSO2SavedLifetime + " lbs/yr");
			document += generateRow("NOx Saved", lbsNOxSavedLifetime + " lbs/yr");
			document += generateRow("HG Saved",  lbsHGSavedLifetime  + " lbs/yr");
			document += generateRow("CH4 Saved", lbsCH4SavedLifetime + " lbs/yr");
			document += generateRow("N2O Saved", lbsN2OSavedLifetime + " lbs/yr");
			document += generateRow("","");
			document += generateRow("Emissions Report (Lifetime Savings)");
			document += generateRow("","");
			document += generateRow("CO2 Saved", lbsCO2SavedLifetime * productLifetime + " lbs");
			document += generateRow("SO2 Saved", lbsSO2SavedLifetime * productLifetime + " lbs");
			document += generateRow("NOx Saved", lbsNOxSavedLifetime * productLifetime + " lbs");
			document += generateRow("HG Saved",  lbsHGSavedLifetime * productLifetime + " lbs");
			document += generateRow("CH4 Saved", lbsCH4SavedLifetime * productLifetime + " lbs");
			document += generateRow("N2O Saved", lbsN2OSavedLifetime * productLifetime + " lbs");
			
		m_output.setText(document);
	}
	
	
	private String generateRow(Object... strings)
	{
		String output = "<tr>";
		
		for(int i = 0; i < strings.length; i++)
			output += "<td>" + strings[i] + "</td>";
		
		output += "</tr>";
		
		return output;
	}
}
