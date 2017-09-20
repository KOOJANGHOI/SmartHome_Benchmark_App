package iotcode.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** Interface Room for all room implementations
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0                
 * @since       2016-01-27
 */
public interface Room extends Remote {

	/** Method to return room ID.
	 *
	 *   @param None.
	 *
	 *   @return [int] Room identifier.
	 */
	public int getRoomID() throws RemoteException;
}
