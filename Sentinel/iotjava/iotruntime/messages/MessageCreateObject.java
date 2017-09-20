package iotruntime.messages;

import java.io.Serializable;

/** Class MessageCreateObject is a sub class of Message
 *  This class wraps-up a message to create an object
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-02-24
 */
public final class MessageCreateObject extends Message {

	/**
	 * MessageCreateObject class property
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
	 * Class constructor (to tell IoTSlave to create a new object)
	 */
	public MessageCreateObject(IoTCommCode sMsg, String sHAddress, String sOClass,
		String sOName, String sOIName, String sOSIName, int iRRPort, int iRSPort,
			Object[] arrOFlds, Class[] arrOFldCls) {

		super(sMsg);
		sHostAddress = sHAddress;
		sObjClass = sOClass;
		sObjName = sOName;
		sObjIntName = sOIName;
		sObjStubIntName = sOSIName;
		iRMIRegPort = iRRPort;
		iRMIStubPort = iRSPort;
		arrObjFields = arrOFlds;
		arrObjFldCls = arrOFldCls;
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
	 * getObjectFields() method
	 *
	 * @return  Object[]
	 */
	public Object[] getObjectFields() {
		return arrObjFields;
	}

	/**
	 * getObjectFldCls() method
	 *
	 * @return  Class[]
	 */
	public Class[] getObjectFldCls() {
		return arrObjFldCls;
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

	/**
	 * setObjectFields() method
	 *
	 * @param   arrOFlds  Array of object fields (object parameters)
	 * @return  void
	 */
	public void setObjectFields(Object[] arrOFlds) {
		arrObjFields = arrOFlds;
	}

	/**
	 * setObjectFieldClasses() method
	 *
	 * @param   arrOFldCls  Array of object classes (object parameters)
	 * @return  void
	 */
	public void setObjectFieldClasses(Class[] arrOFldCls) {
		arrObjFldCls = arrOFldCls;
	}
}
