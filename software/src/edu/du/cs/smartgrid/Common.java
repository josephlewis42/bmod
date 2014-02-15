package edu.du.cs.smartgrid;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;

/**
 * Common methods and variables for the smartgrid package.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class Common
{
	public static final String TEST_HOST = "testsmartgrid.cs.du.edu";
	public static final String DEFAULT_HOST = "smartgrid.cs.du.edu";
	public static final String ENCODING = "UTF-8";
	public static boolean DEBUGGING = false;
	public static final int TIMEOUT_SECS = 30;
	public static final int TIMEOUT_MS = TIMEOUT_SECS * 1000;
	
	
	/**
	 * Parses an YYYY-MM-DDTHH:MM:SSZ type datetime.
	 * @param timestamp
	 * @return
	 */
	public static Date _parseISOTime(String timestamp)
	{
		if(timestamp.charAt(10) == ' ')
			timestamp = timestamp.replace(' ', 'T');
		
		long msToAdd = 0;
		if(timestamp.length() > 20)
		{
			// we have extended timestamp, catch the +/- and everything after
			boolean plus = timestamp.charAt(19) == '+';
			
			String[] hhmm = timestamp.substring(20).split(":");
			
			timestamp = timestamp.substring(0, 19) + "Z";
			
			msToAdd += Integer.parseInt(hhmm[0]) * 3600;
			
			if(hhmm.length > 1)
				msToAdd += Integer.parseInt(hhmm[1]) * 60;
			
			msToAdd *= 1000;
			if(! plus)
				msToAdd = -msToAdd;
			
		}
		
		Date isotime = javax.xml.bind.DatatypeConverter.parseDateTime(timestamp).getTime();
		
		return new Date(isotime.getTime() + msToAdd);
	}
	
	/**
	 * Converts a date to ISO time.
	 * 
	 * @param time - the date to convert
	 * @return A string representing the ISO time of the date.
	 */
	public static String _toISOTime(Date time)
	{
		DateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		return iso.format(time);
	}
	
	
	/**
	 * Encodes a portion of a URL.
	 * 
	 * @param input
	 * @return
	 */
	public static String urlEncode(String input)
	{
		try
		{
			return URLEncoder.encode(input, ENCODING).replace("+", "%20");
		} catch (UnsupportedEncodingException e)
		{
			return input;
		}
	}
	
	

	/**
	 * Fetches the given url, returns a blank string on an error.
	 * 
	 * @param url
	 * @return
	 * @throws IOException 
	 */
	public static String fetch_url(String url) throws IOException
	{
		if(DEBUGGING)
			System.out.println("GET << " + url);
		
    	try
		{
			String output = Request.Get(url)
				.connectTimeout(TIMEOUT_SECS * 1000)
				.socketTimeout(TIMEOUT_SECS * 1000)
				.execute().returnContent().asString();
			
			if(DEBUGGING)
				System.out.println(output.replace("\n", "\nGOT >> "));
			
			return output;
		} catch (ClientProtocolException e)
		{
			return "";
		}
	}
}
