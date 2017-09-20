package iotinstaller;

import java.sql.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import iotruntime.master.RuntimeOutput;

/** A class that wraps connection to MySQL database
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2015-12-01
 */
public final class MySQLInterface {

	/**
	 * MySQLInterface class properties
	 */
	private static Properties prop;
	private static Connection conn;
	private static Statement stmt;
	private boolean bVerbose;

	/**
	 * MySQLInterface class constants
	 */
	private static final String STR_DB_CLASS_NAME = "com.mysql.jdbc.Driver";
	private static final String STR_CONFIG_FILE = "MySQLInterface.config";
	private static String STR_CONNECTION;
	private static String STR_USERNAME;
	private static String STR_PASSWORD;


	/**
	 * Class constructor
	 */
	public MySQLInterface(boolean _bVerbose) {

		bVerbose = _bVerbose;
		// Parse config file
		// e.g. STR_CONNECTION = "jdbc:mysql://<ip_address/hostname>/IoTMain"
		RuntimeOutput.print("Reading MySQLInterface.config:", bVerbose);
		STR_CONNECTION = "jdbc:mysql://" + parseConfigFile(STR_CONFIG_FILE, "HOST") + "/" +
			parseConfigFile(STR_CONFIG_FILE, "DATABASE");
		RuntimeOutput.print("STR_CONNECTION=" + STR_CONNECTION, bVerbose);
        System.out.println("STR_CONNECTION=" + STR_CONNECTION);
		STR_USERNAME = parseConfigFile(STR_CONFIG_FILE, "USERNAME");
		RuntimeOutput.print("STR_USERNAME=" + STR_USERNAME, bVerbose);
        System.out.println("STR_USERNAME="+ STR_USERNAME);
		STR_PASSWORD = parseConfigFile(STR_CONFIG_FILE, "PASSWORD");
		RuntimeOutput.print("STR_PASSWORD=" + STR_PASSWORD, bVerbose);
        System.out.println("STR_PASSWORD="+STR_PASSWORD);

		try {
			RuntimeOutput.print("MySQLInterface: MySQL interface object creation", bVerbose);
			// Loading JDBC classes and creating a drivermanager class factory
			Class.forName(STR_DB_CLASS_NAME);
			// Properties for user and password
			prop = new Properties();
			prop.put("user", STR_USERNAME);
			prop.put("password", STR_PASSWORD);
			// Now try to connect
			conn = DriverManager.getConnection(STR_CONNECTION, prop);
			RuntimeOutput.print("MySQLInterface: Object successfully created.. connection established!", bVerbose);
		} catch (SQLException | ClassNotFoundException ex) {
			System.out.println("MySQLInterface: Exception: ");
			ex.printStackTrace();
		}
	}


	/**
	 * A method to parse information from a config file
	 *
	 * @param	strCfgFileName	Config file name
	 * @param	strCfgField		Config file field name
	 * @return	String
	 */
	private String parseConfigFile(String strCfgFileName, String strCfgField) {
		// Parse configuration file
		Properties prop = new Properties();
		File file = new File(strCfgFileName);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			prop.load(fis);
			fis.close();
		} catch (IOException ex) {
			System.out.println("IoTMaster: Error reading config file: " + strCfgFileName);
			ex.printStackTrace();
		}
		// NULL is returned if the property isn't found
		return prop.getProperty(strCfgField, null);
	}


	/**
	 * A method to wrap MySQL command execution
	 *
	 * @param  strCommand  string that contains SQL query
	 * @return             void
	 */
	public void sqlCommand(String strCommand) {

		try {
			stmt = conn.createStatement();
			stmt.execute(strCommand);
			stmt.close();
		} catch (SQLException ex) {
			System.out.println("MySQLInterface: Exception: ");
			ex.printStackTrace();
		}
	}

	/**
	 * A method to wrap MySQL command query execution
	 *
	 * @param  strCommand  string that contains SQL query
	 * @return             ResultSet that contains the result of query
	 */
	public ResultSet sqlCommandQuery(String strCommand) {

		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strCommand);
		} catch (SQLException ex) {
			System.out.println("MySQLInterface: Exception: ");
			ex.printStackTrace();
		}
		return rs;
	}

	/**
	 * A method to close statement manually
	 *
	 */
	public void closeStatement() {

		try {

			stmt.close();

		} catch (SQLException ex) {

			System.out.println("MySQLInterface: Exception: ");
			ex.printStackTrace();

		}
	}

	/**
	 * A method to close connection manually
	 *
	 */
	public void closeConnection() {

		try {
			conn.close();
		} catch (SQLException ex) {
			System.out.println("MySQLInterface: Exception: ");
			ex.printStackTrace();
		}
	}

	/**
	 * Getting Connection
	 *
	 * @return SQL connection object
	 */
	protected Connection getConnection() {

		return conn;

	}

	/**
	 * Getting JDBC connector string
	 *
	 * @return String database class name
	 */
	private String getDBClassName() {

		return STR_DB_CLASS_NAME;

	}

	/**
	 * Getting Connection string
	 *
	 * @return SQL connection string
	 */
	private String getConnectionString() {

		return STR_CONNECTION;

	}

	/**
	 * Getting username
	 *
	 * @return String username
	 */
	private String getUsername() {

		return STR_USERNAME;

	}

	/**
	 * Getting password
	 *
	 * @return String password
	 */
	private String getPassword() {

		return STR_PASSWORD;

	}
}
