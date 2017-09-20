package IrrigationController;

// Standard Java Packages
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

/** Class DayWeather to represent the parsed weather data as a compact object.
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-03-24
 */
public class DayWeather {

	// enumeration representing the weather conditions
	public enum WeatherConditions {
		THUNDERSTORM_WITH_LIGHT_RAIN,
		THUNDERSTORM_WITH_RAIN,
		THUNDERSTORM_WITH_HEAVY_RAIN,
		LIGHT_THUNDERSTORM,
		THUNDERSTORM,
		HEAVY_THUNDERSTORM,
		RAGGED_THUNDERSTORM,
		THUNDERSTORM_WITH_LIGHT_DRIZZLE,
		THUNDERSTORM_WITH_DRIZZLE,
		THUNDERSTORM_WITH_HEAVY_DRIZZLE,
		LIGHT_INTENSITY_DRIZZLE,
		DRIZZLE,
		HEAVY_INTENSITY_DRIZZLE,
		LIGHT_INTENSITY_DRIZZLE_RAIN,
		DRIZZLE_RAIN,
		HEAVY_INTENSITY_DRIZZLE_RAIN,
		SHOWER_RAIN_AND_DRIZZLE,
		HEAVY_SHOWER_RAIN_AND_DRIZZLE,
		SHOWER_DRIZZLE,
		LIGHT_RAIN,
		MODERATE_RAIN,
		HEAVY_INTENSITY_RAIN,
		VERY_HEAVY_RAIN,
		EXTREME_RAIN,
		FREEZING_RAIN,
		LIGHT_INTENSITY_SHOWER_RAIN,
		SHOWER_RAIN,
		HEAVY_INTENSITY_SHOWER_RAIN,
		RAGGED_SHOWER_RAIN,
		LIGHT_SNOW,
		SNOW,
		HEAVY_SNOW,
		SLEET,
		SHOWER_SLEET,
		LIGHT_RAIN_AND_SNOW,
		RAIN_AND_SNOW,
		LIGHT_SHOWER_SNOW,
		SHOWER_SNOW,
		HEAVY_SHOWER_SNOW,
		MIST,
		SMOKE,
		HAZE,
		SAND_DUST_WHIRLS,
		FOG,
		SAND,
		DUST,
		VOLCANIC_ASH,
		SQUALLS,
		TORNADO,
		CLEAR_SKY,
		FEW_CLOUDS,
		SCATTERED_CLOUDS,
		BROKEN_CLOUDS,
		OVERCAST_CLOUDS,
		TROPICAL_STORM,
		COLD,
		HOT,
		WINDY,
		HAIL,
		CALM,
		LIGHT_BREEZE,
		GENTLE_BREEZE,
		MODERATE_BREEZE,
		FRESH_BREEZE,
		STRONG_BREEZE,
		HIGH_WIND_NEAR_GALE,
		GALE,
		SEVERE_GALE,
		STORM,
		VIOLENT_STORM,
		HURRICANE,
		UNKNOWN
	};


	/*******************************************************************************************************************************************
	**
	**  Variables
	**
	*******************************************************************************************************************************************/
	private Date date = null;
	private WeatherConditions weatherCondition = WeatherConditions.UNKNOWN;

	// Wind Information
	private String windDirection = "";
	private int windDirectionDegrees = 0;			// In degrees
	private float windSpeed = 0;							// In Miles per hour

	// Temperature Information
	private float dayTemperature = 0;					// In degrees F
	private float eveningTemperature = 0;			// In degrees F
	private float morningTemperature = 0;			// In degrees F
	private float nightTemperature = 0;				// In degrees F
	private float maxTemperature = 0;					// In degrees F
	private float minTemperature = 0;					// In degrees F

	// Other Information
	private float pressure = 0;						// In hPa
	private float humidity = 0;						// In percent
	private float cloudPercentage = 0;		// In percent


	/** Constructor.
	 *
	 *  @param _date                  [String], String representing the date information for this days weather.
	 *  @param _weatherCondition      [String], String representing the weather condition code information for this days weather.
	 *  @param _windDirection         [String], String representing the wind direction in text format information for this days weather.
	 *  @param _windDirectionDegrees  [String], String representing the wind direction in degrees information for this days weather.
	 *  @param _windSpeed             [String], String representing the wind speed information for this days weather.
	 *  @param _dayTemperature        [String], String representing the day temperature information for this days weather.
	 *  @param _eveningTemperature    [String], String representing the evening temperature information for this days weather.
	 *  @param _morningTemperature    [String], String representing the morning temperature information for this days weather.
	 *  @param _nighttemperature      [String], String representing the night temperature information for this days weather.
	 *  @param _maxTemperature        [String], String representing the max temperature information for this days weather.
	 *  @param _minTemperature        [String], String representing the min temperature information for this days weather.
	 *  @param _pressure              [String], String representing the pressure information for this days weather.
	 *  @param _humidity              [String], String representing the humidity percentage information for this days weather.
	 *  @param _cloudPercentage       [String], String representing the cloud coverage percentage information for this days weather.
	 *
	 */
	public DayWeather(String _date,
										String _weatherCondition,
										String _windDirection,
										String _windDirectionDegrees,
										String _windSpeed,
										String _dayTemperature,
										String _eveningTemperature,
										String _morningTemperature,
										String _nighttemperature,
										String _maxTemperature,
										String _minTemperature,
										String _pressure,
										String _humidity,
										String _cloudPercentage) {

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		try {
			date = formatter.parse(_date);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		weatherCondition = getWeatherConditionForCode(Integer.parseInt(_weatherCondition));

		// Wind Variable
		windDirection = _windDirection;
		windDirectionDegrees = Integer.parseInt(_windDirectionDegrees);
		windSpeed = Float.parseFloat(_windSpeed);

		// Temperature Information
		dayTemperature = Float.parseFloat(_dayTemperature);
		eveningTemperature = Float.parseFloat(_eveningTemperature);
		morningTemperature = Float.parseFloat(_morningTemperature);
		nightTemperature = Float.parseFloat(_nighttemperature);
		maxTemperature = Float.parseFloat(_maxTemperature);
		minTemperature = Float.parseFloat(_minTemperature);

		// Other Information
		pressure = Float.parseFloat(_pressure);
		humidity = Float.parseFloat(_humidity);
		cloudPercentage = Float.parseFloat(_cloudPercentage);
	}


	/** Method to get the date for this weather object.
	 *
	 *   @return [Date] Date and time for this weather object.
	 */
	public Date getDate() {
		return date;
	}


	/** Method to get the wind direction text (N,NE,S,EXT).
	 *
	 *   @return [String] Wind direction in compass text.
	 */
	public String getWindDirection() {
		return windDirection;
	}


	/** Method to get the wind direction in degrees.
	 *
	 *   @return [int] Wind direction in degrees.
	 */
	public int getWindDirectionDegrees() {
		return windDirectionDegrees;
	}


	/** Method to get the wind speed in miles per hour.
	 *
	 *   @return [float] Wind speed in miles per hour.
	 */
	public float getWindSpeed() {
		return windSpeed;
	}


	/** Method to get the temperature during the day.
	 *
	 *   @return [float] Temperature in degrees F.
	 */
	public float getDayTemperature() {
		return dayTemperature;
	}

	/** Method to get the temperature during the evening.
	 *
	 *   @return [float] Temperature in degrees F.
	 */
	public float getEveningTemperature() {
		return eveningTemperature;
	}


	/** Method to get the temperature during the morning.
	 *
	 *   @return [float] Temperature in degrees F.
	 */
	public float getMorningTemperature() {
		return morningTemperature;
	}


	/** Method to get the temperature during the night.
	 *
	 *   @return [float] Temperature in degrees F.
	 */
	public float getNighttemperature() {
		return nightTemperature;
	}


	/** Method to get the max temperature.
	 *
	 *   @return [float] Temperature in degrees F.
	 */
	public float getMaxTemperature() {
		return maxTemperature;
	}


	/** Method to get the min temperature.
	 *
	 *   @return [float] Temperature in degrees F.
	 */
	public float getMinTemperature() {
		return minTemperature;
	}


	/** Method to get the pressure.
	 *
	 *   @return [float] Pressure in hPa.
	 */
	public float getPressure() {
		return pressure;
	}


	/** Method to get the humidity.
	 *
	 *   @return [float] Humidity percentage.
	 */
	public float getHumidity() {
		return humidity;
	}


	/** Method to get the cloud coverage percentage.
	 *
	 *   @return [float] Cloud coverage percentage.
	 */
	public float getCloudPercentage() {
		return cloudPercentage;
	}


	/** Method to check if this day is a wet day such as rain or something
	 *
	 *   @return [float] Cloud coverage percentage.
	 */
	public boolean getIsWetDay() {
		if ((weatherCondition == WeatherConditions.THUNDERSTORM_WITH_LIGHT_RAIN) ||
				(weatherCondition == WeatherConditions.THUNDERSTORM_WITH_RAIN) ||
				(weatherCondition == WeatherConditions.THUNDERSTORM_WITH_HEAVY_RAIN) ||
				(weatherCondition == WeatherConditions.LIGHT_THUNDERSTORM) ||
				(weatherCondition == WeatherConditions.THUNDERSTORM) ||
				(weatherCondition == WeatherConditions.HEAVY_THUNDERSTORM) ||
				(weatherCondition == WeatherConditions.RAGGED_THUNDERSTORM) ||
				(weatherCondition == WeatherConditions.THUNDERSTORM_WITH_LIGHT_DRIZZLE) ||
				(weatherCondition == WeatherConditions.THUNDERSTORM_WITH_DRIZZLE) ||
				(weatherCondition == WeatherConditions.THUNDERSTORM_WITH_HEAVY_DRIZZLE) ||
				(weatherCondition == WeatherConditions.LIGHT_INTENSITY_DRIZZLE) ||
				(weatherCondition == WeatherConditions.DRIZZLE) ||
				(weatherCondition == WeatherConditions.HEAVY_INTENSITY_DRIZZLE) ||
				(weatherCondition == WeatherConditions.LIGHT_INTENSITY_DRIZZLE_RAIN) ||
				(weatherCondition == WeatherConditions.DRIZZLE_RAIN) ||
				(weatherCondition == WeatherConditions.HEAVY_INTENSITY_DRIZZLE_RAIN) ||
				(weatherCondition == WeatherConditions.SHOWER_RAIN_AND_DRIZZLE) ||
				(weatherCondition == WeatherConditions.HEAVY_SHOWER_RAIN_AND_DRIZZLE) ||
				(weatherCondition == WeatherConditions.SHOWER_DRIZZLE) ||
				(weatherCondition == WeatherConditions.LIGHT_RAIN) ||
				(weatherCondition == WeatherConditions.MODERATE_RAIN) ||
				(weatherCondition == WeatherConditions.HEAVY_INTENSITY_RAIN) ||
				(weatherCondition == WeatherConditions.VERY_HEAVY_RAIN) ||
				(weatherCondition == WeatherConditions.EXTREME_RAIN) ||
				(weatherCondition == WeatherConditions.FREEZING_RAIN) ||
				(weatherCondition == WeatherConditions.LIGHT_INTENSITY_SHOWER_RAIN) ||
				(weatherCondition == WeatherConditions.SHOWER_RAIN) ||
				(weatherCondition == WeatherConditions.HEAVY_INTENSITY_SHOWER_RAIN) ||
				(weatherCondition == WeatherConditions.RAGGED_SHOWER_RAIN) ||
				(weatherCondition == WeatherConditions.SHOWER_SLEET) ||
				(weatherCondition == WeatherConditions.LIGHT_RAIN_AND_SNOW) ||
				(weatherCondition == WeatherConditions.RAIN_AND_SNOW) ||
				(weatherCondition == WeatherConditions.LIGHT_SHOWER_SNOW) ||
				(weatherCondition == WeatherConditions.SHOWER_SNOW) ||
				(weatherCondition == WeatherConditions.HEAVY_SHOWER_SNOW) ||
				(weatherCondition == WeatherConditions.TORNADO) ||
				(weatherCondition == WeatherConditions.TROPICAL_STORM) ||
				(weatherCondition == WeatherConditions.WINDY) ||
				(weatherCondition == WeatherConditions.HAIL) ||
				(weatherCondition == WeatherConditions.STORM) ||
				(weatherCondition == WeatherConditions.VIOLENT_STORM) ||
				(weatherCondition == WeatherConditions.HURRICANE)) {
			return true;
		}
		return false;
	}


	/** Method to get the string representation of this object.
	 *
	 *   @return [String] String representation of this object.
	 */
	public String toString() {
		String retString = "";
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		retString += "Forcast for day: " + df.format(date) + "\n";
		retString += "\t" + "Weather Condition:" + "\n";
		retString += "\t\t" + "Weather Condition: " + getStringforWeatherCondition(weatherCondition) + "\n";

		// Wind information to string
		retString += "\t" + "Wind Information:" + "\n";
		retString += "\t\t" + "Wind Direction (Text)   : " + windDirection + "\n";
		retString += "\t\t" + "Wind Direction (Degrees): " + Integer.toString(windDirectionDegrees) + "\n";
		retString += "\t\t" + "Wind Speed (mph)        : " + Float.toString(windSpeed) + "\n";

		// Temperature information to string
		retString += "\t" + "Temperature Information:" + "\n";
		retString += "\t\t" + "Day Temperature (Degrees F)     : " + Float.toString(dayTemperature) + "\n";
		retString += "\t\t" + "Evening Temperature (Degrees F) : " + Float.toString(eveningTemperature) + "\n";
		retString += "\t\t" + "Morning Temperature (Degrees F) : " + Float.toString(morningTemperature) + "\n";
		retString += "\t\t" + "Night Temperature (Degrees F)   : " + Float.toString(nightTemperature) + "\n";
		retString += "\t\t" + "Max Temperature (Degrees F)     : " + Float.toString(maxTemperature) + "\n";
		retString += "\t\t" + "Min Temperature (Degrees F)     : " + Float.toString(minTemperature) + "\n";

		// Other information to string
		retString += "\t" + "Other Information:" + "\n";
		retString += "\t\t" + "Pressure (hPa)     : " + Float.toString(pressure) + "\n";
		retString += "\t\t" + "Humidity (Percentage)     : " + Float.toString(humidity) + "\n";
		retString += "\t\t" + "Cloud Coverage (Percentage)     : " + Float.toString(cloudPercentage) + "\n";


		return retString;
	}


	/*******************************************************************************************************************************************
	**
	**  Helper Methods
	**
	*******************************************************************************************************************************************/

	/** Method get the string representation of the weather condition for display.
	 *
	 *   @param cond [WeatherConditions], weather condition code.
	 *
	 *   @return [String] String representation of the weather condition.
	 */
	private String getStringforWeatherCondition(WeatherConditions cond) {
		switch (cond) {
		case THUNDERSTORM_WITH_LIGHT_RAIN:
			return "THUNDERSTORM_WITH_LIGHT_RAIN";

		case THUNDERSTORM_WITH_RAIN:
			return "THUNDERSTORM_WITH_RAIN";

		case THUNDERSTORM_WITH_HEAVY_RAIN:
			return "THUNDERSTORM_WITH_HEAVY_RAIN";

		case LIGHT_THUNDERSTORM:
			return "LIGHT_THUNDERSTORM";

		case THUNDERSTORM:
			return "THUNDERSTORM";

		case HEAVY_THUNDERSTORM:
			return "HEAVY_THUNDERSTORM";

		case RAGGED_THUNDERSTORM:
			return "RAGGED_THUNDERSTORM";

		case THUNDERSTORM_WITH_LIGHT_DRIZZLE:
			return "THUNDERSTORM_WITH_LIGHT_DRIZZLE";

		case THUNDERSTORM_WITH_DRIZZLE:
			return "THUNDERSTORM_WITH_DRIZZLE";

		case THUNDERSTORM_WITH_HEAVY_DRIZZLE:
			return "THUNDERSTORM_WITH_HEAVY_DRIZZLE";

		case LIGHT_INTENSITY_DRIZZLE:
			return "LIGHT_INTENSITY_DRIZZLE";

		case DRIZZLE:
			return "DRIZZLE";

		case HEAVY_INTENSITY_DRIZZLE:
			return "HEAVY_INTENSITY_DRIZZLE";

		case LIGHT_INTENSITY_DRIZZLE_RAIN:
			return "LIGHT_INTENSITY_DRIZZLE_RAIN";

		case DRIZZLE_RAIN:
			return "DRIZZLE_RAIN";

		case HEAVY_INTENSITY_DRIZZLE_RAIN:
			return "HEAVY_INTENSITY_DRIZZLE_RAIN";

		case SHOWER_RAIN_AND_DRIZZLE:
			return "SHOWER_RAIN_AND_DRIZZLE";

		case HEAVY_SHOWER_RAIN_AND_DRIZZLE:
			return "HEAVY_SHOWER_RAIN_AND_DRIZZLE";

		case SHOWER_DRIZZLE:
			return "SHOWER_DRIZZLE";

		case LIGHT_RAIN:
			return "LIGHT_RAIN";

		case MODERATE_RAIN:
			return "MODERATE_RAIN";

		case HEAVY_INTENSITY_RAIN:
			return "HEAVY_INTENSITY_RAIN";

		case VERY_HEAVY_RAIN:
			return "VERY_HEAVY_RAIN";

		case EXTREME_RAIN:
			return "EXTREME_RAIN";

		case FREEZING_RAIN:
			return "FREEZING_RAIN";

		case LIGHT_INTENSITY_SHOWER_RAIN:
			return "LIGHT_INTENSITY_SHOWER_RAIN";

		case SHOWER_RAIN:
			return "SHOWER_RAIN";

		case HEAVY_INTENSITY_SHOWER_RAIN:
			return "HEAVY_INTENSITY_SHOWER_RAIN";

		case RAGGED_SHOWER_RAIN:
			return "RAGGED_SHOWER_RAIN";

		case LIGHT_SNOW:
			return "LIGHT_SNOW";

		case SNOW:
			return "SNOW";

		case HEAVY_SNOW:
			return "HEAVY_SNOW";

		case SLEET:
			return "SLEET";

		case SHOWER_SLEET:
			return "SHOWER_SLEET";

		case LIGHT_RAIN_AND_SNOW:
			return "LIGHT_RAIN_AND_SNOW";

		case RAIN_AND_SNOW:
			return "RAIN_AND_SNOW";

		case LIGHT_SHOWER_SNOW:
			return "LIGHT_SHOWER_SNOW";

		case SHOWER_SNOW:
			return "SHOWER_SNOW";

		case HEAVY_SHOWER_SNOW:
			return "HEAVY_SHOWER_SNOW";

		case MIST:
			return "MIST";

		case SMOKE:
			return "SMOKE";

		case HAZE:
			return "HAZE";

		case SAND_DUST_WHIRLS:
			return "SAND_DUST_WHIRLS";

		case FOG:
			return "FOG";

		case SAND:
			return "SAND";

		case DUST:
			return "DUST";

		case VOLCANIC_ASH:
			return "VOLCANIC_ASH";

		case SQUALLS:
			return "SQUALLS";

		case TORNADO:
			return "TORNADO";

		case CLEAR_SKY:
			return "CLEAR_SKY";

		case FEW_CLOUDS:
			return "FEW_CLOUDS";

		case SCATTERED_CLOUDS:
			return "SCATTERED_CLOUDS";

		case BROKEN_CLOUDS:
			return "BROKEN_CLOUDS";

		case OVERCAST_CLOUDS:
			return "OVERCAST_CLOUDS";

		case TROPICAL_STORM:
			return "TROPICAL_STORM";

		case HURRICANE:
			return "HURRICANE";

		case COLD:
			return "COLD";

		case HOT:
			return "HOT";

		case WINDY:
			return "WINDY";

		case HAIL:
			return "HAIL";

		case CALM:
			return "CALM";

		case LIGHT_BREEZE:
			return "LIGHT_BREEZE";

		case GENTLE_BREEZE:
			return "GENTLE_BREEZE";

		case MODERATE_BREEZE:
			return "MODERATE_BREEZE";

		case FRESH_BREEZE:
			return "FRESH_BREEZE";

		case STRONG_BREEZE:
			return "STRONG_BREEZE";

		case HIGH_WIND_NEAR_GALE:
			return "HIGH_WIND_NEAR_GALE";

		case GALE:
			return "GALE";

		case SEVERE_GALE:
			return "SEVERE_GALE";

		case STORM:
			return "STORM";

		case VIOLENT_STORM:
			return "VIOLENT_STORM";

		default:
			return "UNKNOWN";
		}
	}


	/** Method to change the weather condition code into a WeatherConditions enum type
	 *
	 *   @param cond [int], weather condition code.
	 *
	 *   @return [WeatherConditions] weather conditions enum type.
	 */
	private WeatherConditions getWeatherConditionForCode(int code) {
		switch (code) {
		case 200:
			return WeatherConditions.THUNDERSTORM_WITH_LIGHT_RAIN;

		case 201:
			return WeatherConditions.THUNDERSTORM_WITH_RAIN;

		case 202:
			return WeatherConditions.THUNDERSTORM_WITH_HEAVY_RAIN;

		case 210:
			return WeatherConditions.LIGHT_THUNDERSTORM;

		case 211:
			return WeatherConditions.THUNDERSTORM;

		case 212:
			return WeatherConditions.HEAVY_THUNDERSTORM;

		case 221:
			return WeatherConditions.RAGGED_THUNDERSTORM;

		case 230:
			return WeatherConditions.THUNDERSTORM_WITH_LIGHT_DRIZZLE;

		case 231:
			return WeatherConditions.THUNDERSTORM_WITH_DRIZZLE;

		case 232:
			return WeatherConditions.THUNDERSTORM_WITH_HEAVY_DRIZZLE;

		case 300:
			return WeatherConditions.LIGHT_INTENSITY_DRIZZLE;

		case 301:
			return WeatherConditions.DRIZZLE;

		case 302:
			return WeatherConditions.HEAVY_INTENSITY_DRIZZLE;

		case 310:
			return WeatherConditions.LIGHT_INTENSITY_DRIZZLE_RAIN;

		case 311:
			return WeatherConditions.DRIZZLE_RAIN;

		case 312:
			return WeatherConditions.HEAVY_INTENSITY_DRIZZLE_RAIN;

		case 313:
			return WeatherConditions.SHOWER_RAIN_AND_DRIZZLE;

		case 314:
			return WeatherConditions.HEAVY_SHOWER_RAIN_AND_DRIZZLE;

		case 321:
			return WeatherConditions.SHOWER_DRIZZLE;

		case 500:
			return WeatherConditions.LIGHT_RAIN;

		case 501:
			return WeatherConditions.MODERATE_RAIN;

		case 502:
			return WeatherConditions.HEAVY_INTENSITY_RAIN;

		case 503:
			return WeatherConditions.VERY_HEAVY_RAIN;

		case 504:
			return WeatherConditions.EXTREME_RAIN;

		case 511:
			return WeatherConditions.FREEZING_RAIN;

		case 520:
			return WeatherConditions.LIGHT_INTENSITY_SHOWER_RAIN;

		case 521:
			return WeatherConditions.SHOWER_RAIN;

		case 522:
			return WeatherConditions.HEAVY_INTENSITY_SHOWER_RAIN;

		case 531:
			return WeatherConditions.RAGGED_SHOWER_RAIN;

		case 600:
			return WeatherConditions.LIGHT_SNOW;

		case 601:
			return WeatherConditions.SNOW;

		case 602:
			return WeatherConditions.HEAVY_SNOW;

		case 611:
			return WeatherConditions.SLEET;

		case 612:
			return WeatherConditions.SHOWER_SLEET;

		case 615:
			return WeatherConditions.LIGHT_RAIN_AND_SNOW;

		case 616:
			return WeatherConditions.RAIN_AND_SNOW;

		case 620:
			return WeatherConditions.LIGHT_SHOWER_SNOW;

		case 621:
			return WeatherConditions.SHOWER_SNOW;

		case 622:
			return WeatherConditions.HEAVY_SHOWER_SNOW;

		case 701:
			return WeatherConditions.MIST;

		case 711:
			return WeatherConditions.SMOKE;

		case 721:
			return WeatherConditions.HAZE;

		case 731:
			return WeatherConditions.SAND_DUST_WHIRLS;

		case 741:
			return WeatherConditions.FOG;

		case 751:
			return WeatherConditions.SAND;

		case 761:
			return WeatherConditions.DUST;

		case 762:
			return WeatherConditions.VOLCANIC_ASH;

		case 771:
			return WeatherConditions.SQUALLS;

		case 781:
			return WeatherConditions.TORNADO;

		case 800:
			return WeatherConditions.CLEAR_SKY;

		case 801:
			return WeatherConditions.FEW_CLOUDS;

		case 802:
			return WeatherConditions.SCATTERED_CLOUDS;

		case 803:
			return WeatherConditions.BROKEN_CLOUDS;

		case 804:
			return WeatherConditions.OVERCAST_CLOUDS;

		case 900:
			return WeatherConditions.TORNADO;

		case 901:
			return WeatherConditions.TROPICAL_STORM;

		case 902:
			return WeatherConditions.HURRICANE;

		case 903:
			return WeatherConditions.COLD;

		case 904:
			return WeatherConditions.HOT;

		case 905:
			return WeatherConditions.WINDY;

		case 906:
			return WeatherConditions.HAIL;

		case 951:
			return WeatherConditions.CALM;

		case 952:
			return WeatherConditions.LIGHT_BREEZE;

		case 953:
			return WeatherConditions.GENTLE_BREEZE;

		case 954:
			return WeatherConditions.MODERATE_BREEZE;

		case 955:
			return WeatherConditions.FRESH_BREEZE;

		case 956:
			return WeatherConditions.STRONG_BREEZE;

		case 957:
			return WeatherConditions.HIGH_WIND_NEAR_GALE;

		case 958:
			return WeatherConditions.GALE;

		case 959:
			return WeatherConditions.SEVERE_GALE;

		case 960:
			return WeatherConditions.STORM;

		case 961:
			return WeatherConditions.VIOLENT_STORM;

		case 962:
			return WeatherConditions.HURRICANE;

		default:
			return WeatherConditions.UNKNOWN;
		}
	}
}