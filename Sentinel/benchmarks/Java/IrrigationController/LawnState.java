package IrrigationController;

// Standard Java Packages
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

// RMI packages
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// IoT Driver Packages
import iotcode.interfaces.*;

// Checker annotations
//import iotchecker.qual.*;

/** Class LawnState that represents the state of the lawn, also help calculate if the lawn needs to be watered.
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-04-04
 */

public class LawnState extends UnicastRemoteObject implements MotionDetectionCallback, MoistureSensorCallback {


	/*******************************************************************************************************************************************
	**
	**  Constants
	**
	*******************************************************************************************************************************************/
	private static final long MAX_TIME_BETWEEN_WATERING_SESSIONS = 4 * 24 * 60 * 60;	// 5 days
	private static final int MAX_DAYS_RAINED_RECORD = 20;
	private static final int RAINED_RECENTLY_DAYS_INTERVAL = 1;
	private static final long TWENTY_FIVE_HOURS = 25 * 60 * 60;
	private static final long TWO_HOURS = 2 * 60 * 60;

	private static final long NEW_MOTION_THRESHOLD = 2 * 60;	// 2 minutes
	private static final long AMOUNT_OF_MOTION_FOR_ACTIVE = 60 * 60;	// 1 hour
	private static final long AMOUNT_OF_TIME_FOR_ACTIVE_TO_HOLD = 7 * 24 * 60 * 60;		// 1 week

	private static final double MOISTURE_LEVEL_FOR_NORMAL_WATERING = 25;		// Percentage
	private static final double MOISTURE_LEVEL_FOR_EMERGENCY_WATERING = 5;	// Percentage
	private static final double MOISTURE_LEVEL_FOR_NO_WATERING = 80;	// Percentage

	/*******************************************************************************************************************************************
	**
	**  Variables
	**
	*******************************************************************************************************************************************/
	private boolean isInHibernationMode = false;
	private Date lastTimeWatered = null;
	private boolean didWaterSinceLastSchedualedDate = false;
	private List<Date> daysRained = new ArrayList<Date>();
	private int daysToWaterOn = 0;
	private LawnSmart iotLawnObject;
	private MotionDetection motionDetector;
	private double inchesPerMinute = 0;
	private double inchesPerWeek = 0;
	private double timePerWatering = 0;
	private double timePerWeek = 0;
	private double timeWateredSoFar = 0;
	private SprinklerSmart sprinkler;
	private int zone = 0;
	private Date lastMotionDetectedTime = null;
	private Date startOfThisMotion = null;
	private Date lastUpdateDate = null;
	private Lock mutex = new ReentrantLock();
	private long totalMotionOnLawn = 0;
	private long numberOfMotionsOnLawnToday = 0;
	private boolean lawnIsActive = false;
	private Date lawnBecameActiceDate = null;
	private Map<Integer, Double> moistureSensorReadings = 
		new ConcurrentHashMap<Integer, Double>();
	private Map<Integer, Date> moistureSensorUpdateTimes = 
		new ConcurrentHashMap<Integer, Date>();


	// 0th bit = Monday, 1th bit = Tuesday ext
	public LawnState(LawnSmart _l, int _daysToWaterOn, MotionDetection _mo, 
					double _inchesPerMinute, double _inchesPerWeek, SprinklerSmart _sprinkler, 
					int _zone, Set<MoistureSensorSmart> _moistureSensors) throws RemoteException {
		iotLawnObject = _l;
		daysToWaterOn = _daysToWaterOn;
		inchesPerMinute = _inchesPerMinute;
		inchesPerWeek = _inchesPerWeek;
		sprinkler = _sprinkler;
		zone = _zone;

		// register the callback with self
		motionDetector = _mo;
		_mo.registerCallback(this);

		// register callback to self
		for (MoistureSensorSmart sen : _moistureSensors) {

			try {
				sen.registerCallback(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// parse the days that we are going to water on
		int numberOfDaysForWatering = 0;
		for (int i = 0; i < 7; i++) {
			if ((daysToWaterOn & (1 << i)) > 0) {
				numberOfDaysForWatering++;
			}
		}

		// calculate lawn watering water amounts
		timePerWeek = _inchesPerWeek / _inchesPerMinute;
		timePerWatering = timePerWeek / (double)numberOfDaysForWatering;
	}

	/*******************************************************************************************************************************************
	**
	**  Public Methods
	**
	*******************************************************************************************************************************************/


	/** Method to update the lawn state, updates lawn activity state based on activity timeout
	 *
	 *   @param _currentDate  [Date], the current date and time.
	 *
	 *   @return [void] None.
	 */
	public void updateLawn(Date _currentDate) {
		if (lastUpdateDate != null) {

			// check if we already did an update today
			if ((lastUpdateDate.getDate() == _currentDate.getDate())
					&& (lastUpdateDate.getMonth() == _currentDate.getMonth())
					&& (lastUpdateDate.getYear() == _currentDate.getYear())) {
				return;
			}
		}

		lastUpdateDate = _currentDate;

		// lawn was active at some time so check if it can be deemed inactive because
		// time has passed and it has not been active in that time
		if (lawnBecameActiceDate != null) {
			long timeElapsed = (_currentDate.getTime() - lawnBecameActiceDate.getTime()) / 1000;

			if (timeElapsed >= AMOUNT_OF_TIME_FOR_ACTIVE_TO_HOLD) {
				lawnBecameActiceDate = null;
				lawnIsActive = false;
			}
		}


		// check activity of lawn
		boolean isActiveLawn = false;
		try {
			mutex.lock();
			if (totalMotionOnLawn >= AMOUNT_OF_MOTION_FOR_ACTIVE) {
				isActiveLawn = true;
			}

			// reset motion counters
			totalMotionOnLawn = 0;
			numberOfMotionsOnLawnToday = 0;

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			mutex.unlock();
		}

		// update lawn state
		if (isActiveLawn) {
			lawnIsActive = true;
			lawnBecameActiceDate = _currentDate;
		}
	}


	/** Method to test if this lawn is active or not.
	 *
	 *   @return [Boolean] lawn is active.
	 */
	public boolean lawnHasSufficientMotion() {
		return lawnIsActive;
	}


	/** Method to test if this lawn should be watered or not right now.
	 *   Lawn urgently needs to be watered right now.
	 *
	 *   @param _currentDate  [Date], the current date and time.
	 *
	 *   @return [Boolean] lawn does need watering.
	 */
	public boolean needsWateringUrgently(Date _currentDate) {

		// get difference between now and last time watered
		// TODO: Remove this to uncommented!!! This is only for testing!!!
/*		long timeElapsed = (_currentDate.getTime() - lastTimeWatered.getTime()) / 1000;

		// needs watering now urgently
		if (timeElapsed >= MAX_TIME_BETWEEN_WATERING_SESSIONS) {
			return true;
		}

		// calculate the average moisture readings of all the
		// sensors in this lawn
		double averageMoistureValue = getAverageMoistureReading();

		// is a valid average
		if (averageMoistureValue != -1) {
			// moisture is very low so we need to water now!
			if (averageMoistureValue <= MOISTURE_LEVEL_FOR_EMERGENCY_WATERING) {
				return true;
			} else if (averageMoistureValue >= MOISTURE_LEVEL_FOR_NO_WATERING) {
				// moisture is high so no need to water
				return false;
			}
		}

		return false;
*/
		double averageMoistureValue = getAverageMoistureReading();
//		System.out.println("DEBUG: Average moisture value: " + averageMoistureValue);

		return true;
	}


	/** Method to test if this lawn should be watered or not
	 *
	 *   @param _currentDate  [Date], the current date and time.
	 *
	 *   @return [Boolean] lawn does need watering.
	 */
	public boolean needsWatering(Date _currentDate) {

		// only check if we have watered since the last date
		if (didWaterSinceLastSchedualedDate) {
			// get the day of the week from the date and convert it to be
			// 0=Monday, 1=Sunday, ....
			int dayOfWeek = _currentDate.getDay();
			dayOfWeek = (dayOfWeek - 1) % 7;

			// Calculate what we should mask out days to water byte to see if it is a 1
			int mask = (1 << dayOfWeek);

			// mask the bye
			int shouldWaterToday = daysToWaterOn & mask;

			// if the post masked data is 0 then we should not water today since that bit was not set to 1
			// do not water today
			if (shouldWaterToday == 0) {
				return false;
			}

		}

		// it is a scheduled day so we need to water soon;
		didWaterSinceLastSchedualedDate = false;

		// check if it rained in the last little bit so there is no need to water this grass right now.
		if (didRainRecently(_currentDate, RAINED_RECENTLY_DAYS_INTERVAL)) {
			return false;
		}

		// The grass was never watered before so water now
		if (lastTimeWatered == null) {
			return true;
		}

		// calculate the average moisture readings of all the
		// sensors in this lawn
		double averageMoistureValue = getAverageMoistureReading();

		// is a valid average
		if (averageMoistureValue != -1) {
			// moisture is low enough to need to water now
			if (averageMoistureValue <= MOISTURE_LEVEL_FOR_NORMAL_WATERING) {
				return true;
			} else if (averageMoistureValue >= MOISTURE_LEVEL_FOR_NO_WATERING) {
				// moisture is high so no need to water
				return false;
			}
		}

		// if got here then no condition says we should not water today so we should
		// water the grass today
		return true;
	}


	/** Method to get the date of the last time the lawn was watered
	 *
	 *   @return [Date] date of last watering.
	 */
	public Date getLastTimeWatered() {
		return lastTimeWatered;
	}

	/** Method to keep track of the last few times it rained on this lawn
	 *
	 *   @param _dateOfRain  [Date], the date of the rain.
	 *
	 *   @return [void] None.
	 */
	public void rainedOnDate(Date _dateOfRain) {

		// the grass was technically watered on this day
		lastTimeWatered = _dateOfRain;

		didWaterSinceLastSchedualedDate = true;

		// it rained on this date
		daysRained.add(_dateOfRain);

		// only keep the last 20 days that it rained
		if (daysRained.size() > 20) {
			daysRained.remove(0);
		}

	}


	/** Method to water lawn, calculates how much to water and sends water signal to controller
	 *
	 *   @param _currentDate  [Date], the current date and time.
	 *
	 *   @return [void] None.
	 */
	public void waterLawn(Date _currentDate) {
		lastTimeWatered = _currentDate;
		didWaterSinceLastSchedualedDate = true;

		// get the day of the week from the date and convert it to be
		// 0=Monday, 1=Sunday, ....
		int dayOfWeek = _currentDate.getDay();
		dayOfWeek = (dayOfWeek - 1) % 7;


		// check if it is the last day to water for this week
		boolean isLastDay = true;
		for (int i = 6; i > dayOfWeek; i--) {
			int mask = (1 << dayOfWeek);

			int shouldWaterToday = daysToWaterOn & mask;

			if (shouldWaterToday != 0) {
				isLastDay = false;
				break;
			}
		}


		int secondsToWater = 0;
		if (isLastDay) {

			// last day of week to water so water the remaining amount
			double minutesToWater = timePerWeek - timeWateredSoFar;
			timeWateredSoFar = 0;
			secondsToWater = (int)((double)(minutesToWater * 60));

		} else {

			// if it is not the last day then just water a normal amount
			timeWateredSoFar += timePerWatering;
			secondsToWater = (int)((double)(timePerWatering * 60));
		}

		try {
			System.out.println("DEBUG: We water the lawn!!! Zone: " + zone + " Seconds to water: " + secondsToWater);
			sprinkler.setZone(zone, true, secondsToWater);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/** Method callback from the motion detection callback interface, processes the motion
	 *  to see how long the motion was and saves that motion.
	 *
	 *   @param _md  [MotionDetection], motion detector with the motion
	 *
	 *   @return [void] None.
	 */
	public void motionDetected(long timeStampOfLastMotion) {

		Date currMotTime = new Date(timeStampOfLastMotion);

		if (lastMotionDetectedTime == null) {
			lastMotionDetectedTime = currMotTime;
		}

		if (startOfThisMotion == null) {
			startOfThisMotion = currMotTime;
		}

		long timeElapsed = (currMotTime.getTime() - lastMotionDetectedTime.getTime()) / 1000;

		if (timeElapsed >= NEW_MOTION_THRESHOLD) {
			try {
				mutex.lock();
				long motiontime = (lastMotionDetectedTime.getTime() - startOfThisMotion.getTime()) / 1000;
				totalMotionOnLawn += motiontime;
				numberOfMotionsOnLawnToday++;


			} catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				mutex.unlock();
			}

			startOfThisMotion = currMotTime;
		}

		lastMotionDetectedTime = currMotTime;
	}


	/** Callback method for when a new moisture reading is available.
	 *   Called when a new reading is ready by the sensor and the sensor
	 *   can be checked for the frame data.
	 *
	 *   @param _sensor [MoistureSensor] .
	 *
	 *   @return [void] None.
	 */
	public void newReadingAvailable(int sensorId, float moisture, long timeStampOfLastReading) {

		moistureSensorReadings.put(sensorId, (double) moisture);
		moistureSensorUpdateTimes.put(sensorId, new Date(timeStampOfLastReading));
	}


	/*******************************************************************************************************************************************
	**
	**  Helper Methods
	**
	*******************************************************************************************************************************************/

	/** Method to check if it rained recently in the near past.
	 *
	 *   @param _numberOfDaysInPast  [long], number of days in the past to check if it rained recently.
	 *   @param _currentDate  [Date], the current date and time.
	 *
	 *   @return [boolean] weather it rained recently or not.
	 */
	private boolean didRainRecently(Date _currentDate, long _numberOfDaysInPast) {

		// it never rained before
		if (daysRained.size() == 0) {
			return false;
		}

		// convert the days to seconds for calculation
		long numberOfSecondsInPast = _numberOfDaysInPast * 24 * 60 * 60;

		// go through all the stored days that it rained on
		for (Date d : daysRained) {

			// check the difference time and convert to seconds.
			long numberOfSecondsDifference = (_currentDate.getTime() - d.getTime()) / 1000;

			// if it rained in the last specified time then return true
			if (numberOfSecondsDifference < numberOfSecondsInPast) {
				return true;
			}
		}

		return false;
	}


	/** Method calculate the average moisture readings of the most recent moisture reading of each sensor
	 *   if that reading is not stale
	 *
	 *   @return [double] average value of moisture readings.
	 */
	private double getAverageMoistureReading() {

		Date currentTime = new Date();
		double total = 0;
		int numberOfReadings = 0;

		for (Integer sen : moistureSensorReadings.keySet()) {

			// check the timestamp of the watering of the lawn
			Date readingTimestamp = moistureSensorUpdateTimes.get(sen);

			System.out.println("DEBUG: Sensor reading time stamp: " + readingTimestamp.getTime());
			System.out.println("DEBUG: Current time stamp: " + currentTime.getTime());
			System.out.println("Time elapsed: " + (currentTime.getTime() - readingTimestamp.getTime()) / 1000);			

			//long timeElapsedSinceLastWatering = (currentTime.getTime() - readingTimestamp.getTime()) / 1000;

			// if reading is old then dont use it since it is noise
			//if (timeElapsedSinceLastWatering > TWO_HOURS) {
			//	continue;
			//}

			// Do averaging
			numberOfReadings++;
			total += moistureSensorReadings.get(sen);

			System.out.println("DEBUG: Sensor reading value: " + moistureSensorReadings.get(sen) + " with total: " + total);
		}


		// if no readings were valid then return -1 so that we can signal that moisture cannot be used for now
		if (numberOfReadings == 0) {
			return -1;
		}

		// return the calculated average of all the recent moisture readings
		return total / (double)numberOfReadings;
	}
}











