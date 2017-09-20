package iotinstaller;

// Java libraries
import java.io.*;
import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Properties;

import iotruntime.master.RuntimeOutput;

/** A class that extends Table class to do table operations on IoTSet
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-02-29
 */
public class TableSet extends Table {

	/**
	 * TableSet class properties
	 */
	protected String strWhere;

	/**
	 * Class constructor - for IoTSet (only one table is needed)
	 *
	 * @param     strTblName  String table name that this Table object operates on
	 */
	public TableSet(String strTblName, boolean _bVerbose) {

		super(strTblName, _bVerbose);
	}

	/**
	 * A method to set table name and select entry from a SQL query config file
	 *
	 * @param  strCfgFileName String config file name for device/entity
	 */
	public void setTableSetFromQueryFile(String strQueryFileName, String strObjectID) {

		try {
			// Parse configuration file
			// Assumption here is that .config file is written with the correct syntax (need typechecking)
			File file = new File(strQueryFileName);
			Scanner scanFile = new Scanner(new FileReader(file));
			// String for scanning the file
			String strScan = "";
			while (scanFile.hasNext()) {
				strScan = scanFile.next();
				// if this is for IoTSet table
				if (strScan.equals("SELECT FROM")) {
					// The next token is definitely the table name
					strScan = scanFile.next();
					this.setTableName(strScan);
				}
				// Scan WHERE for either IoTSet or IoTRelation
				if (strScan.equals("WHERE")) {
					// The next token is definitely the WHERE statement
					strScan = "";
					String strWhere = scanFile.next();
					while (!strWhere.equals(";")) {
						strScan = strScan + " " + strWhere;
						strWhere = scanFile.next();
					}
					RuntimeOutput.print("strScan: " + strScan, bVerbose);
					
					if (strObjectID != null) {
						// Object ID for IoTDeviceAddress address selection
						strScan = strScan + " AND ID='" + strObjectID + "'";
					}
					this.setWhereCondition(strScan);
				}
			}

		} catch (FileNotFoundException ex) {

			System.out.println("Table: Exception: ");
			ex.printStackTrace();

		}
	}

	/**
	 * A method to set the String WHERE for a more complex query
	 *
	 * @param     strWhr  String WHERE for a more complex query
	 * @return            void
	 */
	public void setWhereCondition(String strWhr) {

		strWhere = strWhr;

	}

	/**
	 * A method to select entries by giving more complex WHERE in SQL query for IoTSet
	 *
	 * @param  strTableName String table name to create device table
	 * @param  strWhere     String WHERE part of the query
	 * @return              void
	 */
	public void selectSetEntry() {

		// Creating SQL command
		String strCommand = "SELECT * FROM " + strTableName;
		if (strWhere == null) {
			// No condition for query
			strCommand = strCommand + ";";
		} else {
			// Condition for query
			strCommand = strCommand + " WHERE " + strWhere + ";";
		}
		// Execute SQL command
		RuntimeOutput.print("Executing: " + strCommand, bVerbose);
		rs = sqlInterface.sqlCommandQuery(strCommand);
		try {
			rsmd = rs.getMetaData();
		} catch (SQLException ex) {
			System.out.println("Table: Exception: ");
			ex.printStackTrace();
		}
	}

	/**
	 * A method to get number of columns in the table
	 *
	 * @param   iIndex Row number in the ResultSet
	 * @return  integer
	 */
	public int getNumOfCols(int iIndex) {

		int iCnt = 0;
		int iCols = 0;
		try {
			rs.beforeFirst();
			while(rs.next()) {
				iCnt++;
				// Break when reaching the desired location
				if(iCnt > iIndex)
					break;
			}
			// Get the specific class table name and table ID
			// e.g. ProximitySensorBrandC + PS1
			String strClassImplTableID = rs.getString(1);
			String strClassImplTableName = rs.getString(2);
			String strSQLCommand = "SELECT * FROM " + strClassImplTableName +
														 strClassImplTableID + ";";
			ResultSet rsClassImplementation = sqlInterface.sqlCommandQuery(strSQLCommand);
			if(rsClassImplementation.next()) {
				// Get the column type name
				rsmd = rsClassImplementation.getMetaData();
				iCols = rsmd.getColumnCount();
			}
			rs.beforeFirst();
		} catch (SQLException ex) {
			System.out.println("Table: Exception: ");
			ex.printStackTrace();
		}

		return iCols;
	}

	/**
	 * A method to get column data type
	 *
	 * @param   iCol   Column number
	 * @param   iIndex Row number in the ResultSet
	 * @return         String
	 */
	public String getFieldType(int iCol, int iIndex) {

		String strColumnTypeName = "";
		int iCnt = 0;
		try {
			rs.beforeFirst();
			while(rs.next()) {
				iCnt++;
				// Break when reaching the desired location
				if(iCnt > iIndex)
					break;
			}
			// Get the specific class table name and table ID
			// e.g. ProximitySensorBrandC + PS1
			String strClassImplTableID = rs.getString(1);
			String strClassImplTableName = rs.getString(2);
			String strCommand = "SELECT * FROM " + strClassImplTableName +
													strClassImplTableID + ";";
			RuntimeOutput.print(strCommand, bVerbose);
			ResultSet rsClassImplementation = sqlInterface.sqlCommandQuery(strCommand);
			// Get the column type name
			rsmd = rsClassImplementation.getMetaData();
			strColumnTypeName = rsmd.getColumnTypeName(iCol);
			rs.beforeFirst();
		} catch (SQLException ex) {
			System.out.println("Table: Exception: ");
			ex.printStackTrace();
		}

		return strColumnTypeName;
	}

	/**
	 * A method to return a array of String data structure that
	 * contains the list of ID field values of objects
	 *
	 * @return  String[]
	 */
	public String[] getFieldObjectIDs() {

		String[] arrFieldObjectIDs = new String[getNumOfRows()];
		try {
			int iCnt=0;
			rs.beforeFirst();
			while (rs.next()) {
				arrFieldObjectIDs[iCnt] = new String(rs.getString(1));
				iCnt++;
			}
			rs.beforeFirst();
		} catch (SQLException ex) {
			System.out.println("Table: Exception: ");
			ex.printStackTrace();
		}
		return arrFieldObjectIDs;
	}

	/**
	 * A method to return a HashMap data structure that contains the list
	 * of device names
	 * <p>
	 * It matches the device ID in the specific table device, e.g. ProximitySensor
	 * with the name of that device/entry in the main IoTMain table, e.g.
	 * AtmelProximitySensor, GEProximitySensor, etc. These also represent the
	 * class names of these objects
	 *
	 * @return  HashMap<String, String>
	 */
	public HashMap<String, String> getEntryTypes() {

		HashMap<String, String> hmEntryTypes = new HashMap<String, String>();
		try {
			rs.beforeFirst();
			while (rs.next()) {
				hmEntryTypes.put(rs.getString(1), rs.getString(2));
			}
			rs.beforeFirst();
		} catch (SQLException ex) {
			System.out.println("Table: Exception: ");
			ex.printStackTrace();
		}
		return hmEntryTypes;
	}

	/**
	 * A method to return an array data structure representative for DB table
	 * <p>
	 * The outer array structure indexes the inner array structure that
	 * represents a single database entry.
	 *
	 * @return  String[][]
	 */
	//public HashMap<Integer, HashMap<Integer, String>> getDBTable() {
	public String[][] getDBTable() {

		int iCnt = 0;
		int iCols = 0;
		String[] arrTblElement;
		String[][] arrTbl = new String[getNumOfRows()][];
		try {
			rs.beforeFirst();
			while (rs.next()) {
				// Get the class implementation table name from the second column
				// and we compound it with the ID so that we will get a unique name
				// This is to allow a case where we have more than one instance
				// of a device type
				// e.g. ProximitySensorImplPS1 from the table below
				// +------+-----------------------+
				// | ID   | TYPE                  |
				// +------+-----------------------+
				// | PS1  | ProximitySensorImpl   |
				// | PS2  | ProximitySensorBrandC |
				// | PS3  | ProximitySensorBrandD |
				// +------+-----------------------+
				String strClassImplTableID = rs.getString(1);
				String strClassImplTableName = rs.getString(2);
				// We just select everything because there is only one entry
				// to store all the necessary constructor values (if any)
				// If constructor is empty then it returns nothing
				// e.g. ProximitySensorImplPS1
				// +------+-------+
				// | ZONE | POWER |
				// +------+-------+
				// |    0 |   100 |
				// +------+-------+
				String strCommand = "SELECT * FROM " + strClassImplTableName +
														strClassImplTableID + ";";
				RuntimeOutput.print(strCommand, bVerbose);
				ResultSet rsClassImplementation = sqlInterface.sqlCommandQuery(strCommand);
				rsmd = rsClassImplementation.getMetaData();
				iCols = rsmd.getColumnCount();
				arrTblElement = new String[iCols];
				if(rsClassImplementation.next()) {
					for(int i=0; i<iCols; i++) {
						// Extract field information - columns start from 1
						// Store each field value into one table element
						arrTblElement[i] = new String(rsClassImplementation.getString(i+1));
					}
				}
				// Insert one row into the table
				arrTbl[iCnt++] = arrTblElement;
			}
			rs.beforeFirst();
		} catch (SQLException ex) {
			System.out.println("Table: Exception: ");
			ex.printStackTrace();
		}

		return arrTbl;
	}

	/**
	 * A method to close statement manually
	 */
	public void closeStmt() {

		sqlInterface.closeStatement();

	}

	/**
	 * A method to close connection manually
	 */
	public void closeConn() {

		sqlInterface.closeConnection();

	}

	/**
	 * A method to close ResultSet manually
	 */
	public void closeRS() {

		try {
			rs.close();
		} catch (SQLException ex) {
			System.out.println("Table: Exception: ");
			ex.printStackTrace();
		}
	}
}
