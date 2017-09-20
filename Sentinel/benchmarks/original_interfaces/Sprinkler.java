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



/** Class Sprinkler interface for sprinkler devices.
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-03-31
 */
public interface Sprinkler extends Remote {

	/** Method to set the state of a specified zone
	 *
	 *   @param _zone [int]             : zone number to set.
	 *   @param _onOff [boolean]        : the state to set the zone to, on or off.
	 *   @param _onDurationSeconds [int]: the duration to set the state on to, if -1 then infinite.
	 *
	 *   @return [void] None.
	 */
	public void setZone(int _zone, boolean _onOff, int _onDurationSeconds) throws RemoteException;


	/** Method to get the current state of all the zones.
	 *
	 *   @param None.
	 *
	 *   @return [List<ZoneState>] list of the states for the zones.
	 */
	public List<ZoneState> getZoneStates() throws RemoteException;


	/** Method to get the number of zones this sprinkler can control.
	 *
	 *   @param None.
	 *
	 *   @return [int] number of zones that can be controlled.
	 */
	public int getNumberOfZones() throws RemoteException;


	/** Method to get whether or not this sprinkler can control durations.
	 *
	 *   @param None.
	 *
	 *   @return [boolean] boolean if this sprinkler can do durations.
	 */
	public boolean doesHaveZoneTimers() throws RemoteException;


	/** Method to initialize the sprinkler.
	 *
	 *   @param None.
	 *
	 *   @return [void] None.
	 */
	public void init() throws RemoteException;
}














