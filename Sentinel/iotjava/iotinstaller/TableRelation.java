package iotinstaller;

// Java libraries
import java.io.*;
import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Properties;

import iotruntime.master.RuntimeOutput;

/** A class that extends Table/TableSet class to do table operations on IoTRelation
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-02-29
 */
public final class TableRelation extends TableSet {

	/**
	 * TableRelation class properties
	 */
	protected String strOtherTableName;

	/**
	 * Class constructor - for IoTRelation
	 *
	 * @param     strTblName      	String of first table name that this Table object operates on
	 * @param     strOthTblName   	String of the other table name that this Table object operates on
	 * @param _bVerbose		 		Verboseness of runtime output
	 */
	public TableRelation(String strTblName, String strOthTblName, boolean _bVerbose) {

		super(strTblName, _bVerbose);
		strOtherTableName = strOthTblName;
	}

	/**
	 * A method to create a table for IoTRelation - equivalent of selectSetEntry()
	 * <p>
	 * We always base our search on the communication (IoTComm) table
	 * This function is capable of constructing a more complex SQL query
	 * Note: We check here that strOtherTableName is not NULL; this represents
	 *       that this use of Table object is for IoTRelation
	 *
	 * @return           void
	 */
	public void selectRelationEntry() {

		if (strOtherTableName != null) {

			try {
				String strCommand = "SELECT " + strTableName + ".*, "
														+ strOtherTableName + ".*, "
														+ STR_COMM_TABLE_NAME + ".ACCESS "
														+ "FROM "
														+ strTableName + ", "
														+ strOtherTableName + ", "
														+ STR_COMM_TABLE_NAME
														+ " WHERE "
														+ strTableName + ".ID="
														+ STR_COMM_TABLE_NAME + ".ID_SOURCE"
														+ " AND "
														+ strOtherTableName + ".ID="
														+ STR_COMM_TABLE_NAME + ".ID_DESTINATION";
				// Check for strWhere to construct a more complex
				if (strWhere != null) {
					strCommand = strCommand + " AND " + strWhere;
				}
				strCommand = strCommand + ";";
				RuntimeOutput.print(strCommand, bVerbose);
				rs = sqlInterface.sqlCommandQuery(strCommand);
				rsmd = rs.getMetaData();
			} catch(SQLException ex) {
				System.out.println("Table: Exception: ");
				ex.printStackTrace();
			}
		} else {
			RuntimeOutput.print("Table: The other table name is not set! Illegal use of this method!", bVerbose);
		}
	}

	/**
	 * A method to create a table for IoTRelation and display just the first table
	 * <p>
	 * We always base our search on the communication (IoTComm) table
	 * This function is capable of constructing a more complex SQL query
	 * Note: We check here that strOtherTableName is not NULL; this represents
	 *       that this use of Table object is for IoTRelation
	 *
	 * @return           void
	 */
	public void selectRelationOnFirstTable() {

		if (strOtherTableName != null) {

			try {
				String strCommand = "SELECT " + strTableName + ".* "
														/*+ strOtherTableName + ".*, "
														 + STR_COMM_TABLE_NAME + ".ACCESS "*/
														+ "FROM "
														+ strTableName + ", "
														+ strOtherTableName + ", "
														+ STR_COMM_TABLE_NAME
														+ " WHERE "
														+ strTableName + ".ID="
														+ STR_COMM_TABLE_NAME + ".ID_SOURCE"
														+ " AND "
														+ strOtherTableName + ".ID="
														+ STR_COMM_TABLE_NAME + ".ID_DESTINATION";
				// Check for strWhere to construct a more complex
				if (strWhere != null) {
					strCommand = strCommand + " AND " + strWhere;
				}
				strCommand = strCommand + ";";
				RuntimeOutput.print(strCommand, bVerbose);
				rs = sqlInterface.sqlCommandQuery(strCommand);
				rsmd = rs.getMetaData();
			} catch(SQLException ex) {
				System.out.println("Table: Exception: ");
				ex.printStackTrace();
			}
		} else {

			RuntimeOutput.print("Table: The other table name is not set! Illegal use of this method!", bVerbose);
		}
	}

	/**
	 * A method to create a table for IoTRelation and display just the second table
	 * <p>
	 * We always base our search on the communication (IoTComm) table
	 * This function is capable of constructing a more complex SQL query
	 * Note: We check here that strOtherTableName is not NULL; this represents
	 *       that this use of Table object is for IoTRelation
	 *
	 * @return           void
	 */
	public void selectRelationOnOtherTable() {

		if (strOtherTableName != null) {
			try {
				String strCommand = "SELECT "/*+ strTableName + ".*, "*/
														+ strOtherTableName + ".* "
														/*+ STR_COMM_TABLE_NAME + ".ACCESS "*/
														+ "FROM "
														+ strTableName + ", "
														+ strOtherTableName + ", "
														+ STR_COMM_TABLE_NAME
														+ " WHERE "
														+ strTableName + ".ID="
														+ STR_COMM_TABLE_NAME + ".ID_SOURCE"
														+ " AND "
														+ strOtherTableName + ".ID="
														+ STR_COMM_TABLE_NAME + ".ID_DESTINATION";
				// Check for strWhere to construct a more complex
				if (strWhere != null) {
					strCommand = strCommand + " AND " + strWhere;
				}
				strCommand = strCommand + ";";
				RuntimeOutput.print(strCommand, bVerbose);
				rs = sqlInterface.sqlCommandQuery(strCommand);
				rsmd = rs.getMetaData();
			} catch(SQLException ex) {
				System.out.println("Table: Exception: ");
				ex.printStackTrace();
			}
		} else {
			RuntimeOutput.print("Table: The other table name is not set! Illegal use of this method!", bVerbose);
		}
	}

	/**
	 * A method to set table name and select entry from a SQL query config file
	 *
	 * @param  strCfgFileName String config file name for device/entity
	 */
	public void setTableRelationFromQueryFile(String strQueryFileName) {

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
				// it means that this is for IoTRelation table
				if (strScan.equals("FIRST")) {
					// The next token is definitely the first table name
					strScan = scanFile.next();
					this.setTableName(strScan);
				}
				if (strScan.equals("OTHER")) {
					// The next token is definitely the other table name
					strScan = scanFile.next();
					this.setOtherTableName(strScan);
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
					this.setWhereCondition(strScan);
				}
			}
		} catch (FileNotFoundException ex) {
			System.out.println("Table: Exception: ");
			ex.printStackTrace();
		}
	}

	/**
	 * A method to set the other table name
	 *
	 * @param     strOthTblName  String table name that this Table object operates on
	 * @return                   void
	 */
	public void setOtherTableName(String strOthTblName) {

		strOtherTableName = strOthTblName;

	}

	/**
	 * A method to get the other table name
	 *
	 * @return  String
	 */
	public String getOtherTableName() {

		return strOtherTableName;

	}
}
