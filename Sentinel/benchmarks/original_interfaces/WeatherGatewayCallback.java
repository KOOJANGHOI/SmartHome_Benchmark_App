package iotcode.interfaces;

// RMI Packages
import java.rmi.Remote;
import java.rmi.RemoteException;

// Checker annotations
import iotchecker.qual.NonLocalRemote;

/** Interface WeatherGatewayCallback for allowing callbacks from the PhoneGateway class.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0                
 * @since       2016-04-26
 */

public interface WeatherGatewayCallback extends Remote {

	/** Callback method for when the information is retrieved.
	 *
	 * @param _wgw [WeatherGateway].
	 * @return [void] None.
	 */
	public void informationRetrieved(@NonLocalRemote WeatherGateway _wgw) throws RemoteException;
}
