package bmod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class WattageEventCollection implements Iterable<WattageEvent>
{
	private static final class WattageEventList extends LinkedList<WattageEvent>
	{
		private static final long serialVersionUID = 5257009505439113587L;
		public WattageEventList()
		{
		}
		
		public WattageEventList(WattageEventList other)
		{
			super(other);
		}
		
	}
	
	private final Kryo m_kryo = new Kryo();
	private static final int FILE_SIZE_BREAKPOINT = 20_000;
	private final LinkedList<File> m_pages = new LinkedList<File>();
	private int currentIndex = 0;
	private int m_evtCount = 0;
	
	private final WattageEventList m_events = new WattageEventList();
	
	public void addEvent(WattageEvent e)
	{
		m_evtCount++;
		if(m_events.size() == FILE_SIZE_BREAKPOINT)
		{
			writeToDisk();
		}
		
		m_events.add(e);
	}

	/**
	 * Stores the current elements to disk.
	 */
	private void writeToDisk()
	{
		try
		{
			File nextPageName = getNextPage();
			Output output = new Output(new FileOutputStream(nextPageName));
			m_kryo.writeObject(output, m_events);
			output.close();
			
			m_events.clear();
			
			m_pages.add(nextPageName);
		}catch(Exception ex)
		{
			
		}
	}
	
	private WattageEventList readFromDisk(File page)
	{
		try
		{
			Input input = new Input(new FileInputStream(page));
			WattageEventList someObject = m_kryo.readObject(input, WattageEventList.class);
			input.close();
			
			return someObject;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return new WattageEventList();


	}
	
	private File getNextPage()
	{
		currentIndex++;
		return new File(getPageName(currentIndex - 1));
	}
	
	private String getPageName(int pageId)
	{
		return "output_" + pageId + ".page";
	}

	@Override
	public Iterator<WattageEvent> iterator()
	{
		return new Iterator<WattageEvent>(){
			
			int currPage = 0;
			WattageEventList currEvents = new WattageEventList(m_events);

			@Override
			public boolean hasNext()
			{
				if(currEvents.size() > 0)
				{
					return true;
				}
				
				if(currPage < m_pages.size())
				{
					return true;
				}
				
				return false;
			}

			@Override
			public WattageEvent next()
			{
				if(currEvents.size() > 0)
				{
					return currEvents.pop();
				}
				
				if(currPage < m_pages.size())
				{
					currEvents = readFromDisk(m_pages.get(currPage));
					currPage++;
					return next();
				}
				
				return null;
			}

			@Override
			public void remove()
			{				
			}
			
		};
	}

	public void addAll(Collection<WattageEvent> e)
	{
		for(WattageEvent evt : e)
		{
			addEvent(evt);
		}
	}

	public int size()
	{
		return m_evtCount;
	}
}
