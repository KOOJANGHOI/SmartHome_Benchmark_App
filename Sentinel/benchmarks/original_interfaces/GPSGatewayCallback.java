package iotcode.interfaces;

// RMI Packages
import java.rmi.Remote;
import java.rmi.RemoteException;

// Checker annotations
import iotchecker.qual.NonLocalRemote;

/** Interface GPSGatewayCallback for allowing callbacks from the GPSGateway class.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0                
 * @since       2016-04-28
 */

public interface GPSGatewayCallback extends Remote {

	/** Callback method for when room ID is retrieved.
	 *
	 * @param _wgw [GPSGateway].
	 * @return [void] None.
	 */
	public void newRoomIDRetrieved(@NonLocalRemote GPSGateway _wgw) throws RemoteException;

	/** Callback method for when ring status is retrieved.
	 *
	 * @param _wgw [GPSGateway].
	 * @return [void] None.
	 */
	public void newRingStatusRetrieved(@NonLocalRemote GPSGateway _wgw) throws RemoteException;
}
