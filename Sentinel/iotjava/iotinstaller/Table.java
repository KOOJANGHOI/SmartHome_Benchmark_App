package iotinstaller;

import iotinstaller.MySQLInterface;
import iotinstaller.TableProperty;
import iotruntime.master.RuntimeOutput;

// Java libraries
import java.io.*;
import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Properties;

/** A class that does table related operations in a Table object
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2015-12-07
 */
public class Table {

	/**
	 * Table class properties
	 */
	protected MySQLInterface sqlInterface;
	protected String strTableName;
	protected String strWhere;
	protected ResultSet rs;
	protected ResultSetMetaData rsmd;
	protected boolean bVerbose;

	/**
	 * Table class constants
	 */
	protected final static String STR_COMM_TABLE_NAME = "IoTComm";
	protected final static String STR_MAIN_TABLE_NAME = "IoTMain";

	/**
	 * Class constructor #1
	 */
	public Table(boolean _bVerbose) {

		sqlInterface = new MySQLInterface(_bVerbose);
		strTableName = null;
		strWhere = null;
		rs = null;
		rsmd = null;
		bVerbose = _bVerbose;
	}

	/**
	 * Class constructor #2 - with table name specified
	 *
	 * @param     strTblName  String table name that this Table object operates on
	 */
	public Table(String strTblName, boolean _bVerbose) {

		try {
			sqlInterface = new MySQLInterface(_bVerbose);
			strTableName = strTblName;
			strWhere = null;
			rs = sqlInterface.sqlCommandQuery("SELECT * FROM " + strTableName + ";");
			rsmd = rs.getMetaData();
			bVerbose = _bVerbose;
		} catch(SQLException ex) {
			System.out.println("Table: Exception: ");
			ex.printStackTrace();
		}
	}

	/**
	 * A method to set table name
	 *
	 * @param     strTableName  String table name that this Table object operates on
	 * @return                  void
	 */
	public void setTableName(String strTblName) {

		strTableName = strTblName;

	}

	/**
	 * A method to get table name
	 *
	 * @return  String
	 */
	public String getTableName() {

		return strTableName;

	}



	/**
	 * A method to create a new table (Table object)
	 *
	 * @param  tp               array of TableProperty class to construct query
	 * @param  strUniqueField   field that is unique in this table
	 * @return                  void
	 */
	public void createTable(TableProperty[] tp, String strUniqueField) {

		// Creating SQL command
		String strCommand = "CREATE TABLE " + strTableName + " (";
		// Iterate along the array tp to construct '<field> VARCHAR(<length>)' string
		for(int i=0; i<tp.length; i++) {
			strCommand = strCommand + tp[i].getField() +
				" " + tp[i].getType() + "(" + tp[i].getLength() + ")";
			// Add ', ' except for the last entry in the array
			if (i<tp.length-1) {
				strCommand = strCommand + ", ";
			}
		}
		strCommand = strCommand + ");";
		// Execute SQL command
		sqlInterface.sqlCommand(strCommand);
		// Assuming that there is always a PK column for each table
		// This has to be made unique
		if (strUniqueField != null) {
			sqlInterface.sqlCommand("ALTER IGNORE TABLE " + strTableName + " ADD UNIQUE(" + strUniqueField +");");
		}
		RuntimeOutput.print("Table: Creating a new entity/device table", bVerbose);
	}

	/**
	 * A method to insert a record into a table for a specific device
	 *
	 * @param  strFieldVals  array of String that contains field values of a table
	 * @return               void
	 */
	public void insertEntry(String[] strFieldVals) {

		// Creating SQL command
		String strCommand = "INSERT INTO " + strTableName + " VALUES (";
		// Iterate along the array strFields to construct '<field>' string
		for(int i=0; i<strFieldVals.length; i++) {
			strCommand = strCommand + "'" + strFieldVals[i] + "'";

			// Add ', ' except for the last entry in the array
			if (i<strFieldVals.length-1) {
				strCommand = strCommand + ", ";
			}
		}
		strCommand = strCommand + ");";
        System.out.println("STRCOMMAND : " + strCommand);
		// Execute SQL command
		sqlInterface.sqlCommand(strCommand);
		RuntimeOutput.print("Table: Inserting a new entry into " + strTableName + "..", bVerbose);
	}

	/**
	 * A method to delete a record into a table for a specific device
	 *
	 * @param  strWhere  String WHERE part of the query
	 * @return           void
	 */
	public void deleteEntry(String strWhere) {

		// Creating SQL command
		String strCommand = "DELETE FROM " + strTableName;
		if (strWhere == null) {
			// No condition for query
			strCommand = strCommand + ";";
		} else {
			// Condition for query
			strCommand = strCommand + " WHERE " + strWhere + ";";
		}
		// Execute SQL command
		sqlInterface.sqlCommand(strCommand);
		RuntimeOutput.print("Table: Deleting entry from " + strTableName + "..", bVerbose);
	}

	/**
	 * A method to drop a table
	 *
	 * @return           void
	 */
	public void dropTable() {

		// Creating SQL command
		String strCommand = "DROP TABLE " + strTableName;
		// Execute SQL command
		sqlInterface.sqlCommand(strCommand);
		RuntimeOutput.print("Table: Dropping table " + strTableName + "..", bVerbose);
	}

	/**
	 * A method to check table existence in the database
	 *
	 * @return           boolean
	 */
	public boolean isTableExisting() {

		// Assume table does not exist
		boolean bExist = false;
		// Creating SQL command
		String strCommand = "SHOW TABLES LIKE '" + strTableName + "';";
		// Execute SQL command
		rs = sqlInterface.sqlCommandQuery(strCommand);
		try {
			if (rs != null) {
				rs.beforeFirst();
				if (rs.next()) {
					// Table does exist
					bExist = true;
				}
				rs.beforeFirst();
			}
		} catch (SQLException ex) {
			System.out.println("Table: Exception: ");
			ex.printStackTrace();
		}

		return bExist;
	}

	/**
	 * A method to return ResultSet
	 *
	 * @return           ResultSet
	 */
	public ResultSet getResultSet() {

		return rs;

	}

	/**
	 * A method to check if table is empty
	 *
	 * @return           boolean
	 */
	public boolean isTableEmpty() {

		if (rs == null) {
			return true;
		}
		return false;

	}

	/**
	 * A method to get number of rows in the table
	 *
	 * @return           integer
	 */
	public int getNumOfRows() {

		int iRows = 0;
		try {
			rs.first();
			if (rs.last()) {
				iRows = rs.getRow();
				rs.beforeFirst();
			}
		} catch (SQLException ex) {
			System.out.println("Table: Exception: ");
			ex.printStackTrace();
		}
		return iRows;
	}

	/**
	 * A method to get number of columns in general table
	 * <p>
	 * This doesn't do 2-round lookup as it does for device driver table
	 *
	 * @return  integer
	 */
	public int getGeneralNumOfCols() {

		int iCols = 0;
		try {
			rsmd = rs.getMetaData();
			iCols = rsmd.getColumnCount();
		} catch (SQLException ex) {
			System.out.println("Table: Exception: ");
			ex.printStackTrace();
		}
		return iCols;
	}

	/**
	 * A method to return a narray data structure representative for DB table
	 * <p>
	 * This works just like getDBTable() but for other tables in general
	 * It does not do 2-round process as it does for device driver table lookup
	 *
	 * @return  String[][]
	 */
	public String[][] getGeneralDBTable() {

		int iCnt = 0;
		int iCols = getGeneralNumOfCols();
		String[] arrTblElement = new String[iCols];
		String[][] arrTbl = new String[getNumOfRows()][];

		try {
			rs.beforeFirst();
			while (rs.next()) {
				arrTblElement = new String[iCols];
				for(int i=0; i<iCols; i++) {
					// Extract field information - columns start from 1
					// Store each field value into one table element
					arrTblElement[i] = new String(rs.getString(i+1));
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
