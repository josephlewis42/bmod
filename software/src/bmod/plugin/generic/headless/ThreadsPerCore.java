package bmod.plugin.generic.headless;

import bmod.GenericPlugin;

/**
 * Sets maxThreads to something sane if < 0.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class ThreadsPerCore extends GenericPlugin
{
	
	public ThreadsPerCore()
	{
		super("Thread Chooser", 
				"Decides to put your whole computer to work when running a " +
				"simulation to make it go faster. If your computer is too slow" +
				"while simulating you may wish to disable this plugin.");
	}

	@Override
	public int maxThreadsHook(int maxThreads)
	{
		if(maxThreads <= 0)
			return Runtime.getRuntime().availableProcessors();
		
		return maxThreads;
	}

	@Override
	public void setupHeadless()
	{		
	}

	@Override
	public void teardown()
	{		
	}
	
}
