package iotcode.GPSPhoneGateway;

/** PhoneInfoInterface interface to be implemented by a real class
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0                
 * @since       2016-04-27
 */
public interface PhoneInfoInterface {

	/** 
	 * GPSPhoneGateway takes 2 inputs
	 * - Room identifier (getRoomID)
	 * - Phone status (getRingStatus: ringing/not ringing)
	 */
	String setRoomID(Integer iId);
	String setRingStatus(Boolean bStatus);
}
