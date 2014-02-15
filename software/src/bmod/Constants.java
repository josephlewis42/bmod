package bmod;

/**
 * Contains constants for the whole BMOD project, as well as for all of the 
 * plugins.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 *
 */
public class Constants
{
	public static final String FETCH_API_KEY = "xxx-xxx-xxx";
	public static final String POST_API_KEY = "xxx-xxx-xxx";
	public static final String API_HOST = "smartgrid.domain.com";
	public static final int HTTP_CONNECT_TIMEOUT_MS = 30000;
	public static final int HTTP_SOCKET_TIMEOUT_MS = 15000;
			
	
	public static final String SOFTWARE_NAME = "Bmod";
	public static final String _PROJECT_DIRECTORY_NAME = "building_modeler";
	
	
	// LAT and LONG are for the University of Denver
	// Used as the default latitude for anything that hasn't specified another.
	public static final double DEFAULT_LATITUDE = 39.678345;
	// Used as the default longitude for anything that hasn't specified another.
	public static final double DEFAULT_LONGITUDE = -104.961448;

	public static final String DEFAULT_TIME_ZONE = "America/Denver";
}
