package bmod.gui.widgets;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

import bmod.DataSet;
import bmod.util.DateTime;

/**
 * Provides a simple table that will show the outputs of several data sets.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class DataSetTable extends JPanel
{
	private static final long serialVersionUID = 1L;
	private final NTable m_table = new NTable(true, true, true, false);
	
	public DataSetTable(DataSet[] tables, boolean sumCols, boolean sumRows)
	{
		this();
		setTables(tables, sumCols, sumRows);
	}
	
	public DataSetTable()
	{
		setLayout(new BorderLayout());
		add(m_table, BorderLayout.CENTER);
	}
	
	public NTable getUnderlyingNTable()
	{
		return m_table;
	}
	
	public void setTables(DataSet[] tables, 
			boolean sumCols, 
			boolean sumRows)
	{
		setTables(tables, sumCols, sumRows, false);
	}
	
	public void setTables(DataSet[] tables, 
							boolean sumCols, 
							boolean sumRows,
							boolean militaryTime)
	{
		// Get all DateTimes
		Set<DateTime> times = new HashSet<DateTime>();
		
		for(DataSet tmp : tables)
			times.addAll(tmp.keySet());
		
		int rows = times.size();
		if(sumRows)
		{
			rows++;
		}
		
		int cols = countCols(tables, sumCols,militaryTime);
		
		// row, column
		String[] header = generateHeader(tables, sumCols, sumRows, militaryTime);
		
		
		String[][] data = new String[rows][cols];
		
		DateTime[] dates = times.toArray(new DateTime[times.size()]);
		Arrays.sort(dates);
		
		for(int r = 0; r < dates.length; r++)
		{
			data[r] = generateRow(dates[r], tables, sumCols, militaryTime);
			/**int pos = 0;
			// Do Timestamp/Datestamp
			if(militaryTime)
			{
				
			}
			else
			{
				
			}
			
			// put in the datestamp
			data[r][0] = dates[r].toISODate();
			
			double colsum = 0;
			
			for(int c = 0; c < tables.length; c++)
			{
				Double colval = tables[c].getValue(dates[r]);
				
				if(colval.isNaN())
					colval = 0.0;
				
				colsum += colval;
				data[r][c + 1] = colval + "";
			}
			
			if(sumCols)
				data[r][cols - 1] = colsum + "";
			**/
		}
		
		if(sumRows)
		{
			data[rows - 1] = generateSumRow(tables, sumCols, militaryTime);
			/**data[rows - 1][0] = "Sum";
			for(int c = 0; c < tables.length; c++)
			{
				data[rows - 1][c + 1] = tables[c].getSum() + "";
			}**/
		}
		
		m_table.setTable(header, data);
	}
	
	/**
	 * Counts the number of columns the header and body will make up.
	 * @param tables - the tables to use.
	 * @param sumCols - whether or not we're summing the tables.
	 * @param militaryTime - whether or not we're doing military time.
	 * @return - the column count.
	 */
	private int countCols(DataSet[] tables, boolean sumCols, boolean militaryTime)
	{
		int numCols = tables.length;
		numCols += (sumCols)? 1 : 0;
		numCols += (militaryTime)? 2 : 1;
		
		return numCols;
	}
	
	
	private String[] generateHeader(DataSet[] tables, 
			boolean sumCols, 
			boolean sumRows,
			boolean militaryTime)
	{
		int length = countCols(tables, sumCols, militaryTime);
		String[] header = new String[length];
		int counter = 0;
		
		
		if(militaryTime)
		{
			header[counter] = "Date";
			counter++;
			header[counter] = "Time";
			counter++;
		}
		else
		{
			header[counter] = "Timestamp";
			counter++;
		}
		
		for(int i = 0; i < tables.length; i++)
		{
			header[counter] = tables[i].getTitle();
			counter++;
		}
		
		if(sumCols)
		{
			header[counter] = "Sum";
			counter++;
		}
		
		return header;
	}
	
	
	private String[] generateRow(DateTime time, DataSet[] tables, boolean sumCols, boolean militaryTime)
	{
		int length = countCols(tables, sumCols, militaryTime);
		String[] row = new String[length];
		int counter = 0;
		
		
		// Do the time
		if(militaryTime)
		{
			row[counter] = time.toISODay();
			counter++;
			row[counter] = time.toFractionHours();
			counter++;
		}
		else
		{
			row[counter] = time.toISODate();
			counter++;
		}
		
		// Do each table.
		double sum = 0;
		for(int i = 0; i < tables.length; i++)
		{
			double value = tables[i].getValue(time);
			row[counter] = "" + value;
			
			sum += value;
			
			counter++;
		}
		
		if(sumCols)
		{
			row[counter] = "" + sum;
			counter++;
		}
		
		return row;
	}
	
	private String[] generateSumRow(DataSet[] tables, boolean sumCols, boolean militaryTime)
	{
		int length = countCols(tables, sumCols, militaryTime);
		String[] row = new String[length];
		int counter = 0;
		
		
		// Do the time
		if(militaryTime)
		{
			row[counter] = "";
			counter++;
			row[counter] = "Sum";
			counter++;
		}
		else
		{
			row[counter] = "Sum";
			counter++;
		}
		
		// Do each table.
		double sum = 0;
		for(int i = 0; i < tables.length; i++)
		{
			double value = tables[i].getSum();
			row[counter] = "" + value;
			
			sum += value;
			
			counter++;
		}
		
		if(sumCols)
		{
			row[counter] = "" + sum;
			counter++;
		}
		
		return row;
	}
}
