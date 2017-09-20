package iotruntime.master;

import iotruntime.slave.IoTSet;

import iotinstaller.MySQLInterface;
import iotinstaller.TableProperty;
import iotinstaller.TableSet;

import java.sql.*;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import java.util.Arrays;

import java.lang.Class;
import java.lang.Integer;
import java.lang.reflect.*;

/** Class SetInstrumenter helps instrument the bytecode.
 *  This class should extract information from the database
 *  Input is the name of the device/entity extracted from the
 *  generic Set class in the bytecode, e.g. IoTSet<ProximitySensor>.
 *  Upon extracting information, this class can be used to create
 *  an IoTSet object that contains a list of objects from
 *  the Set declaration.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2015-12-01
 */
public final class SetInstrumenter {

	/**
	 * SetInstrumenter class properties
	 */
	private String[][] arrSet;
	private HashMap<String, String> hmEntryTypes;
	private TableSet tbl;
	private int iRows;
	private int iCols;
	private String strSetEntityName;
	private boolean bVerbose;

	/**
	 * SetInstrumenter class constants
	 */
	private static final String STR_PACKAGE_PREFIX = "iotcode.";
	private static final String STR_FIELD_ID_NAME = "ID";

	/**
	 * Class constructor
	 *
	 * @param strSetEntName     String that contains the IoTSet entity name in the DB, e.g. ProximitySensor
	 * @param strQueryFileName  String name for SQL query config file
	 * @param strObjectID	    String ID to select the right device ID in IoTDeviceAddress table
	 * @param _bVerbose		 	Verboseness of runtime output
	 */
	public SetInstrumenter(String strSetEntName, String strQueryFileName, String strObjectID, boolean _bVerbose) {

		strSetEntityName = strSetEntName;
		tbl = new TableSet(strSetEntName, _bVerbose);
		tbl.setTableSetFromQueryFile(strQueryFileName, strObjectID);
		tbl.selectSetEntry();
		arrSet = tbl.getDBTable();
		hmEntryTypes = tbl.getEntryTypes();
		iRows = tbl.getNumOfRows();
		iCols = 0;
		bVerbose = _bVerbose;
		RuntimeOutput.print("SetInstrumentation: Creating a Set for " + strSetEntityName, bVerbose);
	}


	/**
	 * A method to give the object/table name
	 *
	 * @return String
	 */
	public String getObjTableName() {

		return strSetEntityName;

	}

	/**
	 * A method to give the number of columns
	 *
	 * @param  iIndex  integer index
	 * @return int
	 */
	public int numberOfCols(int iIndex) {

		iCols = tbl.getNumOfCols(iIndex);
		return iCols;
	}

	/**
	 * A method to give the number of rows
	 *
	 * @return int
	 */
	public int numberOfRows() {

		return iRows;

	}

	/**
	 * A method to return the field object ID from entry pointed by certain index
	 *
	 * @param  iIndex  integer index
	 * @return String
	 */
	public String fieldObjectID(int iIndex) {

		// Get the value of that field
		String[] arrObjectID = tbl.getFieldObjectIDs();
		String strID = arrObjectID[iIndex];

		RuntimeOutput.print("RelationInstrumentation: Extracting field object ID from value..", bVerbose);

		return strID;
	}

	/**
	 * A method to return the entry type name from an entry pointed by its ID
	 *
	 * @param  sID     device/entry ID
	 * @return String
	 */
	public String fieldEntryType(String sID) {

		// Get the entry type
		String strEntryType = hmEntryTypes.get(sID);

		RuntimeOutput.print("RelationInstrumentation: Extracting entry type from entry..", bVerbose);

		return strEntryType;
	}

	/**
	 * A method to return the field values of certain index
	 *
	 * @param  iIndex  integer index
	 * @return Object[]
	 */
	public Object[] fieldValues(int iIndex) {

		iCols = tbl.getNumOfCols(iIndex);
		// Get the right entry based on iIndex
		String[] arrSetEntry = arrSet[iIndex];
		Object[] arrFieldValues = new Object[iCols];

		for(int i=0; i<iCols; i++) {
			// MySQL column starts from 1, NOT 0
			arrFieldValues[i] = getObjectConverted(arrSetEntry[i], getClassName(tbl.getFieldType(i+1, iIndex)).getName());
		}

		RuntimeOutput.print("SetInstrumentation: Extracting field values..", bVerbose);

		return arrFieldValues;
	}

	/**
	 * A method to return the field classes of certain index
	 *
	 * @param  iIndex  integer index
	 * @return Class[]
	 */
	public Class[] fieldClasses(int iIndex) {

		iCols = tbl.getNumOfCols(iIndex);
		// Get the right entry set based on iIndex
		RuntimeOutput.print("SetInstrumentation: Extracting table " + strSetEntityName + ".", bVerbose);
		Class[] arrFieldClasses = new Class[iCols];

		for(int i=0; i<iCols; i++) {
			// MySQL column starts from 1, NOT 0
			arrFieldClasses[i] = getClassName(tbl.getFieldType(i+1, iIndex));
		}

		RuntimeOutput.print("SetInstrumentation: Extracting field classes from field types..", bVerbose);
		return arrFieldClasses;
	}

	/**
	 * A helper function that gives the Class object of a particular DB data type
	 *
	 * @param  strDataType  String MySQL data type
	 * @return              Class
	 */
	public Class getClassName(String strDataType) {

		if (strDataType.equals("VARCHAR")) {
			return String.class;
		} else if (strDataType.equals("INT")) {
			return int.class;
		} else {
			return null;
		}
	}

	/**
	 * A helper function that returns an Object in the right format
	 * <p>
	 * We give it input from the elements of the HashSet where we
	 * populate all the DB information for a certain Object
	 *
	 * @param  obj           Object to be converted
	 * @param  strClassType  String Java Class type
	 * @return               Object
	 */
	public Object getObjectConverted(Object obj, String strClassType) {

		if (strClassType.equals("java.lang.String")) {
			// We use String "true" or "false" as booleans in MySQL
			String strObj = (String) obj;
			if (strObj.equals("true") || strObj.equals("false")) {
			// Check if this is a boolean
				return Boolean.parseBoolean(strObj);
			} else {
			// Return just the string if it's not a boolean
				return strObj;
			}
		} else if (strClassType.equals("int")) {
			return Integer.parseInt((String) obj);
		} else {
			return null;
		}
	}
}
