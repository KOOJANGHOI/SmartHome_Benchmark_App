package iotcode.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Checker annotations
import iotchecker.qual.NonLocalRemote;

/** Speaker Interface Class
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-04-29
 */
public interface Speaker extends Remote {

	/** Init method
	 *
	 *   @param None.
	 *
	 *   @return [void] None.
	 */
    public void init() throws RemoteException;

	/** Method to start playback
	 *
	 *   @param None.
	 *
	 *   @return [boolean] True/false to start playback.
	 */
    public boolean startPlayback() throws RemoteException;

	/** Method to stop playback
	 *
	 *   @param None.
	 *
	 *   @return [boolean] True/false to stop playback.
	 */
    public boolean stopPlayback() throws RemoteException;


	/** Method to getPlaybackState
	 *
	 *   @param None.
	 *
	 *   @return [boolean] True/false of playback state
	 */
    public boolean getPlaybackState() throws RemoteException;

	/** Method to set volume
	 *
	 *   @param [float] Volume percentage.
	 *
	 *   @return [boolean] True/false to set volume.
	 */
    public boolean setVolume(float _percent) throws RemoteException;

	/** Method to get volume
	 *
	 *   @param None.
	 *
	 *   @return [float] Volume percentage.
	 */
    public float getVolume() throws RemoteException;

	/** Method to get position in the song
	 *
	 *   @param None.
	 *
	 *   @return [int] Position in the song when playing music.
	 */
    public int getPosition()throws RemoteException;

	/** Method to set position
	 *
	 *   @param [int] Position to set (in milliseconds)
	 *
	 *   @return [void] None.
	 */
    public void setPosition(int _mSec) throws RemoteException;

	/** Method to set position
	 *
	 *   @param [short[]] Sample packets from music file
	 *   @param [int] Offset
	 *   @param [int] Length
	 *
	 *   @return [void] None.
	 */
    public void loadData(short[] _samples, int _offs, int _len) throws RemoteException;


   	/** Method to clear all pcm data
	 *
	 *   @param None
	 *
	 *   @return [void] None.
	 */
    public void clearData() throws RemoteException;


	/** Method to register callbacks
	 *
	 *   @param [SpeakerCallback] Callback object
	 *
	 *   @return [void] None.
	 */
    public void registerCallback(@NonLocalRemote SpeakerCallback _cb) throws RemoteException;
}
