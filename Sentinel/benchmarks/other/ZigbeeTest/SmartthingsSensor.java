// Standard Java Packages
import java.util.Date;

//RMI packages
import java.rmi.Remote;
import java.rmi.RemoteException;

// Checker annotations
//import iotchecker.qual.*;


/** Class Smartthings sensor interface for Smartthings sensor devices.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>, Changwoo Lee
 * @version     1.0
 * @since       2016-12-21
 */
public interface SmartthingsSensor extends Remote {

	/** Method to get the latests moisture reading from the sensor
	 *
	 *   @return [float] Moisture as a percentage.
	 */
	public int getValue() throws RemoteException;


	/** Method to probe the sensor for active value
	 *
	 *   @return [boolean] True means sensor is actively detecting something.
	 */
	public boolean isActiveValue() throws RemoteException;


	/** Method to get the latests moisture reading timestamp from the sensor
	 *
	 *   @return [Date] timestamp of latest moisture reading, null if no reading occurred yet.
	 */
	public long getTimestampOfLastReading() throws RemoteException;


	/** Method to initialize the moisture sensor.
	 *
	 *   @param None.
	 *
	 *   @return [void] None.
	 */
	public void init() throws RemoteException;


	/** Register an object to retrieve callbacks when new sensor reading is available
	 *
	 *   @param _callbackTo [MoistureSensorCallback].
	 *
	 *   @return [void] None.
	 */
	public void registerCallback(SmartthingsSensorCallback _callbackTo) throws RemoteException;
}














