package iotruntime.messages;

import java.io.Serializable;

/** Class MessageCreateSetRelation is a sub class of Message
 *  This class wraps-up a message to create a new IoTSet/IoTRelation
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-02-24
 */
public final class MessageCreateSetRelation extends Message {

	/**
	 * MessageCreateSetRelation class property
	 */
	private String sObjFieldName;

	/**
	 * Class constructor (to tell IoTSlave to create a new IoTSet/IoTRelation)
	 */
	public MessageCreateSetRelation(IoTCommCode sMsg, String sOFName) {

		super(sMsg);
		sObjFieldName = sOFName;
	}

	/**
	 * getObjectFieldName() method
	 *
	 * @return  String
	 */
	public String getObjectFieldName() {
		return sObjFieldName;
	}

	/**
	 * setObjectFieldName() method
	 *
	 * @param   sOFName  String object name
	 * @return  void
	 */
	public void setObjectFieldName(String sOFName) {
		sObjFieldName = sOFName;
	}
}
