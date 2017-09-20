package iotruntime.zigbee;


/** Zigbee Message Send Address Response.
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-04-19
 */
public final class IoTZigbeeMessageSendAddressResponse extends IoTZigbeeMessage {

	// private variables
	private boolean succeeded;
	private String message;

	/**
	 * Constructor
	 */
	public IoTZigbeeMessageSendAddressResponse(int _packetId, boolean _succeded) {
		super(_packetId);
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
}
