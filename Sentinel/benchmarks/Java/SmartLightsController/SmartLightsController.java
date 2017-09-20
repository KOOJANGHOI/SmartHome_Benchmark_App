package SmartLightsController;

// IoT Runtime packages
import iotruntime.slave.IoTSet;
import iotruntime.slave.IoTRelation;

// IoT driver packages
import iotcode.interfaces.*;
import iotcode.annotation.*;


// Standard Java packages
import java.util.HashMap;
import java.util.Map;
import java.util.Date;	// TODO: Get rid of all depreciated stuff for date, switch to Calender
import java.util.concurrent.Semaphore;

// RMI packages
import java.rmi.RemoteException;

// Checker annotations
//import iotchecker.qual.*;

/** Class Smart Lights Controller for the smart home application benchmark
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-01-27
 */
public class SmartLightsController {

	/*
	 *  Constants
	 */
	public static final int MOTION_TIME_THRESHOLD = 60; 	// in seconds
	public static final int CHECK_TIME_WAIT = 1;			// in seconds
	public static final int COLOR_TEMP_MIN_KELVIN = 2500;	// In Kelvin
	public static final int COLOR_TEMP_MAX_KELVIN = 9000;	// In Kelvin
	public static final int CAMERA_FPS = 15;				// In frames per second

	/*
	 *  IoT Sets of Devices
	 */
	@config private IoTSet<LightBulbSmart> mainRoomLightBulbs;
	@config private IoTSet<CameraSmart> cameras;

	/*
	 *  IoT Sets of Things that are not devices such as rooms
	 */
	@config private IoTSet<RoomSmart> rooms;

	/*
	 *  IoT Relations
	 */
	@config private IoTRelation<RoomSmart,CameraSmart> roomCameraRel;
	@config private IoTRelation<RoomSmart,LightBulbSmart> roomMainBulbRel;


	/*
	 *  The state that the room main lights are supposed to be in
	 */
	Map<RoomSmart, Boolean> roomLightOnOffStatus =
		new HashMap<RoomSmart, Boolean>();
	Map<RoomSmart, ColorTemperature> roomLightColorTemperature =
		new HashMap<RoomSmart, ColorTemperature>();

	/*
	 *  Motion detectors that are bound to specific cameras
	 */
	private Map<CameraSmart, MotionDetection>
	camMotionDetect = new HashMap<CameraSmart, MotionDetection>();


	/*******************************************************************************************************************************************
	**
	**  Variables
	**
	*******************************************************************************************************************************************/
	long lastTimeChecked = 0;

	boolean print830 = true;
	boolean print1030 = true;
	boolean print730 = true;
	boolean print930 = true;
	boolean printNite = true;

	/*******************************************************************************************************************************************
	**
	** Private Helper Methods
	**
	*******************************************************************************************************************************************/

	/** Method to detect if a room has seen motion within the last few seconds (time specified as parameter).
	 *   Checks all the motion detectors for the given room
	 *
	 *   @param _room            [RoomSmart] , RoomSmart of interest.
	 *   @param _numberOfSeconds [int]  , Number of seconds in the past that we consider recent.
	 *   @param _upperThreshold  [int]  , Number of seconds as an upper bound before we turn off.
	 *
	 *   @return [boolean] if motion was detected recently.
	 */
	private boolean roomDidHaveMotionRecently(RoomSmart _room, int _numberOfSeconds) {
		long currentTimeSeconds = (new Date()).getTime() / 1000;

		// Loop through all the motion sensors in the room
		for (CameraSmart cam : roomCameraRel.get(_room)) {
			long lastDetectedMotionSeconds = currentTimeSeconds;

			Date motionTime = ((MotionDetection)camMotionDetect.get(cam)).getTimestampOfLastMotion();

			// Motion was detected at least once
			if (motionTime != null) {
				lastDetectedMotionSeconds = motionTime.getTime() / 1000;
			} else {
				// motionTime == null means this is the initialization phase
				// so we return false to initialize the lightbulbs to off
				return false;
			}

			// Did detect motion recently
			if (Math.abs(currentTimeSeconds - lastDetectedMotionSeconds) < _numberOfSeconds) {
				return true;
			}
		}

		return false;
	}



	/**  Set the temperature of the room based on the time of day.
	 *  Do this to make sure people are able to get a good nights sleep.
	 *  based on this: https://justgetflux.com/research.html
	 *
	 *   @return [void] None;
	 */
	private void setRoomColorTemperatureForSleep() throws RemoteException {

		long currentTimeSeconds = (new Date()).getTime() / 1000;
		Date today = new Date();
		Date beginningOfToday = new Date(today.getYear(),
			today.getMonth(), today.getDate());

		long secondsSinceStartOfDay = currentTimeSeconds - (beginningOfToday.getTime() / 1000);

		for (RoomSmart room : rooms.values()) {

			// before 8:30 am
			if (secondsSinceStartOfDay <= 30600) {
				ColorTemperature colTemp = roomLightColorTemperature.get(room);
				colTemp.temperature = COLOR_TEMP_MIN_KELVIN;
				roomLightColorTemperature.put(room, colTemp);

				if (print830) {
					System.out.println("Before 8:30am!");
					System.out.println("Color temperature: " + colTemp.temperature);
					print830 = false;
					print1030 = true;
					print730 = true;
					print930 = true;
					printNite = true;
				}

			} else if ((secondsSinceStartOfDay > 30600) && (secondsSinceStartOfDay < 37800)) {
				// 8:30am - 10:30 am
				// Slowly turn lights from warm to work white
				double newKelvinValue = (double) (secondsSinceStartOfDay - 30600) / (37800 - 30600);
				newKelvinValue = (newKelvinValue * (COLOR_TEMP_MAX_KELVIN - COLOR_TEMP_MIN_KELVIN)) + COLOR_TEMP_MIN_KELVIN;
				ColorTemperature colTemp = roomLightColorTemperature.get(room);
				colTemp.temperature = (int)newKelvinValue;
				roomLightColorTemperature.put(room, colTemp);

				if (print1030) {
					System.out.println("8:30am - 10:30am!");
					print830 = true;
					print1030 = false;
					print730 = true;
					print930 = true;
					printNite = true;
				}

			} else if ((secondsSinceStartOfDay > 37800 ) && (secondsSinceStartOfDay < 70200)) {
				// Between 10:30am and 7:30pm
				// Keep white Work Light
				ColorTemperature colTemp = roomLightColorTemperature.get(room);
				colTemp.temperature = COLOR_TEMP_MAX_KELVIN;
				roomLightColorTemperature.put(room, colTemp);

				if (print730) {
					System.out.println("10:30am - 7:30pm!");
					System.out.println("Color temperature: " + colTemp.temperature);
					print830 = true;
					print1030 = true;
					print730 = false;
					print930 = true;
					printNite = true;
				}

			} else if ((secondsSinceStartOfDay > 70200) && (secondsSinceStartOfDay < 77400)) {
				// Between 7:30pm and 9:30pm
				// Slowly turn lights from work to warm
				double newKelvinValue = (double) (secondsSinceStartOfDay - 30600) / (37800 - 30600);
				newKelvinValue = (newKelvinValue * (COLOR_TEMP_MAX_KELVIN - COLOR_TEMP_MIN_KELVIN)) + COLOR_TEMP_MIN_KELVIN;
				ColorTemperature colTemp = roomLightColorTemperature.get(room);
				colTemp.temperature = (int)newKelvinValue;
				roomLightColorTemperature.put(room, colTemp);

				if (print930) {
					System.out.println("7:30pm - 9:30pm!");
					print830 = true;
					print1030 = true;
					print730 = true;
					print930 = false;
					printNite = true;
				}

			} else if (secondsSinceStartOfDay > 77400) {
				// past 9:30pm
				// Keep warm Light
				ColorTemperature colTemp = roomLightColorTemperature.get(room);
				colTemp.temperature = COLOR_TEMP_MIN_KELVIN;
				roomLightColorTemperature.put(room, colTemp);

				if (printNite) {
					System.out.println("After 9:30pm!");
					System.out.println("Color temperature: " + colTemp.temperature);
					print830 = true;
					print1030 = true;
					print730 = true;
					print930 = true;
					printNite = false;
				}
			}
		}
	}



	/** Sets bulbs to the proper state.  Sets the On/Off state as well as the color.
	 * The method changes the state of the bulb.  It does not calculate what the state
	 * of the bulb should be, this is done elsewhere.
	 *
	 * When setting the color of the bulb, a best attempt effort is made.  If the needed
	 * temperature and color of the bulb is outside the bulb range then the system gets
	 * as close as it can
	 *
	 *   @return [void] None;
	 */
	private void setMainBulbs() throws RemoteException {

		for (RoomSmart room : rooms.values()) {

			// Lights in room should be turned off
			if (!roomLightOnOffStatus.get(room)) {

				// turn the bulbs off if they are on
				for (LightBulbSmart bulb : roomMainBulbRel.get(room)) {
					if (bulb.getState()) {
						bulb.turnOff();
						System.out.println("SmartLightsController: Send off signal!!!");
					}
				}

			} else {

				// Lights in room should be turned on
				// set the color of the bulbs
				ColorTemperature colTemp = roomLightColorTemperature.get(room);
				for (LightBulbSmart bulb : roomMainBulbRel.get(room)) {
					// Turn on the bulb if they are off
					if (!bulb.getState()) {
						bulb.turnOn();
						System.out.println("SmartLightsController: Send on signal!!!");
					}

					// Get the requested color of the room
					double hue = colTemp.hue;
					double saturation = colTemp.saturation;
					double brightness = colTemp.brightness;
					int temperature = colTemp.temperature;

					// Make sure hue is in range that light bulb supports
					if (hue < bulb.getHueRangeLowerBound()) {
						hue = bulb.getHueRangeLowerBound();
					} else if (hue > bulb.getHueRangeUpperBound()) {
						hue = bulb.getHueRangeUpperBound();
					}

					// Make sure saturation is in range that light bulb supports
					if (saturation < bulb.getSaturationRangeLowerBound()) {
						saturation = bulb.getSaturationRangeLowerBound();
					} else if (saturation > bulb.getSaturationRangeUpperBound()) {
						saturation = bulb.getSaturationRangeUpperBound();
					}

					// Make sure brightness is in range that light bulb supports
					if (brightness < bulb.getBrightnessRangeLowerBound()) {
						brightness = bulb.getBrightnessRangeLowerBound();
					} else if (brightness > bulb.getBrightnessRangeUpperBound()) {
						brightness = bulb.getBrightnessRangeUpperBound();
					}

					// Make sure temperature is in range that light bulb supports
					if (temperature < bulb.getTemperatureRangeLowerBound()) {
						temperature = bulb.getTemperatureRangeLowerBound();
					} else if (temperature > bulb.getTemperatureRangeUpperBound()) {
						temperature = bulb.getTemperatureRangeUpperBound();
					}

					// Actually set the bulb to that color and temp
					bulb.setColor(hue, saturation, brightness);
					bulb.setTemperature(temperature);
				}
			}
		}
	}


	/********************************************************************************************************
	** Public methods, called by the runtime
	*********************************************************************************************************/

	/** Initialization method, called by the runtime (effectively the main of the controller)
	 *   This method runs a continuous loop and is blocking
	 *
	 *   @return [void] None;
	 */
	public void init() throws RemoteException, InterruptedException {

		System.out.println("Initialized init()!");
		// Initialize the rooms
		for (RoomSmart room : rooms.values()) {

			// All rooms start with the lights turned off
			roomLightOnOffStatus.put(room, false);

			// All rooms have a default color and temperature
			roomLightColorTemperature.put(room, new ColorTemperature(0, 0, 100, 2500));
		}
		System.out.println("Initialized rooms!");

		// Setup the cameras, start them all and assign each one a motion detector
		for (CameraSmart cam : cameras.values()) {

			// Each camera will have a motion detector unique to it since the motion detection has state
			MotionDetection mo = new MotionDetection(12, 0.5f, 10, 10);

			// initialize the camera, might need to setup some stuff internally
			cam.init();

			// set the camera parameters.
			cam.setFPS(CAMERA_FPS);
			cam.setResolution(Resolution.RES_VGA);

			// camera will call the motion detector directly with data not this controller
			cam.registerCallback(mo);

			// Start the camera (example is start the HTTP stream if it is a network camera)
			cam.start();

			// Remember which motion detector is for what camera
			camMotionDetect.put(cam, mo);
		}
		System.out.println("Initialized cameras!");

		//Initialize the light-bulbs, will turn off the bulb
		for (LightBulbSmart bulb : mainRoomLightBulbs.values()) {
			System.out.println("Trying to init bulb?");
			bulb.init();
			System.out.println("Done init!");
			Thread.sleep(1000);
		}
		System.out.println("Initialized bulbs!");

		// Run the main loop that will keep check the bulbs and rooms periodically
		while (true) {

			// Run this code every <specified time>
			long currentTimeSeconds = (new Date()).getTime() / 1000;
			if ((currentTimeSeconds - lastTimeChecked) > CHECK_TIME_WAIT) {
				lastTimeChecked = currentTimeSeconds;

				// Check for motion in rooms and if there is motion then turn on the lights
				for (RoomSmart room : rooms.values()) {

					if (roomDidHaveMotionRecently(room, MOTION_TIME_THRESHOLD)) {

						// Motion was detected
						roomLightOnOffStatus.put(room, true);

					} else {

						// No motion was detected
						roomLightOnOffStatus.put(room, false);

					}
				}

				// Check what the temperature of the light in the room should be
				setRoomColorTemperatureForSleep();

				// Set the bulbs to the new values
				setMainBulbs();

			} else {
				try {
					Thread.sleep(CHECK_TIME_WAIT * 100); // sleep for a tenth of the time
				} catch (Exception e) {

				}
			}

		}
	}
}




