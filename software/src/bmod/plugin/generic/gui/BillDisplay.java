package bmod.plugin.generic.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import bmod.DataSet;
import bmod.IconLoader;
import bmod.PredictionModel;
import bmod.gui.widgets.Dialogs;
import bmod.gui.widgets.HTMLPane;
import bmod.plugin.baseclass.OutputWidget;
import bmod.util.DateTime;
import bmod.util.TimeDelta;

public class BillDisplay extends OutputWidget
{
	private final HTMLPane m_output = new HTMLPane();
	private final JToolBar m_toolbar = new JToolBar();
	private final JButton m_generateYearsButton = new JButton("Generate Multiple Years");

	private final static double CO2EmRate = .191088;
	private final static double SO2EmRate = .0025346;
	private final static double NOxEmRate = .0029224;
	private final static double HGEmRate  = .0000000164;
	private final static double CH4EmRate = .00002348;
	private final static double N2OEmRate = .00002926;
	private static final int DAYS_IN_BILLING_PERIOD = 29;
	


	private String buildingName;
	private static final String INFO_TEXT = "<h1>Bill Calculator</h1>" +
		"<p>To use the bill calculator:</p><ul><li> Set the increment to 15 min (900 secs) for best results</li><li>" +
		"Make sure the start date to end date is &gt;= 29 days</li>" + 
		"<li>To generate multi-year bills, make sure the start and end dates are the same, and one year apart.</li></ul>";
	
	double	bmodDemand;
	double	bmodConsumption;
	String startDate;	//MM/DD/YYYY
	String endDate;		//MM/DD/YYYY
	transient PredictionModel m_model = null;
	
	int numReportsSoFar = 0; // The number of reports generated so far, there are 13 in a year.

	
	String currentBill = ""; // Holds the most recently generated bill.
	int year = 0;  //This is only used for long term financial.  Dont worry about this now.

	int m_numYears; // This can be set by calling functions to get rid of
						  // the prompt for number of years. -1 says prompt user.
	
	public BillDisplay()
	{
		this(-1); 
	}
	
	
	public BillDisplay(int numberOfYearsToGenerate)
	{
		super("Electric Bill",
				"An Excel Energy bill simulator.",
				IconLoader.getIcon("lightning.png"));
		
		m_numYears = numberOfYearsToGenerate;
		
		outputWidgetPanel.setLayout(new BorderLayout(0, 0));
		JScrollPane m_scroller = new JScrollPane();
		outputWidgetPanel.add(m_scroller, BorderLayout.CENTER);
		
		outputWidgetPanel.add(m_toolbar, BorderLayout.PAGE_START);
		m_toolbar.add(m_output.getPrintButton());
		m_toolbar.add(m_output.getSaveButton());
		m_toolbar.add(m_generateYearsButton);
		
		m_generateYearsButton.setEnabled(false);
		
		m_scroller.setViewportView(m_output);
		m_output.setText(INFO_TEXT);
		
		m_generateYearsButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				// Get the desired number of years, or none
				Integer i = (Integer) Dialogs.showOptionDialog("Years", "Generate x years of bills:", new Integer[]{1,2,3,4,5,10,15,20,25,50});		
				if(i == null)
				{
					return;
				}
				
				m_numYears = i;
				generateYear();
			}
		});
	}
	
	@Override
	public void generate(PredictionModel model)
	{
		m_model = model;
		numReportsSoFar = 0;
		
		m_generateYearsButton.setEnabled(canGenerateYear());
		
		if(m_model == null)
		{
			m_output.setText(INFO_TEXT);
			return;
		}
		
		try
		{
			buildingName = m_model.getBuilding().getId();
		} catch(ArrayIndexOutOfBoundsException ex)
		{
			m_output.setText("<h1>Error</h1><p>"+ex.getMessage()+"</p>" + INFO_TEXT);
			return;
		}
		
		setupParams();
		
		
		year = 0;
		
		if(m_numYears != 0 && generateYear())
		{
			return;
		}
		
		// If the dates are not the same, just generate for as long as possible.
		generateMonths();
	}
	
	/**
	 * Checks if the year can be generated with the present model. 
	 * @return
	 */
	private boolean canGenerateYear()
	{
		if(m_model == null)
		{
			return false;
		}
		
		if(!m_model.getStartTime().isSameDayAndMonth(m_model.getEndTime()))
		{
			return false;
		}
		
		if(m_model.getStartTime().getYear() == m_model.getEndTime().getYear())
		{
			return false;
		}
		
		return true;
	}
		
	
	/**
	 * Generates as many 28 day cycles of data as possible.
	 * @param ipc 
	 */
	private void generateMonths()
	{
		LinkedList<DateTime> dates = new LinkedList<DateTime>();
		DateTime start = m_model.getStartTime();
		String output = "";
		
		dates.push(start);
		
		// Make sure the length of time is right
		DateTime end = start.plusTime(new TimeDelta(DAYS_IN_BILLING_PERIOD)); // 29 days
		while (end.equals(m_model.getStartTime()) || 
				 (end.after(m_model.getStartTime()) && end.before(m_model.getEndTime()) ))
		{
			dates.add(end);
			end = end.plusTime(new TimeDelta(DAYS_IN_BILLING_PERIOD)); // 29 days
		}
				
		// If there aren't enough dates, just quit.
		if(dates.size() < 2)
		{
			m_output.setText(INFO_TEXT);
			return;
		}
			
		// For each date in the range, generate a model.
		for(int i = 0; i < dates.size() - 1; i++)
		{
			start = dates.get(i);
			end   = dates.get(i + 1);
			startDate = start.format("MM/dd/yyyy");
			endDate   = end.format("MM/dd/yyyy");
			
		
			// Get the max power usage for the month
			double max_usage = 0;
			double total_usage = 0;
			
			for(DateTime t : m_model.getTimeRange())
			{
				if(t.before(end) && t.after(start))
				{
					double thisUsage = m_model.getTotalWattageAtTime(t);
					
					if(thisUsage > max_usage)
						max_usage = m_model.getTotalWattageAtTime(t);
					
					total_usage += thisUsage;
				}
			}
			
			bmodDemand = max_usage / 1000.0;	// to kw
			bmodConsumption = (total_usage / 1000.0) * (m_model.getInterval() / 3600.0);	// To KWH
			
			
			// Do the output
			output += doOutput();
		}
		
		m_output.setText(output);
	}
	
	/**
	 * Sums up all of the wattages for a given date.
	 * @return
	 */
	private Map<Integer, Double> getWattsPerDate()
	{
		DataSet estimatedW = m_model.getEstimatedWData().dailySums();
		HashMap<Integer, Double> wattsPerDate = new HashMap<Integer, Double>(366);
		
		for(Entry<DateTime, Double> ent : estimatedW.getPoints())
		{
			wattsPerDate.put(toTimeRepresentation(ent.getKey()),ent.getValue());
		}
		
		if(wattsPerDate.get(129) == null) // FEB 29 (things start with 0)
		{
			wattsPerDate.put(129, wattsPerDate.get(128));
		}
		
		return wattsPerDate;
	}
	
	private int toTimeRepresentation(DateTime time)
	{
		return time.getMonth() * 100 + time.getDayOfMonth();
	}
	
	/**
	 * Returns a map of the daily peaks of the given model where the key is
	 * a result of toTimeRepresentation for the respective date.
	 * @return
	 */
	private Map<Integer, Double> getDailyPeaks()
	{
		DataSet estimatedW = m_model.getEstimatedWData();
		HashMap<Integer, Double> dailyPeaks = new HashMap<Integer, Double>(366);
		
		for(DateTime t : m_model.getTimeRange())
		{
			dailyPeaks.put(toTimeRepresentation(t), 0.0);
		}

		// Setup the HashMap of dates
		for(DateTime t : m_model.getTimeRange())
		{
			int timeRepresentation = toTimeRepresentation(t);
			
			Double todayPeak = dailyPeaks.get(timeRepresentation);			
			double thisUsage = estimatedW.getValue(t, 0);
			
			if(thisUsage > todayPeak)
			{
				dailyPeaks.put(timeRepresentation, thisUsage);
			}
		}
		
		// Calculate leap year if it is not calculated
		if(dailyPeaks.get(129) == null) // FEB 29 (things start with 0)
		{
			dailyPeaks.put(129, dailyPeaks.get(128));
		}
		
		return dailyPeaks;
	}
	
	
	/**
	 * Generates a full year of data.
	 * @param ipc 
	 * @return true if the data was generated from this function.
	 */
	private boolean generateYear()
	{
		boolean cgy = canGenerateYear();
		
		if(cgy == false)
		{
			return false;
		}
		
		
		int i = m_numYears;
		
		if(m_numYears < 0)
		{
			return false;
		}
		
		
		Map<Integer, Double> wattsPerDate = getWattsPerDate();
		Map<Integer, Double> dailyPeaks = getDailyPeaks();
		String output = "<html><body>";
		
		
		// add i years plus max number of leap days	
		DateTime generationEnd = m_model.getStartTime().plusTime(new TimeDelta( 365 * i + 24 * (i / 4))); 
		
		
		
		
		for(int k : dailyPeaks.keySet())
			System.out.println(k+" Peak: "+ dailyPeaks.get(k) + " Usage: " + wattsPerDate.get(k));

		
		
		// Now loop over the dates and generate bills for them.		
		DateTime curr = m_model.getStartTime();
		DateTime next, start;
		while(curr.before(generationEnd))
		{
			next = curr.plusTime(new TimeDelta(DAYS_IN_BILLING_PERIOD));
			start = curr.clone();
			
			double totalWatts = Double.MIN_VALUE;
			double maxWatts = Double.MIN_VALUE;
			
			while(! curr.isSameDayAndMonth(next))
			{
				curr = curr.plusTime(new TimeDelta(1));
				
				int timeRepresentation = toTimeRepresentation(curr);
				
				double dailyPeak = dailyPeaks.get(timeRepresentation);
				if(dailyPeak > maxWatts)
				{
					maxWatts = dailyPeak;
				}
				
				totalWatts += wattsPerDate.get(timeRepresentation);
			}
			
			bmodDemand = maxWatts / 1000.0;	// 
			bmodConsumption = (totalWatts / 1000.0) * (m_model.getInterval() / 3600.0);	// 
			startDate = start.format("MM/dd/yyyy");
			endDate = next.format("MM/dd/yyyy");
			
			
			// Do the output
			output += doOutput();	
			
			curr = next;
		}
		
		output += "</body></html>";
		
		
		m_output.setText(output);
		currentBill = output;
		
		return true;
	}

	/**
	 * @param args
	 
	 
	 demand 15min peak for 29 days
	 consumption number of kWh
	 
	 kWh = kW * (t(in hours)) / 1 hr (60 kw at 15 min is 4 kwh)
	 
	 
	 	 
	 */
	private String doOutput() 
	{
		numReportsSoFar++;
		//Inputs that should be pulled from the model
		//String StartDate = "01/13/2011";  // Must be in this format
		//String EndDate = "02/11/2011";  // Must be in this format
		String Rate = "SG       Seconday General";  //This is specific to each building
		int Multiplier = 160;  //This is specific to each building (essentially the smallest # the meter can handle)
		int BilledDemandMin = 120; //Leave this hardcoded for now It is too hard to explain
		String ServiceAddress1 = "2190 E Iliff Ave";  //This is specific to each building
		int PreviousReadingnum = 0;  // Get this from previous Bill
		String DateofBill = startDate;  //Don't worry about this for now.  It has no real effect on anything
		String DateDue = "Jan 01, XXXX";  //Don't worry about this for now.  It has no real effect on anything
		double ElectricityEscalationRate = .055;  //This is only used for long term financial.  Dont worry about this now.
		int DaysWinter = 0;  //This actually comes from http://www.xcelenergy.com/staticfiles/xe/Regulatory/COBusRates.pdf.  It has nothing to do with summer or winter, but with summer or winter rate0
		//http://www.xcelenergy.com/About_Us/Rates_&_Regulations/Supplemental_Information/Tiered_Rate_Bill_Estimator
		
		
		double MeasuredDemandnum = bmodDemand / Multiplier;
		double MeasuredUsage = Math.round(bmodConsumption / Multiplier);
		
		
		int DaysSummer = DAYS_IN_BILLING_PERIOD - DaysWinter;
		double GTDmult1 = 8.000000;
		
		double GTDmult2 = 10.960000;
		
		double GTDmult1Esc = EscalationInflationCalc(GTDmult1, ElectricityEscalationRate, year);
		double GTDmult2Esc = EscalationInflationCalc(GTDmult2, ElectricityEscalationRate, year);
		double GTDmultBlendEsc = DaysWinter / (double) DAYS_IN_BILLING_PERIOD * GTDmult1Esc + DaysSummer / (double) DAYS_IN_BILLING_PERIOD * GTDmult2Esc;
		
		
		double DistDemmult = 4.840000;
		double DistDemmultEsc = EscalationInflationCalc(DistDemmult, ElectricityEscalationRate, year);
		double PCCAmult = 4.100000;
		double PCCAmultEsc = EscalationInflationCalc(PCCAmult, ElectricityEscalationRate, year);
		double DSMmult = .310000;
		double DSMmultEsc = EscalationInflationCalc(DSMmult, ElectricityEscalationRate, year);
		double ElecComAdjmult = .028510;
		double ElecComAdjmultEsc = EscalationInflationCalc(ElecComAdjmult, ElectricityEscalationRate, year);
		double TransCostAdjmult = .070000;
		double TransCostAdjmultEsc = EscalationInflationCalc(TransCostAdjmult, ElectricityEscalationRate, year);
		double SecondaryGeneralmult = .004730;
		double SecondaryGeneralmultEsc = EscalationInflationCalc(SecondaryGeneralmult, ElectricityEscalationRate, year);
		//double CurrentReadingnum = BmodConsumption;
		String CustomerName = "UNIV OF DENVER EDUCATIONAL PHYSICAL PLANT";
		String ServiceAddress2 = "DENVER, CO 80210-5212";
		int REfee = 66;
		double REfeeEsc = EscalationInflationCalc(REfee, ElectricityEscalationRate, year);
		//MeasuredUsage = CurrentReadingnum - PreviousReadingnum;
		double kWhUsed = MeasuredUsage * Multiplier;
		String SecondaryGeneraltxt = kWhUsed + " kWh x " + SecondaryGeneralmult;
		double SecondaryGeneralnum = kWhUsed * SecondaryGeneralmultEsc;
		double SecondaryGeneralRound = Math.round(SecondaryGeneralnum * 100.0) / 100.0;
		double BilledDemandnum = Math.max(BilledDemandMin, Math.round(Multiplier * MeasuredDemandnum));
		double BilledDemandActual = Math.round(Multiplier * MeasuredDemandnum);
		String TransCostAdjtxt = BilledDemandActual + " kW x " + TransCostAdjmultEsc;
		double TransCostAdjnum = BilledDemandActual * TransCostAdjmultEsc;
		String ElectricityCommodityAdjtxt = kWhUsed + " kWh x " + ElecComAdjmultEsc;
		double ElectricityCommodityAdjnum = kWhUsed * ElecComAdjmultEsc;
		String DSMtxt = BilledDemandActual + " kW x " + DSMmultEsc;
		double DSMnum = BilledDemandActual * DSMmultEsc;
		String PCCAtxt = BilledDemandActual + " kW x " + PCCAmultEsc;
		double PCCAnum = BilledDemandActual * PCCAmultEsc;
		String DistDemtxt = BilledDemandnum + " kW x " + DistDemmultEsc;
		double DistDemnum = BilledDemandnum * DistDemmultEsc;
		String GTDtxt = BilledDemandActual + " kW x " + GTDmultBlendEsc;
		double GTDnum = BilledDemandActual * GTDmultBlendEsc;
		
		double TransCostAdjRound = Math.round(TransCostAdjnum*100.0) / 100.0;
		double ElectricityCommodityAdjRound = Math.round(ElectricityCommodityAdjnum*100.0) / 100.0;
		double DSMRound = Math.round(DSMnum*100.0) / 100.0;
		double PCCARound = Math.round(PCCAnum*100.0) / 100.0;
		double DistDemRound = Math.round(DistDemnum*100.0) / 100.0;
		double GTDRound = Math.round(GTDnum*100.0) / 100.0;
		double ServiceFee = 40.00;
		double ServiceFeeEsc = EscalationInflationCalc(ServiceFee, ElectricityEscalationRate, year);
		double Subtotal = ServiceFeeEsc + GTDRound + DistDemRound + PCCARound + DSMRound + ElectricityCommodityAdjRound + TransCostAdjRound + REfeeEsc + SecondaryGeneralRound;
		double SubtotalRound = Math.round(Subtotal*100.0) / 100.0;
		double FranchiseFeemult = 3;
		double FranchiseFeenum = Subtotal * FranchiseFeemult / 100;
		double FranchiseFeeRound = Math.round(FranchiseFeenum*100.0) / 100.0;
		String FranchiseFeetxt = FranchiseFeemult + ".%";
		double SalesTaxmult = 0;
		double SalesTaxnum = (SubtotalRound + FranchiseFeeRound) * SalesTaxmult / 100;
		double SalesTaxRound = Math.round(SalesTaxnum*100.0) / 100.0;
		double TotalAmount = Math.round((SalesTaxRound + FranchiseFeeRound + Subtotal)*100.0) / 100.0;
		
		double PercfromDem = Math.round((GTDRound + DistDemRound + PCCARound + DSMRound  + TransCostAdjRound) / Subtotal * 100);
		double PercfromCon =  Math.round((ElectricityCommodityAdjRound + SecondaryGeneralRound) / Subtotal * 100);
		double DemandCostRed = Math.round((1 * (GTDmultBlendEsc + DistDemmultEsc + PCCAmultEsc + DSMmultEsc + TransCostAdjmultEsc) * (1 + FranchiseFeemult/100) * (1 + SalesTaxmult/100))*100.0) / 100.0;
		double ConsumpCostRed = Math.round((1 * (ElecComAdjmultEsc + SecondaryGeneralmult) * (1 + FranchiseFeemult/100) * (1 + SalesTaxmult/100))*10000.0) / 10000.0;
		double ConsumpCostRedWholeBill = Math.round(ConsumpCostRed * DAYS_IN_BILLING_PERIOD * 24 * 100.0) / 100.0;
		
		
		double BlendedCostperkWh = TotalAmount / kWhUsed;

		double lbsCO2 = CO2EmRate * kWhUsed;
		double lbsSO2 = SO2EmRate * kWhUsed;
		double lbsNOx = NOxEmRate * kWhUsed;
		double lbsHG  = HGEmRate  * kWhUsed;
		double lbsCH4 = CH4EmRate * kWhUsed;
		double lbsN2O = N2OEmRate * kWhUsed;
		
		
		
		String doc = "";
		
		doc += "<br>";
		doc += "<h3>" + buildingName + "(" + startDate + " - " + endDate + ") Demand: " + bmodDemand + "kW Consumption: " + bmodConsumption + "kWh</h3>";
		doc += "<hr>";
		
		doc += "<table>";
		doc += generateHeaderRow("Customer Name", "Service Address", "Account No.", "Date Due", "Amount Due");
		doc += generateRow(CustomerName, ServiceAddress1, ServiceAddress2, "xx-xxxxxxxx-x", DateDue, TotalAmount);
		doc += "</table>";
		
		doc += "<hr>";
		
		
		doc += "<table>";
		doc += generateHeaderRow("Acount Activity");
		doc += generateRow("Date of Bill", DateofBill, "Previous Balance", "$0.00");
		doc += generateRow("Number of Payments Received", "x", "Total Payments", "($0.00)");
		doc += generateRow("Number of Days In Bill Period", DAYS_IN_BILLING_PERIOD, "Balance Forward", "$0.00");
		doc += generateRow("Statement Number", "xxxxxxxxx", "+ Current Bill", TotalAmount);
		doc += generateRow("Premise Number", "xxxxxxxxx", "Current Balance", TotalAmount);
		doc += "</table>";
		
		doc += "<hr>";
		
		doc += "<table>";
		doc += generateHeaderRow("Electric Service - Account Summary");
		doc += generateRow("Invoice Number", "xxxxxxxxx", "", "Secondary General", SecondaryGeneraltxt, SecondaryGeneralRound);
		doc += generateRow("Meter No", "xxxxxxxxxxxx", "","Trans Cost Adj", TransCostAdjtxt, (Math.round(TransCostAdjnum*100.0) / 100.0));
		doc += generateRow("Rate", Rate,"", "Elec Commodity Adj", ElectricityCommodityAdjtxt, ElectricityCommodityAdjRound);
		doc += generateRow("Days In Bill Period", DAYS_IN_BILLING_PERIOD, "", "Demand Side Mgmt Cost", DSMtxt,DSMRound);
		doc += generateRow("Current Reading", bmodConsumption, "Actual    " + endDate,"Purch Cap Cost Adj", PCCAtxt, PCCARound);
		doc += generateRow("Previous Reading", PreviousReadingnum, "Actual    " + startDate, "Distribution Demand", DistDemtxt, DistDemRound);
		doc += generateRow("Multiplier", Multiplier, "", "Gen & Transm Demand", GTDtxt, GTDRound);
		doc += generateRow("Measurured Usage", MeasuredUsage, "", "Service & Facility", "",String.format("%.2f", ServiceFeeEsc));
		doc += generateRow("Kilowatt-Hours Used", kWhUsed, "", "Renew. Energy Std Adj", "", REfeeEsc);
		doc += generateRow("Measured Demand", String.format("%.4f", MeasuredDemandnum), "kW     Actual", "<b>Subtotal</b>", "",SubtotalRound);
		doc += generateRow("Billed Demand", BilledDemandnum, "kW", "Franchise Fee", FranchiseFeetxt, String.format("%.2f", FranchiseFeenum));
		doc += generateRow("","","","Sales Tax","",SalesTaxRound);
		doc += generateHeaderRow("","","","Total Amount","",TotalAmount);
		doc += "</table>";
		
		doc += "<hr>";
		
		doc += "<table>";
		doc += generateHeaderRow("DU Smart Grid Summary");
		doc += generateRow("% Of Bill from Demand", PercfromDem + "%");
		doc += generateRow("% Of Bill from Consumption", PercfromCon + "%");
		doc += generateRow("Blended Cost per kWh", String.format("$%.5f", BlendedCostperkWh));
		doc += generateRow("$ per kW Reduction", "$" + DemandCostRed);
		doc += generateRow("$ per kWh Reduction","$" + ConsumpCostRed);
		doc += generateRow("$ per kWh x billed hours", "$" + ConsumpCostRedWholeBill);
		doc += generateRow("$ per baseline kW reduced", "$" + (ConsumpCostRedWholeBill + DemandCostRed));
		doc += "</table>";
		
		doc += "<hr>";
		
		//http://cfpub.epa.gov/egridweb/view_st.cfm 2005 colorado data
		doc += "<table>";		
		doc += generateHeaderRow("Emmissions Report");
		doc += generateRow("CO2", String.format("%.5f", lbsCO2), "lbs");
		doc += generateRow("SO2", String.format("%.5f", lbsSO2), "lbs");
		doc += generateRow("NOx", String.format("%.5f", lbsNOx), "lbs");
		doc += generateRow("Hg",  String.format("%.5f", lbsHG),  "lbs");
		doc += generateRow("CH4", String.format("%.5f", lbsCH4), "lbs");
		doc += generateRow("N2O", String.format("%.5f", lbsN2O), "lbs");
		doc += "</table>";
		
		
		// Do param saving to the IPC for other plugins
		ipc_lbsCO2Year += lbsCO2;
		ipc_lbsSO2Year += lbsSO2;
		ipc_lbsNOxYear += lbsNOx;
		ipc_lbsHgYear += lbsHG;
		ipc_lbsCH4Year += lbsCH4;
		ipc_lbsN2OYear += lbsN2O;
		
		if(numReportsSoFar - 1 < 13 )
		{
			ipc_firstYearBills[numReportsSoFar - 1] = TotalAmount;
		}
				
		currentBill = doc;
		
		return doc;
	}
	
	private String generateRow(Object... strings)
	{
		String output = "<tr>";
		
		for(int i = 0; i < strings.length; i++)
			output += "<td>" + strings[i] + "</td>";
		
		output += "</tr>";
		
		return output;
	}
	
	private String generateHeaderRow(Object... strings)
	{
		String output = "<tr>";
		
		for(int i = 0; i < strings.length; i++)
			output += "<th>" + strings[i] + "</th>";
		
		output += "</tr>";
		
		return output;
	}
	
	public static double EscalationInflationCalc(double OldRate, double ElectricityEscalationRate, int Year)
	{
		return OldRate * Math.pow((1 + ElectricityEscalationRate),Year);
    }
	
	double ipc_lbsCO2Year = 0;
	double ipc_lbsSO2Year = 0;
	double ipc_lbsNOxYear = 0;
	double ipc_lbsHgYear = 0;
	double ipc_lbsCH4Year = 0;
	double ipc_lbsN2OYear = 0;
	double[] ipc_firstYearBills = new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0};
	
	
	private void setupParams()
	{
		
		ipc_lbsCO2Year = 0;
		ipc_lbsSO2Year = 0;
		ipc_lbsNOxYear = 0;
		ipc_lbsHgYear = 0;
		ipc_lbsCH4Year = 0;
		ipc_lbsN2OYear = 0;
		ipc_firstYearBills = new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0};
	}


	/**
	 * Things for doing headless calculations.
	 */

	public double getLbsCO2Year()
	{
		return ipc_lbsCO2Year;
	}


	public double getLbsSO2Year()
	{
		return ipc_lbsSO2Year;
	}


	public double getLbsNOxYear()
	{
		return ipc_lbsNOxYear;
	}


	public double getLbsHgYear()
	{
		return ipc_lbsHgYear;
	}


	public double getLbsCH4Year()
	{
		return ipc_lbsCH4Year;
	}


	public double getLbsN2OYear()
	{
		return ipc_lbsN2OYear;
	}


	public double[] getFirstYearBills()
	{
		return ipc_firstYearBills;
	}
	
	public String getCurrentBill()
	{
		return currentBill;
	}
}
