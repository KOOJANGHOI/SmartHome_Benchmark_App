package iotruntime.messages;

import java.io.Serializable;

/** Class Message is an abstract class that creates a simple
 *  data structure to pack the needed payloads for communication,
 *  e.g. host address, file name object information, etc.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-01-07
 */
public abstract class Message implements Serializable {

	/**
	 * Message class property
	 */
	private IoTCommCode sMessage;

	/**
	 * Class constructor (communication code only)
	 */
	public Message(IoTCommCode sMsg) {

		sMessage = sMsg;

	}

	/**
	 * getMessage() method
	 *
	 * @return  IoTCommCode
	 */
	public IoTCommCode getMessage() {

		return sMessage;

	}

	/**
	 * setMessage() method
	 *
	 * @param   sMsg  IoTCommCode message
	 * @return  void
	 */
	public void setMessage(IoTCommCode sMsg) {

		sMessage = sMsg;

	}

}
