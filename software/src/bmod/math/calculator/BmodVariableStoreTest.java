package bmod.math.calculator;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import bmod.database.Database;
import bmod.database.objects.DeviceType;
import bmod.database.objects.DeviceTypeVariable;

public class BmodVariableStoreTest
{

	@Test
	public void testWatts() throws MalformedExpression
	{
		DeviceType dt = new DeviceType("TEST_PROC_KEY", "100","",1.0,"0", Database.getNewPrimaryKey()).create();
		BmodVariableStore bvs = new BmodVariableStore();

		assertTrue(Eval.eval("$.watts('TEST_PROC_KEY')", bvs) == 100.0);

		dt.delete();
	}

	@Test
	public void testVar() throws MalformedExpression
	{
		DeviceTypeVariable dtv = new DeviceTypeVariable("TEST_PROC_KEY", 10.0).create();
		BmodVariableStore bvs = new BmodVariableStore();
		
		assertTrue(Eval.eval("$.var('TEST_PROC_KEY', 4)", bvs) == 10.0);
		assertTrue(Eval.eval("$.var('TEST_PROC_KEY_NONE', 4)", bvs) == 4.0);

		dtv.delete();
		
	}
	
	/**
	@Test
	public void testCalculatorSimilarity()
	{
		BmodVariableStore bvs = new BmodVariableStore();

		//assertTrue(Calculator.getWatts("3 * 5") == Eval.eval("3 * 5"));
		
		for(DeviceType dt : Database.templateDeviceType.readAll())
		{
			String expr = dt.getWattsExpr();
			try
			{
				double calculator = Calculator.getWatts(expr);
				double eval = Eval.eval(expr, bvs);
				
				if(eval != calculator)
					System.err.println(expr + " " + calculator + " " + eval);
				
				assertTrue(eval == calculator);
			}catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
	}**/

}
