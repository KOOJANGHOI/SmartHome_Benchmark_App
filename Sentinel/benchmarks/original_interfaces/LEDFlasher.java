package iotcode.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** Class LEDFlasher interface for LEDFlasher Brillo devices.
 *  <p>
 *  This interface is generated by IoTBrilloWeaveCodeGenerator.
 *  These comments were added after code generation.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2016-06-13
 */
public interface LEDFlasher extends Remote {

	public void init() throws RemoteException;
	public void _accessControlBlackList_block(String applicationId, Integer expirationTimeoutSec, String userId) throws RemoteException;
	public void _accessControlBlackList_list() throws RemoteException;
	public void _accessControlBlackList_unblock(String applicationId, String userId) throws RemoteException;
	public void _ledflasher_animate(Float duration, String type) throws RemoteException;
	public void _metrics_disableAnalyticsReporting() throws RemoteException;
	public void _metrics_enableAnalyticsReporting() throws RemoteException;
	public void _updater_checkForUpdates() throws RemoteException;
	public void _updater_trackChannel(String channel) throws RemoteException;
	public void base_identify() throws RemoteException;
	public void base_reboot() throws RemoteException;
	public void base_updateBaseConfiguration(String localAnonymousAccessMaxRole, Boolean localDiscoveryEnabled, Boolean localPairingEnabled) throws RemoteException;
	public void base_updateDeviceInfo(String description, String location, String name) throws RemoteException;
	public void onOff_setConfig(String state) throws RemoteException;
	
}
