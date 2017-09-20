package iotinstaller;

import iotinstaller.MySQLInterface;
import iotinstaller.TableProperty;
import iotinstaller.Table;

import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.util.Properties;

/** A class that creates an object for IoT device/entity installation into database
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2015-12-01
 */
public final class IoTInstaller {

	/**
	 * IoTInstaller class properties
	 */
	private Table tbl;

	/**
	 * IoTInstaller class constants
	 */
	private static final String STR_MAIN_TABLE_NAME = "IoTMain";
	private static final String STR_COMM_TABLE_NAME = "IoTComm";
	private static final String STR_HOST_TABLE_NAME = "IoTComputeNode";
	private static final String STR_ADDRESS_TABLE_NAME = "IoTAddress";
	private static final String STR_DEV_ADD_TABLE_NAME = "IoTDeviceAddress";
	private static final String STR_ZB_ADD_TABLE_NAME = "IoTZigbeeAddress";
	private static final int INT_NUM_COMM_FIELDS = 5;
	private static final int INT_NUM_HOST_FIELDS = 3;

	private static final String STR_INSTALL_ENTITY_CMD = "-install_ent";
	private static final String STR_INSTALL_COMMUNICATION_CMD = "-install_comm";
	private static final String STR_INSTALL_COMPLETE_CMD = "-install_comp";
	private static final String STR_INSTALL_ADDRESS_CMD = "-install_add";
	private static final String STR_INSTALL_DEV_ADDRESS_CMD = "-install_dev_add";
	private static final String STR_INSTALL_ZB_ADDRESS_CMD = "-install_zb_add";
	private static final String STR_INSTALL_HOST_CMD = "-install_host";
	private static final String STR_DELETE_ENTITY_CMD = "-delete_ent";
	private static final String STR_DELETE_ADDRESS_CMD = "-delete_add";
	private static final String STR_DELETE_DEV_ADD_CMD = "-delete_dev_add";
	private static final String STR_DELETE_ZB_ADD_CMD = "-delete_zb_add";
	private static final String STR_DELETE_HOST_CMD = "-delete_host";
	private static final String STR_HELP_CMD = "-help";

	/**
	 * Class constructor
	 */
	public IoTInstaller() {

		// Make this not verbose by default
		tbl = new Table(false);
		System.out.println("IoTInstaller: Initializing installation..");
	}

	/**
	 * A method to insert a new entry to the main table (IoTMain)
	 * <p>
	 * This entry can be a new device or a new entity that we should keep track about
	 * A new entry will need a unique ID and a type name from the driver
	 *
	 * @param  strID    string ID to insert device into the main table
	 * @param  strType  string type to insert device into the main table
	 * @return          void
	 */
	private void insertMainDBEntry(String strID, String strType) {

		// Creating String array
		String[] strFlds = new String[2];
		for(int i=0; i<2; i++) {
			strFlds[i] = new String();
		}
		strFlds[0] = strID;
		strFlds[1] = strType;

		// Insert entry through Table object
		tbl.setTableName(STR_MAIN_TABLE_NAME);
		tbl.insertEntry(strFlds);
		System.out.println("IoTInstaller: Inserting a new entry into main table");
	}

	/**
	 * A method to extract device/entity information from the user
	 * <p>
	 * Users are supposed to supply the information needed for installation
	 *
	 * @param  strCfgFileName String config file name for device/entity
	 * @return                void
	 */
	public void extractTableAndInstall(String strCfgFileName) {
		// TO DO: WE PROBABLY NEED TO IMPROVE THE FILE PARSING BUT FOR NOW KEEP IT MINIMUM

		try {

			// Parse configuration file
			// Assumption here is that .config file is written with the correct syntax (need typechecking)
			File file = new File(strCfgFileName);
			Scanner scanFile = new Scanner(new FileReader(file));
			System.out.println("IoTInstaller: Extracting information from config file: " + strCfgFileName);

			// Initialize String for ID and TYPE
			String strID = "";
			String strType = "";
			String strTypeSpecific = "";

			// Initialize TableProperty for devices and specific devices
			// We have 2 tables,
			// e.g. ProximitySensor - table of many ProximitySensor devices
			//      ProximitySensorBrandA - table that contains the constructor
			//                              information for a specific device
			TableProperty[] tpDevice = new TableProperty[1];
			TableProperty[] tpDeviceSpecific = new TableProperty[1];

			// Initialize array of string
			String[] strFields = new String[1];
			String[] strFieldsSpecific = new String[1];

			// String for scanning the file
			String strScan = "";

			// Store number of fields here
			int iFields = 0;
			while (scanFile.hasNext()) {

				strScan = scanFile.next();
				if (strScan.equals("IoTMain")) {

					while (scanFile.hasNext()) {
						strScan = scanFile.next();

						// Get ID
						if (strScan.equals("ID")) {
							strID = scanFile.next();
						}
						// Get TYPE
						else if (strScan.equals("TYPE")) {
							strType = scanFile.next();
						}
						// Get TYPE
						else if (strScan.equals("TYPESPECIFIC")) {
							strTypeSpecific = scanFile.next();
						} else if (strScan.equals("END")) {
							// Break out of loop
							break;
						}
					}
				} else if (strScan.equals("Table")) {

					// Get number of fields, e.g. Table 3
					iFields = scanFile.nextInt();

					// We have device ID and device specific names
					// e.g. ID = PS1; TYPE
					tpDevice = new TableProperty[2];
					tpDevice[0] = new TableProperty();
					tpDevice[0].setField("ID");
					tpDevice[0].setType("VARCHAR");
					tpDevice[0].setLength("5");
					tpDevice[1] = new TableProperty();
					tpDevice[1].setField("TYPE");
					tpDevice[1].setType("VARCHAR");
					tpDevice[1].setLength("30");

					// Prepare properties for a specific device
					tpDeviceSpecific = new TableProperty[iFields];
					for (int i=0; i<iFields; i++) {
						tpDeviceSpecific[i] = new TableProperty();

						// Looping over the fields
						strScan = scanFile.next();
						tpDeviceSpecific[i].setField(strScan);
						strScan = scanFile.next();
						tpDeviceSpecific[i].setType(strScan);
						strScan = scanFile.next();
						tpDeviceSpecific[i].setLength(strScan);
					}
				} else if (strScan.equals("Data")) {

					// Get the device information
					strFields = new String[2];
					strFields[0] = strID;
					strFields[1] = strTypeSpecific;

					if ((tpDeviceSpecific.length == 1) &&
							(tpDeviceSpecific[0].getField().equals("EMPTY"))) {

						// Get the fields for specific device
						strFieldsSpecific = null;
						System.out.println("IoTInstaller: Empty constructor for: " + strTypeSpecific);

					} else {

						// Get the fields for specific device
						strFieldsSpecific = new String[iFields];
						for (int i=0; i<iFields; i++) {
							strScan = scanFile.next();
							strFieldsSpecific[i] = strScan;
						}
					}
				}
			}

			installNewEntity(strType, strTypeSpecific, strID, tpDevice,
											 tpDeviceSpecific, strFields, strFieldsSpecific);
			System.out.println("IoTInstaller: Installing a new entity/device into the system");

		} catch (FileNotFoundException ex) {

			System.out.println("IoTInstaller: Exception: ");
			ex.printStackTrace();

		}
	}

	/**
	 * A method to install a new entity/device into the database
	 * <p>
	 * 1) Insert this device/entity into the main table IoTMain
	 * 2) Create a new device/entity table if it doesn't exist yet
	 * 3) Insert this entry into the specific device/entity table
	 *
	 * @param  strType           String device type
	 * @param  strTypeSpecific   String device specific type
	 * @param  strID             String unique device/entity ID
	 * @param  tpDevice          array of TableProperty to construct the new table
	 * @param  tpDeviceSpecific  array of TableProperty to construct the new table
	 * @param  strFields         field values of device table
	 * @param  strFieldsSpecific field values of device specific table
	 */
	private void installNewEntity(String strType, String strTypeSpecific, String strID,
		TableProperty[] tpDevice, TableProperty[] tpDeviceSpecific, String[] strFields, String[] strFieldsSpecific) {

		// Create a new IoTInstaller object
		System.out.println("IoTInstaller: Installing device " + strType + " with specific type " + strTypeSpecific);
		tbl.setTableName(strType);

		// 1) Insert this device/entity into the main table IoTMain
		insertMainDBEntry(strID, strType);

		// Device table
		// 2) Create a new device/entity table if it doesn't exist yet
		tbl.setTableName(strType);
		if (tbl.isTableExisting()) {
			// table does exist
			System.out.println("IoTInstaller: Table " + strType + " exists.. just insert new entry!");
		} else {
			// table does not exist yet
			tbl.createTable(tpDevice, "ID");
		}

		// 3) Insert this entry into the device/entity table
		tbl.insertEntry(strFields);

		// Device specific table
		// 2) Create a new device/entity table if it doesn't exist yet
		// P.S. We should assume that table doesn't exist yet, and we throw error otherwise!
		tbl.setTableName(strTypeSpecific + strID);
		tbl.createTable(tpDeviceSpecific, null);

		// 3) Insert this entry into the device/entity table
		if (strFieldsSpecific != null) {
			tbl.insertEntry(strFieldsSpecific);
		}
	}

	/**
	 * A method to extract device/entity communication configuration from the user
	 * <p>
	 * Users are supposed to supply the information needed for installation
	 *
	 * @param  strCfgFileName String config file name for device/entity
	 * @return                void
	 */
	public void extractCommAndInstall(String strCfgFileName) {
		// TODO: WE PROBABLY NEED TO IMPROVE THE FILE PARSING BUT FOR NOW KEEP IT MINIMUM

		try {

			// Parse configuration file
			// Assumption here is that .config file is written with the correct syntax (need typechecking)
			File file = new File(strCfgFileName);
			Scanner scanFile = new Scanner(new FileReader(file));

			System.out.println("IoTInstaller: Extracting information from config file: " + strCfgFileName);

			// Field counter
			int iFieldCnt = 0;

			// Initialize array of string
			String[] strFields = new String[INT_NUM_COMM_FIELDS];
			for(int i=0; i<INT_NUM_COMM_FIELDS; i++) {
				strFields[i] = new String();
			}
			while (scanFile.hasNext() && (iFieldCnt < INT_NUM_COMM_FIELDS)) {

				strFields[iFieldCnt++] = scanFile.next();
			}

			// Create a new installer object
			tbl.setTableName(STR_COMM_TABLE_NAME);
			tbl.insertEntry(strFields);

			System.out.println("IoTInstaller: Installing a new communication pattern into the system");

		} catch (FileNotFoundException ex) {

			System.out.println("IoTInstaller: Exception: ");
			ex.printStackTrace();

		}
	}

	/**
	 * A method to extract device/entity addresses information
	 * <p>
	 * Users are supposed to supply the information needed for installation
	 *
	 * @param  strCfgFileName String config file name for device/entity
	 * @return                void
	 */
	public void installDeviceAddress(String strCfgFileName) {

		try {

			// Parse configuration file
			Properties prop = new Properties();
			File file = new File(strCfgFileName);
			FileInputStream fis = new FileInputStream(file);
			try {
				prop.load(fis);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			System.out.println("IoTInstaller: Extracting information from config file: " + strCfgFileName);
			// Initialize string
			// We can only install one device address per one time with the following sequence
			String[] strFields = new String[2];
			String[] strFieldsAddress = null;
			// Check for wildcard feature
			if ((prop.getProperty("SOURCEWILDCARD", null) != null) &&
				(prop.getProperty("DESTWILDCARD", null) != null)) {
				strFieldsAddress = new String[5];
				strFieldsAddress[3] = prop.getProperty("SOURCEWILDCARD");
				strFieldsAddress[4] = prop.getProperty("DESTWILDCARD");
			} else {
				strFieldsAddress = new String[3];
			}
			strFields[0] = prop.getProperty("ID");
			strFields[1] = prop.getProperty("ADDRESSFOR");
			strFieldsAddress[0] = prop.getProperty("DEVICEADDRESS");
			strFieldsAddress[1] = prop.getProperty("PORTNUMBER");
			strFieldsAddress[2] = prop.getProperty("PROTOCOL");

			// Insert this entry into the main device address table
			tbl.setTableName(STR_DEV_ADD_TABLE_NAME);
			tbl.insertEntry(strFields);

			// Create a new table for a specific device address
			// e.g. AmcrestCameraAdd + CM1 = AmcrestCameraAddCM1
			tbl.setTableName(strFields[1] + strFields[0]);

			// Table does not exist yet
			// Set TableProperty for device address (MAC address)
			TableProperty[] tp = null;
			// Check for wildcard feature
			if (strFieldsAddress.length == 5) {
				tp = new TableProperty[5];
				tp[3] = new TableProperty();
				tp[3].setField("SOURCEWILDCARD");
				tp[3].setType("VARCHAR");
				tp[3].setLength("5");
				tp[4] = new TableProperty();
				tp[4].setField("DESTWILDCARD");
				tp[4].setType("VARCHAR");
				tp[4].setLength("5");
			} else {
				tp = new TableProperty[3];
			}
			tp[0] = new TableProperty();
			tp[0].setField("DEVICEADDRESS");
			tp[0].setType("VARCHAR");
			tp[0].setLength("20");
			tp[1] = new TableProperty();
			tp[1].setField("PORTNUMBER");
			tp[1].setType("INT");
			tp[1].setLength("11");
			tp[2] = new TableProperty();
			tp[2].setField("PROTOCOL");
			tp[2].setType("VARCHAR");
			tp[2].setLength("5");
			tbl.createTable(tp, "DEVICEADDRESS");

			// Insert new address entry
			tbl.insertEntry(strFieldsAddress);

			System.out.println("IoTInstaller: Installing a new device/entity address into the system");

		} catch (FileNotFoundException ex) {

			System.out.println("IoTInstaller: Exception: ");
			ex.printStackTrace();

		}
	}

	/**
	 * A method to extract Zigbee device addresses information
	 * <p>
	 * Users are supposed to supply the information needed for installation
	 *
	 * @param  strCfgFileName String config file name for device/entity
	 * @return                void
	 */
	public void installZigbeeAddress(String strCfgFileName) {

		try {

			// Parse configuration file
			Properties prop = new Properties();
			File file = new File(strCfgFileName);
			FileInputStream fis = new FileInputStream(file);
			try {
				prop.load(fis);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			System.out.println("IoTInstaller: Extracting information from config file: " + strCfgFileName);
			// Initialize string
			// We can only install one device address per one time with the following sequence
			String[] strFields = new String[2];
			String[] strFieldsAddress = new String[1];
			strFields[0] = prop.getProperty("ID");
			strFields[1] = prop.getProperty("ADDRESSFOR");
			strFieldsAddress[0] = prop.getProperty("DEVICEADDRESS");

			// Insert this entry into the main device address table
			tbl.setTableName(STR_ZB_ADD_TABLE_NAME);
            System.out.println("strfields: " +strFields);
			tbl.insertEntry(strFields);

			// Create a new table for a specific device address
			// e.g. AmcrestCameraZBAdd + CM1 = AmcrestCameraZBAddCM1
			tbl.setTableName(strFields[1] + strFields[0]);

			// Table does not exist yet
			// Set TableProperty for device address (MAC address)
			TableProperty[] tp = new TableProperty[1];
			tp[0] = new TableProperty();
			tp[0].setField("DEVICEADDRESS");
			tp[0].setType("VARCHAR");
			tp[0].setLength("25");
			tbl.createTable(tp, "DEVICEADDRESS");

			// Insert new address entry
			tbl.insertEntry(strFieldsAddress);

			System.out.println("IoTInstaller: Installing a new device/entity address into the system");

		} catch (FileNotFoundException ex) {

			System.out.println("IoTInstaller: Exception: ");
			ex.printStackTrace();

		}
	}


	/**
	 * A method to extract simple addresses information, e.g. www.google.com
	 * <p>
	 * Users are supposed to supply the information needed for installation
	 *
	 * @param  strCfgFileName String config file name for device/entity
	 * @return                void
	 */
	public void installAddress(String strCfgFileName) {

		try {

			// Parse configuration file
			Properties prop = new Properties();
			File file = new File(strCfgFileName);
			FileInputStream fis = new FileInputStream(file);
			try {
				prop.load(fis);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			System.out.println("IoTInstaller: Extracting information from config file: " + strCfgFileName);
			// Initialize string
			// We can only install one device address per one time with the following sequence
			String[] strFields = new String[2];
			String[] strFieldsAddress = new String[1];
			strFields[0] = prop.getProperty("ID");
			strFields[1] = prop.getProperty("ADDRESSFOR");
			strFieldsAddress[0] = prop.getProperty("ADDRESS");

			// Insert this entry into the main device address table
			tbl.setTableName(STR_ADDRESS_TABLE_NAME);
			tbl.insertEntry(strFields);

			// Create a new table for a specific device address
			// e.g. WeatherForecastAdd + WF1 = WeatherForecastAddCM1
			tbl.setTableName(strFields[1] + strFields[0]);

			// Table does not exist yet
			// Set TableProperty for device address (MAC address)
			TableProperty[] tp = new TableProperty[1];
			tp[0] = new TableProperty();
			tp[0].setField("ADDRESS");
			tp[0].setType("VARCHAR");
			tp[0].setLength("50");
			tbl.createTable(tp, "ADDRESS");

			// Insert new address entry
			tbl.insertEntry(strFieldsAddress);

			System.out.println("IoTInstaller: Installing a new device/entity address into the system");

		} catch (FileNotFoundException ex) {

			System.out.println("IoTInstaller: Exception: ");
			ex.printStackTrace();

		}
	}

	/**
	 * A method to extract host information for host installation
	 * <p>
	 * Users are supposed to supply the information needed for installation
	 *
	 * @param  strCfgFileName String config file name for device/entity
	 * @return                void
	 */
	public void installHost(String strCfgFileName) {
		try {
			// Parse configuration file
			Properties prop = new Properties();
			File file = new File(strCfgFileName);
			FileInputStream fis = new FileInputStream(file);
			try {
				prop.load(fis);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			System.out.println("IoTInstaller: Extracting information from config file: " + strCfgFileName);
			// Initialize array of string
			String[] strFields = new String[3];
			strFields[0] = prop.getProperty("HOSTADDRESS");
			strFields[1] = prop.getProperty("PROCESSOR");
			strFields[2] = prop.getProperty("MEMORY");
			// Create a new installer object
			tbl.setTableName(STR_HOST_TABLE_NAME);
			tbl.insertEntry(strFields);

			System.out.println("IoTInstaller: Installing a new host into the system");

		} catch (FileNotFoundException ex) {

			System.out.println("IoTInstaller: Exception: ");
			ex.printStackTrace();

		}
	}

	/**
	 * A method to delete host information from database by putting in host address
	 *
	 * @param  strHostAddress String for host address
	 * @return                void
	 */
	public void deleteHost(String strHostAddress) {

		tbl.setTableName(STR_HOST_TABLE_NAME);
		String strWhere = "HOSTADDRESS='" + strHostAddress + "';";
		tbl.deleteEntry(strWhere);

		System.out.println("IoTInstaller: Deleting a host from the system");
	}

	/**
	 * A method to delete entity information from database
	 *
	 * @param  strEntID		String for entity ID
	 * @param  strEntType	String for entity type
	 * @param  strEntName	String for entity name
	 * @return            	void
	 */
	public void deleteEntity(String strEntID, String strEntType, String strEntName) {

		// Delete from table IoTMain
		tbl.setTableName(STR_MAIN_TABLE_NAME);
		String strWhere = "ID='" + strEntID + "';";
		tbl.deleteEntry(strWhere);
		System.out.println("IoTInstaller: Removing entity from table " + STR_MAIN_TABLE_NAME);

		// Delete from table with type name, e.g. Camera
		tbl.setTableName(strEntType);
		strWhere = "ID='" + strEntID + "';";
		tbl.deleteEntry(strWhere);
		System.out.println("IoTInstaller: Removing entity from table type: " + strEntType);
		// Drop table if this was the last entry
		if (tbl.isTableEmpty()) {
			tbl.dropTable();
			System.out.println("IoTInstaller: Dropping the table.. It was the last entry!");
		}

		// Drop the table that contains constructor information
		tbl.setTableName(strEntName + strEntID);
		tbl.dropTable();
		System.out.println("IoTInstaller: Dropping class constructor table...");

		System.out.println("IoTInstaller: Deleting an entry from the system...");
	}

	/**
	 * A method to delete address information from database
	 *
	 * @param  strTableName		String for main table, i.e. IoTAddress, IoTDeviceAddress, or IoTZigbeeAddress
	 * @param  strEntID			String for entity ID, e.g. CM1
	 * @param  strEntAddType	String for entity address type, e.g. AmcrestCameraAdd
	 * @return					void
	 */
	public void deleteAddress(String strTableName, String strEntID, String strEntAddType) {

		// Delete from main table, e.g. IoTAddress, IoTDeviceAddress, or IoTZigbeeAddress
		tbl.setTableName(strTableName);
		String strWhere = "ID='" + strEntID + "';";
		tbl.deleteEntry(strWhere);
		System.out.println("IoTInstaller: Removing entity from table " + strTableName);

		// Drop the table that contains constructor information
		tbl.setTableName(strEntAddType + strEntID);
		tbl.dropTable();
		System.out.println("IoTInstaller: Dropping class constructor table...");

		System.out.println("IoTInstaller: Deleting an entry from the system...");
	}


	/**
	 * A method to install a pair of new devices with their communication pattern
	 *
	 * @param  strFirstDeviceFile  String that contains the file name of the fist device
	 * @param  strSecondDeviceFile String that contains the file name of the second device
	 * @param  strCommFile         String that contains the file name of the communication file
	 * @return                     void
	 */
	public void installPairOfEntities(String strFirstEntityFile,
		String strSecondEntityFile, String strCommFile) {
		// TODO: NEED TO DO THE INPUT FAILURE CHECKING HERE
		// NOW JUST ASSUME THAT THE INPUT FILES ARE GOOD

		extractTableAndInstall(strFirstEntityFile);
		extractTableAndInstall(strSecondEntityFile);
		extractCommAndInstall(strCommFile);
	}

	/**
	 * A method to output help messages
	 *
	 * @return void
	 */
	private void helpMessages() {
		System.out.println();
		System.out.println("IoTInstaller: Command line options:");
		System.out.println("IoTInstaller: 1) Install one device, e.g. java iotinstaller.IoTInstaller -install_ent <filename>");
		System.out.println("IoTInstaller: 2) Install comm pattern, e.g. java iotinstaller.IoTInstaller -install_comm <filename>");
		System.out.print("IoTInstaller: 3) Install two devices and comm pattern, e.g. java iotinstaller.IoTInstaller ");
		System.out.println("-install_comp <first_entity_filename> <second_entity_filename> <communication_filename>");
		System.out.println("IoTInstaller: 4) Install address, e.g. java iotinstaller.IoTInstaller -install_add <filename>");
		System.out.println("IoTInstaller: 5) Install device address, e.g. java iotinstaller.IoTInstaller -install_dev_add <filename>");
		System.out.println("IoTInstaller: 6) Install zigbee device address, e.g. java iotinstaller.IoTInstaller -install_zb_add <filename>");
		System.out.println("IoTInstaller: 7) Install host, e.g. java iotinstaller.IoTInstaller -install_host <filename>");
		System.out.println("IoTInstaller: 8) Delete entity, e.g. java iotinstaller.IoTInstaller -delete_ent <ent_id> <ent_type> <ent_name>");
		System.out.println("IoTInstaller: 9) Delete address, e.g. java iotinstaller.IoTInstaller -delete_add <ent_id>");
		System.out.println("IoTInstaller: 10) Delete device address, e.g. java iotinstaller.IoTInstaller -delete_dev_add <ent_id>");
		System.out.println("IoTInstaller: 11) Delete zigbee device address, e.g. java iotinstaller.IoTInstaller -delete_zb_add <ent_id>");
		System.out.println("IoTInstaller: 12) Delete host, e.g. java iotinstaller.IoTInstaller -delete_host <host_address>");
		System.out.println("IoTInstaller: Type 'java iotinstaller.IoTInstaller -help' to display this help.");
		System.out.println();
	}

	/**
	 * Main method that accepts inputs for installation
	 *
	 * @param  args[0]  String that contains the command line parameter
	 * @param  args[1]  String that contains the first file name / entity ID / host address
	 * @param  args[2]  String that contains the second file name
	 * @param  args[3]  String that contains the third file name
	 * @see    helpMessages
	 */
	public static void main(String[] args) {

		// Testing IoTInstaller object
		IoTInstaller iotinst = new IoTInstaller();

		// TODO: PROBABLY NEED A BETTER ERROR HANDLING FOR INPUTS HERE
		// NOW ASSUME MINIMAL ERROR FOR INPUTS
		if (args.length > 0) {
			// Check for input parameters
			if (args[0].equals(STR_INSTALL_ENTITY_CMD)) {
				iotinst.extractTableAndInstall(args[1]);

			} else if (args[0].equals(STR_INSTALL_COMMUNICATION_CMD)) {
				iotinst.extractCommAndInstall(args[1]);

			} else if (args[0].equals(STR_INSTALL_COMPLETE_CMD)) {
				iotinst.installPairOfEntities(args[1], args[2], args[3]);

			} else if (args[0].equals(STR_INSTALL_ADDRESS_CMD)) {
				iotinst.installAddress(args[1]);

			} else if (args[0].equals(STR_INSTALL_DEV_ADDRESS_CMD)) {
				iotinst.installDeviceAddress(args[1]);

			} else if (args[0].equals(STR_INSTALL_ZB_ADDRESS_CMD)) {
				iotinst.installZigbeeAddress(args[1]);

			} else if (args[0].equals(STR_INSTALL_HOST_CMD)) {
				iotinst.installHost(args[1]);

			} else if (args[0].equals(STR_DELETE_ENTITY_CMD)) {
				iotinst.deleteEntity(args[1], args[2], args[3]);

			} else if (args[0].equals(STR_DELETE_ADDRESS_CMD)) {
				iotinst.deleteAddress(STR_ADDRESS_TABLE_NAME, args[1], args[2]);

			} else if (args[0].equals(STR_DELETE_DEV_ADD_CMD)) {
				iotinst.deleteAddress(STR_DEV_ADD_TABLE_NAME, args[1], args[2]);

			} else if (args[0].equals(STR_DELETE_ZB_ADD_CMD)) {
				iotinst.deleteAddress(STR_ZB_ADD_TABLE_NAME, args[1], args[2]);

			} else if (args[0].equals(STR_DELETE_HOST_CMD)) {
				iotinst.deleteHost(args[1]);

			} else if (args[0].equals(STR_HELP_CMD)) {
				iotinst.helpMessages();

			} else {
				System.out.println("IoTInstaller: ERROR: Wrong input parameters!");
				iotinst.helpMessages();
			}
		} else {
			System.out.println("IoTInstaller: ERROR: No input parameters detected!");
			iotinst.helpMessages();
		}
	}


}
