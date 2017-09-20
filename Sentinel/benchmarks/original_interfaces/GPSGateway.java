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
 * @since       2016-04-27
 */

public interface GPSGateway extends Remote {

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
	 *   @param _callbackTo [PhoneGatewayCallback].
	 *
	 *   @return [void] None.
	 */
	public void registerCallback(@NonLocalRemote GPSGatewayCallback _callbackTo) throws RemoteException;


	/** Get room identifier
	 *
	 *   @param None.
	 *
	 *   @return [int] Room identifier.
	 */
	public int getRoomID() throws RemoteException;


	/** Get ring status
	 *
	 *   @param None.
	 *
	 *   @return [boolean] Ring status (true/false).
	 */
	public boolean getRingStatus() throws RemoteException;

	/** Set boolean of new room identifier availability
	 *
	 *   @param [boolean] Room identifier availability (true if there is new room ID)
	 *
	 *   @return [void] None.
	 */
	public void setNewRoomIDAvailable(boolean bValue) throws RemoteException;

	/** Set boolean of new ring status availability
	 *
	 *   @param [boolean] Ring status availability (true if there is new ring status)
	 *
	 *   @return [void] None.
	 */
	public void setNewRingStatusAvailable(boolean bValue) throws RemoteException;
}














