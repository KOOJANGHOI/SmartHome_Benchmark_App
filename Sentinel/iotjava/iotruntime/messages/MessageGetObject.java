package iotruntime.messages;

import java.io.Serializable;

/** Class MessageGetObject is a sub class of Message
 *  This class wraps-up a message to get an object
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-02-24
 */
public final class MessageGetObject extends Message {

	/**
	 * MessageGetObject class property
	 */
	private String sHostAddress;
	private String sObjClass;
	private String sObjName;
	private String sObjIntName;
	private String sObjStubIntName;
	private int iRMIRegPort;
	private int iRMIStubPort;
	private Object[] arrObjFields;
	private Class[] arrObjFldCls;

	/**
	 * Class constructor (to tell IoTSlave controller to get objects for IoTSet/IoTRelation)
	 */
	public MessageGetObject(IoTCommCode sMsg, String sHAddress, String sOName,
		String sOClass, String sOIName, String sOSIName, int iRRPort, int iRSPort) {


		super(sMsg);
		sHostAddress = sHAddress;
		sObjClass = sOClass;
		sObjName = sOName;
		sObjIntName = sOIName;
		sObjStubIntName = sOSIName;
		iRMIRegPort = iRRPort;
		iRMIStubPort = iRSPort;
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
	 * getObjectClass() method
	 *
	 * @return  String
	 */
	public String getObjectClass() {
		return sObjClass;
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
	 * getObjectInterfaceName() method
	 *
	 * @return  String
	 */
	public String getObjectInterfaceName() {
		return sObjIntName;
	}

	/**
	 * getObjectStubInterfaceName() method
	 *
	 * @return  String
	 */
	public String getObjectStubInterfaceName() {
		return sObjStubIntName;
	}

	/**
	 * getRMIRegPort() method
	 *
	 * @return  int
	 */
	public int getRMIRegPort() {
		return iRMIRegPort;
	}

	/**

	 * getRMIStubPort() method
	 *
	 * @return  int
	 */
	public int getRMIStubPort() {
		return iRMIStubPort;
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

	/**
	 * setObjectClass() method
	 *
	 * @param   sOClass  String object name
	 * @return  void
	 */
	public void setObjectClass(String sOClass) {
		sObjClass = sOClass;
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

	/**
	 * setObjectInterfaceName() method
	 *
	 * @param   sOIName  String object name
	 * @return  void
	 */
	public void setObjectInterfaceName(String sOIName) {
		sObjIntName = sOIName;
	}

	/**
	 * setObjectStubInterfaceName() method
	 *
	 * @param   sOIName  String object name
	 * @return  void
	 */
	public void setObjectStubInterfaceName(String sOSIName) {
		sObjStubIntName = sOSIName;
	}

	/**
	 * setRMIRegPort() method
	 *
	 * @param   iRRPort  RMI registry port number
	 * @return  void
	 */
	public void setRMIRegPort(int iRRPort) {
		iRMIRegPort = iRRPort;
	}

	/**
	 * setRMIStubPort() method
	 *
	 * @param   iRSPort  RMI stub port number
	 * @return  void
	 */
	public void setRMIStubPort(int iRSPort) {
		iRMIStubPort = iRSPort;
	}

}
