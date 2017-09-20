package iotruntime.master;

// Java standard libraries
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// IoTJava library
import iotruntime.messages.IoTCommCode;

/** Class ObjectInitHandler is a class that maintains
 *  a data structure that preserves a collection information
 *  for object creation and re-initialization in IoTMaster/IoTSlave.
 *  The purpose of this class is to allow field instrumentation and object generation
 *  for the main controller to be separate from field re-initialization.
 *  This way, object creations can be parallelized.
 *  +------------+----------------------+----------------+
 *  | FIELD_NAME | ARRAYLIST OF List    | OBJECTINITINFO |
 *  +------------+----------------------+----------------+
 *  | XXXXXXXXXX | #1                   | XXXXX          |
 *  |            |         		        | XXXXX          |
 *  |            |                      | XXXXX          |
 *  |            |                      | ...            |
 *  |            | #2                   | XXXXX          |
 *  |            |                      | XXXXX          |
 *  |            |                      | XXXXX          |
 *  +------------+----------------------+----------------+
 *  | XXXXXXXXXX | #1                   | XXXXX          |
 *  |            |         		        | XXXXX          |
 *  |            |                      | XXXXX          |
 *  |            |                      | ...            |
 *  |            | #2                   | XXXXX          |
 *  |            |                      | XXXXX          |
 *  |            |                      | XXXXX          |
 *  |            | ...                  | ...            |
 *  +------------+----------------------+----------------+
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-05-12
 */
public final class ObjectInitHandler {


	/**
	 * ObjectInitHandler class properties
     * <p>
     * listFieldToObject is our data structure that is implemented
     * based on the above description; First we do a lookup on
	 * listField to find fieldname's index and we use it to access
	 * other list data structures
	 */
	private List<String> listField;
	private List<IoTCommCode> listFieldToSetRelation;
	private List<List<ObjectInitInfo>> listFieldToObject;
	private Map<Integer, List<ObjectInitInfo>> mapFieldToSecondObject;
	private int iNumOfFields;
	private boolean bVerbose;


	/**
	 * Empty constructor
	 */
	public ObjectInitHandler(boolean _bVerbose) {

		listField = new ArrayList<String>();
		listFieldToSetRelation = new ArrayList<IoTCommCode>();
		listFieldToObject = new ArrayList<List<ObjectInitInfo>>();
		mapFieldToSecondObject = new HashMap<Integer, List<ObjectInitInfo>>();
		iNumOfFields = 0;
		bVerbose = _bVerbose;
		RuntimeOutput.print("ObjectInitHandler: Creating a new ObjectInitHandler object!", bVerbose);
	}

	/**
	 * Method addField()
	 * <p>
	 * Add a new field
	 *
	 * @param   strField  	String field name
	 * @param   iotcommMsg  Store IoTCommCode from master
	 * @return  void
	 */
	public void addField(String strField, IoTCommCode iotcommMsg) {


		// Add a new object in the list of objects
		listField.add(iNumOfFields, strField);
		listFieldToSetRelation.add(iNumOfFields, iotcommMsg);

		List<ObjectInitInfo> list = new ArrayList<ObjectInitInfo>();
		listFieldToObject.add(iNumOfFields, list);
		if (iotcommMsg == IoTCommCode.CREATE_NEW_IOTRELATION) {
			List<ObjectInitInfo> listSecond = new ArrayList<ObjectInitInfo>();
			mapFieldToSecondObject.put(iNumOfFields, listSecond);
		}
		iNumOfFields++;
	}


	/**
	 * Method addObjectIntoField()
	 * <p>
	 * Add a new field
	 *
	 * @param   strField  					String field name
	 * @param   strIoTSlaveObjectHostAdd  	String IoTSlave object hostname
	 * @param   strObjName  				String object name
	 * @param   strObjClassName  			String object class
	 * @param   strObjClassInterfaceName  	String object class interface
	 * @param	iRMIRegPort					Integer RMI registry port
	 * @param	iRMIStubPort				Integer RMI stub port
	 * @return  void
	 */
	public void addObjectIntoField(String strField, String strIoTSlaveObjectHostAdd,
		String strObjName, String strObjClassName, String strObjClassInterfaceName,
		String strObjStubClsIntfaceName, int iRMIRegPort, int iRMIStubPort) {

		// Get index of strField
		int iFieldIndex = listField.indexOf(strField);

		// Get list structure at index of field
		List<ObjectInitInfo> list = listFieldToObject.get(iFieldIndex);
		// Create a new ObjectInitInfo for a new object in the field
		ObjectInitInfo objInitInfo = new ObjectInitInfo(strIoTSlaveObjectHostAdd, strObjName,
			strObjClassName, strObjClassInterfaceName, strObjStubClsIntfaceName, iRMIRegPort, iRMIStubPort);
		// Add the new ObjectInitInfo
		list.add(objInitInfo);
	}


	/**
	 * Method addSecondObjectIntoField()
	 * <p>
	 * Add a new field
	 *
	 * @param   strField  					String field name
	 * @param   strIoTSlaveObjectHostAdd  	String IoTSlave object hostname
	 * @param   strObjName  				String object name
	 * @param   strObjClassName  			String object class
	 * @param   strObjClassInterfaceName  	String object class interface
	 * @param	iRMIRegPort					Integer RMI registry port
	 * @param	iRMIStubPort				Integer RMI stub port
	 * @return  void
	 */
	public void addSecondObjectIntoField(String strField, String strIoTSlaveObjectHostAdd,
		String strObjName, String strObjClassName, String strObjClassInterfaceName,
		String strObjStubClsIntfaceName, int iRMIRegPort, int iRMIStubPort) {

		// Get index of strField
		int iFieldIndex = listField.indexOf(strField);
		// Get list structure at index of field
		List<ObjectInitInfo> list = mapFieldToSecondObject.get(iFieldIndex);
		// Create a new ObjectInitInfo for a new object in the field
		ObjectInitInfo objInitInfo = new ObjectInitInfo(strIoTSlaveObjectHostAdd, strObjName,
			strObjClassName, strObjClassInterfaceName, strObjStubClsIntfaceName, iRMIRegPort, iRMIStubPort);
		// Add the new ObjectInitInfo
		list.add(objInitInfo);
	}


	/**
	 * Method getNumOfFields()
	 *
	 * @return  int
	 */
	public int getNumOfFields() {

		return iNumOfFields;

	}

	/**
	 * Method getListOfFields()
	 *
	 * @return  List<String> 	List of fields
	 */
	public List<String> getListOfFields() {

		return listField;
	}

	/**
	 * Method getFieldMessage()
	 *
	 * @param   strField  		String field name
	 * @return  IoTCommCode
	 */
	public IoTCommCode getFieldMessage(String strField) {

		return listFieldToSetRelation.get(listField.indexOf(strField));
	}


	/**
	 * Method getListObjectInitInfo()
	 *
	 * @param   strField  				String field name
	 * @return  List<ObjectInitInfo>
	 */
	public List<ObjectInitInfo> getListObjectInitInfo(String strField) {

		return listFieldToObject.get(listField.indexOf(strField));
	}


	/**
	 * Method getSecondObjectInitInfo()
	 *
	 * @param   strField  				String field name
	 * @return  List<ObjectInitInfo>
	 */
	public List<ObjectInitInfo> getSecondObjectInitInfo(String strField) {

		return mapFieldToSecondObject.get(listField.indexOf(strField));
	}


	/**
	 * Method printLists()
	 *
	 * @return  int
	 */
	public void printLists() {

		// Iterate on HostAddress
		for(String s : listField) {

			RuntimeOutput.print("ObjectInitHandler: Field: " + s, bVerbose);
			RuntimeOutput.print("ObjectInitHandler: Message type: " + listFieldToSetRelation.get(listField.indexOf(s)), bVerbose);
			List<ObjectInitInfo> listObject = listFieldToObject.get(listField.indexOf(s));
			List<ObjectInitInfo> listSecObject = mapFieldToSecondObject.get(listField.indexOf(s));

			Iterator it = null;
			if (listFieldToSetRelation.get(listField.indexOf(s)) == IoTCommCode.CREATE_NEW_IOTRELATION) {
				it = listSecObject.iterator();
			}

			for (ObjectInitInfo objInitInfo : listObject) {
				RuntimeOutput.print("ObjectInitHandler: Object info: ", bVerbose);
				RuntimeOutput.print("==> Slave object host address: " + objInitInfo.getIoTSlaveObjectHostAdd(), bVerbose);
				RuntimeOutput.print("==> Object name: " + objInitInfo.getObjectName(), bVerbose);
				RuntimeOutput.print("==> Object class name: " + objInitInfo.getObjectClassName(), bVerbose);
				RuntimeOutput.print("==> Object class interface: " + objInitInfo.getObjectClassInterfaceName(), bVerbose);
				RuntimeOutput.print("==> Object stub class interface: " + objInitInfo.getObjectStubClassInterfaceName(), bVerbose);
				RuntimeOutput.print("==> RMI registry port: " + objInitInfo.getRMIRegistryPort(), bVerbose);
				RuntimeOutput.print("==> RMI stub port: " + objInitInfo.getRMIStubPort(), bVerbose);

				if (listFieldToSetRelation.get(listField.indexOf(s)) == IoTCommCode.CREATE_NEW_IOTRELATION) {
					ObjectInitInfo objSecObj = (ObjectInitInfo) it.next();

					RuntimeOutput.print("ObjectInitHandler: Second object info: ", bVerbose);
					RuntimeOutput.print("==> Slave object host address: " + objSecObj.getIoTSlaveObjectHostAdd(), bVerbose);
					RuntimeOutput.print("==> Object name: " + objSecObj.getObjectName(), bVerbose);
					RuntimeOutput.print("==> Object class name: " + objSecObj.getObjectClassName(), bVerbose);
					RuntimeOutput.print("==> Object class interface: " + objSecObj.getObjectClassInterfaceName(), bVerbose);
					RuntimeOutput.print("==> Object stub class interface: " + objInitInfo.getObjectStubClassInterfaceName(), bVerbose);
					RuntimeOutput.print("==> RMI registry port: " + objSecObj.getRMIRegistryPort(), bVerbose);
					RuntimeOutput.print("==> RMI stub port: " + objSecObj.getRMIStubPort(), bVerbose);				
				}
			}
		}
	}
}
