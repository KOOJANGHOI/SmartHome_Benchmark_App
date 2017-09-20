package iotruntime.zigbee;

/** Zigbee Callback for when a zigbee message is received.
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-04-12
 */
public interface IoTZigbeeCallback {

	/** Callback method for when data comes from the zigbee object
	 *
	 *   @param zigbee message class [IoTZigbeeMessage] .
	 *
	 *   @return [void] None.
	 */
	public void newMessageAvailable(IoTZigbeeMessage _zm);
}
