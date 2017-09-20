package iotcode.interfaces;

// RMI Packages
import java.rmi.Remote;
import java.rmi.RemoteException;

// Checker annotations
import iotchecker.qual.NonLocalRemote;

/** Camera Callback for when a camera changes state (new frame available).
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-01-27
 */

public interface CameraCallback extends Remote {

	/** Callback method for when a new camera frame is available.
	 *   Called when a new frame is ready by the camera and the camera
	 *   can be checked for the frame data.
	 *
	 *   @param _camera [Camera] .
	 *
	 *   @return [void] None.
	 */
	public void newCameraFrameAvailable(@NonLocalRemote Camera _camera) throws RemoteException;
}
