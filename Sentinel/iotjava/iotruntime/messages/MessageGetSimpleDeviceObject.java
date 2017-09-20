package iotruntime.messages;

import java.io.Serializable;

/** Class MessageGetSimpleDeviceObject is a sub class of Message
 *  This class wraps-up a message to get device object, i.e.
 *  IoTSet that contains IoTZigbeeAddress objects
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-02-24
 */
public final class MessageGetSimpleDeviceObject extends Message {

	/**
	 * MessageGetDeviceObject class property
	 */
	private String sHostAddress;

	/**
	 * Class constructor (to tell IoTSlave to get objects for IoTSet that contains e.g. IoTZigbeeAddress objects)
	 */
	public MessageGetSimpleDeviceObject(IoTCommCode sMsg, String sHAddress) {

		super(sMsg);
		sHostAddress = sHAddress;
	}

	/**
	 * getHostAddress() method
	 *
	 * @return  String
	 */
	public String getHostAddress() {
		return sHostAddress;
	}

	/**
	 * setHostAddress() method
	 *
	 * @param   sHAddress  String host address
	 * @return  void
	 */
	public void setHostAddress(String sHAddress) {
		sHostAddress = sHAddress;
	}
}
