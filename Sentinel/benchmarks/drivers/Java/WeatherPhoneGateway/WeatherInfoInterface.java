package iotcode.WeatherPhoneGateway;

/** WeatherInfoInterface interface to be implemented by a real class
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0                
 * @since       2016-04-26
 */
public interface WeatherInfoInterface {

	/** 
	 * WeatherPhoneGateway takes 4 inputs
	 * - inchesPerWeek (double)
	 * - weatherZipCode (int)
	 * - daysToWaterOn (int)
	 * - inchesPerMinute (double)
	 */
	String getIrrigationInfo(Double dInchesPerWeek, Integer iWeatherZipCode, 
		Integer iDaysToWaterOn, Double dInchesPerMinute);
}
