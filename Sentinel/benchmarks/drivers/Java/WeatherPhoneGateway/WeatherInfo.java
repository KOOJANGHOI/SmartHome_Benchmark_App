package iotcode.WeatherPhoneGateway;

/** WeatherInfo that implements WeatherInfoInterface
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0                
 * @since       2016-04-26
 */
public class WeatherInfo implements WeatherInfoInterface {

	/**
	 * WeatherInfo class properties
	 */
	private Double dInchesPerWeek;
	private Integer iWeatherZipCode;
	private Integer iDaysToWaterOn;
	private Double dInchesPerMinute;
	private boolean bNewDataAvailable;

	/**
	 * Constructor
	 */
	public WeatherInfo() {
		this.dInchesPerWeek = 0.0;
		this.iWeatherZipCode = 0;
		this.iDaysToWaterOn = 0;
		this.dInchesPerMinute = 0.0;
		this.bNewDataAvailable = false;
	}

	/**
	 * Get irrigation info from the phone app using IoTRemoteCall
	 *
	 * @param   dInchesPerWeek		Rainfall information (inches per week)
	 * @param   iWeatherZipCode		Area zip code for weather info
	 * @param   iDaysToWaterOn		Number of days to water the lawn
	 * @param   dInchesPerMinute	Rainfall information (inches per minute)
	 * @
	 */
	public String getIrrigationInfo(Double dInchesPerWeek, Integer iWeatherZipCode,
		Integer iDaysToWaterOn, Double dInchesPerMinute) {

		this.dInchesPerWeek = dInchesPerWeek;
		this.iWeatherZipCode = iWeatherZipCode;
		this.iDaysToWaterOn = iDaysToWaterOn;
		this.dInchesPerMinute = dInchesPerMinute;
		this.bNewDataAvailable = true;
		System.out.println("DEBUG: We are getting data from phone!");
		System.out.println("DEBUG: New data available?" + bNewDataAvailable);
		
		return "info sent";
	}

	/**
	 * Simply return this.dInchesPerWeek
	 */
	public Double getInchesPerWeek() {

		return this.dInchesPerWeek;
	}

	/**
	 * Simply return this.iWeatherZipCode
	 */
	public Integer getWeatherZipCode() {

		return this.iWeatherZipCode;
	}

	/**
	 * Simply return this.iDaysToWaterOn
	 */
	public Integer getDaysToWaterOn() {

		return this.iDaysToWaterOn;
	}

	/**
	 * Simply return this.dInchesPerMinute
	 */
	public Double getInchesPerMinute() {

		return this.dInchesPerMinute;
	}

	/**
	 * Simply return this.bNewDataAvailable
	 */
	public boolean isNewDataAvailable() {

		return this.bNewDataAvailable;
	}

	/**
	 * Set this.bNewDataAvailable
	 */
	public void setNewDataAvailable(boolean bValue) {

		this.bNewDataAvailable = bValue;
	}
}
