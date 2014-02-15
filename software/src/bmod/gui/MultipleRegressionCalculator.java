package bmod.gui;

import java.util.Arrays;

import Jama.Matrix;
import bmod.database.DBWarningsList;

public class MultipleRegressionCalculator
{
	
	private final DBWarningsList m_warnings = new DBWarningsList();
	
	private final String m_output;
	
	/**
	 * Gets the HTML formatted output of the calculator.
	 * @return
	 */
	public String getOutput()
	{
		return m_output;
	}

	
	public MultipleRegressionCalculator(double[][] InputArray)
	{
		String[] output_names = new String[InputArray[0].length];
		for(int i = 0; i < output_names.length; i++)
			output_names[i] = "Regressor " + i;
		output_names[output_names.length - 1] = "Regressand";
		
		
		twoDArrayToString(InputArray);
		
		
		final int N = InputArray.length; // Sample Size
		final int p = InputArray[0].length - 1; // Number of regressors
		final int totalwidth = InputArray[0].length; // Width of Storage
														// Matricies
		final int yPos = InputArray[0].length - 1;

		final double[][] Xarr = new double[N][totalwidth];
		final double[][] Yarr = new double[N][1];
		// Populate X and Y Arrays
		for (int i = 0; i < N; i++)
		{
			Yarr[i][0] = InputArray[i][yPos];
			for (int j = 0; j < totalwidth; j++)
				if (j == 0)
					Xarr[i][j] = 1;
				else
					Xarr[i][j] = InputArray[i][j - 1];
		}
		// Calculate Regression Coefficients
		final double[][] Regcoefficcients = extractRegCoefficcients(Xarr, Yarr);

		final double[] Mean = calculateMeans(N, totalwidth, yPos, Xarr, Yarr);

		// Calculate Standard Deviation
		final double[] Stddev = calculateStandardDeviation(N, totalwidth, yPos,
				Xarr, Yarr, Mean);

		// Calculate Correlation Coefficients
		final double[] CorrelationCoeff = new double[p];
		for (int j = 0; j < p; j++)
		{
			double Temp = 0;
			for (int i = 0; i < N; i++)
				Temp = (Xarr[i][j + 1] - Mean[j]) * (Yarr[i][0] - Mean[yPos])
						+ Temp;
			CorrelationCoeff[j] = Temp / (N - 1) / (Stddev[j] * Stddev[yPos]);
		}
		final double[][] CorrelationCoeffReg = new double[p][p];
		for (int j = 0; j < p; j++)
			for (int k = 0; k < p; k++)
				if (k != j)
				{
					double Temp = 0;
					for (int i = 0; i < N; i++)
						Temp = (Xarr[i][j + 1] - Mean[j])
								* (Xarr[i][k + 1] - Mean[k]) + Temp;

					CorrelationCoeffReg[j][k] = Temp / (N - 1)
							/ (Stddev[j] * Stddev[k]);
				} else
					CorrelationCoeffReg[j][k] = 1;
		
		// Print2dimArray(CorrelationCoeffReg);
		// Calculate Y-Yhat Y is actual value Yhat is predicted values
		final double YminYhat[] = new double[N];
		for (int i = 0; i < N; i++)
		{
			double Temp = Yarr[i][0] - Regcoefficcients[0][0];
			for (int j = 0; j < Xarr[1].length - 1; j++)
				Temp = Temp - Regcoefficcients[j + 1][0] * Xarr[i][j + 1];
			YminYhat[i] = Temp;
			Temp = 0;
		}
		// Calculate RSS
		double RSS = 0;
		for (int i = 0; i < N; i++)
			RSS = RSS + Math.pow(YminYhat[i], 2);
		// Calculate VminVbar can be X or Y depending on location V = actual
		// value V bar = Mean value
		final double VminVbar[][] = new double[N][totalwidth];
		for (int i = 0; i < N; i++)
		{

			double Temp = Regcoefficcients[0][0];
			for (int j = 0; j < totalwidth; j++)
				if (j == yPos)
					VminVbar[i][j] = Temp - Mean[yPos];// Yarr[i][0] -
														// Mean[yPos];
				else
				{
					Temp = Temp + Regcoefficcients[j + 1][0] * Xarr[i][j + 1];
					VminVbar[i][j] = Xarr[i][j + 1] - Mean[j];
				}
			Temp = 0;
		}
		// Calculate ESS
		double ESS = 0;
		for (int i = 0; i < N; i++)
			ESS = ESS + Math.pow(VminVbar[i][yPos], 2);
		// Calculate TSS
		double TSS = 0;
		TSS = ESS + RSS;
		// R2 calculations
		double R2 = 0;
		double R = 0;
		double AdjustedR2 = 0;
		double StdErrorofEstimate = 0;
		R2 = ESS / TSS;
		R = Math.sqrt(R2);
		AdjustedR2 = 1 - (1 - R2) * (N - 1) / (N - (p - 1) - 1);
		StdErrorofEstimate = Math.sqrt(RSS / (N - (p - 1) - 1));
		// ANOVA Calculations
		final double MSRegression = ESS / p;
		final double MSResidual = RSS / (N - p - 1);
		final double Fvalue = MSRegression / MSResidual; // MSregression /
															// MSresidual

		// Standard Error Calulcations
		// Calculate Avg ((Y-Ybar)^2)
		double EstimatedYMinYbar2 = 0;
		double Temp = 0;
		for (int i = 0; i < N; i++)
			Temp = Math.pow(VminVbar[i][yPos], 2) + Temp;
		EstimatedYMinYbar2 = Temp;
		Temp = 0;
		final double[] EstimatedXminXBarTimYMinYbar2 = new double[p];
		for (int j = 0; j < p; j++)
		{
			for (int i = 0; i < N; i++)
				Temp = Math.pow(VminVbar[i][j] * VminVbar[i][yPos], 2) + Temp;
			EstimatedXminXBarTimYMinYbar2[j] = Temp;
			Temp = 0;
		}
		final double[] EstimatedXminXBar2 = new double[p];
		for (int j = 0; j < p; j++)
		{
			for (int i = 0; i < N; i++)
				Temp = Math.pow(VminVbar[i][j], 2) + Temp;
			EstimatedXminXBar2[j] = Temp;
			Temp = 0;
		}
		final double[] StdErr = new double[p];
		for (int j = 0; j < p; j++)
			StdErr[j] = Math
					.sqrt((EstimatedYMinYbar2 - EstimatedXminXBarTimYMinYbar2[j]
							/ EstimatedXminXBar2[j])
							/ (N - 2));

		StringBuilder sb = new StringBuilder();
		sb.append("<pre>");
		sb.append(Arrays.toString(output_names) + "\n");
		
		sb.append("Mean\t");
		sb.append(Arrays.toString(Mean));
		sb.append("\n");
		
		sb.append(Arrays.toString(output_names) + "\n");
		
		sb.append("Standard Deviation\t");
		sb.append(Arrays.toString(Stddev));
		sb.append("\n");
		
		sb.append("Regression Coefficients");
		sb.append("	");
		sb.append(twoDArrayToString(Regcoefficcients));
		sb.append("\n");
		sb.append("\n");
		sb.append("Correlation Coefficients");
		sb.append("	");
		sb.append("\n");
		sb.append(Arrays.toString(CorrelationCoeff));
		sb.append("\n");
		sb.append("Multiple Correlation Coefficients");
		sb.append("	");
		sb.append("\n");
		sb.append(twoDArrayToString(CorrelationCoeffReg));
		sb.append("\n");
		sb.append("Explained Sum of the Squares");
		sb.append("\n");
		sb.append(ESS);
		sb.append("\n");
		sb.append("\n");
		sb.append("Residual Sum of the Squares");
		sb.append("\n");
		sb.append(RSS);
		sb.append("\n");
		sb.append("\n");
		sb.append("Total Sum of the Squares");
		sb.append("\n");
		sb.append(TSS);
		sb.append("\n");
		sb.append("\n");
		sb.append("Mean Square Regression");
		sb.append("\n");
		sb.append(MSRegression);
		sb.append("\n");
		sb.append("\n");
		sb.append("Mean Square Residual");
		sb.append("\n");
		sb.append(MSResidual);
		sb.append("\n");
		sb.append("\n");
		sb.append("F Value");
		sb.append("\n");
		sb.append(Fvalue);
		sb.append("\n");
		sb.append("\n");
		sb.append("R			R2			Adjusted R2		Std Error of Estimate");
		sb.append("\n");
		sb.append(R);
		sb.append("	");
		sb.append(R2);
		sb.append("	");
		sb.append(AdjustedR2);
		sb.append("	");
		sb.append(StdErrorofEstimate);
		if(!m_warnings.toString().equals(""))
		{
			sb.append("</pre>");
			sb.append("<h2>Errors/h2><pre>");
			sb.append(m_warnings.toString());
		}
		//sb.append("</pre><h2>Inputs</h2><pre>");
		//sb.append(Arrays.toString(output_names));
		//sb.append("\n");
		//sb.append(twoDArrayToString(InputArray));
		sb.append("</pre>");
		
		m_output = sb.toString();

	}
	
	
	public static double[][] transposeMatrix(double[][] input)
	{
		final int lengthOrigX = input[0].length;
		final int lengthOrigY = input.length;
		
		double[][] transposed = new double[lengthOrigX][];
		
		for(int i = 0; i < transposed.length; ++i)
			transposed[i] = new double[lengthOrigY];
		
		
		
		for(int x = 0; x < lengthOrigX; ++x)
			for(int y = 0; y < lengthOrigY; ++y)
				transposed[x][y] = input[y][x];
		
		return transposed;
	}
	
	
	/**
	 * @param N
	 * @param totalwidth
	 * @param yPos
	 * @param Xarr
	 * @param Yarr
	 * @param Mean
	 * @return
	 */
	private static double[] calculateStandardDeviation(final int N,
			final int totalwidth, final int yPos, final double[][] Xarr,
			final double[][] Yarr, final double[] Mean)
	{
		final double[] Stddev = new double[totalwidth];
		// double Temp = 0;
		for (int j = 0; j < totalwidth; j++)
		{
			double Temp = 0;
			for (int i = 0; i < N; i++)
				if (j == yPos)
					Temp = Temp + Math.pow(Yarr[i][0] - Mean[yPos], 2.);
				else
					Temp = Temp + Math.pow(Xarr[i][j + 1] - Mean[j], 2.);
			Stddev[j] = Math.sqrt(Temp / (N - 1));
		}
		return Stddev;
	}

	/**
	 * @param N
	 * @param totalwidth
	 * @param yPos
	 * @param Xarr
	 * @param Yarr
	 * @return
	 */
	private static double[] calculateMeans(final int N, final int totalwidth,
			final int yPos, final double[][] Xarr, final double[][] Yarr)
	{
		// Calculate Means
		final double[] Sum = new double[totalwidth];
		final double[] Mean = new double[totalwidth];
		for (int j = 0; j < totalwidth; j++)
			for (int i = 0; i < N; i++)
				if (j == yPos)
					Sum[j] = Sum[j] + Yarr[i][0];
				else
					Sum[j] = Xarr[i][j + 1] + Sum[j];
		// Calculate Means
		for (int j = 0; j < totalwidth; j++)
			Mean[j] = Sum[j] / N;
		return Mean;
	}

	/**
	 * @param Xarr
	 * @param Yarr
	 * @return
	 */
	private static double[][] extractRegCoefficcients(final double[][] Xarr,
			final double[][] Yarr)
	{
		final Matrix X = new Matrix(Xarr);
		final Matrix Y = new Matrix(Yarr);
		final Matrix RegCoeff = X.transpose().times(X).inverse().times(X.transpose()).times(Y);
		final double[][] Regcoefficcients = RegCoeff.getArray();
		return Regcoefficcients;
	}

	public static String twoDArrayToString(final double[][] value)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < value.length; i++)
		{
			sb.append( Arrays.toString(value[i]));
			sb.append("\n");
		}
		return sb.toString();
	}
}
