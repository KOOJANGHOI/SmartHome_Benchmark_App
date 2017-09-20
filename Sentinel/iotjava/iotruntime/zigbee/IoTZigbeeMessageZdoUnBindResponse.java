package iotruntime.zigbee;

/** Zigbee Message Zdo UnBind Response.
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-05-04
 */
public final class IoTZigbeeMessageZdoUnBindResponse extends IoTZigbeeMessage {

	// private variables
	private boolean succeeded;
	private String message;

	/**
	 * Constructor
	 */
	public IoTZigbeeMessageZdoUnBindResponse(int _packetId, boolean _succeded, String _message) {
		super(_packetId);
		message = _message;
		succeeded = _succeded;
	}

	/**
	 * getSucceeded() method that returns the success status
	 *
	 * @return boolean
	 */
	public boolean getSucceeded() {
		return succeeded;
	}

	/**
	 * getMessage() method that returns the error message
	 *
	 * @return String
	 */
	public String getMessage() {
		return message;
	}
}
