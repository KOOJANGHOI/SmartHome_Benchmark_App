package iotruntime.messages;

import java.io.Serializable;

/** Class MessageSimple is a sub class of Message
 *  This class only wraps-up a simple message
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-02-24
 */
public final class MessageSimple extends Message {

	/**
	 * Class constructor (communication code only)
	 */
	public MessageSimple(IoTCommCode sMsg) {

		super(sMsg);
	}
}
