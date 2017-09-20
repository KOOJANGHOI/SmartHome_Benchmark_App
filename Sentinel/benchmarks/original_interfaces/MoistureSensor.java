package iotcode.interfaces;

// Standard Java Packages
import java.util.Date;

//RMI packages
import java.rmi.Remote;
import java.rmi.RemoteException;

// Checker annotations
import iotchecker.qual.*;


/** Class Moisture sensor interface for Moisture sensor devices.
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-04-19
 */
public interface MoistureSensor extends Remote {

	/** Method to get the latests moisture reading from the sensor
	 *
	 *   @return [float] Moisture as a percentage.
	 */
	public float getMoisture() throws RemoteException;


	/** Method to get the latests moisture reading timestamp from the sensor
	 *
	 *   @return [Date] timestamp of latest moisture reading, null if no reading occurred yet.
	 */
	public Date getTimestampOfLastReading() throws RemoteException;


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
	public void registerCallback(@NonLocalRemote MoistureSensorCallback _callbackTo) throws RemoteException;
}














