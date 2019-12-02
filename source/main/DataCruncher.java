package source.main;

///////////////////////////////////////////
////// FIGURE OUT UNITS OF AIR PRESSURE FOR EACH API <-- finish air pressure
//// add check boxes for temperature unit of choice
//////////////////////////////////////////

public class DataCruncher {
	
	private String[] oW, dS, wB; // openWeather, darkSky, weatherBit
	private String[] data; // "averaged" final data
	private String[] descript; // descriptions of the final data
	private String[] footer;
	
	public static enum TEMP_SCALE {F, C, K};
	TEMP_SCALE tempSelection;
	
	public static enum MEAS_SCALE {I, M}; // imperial, metric
	MEAS_SCALE measType;
	
	private final int dataSize = 15; // number of elements in the complete data set
	private final int numberArrays = 3;
	private boolean[] loadedProperly = new boolean[numberArrays];
	
	private final String FAIL_MESSAGE = "Data currently unavailable";
	
	private String tempFooter;
	
	// * denotes number of sources used in final data set
	public DataCruncher(String[] o, String[] d, String[] w)
	{
		inputData(o,d,w);
		tempSelection = TEMP_SCALE.F;
		tempFooter = " F";
		measType = MEAS_SCALE.I;
		descript = new String[] {"*Area name", "**Latitude", "**Longitude", "**Temperature", "*Temp min", "*Temp max", "*Feels Like", "*Description",
				"***Humidity(%)", "**Air Pressure(mB)", "***Cloud Cover(%)", "*Precip Chance(%)", "***Wind Speed(mi/hr)", "***Wind Direction(deg)", "**Average Visibility(mi)"};
		footer = new String[dataSize];
		for (int i = 0; i < footer.length; i++)
			footer[i] = "";
		crunchData();
	}
	
	public void resetData(String[] o, String[] d, String[] w)
	{
		inputData(o,d,w);
		crunchData();
	}
	
	private void crunchData()
	{
		data = new String[dataSize];
		// default set to false
		if (!oW[0].equals("$"))
		{
			int count = 0;
			for (int i = 0; i < oW.length; i++)
			{
				if (oW[i] != null)
					count++;
			}
			if(count == oW.length)
				loadedProperly[0] = true;
			else
				System.err.println("Null found in openWeather data");
		}
		if (!dS[0].equals("$"))
		{
			int count = 0;
			for (int i = 0; i < dS.length; i++)
			{
				if (dS[i] != null)
					count++;
			}
			if(count == dS.length)
				loadedProperly[1] = true;
			else
				System.err.println("Null found in darkSky data");

		}
		if (!wB[0].equals("$"))
		{
			int count = 0;
			for (int i = 0; i < wB.length; i++)
			{
				if (wB[i] != null)
					count++;
			}
			if(count == wB.length)
				loadedProperly[2] = true;
			else
				System.err.println("Null found in weatherBit data");
		}
		// location / Area Name (1)
		setName();
		// Location coordinates (2,3)
		setLatitude();
		setLongitude();
		// Temperature, temp max, temp min (4,5,6) feels like (7)
		setTemp();
		setTempRange();
		setFeelsLike();
		// Description of current weather conditions (8)
		setDescript();
		// Humidity, air pressure (9) (10)
		setHumidity();
		setAirPressure();
		// cloud coverage in percent (11) precip chance (12)
		setCloudCover();
		setPrecipChance();
		// wind speed and direction in degrees (13,14)
		setWindSpeed();
		setWindDirection();
		// average visibility (15)
		setVisibility();
		
		addFooters();
	}
	
	private void setName()
	{
		if (loadedProperly[0])
			data[0] = oW[15];
		else
			data[0] = FAIL_MESSAGE;
	}
	
	private void setLatitude()
	{
		if (loadedProperly[0] && loadedProperly[2])
		{
			double lat = ((Double.parseDouble(oW[1]) + Double.parseDouble(wB[7])) / 2);
			lat = Math.round(lat*100.0)/100.0;
			data[1] = Double.toString(lat);
		}
		else if (loadedProperly[0])
		{
			data[1] = oW[1];
			System.err.println("failed to average latitude for weatherBit");
		}
		else if (loadedProperly[2])
		{
			data[1] = wB[7];
			System.err.println("failed to average latitude for openWeather");
		}
		else
		{
			data[1] = FAIL_MESSAGE;
		}
	}
	
	private void setLongitude()
	{
		if (loadedProperly[0] && loadedProperly[2])
		{
			double lon = ((Double.parseDouble(oW[0]) + Double.parseDouble(wB[11])) / 2);
			lon = Math.round(lon*100.0)/100.0;
			data[2] = Double.toString(lon);
		}
		else if (loadedProperly[0])
		{
			data[2] = oW[0];
			System.err.println("failed to average longitude for weatherBit");
		}
		else if (loadedProperly[2])
		{
			data[2] = wB[11];
			System.err.println("failed to average latitude for openWeather");
		}
		else
		{
			data[2] = FAIL_MESSAGE;
		}
	}
	
	private void setTemp()
	{
		if (!loadedProperly[0] && !loadedProperly[1])
		{
			data[3] = FAIL_MESSAGE;
			return;
		}
		
		double tempOw = -12345.6;
		double tempDs = -12345.6;
		double temp = -12345.6;
		
		if (loadedProperly[0])
			tempOw = Double.parseDouble(oW[4]); // in Kelvin
		else
			System.err.println("Failed to use openWeather temperature");
		
		if (loadedProperly[1])
			tempDs = Double.parseDouble(dS[4]); // in Farenheit
		else
			System.err.println("Failed to use darkSky temperature");
		
		if (tempSelection == TEMP_SCALE.F)
		{
			tempOw = convertTemp(tempOw, TEMP_SCALE.K, TEMP_SCALE.F);
		}
		else if (tempSelection == TEMP_SCALE.C)
		{
			tempOw = convertTemp(tempOw, TEMP_SCALE.K, TEMP_SCALE.C);
			tempDs = convertTemp(tempDs, TEMP_SCALE.F, TEMP_SCALE.C);
		}
		else // in Kelvin
		{
			tempDs = convertTemp(tempDs, TEMP_SCALE.F, TEMP_SCALE.K);
		}
		
		if (loadedProperly[0] && loadedProperly[1])
			temp = (tempOw+tempDs)/2;
		else if (loadedProperly[0])
			temp = tempOw;
		else if (loadedProperly[1])
			temp = tempDs;
		else
			System.err.println("problem with final tempSet in DataCruncher");
		
		temp = Math.round(temp*100.0)/100.0;
		data[3] = Double.toString(temp);
	}
	
	private void setTempRange()
	{
		if (loadedProperly[0])
		{
			double minTemp = Double.parseDouble(oW[7]); // in K
			double maxTemp = Double.parseDouble(oW[8]); // in K
			
			if (tempSelection == TEMP_SCALE.F)
			{
				minTemp = convertTemp(minTemp, TEMP_SCALE.K, TEMP_SCALE.F);
				maxTemp = convertTemp(maxTemp, TEMP_SCALE.K, TEMP_SCALE.F);
			}
			else if (tempSelection == TEMP_SCALE.C)
			{
				minTemp = convertTemp(minTemp, TEMP_SCALE.K, TEMP_SCALE.C);
				maxTemp = convertTemp(maxTemp, TEMP_SCALE.K, TEMP_SCALE.C);
			}
			else // kelvin
			{
				// default is in kelvin
			}
			
			minTemp = Math.round(minTemp*100.0)/100.0;
			maxTemp = Math.round(maxTemp*100.0)/100.0;
			data[4] = Double.toString(minTemp);
			data[5] = Double.toString(maxTemp);
		}
		else
		{
			data[4] = FAIL_MESSAGE;
			data[5] = FAIL_MESSAGE;
		}
	}
	
	private void setFeelsLike()
	{
		if (loadedProperly[1])
		{
			double feelsT = Double.parseDouble(dS[5]); // in F
			
			if (tempSelection == TEMP_SCALE.F)
			{
				// default in F
			}
			else if (tempSelection == TEMP_SCALE.C)
			{
				feelsT = convertTemp(feelsT, TEMP_SCALE.F, TEMP_SCALE.C);
			}
			else // kelvin
			{
				feelsT = convertTemp(feelsT, TEMP_SCALE.F, TEMP_SCALE.K);
			}
			
			feelsT = Math.round(feelsT*100.0)/100.0;
			data[6] = Double.toString(feelsT);
		}
		else
		{
			data[6] = FAIL_MESSAGE;
		}
	}
	
	private void setDescript()
	{
		if (loadedProperly[0])
		{
			data[7] = (oW[2] + ", " + oW[3]);
		}
		else
			data[7] = FAIL_MESSAGE;
	}
	
	private void setHumidity()
	{
		// ow 6 // dS 7 // wB 1
		double hOpen = 0;
		double hDark = 0;
		double hBit = 0;
		
		if (!loadedProperly[0] && !loadedProperly[1] && !loadedProperly[2])
		{
			data[8] = FAIL_MESSAGE;
			return;
		}
		
		if (loadedProperly[0])
			hOpen = Double.parseDouble(oW[6]);
		if (loadedProperly[1])
			hDark = Double.parseDouble(dS[7]) * 100; // reports as decimal
		if (loadedProperly[2])
			hBit = Double.parseDouble(wB[1]);
		
		double humidity = (hOpen+hDark+hBit) / 3;
		humidity = Math.round(humidity*100.0)/100.0;
		data[8] = Double.toString(humidity);	
	}
	
	private void setAirPressure()
	{
		double openPressure = 0;
		double bitPressure = 0;
		
		if (!loadedProperly[0]  && !loadedProperly[2])
		{
			data[8] = FAIL_MESSAGE;
			return;
		}
		
		if (loadedProperly[0])
			openPressure = Double.parseDouble(oW[5]);
		if (loadedProperly[2])
			bitPressure = Double.parseDouble(wB[3]);
		
		data[9] = Double.toString((openPressure + bitPressure) / 2);
	}
	
	private void setCloudCover()
	{
		if (!loadedProperly[0] && !loadedProperly[1] && !loadedProperly[2])
		{
			data[10] = FAIL_MESSAGE;
			return;
		}
		
		double oClouds = 0;
		double dClouds = 0;
		double bClouds = 0;
		
		if (loadedProperly[0])
			oClouds = Double.parseDouble(oW[11]);
		if (loadedProperly[1])
			dClouds = Double.parseDouble(dS[12]);
		if (loadedProperly[2])
			bClouds = Double.parseDouble(wB[4]);
		
		
		
		double clouds = (oClouds+dClouds+bClouds) / 3;
		clouds = Math.round(clouds*100.0)/100.0;
		data[10] = Double.toString(clouds);
	}
	
	private void setPrecipChance()
	{
		if (loadedProperly[1])
		{
			double precip = Double.parseDouble(dS[3]) * 100;
			data[11] = Double.toString(precip);
		}
		else
			data[11] = FAIL_MESSAGE;
	}
	
	private void setWindSpeed()
	{
		if (!loadedProperly[0] && !loadedProperly[1] && !loadedProperly[2])
		{
			data[12] = FAIL_MESSAGE;
			return;
		}
		
		double openSpeed = 0;
		double darkSpeed = 0;
		double bitSpeed = 0;
		
		if (loadedProperly[0])
			openSpeed = Double.parseDouble(oW[9]);
		if (loadedProperly[1])
			darkSpeed = Double.parseDouble(dS[9]);
		if (loadedProperly[2])
			bitSpeed = Double.parseDouble(wB[8]);
		
		if (measType == MEAS_SCALE.I)
		{
			if (loadedProperly[0]) // m/s
			{
				openSpeed = convertMeasure(openSpeed, MEAS_SCALE.M,MEAS_SCALE.I); // yd/s
				openSpeed = (openSpeed*3600)/1760; // yd/s -> yd/hr -> mi/hr
			}
			
			if (loadedProperly[2]) // m/s
			{
				bitSpeed = convertMeasure(bitSpeed, MEAS_SCALE.M,MEAS_SCALE.I); // yd/s
				bitSpeed = (bitSpeed*3600)/1760; // yd/s -> yd/hr -> mi/hr
			}
		}
		else if (measType == MEAS_SCALE.M)
		{
			if (loadedProperly[1]) // in mi/hr
			{
				darkSpeed = (darkSpeed*1760)/3600; // mi/hr->yd/hr->yd/s
				darkSpeed = convertMeasure(darkSpeed,MEAS_SCALE.I,MEAS_SCALE.M); // yards to meters
			}
		}
		
		double speed = (openSpeed+darkSpeed+bitSpeed) / 3;
		speed = Math.round(speed*100.0)/100.0;
		data[12] = Double.toString(speed);
	}
	
	private void setWindDirection()
	{
		if (!loadedProperly[0] && !loadedProperly[1] && !loadedProperly[2])
		{
			data[13] = FAIL_MESSAGE;
			return;
		}
		
		double openDir = 0;
		double darkDir = 0;
		double bitDir = 0;
		
		if (loadedProperly[0])
			openDir = Double.parseDouble(oW[10]);
		if (loadedProperly[1])
			darkDir = Double.parseDouble(dS[11]);
		if (loadedProperly[2])
			bitDir = Double.parseDouble(wB[15]);
		
		double dir = (openDir+darkDir+bitDir) / 3;
		dir = Math.round(dir*100.0)/100.0;
		data[13] = Double.toString(dir);
	}
	
	private void setVisibility()
	{
		if (!loadedProperly[1] && !loadedProperly[2])
		{
			data[14] = FAIL_MESSAGE;
			return;
		}
		
		double darkVis = 0; // mi
		double bitVis = 0; // km
		
		if (loadedProperly[1])
			darkVis = Double.parseDouble(dS[14]);
		if (loadedProperly[2])
			bitVis = Double.parseDouble(wB[10]);
		
		if (measType == MEAS_SCALE.I)
		{
			bitVis = bitVis / 1000; //km->m
			bitVis = convertMeasure(bitVis, MEAS_SCALE.M,MEAS_SCALE.I); //m -> yd
			bitVis = bitVis * 1760; // yd -> mi
		}
		else if (measType == MEAS_SCALE.M)
		{
			darkVis = darkVis / 1760; // mi -> yd
			darkVis = convertMeasure(darkVis, MEAS_SCALE.I,MEAS_SCALE.M); // yd -> m
			darkVis = darkVis * 1000;
		}
		
		double vis = (darkVis+bitVis)/2;
		vis = Math.round(vis*100.0)/100.0;
		data[14] = Double.toString(vis);
	}
	
	public void inputData(String[] o, String[] d, String[] w)
	{
		oW = o;
		dS = d;
		wB = w;
	}
	
	public String[] getDescript()
	{
		return descript;
	}
	
	public String[] getData()
	{
		return data;
	}
	
	public void setTempScale(TEMP_SCALE s)
	{
		if (s == TEMP_SCALE.F)
		{
			tempSelection = TEMP_SCALE.F;
			tempFooter = " F";
		}
		else if (s == TEMP_SCALE.C)
		{
			tempSelection = TEMP_SCALE.C;
			tempFooter = " C";
		}
		else if (s == TEMP_SCALE.K)
		{
			tempSelection = TEMP_SCALE.K;
			tempFooter = " K";
		}
		crunchData();
	}
	
	private double convertTemp(double t, TEMP_SCALE START, TEMP_SCALE FINISH)
	{
		if (START == FINISH)
			return t;
		double temp = t;
		
		if (START == TEMP_SCALE.F)
		{
			if (FINISH == TEMP_SCALE.C)
			{
				temp = (temp - 32);
				if (temp != 0)
					temp = (temp*5)/9;
			}
			else if (FINISH == TEMP_SCALE.K)
			{
				temp = (temp - 32);
				if (temp != 0)
					temp = (temp*5)/9 + 273.15;
				else
					temp = temp + 273.15;
			}
			else
				System.err.println("convertTemp error: START F, NO END");
		}
		else if (START == TEMP_SCALE.C)
		{
			if (FINISH == TEMP_SCALE.F)
			{
				temp = (temp*9)/5 + 32;
			}
			else if (FINISH == TEMP_SCALE.K)
			{
				temp = temp+273.15;
			}
			else
				System.err.println("convertTemp error: START C, NO END");
		}
		else if (START == TEMP_SCALE.K)
		{
			if (FINISH == TEMP_SCALE.C)
			{
				temp = temp - 273.15;
			}
			else if (FINISH == TEMP_SCALE.F)
			{
				temp = ((temp - 273.15)*9)/5 + 32;
			}
			else
				System.err.println("convertTemp error: START K, NO END");
		}
		else
			System.err.println("Incompatible TEMP_SCALE presented to convertTemp");
		
		return temp;
	}
	
	private double convertMeasure(double u, MEAS_SCALE START, MEAS_SCALE FINISH)
	{
		if (START == FINISH)
			return u;
		double unit = u;
		
		if (START == MEAS_SCALE.I)
		{
			if (FINISH == MEAS_SCALE.M)
			{
				unit = unit*0.9144; // yards to meters
			}
			else
				System.err.println("Unconfigured FINISH presented for unit converion");

		}
		else if (START == MEAS_SCALE.M)
		{
			if (FINISH == MEAS_SCALE.I)
			{
				unit = unit*1.09361; // meters to yards
			}
			else
				System.err.println("Unconfigured FINISH presented for unit converion");

		}
		else
			System.err.println("Unconfigured Measurement scale presented for converion");
		
		return unit;
	}
	
	private void addFooters()
	{
		/*descript = new String[] {"*Area name"0, "**Latitude"1, "**Longitude"2, "**Temperature"3, "*Temp min"4, "*Temp max"5, "*Feels Like"6, "*Description"7,
				"***Humidity8", "**Air Pressure9", "***Cloud Cover %"10, "*Precip Chance"11, "***Wind Speed"12, "***Wind Direction (deg)"13, "**Average Visibility"14};*/
		for (int i = 3; i <= 6; i++)
			footer[i] = tempFooter;
		
		footer[8] = " %";
		footer[9] = " mB";
		footer[10] = " %";
		footer[11] = " %";
		footer[13] = " deg";
		
		if (measType == MEAS_SCALE.I)
		{
			footer[12] = " mi/hr";
			footer[14] = " mi";
		}
		else if (measType == MEAS_SCALE.M)
		{
			footer[12] = " m/s";
			footer[14] = " km";
		}
	}
	
	public String[] getFooter()
	{
		return footer;
	}
}
