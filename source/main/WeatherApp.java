package source.main;

import java.io.*;

import java.util.Scanner;
import java.net.URL;
import java.net.HttpURLConnection;

import source.display.*;


public class WeatherApp {
	
	// Strings that contain the API web addresses
	// cat fact
	public static final String catFactURL = "https://catfact.ninja/fact?max_length=140";
	// Open Weather // URL + zipcode + key
	public static final String openWeatherKey = "key_here"; // add to end of URL
	public static final String openWeatherURL = "http://api.openweathermap.org/data/2.5/weather?zip=";
	public static final String openWeatherForURL = "http://api.openweathermap.org/data/2.5/forecast?zip=";
	
	// Dark Sky // URL(includes key) + latitude,longitude /// 37.8267,-122.4233
	public static final String darkSkyURL = "https://api.darksky.net/forecast/fff91e90a2fe3c44dc3bf82bd0b89752/";
	
	// Weatherbit.io // URL + zipcode + KEY
	public static final String weatherBitURL = "https://api.weatherbit.io/v2.0/current?postal_code=";
	public static final String weatherBitKey = "&country=US&key=key_here";
	
	//////////////////
	// Strings that contain the output filenames
	public static final String optionsFile = "options.txt";
	public static final String catFile = "cat.txt";
	public static final String openWeatherFile = "oWeather.txt";
	public static final String weatherBitFile = "weatherBit_IO.txt";
	public static final String darkSkyFile = "darkSky.txt";
	public static final String openWeatherForFile = "oWeatherF.txt";
	
	//////////////////
	// Search options to decide which data to extract from files in readData()
	public enum OPTION {OPTIONS, CAT, O_WEATHER, DARK_SKY, WEATHER_BIT};
	public static final short OPTIONS_LENGTH = 2;
	
	public static void main(String[] args)
	{
		processData();
	}
	
	public static String[] getOptions()
	{
		String[] loadedOptions = readData(optionsFile, OPTION.OPTIONS);
		String zipcode;
		if (loadedOptions != null)
		{
			zipcode = loadedOptions[0]; // zipcode
			try
			{
				int temp = Integer.parseInt(zipcode);
			}
			catch (NumberFormatException e)
			{
				System.err.println("Invalid zipcode written to options. Reseting to default");
				zipcode = "21234";
			}
			if (zipcode.length() != 5)
			{
				System.err.println("Invalid zipcode length written to options. Reseting to default");
				zipcode = "21234";
			}
		}
		else // it is null
		{
			zipcode = "21234";
			
			String[] newArray = new String[2];
			newArray[0] = zipcode;
			newArray[1] = null;
			
			return newArray;
		}
		
		return loadedOptions;
	}
	
	public static void processData()
	{
		String[] options = getOptions();
		String zipcode = options[0];
		String loc = options[1];
		// These methods fetch data from URLs and save it locally
		// cat fact
		StringBuffer dataString = new StringBuffer(getData(catFactURL, "userID", "application/json")); // eventually pass the entire url through here, grab data on way back out
		saveData(dataString, catFile);
		// openWeather using zipcode
		dataString = new StringBuffer(getData(openWeatherURL+zipcode+openWeatherKey, "userID", "application/json"));
		saveData(dataString, openWeatherFile);
		
		// weatherBit using zipcode
		dataString = new StringBuffer(getData(weatherBitURL+zipcode+weatherBitKey, "trevoramburgey", "application/json"));
		saveData(dataString, weatherBitFile);
		
		// Dark Sky using lat long
		if (loc != null)
		{
			dataString = new StringBuffer(getData(darkSkyURL+loc, "trevoramburgey", "application/json"));
			saveData(dataString, darkSkyFile);
		}
		else // loc is null / location is not written to the file
		{
			String[] openWeather = readData(openWeatherFile, OPTION.O_WEATHER);
			loc = openWeather[1]+","+openWeather[0];
			options[1] = loc;
			dataString = new StringBuffer(getData(darkSkyURL+loc, "trevoramburgey", "application/json"));
			saveData(dataString, darkSkyFile);
			saveOptions(options, optionsFile);
		}
		
		launchGUI(Integer.parseInt(zipcode)); // checked to make sure that it was parsable in getZipCode();
	}
	
	public static void launchGUI(int zip)
	{
		Display display = new Display("Weather App, COSC-350 API Project", 700, 700, zip);
		
		////// These methods read the formatted data saved locally
		String[] catFact = readData(catFile, OPTION.CAT);
		String[] openWeather = readData(openWeatherFile, OPTION.O_WEATHER);
		/*String[] openWeatherDescript = {"//////////////longitude","latitude","condition","description","temp K", "pressure",
				"humidity","min temp K", "max temp K", "wind speed","wind dir deg", "cloud cover %","time","sunrise","sunset","location"};*/
		
		String[] darkSky = readData(darkSkyFile, OPTION.DARK_SKY);
		// time, summary, precipIntensity, precipProbability, temperature, apparentTemperature, dewPoint, humidity, pressure, windSpeed, windGust, windBearing, cloudCover
		//uvIndex, visibility, ozone, 
		/*String[] darkSkyDescript = {"//////////////timeUnix", "summaryGeneral", "precipIntensity in inches", "precipProbability", "temperature F", "feels like temp", "dewPoint F", "humidity",
				"sea-level pressure milibars", "wind speed", "wind Gust", "wind bearing/direction", "cloud cover", "UV index", "average visibility mi", "ozone (Dobson)"};*/
		
		String[] weatherBit = readData(weatherBitFile, OPTION.WEATHER_BIT);
		// wind direction letters, rh=relative humidity, pod=day/night, pressure, cloud coverage, time, estimate solar radiation, latitude, wind speed
		// pressure sea level (mb), visibility, longitude, uv, dew point celcius, air quality index, wind direction degrees
		// precip mm/hr, sunrise, description
		/*String[] weatherBitDescript = {"////////////relative wind direction", "humidity", "day or night?", "air pressure (mb)", "cloud coverage", "time", "est. solar radiation", "latitude", "wind speed",
				"sea level pressure (mb)", "visibility", "longitude", "UV", "dew point cel", "air quality index", "wind direction deg", "precipitation", "sunrise", "description"};
		*/
		DataCruncher averager = new DataCruncher(openWeather, darkSky, weatherBit);
		
		/*String[] total = extendArrays(openWeather, weatherBit);
		total = extendArrays(total, darkSky);
		
		String[] totalDescript = extendArrays(openWeatherDescript, weatherBitDescript);
		totalDescript = extendArrays(totalDescript, darkSkyDescript);*/
		
		if (catFact[0].equals("$"))
			display.setCatFact("Connection error getting cat fact");
		else
			display.setCatFact(catFact[0]);
		
		display.setWeather(mergeArrays(averager.getDescript(), averager.getData())); // merge arrays merges each element as: "array 1: " "array 2" 
		display.pack();
		
		while (display.getFrame().isEnabled())
		{
			if (display.isSubmitClicked())
			{
				zip = display.getZipCode();
				
				boolean[] passedCheck = {false,false,false};
				String loc = null;
				// fetch new data
				StringBuffer dataString = new StringBuffer(getData(openWeatherURL+zip+openWeatherKey, "userID", "application/json"));
				if (dataString.charAt(0) != '$')
				{
					passedCheck[0] = true;
					saveData(dataString, openWeatherFile);
					openWeather = readData(openWeatherFile, OPTION.O_WEATHER);
					String[] temp = new String[OPTIONS_LENGTH];
					loc = openWeather[1]+","+openWeather[0];
					temp[0] = Integer.toString(zip);
					temp[1] = loc;
					saveOptions(temp, optionsFile);
				}
				
				dataString = new StringBuffer(getData(weatherBitURL+zip+weatherBitKey, "trevoramburgey", "application/json"));
				if (dataString.charAt(0) != '$')
				{
					passedCheck[1] = true;
					saveData(dataString, weatherBitFile);
					darkSky = readData(darkSkyFile, OPTION.DARK_SKY);
				}
				
				dataString = new StringBuffer(getData(darkSkyURL+loc, "trevoramburgey", "application/json"));
				if (dataString.charAt(0) != '$')
				{
					passedCheck[2] = true;
					saveData(dataString, darkSkyFile);
					weatherBit = readData(weatherBitFile, OPTION.WEATHER_BIT);
				}
				
				if (passedCheck[0] == true && passedCheck[1] == true && passedCheck[2] == true)
				{
					averager.resetData(openWeather, darkSky, weatherBit);
					display.setWeather(mergeArrays(averager.getDescript(), averager.getData())); // merge arrays merges each element as: "array 1: " "array 2" 
					display.resetClick();
					display.pack();
				}
				else
				{
					display.setZipText("No Data for that Zipcode");
					display.resetClick();
				}
				
			}
			int checkButton = display.isRadioChanged();
			if (checkButton != -1) // if not equal to the sentinel value
			{
				if (checkButton == 0)
				{
					averager.setTempScale(DataCruncher.TEMP_SCALE.F);
				}
				else if (checkButton == 1)
				{
					averager.setTempScale(DataCruncher.TEMP_SCALE.C);
				}
				else if (checkButton == 2)
				{
					averager.setTempScale(DataCruncher.TEMP_SCALE.K);
				}
				else
					System.err.println("Strange value returned for radio buttons to WeatherApp");
				display.setWeather(mergeArrays(averager.getDescript(), averager.getData()));
				display.pack();
			}
			System.out.toString(); // I guess the java window takes all priority away from the while loop, so this causes the console to take back control every while loop ?
		}
	}
	
	public static StringBuffer getData(String site, String par1, String par2)
	{
		StringBuffer bufferedData = new StringBuffer();
		
		try
		{
			URL getRequestURL = new URL(site); //holds the url of the website you are trying to reach
			HttpURLConnection httpCon = (HttpURLConnection) getRequestURL.openConnection();
			httpCon.setRequestMethod("GET");
			httpCon.setRequestProperty(par1, par2);
			int responseCode = httpCon.getResponseCode();
			
			if (responseCode == HttpURLConnection.HTTP_OK)
			{
				String readLine = null;
				
				BufferedReader input = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));
				while ((readLine = input.readLine()) != null)
					bufferedData.append(readLine);
				input.close();
			}
			else
			{
				System.out.println("GET didn't work");
				bufferedData.append('$');
			}
		}
		catch (IOException e)
		{
			System.err.println("IOException error in getData: " + site);
			bufferedData.append('$');
		}
		
		return bufferedData;
	}
	
	public static void saveData(StringBuffer data, String filename)
	{	
		File f = new File(filename);
		
		try 
		{
			PrintStream output = new PrintStream(f);
			
			for (int i = 0; i < data.length(); i++)
			{
				if (data.charAt(i) == ',' || data.charAt(i) == ':' || data.charAt(i) == '"')
				{
					output.print(" ");
				}
				else if (data.charAt(i) == '{')
				{
					output.print(data.charAt(i));
					output.println();
					output.print("\t");
				}
				else if (data.charAt(i) == '}')
				{
					output.println();
					output.print(data.charAt(i) + " ");
				}
				else
					output.print(data.charAt(i));
			}
			
			//System.out.println("output to: " + f.getAbsolutePath()); // will display in console the path output
			output.close();
		}
		catch (FileNotFoundException e)
		{
			System.err.println("FileNotFoundException in saveData(): " + filename);
		}
	}
	
	public static void saveOptions(String[] data, String filename)
	{	
		File f = new File(filename);
		
		try 
		{
			PrintStream output = new PrintStream(f);
			
			for (int i = 0; i < data.length; i++)
			{
				output.println(data[i]);
			}
			output.close();
		}
		catch (FileNotFoundException e)
		{
			System.err.println("FileNotFoundException in saveOptions(): " + filename);
		}
	}
	
	public static String[] readData(String filename, OPTION DATA_TYPE)
	{
		File f = new File(filename);
		try
		{
			Scanner input = new Scanner(f);
			
			// if statements to extract only the data I want from my local files
			if (DATA_TYPE == OPTION.CAT)
			{
				String[] single = {findKeyWord(input, "fact", "length", true)};
				input.close();
				return single;
			}
			else if (DATA_TYPE == OPTION.O_WEATHER)
			{
				String[] oWeather = new String[16];
				while (input.hasNext())
				{
					for (int i = 0; i < oWeather.length && input.hasNext(); i++)
					{	
						if (i == 0)
							oWeather[i] = findKeyWord(input, "lon");
						else if (i == 1)
							oWeather[i] = findKeyWord(input, "lat");
						else if (i == 2)
							oWeather[i] = findKeyWord(input, "main");
						else if (i == 3)
							oWeather[i] = findKeyWord(input, "description");
						else if (i == 4)
							oWeather[i] = findKeyWord(input, "temp");
						else if (i == 5)
							oWeather[i] = findKeyWord(input, "pressure");
						else if (i == 6)
							oWeather[i] = findKeyWord(input, "humidity");
						else if (i == 7)
							oWeather[i] = findKeyWord(input, "temp_min");
						else if (i == 8)
							oWeather[i] = findKeyWord(input, "temp_max");
						else if (i == 9)
							oWeather[i] = findKeyWord(input, "speed");
						else if (i == 10)
							oWeather[i] = findKeyWord(input, "deg");
						else if (i == 11)
							oWeather[i] = findKeyWord(input, "all");
						else if (i == 12)
							oWeather[i] = findKeyWord(input, "dt");
						else if (i == 13)
							oWeather[i] = findKeyWord(input, "sunrise");
						else if (i == 14)
							oWeather[i] = findKeyWord(input, "sunset");
						else if (i == 15)
							oWeather[i] = findKeyWord(input, "name");
						else
							input.next();
					}
				}
				input.close();
				input = new Scanner(f); // for some reason the first element of the array doesn't work properly
				oWeather[0] = findKeyWord(input, "lon"); // this is a brute force fix - I'm tired
				oWeather[15] = findKeyWord(input, "name", "cod", true);
				input.close();
				return oWeather;
			}
			else if (DATA_TYPE == OPTION.DARK_SKY)
			{
				String[] darkWeather = new String[16];
				while (input.hasNext())
				{
					for (int i = 0; i < darkWeather.length && input.hasNext(); i++)
					{	
						if (i == 0)
							darkWeather[i] = findKeyWord(input, "time");
						else if (i == 1)
							darkWeather[i] = findKeyWord(input, "summary");
						else if (i == 2)
							darkWeather[i] = findKeyWord(input, "precipIntensity");
						else if (i == 3)
							darkWeather[i] = findKeyWord(input, "precipProbability");
						else if (i == 4)
							darkWeather[i] = findKeyWord(input, "temperature");
						else if (i == 5)
							darkWeather[i] = findKeyWord(input, "apparentTemperature");
						else if (i == 6)
							darkWeather[i] = findKeyWord(input, "dewPoint");
						else if (i == 7)
							darkWeather[i] = findKeyWord(input, "humidity");
						else if (i == 8)
							darkWeather[i] = findKeyWord(input, "pressure");
						else if (i == 9)
							darkWeather[i] = findKeyWord(input, "windSpeed");
						else if (i == 10)
							darkWeather[i] = findKeyWord(input, "windGust");
						else if (i == 11)
							darkWeather[i] = findKeyWord(input, "windBearing");
						else if (i == 12)
							darkWeather[i] = findKeyWord(input, "cloudCover");
						else if (i == 13)
							darkWeather[i] = findKeyWord(input, "uvIndex");
						else if (i == 14)
							darkWeather[i] = findKeyWord(input, "visibility");
						else if (i == 15)
							darkWeather[i] = findKeyWord(input, "ozone");
						else
							input.next();
					}
				}
				input.close();
				input = new Scanner(f); // for some reason the first element of the array doesn't work properly
				darkWeather[4] = findKeyWord(input, "temperature");
				input.close();
				return darkWeather;
			}
			else if (DATA_TYPE == OPTION.WEATHER_BIT)
			{
				// wind direction letters, rh=relative humidity, pod=day/night, pressure, cloud coverage, time, estimate solar radiation, latitude, wind speed
				// pressure sea level (mb), visibility, longitude, uv, dew point celcius, air quality index, wind direction degrees
				// precip mm/hr, sunrise, description
				String[] weatherBit = new String[19];
				while (input.hasNext())
				{
					for (int i = 0; i < weatherBit.length && input.hasNext(); i++)
					{	
						if (i == 0)
							weatherBit[i] = findKeyWord(input, "wind_cdir");
						else if (i == 1)
							weatherBit[i] = findKeyWord(input, "rh");
						else if (i == 2)
							weatherBit[i] = findKeyWord(input, "pod");
						else if (i == 3)
							weatherBit[i] = findKeyWord(input, "pres");
						else if (i == 4)
							weatherBit[i] = findKeyWord(input, "clouds");
						else if (i == 5)
							weatherBit[i] = findKeyWord(input, "ts");
						else if (i == 6)
							weatherBit[i] = findKeyWord(input, "solar_rad");
						else if (i == 7)
							weatherBit[i] = findKeyWord(input, "lat");
						else if (i == 8)
							weatherBit[i] = findKeyWord(input, "wind_spd");
						else if (i == 9)
							weatherBit[i] = findKeyWord(input, "slp");
						else if (i == 10)
							weatherBit[i] = findKeyWord(input, "vis");
						else if (i == 11)
							weatherBit[i] = findKeyWord(input, "lon");
						else if (i == 12)
							weatherBit[i] = findKeyWord(input, "uv");
						else if (i == 13)
							weatherBit[i] = findKeyWord(input, "dewpt");
						else if (i == 14)
							weatherBit[i] = findKeyWord(input, "aqi");
						else if (i == 15)
							weatherBit[i] = findKeyWord(input, "wind_dir");
						else if (i == 16)
							weatherBit[i] = findKeyWord(input, "precip");
						else if (i == 17)
							weatherBit[i] = findKeyWord(input, "sunrise");
						else if (i == 18)
							weatherBit[i] = findKeyWord(input, "description");
						else
							input.next();
					}
				}
				input.close();
				input = new Scanner(f); // for some reason the first element of the array doesn't work properly
				weatherBit[0] = findKeyWord(input, "wind_cdir");
				weatherBit[18] = findKeyWord(input, "description", "}", true);
				input.close();
				return weatherBit;
			}
			else if (DATA_TYPE == OPTION.OPTIONS)
			{
				String[] temp = new String[OPTIONS_LENGTH];
				for (short i = 0; i < OPTIONS_LENGTH && input.hasNextLine(); i++)
					temp[i] = input.nextLine();
				
				input.close();
				return temp;
			}
			// else
			input.close();
		}
		catch (FileNotFoundException e)
		{
			System.err.println("FileNotFoundException in readData(): " + filename);
			
			if (DATA_TYPE == OPTION.OPTIONS)
			{
				String[] temp = new String[] {"21234"};
				saveOptions(temp, optionsFile);
			}
			return null;
		}
		
		return null;
	}
	
	// for a multi token string return
	public static String findKeyWord(Scanner sc, String keyWord, String stopWord, boolean spaces)
	{
		String temp = "NOTKEYWORD";
		
		while (sc.hasNext())
		{
			if (temp.equals(keyWord))
			{
				temp = sc.next(); // skip keyword
				StringBuffer sb = new StringBuffer();
				while (!temp.equals(stopWord) && sc.hasNext())
				{
					sb.append(temp);
					if (spaces)
						sb.append(" ");
					temp = sc.next();
				}
				temp = sb.toString();
				return temp;
			}
			else
				temp = sc.next();
		}
		
		return temp;
	}
	
	// for a single token return 1
		public static String findKeyWord(Scanner sc, String keyWord)
		{
			String temp = "NOTKEYWORD";
			
			while (sc.hasNext())
			{
				if (temp.equals(keyWord))
				{
					temp = sc.next(); // skip keyword
					return temp;
				}
				else
					temp = sc.next();
			}
			
			return temp;
		}
	
	public static void printArray(String[] array)
	{
		for (int i = 0; i < array.length; i++)
		{
			System.out.print(array[i] + " ");
		}
		System.out.println();
	}
	
	public static String[] mergeArrays(String[] a1, String[]  a2)
	{
		String[] temp;
		
		if (a1 == null || a2 == null)
			return null;
		
		if (a1.length >= a2.length)
			temp = new String[a1.length];
		else
			temp = new String[a2.length];
		
		for (int i = 0; i < temp.length; i++)
		{
			temp[i] = a1[i] + ": " + a2[i];
		}
		
		return temp;
	}
	
	public static String[] extendArrays(String[] a1, String[] a2)
	{
		String[] temp = new String[a1.length + a2.length];
		
		if (a1 == null || a2 == null)
			return null;
		
		for (int i = 0; i < a1.length; i++)
		{
			temp[i] = a1[i];
		}
		
		for (int i = 0; i < a2.length; i++)
		{
			temp[i+a1.length] = a2[i];
		}
			
		return temp;
	}
}