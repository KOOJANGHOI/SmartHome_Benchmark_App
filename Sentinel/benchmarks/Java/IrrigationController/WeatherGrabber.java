package IrrigationController;

// IoT Packages
import iotruntime.slave.IoTAddress;
import iotruntime.IoTURL;

// Standard Java Packages
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/** Class WeatherGrabber to get weather data information using the OpenWeatherMap api.
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-03-24
 */
public class WeatherGrabber {

	/*******************************************************************************************************************************************
	**
	**  Constants
	**
	*******************************************************************************************************************************************/

	// KEy for using the api, needed for authentication with the api when making calls to it
	public static final String API_KEY_ID = "fbc55f11190c6472e14b7743f8a38c92";

	// location in the jar file where the zipcodes csv data is located
	public static final String ZIPCODE_CSV_FILE = "./resources/zipcode.csv";

	// location in the jar file where the area codes csv data is located
	public static final String AREA_CODE_CSV_FILE = "./resources/area_codes.csv";




	/*******************************************************************************************************************************************
	**
	**  Variables
	**
	*******************************************************************************************************************************************/

	// Communications information for interfacing with the api
	private IoTAddress weatherDataAddress;

	// Things needed for caching of the data
	private long timestampOfLastWeatherRetrieval = 0;
	private int lastZipcodeLookup = 0;
	private List<DayWeather> weatherDataCache = null;

	private int savedZipcode = 0;
	private int savedNumberOfDays = 16;


	public WeatherGrabber(IoTAddress _IoTAddress) {
		this.weatherDataAddress = _IoTAddress;
	}




	/*******************************************************************************************************************************************
	**
	**  Public Methods
	**
	*******************************************************************************************************************************************/


	/** Method to get and parse the weather data for the saved zipcode and number of days
	 *
	 *   @return [List<DayWeather>] list of day by day weather data.
	 */
	public List<DayWeather> getWeatherData() {
		if (savedZipcode <= 0) {
			return null;
		}
		return getWeatherData(savedZipcode, savedNumberOfDays);
	}


	/** Method to get and parse the weather data for a specific zipcode for a specified number of days
	 *
	 *   @param _weatherCode  [int], zipcode to get the weather for.
	 *   @param _numberOfDays [int], number of days to lookup weather for.
	 *
	 *   @return [List<DayWeather>] list of day by day weather data.
	 */
	public List<DayWeather> getWeatherData(int _zipcode, int _numberOfDays) {

		// less than or equal to 0 means that the list will be empty
		if (_numberOfDays <= 0) {
			return new ArrayList<DayWeather>();
		}

		// get current date and time
		Date date = new Date();

		// check if we ever got the weather data
		if (this.timestampOfLastWeatherRetrieval != 0) {

			// check the elapsed time since we got the weather data
			long timeElapsedFromLastWeatherDataRead = date.getTime() - this.timestampOfLastWeatherRetrieval;
			timeElapsedFromLastWeatherDataRead /= 1000;				// convert to seconds

			// we got the cached weather data less than 12 hours ago so just use the cached data
			// The api limits how many calls we can make in a given time and so we should cache the data
			// and reuse it.  Also the weather doesnt change that fast
			if (timestampOfLastWeatherRetrieval <= 43200) {

				// make sure the cached weather data is for the zipcode that we are being asked for
				if (lastZipcodeLookup == _zipcode) {

					// now check that we actually have weather data available
					if (weatherDataCache != null) {

						// make sure we have enough weather data, we may only have data for some of the days that
						// are being requested but not all
						if (weatherDataCache.size() >= _numberOfDays) {
							return weatherDataCache;
						}
					}
				}
			}
		}

		// convert zipcode into weather api specific area code
		int weatherLocationCode = getWeatherCode(_zipcode);

		// check if weather information can be attained for the zipcode specified
		if (weatherLocationCode == -1) {
			return null;
		}

		// save information for caching
		lastZipcodeLookup = _zipcode;
		timestampOfLastWeatherRetrieval = date.getTime();

		// try to get the weather data XML from the server
		InputStream inputStream = getXmlData(weatherLocationCode, _numberOfDays);
		if (inputStream == null) {
			return null;
		}

		// convert the XML into an easier to use format
		weatherDataCache = parseXmlData(inputStream);
		return weatherDataCache;
	}


	/** Method to set the zipcode of this weather grabber
	 *
	 *   @param _zipcode  [int], zipcode to get the weather for.
	 *
	 *   @return [void] None.
	 */
	public void setZipcode(int _zipcode) {
		savedZipcode = _zipcode;
	}


	/** Method to set the number of days of this weather grabber
	 *
	 *   @param _numberOfDays  [int], number of days to get the weather for.
	 *
	 *   @return [void] None.
	 */
	public void setNumberOfDays(int _numberOfDays) {
		savedNumberOfDays = _numberOfDays;
	}


	/** Method to get the zipcode of this weather grabber
	 *
	 *   @return [int] zipcode of the weather grabber.
	 */
	public int getZipcode() {
		return savedZipcode;
	}


	/** Method to get the number of days of this weather grabber
	 *
	 *   @return [int] number of days of the weather grabber.
	 */
	public int getNumberOfDays() {
		return savedNumberOfDays;
	}

	/*******************************************************************************************************************************************
	**
	**  Helper Methods
	**
	*******************************************************************************************************************************************/

	/** Method to get the XML file that the weather api returns after a query
	 *
	 *   @param _weatherCode  [int], weather api specific location code.
	 *   @param _numberOfDays [int], number of days to lookup weather for.
	 *
	 *   @return [InputStream] InputStream containing the xml file data.
	 */
	private InputStream getXmlData(int _weatherCode, int _numberOfDays) {

		// We can only get a max of 16 days into the future
		if (_numberOfDays > 16) {
			_numberOfDays = 16;
		}

		// Create the url ending path with all the parameters needed to get the XML file
		String urlEnd = "/data/2.5/forecast/daily?id=" + Integer.toString(_weatherCode) + "&units=imperial&mode=xml&cnt=" + Integer.toString(_numberOfDays) + "&APPID=" + "fbc55f11190c6472e14b7743f8a38c92";

		// Communication object created based on address passed in by the runtime system
		IoTURL urlConnection = new IoTURL(weatherDataAddress);
		System.out.println("URL: " + urlEnd);

		try {
			// sets the connection ending address
			urlConnection.setURL(urlEnd);
			System.out.println("Connected to URL!");

			// Return the stream
			return urlConnection.openStream();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// something happened and the URL connection could not be opened.
		return null;
	}


	/** Method to parse the XML file that the weather api returns after a query
	 *
	 *   @param inputStream [InputStream], input stream containing weather XML file.
	 *
	 *   @return [List<DayWeather>] list of day by day weather data.
	 */
	private List<DayWeather> parseXmlData(InputStream inputStream) {
		try {

			// array to store the parsed weather data per day
			ArrayList<DayWeather> weatherDataList = new ArrayList<DayWeather>();

			// stuff needed to open and parse an XML file
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setValidating(false);
			documentBuilderFactory.setNamespaceAware(false);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(inputStream);

			// get the forecast node that is the main node for each of the day subnodes
			NodeList forcastNodes = document.getElementsByTagName("forecast").item(0).getChildNodes();
			for (int i = 0; i < forcastNodes.getLength(); ++i) {

				// the node for each of the individual days
				Node dayNode = forcastNodes.item(i);

				// make sure the node is an actually day node and not an invalid node
				if (dayNode.getNodeType() == Node.ELEMENT_NODE) {

					// Convert to elements since we can only pull data out of elements not nodes
					Element dayElement = (Element)dayNode;

					// Information is saved in child nodes
					NodeList informationNodes = dayNode.getChildNodes();

					// Data that pulls the pre-parse data from the XML file
					String dateString = dayElement.getAttribute("day");
					String weatherInfoCode = "";
					String windDirectionText = "";
					String windDirectionDegrees = "";
					String windSpeed = "";
					String temperatureDay = "";
					String temperatureEvening = "";
					String temperatureMorning = "";
					String temperatureNight = "";
					String temperatureMax = "";
					String temperatureMin = "";
					String pressure = "";
					String humidity = "";
					String cloudCoverage = "";

					// go through the child info nodes and pull the data out
					for (int j = 0; j < informationNodes.getLength(); ++j) {

						Node informationNode = informationNodes.item(j);

						if (informationNode.getNodeType() == Node.ELEMENT_NODE) {

							Element informationElement = (Element)informationNode;
							String informationName = informationElement.getTagName();

							if (informationName.equals("symbol")) {
								weatherInfoCode = informationElement.getAttribute("number");

							} else if (informationName.equals("windDirection")) {
								windDirectionText = informationElement.getAttribute("code");
								windDirectionDegrees = informationElement.getAttribute("deg");

							} else if (informationName.equals("windSpeed")) {
								windSpeed = informationElement.getAttribute("mps");

							} else if (informationName.equals("temperature")) {
								temperatureDay = informationElement.getAttribute("day");
								temperatureEvening = informationElement.getAttribute("eve");
								temperatureMorning = informationElement.getAttribute("morn");
								temperatureNight = informationElement.getAttribute("night");
								temperatureMax = informationElement.getAttribute("max");
								temperatureMin = informationElement.getAttribute("min");

							} else if (informationName.equals("pressure")) {
								pressure = informationElement.getAttribute("value");

							} else if (informationName.equals("humidity")) {
								humidity = informationElement.getAttribute("value");

							} else if (informationName.equals("clouds")) {
								cloudCoverage = informationElement.getAttribute("all");

							}
						}
					}

					// Create the day object,  this object will automatically convert the string data into
					// the appropriate data types
					DayWeather dayWeather = new DayWeather(dateString,
																								 weatherInfoCode,
																								 windDirectionText,
																								 windDirectionDegrees,
																								 windSpeed,
																								 temperatureDay,
																								 temperatureEvening,
																								 temperatureMorning,
																								 temperatureNight,
																								 temperatureMax,
																								 temperatureMin,
																								 pressure,
																								 humidity,
																								 cloudCoverage);

					// add this day to the list
					weatherDataList.add(dayWeather);
				}
			}

			// return the weather data to the caller
			return weatherDataList;
		} catch (Exception e) {
			System.err.println("unable to load XML: ");
			e.printStackTrace();
		}

		// There was an error parsing so return null
		return null;
	}


	/** Method to weather api specific location code for a given zipcode.
	 *
	 *   @param _zipcode [double], zipcode to lookup.
	 *
	 *   @return [double] weather api location code for the given zipcode.
	 */
	private int getWeatherCode(int _zipcode) {

		// used for reading the .csv files
		BufferedReader bufferedReader = null;

		// latitude and longitude of the zipcode, will stay -1 if zipcode not found
		float locationLatitude = -1;
		float locationLongitude = -1;


		try {

			// Open the csv file
			//InputStream inputStream = this.getClass().getResourceAsStream(ZIPCODE_CSV_FILE);
			InputStream inputStream = new FileInputStream(new File(ZIPCODE_CSV_FILE));
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

			if (bufferedReader != null)
				System.out.println("DEBUG: Zip code file read successfully!");

			// read the .csv file line by line and parse the file
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {

				// split lines on commas, should result in an array of strings of size 3
				String[] splitString = line.split(",");

				// make sure the line has the correct format
				if (splitString.length != 3) {
					continue;
				}

				// parse the line for the individual data pieces
				int zipcodeValue = Integer.parseInt(splitString[0]);
				float latValue = Float.parseFloat(splitString[1]);
				float lonValue = Float.parseFloat(splitString[2]);

				// if the zipcode of this line matches the zipcode that was requested
				if (zipcodeValue == _zipcode) {

					// It does get the lat and long
					locationLatitude = latValue;
					locationLongitude = lonValue;

					// dont need to search anymore since we found the exact zipcode
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {

			// always close the file since we are going to use the buffered reader again
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// if zipcode was not found then will be -1 and we should report an error
		if (locationLatitude == -1) {
			return -1;
		}

		// reset so we can use it again
		bufferedReader = null;

		// used to store the closest location matched since lat and long may not match up exactly
		float closestDistance = 100000;
		int closestWeatherCode = 0;


		try {
			// Open the csv file
			//InputStream inputStream = this.getClass().getResourceAsStream(AREA_CODE_CSV_FILE);
			InputStream inputStream = new FileInputStream(new File(AREA_CODE_CSV_FILE));
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

			if (bufferedReader != null)
				System.out.println("DEBUG: Area code file read successfully!");

			// read the .csv file line by line and parse the file
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {

				// split lines on commas, should result in an array of strings of size 3
				String[] splitString = line.split(",");

				// make sure the line has the correct format
				if (splitString.length != 3) {
					continue;
				}

				// parse the line for the individual data pieces
				int weatheCodeValue = Integer.parseInt(splitString[0]);
				float lonValue = Float.parseFloat(splitString[1]);
				float latValue = Float.parseFloat(splitString[2]);


				// calculate the distance from this lat long from the one matched to the zipcode
				float currDistance = (float)distance(latValue, lonValue, locationLatitude, locationLongitude);

				// of distance is closer
				if (currDistance <= closestDistance) {

					// save this entry
					closestDistance = currDistance;
					closestWeatherCode = weatheCodeValue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {

			// make sure we close the file
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return closestWeatherCode;
	}


	/** Static Method to get distance from 2 latitude and longitude positions.
	 *   Adapted from https://www.geodatasource.com/developers/java
	 *
	 *   @param _lat1   [double], latitude pair 1.
	 *   @param _long1  [double], longitude pair 1.
	 *   @param _lat2   [double], latitude pair 2.
	 *   @param _long2  [double], longitude pair 2.
	 *
	 *   @return [double] distance in miles.
	 */
	private static double distance(double _lat1, double _lon1, double _lat2, double _lon2) {
		double theta = _lon1 - _lon2;
		double dist = Math.sin(deg2rad(_lat1)) * Math.sin(deg2rad(_lat2)) + Math.cos(deg2rad(_lat1)) * Math.cos(deg2rad(_lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		return (dist);
	}


	/** Static Method convert degrees to radians.
	 *   Adapted from https://www.geodatasource.com/developers/java
	 *
	 *   @param _degree  [double], degrees.
	 *
	 *   @return [double] radians value.
	 */
	private static double deg2rad(double _degree) {
		return _degree * 3.141592653589793 / 180.0;
	}


	/** Static Method convert radians to degrees.
	 *   Adapted from https://www.geodatasource.com/developers/java
	 *
	 *   @param _rad  [double], radians.
	 *
	 *   @return [double] degrees value.
	 */
	private static double rad2deg(double _rad) {
		return _rad * 180.0 / 3.141592653589793;
	}



	/*******************************************************************************************************************************************
	**  Main Method used for testing
	*******************************************************************************************************************************************/
/*	public static void main(String[] arrstring) {
		System.out.println("WE ARE RUNNING!");


		try {
			IoTAddress devAddress = new IoTAddress("api.openweathermap.org");
			WeatherGrabber we = new WeatherGrabber(devAddress);

			//List<DayWeather> dw = we.getWeatherData(92130, 16);
			List<DayWeather> dw = we.getWeatherData(92612, 255);


			for (DayWeather day : dw) {
				System.out.println(day);
			}

		} catch (Exception e) {

		}
	}*/
}


