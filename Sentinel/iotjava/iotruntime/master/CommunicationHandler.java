package iotruntime.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Class CommunicationHandler is a class that maintains
 *  a data structure that preserves a collection of host name,
 *  port numbers, and objects that are running
 *  +-----------------+----+--------+------------------+--------------+----------------+
 *  | HOST-ADDRESS    | ...|IN-PORT | RMIREGISTRY-PORT | RMISTUB-PORT | ACTIVE OBJECTS |
 *  +-----------------+----+--------+------------------+--------------+----------------+
 *  | XXX.XXX.XXX.XXX |    | XXXXX  | XXXXX            | XXXXX        | XXXXXXXXXXXXXX |
 *  |                 |    | XXXXX  | XXXXX            | XXXXX        | XXXXXXXXXXXXXX |
 *  |                 |    | XXXXX  | XXXXX            | XXXXX        | XXXXXXXXXXXXXX |
 *  |                 | ...| ...    | ...              | ...          | ...            |
 *  +-----------------+----+--------+------------------+--------------+----------------+
 *  In this case we use ACTIVE OBJECTS names as the key
 *  So ACTIVE OBJECTS maps to numbers and these numbers map to each other
 *  entry in hashmaps (HostAddress can be repetitive)
 *  e.g. ACTIVE OBJECTS ProximitySensorPS0 - 0
 *                      ProximitySensorPS1 - 1
 *                      TempSensorTS1      - 2
 *                      ...
 *       IN-PORT / RMIREGISTRY-PORT / RMISTUB-PORT / HOST-ADDRESS: 0 - XXXXX
 *                                                                 1 - XXXXX
 *                                                                 2 - XXXXX
 *  +-------------+
 *  | DEVICE-PORT |
 *  +-------------+
 *  | XXXXX       |
 *  | XXXXX       |
 *  | XXXXX       |
 *  | ...         |
 *  +-------------+
 *  We add a Set structure to handle all the other ports that are used by devices
 *  when communicating with their respective drivers
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-01-07
 */
public final class CommunicationHandler {

	/**
	 * CommunicationHandler class properties
	 * <p>
	 * Random, host name, port number, active objects
	 * HostAdd is the key to the table so we use it
	 * as a key to elements
	 * HostList gives a mapping from hostname to list of objects
	 */
	private Random random;
	private List<String> listActiveControllerObj;
	private List<String> listFieldObjectID;
	private List<ObjectCreationInfo> listObjCrtInfo;
	private List<Object[]> listArrFieldValues;
	private List<Class[]> listArrFieldClasses;
	private Map<String, Integer> hmActiveObj;
	private Map<Integer, String> hmHostAdd;
	private Map<String, ArrayList<String> > hmHostList;
	private Map<Integer, Integer> hmComPort;
	private Map<Integer, Integer> hmRMIRegPort;
	private Map<Integer, Integer> hmRMIStubPort;
	private Set<Integer> hsDevicePort;
	private Map<Integer, Integer> hmAdditionalPort;
	private int iNumOfObjects;
	private int iNumOfHosts;
	private boolean bVerbose;

	/**
	 * CommunicationHandler class constants
	 */
	private final int INT_MAX_PORT = 65535;
	private final int INT_MIN_PORT = 10000;

	/**
	 * Empty constructor
	 */
	public CommunicationHandler(boolean _bVerbose) {

		random = new Random();
		listActiveControllerObj = new ArrayList<String>();
		listFieldObjectID = new ArrayList<String>();
		listObjCrtInfo = new ArrayList<ObjectCreationInfo>();
		listArrFieldValues = new ArrayList<Object[]>();
		listArrFieldClasses = new ArrayList<Class[]>();
		hmActiveObj = new HashMap<String, Integer>();
		hmHostAdd = new HashMap<Integer, String>();
		hmHostList = new HashMap<String, ArrayList<String>>();
		hmComPort = new HashMap<Integer, Integer>();
		hmRMIRegPort = new HashMap<Integer, Integer>();
		hmRMIStubPort = new HashMap<Integer, Integer>();
		hsDevicePort = new HashSet<Integer>();
		hmAdditionalPort = new HashMap<Integer, Integer>();
		iNumOfObjects = 0;
		iNumOfHosts = 0;
		bVerbose = _bVerbose;
		RuntimeOutput.print("CommunicationHandler: Creating a new CommunicationHandler object!", bVerbose);
	}

	/**
	 * Method addPortConnection()
	 * <p>
	 * Add a new connection then generate new in-port and out-port numbers
	 *
	 * @param   sHAddress 			String host address
	 * @param   sAObject  			String active object name
	 * @return  void
	 */
	public void addPortConnection(String sHAddress, String sAObject) {

		// Increment counter first before we add objects as we start from 0
		// Objects are named uniquely so we record this and match with the host
		// Hostname occurrence can be repetitive as there can be more than
		// one host on one compute node

		// Add a new object in the list of objects
		hmActiveObj.put(sAObject, iNumOfObjects);

		// Check host existence in our data structure
		// Add a new host and a new object
		if(hmHostList.containsKey(sHAddress) == false) {
			iNumOfHosts++;
			hmHostList.put(sHAddress, new ArrayList<String>());
		}
		hmHostList.get(sHAddress).add(sAObject);

		// Map object to host
		hmHostAdd.put(iNumOfObjects, sHAddress);

		int iComPort = 0;
		do {
			iComPort = random.nextInt(INT_MAX_PORT - INT_MIN_PORT + 1) + INT_MIN_PORT;
			// Check port existence in HashMap
		} while (portIsAvailable(iComPort) == false);
		hmComPort.put(iNumOfObjects, iComPort);

		int iRMIRegPort = 0;
		do {
			iRMIRegPort = random.nextInt(INT_MAX_PORT - INT_MIN_PORT + 1) + INT_MIN_PORT;
			// Check port existence in HashMap
		} while (portIsAvailable(iRMIRegPort) == false);
		hmRMIRegPort.put(iNumOfObjects, iRMIRegPort);

		int iRMIStubPort = 0;
		do {
			iRMIStubPort = random.nextInt(INT_MAX_PORT - INT_MIN_PORT + 1) + INT_MIN_PORT;
			// Check port existence in HashMap
		} while (portIsAvailable(iRMIStubPort) == false);
		hmRMIStubPort.put(iNumOfObjects, iRMIStubPort);

		iNumOfObjects++;
	}

	/**
	 * A private method to add a new active controller object
	 *
	 * @params  strFieldObjectID  			String field object ID
	 * @params  strObjName  				String object name
	 * @params  strObjClassName 			String object class name
	 * @params  strObjClassInterfaceName 	String object class interface name
	 * @params  strIoTSlaveObjectHostAdd 	String IoTSlave host address
	 * @params  arrFieldValues				Array of field values
	 * @params  arrFieldClasses				Array of field classes
	 * @return  void
	 */
	public void addActiveControllerObject(String strFieldObjectID, String strObjName, String strObjClassName,
		String strObjClassInterfaceName, String strObjStubClsIntfaceName, String strIoTSlaveObjectHostAdd, Object[] arrFieldValues, 
		Class[] arrFieldClasses) {

		listActiveControllerObj.add(strObjName);
		listFieldObjectID.add(strFieldObjectID);
		listArrFieldValues.add(arrFieldValues);
		listArrFieldClasses.add(arrFieldClasses);
		ObjectCreationInfo objCrtInfo = new ObjectCreationInfo(strIoTSlaveObjectHostAdd, strObjName,
			strObjClassName, strObjClassInterfaceName, strObjStubClsIntfaceName);
		listObjCrtInfo.add(objCrtInfo);
	}

	/**
	 * Method addDevicePort()
	 * <p>
	 * Add a port that is used by a device when communicating with its driver
	 * This port will be taken into account when checking for port availability
	 *
	 * @param   iDevPort  Device port number
	 * @return  void
	 */
	public void addDevicePort(int iDevPort) {

		hsDevicePort.add(iDevPort);

	}

	/**
	 * Method addAdditionalPort()
	 * <p>
	 * Add a new port for new connections for any objects in the program.
	 * This newly generated port number will be recorded.
	 *
	 * @return  int		One new port
	 */
	public int addAdditionalPort(String sAObject) {

		hmActiveObj.put(sAObject, iNumOfObjects);

		int iAdditionalPort = 0;
		do {
			iAdditionalPort = random.nextInt(INT_MAX_PORT - INT_MIN_PORT + 1) + INT_MIN_PORT;
			// Check port existence in HashMap
		} while (portIsAvailable(iAdditionalPort) == false);
		hmAdditionalPort.put(iNumOfObjects, iAdditionalPort);

		iNumOfObjects++;

		return iAdditionalPort;
	}

	/**
	 * Method portIsAvailable()
	 * <p>
	 * Checks the availability of the newly generated port.
	 * If port number has been used in any of the lists then
	 * it is not available
	 *
	 * @param   iPortNumber  Device port number
	 * @return  boolean
	 */
	public boolean portIsAvailable(int iPortNumber) {

		if (hmComPort.containsValue(iPortNumber) == true) {
			return false;
		} else if (hmRMIRegPort.containsValue(iPortNumber) == true) {
			return false;
		} else if (hmRMIStubPort.containsValue(iPortNumber) == true) {
			return false;
		} else if (hmAdditionalPort.containsValue(iPortNumber) == true) {
			return false;
		} else if (hsDevicePort.contains(iPortNumber) == true) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Method getNumOfObjects()
	 *
	 * @return  int
	 */
	public int getNumOfObjects() {

		return iNumOfObjects;

	}

	/**
	 * Method getNumOfHosts()
	 *
	 * @return  int
	 */
	public int getNumOfHosts() {

		return iNumOfHosts;

	}

	/**
	 * Method objectExists()
	 *
	 * @param   sObjName  String object name
	 * @return  boolean
	 */
	public boolean objectExists(String sObjName) {

		return hmActiveObj.containsKey(sObjName);

	}

	/**
	 * Method hostExists()
	 *
	 * @param   sHostName  String host name
	 * @return  boolean
	 */
	public boolean hostExists(String sHostName) {

		return hmHostList.containsKey(sHostName);

	}

	/**
	 * Method getHostAddress()
	 * <p>
	 * User finds HostAddress using Object name
	 *
	 * @param   sAObject  String active object name
	 * @return  String
	 */
	public String getHostAddress(String sAObject) {

		return hmHostAdd.get(hmActiveObj.get(sAObject));

	}

	/**
	 * Method getHosts()
	 * <p>
	 * User gets the set of hostnames
	 *
	 * @return  String
	 */
	public Set<String> getHosts() {

		return hmHostList.keySet();

	}

	/**
	 * Method getComPort()
	 * <p>
	 * User finds In-Port number using Object name
	 *
	 * @param   sAObject  String active object name
	 * @return  Integer
	 */
	public Integer getComPort(String sAObject) {

		return hmComPort.get(hmActiveObj.get(sAObject));
	}

	/**
	 * Method getAdditionalPort()
	 * <p>
	 * User finds a port number using Object name
	 *
	 * @param   sAObject  String active object name
	 * @return  Integer
	 */
	public Integer getAdditionalPort(String sAObject) {

		return hmAdditionalPort.get(hmActiveObj.get(sAObject));
	}

	/**
	 * Method getRMIRegPort()
	 * <p>
	 * User finds Out-Port number using Object name
	 *
	 * @param   sAObject  String active object name
	 * @return  Integer
	 */
	public Integer getRMIRegPort(String sAObject) {

		return hmRMIRegPort.get(hmActiveObj.get(sAObject));

	}

	/**
	 * Method getRMIStubPort()
	 * <p>
	 * User finds Out-Port number using Object name
	 *
	 * @param   sAObject  String active object name
	 * @return  Integer
	 */
	public Integer getRMIStubPort(String sAObject) {

		return hmRMIStubPort.get(hmActiveObj.get(sAObject));

	}

	/**
	 * Method getFieldObjectID()
	 * <p>
	 * User finds field object ID using Object name
	 *
	 * @param   sAObject  String active object name
	 * @return  String
	 */
	public String getFieldObjectID(String sAObject) {

		return listFieldObjectID.get(listActiveControllerObj.indexOf(sAObject));

	}

	/**
	 * Method getObjectCreationInfo()
	 * <p>
	 * User finds ObjectCreationInfo using Object name
	 *
	 * @param   sAObject  String active object name
	 * @return  ObjectCreationInfo
	 */
	public ObjectCreationInfo getObjectCreationInfo(String sAObject) {

		return listObjCrtInfo.get(listActiveControllerObj.indexOf(sAObject));

	}

	/**
	 * Method getArrayFieldClasses()
	 * <p>
	 * User finds array of field classes using Object name
	 *
	 * @param   sAObject  String active object name
	 * @return  Class[]
	 */
	public Class[] getArrayFieldClasses(String sAObject) {

		return listArrFieldClasses.get(listActiveControllerObj.indexOf(sAObject));

	}

	/**
	 * Method getArrayFieldValues()
	 * <p>
	 * User finds array of field values using Object name
	 *
	 * @param   sAObject  String active object name
	 * @return  Object[]
	 */
	public Object[] getArrayFieldValues(String sAObject) {

		return listArrFieldValues.get(listActiveControllerObj.indexOf(sAObject));

	}

	/**
	 * Method getActiveControllerObjectList()
	 *
	 * @return  List<String>
	 */
	public List<String> getActiveControllerObjectList() {

		return listActiveControllerObj;

	}

	/**
	 * Method printLists()
	 *
	 * @return  void
	 */
	public void printLists() {

		// Iterate on HostAddress
		for(String s : hmHostList.keySet()) {

			for(String str : hmHostList.get(s)) {

				int iIndex = hmActiveObj.get(str);
				RuntimeOutput.print("Active Object: " + str, bVerbose);
				RuntimeOutput.print("Communication Port: " + hmComPort.get(iIndex), bVerbose);
				RuntimeOutput.print("RMI Registry Port: " + hmRMIRegPort.get(iIndex), bVerbose);
				RuntimeOutput.print("RMI Stub Port: " + hmRMIStubPort.get(iIndex), bVerbose);
				RuntimeOutput.print("\n", bVerbose);
			}
		}

		for(int iPort : hsDevicePort) {
			RuntimeOutput.print("Device Port: " + iPort, bVerbose);
		}
		RuntimeOutput.print("\n", bVerbose);
	}
}
