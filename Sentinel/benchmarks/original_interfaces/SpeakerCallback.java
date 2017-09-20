package iotcode.interfaces;

// RMI Packages
import java.rmi.Remote;
import java.rmi.RemoteException;

// Checker annotations
import iotchecker.qual.NonLocalRemote;

/** speaker Callback for when a speaker changes state (sound ends).
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-01-27
 */

public interface SpeakerCallback extends Remote {

	/** Callback method for when speaker music ends.
	 *
	 *   @param _speaker [speaker] .
	 *
	 *   @return [void] None.
	 */
	public void speakerDone(@NonLocalRemote Speaker _speaker) throws RemoteException;
}
