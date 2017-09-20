package iotcode.interfaces;

//RMI packages
import java.rmi.Remote;
import java.rmi.RemoteException;

// Checker annotations
import iotchecker.qual.NonLocalRemote;

/** Gateway public interface, e.g. for PhoneGateway
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0                
 * @since       2016-04-26
 */

public interface WeatherGateway extends Remote {

	/** Method to start the gateway.
	 *
	 *   @param None.
	 *
	 *   @return None
	 */
	public void start() throws RemoteException;


	/** Method to stop the gateway.
	 *
	 *   @param None.
	 *
	 *   @return None
	 */
	public void stop() throws RemoteException;


	/** Method to initialize the gateway.
	 *
	 *   @return [void] None.
	 */
	public void init() throws RemoteException;


	/** Register an object to retrieve callbacks when new data is available.
	 *
	 *   @param _callbackTo [WeatherGatewayCallback].
	 *
	 *   @return [void] None.
	 */
	public void registerCallback(@NonLocalRemote WeatherGatewayCallback _callbackTo) throws RemoteException;


	/** Get inches per week data
	 *
	 *   @param None.
	 *
	 *   @return [double] Rainfall (inches per week).
	 */
	public double getInchesPerWeek() throws RemoteException;


	/** Get weather area zip code
	 *
	 *   @param None.
	 *
	 *   @return [int] Area zipcode.
	 */
	public int getWeatherZipCode() throws RemoteException;


	/** Days to keep watering the lawns
	 *
	 *   @param None.
	 *
	 *   @return [int] Number of days.
	 */
	public int getDaysToWaterOn() throws RemoteException;


	/** Get inches per minute data
	 *
	 *   @param None.
	 *
	 *   @return [double] Rainfall (inches per minute).
	 */
	public double getInchesPerMinute() throws RemoteException;
}














