package iotruntime.messages;

import java.io.Serializable;

/** Class MessageCreateMainObject is a sub class of Message
 *  This class wraps-up a message to create a controller/device object
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-02-24
 */
public final class MessageCreateMainObject extends Message {

	/**
	 * MessageCreateMainObject class property
	 */
	private String sObjName;

	/**
	 * Class constructor (to tell IoTSlave controller/device to create controller/device object)
	 */
	public MessageCreateMainObject(IoTCommCode sMsg, String sOName) {

		super(sMsg);
		sObjName = sOName;
	}

	/**
	 * getObjectName() method
	 *
	 * @return  String
	 */
	public String getObjectName() {
		return sObjName;
	}

	/**
	 * setObjectName() method
	 *
	 * @param   sOName  String object name
	 * @return  void
	 */
	public void setObjectName(String sOName) {
		sObjName = sOName;
	}
}
