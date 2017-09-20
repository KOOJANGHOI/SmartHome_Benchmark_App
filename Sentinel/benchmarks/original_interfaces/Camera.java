package iotcode.interfaces;

// Standard Java Packages
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

//RMI packages
import java.rmi.Remote;
import java.rmi.RemoteException;

// Checker annotations
import iotchecker.qual.NonLocalRemote;

/** Class Camera interface for camera devices.
 *  This Interface supports single lens cameras, can only produce 1 frame at a time
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-01-27
 */

public interface Camera extends Remote {

	/** Enumeration of the standard resolutions supported by general cameras
	 *
	 */
	public enum Resolution {
		RES_1080P,
		RES_720P,
		RES_VGA
	};


	/** Method to get the latest image frame data of the camera.
	 *
	 *   @param None.
	 *
	 *   @return [byte[]] Image frame byte data of buffered image.
	 */
	public byte[] getLatestFrame() throws RemoteException;


	/** Method to get the time-stamp of when the image was taken.
	 *
	 *   @param None.
	 *
	 *   @return [Date] Time-stamp of when the image was taken.
	 */
	public Date getTimestamp() throws RemoteException;

	/** Method to start the camera.
	 *
	 *   @param None.
	 *
	 *   @return None
	 */
	public void start() throws RemoteException;


	/** Method to stop the camera.
	 *
	 *   @param None.
	 *
	 *   @return None
	 */
	public void stop() throws RemoteException;

	/** Method to set the resolution of the camera.
	 *
	 *   @param _res [Camera.Resolution]: the new resolution of the camera
	 *
	 *   @return true if the resolution was set
	 */
	public boolean setResolution(Camera.Resolution _res) throws RemoteException;


	/** Method to set the frames per second of the camera.
	 *
	 *   @param _fps [int]: the new frames per second of the camera
	 *
	 *   @return true if the frames per second was set
	 */
	public boolean setFPS(int _fps) throws RemoteException;


	/** Method to get the max supported frames per second by the camera.
	 *
	 *   @param None.
	 *
	 *   @return [int] the max frames per second supported by the camera.
	 */
	public int getMaxFPS() throws RemoteException;


	/** Method to get the min supported frames per second by the camera.
	 *
	 *   @param None.
	 *
	 *   @return [int] the min frames per second supported by the camera.
	 */
	public int getMinFPS() throws RemoteException;


	/** Method to get the supported resolutions of the camera.
	 *
	 *   @param None.
	 *
	 *   @return [List<Camera.Resolution>] the supported resolutions of the camera.
	 */
	public List<Camera.Resolution> getSupportedResolutions() throws RemoteException;


	/** Register an object to retrieve callbacks when new camera data is available.
	 *
	 *   @param _callbackTo [CameraCallback].
	 *
	 *   @return [void] None.
	 */
	public void registerCallback(@NonLocalRemote CameraCallback _callbackTo) throws RemoteException;


	/** Method to initialize the camera, if the bulb needs to be camera.
	 *
	 *   @return [void] None.
	 */
	public void init() throws RemoteException;

}














