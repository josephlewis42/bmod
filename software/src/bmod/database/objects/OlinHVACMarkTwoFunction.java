package bmod.database.objects;

import java.util.Collection;
import java.util.LinkedList;

import bmod.DataSet;
import bmod.PredictionModel;
import bmod.WattageEvent;
import bmod.database.DBWarningsList;
import bmod.database.DataFeed;
import bmod.database.DataNotAvailableException;
import bmod.database.Database;
import bmod.gui.builder.GUIBuilderPanel;
import bmod.gui.builder.IntWidget;
import bmod.gui.builder.LongWidget;
import bmod.plugin.generic.headless.SmartGridProvider;
import bmod.util.DateTime;
import bmod.util.DateTime.DateTimeRange;


public class OlinHVACMarkTwoFunction extends BuildingFunction<OlinHVACMarkTwoFunction> implements DataFeed
{
	
	public static final double fahrenheitToKelvin(double degrees_f)
	{
		return (degrees_f - 32) * (5.0/9.0) + 273.15;
	}
	
	private static final long DEFAULT_PRIMARY_KEY = -1;
	private final long m_building;
	private final int m_order;
	// Building Variables
	private final double B0 = 0;
	private final double B1 = -0.153549358708992;
	private final double B2 = 8.24970820569186e-05;
	private final double B3 = -6.74327203722313e-09;
	private final double B4 = 0.325069280097628;
	private final double B5 = -0.000536706491262811;
	private final double B6 = 4.33019282961602e-07;
	private final double B7 = -0.115218877952972;
	private final double B8 = -0.000669298048840124;
	private final double B9 = 9.61233059107002e-07;
	private final double B10 = 0.162696444172254;
	private final double B11 = -0.000158546298174965;
	private final double B12 = 1.83079980319893e-07;
	private final double B13 = 0.0761908709579006;
	private final double B14 = -6.12101317317983e-05;
	private final double B15 = -2.64775223089186e-08;
	private final double B16 = -0.0334584637598222;
	private final double B17 = -0.000316176262136607;
	private final double B18 = 3.27752558475579e-07;
	private final double B19 = -0.205026803866394;
	private final double B20 = -5.66391616575977e-05;
	private final double B21 = -1.83697373780892e-08;
	private final double B22 = 0.340629492339922;
	private final double B23 = 9.18497040313122e-05;
	private final double B24 = -5.38781693800114e-08;
	private final double B25 = 0;
	private final double B26 = -0.536630237098264;
	private final double B27 = -0.0234147083273479;
	private final double B28 = 0;
	private final double B29 = -0.246519039074904;
	private final double B30 = 0.0176746434455623;

	private final double DeltaChilledWater = 9;
	private final double CompressorCoolingRegressor1 = 512.7104;
	private final double CompressorCoolingRegressor2 = -4.8813;
	private final double CompressorCoolingRegressor3 = 11.9937;
	private final double CompressorElectricalRegressor1 = 50.5875;
	private final double CompressorElectricalRegressor2 = 2.1173;
	private final double CompressorElectricalRegressor3 = 1.8375;
	private final double WallAngle = 90; // This is a vertical wall
	private final double RoofAngle = 30; // Assumed I have no idea
	private final double EastWallAngle = 90;// Deg away from south Assumed perfectly east
	private final double SouthWallAngle = 0;// Deg away from south Assumed perfectly South
	private final double WestWallAngle = -90;// Deg away from south Assumed perfectly West
	private final double NorthWallAngle = 180;
	
	private static final int OUTSIDE_AIR_TEMP_FEED_ID = 846;
	private static final String[] REPORTED_ZONES = new String[]{"Olin HVAC"};
	private static final String HVAC_TOTAL_STRING = "Olin HVAC Total";
	private static final String HVAC_SUN_STRING = "Olin HVAC Sun";
	private static final String HVAC_ACTIVITIES_TOTAL_STRING = "Olin HVAC Activities All";
	private static final String HVAC_TEMP_STRING = "Olin HVAC Temp";
	private static final String HVAC_FEED_OFFLINE = "Olin HVAC Feed Offline";

	public OlinHVACMarkTwoFunction()
	{
		this(DEFAULT_PRIMARY_KEY, 0, 0);
	}

	public OlinHVACMarkTwoFunction(long building)
	{
		this(Database.getNewPrimaryKey(), building, 0);
	}

	public OlinHVACMarkTwoFunction(long pKey, long building, int order)
	{
		super("OlinHVACRegressionMark1Function",
				new String[]{"PrimaryKey","BuildingID","Order"}, 
				new String[]{"BIGINT","BIGINT","INTEGER"}, 
				new String[]{"BuildingID"}, 
				new Record<?>[]{null, Database.templateBuilding,null},
				pKey, 
				order);

		m_building = building;
		m_order = order;
	}

	@Override
	public BuildingFunction<?> createNewForBuilding(long buildingKey)
	{
		return new OlinHVACMarkTwoFunction(buildingKey).create();
	}

	@Override
	public long getBuildingID()
	{
		return m_building;
	}

	@Override
	protected OlinHVACMarkTwoFunction getThis()
	{
		return this;
	}

	@Override
	protected String getId()
	{
		return "Olin HVAC Mark 1"; // Report as Mark 1 so Jon doesn't have to update his databases
	}

	@Override
	public Object[] toSQL()
	{
		return new Object[]{
				getPrimaryKey(),
				m_building,
				m_order
		};
	}

	@Override
	public void reportIntegrityErrors(DBWarningsList list)
	{		
	}

	@Override
	public OlinHVACMarkTwoFunction fromSQL(Object[] parts) throws IllegalArgumentException
	{
		return new OlinHVACMarkTwoFunction(
				(Long)parts[0], 
				(Long)parts[1], 
				(Integer)parts[2]);
	}

	@Override
	public GUIBuilderPanel getEditor()
	{
		return new GUIBuilderPanel(toSQL(), 
				new LongWidget("PrimaryKey", getPrimaryKey(), false),
				new LongWidget("Building ID", m_building, false),
				new IntWidget("Order", m_order));
	}
	
	
	public static boolean isOnAtTime(DateTime givenTime)
	{
		double OutsideAirTemperature;
		try
		{
			OutsideAirTemperature = getFeedValue(OUTSIDE_AIR_TEMP_FEED_ID, givenTime, null);
		} catch (DataNotAvailableException e)
		{			
			return false;
		}
		return OutsideAirTemperature > 55;
	}

	@Override
	public void addWattageEvents(PredictionModel m, DBWarningsList dw)
	{
		if(m == null || dw == null)
			throw new NullPointerException();
		
		final LinkedList<WattageEvent> events = new LinkedList<WattageEvent>();
		final DateTime[] sdt  = m.getTimeRange().toArray();
		final DataSet[] dataSets = m.getActivityDataSets();
		
		for(DateTime t : sdt)
		{

			try
			{
				final double DiffuseHorizontal = getFeedValue(2840, t, m);
				final double DirectNormal = getFeedValue(2839, t, m);
				final double ZenithDeg = getFeedValue(2841, t, m);
				final double AzimuthDeg = getFeedValue(2842, t, m) - 180;
				final double OutsideAirTemperature = getFeedValue(846, t, m);
				final double OOT = fahrenheitToKelvin(OutsideAirTemperature);

				//final double wattageAtTimeMinusHVAC = m.getTotalWattageAtTimeMinusZone(t, "HVAC");
				final double wattageAtTimeMinusHVAC = m.getTotalWattageAtTime(t);

				double CurrentInsideBuildingElectricalUsage = wattageAtTimeMinusHVAC / 1000;

				// Start of actual Code
				if( isOnAtTime(t) )
				{
					double EastWallRadiation = IncidentSolarOnFlatSurface(EastWallAngle, WallAngle, ZenithDeg, AzimuthDeg, DiffuseHorizontal, DirectNormal); // W/m^2
					double SouthWallRadiation = IncidentSolarOnFlatSurface(SouthWallAngle, WallAngle, ZenithDeg, AzimuthDeg, DiffuseHorizontal, DirectNormal); // W/m^2
					double WestWallRadiation = IncidentSolarOnFlatSurface(WestWallAngle, WallAngle, ZenithDeg, AzimuthDeg, DiffuseHorizontal, DirectNormal); // W/m^2
					double NorthWallRadiation = IncidentSolarOnFlatSurface(NorthWallAngle, WallAngle, ZenithDeg, AzimuthDeg, DiffuseHorizontal, DirectNormal);
					double EastRoofRadiation = IncidentSolarOnFlatSurface(EastWallAngle, RoofAngle, ZenithDeg, AzimuthDeg, DiffuseHorizontal, DirectNormal); // W/m^2
					double SouthRoofRadiation = IncidentSolarOnFlatSurface(SouthWallAngle, RoofAngle, ZenithDeg, AzimuthDeg, DiffuseHorizontal, DirectNormal); // W/m^2
					double WestRoofRadiation = IncidentSolarOnFlatSurface(WestWallAngle, RoofAngle, ZenithDeg, AzimuthDeg, DiffuseHorizontal, DirectNormal); // W/m^2
					double NorthRoofRadiation = IncidentSolarOnFlatSurface(NorthWallAngle, RoofAngle, ZenithDeg, AzimuthDeg, DiffuseHorizontal, DirectNormal);
					double MaxLoadCooling = CompressorCoolingRegressor1 + CompressorCoolingRegressor2 * (OOT - 273.15) + CompressorCoolingRegressor3 * (DeltaChilledWater);
					double MaxLoadElectrical = CompressorElectricalRegressor1 + CompressorElectricalRegressor2 * (OOT - 273.15) + CompressorElectricalRegressor3 * (DeltaChilledWater);
					double CooolingLoadFromSun = B0 + 
							B1 * EastWallRadiation + 
							B2 * Math.pow(EastWallRadiation, 2) + 
							B3 * Math.pow(EastWallRadiation, 3) + 
							B4 * SouthWallRadiation + 
							B5 * Math.pow(SouthWallRadiation ,  2) + 
							B6 * Math.pow(SouthWallRadiation ,  3) + 
							B7 * NorthWallRadiation + 
							B8 * Math.pow(NorthWallRadiation ,  2) + 
							B9 * Math.pow(NorthWallRadiation ,  3) + 
							B10 * WestWallRadiation + 
							B11 * Math.pow(WestWallRadiation ,  2) + 
							B12 * Math.pow(WestWallRadiation ,  3) + 
							B13 * EastRoofRadiation + 
							B14 * Math.pow(EastRoofRadiation ,  2) + 
							B15 * Math.pow(EastRoofRadiation ,  3) + 
							B16 * SouthRoofRadiation + 
							B17 * Math.pow(SouthRoofRadiation ,  2) + 
							B18 * Math.pow(SouthRoofRadiation ,  3) + 
							B19 * WestRoofRadiation + 
							B20 * Math.pow(WestRoofRadiation ,  2) + 
							B21 * Math.pow(WestRoofRadiation ,  3) + 
							B22 * NorthRoofRadiation + 
							B23 * Math.pow(NorthRoofRadiation ,  2) + 
							B24 * Math.pow(NorthRoofRadiation ,  3);
					double CoolingLoadFromTemperature = 
							B25 * (OutsideAirTemperature - 72) + 
							B26 * Math.pow((OutsideAirTemperature - 72) ,  2) + 
							B27 * Math.pow((OutsideAirTemperature - 72) ,  3) + 
							B28 * (OutsideAirTemperature - 55) + 
							B29 * Math.pow((OutsideAirTemperature - 55) ,  2) + 
							B30 * Math.pow((OutsideAirTemperature - 55) ,  3);
					double CoolingLoadFromActivities = CurrentInsideBuildingElectricalUsage;
					
					
					double CoolingLoad = CoolingLoadFromActivities + CoolingLoadFromTemperature + CooolingLoadFromSun;
					
				    double AbsCoolingLoad = Math.max(CoolingLoadFromActivities,0) + Math.max(CoolingLoadFromTemperature,0) + Math.max(CooolingLoadFromSun,0);
					
				    double PercentFromSun = Math.max(CooolingLoadFromSun,0)/AbsCoolingLoad;
				    double PercentFromTemp = Math.max(CoolingLoadFromTemperature,0)/AbsCoolingLoad;
				    double PercentFromAct = Math.max(CoolingLoadFromActivities,0)/AbsCoolingLoad;
					
					double wCompressorElectrical = Math.max(CoolingLoad / MaxLoadCooling * MaxLoadElectrical, 0.0) * 1000;
					
					if(((Double)wCompressorElectrical).isNaN())
						wCompressorElectrical = -0.001;
					
					
					
					
					
					// Add broken down events
					events.add(new WattageEvent(t, wCompressorElectrical * PercentFromSun, this, REPORTED_ZONES, new String[]{HVAC_TOTAL_STRING, HVAC_SUN_STRING}));
					events.add(new WattageEvent(t, wCompressorElectrical * PercentFromTemp, this, REPORTED_ZONES, new String[]{HVAC_TOTAL_STRING, HVAC_TEMP_STRING}));
					
					final double activityWatts = wCompressorElectrical * PercentFromAct;

					for(DataSet ds : dataSets)
						events.add(new WattageEvent(t, activityWatts * (ds.getValue(t) / wattageAtTimeMinusHVAC), this, REPORTED_ZONES, new String[]{HVAC_TOTAL_STRING, HVAC_ACTIVITIES_TOTAL_STRING, "Olin HVAC Activity: " + ds.getTitle()}));
				
				}else{
					events.add(new WattageEvent(t, 0.0, this, new String[]{"HVAC"}, new String[]{"HVAC Regression Mark 1"}));
				}
				m.addNotice(HVAC_FEED_OFFLINE, t, 0.0);

			}catch(DataNotAvailableException ex)
			{
				m.addNotice(HVAC_FEED_OFFLINE, t, 1.0);
				dw.addWarning("Olin HVAC: Couldn't fetch feeds for: "+t.toISODate());
				events.add(new WattageEvent(t, 0.0, this, new String[]{"HVAC"}, new String[]{"HVAC Regression Mark 1"}));
			}
		}	
		if(m != null)
			m.addEvents(events);
	}

	private double IncidentSolarOnFlatSurface(double HorizontalangleI, double VerticalangleI, double ZenithDeg, double AzimuthDeg, double DiffuseHorizontal, double DirectNormal){
		double Azimuth = Math.toRadians(AzimuthDeg);
		double AltitudeAngle = Math.toRadians(90 - ZenithDeg);
		double VerticalAngle = Math.toRadians(VerticalangleI);
		double HorizontalAngle = Math.toRadians(HorizontalangleI);
		double CosAngleOfIncidence = Math.cos(AltitudeAngle)*Math.cos(Azimuth - HorizontalAngle)*Math.sin(VerticalAngle)+Math.sin(AltitudeAngle)*Math.cos(VerticalAngle);
		//double AngleOfIncidence = Math.toDegrees(Math.acos(CosAngleOfIncidence));  //acos is inverse cosine.  I don't know how to do this in java

		if(ZenithDeg >= 89 || ZenithDeg == Double.POSITIVE_INFINITY || ZenithDeg == Double.NEGATIVE_INFINITY)
			return 0;
		
		double DirectIncident = DirectNormal*CosAngleOfIncidence;

		double DiffuseIncident = DiffuseHorizontal*((1 + Math.cos(VerticalAngle))/2);
		return DirectIncident + DiffuseIncident;
	}

	@Override
	public double getDataAtTime(DateTime t) throws DataNotAvailableException
	{
		return (isOnAtTime(t))? 1.0 : 0.0;
	}

	@Override
	public void preCache(DateTimeRange precache)
			throws DataNotAvailableException
	{
		SmartGridProvider.cacheClosestFeedValueRange(precache, OUTSIDE_AIR_TEMP_FEED_ID);
	}

	@Override
	public String getFeedName()
	{
		return "Olin HVAC On?";
	}

	@Override
	public OlinHVACMarkTwoFunction createNew(Collection<Record<?>> filterObjects)
	{
		// Make sure we have enough objects
		if (filterObjects.size() != 1)
		{
			throw new IllegalArgumentException("Cannot instantiate instance without proper arguments.");
		}
		
		// Declare our objects
		Building templateBuilding = null;
		
		// Instantiate all of our declared objects
		for (Record<?> tmp : filterObjects)
		{
			if (tmp instanceof Building)
			{
				templateBuilding = (Building) tmp;
			}
		}
		
		// Make sure we got all of our objects.
		if (templateBuilding == null)
		{
			throw new IllegalArgumentException("Cannot instantiate instance without proper arguments.");
		}
		
		// Return the new item.
		return new OlinHVACMarkTwoFunction(templateBuilding.getPrimaryKey());
	}
}
