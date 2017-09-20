package iotruntime.zigbee;

/** Zigbee Message Zdo Bind Response.
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-04-19
 */
public final class IoTZigbeeMessageZdoBindResponse extends IoTZigbeeMessage {

	// private variables
	private boolean succeeded;
	private String message;

	/**
	 * Constructor
	 */
	public IoTZigbeeMessageZdoBindResponse(int _packetId, boolean _succeded, String _message) {
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
