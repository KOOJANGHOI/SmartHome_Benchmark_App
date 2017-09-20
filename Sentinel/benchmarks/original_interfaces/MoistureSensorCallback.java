package iotcode.interfaces;

// RMI Packages
import java.rmi.Remote;
import java.rmi.RemoteException;

// Checker annotations
import iotchecker.qual.*;


/** Camera Callback for when a camera changes state (new frame available).
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-01-27
 */

public interface MoistureSensorCallback extends Remote {

	/** Callback method for when a new moisture reading is available.
	 *   Called when a new reading is ready by the sensor and the sensor
	 *   can be checked for the frame data.
	 *
	 *   @param _sensor [MoistureSensor] .
	 *
	 *   @return [void] None.
	 */
	public void newReadingAvailable(@NonLocalRemote MoistureSensor _sensor) throws RemoteException;
}
