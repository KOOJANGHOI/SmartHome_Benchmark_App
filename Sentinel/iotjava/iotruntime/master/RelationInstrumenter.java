package iotruntime.master;

import iotruntime.slave.IoTRelation;

import iotinstaller.MySQLInterface;
import iotinstaller.TableProperty;
import iotinstaller.TableSet;
import iotinstaller.TableRelation;

import java.sql.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import java.lang.Class;
import java.lang.Integer;
import java.lang.reflect.*;

/** Class RelationInstrumenter helps instrument the bytecode.
 *  This class should extract information from the database
 *  Input is the name of the device/entity extracted from the
 *  generic Set class in the bytecode,
 *  e.g. IoTRelation<ProximitySensor, LightBulb>
 *  Upon extracting information, this class can be used to create
 *  an IoTRelation object that contains a list of objects from
 *  the Relation declaration.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2015-12-15
 */
public final class RelationInstrumenter {

	/**
	 * RelationInstrumenter class properties
	 */
	private String[][] arrRelation;
	private String[][] arrRelOther;
	private HashMap<String, String> hmEntryTypes;
	private TableRelation tbl;
	private int iRows;
	private int iCols;
	private int iColsOther;
	private String strRelationEntityName;
	private String strRelationOtherName;
	private boolean bVerbose;

	/**
	 * RelationInstrumenter class constants
	 */
	private final String STR_PACKAGE_PREFIX = "iotcode.";
	private final String STR_FIELD_ID_NAME = "ID";

	/**
	 * Class constructor #1
	 *
	 * @param strRelEntName  String that contains the IoTRelation entity name in the DB, e.g. ProximitySensor
	 * @param strRelOthName  String that contains the other IoTRelation entity name in the DB, e.g. LightBulb
	 * @param _bVerbose		 Verboseness of runtime output
	 */
	public RelationInstrumenter(String strRelEntName, String strRelOthName, boolean _bVerbose) {

		arrRelation = null;
		arrRelOther = null;
		strRelationEntityName = strRelEntName;
		strRelationOtherName = strRelOthName;
		tbl = new TableRelation(strRelationEntityName, strRelationOtherName, _bVerbose);
		iRows = tbl.getNumOfRows();
		iCols = 0;
		iColsOther = 0;
		bVerbose = _bVerbose;
		RuntimeOutput.print("RelationInstrumentation: Creating a Relation for "
			+ strRelationEntityName + " and " + strRelationOtherName, bVerbose);
	}

	/**
	 * Class constructor #2
	 *
	 * @param strRelEntName  	String that contains the IoTRelation entity name in the DB, e.g. ProximitySensor
	 * @param strRelOthName  	String that contains the other IoTRelation entity name in the DB, e.g. LightBulb
	 * @param strQueryFileName  String name for SQL query config file
	 * @param _bVerbose		 	Verboseness of runtime output
	 */
	public RelationInstrumenter(String strRelEntName, String strRelOthName, String strQueryFileName, boolean _bVerbose) {

		arrRelation = null;
		arrRelOther = null;
		strRelationEntityName = strRelEntName;
		strRelationOtherName = strRelOthName;
		tbl = new TableRelation(strRelationEntityName, strRelationOtherName, _bVerbose);
		tbl.setTableRelationFromQueryFile(strQueryFileName);
		tbl.selectRelationEntry();
		iRows = tbl.getNumOfRows();
		iCols = 0;
		iColsOther = 0;
		bVerbose = _bVerbose;
		RuntimeOutput.print("RelationInstrumentation: Creating a Relation for "
			+ strRelationEntityName + " and " + strRelationOtherName, bVerbose);
	}


	/**
	 * A method to give the object/table name of the first set
	 *
	 * @return String
	 */
	public String firstObjectTableName() {

		return strRelationEntityName;

	}

	/**
	 * A method to give the object/table name of the first set
	 *
	 * @return String
	 */
	public String secondObjectTableName() {

		return strRelationOtherName;

	}


	/**
	 * A method to give the number of columns of the first Set
	 *
	 * @param  iIndex  integer index
	 * @return int
	 */
	public int numberOfFirstCols(int iIndex) {

		tbl.selectRelationOnFirstTable();
		iCols = tbl.getNumOfCols(iIndex);
		return iCols;
	}

	/**
	 * A method to give the number of columns of the second Set
	 *
	 * @param  iIndex  integer index
	 * @return int
	 */
	public int numberOfSecondCols(int iIndex) {

		tbl.selectRelationOnOtherTable();
		iColsOther = tbl.getNumOfCols(iIndex);
		return iColsOther;
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
	 * A method to return the entry field TYPE of the first Set based on ID
	 *
	 * @return String
	 */
	public String firstEntryFieldType(String sID) {

		tbl.selectRelationOnFirstTable();
		hmEntryTypes = tbl.getEntryTypes();

		// Get the entry type
		String strEntryType = hmEntryTypes.get(sID);

		return strEntryType;
	}

	/**
	 * A method to return the entry field TYPE of the first Set based on ID
	 *
	 * @return String
	 */
	public String secondEntryFieldType(String sID) {

		tbl.selectRelationOnOtherTable();
		hmEntryTypes = tbl.getEntryTypes();

		// Get the entry type
		String strEntryType = hmEntryTypes.get(sID);

		return strEntryType;
	}

	/**
	 * A method to return the field object ID from the first set entry pointed by certain index
	 *
	 * @param  iIndex  integer index
	 * @return String
	 */
	public String firstFieldObjectID(int iIndex) {

		// Select the first table
		tbl.selectRelationOnFirstTable();

		// Get the right entry based on iIndex
		String[] arrObjectID = tbl.getFieldObjectIDs();
		String strID = arrObjectID[iIndex];

		RuntimeOutput.print("RelationInstrumentation: Extracting field object ID from value..", bVerbose);

		return strID;
	}

	/**
	 * A method to return the field object ID from the second set entry pointed by certain index
	 *
	 * @param  iIndex  integer index
	 * @return String
	 */
	public String secondFieldObjectID(int iIndex) {

		// Select the second table
		tbl.selectRelationOnOtherTable();

		// Get the right entry based on iIndex
		String[] arrObjectID = tbl.getFieldObjectIDs();
		String strID = arrObjectID[iIndex];

		RuntimeOutput.print("RelationInstrumentation: Extracting field object ID from value..", bVerbose);

		return strID;
	}

	/**
	 * A method to return the field values of certain index from the first set
	 *
	 * @param  iIndex  integer index
	 * @return Object[]
	 */
	public Object[] firstFieldValues(int iIndex) {

		// Select the first table
		tbl.selectRelationOnFirstTable();
		arrRelation = tbl.getDBTable();
		iCols = tbl.getNumOfCols(iIndex);

		// Get the right entry based on iIndex
		String[] arrRelEntry = arrRelation[iIndex];

		// Fill in the params array with the Objects needed as parameters
		// for the constructor
		Object[] arrFirstFieldValues = new Object[iCols];

		for(int i=0; i<iCols; i++) {
			// MySQL column starts from 1, NOT 0
			arrFirstFieldValues[i] = getObjectConverted(arrRelEntry[i], getClassName(tbl.getFieldType(i+1, iIndex)).getName());
		}

		RuntimeOutput.print("RelationInstrumentation: Extracting field values..", bVerbose);

		return arrFirstFieldValues;
	}

	/**
	 * A method to return the field values of certain index from second Set
	 *
	 * @param  iIndex  integer index
	 * @return Object[]
	 */
	public Object[] secondFieldValues(int iIndex) {

		// Select the second table
		tbl.selectRelationOnOtherTable();
		arrRelOther = tbl.getDBTable();
		iColsOther = tbl.getNumOfCols(iIndex);

		// Get the right entry based on iIndex
		String[] arrRelEntry = arrRelOther[iIndex];

		// Fill in the params array with the Objects needed as parameters
		// for the constructor
		Object[] arrSecondFieldValues = new Object[iCols];

		for(int i=0; i<iCols; i++) {
			// MySQL column starts from 1, NOT 0
			arrSecondFieldValues[i] = getObjectConverted(arrRelEntry[i], getClassName(tbl.getFieldType(i+1, iIndex)).getName());
		}

		RuntimeOutput.print("RelationInstrumentation: Extracting field values..", bVerbose);

		return arrSecondFieldValues;
	}

	/**
	 * A method to return the field classes of a certain index
	 *
	 * @param  iIndex  integer index
	 * @return Class[]
	 */
	public Class[] firstFieldClasses(int iIndex) {

		// Select the first table
		tbl.selectRelationOnFirstTable();
		arrRelation = tbl.getDBTable();
		iCols = tbl.getNumOfCols(iIndex);

		RuntimeOutput.print("RelationInstrumentation: Extracting table " + strRelationEntityName + ".", bVerbose);

		Class[] arrFirstFieldClasses = new Class[iCols];

		// We start from column 1 and we skip column 0
		// Column 0 is for ID
		for(int i=0; i<iCols; i++) {
			// MySQL column starts from 1, NOT 0
			arrFirstFieldClasses[i] = getClassName(tbl.getFieldType(i+1, iIndex));
		}

		RuntimeOutput.print("RelationInstrumentation: Extracting field classes from field types..", bVerbose);
		return arrFirstFieldClasses;
	}

	/**
	 * A method to return the field classes
	 *
	 * @param  iIndex  integer index
	 * @return Class[]
	 */
	public Class[] secondFieldClasses(int iIndex) {

		// Select the second table
		tbl.selectRelationOnOtherTable();
		arrRelOther = tbl.getDBTable();
		iCols = tbl.getNumOfCols(iIndex);

		RuntimeOutput.print("RelationInstrumentation: Extracting table " + strRelationOtherName + ".", bVerbose);

		Class[] arrSecondFieldClasses = new Class[iCols];

		// We start from column 1 and we skip column 0
		// Column 0 is for ID
		for(int i=0; i<iCols; i++) {
			// MySQL column starts from 1, NOT 0
			arrSecondFieldClasses[i] = getClassName(tbl.getFieldType(i+1, iIndex));
		}

		RuntimeOutput.print("RelationInstrumentation: Extracting field classes from field types..", bVerbose);
		return arrSecondFieldClasses;
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
			return (String) obj;
		} else if (strClassType.equals("int")) {
			return Integer.parseInt((String) obj);
		} else {
			return null;
		}
	}
}
