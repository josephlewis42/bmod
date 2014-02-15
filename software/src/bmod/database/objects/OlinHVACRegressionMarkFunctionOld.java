package bmod.database.objects;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

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


public class OlinHVACRegressionMarkFunctionOld extends BuildingFunction<OlinHVACRegressionMarkFunctionOld> implements DataFeed
{
	
	public static final double fahrenheitToKelvin(double degrees_f)
	{
		return (degrees_f - 32) * (5.0/9.0) + 273.15;
	}
	
	private static final long DEFAULT_PRIMARY_KEY = -1;
	private final long m_building;
	private final int m_order;

	public OlinHVACRegressionMarkFunctionOld()
	{
		this(DEFAULT_PRIMARY_KEY, 0, 0);
	}

	public OlinHVACRegressionMarkFunctionOld(long building)
	{
		this(Database.getNewPrimaryKey(), building, 0);
	}

	public OlinHVACRegressionMarkFunctionOld(long pKey, long building, int order)
	{
		super("OlinHVACRegressionMark1FunctionOld",
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
		return new OlinHVACRegressionMarkFunctionOld(buildingKey).create();
	}

	@Override
	public long getBuildingID()
	{
		return m_building;
	}

	@Override
	protected OlinHVACRegressionMarkFunctionOld getThis()
	{
		return this;
	}

	@Override
	protected String getId()
	{
		return "Olin HVAC Mark 1 Old";
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
	public OlinHVACRegressionMarkFunctionOld fromSQL(Object[] parts) throws IllegalArgumentException
	{
		return new OlinHVACRegressionMarkFunctionOld(
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

	
	public static boolean isOnAtTime(DateTime givenTime)
	{
		double OutsideAirTemperature;
		try
		{
			OutsideAirTemperature = getFeedValue(OUTSIDE_AIR_TEMP_FEED_ID, givenTime, null);
		} 
		catch (DataNotAvailableException e)
		{
			System.out.println("HVAC is off at time: " + givenTime);
			return false;
		}
		return OutsideAirTemperature > 55;
	}

	@Override
	public void addWattageEvents(PredictionModel m, DBWarningsList dw)
	{
		LinkedList<WattageEvent> events = new LinkedList<WattageEvent>();
		Set<DateTime> sdt;
		if(m != null)
		{
			sdt = m.getTimeRange().toSet();
		}
		else
		{
			sdt = new HashSet<DateTime>();
			sdt.add(new DateTime("2012-08-05 05:00:00"));
		}
		
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

				//TODO fix this to not take the cooling pump
				double CurrentInsideBuildingElectricalUsage;
				if(m == null)
					CurrentInsideBuildingElectricalUsage = 60;  // For now, just take everything in Olin but the compressor and the cooling pump which is based on the compressor and add it together at time t.
				else
					CurrentInsideBuildingElectricalUsage = m.getTotalWattageAtTime(t) / 1000;

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
					double CoolingLoad = B0 + 
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
							B24 * Math.pow(NorthRoofRadiation ,  3) + 
							B25 * (OutsideAirTemperature - 72) + 
							B26 * Math.pow((OutsideAirTemperature - 72) ,  2) + 
							B27 * Math.pow((OutsideAirTemperature - 72) ,  3) + 
							B28 * (OutsideAirTemperature - 55) + 
							B29 * Math.pow((OutsideAirTemperature - 55) ,  2) + 
							B30 * Math.pow((OutsideAirTemperature - 55) ,  3) + 
							CurrentInsideBuildingElectricalUsage;
					System.err.println("Cooling: " + CoolingLoad);
					
					double kWCompressorElectrical = Math.max(CoolingLoad / MaxLoadCooling * MaxLoadElectrical, 0.0);
					kWCompressorElectrical = kWCompressorElectrical * 1000;
					if(((Double)kWCompressorElectrical).isNaN())
						kWCompressorElectrical = -0.001;
					events.add(new WattageEvent(t, kWCompressorElectrical, this, new String[]{"HVAC"}, new String[]{"HVAC Regression Mark 1"}));
					if(m == null)
					{
						System.err.println("OutsideAirTemperature: " + OutsideAirTemperature);
						System.err.println("kW: " + kWCompressorElectrical);
					}
				}
				else
				{
					events.add(new WattageEvent(t, 0.0, this, new String[]{"HVAC"}, new String[]{"HVAC Regression Mark 1"}));
				}
			}catch(DataNotAvailableException ex)
			{
				dw.addWarning("GeneralHVAC: Couldn't fetch feeds for the given time.");
				events.add(new WattageEvent(t, 0.0, this, new String[]{"HVAC"}, new String[]{"HVAC Regression Mark 1"}));
			}

		}	
		if(m != null)
			m.addEvents(events);
	}

	private double IncidentSolarOnFlatSurface(double HorizontalangleI, double VerticalangleI, double ZenithDeg, double AzimuthDeg, double DiffuseHorizontal, double DirectNormal){
		double Azimuth = Deg2Rad(AzimuthDeg);
		double AltitudeAngle = Deg2Rad(90 - ZenithDeg);
		double VerticalAngle = Deg2Rad(VerticalangleI);
		double HorizontalAngle = Deg2Rad(HorizontalangleI);
		double CosAngleOfIncidence = Math.cos(AltitudeAngle)*Math.cos(Azimuth - HorizontalAngle)*Math.sin(VerticalAngle)+Math.sin(AltitudeAngle)*Math.cos(VerticalAngle);
		double AngleOfIncidence = Rad2Deg(Math.acos(CosAngleOfIncidence));  //acos is inverse cosine.  I don't know how to do this in java

		double DirectIncident = (AngleOfIncidence <= 90)? DirectNormal*CosAngleOfIncidence:0;

		double DiffuseIncident = DiffuseHorizontal*((1 + Math.cos(VerticalAngle))/2);
		return DirectIncident + DiffuseIncident;
	}

	private double Deg2Rad(double Degrees) {
		return Math.toRadians(Degrees);
		//return  Degrees*(2*3.14159265)/360;
	}

	private double Rad2Deg(double Radians) {
		return Math.toDegrees(Radians);
		//return Radians*360/(2*3.14159265);
	}

	public static void main(String[] args)
	{
		OlinHVACRegressionMarkFunctionOld tmp = new OlinHVACRegressionMarkFunctionOld();
		tmp.addWattageEvents(null, new DBWarningsList());
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
	public String getUserEditableClass()
	{
		return BuildingFunction.class.getCanonicalName();
	}
	
	@Override
	public OlinHVACRegressionMarkFunctionOld createNew(Collection<Record<?>> filterObjects)
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
		return new OlinHVACRegressionMarkFunctionOld(templateBuilding.getPrimaryKey());	}
}
