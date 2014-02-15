package bmod.gui.widgets;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class DecimalSpinner extends JSpinner
{
	private static final long serialVersionUID = -3648526813363713316L;

	public DecimalSpinner(double val, double min, double max, double step, int places)
	{
		SpinnerNumberModel model = new SpinnerNumberModel(val, min, max, step);  
		setModel(model);
		
		JSpinner.NumberEditor editor = (JSpinner.NumberEditor)getEditor();  
        editor.getFormat().setMinimumFractionDigits(places);
	}
	
	public double getDouble()
	{
		return (Double) super.getValue();
	}
}
