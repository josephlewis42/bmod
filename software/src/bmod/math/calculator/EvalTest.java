package bmod.math.calculator;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EvalTest
{

	@Test
	public void testBasic()
	{
		assertTrue(Eval.fetchVariables("").length == 0);
		assertTrue(Eval.fetchVariables("$").length == 1);
	}
	
	@Test
	public void testMultiVar()
	{
		assertTrue(Eval.fetchVariables("$a $b").length == 2);
		assertTrue(Eval.fetchVariables("$a $b")[0].equals("a"));
		assertTrue(Eval.fetchVariables("$a $b")[1].equals("b"));
	}
	
	@Test
	public void testQuotes()
	{
		assertTrue(Eval.fetchVariables("$\"a\"").length == 1);
		assertTrue(Eval.fetchVariables("$(a)")[0].equals("(a)"));
		assertTrue(Eval.fetchVariables("$(b) $(a)")[0].equals("(b)"));
		assertTrue(Eval.fetchVariables("$(b o y) $(a)")[0].equals("(b o y)"));
	}
	
	@Test
	public void testNesting()
	{
		assertTrue(Eval.fetchVariables("$(b o (y)) $(a)")[0].equals("(b o (y))"));
		assertTrue(Eval.fetchVariables("$(b \"\" (y)) $(a)")[0].equals("(b \"\" (y))"));
	}
	
	@Test
	public void testTokenizeSimple()
	{
		try
		{
			assertTrue(Eval.tokenize("").size() == 0);
			assertTrue(Eval.tokenize("+").size() == 1);
			assertTrue(Eval.tokenize("1+2").size() == 3);
			assertTrue(Eval.tokenize("1+2-").size() == 4);
			assertTrue(Eval.tokenize("1 + 2 -").size() == 4);

			assertTrue(Eval.tokenize("1.0001").size() == 1);
			assertTrue(Eval.tokenize("1.00 01").size() == 2);

		
		} catch (MalformedExpression e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void testNumber()
	{
		try
		{
			assertTrue(Eval.evalNumber(Eval.tokenize("1")) == 1.0);
			assertTrue(Eval.evalNumber(Eval.tokenize("-1")) == -1.0);
			assertTrue(Eval.evalNumber(Eval.tokenize("-1.01")) == -1.01);


			
		}catch (MalformedExpression e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			assertTrue(false);
		}
	}

	@Test
	public void testFactor()
	{
		try
		{
			assertTrue(Eval.evalFactor(Eval.tokenize("1")) == 1.0);
			assertTrue(Eval.evalFactor(Eval.tokenize("(-1)")) == -1.0);
			assertTrue(Eval.evalFactor(Eval.tokenize("(-1.01)")) == -1.01);
		}catch (MalformedExpression e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void testTerm()
	{
		try
		{
			assertTrue(Eval.evalTerm(Eval.tokenize("1")) == 1.0);
			assertTrue(Eval.evalTerm(Eval.tokenize("1/1")) == 1.0);
			assertTrue(Eval.evalTerm(Eval.tokenize("1/-1")) == -1.0);
			assertTrue(Eval.evalTerm(Eval.tokenize("1*12")) == 12.0);
			assertTrue(Eval.evalTerm(Eval.tokenize("1*.01")) == .01);
			assertTrue(Eval.evalTerm(Eval.tokenize("1 * 10 / 10")) == 1);
		}catch (MalformedExpression e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	
	@Test
	public void testExpression()
	{
		try
		{
			assertTrue(Eval.evalExpression(Eval.tokenize("1")) == 1.0);
			assertTrue(Eval.evalExpression(Eval.tokenize("1-1")) == 0);
			assertTrue(Eval.evalExpression(Eval.tokenize("1+-1")) == 0.0);
			assertTrue(Eval.evalExpression(Eval.tokenize("1+1")) == 2.0);
			assertTrue(Eval.evalExpression(Eval.tokenize("1+.01")) == 1.01);
			assertTrue(Eval.evalExpression(Eval.tokenize("1 + 3 - 3")) == 1);
			
			assertTrue(Eval.evalExpression(Eval.tokenize("3 + 4 * 2")) == 11);
			assertTrue(Eval.evalExpression(Eval.tokenize("(3 + 4) * 2")) == 14);
		}catch (MalformedExpression e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	
	@Test
	public void testEval()
	{
		try
		{
			assertTrue(Eval.eval("1") == 1.0);
			assertTrue(Eval.eval("1-1") == 0);
			assertTrue(Eval.eval("1+-1") == 0.0);
			assertTrue(Eval.eval("1+1") == 2.0);
			assertTrue(Eval.eval("1+.01") == 1.01);
			assertTrue(Eval.eval("1 + 3 - 3") == 1);
			
			assertTrue(Eval.eval("3 + 4 * 2") == 11);
			assertTrue(Eval.eval("(3 + 4) * 2") == 14);
		}catch (MalformedExpression e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	// always returns 3
	private class TestVariableStore implements VariableStore
	{
		@Override
		public double getValue(String variable) throws MalformedExpression
		{
			return 3;
		}
	}
	
	@Test
	public void testEvalVs()
	{
		TestVariableStore tvs = new TestVariableStore();
		
		try
		{
			assertTrue(Eval.eval("$a", tvs) == 3.0);
			assertTrue(Eval.eval("1 + $a - $b", tvs) == 1);
			
			assertTrue(Eval.eval("$a + 4 * 2", tvs) == 11);
			assertTrue(Eval.eval("($a + 4) * 2", tvs) == 14);
		}catch (MalformedExpression e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			assertTrue(false);
		}
	}
		
}
