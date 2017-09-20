package iotruntime.messages;

import java.io.Serializable;

/** Class MessageGetDeviceObject is a sub class of Message
 *  This class wraps-up a message to get device object, i.e.
 *  IoTSet that contains are IoTDeviceAddress objects
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-02-24
 */
public final class MessageGetDeviceObject extends Message {

	/**
	 * MessageGetDeviceObject class property
	 */
	private String sHostAddress;
	private int iSrcDevDrvPort;
	private int iDstDevDrvPort;
	private boolean bSrcPortWildCard;
	private boolean bDstPortWildCard;

	/**
	 * Class constructor (to tell IoTSlave to get objects for IoTSet that contains IoTDeviceAddress objects)
	 */
	public MessageGetDeviceObject(IoTCommCode sMsg, String sHAddress, int iSDDPort, int iDDDPort,
		boolean bSPWildCard, boolean bDPWildCard) {

		super(sMsg);
		sHostAddress = sHAddress;
		iSrcDevDrvPort = iSDDPort;
		iDstDevDrvPort = iDDDPort;
		bSrcPortWildCard = bSPWildCard;
		bDstPortWildCard = bDPWildCard;
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

	 * getSourceDeviceDriverPort() method
	 *
	 * @return  int
	 */
	public int getSourceDeviceDriverPort() {
		return iSrcDevDrvPort;
	}

	/**

	 * getDestinationDeviceDriverPort() method
	 *
	 * @return  int
	 */
	public int getDestinationDeviceDriverPort() {
		return iDstDevDrvPort;
	}

	/* isSourcePortWildCard() method
	 *
	 * @return  boolean  Source port wild card option (true/false)
	 */
	public boolean isSourcePortWildCard() {
		return bSrcPortWildCard;
	}

	/* isDestinationPortWildCard() method
	 *
	 * @return  boolean  Destination port wild card option (true/false)
	 */
	public boolean isDestinationPortWildCard() {
		return bDstPortWildCard;
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

	/* setSourceDeviceDriverPort() method
	 *
	 * @param   iSDDPort  Device driver port number
	 * @return  void
	 */
	public void setSourceDeviceDriverPort(int iSDDPort) {
		iSrcDevDrvPort = iSDDPort;
	}

	/* setDestinationDeviceDriverPort() method
	 *
	 * @param   iDDDPort  Device driver port number
	 * @return  void
	 */
	public void setDestinationDeviceDriverPort(int iDDDPort) {
		iDstDevDrvPort = iDDDPort;
	}

	/* setSourcePortWildCard() method
	 *
	 * @param   bSPWildCard  Port wild card option (true/false)
	 * @return  void
	 */
	public void setSourcePortWildCard(boolean bSPWildCard) {
		bSrcPortWildCard = bSPWildCard;
	}

	/* setDestionationPortWildCard() method
	 *
	 * @param   bDPWildCard  Port wild card option (true/false)
	 * @return  void
	 */
	public void setDestionationPortWildCard(boolean bDPWildCard) {
		bDstPortWildCard = bDPWildCard;
	}
}
