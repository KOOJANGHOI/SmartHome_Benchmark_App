package iotcode.GPSPhoneGateway;

/** PhoneInfo that implements PhoneInfoInterface
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0                
 * @since       2016-04-27
 */
public class PhoneInfo implements PhoneInfoInterface {

	/**
	 * PhoneInfo class properties
	 */
	private int iRoomIdentifier;
	private boolean bRingStatus;
	private boolean bNewRoomIDAvail;
	private boolean bNewRingStatusAvail;

	/**
	 * Constructor
	 */
	public PhoneInfo() {
		this.iRoomIdentifier = 0;
		this.bRingStatus = false;
		this.bNewRoomIDAvail = false;
		this.bNewRingStatusAvail = false;
	}

	/**
	 * Set room identifier info from the phone app using IoTRemoteCall
	 *
	 * @param   iId		Room identifier (integer)
	 * @return  String
	 */
	public String setRoomID(Integer iId) {

		this.iRoomIdentifier = iId;
		this.bNewRoomIDAvail = true;
		System.out.println("New room ID set: " + this.iRoomIdentifier);
		return "info sent";
	}

	/**
	 * Set ring status info from the phone app using IoTRemoteCall
	 *
	 * @param   bStatus		Ring status (true/false)
	 * @return  String
	 */
	public String setRingStatus(Boolean bStatus) {

		this.bRingStatus = bStatus;
		this.bNewRingStatusAvail = true;
		System.out.println("New ring status set: " + this.bRingStatus);
		return "info sent";
	}

	/**
	 * Simply return this.iRoomIdentifier
	 */
	public int getRoomID() {

		return this.iRoomIdentifier;
	}

	/**
	 * Simply return this.bRingStatus
	 */
	public boolean getRingStatus() {

		return this.bRingStatus;
	}
	
	/**
	 * Simply return this.bNewRoomIDAvail
	 */
	public boolean isNewRoomIDAvailable() {

		return this.bNewRoomIDAvail;
	}

	/**
	 * Simply return this.bNewRingStatusAvail
	 */
	public boolean isNewRingStatusAvailable() {

		return this.bNewRingStatusAvail;
	}

	/**
	 * Set this.bNewRoomIDAvail
	 */
	public void setNewRoomIDAvailable(boolean bValue) {

		this.bNewRoomIDAvail = bValue;
	}

	/**
	 * Set this.bNewRingStatusAvail
	 */
	public void setNewRingStatusAvailable(boolean bValue) {

		this.bNewRingStatusAvail = bValue;
	}
}
