package IrrigationController;
// Standard Java Packages
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;


// RMI packages
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// IoT Runtime Packages
import iotruntime.slave.IoTSet;
import iotruntime.slave.IoTRelation;
import iotruntime.slave.IoTAddress;
import iotcode.annotation.*;

// IoT Driver Packages
import iotcode.interfaces.*;
import iotcode.WeatherPhoneGateway.*;

public class IrrigationController extends UnicastRemoteObject implements WeatherGatewayCallback {


	/*******************************************************************************************************************************************
	**
	**  Constants
	**
	*******************************************************************************************************************************************/
	// private static final int NUMBER_OF_TIMES_PER_WEEK_TO_WATER = 2;
	//TODO: Change these back to normal - this is just for testing to make it awake all the time
//	private static final int TIME_HOURS_TO_WATER_GRASS = 7;		// 7 am
	private static final int TIME_HOURS_TO_WATER_GRASS = 3;
//	private static final int TIME_MINUTES_TO_WATER_GRASS = 30;	// 30 minutes
	private static final int TIME_MINUTES_TO_WATER_GRASS = 30;
//	private static final int TIME_TO_RECOVER_GRASS_FOR = 8 * 24 * 60 * 60;	// 8 days
	private static final int TIME_TO_RECOVER_GRASS_FOR = 10;
//	private static final int TIME_TO_HIBERNATE_GRASS_FOR = 30 * 24 * 60 * 60;		// 30 days
	private static final int TIME_TO_HIBERNATE_GRASS_FOR = 10;
	public static final int CAMERA_FPS = 15;													// In frames per second


	/*******************************************************************************************************************************************
	**
	**  Variables
	**
	*******************************************************************************************************************************************/
	private int dayOfLastCheck = -1;
	private int monthOfLastCheck = -1;
	private boolean isInHibernationRecoveryMode = false;
	private Date hibernationRecoveryModeStartDate = null;
	private boolean isHibernationMode = false;
	private Date hibernationModeStartDate = null;
	private List<LawnState> lawns = new ArrayList<LawnState>();
	private WeatherGrabber weatherGrabber = null;

	// used to block until gui is done and the settings are ready to be polled
	private AtomicBoolean waitingForInterface = new AtomicBoolean(true);

	// the settings from the interface, used to setup the system
	private double inchesPerWeek = 0;
	private int weatherZipCode = 0;
	private int daysToWaterOn = 0;
	private List<Double> inchesPerMinute;

	private static int sensorId = 0;

	/*******************************************************************************************************************************************
	**
	**  IoT Sets and Relations
	**
	*******************************************************************************************************************************************/
	@config private IoTSet<IoTAddress> weatherDataAddresses;
	@config private IoTSet<IoTAddress> weatherDataAddressMain;
	@config private IoTSet<WeatherGatewaySmart> gwSet;
	@config private IoTSet<LawnSmart> lawnSet;
	@config private IoTSet<MoistureSensorSmart> moistureSensorsSet;
	@config private IoTSet<CameraSmart> cameraSet;
	@config private IoTRelation<LawnSmart, CameraSmart> lawnCameraRelation;
	@config private IoTRelation<LawnSmart, SprinklerSmart> lawnSprinklerRelation;
	@config private IoTRelation<LawnSmart, MoistureSensorSmart> lawnMoistureSensorRelation;


	public IrrigationController() throws RemoteException {

	}

	/*******************************************************************************************************************************************
	**
	**  Public Methods
	**
	*******************************************************************************************************************************************/


	/** Method to set whether the controller should maintain the lawns in hibernation mode
	 *   or in normal mode.  Lawns should be put in hibernation mode in drought conditions
	 *
	 *   @param _hibMode [boolean] set the hibernation mode for this lawn controllers (true = hibernation)
	 *
	 *   @return [void] None.
	 */
	public void setHibernationMode(boolean _hibMode) {

		// change hibernation mode status
		isHibernationMode = _hibMode;

		// set the start date for when we started this hibernation mode
		if (_hibMode) {

			// make sure we dont reset this cycle
			if (!isHibernationMode) {
				hibernationModeStartDate = new Date();
			}
		} else {
			// reset all hibernation stuff
			hibernationModeStartDate = null;
			isInHibernationRecoveryMode = false;
			hibernationRecoveryModeStartDate = null;
		}
	}

	/** Method to start the controller and run the main control loop
	 *
	 *   @return [void] None.
	 */
	public void init() throws RemoteException {

		// initialize the controller
		initController();
		System.out.println("Initialized controller!");

		// Main Loop
		while (true) {

			// get the current time of day (date and time)
			Date currentDate = new Date();

			// get the epoch time till the beginning of the day
			Date beginingOfToday = new Date(currentDate.getYear(), currentDate.getMonth(), currentDate.getDate());

			// calculate the seconds since the start of the day.
			long secondsSinceStartOfDay = (currentDate.getTime() - beginingOfToday.getTime()) / 1000;

			// Seconds since the start of the day to start the watering
			long secondsForWateringStart = (TIME_HOURS_TO_WATER_GRASS * 3600) + (TIME_MINUTES_TO_WATER_GRASS * 60);

//			System.out.println("beginingOfToday " + beginingOfToday);
//			System.out.println("secondsSinceStartOfDay " + secondsSinceStartOfDay);
//			System.out.println("secondsForWateringStart " + secondsForWateringStart);

			// check if the current time is within the start watering interval
			/*if ((secondsSinceStartOfDay < secondsForWateringStart) || (secondsSinceStartOfDay > (secondsForWateringStart + (60 * 60)))) {
				System.out.println("Sleep for 10 minutes.. ");
				try {
					//Thread.sleep(10 * 60 * 1000);						// sleep for 10 minutes
					Thread.sleep(10);						// sleep for 10 seconds
				} catch (Exception e) {
					e.printStackTrace();
				}

				continue;
			}*/

			// check if we already checked if we should water today
			// we only need to do this once per day
			/*if ((dayOfLastCheck == currentDate.getDate()) && (monthOfLastCheck == currentDate.getMonth())) {
				System.out.println("Sleep for 1 hour...");
				try {
					Thread.sleep(60 * 60 * 1000);						// sleep for an hour
				} catch (Exception e) {
					e.printStackTrace();
				}

				continue;
			}*/

			// we decided to check if we should water today so save the fact that we chose to water on this day
			dayOfLastCheck = currentDate.getDate();
			monthOfLastCheck = currentDate.getMonth();

			// update the lawn states everyday
			for (LawnState ls : lawns) {
				ls.updateLawn(currentDate);
			}
			// check if we are in hibernation mode and do the correct loop action
			if (isHibernationMode) {
//				System.out.println("Hibernation mode!");
				// If we are in hibernation mode then use the hibernation loop code
				wateringHibernationLoop(currentDate);
			} else {
//				System.out.println("Normal mode!");
				// Using the normal watering loop code
				wateringNormalLoop(currentDate);
			}
		}
	}


	/** Callback method for when the information is retrieved.
	 *
	 * @param _inchesPerWeek [double].
	 * @param _weatherZipCode [int].
	 * @param _daysToWaterOn [int].
	 * @param _inchesPerMinute [double].
	 * @return [void] None.
	 */
	public void informationRetrieved(double _inchesPerWeek, int _weatherZipCode, int _daysToWaterOn, double _inchesPerMinute) {

		System.out.println("DEBUG: Information is retrieved from phone!!!");
		/*try {
			// get the parameters that the interface (phone app) reads from the user
			inchesPerWeek = _wgw.getInchesPerWeek();
			weatherZipCode = _wgw.getWeatherZipCode();
			daysToWaterOn = _wgw.getDaysToWaterOn();
			inchesPerMinute.add(_wgw.getInchesPerMinute());
		} catch(RemoteException ex) {
			ex.printStackTrace();
		}*/

		inchesPerWeek = _inchesPerWeek;
		weatherZipCode = _weatherZipCode;
		daysToWaterOn = _daysToWaterOn;
		inchesPerMinute.add(_inchesPerMinute);

		// the gui is done so release the spin wait that was waiting for the gui
		waitingForInterface.set(false);
	}

	/*******************************************************************************************************************************************
	**
	**  Helper Methods
	**
	*******************************************************************************************************************************************/


	/** Method to initialize the controller variables and all the drivers and such
	 *
	 *   @return [void] None.
	 */
	private void initController() throws RemoteException {

		// Setup the weather grabber object with the correct address of the weather api
		Iterator it = weatherDataAddresses.iterator();
		weatherGrabber = new WeatherGrabber((IoTAddress)it.next());

		// Initialize inchesPerMinute
		inchesPerMinute = new ArrayList<Double>();

		// We setup a Gateway object to get information from the phone app
		for (WeatherGatewaySmart gw : gwSet.values()) {
			gw.init();
			gw.registerCallback(this);
			gw.start();
		}

		System.out.println("DEBUG: Waiting for phone to send weather information");
		while (waitingForInterface.get()) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// TODO: Use a phone input interface later
		//inchesPerWeek = 20.00;
		//weatherZipCode = 92612;
		//daysToWaterOn = 255;
		//inchesPerMinute.add(1.50);

		System.out.println("DEBUG: inchesPerWeek: " + inchesPerWeek);
		System.out.println("DEBUG: weatherZipCode: " + weatherZipCode);
		System.out.println("DEBUG: daysToWaterOn: " + daysToWaterOn);
		System.out.println("DEBUG: inchesPerMinute: " + inchesPerMinute.get(0));

		// set the zip code and the the number of days of the weather grabber
		// here the number of days is set to the max that the grabber supports
		weatherGrabber.setZipcode(weatherZipCode);
		weatherGrabber.setNumberOfDays(16);

		// Setup the cameras, start them all and assign each one a motion detector
		for (CameraSmart cam : cameraSet.values()) {
			//try {
				// initialize the camera, might need to setup some stuff internally
				cam.init();

				// set the camera parameters.
				cam.setFPS(CAMERA_FPS);
				cam.setResolution(Resolution.RES_VGA);

				// Start the camera (example is start the HTTP stream if it is a network camera)
				cam.start();
				System.out.println("DEBUG: Init camera! " + cam.toString());
			//} catch (RemoteException e) {
			//	e.printStackTrace();
			//}

		}

		// counter so that we can match the lawn inches per min data with the specific lawn
		int counter = 0;
		for (LawnSmart l : lawnSet.values()) {
			// create a motionDetector for each lawn object
			MotionDetection mo = new MotionDetection(12, 0.5f, 10, 10);

			// for 1 camera, if there are any then register the camera for that lawn
			HashSet<CameraSmart> cameras = lawnCameraRelation.get(l);
			System.out.println("DEBUG: Camera.size(): " + cameras.size());
			if (cameras.size() >= 1) {

				// we only need 1 camera per lawn so get the first one in the list
				Iterator camIt = cameras.iterator();
				CameraSmart cam = (CameraSmart)camIt.next();
				System.out.println("DEBUG: Registering callback to camera: " + cam.toString());
				//try {
					// setup the callback
					cam.registerCallback(mo);
				//} catch (RemoteException e) {
				//	e.printStackTrace();
				//}
			}

			// we also only need 1 sprinkler controller per lawn so grab the first one
			HashSet<SprinklerSmart> sprinklers = lawnSprinklerRelation.get(l);
			Iterator sprinklersIt = sprinklers.iterator();
			SprinklerSmart spr = (SprinklerSmart)sprinklersIt.next();

			// init the sprinkler controller, do it here since it only needs to be done once per controller
			try {
				spr.init();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("DEBUG: Init sprinkler: " + spr.toString());

			// get and init the moisture sensors for this specific lawn
			HashSet<MoistureSensorSmart> sensors = lawnMoistureSensorRelation.get(l);
			for (MoistureSensorSmart sen : sensors) {
				System.out.println("DEBUG: Init sensors: " + sen.toString());
				try {
					sen.init();
					sen.setId(sensorId++);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// create the lawn objects
			System.out.println("DEBUG: Creating a LawnState object");
			LawnState ls = 
				new LawnState(l, daysToWaterOn, mo, inchesPerMinute.get(counter), inchesPerWeek, spr, counter, sensors);
			lawns.add(ls);

			// dont forget to increment the counter
			counter++;
		}
	}

	/** Main loop for when the controller is watering the lawns under normal conditions, not in hibernation mode
	 *
	 *   @param _currentDate [Date] current date
	 *
	 *   @return [void] None.
	 */
	private void wateringNormalLoop(Date _currentDate) {

		// get the weather data for the next little bit
		List<DayWeather> weatherData = weatherGrabber.getWeatherData();
		// TODO: Replace this with the WeatherGrabber.getWeatherData() above
//		List<DayWeather> weatherData = new ArrayList<DayWeather>();

		// Go through each lawn and check if we should water it and if we should, water it
		for (LawnState ls : lawns) {

			// water for specific lawn
			waterLawn(ls, _currentDate, weatherData);
		}
	}

	/** Main loop for when the controller is watering the lawns in hibernation mode
	 *
	 *   @param _currentDate [Date] current date
	 *
	 *   @return [void] None.
	 */
	private void wateringHibernationLoop(Date _currentDate) {

		// if we are in recovery mode then run the recovery action
		// we are still in hibernation mode but we need to recover the grass
		if (isInHibernationRecoveryMode) {
//			System.out.println("DEBUG: Recovery mode!");
			hibernationRecoveryLoop(_currentDate);
			return;
		}

		// check if we should enter recovery mode
		long elapsedTime = (_currentDate.getTime() - hibernationModeStartDate.getTime()) / 1000;
		if (elapsedTime >= TIME_TO_HIBERNATE_GRASS_FOR) {

			// start recovery mode
			isInHibernationRecoveryMode = true;
			hibernationRecoveryModeStartDate = null;
//			System.out.println("DEBUG: We enter recovery mode for the first time!");
			// first cycle of recovery
			hibernationRecoveryLoop(_currentDate);
			return;
		}

		// get the weather data for the next little bit
		List<DayWeather> weatherData = weatherGrabber.getWeatherData();

		// Go through each lawn and check if we should water it and if we should, water it
		for (LawnState ls : lawns) {

			boolean lawnHasMotion = ls.lawnHasSufficientMotion();

			// there is no motion on the lawn so no need to water it
			if (!lawnHasMotion) {
				continue;
			}
//			System.out.println("DEBUG: We water the lawn! (wateringHibernationLoop)");
			// water specific lawn since it has motion
			waterLawn(ls, _currentDate, weatherData);
		}
	}


	/** Main loop for when the controller is watering the lawns in hibernation mode
	 *
	 *   @param _currentDate [Date] current date
	 *
	 *   @return [void] None.
	 */
	private void hibernationRecoveryLoop(Date _currentDate) {

		// start recovery mode if it wasnt started yet
		if (hibernationRecoveryModeStartDate == null) {
			hibernationRecoveryModeStartDate = _currentDate;
		}

		// time since this mode was started
		long elapsedTime = (_currentDate.getTime() - hibernationRecoveryModeStartDate.getTime()) / 1000;

		// we have been in recovery mode long enough
		if (elapsedTime >= TIME_TO_RECOVER_GRASS_FOR) {

//			System.out.println("DEBUG: We have been in recovery mode long enough!");
			// reset the recovery mode
			isInHibernationRecoveryMode = false;
			hibernationRecoveryModeStartDate = null;

			// revived grass so restart the grass hibernation cycle
			hibernationModeStartDate = _currentDate;

			// do the hibernation loop since we are no longer in recovery mode
			wateringHibernationLoop(_currentDate);
			return;
		}


		// if we got here then we are trying to recover the grass

		// get the weather data for the next little bit
		List<DayWeather> weatherData = weatherGrabber.getWeatherData();

		// Go through each lawn and check if we should water it and if we should, water it
		for (LawnState ls : lawns) {

//			System.out.println("DEBUG: We water the lawn! (hibernationRecoveryLoop)");
			// water specific lawn since it has motion
			waterLawn(ls, _currentDate, weatherData);
		}

	}


	/** Method for watering a specific lawn if it needs to be watered
	 *
	 *   @param _ls [LawnState] lawn to water
	 *   @param _currentDate [Date] current date
	 *   @param _weatherData [List<DayWeather>] latest weather data
	 *
	 *   @return [void] None.
	 */
	private void waterLawn(LawnState _ls, Date _currentDate,  List<DayWeather> _weatherData) {

		// check if today or tomorrow is a wet day
		boolean todayIsWetDay = _weatherData.get(0).getIsWetDay();
		boolean tomorrowIsWetDay = _weatherData.get(1).getIsWetDay();
		// TODO: Remove this later - hack the values for now!!!
//		boolean todayIsWetDay = false;
//		boolean tomorrowIsWetDay = false;

		// lawn cannot wait anymore for water so water not
		boolean lawnNeedsWaterNow = _ls.needsWateringUrgently(_currentDate);
		if (lawnNeedsWaterNow) {
			System.out.println("DEBUG: Need water now!!!");
			System.out.println("DEBUG: Is wet day? " + todayIsWetDay);
			System.out.println("DEBUG: Tomorrow is wet day? " + tomorrowIsWetDay);
			// if it is not going to rain today then water the lawn
			// TODO: Put this back to uncommented!!! Only for testing!!!
//			if (!todayIsWetDay) {
				_ls.waterLawn(_currentDate);
//			}
			return;
		}

		// check if this lawn needs watering based on watering algoritm/sensors/ext
		boolean shouldWaterLawn = _ls.needsWatering(_currentDate);

		// should not water this lawn then just skip to the next lawn
		if (!shouldWaterLawn) {
			return;
		}

		// it is going to rain soon so wait it out.
		// Grass is not in critical condition so it can wait a bit.
		if (todayIsWetDay || tomorrowIsWetDay) {
			return;
		}

		// if we got here then we need to water the lawn
		_ls.waterLawn(_currentDate);
	}
}

