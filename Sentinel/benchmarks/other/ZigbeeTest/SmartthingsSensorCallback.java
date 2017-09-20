// Checker annotations
//import iotchecker.qual.*;


/** Smartthings Sensor Callback for when a camera changes state (new frame available).
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>, Changwoo Lee
 * @version     1.0
 * @since       2016-12-21
 */

public interface SmartthingsSensorCallback {

	//public void newReadingAvailable(@NonLocalRemote SmartthingsSensor _sensor) throws RemoteException;
	public void newReadingAvailable(int _value, boolean _activeValue);
}
