package iotruntime.messages;

import java.io.Serializable;

/** Class MessageSendFile is a sub class of Message
 *  This class wraps-up a message to send file
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-02-24
 */
public final class MessageSendFile extends Message {

	/**
	 * MessageSendFile class property
	 */
	private String sFileName;
	private long lFileSize;

	/**
	 * Class constructor for sending file
	 */
	public MessageSendFile(IoTCommCode sMsg, String sFName, long sFSize) {

		super(sMsg);
		sFileName = sFName;
		lFileSize = sFSize;
	}

	/**
	 * getFileName() method
	 *
	 * @return  String
	 */
	public String getFileName() {
		return sFileName;
	}

	/**
	 * getFileSize() method
	 *
	 * @return  long
	 */
	public long getFileSize() {
		return lFileSize;
	}

	/**
	 * setFileName() method
	 *
	 * @param   sFName  String file name
	 * @return  void
	 */
	public void setFileName(String sFName) {
		sFileName = sFName;
	}

	/**
	 * setFileSize() method
	 *
	 * @param   sFSize  File size
	 * @return  void
	 */
	public void setFileSize(long lFSize) {
		lFileSize = lFSize;
	}
}
