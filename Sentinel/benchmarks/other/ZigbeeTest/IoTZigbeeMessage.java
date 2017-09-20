
/** Zigbee Message generic class.
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-04-19
 */
public class IoTZigbeeMessage {

	// private variables
	private int packetId;

	/**
	 * Constructor
	 */
	public IoTZigbeeMessage(int _packetId) {
		packetId = _packetId;
	}


	/**
	 * getPacketId() method that returns the packet id of the received message
	 *
	 * @return int
	 */
	public int getPacketId() {
		return packetId;
	}

}
