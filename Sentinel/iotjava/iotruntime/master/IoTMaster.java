package iotruntime.master;

import iotruntime.*;
import iotruntime.slave.IoTAddress;
import iotruntime.slave.IoTDeviceAddress;
import iotruntime.messages.*;

// ASM packages
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;

// Java packages
import java.io.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.lang.Class;
import java.lang.reflect.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.util.*;
import static java.lang.Math.toIntExact;

/** Class IoTMaster is responsible to use ClassRuntimeInstrumenterMaster
 *  to instrument the controller/device bytecode and starts multiple
 *  IoTSlave running on different JVM's in a distributed fashion.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-06-16
 */
public final class IoTMaster {

	/**
	 * IoTMaster class properties
	 * <p>
	 * CommunicationHandler maintains the data structure for hostnames and ports
	 * LoadBalancer assigns a job onto a host based on certain metrics
	 */
	private CommunicationHandler commHan;
	private LoadBalancer lbIoT;
	private RouterConfig routerConfig;
	private ProcessJailConfig processJailConfig;
	private ObjectInitHandler objInitHand;
	private ObjectAddressInitHandler objAddInitHand;
	private String[] strObjectNames;
	// Now this can be either ClassRuntimeInstrumenterMaster or CRuntimeInstrumenterMaster
	private Map<String,Object> mapClassNameToCrim;

	/**
	 * These properties hold information of a certain object
	 * at a certain time
	 */
	private String strObjName;
	private String strObjClassName;
	private String strObjClassInterfaceName;
	private String strObjStubClsIntfaceName;
	private String strIoTMasterHostAdd;
	private String strIoTSlaveControllerHostAdd;
	private String strIoTSlaveObjectHostAdd;
	private Class[] arrFieldClasses;
	private Object[] arrFieldValues;
	private Socket filesocket;

	/**
	 * For connection with C++ IoTSlave
	 */
	private ServerSocket serverSocketCpp;
	private Socket socketCpp;
	private BufferedInputStream inputCpp;
	private BufferedOutputStream outputCpp;

	// Constants that are to be extracted from config file
	private static String STR_MASTER_MAC_ADD;
	private static String STR_IOT_CODE_PATH;
	private static String STR_CONT_PATH;
	private static String STR_RUNTIME_DIR;
	private static String STR_SLAVE_DIR;
	private static String STR_CLS_PATH;
	private static String STR_RMI_PATH;
	private static String STR_RMI_HOSTNAME;
	private static String STR_LOG_FILE_PATH;
	private static String STR_USERNAME;
	private static String STR_ROUTER_ADD;
	private static String STR_MONITORING_HOST;
	private static String STR_ZB_GATEWAY_ADDRESS;
	private static String STR_ZB_GATEWAY_PORT;
	private static String STR_ZB_IOTMASTER_PORT;
	private static String STR_JVM_INIT_HEAP_SIZE;
	private static String STR_JVM_MAX_HEAP_SIZE;
	private static String STR_LANGUAGE_CONTROLLER;
	private static String STR_SKEL_CLASS_SUFFIX;
	private static String STR_STUB_CLASS_SUFFIX;
	private static String STR_ACTIVATE_SANDBOXING;
	private static boolean BOOL_VERBOSE;

	/**
	 * IoTMaster class constants
	 * <p>
	 * Name constants - not to be configured by users
	 */
	private static final String STR_IOT_MASTER_NAME = "IoTMaster";
	private static final String STR_CFG_FILE_EXT = ".config";
	private static final String STR_CLS_FILE_EXT = ".class";
	private static final String STR_JAR_FILE_EXT = ".jar";
	private static final String STR_MAC_POLICY_EXT = ".tomoyo.pol";	
	private static final String STR_SHELL_FILE_EXT = ".sh";
	private static final String STR_SO_FILE_EXT = ".so";
	private static final String STR_ZIP_FILE_EXT = ".zip";
	private static final String STR_TCP_PROTOCOL = "tcp";
	private static final String STR_UDP_PROTOCOL = "udp";
	private static final String STR_TCPGW_PROTOCOL = "tcpgw";
	private static final String STR_NO_PROTOCOL = "nopro";
	private static final String STR_SELF_MAC_ADD = "00:00:00:00:00:00";
	private static final String STR_INTERFACE_CLS_CFG = "INTERFACE_CLASS";
	private static final String STR_INT_STUB_CLS_CFG = "INTERFACE_STUB_CLASS";
	private static final String STR_FILE_TRF_CFG = "ADDITIONAL_ZIP_FILE";
	private static final String STR_LANGUAGE = "LANGUAGE";
	private static final String STR_YES = "Yes";
	private static final String STR_NO = "No";
	private static final String STR_JAVA = "Java";
	private static final String STR_CPP = "C++";
	private static final String STR_SSH = "ssh";
	private static final String STR_SCP = "scp";
	private static final String STR_IOTSLAVE_CPP = "./IoTSlave.o";
	private static final String STR_SHELL_HEADER = "#!/bin/sh";
	private static final String STR_JAVA_PATH = "/usr/bin/java";
	private static final String STR_MAC_POL_PATH = "tomoyo/";

	private static int INT_SIZE = 4;	// send length in the size of integer (4 bytes)
	private static final int INT_DNS_PORT = 53;

	/**
	 * Runtime class name constants - not to be configured by users
	 */
	private static final String STR_REL_INSTRUMENTER_CLS = "iotruntime.master.RelationInstrumenter";
	private static final String STR_SET_INSTRUMENTER_CLS = "iotruntime.master.SetInstrumenter";
	private static final String STR_IOT_SLAVE_CLS = "iotruntime.slave.IoTSlave";
	private static final String STR_IOT_DEV_ADD_CLS = "IoTDeviceAddress";
	private static final String STR_IOT_ZB_ADD_CLS = "IoTZigbeeAddress";
	private static final String STR_IOT_ADD_CLS = "IoTAddress";
	
	/**
	 * Class constructor
	 *
	 */
	public IoTMaster(String[] argObjNms) {

		commHan = null;
		lbIoT = null;
		routerConfig = null;
		processJailConfig = null;
		objInitHand = null;
		objAddInitHand = null;
		strObjectNames = argObjNms;
		strObjName = null;
		strObjClassName = null;
		strObjClassInterfaceName = null;
		strObjStubClsIntfaceName = null;
		strIoTMasterHostAdd = null;
		strIoTSlaveControllerHostAdd = null;
		strIoTSlaveObjectHostAdd = null;
		arrFieldClasses = null;
		arrFieldValues = null;
		filesocket = null;
		mapClassNameToCrim = null;
		// Connection with C++ IoTSlave
		serverSocketCpp = null;
		socketCpp = null;
		inputCpp = null;
		outputCpp = null;

		STR_MASTER_MAC_ADD = null;
		STR_IOT_CODE_PATH = null;
		STR_CONT_PATH = null;
		STR_RUNTIME_DIR = null;
		STR_SLAVE_DIR = null;
		STR_CLS_PATH = null;
		STR_RMI_PATH = null;
		STR_RMI_HOSTNAME = null;
		STR_LOG_FILE_PATH = null;
		STR_USERNAME = null;
		STR_ROUTER_ADD = null;
		STR_MONITORING_HOST = null;
		STR_ZB_GATEWAY_ADDRESS = null;
		STR_ZB_GATEWAY_PORT = null;
		STR_ZB_IOTMASTER_PORT = null;
		STR_JVM_INIT_HEAP_SIZE = null;
		STR_JVM_MAX_HEAP_SIZE = null;
		STR_LANGUAGE_CONTROLLER = null;
		STR_ACTIVATE_SANDBOXING = null;
		BOOL_VERBOSE = false;
	}

	/**
	 * A method to initialize CommunicationHandler, LoadBalancer, RouterConfig and ObjectInitHandler
	 *
	 * @return void
	 */
	private void initLiveDataStructure() {

		commHan = new CommunicationHandler(BOOL_VERBOSE);
		lbIoT = new LoadBalancer(BOOL_VERBOSE);
		lbIoT.setupLoadBalancer();
		routerConfig = new RouterConfig();
		routerConfig.getAddressList(STR_ROUTER_ADD);
		processJailConfig = new ProcessJailConfig();
		//processJailConfig.setAddressListObject(routerConfig.getAddressListObject());
		objInitHand = new ObjectInitHandler(BOOL_VERBOSE);
		objAddInitHand = new ObjectAddressInitHandler(BOOL_VERBOSE);
		mapClassNameToCrim = new HashMap<String,Object>();
	}

	/**
	 * getPrintWriter() gets a new PrintWriter for a new object
	 *
	 * @param   strObjectName 	String object name
	 * @return  PrintWriter
	 */
	private PrintWriter getPrintWriter(String strObjectName) {

		FileWriter fw = null;
		try {
			fw = new FileWriter(strObjectName);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		PrintWriter printWriter = new PrintWriter(new BufferedWriter(fw));
		return printWriter;
	}

	/**
	 * A method to initialize constants from config file
	 *
	 * @return void
	 */
	private void parseIoTMasterConfigFile() {
		// Parse configuration file
		Properties prop = new Properties();
		String strCfgFileName = STR_IOT_MASTER_NAME + STR_CFG_FILE_EXT;
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
		// Initialize constants from config file
		STR_MASTER_MAC_ADD = prop.getProperty("MAC_ADDRESS");
		STR_IOT_CODE_PATH = prop.getProperty("IOT_CODE_PATH");
		STR_CONT_PATH = prop.getProperty("CONTROLLERS_CODE_PATH");
		STR_RUNTIME_DIR = prop.getProperty("RUNTIME_DIR");
		STR_SLAVE_DIR = prop.getProperty("SLAVE_DIR");
		STR_CLS_PATH = prop.getProperty("CLASS_PATH");
		STR_RMI_PATH = prop.getProperty("RMI_PATH");
		STR_RMI_HOSTNAME = prop.getProperty("RMI_HOSTNAME");
		STR_LOG_FILE_PATH = prop.getProperty("LOG_FILE_PATH");
		STR_USERNAME = prop.getProperty("USERNAME");
		STR_ROUTER_ADD = prop.getProperty("ROUTER_ADD");
		STR_MONITORING_HOST = prop.getProperty("MONITORING_HOST");
		STR_ZB_GATEWAY_ADDRESS = prop.getProperty("ZIGBEE_GATEWAY_ADDRESS");
		STR_ZB_GATEWAY_PORT = prop.getProperty("ZIGBEE_GATEWAY_PORT");
		STR_ZB_IOTMASTER_PORT = prop.getProperty("ZIGBEE_IOTMASTER_PORT");
		STR_JVM_INIT_HEAP_SIZE = prop.getProperty("JVM_INIT_HEAP_SIZE");
		STR_JVM_MAX_HEAP_SIZE = prop.getProperty("JVM_MAX_HEAP_SIZE");
		STR_SKEL_CLASS_SUFFIX = prop.getProperty("SKEL_CLASS_SUFFIX");
		STR_STUB_CLASS_SUFFIX = prop.getProperty("STUB_CLASS_SUFFIX");
		STR_ACTIVATE_SANDBOXING = prop.getProperty("ACTIVATE_SANDBOXING");
		if(prop.getProperty("VERBOSE").equals(STR_YES)) {
			BOOL_VERBOSE = true;
		}

		RuntimeOutput.print("IoTMaster: Extracting information from config file: " + strCfgFileName, BOOL_VERBOSE);
		RuntimeOutput.print("STR_MASTER_MAC_ADD=" + STR_MASTER_MAC_ADD, BOOL_VERBOSE);
		RuntimeOutput.print("STR_IOT_CODE_PATH=" + STR_IOT_CODE_PATH, BOOL_VERBOSE);
		RuntimeOutput.print("STR_CONT_PATH=" + STR_CONT_PATH, BOOL_VERBOSE);
		RuntimeOutput.print("STR_RUNTIME_DIR=" + STR_RUNTIME_DIR, BOOL_VERBOSE);
		RuntimeOutput.print("STR_SLAVE_DIR=" + STR_SLAVE_DIR, BOOL_VERBOSE);
		RuntimeOutput.print("STR_CLS_PATH=" + STR_CLS_PATH, BOOL_VERBOSE);
		RuntimeOutput.print("STR_RMI_PATH=" + STR_RMI_PATH, BOOL_VERBOSE);
		RuntimeOutput.print("STR_RMI_HOSTNAME=" + STR_RMI_HOSTNAME, BOOL_VERBOSE);
		RuntimeOutput.print("STR_LOG_FILE_PATH=" + STR_LOG_FILE_PATH, BOOL_VERBOSE);
		RuntimeOutput.print("STR_USERNAME=" + STR_USERNAME, BOOL_VERBOSE);
		RuntimeOutput.print("STR_ROUTER_ADD=" + STR_ROUTER_ADD, BOOL_VERBOSE);
		RuntimeOutput.print("STR_MONITORING_HOST=" + STR_MONITORING_HOST, BOOL_VERBOSE);
		RuntimeOutput.print("STR_ZB_GATEWAY_ADDRESS=" + STR_ZB_GATEWAY_ADDRESS, BOOL_VERBOSE);
		RuntimeOutput.print("STR_ZB_GATEWAY_PORT=" + STR_ZB_GATEWAY_PORT, BOOL_VERBOSE);
		RuntimeOutput.print("STR_ZB_IOTMASTER_PORT=" + STR_ZB_IOTMASTER_PORT, BOOL_VERBOSE);
		RuntimeOutput.print("STR_JVM_INIT_HEAP_SIZE=" + STR_JVM_INIT_HEAP_SIZE, BOOL_VERBOSE);
		RuntimeOutput.print("STR_JVM_MAX_HEAP_SIZE=" + STR_JVM_MAX_HEAP_SIZE, BOOL_VERBOSE);
		RuntimeOutput.print("STR_SKEL_CLASS_SUFFIX=" + STR_SKEL_CLASS_SUFFIX, BOOL_VERBOSE);
		RuntimeOutput.print("STR_STUB_CLASS_SUFFIX=" + STR_STUB_CLASS_SUFFIX, BOOL_VERBOSE);
		RuntimeOutput.print("STR_ACTIVATE_SANDBOXING=" + STR_ACTIVATE_SANDBOXING, BOOL_VERBOSE);
		RuntimeOutput.print("BOOL_VERBOSE=" + BOOL_VERBOSE, BOOL_VERBOSE);
		RuntimeOutput.print("IoTMaster: Information extracted successfully!", BOOL_VERBOSE);
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
		System.out.println("IoTMaster: Reading " + strCfgField +
			" from config file: " + strCfgFileName + " with value: " + 
			prop.getProperty(strCfgField, null));
		// NULL is returned if the property isn't found
		return prop.getProperty(strCfgField, null);
	}

	/**
	 * A method to send files from IoTMaster
	 *
	 * @param  filesocket File socket object
	 * @param  sFileName  File name
	 * @param  lFLength   File length
	 * @return            void
	 */
	private void sendFile(Socket filesocket, String sFileName, long lFLength) throws IOException {

		File file = new File(sFileName);
		byte[] bytFile = new byte[toIntExact(lFLength)];
		InputStream inFileStream = new FileInputStream(file);

		OutputStream outFileStream = filesocket.getOutputStream();
		int iCount;
		while ((iCount = inFileStream.read(bytFile)) > 0) {
			outFileStream.write(bytFile, 0, iCount);
		}
		filesocket.close();
		RuntimeOutput.print("IoTMaster: File sent!", BOOL_VERBOSE);
	}

	/**
	 * A method to create a thread
	 *
	 * @param  sSSHCmd    SSH command
	 * @return            void
	 */
	private void createThread(String sSSHCmd) throws IOException {

		// Start a new thread to start a new JVM
		new Thread() {
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(sSSHCmd);
		}.start();
		RuntimeOutput.print("Executing: " + sSSHCmd, BOOL_VERBOSE);
	}

	/**
	 * A method to send command from master and receive reply from slave
	 *
	 * @params  msgSend     Message object
	 * @params  strPurpose  String that prints purpose message
	 * @params  inStream    Input stream
	 * @params  outStream   Output stream
	 * @return  void
	 */
	private void commMasterToSlave(Message msgSend, String strPurpose,
		InputStream _inStream, OutputStream _outStream)  
			throws IOException, ClassNotFoundException {

		// Send message/command from master
		ObjectOutputStream outStream = (ObjectOutputStream) _outStream;
		outStream.writeObject(msgSend);
		RuntimeOutput.print("IoTMaster: Send message: " + strPurpose, BOOL_VERBOSE);

		// Get reply from slave as acknowledgment
		ObjectInputStream inStream = (ObjectInputStream) _inStream;
		Message msgReply = (Message) inStream.readObject();
		RuntimeOutput.print("IoTMaster: Reply message: " + msgReply.getMessage(), BOOL_VERBOSE);
	}

	/**
	 * A private method to instrument IoTSet device
	 *
	 * @params  strFieldIdentifier        String field name + object ID
	 * @params  strFieldName              String field name
	 * @params  strIoTSlaveObjectHostAdd  String slave host address
	 * @params  inStream                  ObjectInputStream communication
	 * @params  inStream                  ObjectOutputStream communication
	 * @params  strLanguage				  String language
	 * @return  void
	 */
	private void instrumentIoTSetDevice(String strFieldIdentifier, String strObjName, String strFieldName, String strIoTSlaveObjectHostAdd,
		InputStream inStream, OutputStream outStream, String strLanguage)  
			throws IOException, ClassNotFoundException, InterruptedException {

		// Get information from the set
		List<Object[]> listObject = objAddInitHand.getFields(strFieldIdentifier);
		// Create a new IoTSet
		if(strLanguage.equals(STR_JAVA)) {
			Message msgCrtIoTSet = new MessageCreateSetRelation(IoTCommCode.CREATE_NEW_IOTSET, strFieldName);
			commMasterToSlave(msgCrtIoTSet, "Create new IoTSet for IoTDeviceAddress!", inStream, outStream);
		} else
			createNewIoTSetCpp(strFieldName, outStream, inStream);
		int iRows = listObject.size();
		RuntimeOutput.print("IoTMaster: Number of rows for IoTDeviceAddress: " + iRows, BOOL_VERBOSE);
		// Transfer the address
		for(int iRow=0; iRow<iRows; iRow++) {
			arrFieldValues = listObject.get(iRow);
			// Get device address - if 00:00:00:00:00:00 that means it needs the driver object address (self)
			String strDeviceAddress = null;
			String strDeviceAddressKey = null;
			if (arrFieldValues[0].equals(STR_SELF_MAC_ADD)) {
				strDeviceAddress = strIoTSlaveObjectHostAdd;
				strDeviceAddressKey = strObjName + "-" + strIoTSlaveObjectHostAdd;
			} else {
				strDeviceAddress = routerConfig.getIPFromMACAddress((String) arrFieldValues[0]);
				strDeviceAddressKey = strObjName + "-" + strDeviceAddress;
			}
			int iDestDeviceDriverPort = (int) arrFieldValues[1];
			String strProtocol = (String) arrFieldValues[2];
			// Check for wildcard feature			
			boolean bSrcPortWildCard = false;
			boolean bDstPortWildCard = false;
			if (arrFieldValues.length > 3) {
				bSrcPortWildCard = (boolean) arrFieldValues[3];
				bDstPortWildCard = (boolean) arrFieldValues[4];
			}
			// Add the port connection into communication handler - if it's not assigned yet
			if (commHan.getComPort(strDeviceAddressKey) == null) {
				commHan.addPortConnection(strIoTSlaveObjectHostAdd, strDeviceAddressKey);
			}

			// TODO: DEBUG!!!
			System.out.println("\n\n DEBUG: InstrumentSetDevice: Object Name: " + strObjName);
			System.out.println("DEBUG: InstrumentSetDevice: Port number: " + commHan.getComPort(strDeviceAddressKey));
			System.out.println("DEBUG: InstrumentSetDevice: Device address: " + strDeviceAddressKey + "\n\n");

			// Send address one by one
			if(strLanguage.equals(STR_JAVA)) {
				Message msgGetIoTSetObj = null;
				if (bDstPortWildCard) {
					String strUniqueDev = strDeviceAddressKey + ":" + iRow;
					msgGetIoTSetObj = new MessageGetDeviceObject(IoTCommCode.GET_DEVICE_IOTSET_OBJECT,
						strDeviceAddress, commHan.getAdditionalPort(strUniqueDev), iDestDeviceDriverPort, bSrcPortWildCard, bDstPortWildCard);
				} else
					msgGetIoTSetObj = new MessageGetDeviceObject(IoTCommCode.GET_DEVICE_IOTSET_OBJECT,
						strDeviceAddress, commHan.getComPort(strDeviceAddressKey), iDestDeviceDriverPort, bSrcPortWildCard, bDstPortWildCard);
				commMasterToSlave(msgGetIoTSetObj, "Get IoTSet objects!", inStream, outStream);
			} else
				getDeviceIoTSetObjectCpp(outStream, inStream, strDeviceAddress, commHan.getComPort(strDeviceAddressKey), iDestDeviceDriverPort, 
					bSrcPortWildCard, bDstPortWildCard);
		}
		// Reinitialize IoTSet on device object
		if(strLanguage.equals(STR_JAVA))
			commMasterToSlave(new MessageSimple(IoTCommCode.REINITIALIZE_IOTSET_FIELD), "Reinitialize IoTSet fields!", inStream, outStream);
		else
			reinitializeIoTSetFieldCpp(outStream, inStream);
	}


	/**
	 * A private method to instrument IoTSet Zigbee device
	 *
	 * @params  Map.Entry<String,Object>  Entry of map IoTSet instrumentation
	 * @params  strFieldName              String field name
	 * @params  strIoTSlaveObjectHostAdd  String slave host address
	 * @params  inStream                  ObjectInputStream communication
	 * @params  inStream                  ObjectOutputStream communication
	 * @params  strLanguage				  String language
	 * @return  void
	 */
	private void instrumentIoTSetZBDevice(Map.Entry<String,Object> map, String strObjName, String strFieldName, String strIoTSlaveObjectHostAdd,
		InputStream inStream, OutputStream outStream, String strLanguage)  
			throws IOException, ClassNotFoundException, InterruptedException {

		// Get information from the set
		SetInstrumenter setInstrumenter = (SetInstrumenter) map.getValue();
		// Create a new IoTSet
		if(strLanguage.equals(STR_JAVA)) {
			Message msgCrtIoTSet = new MessageCreateSetRelation(IoTCommCode.CREATE_NEW_IOTSET, strFieldName);
			commMasterToSlave(msgCrtIoTSet, "Create new IoTSet for IoTZigbeeAddress!", inStream, outStream);
		} else	// TODO: will need to implement IoTSet Zigbee for C++ later
			;
		// Prepare ZigbeeConfig
		String strZigbeeGWAddress = routerConfig.getIPFromMACAddress(STR_ZB_GATEWAY_ADDRESS);
		String strZigbeeGWAddressKey = strObjName + "-" + strZigbeeGWAddress;
		int iZigbeeGWPort = Integer.parseInt(STR_ZB_GATEWAY_PORT);
		int iZigbeeIoTMasterPort = Integer.parseInt(STR_ZB_IOTMASTER_PORT);
		commHan.addDevicePort(iZigbeeIoTMasterPort);
		ZigbeeConfig zbConfig = new ZigbeeConfig(strZigbeeGWAddress, iZigbeeGWPort, iZigbeeIoTMasterPort, 
			BOOL_VERBOSE);
		// Add the port connection into communication handler - if it's not assigned yet
		if (commHan.getComPort(strZigbeeGWAddressKey) == null) {
			commHan.addPortConnection(strIoTSlaveObjectHostAdd, strZigbeeGWAddressKey);
		}		
		int iRows = setInstrumenter.numberOfRows();
		RuntimeOutput.print("IoTMaster: Number of rows for IoTZigbeeAddress: " + iRows, BOOL_VERBOSE);

		// TODO: DEBUG!!!
		System.out.println("\n\nDEBUG: InstrumentZigbeeDevice: Object Name: " + strObjName);
		System.out.println("DEBUG: InstrumentZigbeeDevice: Port number: " + commHan.getComPort(strZigbeeGWAddressKey));
		System.out.println("DEBUG: InstrumentZigbeeDevice: Device address: " + strZigbeeGWAddress + "\n\n");

		// Transfer the address
		for(int iRow=0; iRow<iRows; iRow++) {
			arrFieldValues = setInstrumenter.fieldValues(iRow);
			// Get device address
			String strZBDevAddress = (String) arrFieldValues[0];
			// Send policy to Zigbee gateway - TODO: Need to clear policy first?
			zbConfig.setPolicy(strIoTSlaveObjectHostAdd, commHan.getComPort(strZigbeeGWAddressKey), strZBDevAddress);
			// Send address one by one
			if(strLanguage.equals(STR_JAVA)) {
				Message msgGetIoTSetZBObj = new MessageGetSimpleDeviceObject(IoTCommCode.GET_ZB_DEV_IOTSET_OBJECT, strZBDevAddress);
				commMasterToSlave(msgGetIoTSetZBObj, "Get IoTSet objects!", inStream, outStream);
			} else	// TODO: Implement IoTSet Zigbee for C++
				;
		}
		zbConfig.closeConnection();
		// Reinitialize IoTSet on device object
		commMasterToSlave(new MessageSimple(IoTCommCode.REINITIALIZE_IOTSET_FIELD), "Reinitialize IoTSet fields!", inStream, outStream);
	}

	
	/**
	 * A private method to instrument IoTSet of addresses
	 *
	 * @params  strFieldIdentifier        String field name + object ID
	 * @params  strFieldName              String field name
	 * @params  inStream                  ObjectInputStream communication
	 * @params  inStream                  ObjectOutputStream communication
	 * @params  strLanguage				  String language
	 * @return  void
	 */
	private void instrumentIoTSetAddress(String strFieldIdentifier, String strFieldName,
		InputStream inStream, OutputStream outStream, String strLanguage)  
			throws IOException, ClassNotFoundException, InterruptedException {

		// Get information from the set
		List<Object[]> listObject = objAddInitHand.getFields(strFieldIdentifier);
		// Create a new IoTSet
		if(strLanguage.equals(STR_JAVA)) {
			Message msgCrtIoTSet = new MessageCreateSetRelation(IoTCommCode.CREATE_NEW_IOTSET, strFieldName);
			commMasterToSlave(msgCrtIoTSet, "Create new IoTSet for IoTAddress!", inStream, outStream);
		} else
			;
		int iRows = listObject.size();
		RuntimeOutput.print("IoTMaster: Number of rows for IoTAddress: " + iRows, BOOL_VERBOSE);
		// Transfer the address
		for(int iRow=0; iRow<iRows; iRow++) {
			arrFieldValues = listObject.get(iRow);
			// Get device address
			String strAddress = (String) arrFieldValues[0];
			// Send address one by one
			if(strLanguage.equals(STR_JAVA)) {
				Message msgGetIoTSetAddObj = new MessageGetSimpleDeviceObject(IoTCommCode.GET_ADD_IOTSET_OBJECT, strAddress);
				commMasterToSlave(msgGetIoTSetAddObj, "Get IoTSet objects!", inStream, outStream);
			} else	// TODO: Implement IoTSet Address for C++
				;
		}
		// Reinitialize IoTSet on device object
		commMasterToSlave(new MessageSimple(IoTCommCode.REINITIALIZE_IOTSET_FIELD),
											"Reinitialize IoTSet fields!", inStream, outStream);
	}


	/**
	 * A private method to instrument an object on a specific machine and setting up policies
	 *
	 * @params  strFieldObjectID  		  String field object ID
	 * @params  strObjControllerName	  String object controller name
	 * @params  strLanguage				  String language
	 * @return  void
	 */
	private void instrumentObject(String strFieldObjectID, String strObjControllerName, String strLanguage) throws IOException {

		// Extract the interface name for RMI
		// e.g. ProximitySensorInterface, TempSensorInterface, etc.
		
		String strObjCfgFile = STR_IOT_CODE_PATH + strObjClassName + "/" + strObjClassName + STR_CFG_FILE_EXT;
		strObjClassInterfaceName = parseConfigFile(strObjCfgFile, STR_INTERFACE_CLS_CFG);
		strObjStubClsIntfaceName = parseConfigFile(strObjCfgFile, STR_INT_STUB_CLS_CFG);
		// Create an object name, e.g. ProximitySensorImplPS1
		strObjName = strObjClassName + strFieldObjectID;
		// Check first if host exists
		if(commHan.objectExists(strObjName)) {
			// If this object exists already ...
			// Re-read IoTSlave object hostname for further reference
			strIoTSlaveObjectHostAdd = commHan.getHostAddress(strObjName);
			RuntimeOutput.print("IoTMaster: Object with name: " + strObjName + " has existed!", BOOL_VERBOSE);
		} else {
			// If this is a new object ... then create one
			// Get host address for IoTSlave from LoadBalancer
			//strIoTSlaveObjectHostAdd = lbIoT.selectHost();
			strIoTSlaveObjectHostAdd = routerConfig.getIPFromMACAddress(lbIoT.selectHost());
			if (strIoTSlaveControllerHostAdd == null)
				throw new Error("IoTMaster: Could not translate MAC to IP address! Please check the router's /tmp/dhcp.leases!");
			RuntimeOutput.print("IoTMaster: Object name: " + strObjName, BOOL_VERBOSE);
			// Add port connection and get port numbers
			// Naming for objects ProximitySensor becomes ProximitySensor0, ProximitySensor1, etc.
			commHan.addPortConnection(strIoTSlaveObjectHostAdd, strObjName);
			commHan.addActiveControllerObject(strFieldObjectID, strObjName, strObjClassName, strObjClassInterfaceName, 
				strObjStubClsIntfaceName, strIoTSlaveObjectHostAdd, arrFieldValues, arrFieldClasses);
			// ROUTING POLICY: IoTMaster and device/controller object
			// Master-slave communication
			routerConfig.configureRouterMainPolicies(STR_ROUTER_ADD, strIoTMasterHostAdd,
				strIoTSlaveObjectHostAdd, STR_TCP_PROTOCOL, commHan.getComPort(strObjName));
			// ROUTING POLICY: Send the same routing policy to both the hosts
			routerConfig.configureHostMainPolicies(strIoTMasterHostAdd, strIoTMasterHostAdd,
				strIoTSlaveObjectHostAdd, STR_TCP_PROTOCOL, commHan.getComPort(strObjName));
			routerConfig.configureHostMainPolicies(strIoTSlaveObjectHostAdd, strIoTMasterHostAdd,
				strIoTSlaveObjectHostAdd, STR_TCP_PROTOCOL, commHan.getComPort(strObjName));
			// Need to accommodate callback functions here - open ports for TCP
			routerConfig.configureRouterMainPolicies(STR_ROUTER_ADD, strIoTSlaveControllerHostAdd,
				strIoTSlaveObjectHostAdd, STR_TCP_PROTOCOL);
			routerConfig.configureHostMainPolicies(strIoTSlaveControllerHostAdd, strIoTSlaveControllerHostAdd,
				strIoTSlaveObjectHostAdd, STR_TCP_PROTOCOL);
			routerConfig.configureHostMainPolicies(strIoTSlaveObjectHostAdd, strIoTSlaveControllerHostAdd,
				strIoTSlaveObjectHostAdd, STR_TCP_PROTOCOL);
			// Configure MAC policies for objects
			//String strFileName = STR_MAC_POL_PATH + strObjClassName + STR_MAC_POLICY_EXT;
			String strFileName = STR_MAC_POL_PATH + STR_JAVA + STR_MAC_POLICY_EXT;
			if (STR_ACTIVATE_SANDBOXING.equals("Yes")) {
				processJailConfig.configureProcessJailDeviceDriverPolicies(strIoTSlaveObjectHostAdd, strObjName, strObjClassName,
					strFileName, strIoTMasterHostAdd, commHan.getComPort(strObjName), commHan.getRMIRegPort(strObjName), 
					commHan.getRMIStubPort(strObjName));
				processJailConfig.configureProcessJailContRMIPolicies(strObjControllerName, strIoTSlaveObjectHostAdd, 
					commHan.getRMIRegPort(strObjName), commHan.getRMIStubPort(strObjName));
			}
			// Instrument the IoTSet declarations inside the class file
			instrumentObjectIoTSet(strFieldObjectID, strLanguage);
		}
		// Send routing policy to router for controller object
		// ROUTING POLICY: RMI communication - RMI registry and stub ports
		routerConfig.configureRouterMainPolicies(STR_ROUTER_ADD, strIoTSlaveControllerHostAdd, strIoTSlaveObjectHostAdd,
			STR_TCP_PROTOCOL, commHan.getRMIRegPort(strObjName));
		routerConfig.configureRouterMainPolicies(STR_ROUTER_ADD, strIoTSlaveControllerHostAdd, strIoTSlaveObjectHostAdd,
			STR_TCP_PROTOCOL, commHan.getRMIStubPort(strObjName));
		// Send the same set of routing policies to compute nodes
		routerConfig.configureHostMainPolicies(strIoTSlaveControllerHostAdd, strIoTSlaveControllerHostAdd, strIoTSlaveObjectHostAdd,
			STR_TCP_PROTOCOL, commHan.getRMIRegPort(strObjName));
		routerConfig.configureHostMainPolicies(strIoTSlaveObjectHostAdd, strIoTSlaveControllerHostAdd, strIoTSlaveObjectHostAdd,
			STR_TCP_PROTOCOL, commHan.getRMIRegPort(strObjName));
		routerConfig.configureHostMainPolicies(strIoTSlaveControllerHostAdd, strIoTSlaveControllerHostAdd, strIoTSlaveObjectHostAdd,
			STR_TCP_PROTOCOL, commHan.getRMIStubPort(strObjName));
		routerConfig.configureHostMainPolicies(strIoTSlaveObjectHostAdd, strIoTSlaveControllerHostAdd, strIoTSlaveObjectHostAdd,
			STR_TCP_PROTOCOL, commHan.getRMIStubPort(strObjName));
	}


	/**
	 * A private method to set router policies for IoTDeviceAddress objects
	 *
	 * @params  strFieldIdentifier        String field name + object ID
	 * @params  Map.Entry<String,Object>  Entry of map IoTSet instrumentation
	 * @params  strIoTSlaveObjectHostAdd  String slave host address
	 * @return  void
	 */
	private void setRouterPolicyIoTSetDevice(String strFieldIdentifier, Map.Entry<String,Object> map, 
		String strIoTSlaveObjectHostAdd) {

		// Get information from the set
		SetInstrumenter setInstrumenter = (SetInstrumenter) map.getValue();
		int iRows = setInstrumenter.numberOfRows();
		RuntimeOutput.print("IoTMaster: Number of rows for IoTDeviceAddress: " + iRows, BOOL_VERBOSE);
		// Transfer the address
		for(int iRow=0; iRow<iRows; iRow++) {
			arrFieldValues = setInstrumenter.fieldValues(iRow);
			objAddInitHand.addField(strFieldIdentifier, arrFieldValues);	// Save this for object instantiation
			// Get device address - if 00:00:00:00:00:00 that means it needs the driver object address (self)
			String strDeviceAddress = null;
			String strDeviceAddressKey = null;
			if (arrFieldValues[0].equals(STR_SELF_MAC_ADD)) {
				strDeviceAddress = strIoTSlaveObjectHostAdd;
				strDeviceAddressKey = strObjName + "-" + strIoTSlaveObjectHostAdd;
			} else {	// Concatenate object name and IP address to give unique key - for a case where there is one device for multiple drivers
				strDeviceAddress = routerConfig.getIPFromMACAddress((String) arrFieldValues[0]);
				strDeviceAddressKey = strObjName + "-" + strDeviceAddress;
			}
			int iDestDeviceDriverPort = (int) arrFieldValues[1];
			String strProtocol = (String) arrFieldValues[2];
			// Add the port connection into communication handler - if it's not assigned yet
			if (commHan.getComPort(strDeviceAddressKey) == null)
				commHan.addPortConnection(strIoTSlaveObjectHostAdd, strDeviceAddressKey);
			boolean bDstPortWildCard = false;
			// Recognize this and allocate different ports for it
			if (arrFieldValues.length > 3) {
				bDstPortWildCard = (boolean) arrFieldValues[4];
				if (bDstPortWildCard) {	// This needs a unique source port
					String strUniqueDev = strDeviceAddressKey + ":" + iRow; 
					commHan.addAdditionalPort(strUniqueDev);
				}
			}

			// TODO: DEBUG!!!
			System.out.println("\n\n DEBUG: InstrumentPolicySetDevice: Object Name: " + strObjName);
			System.out.println("DEBUG: InstrumentPolicySetDevice: Port number: " + commHan.getComPort(strDeviceAddressKey));
			System.out.println("DEBUG: InstrumentPolicySetDevice: Device address: " + strDeviceAddressKey + "\n\n");

			// Send routing policy to router for device drivers and devices
			// ROUTING POLICY: RMI communication - RMI registry and stub ports
			if((iDestDeviceDriverPort == -1) && (!strProtocol.equals(STR_NO_PROTOCOL))) {
				// Port number -1 means that we don't set the policy strictly to port number level
				// "nopro" = no protocol specified for just TCP or just UDP (can be both used as well)
				// ROUTING POLICY: Device driver and device
				routerConfig.configureRouterMainPolicies(STR_ROUTER_ADD, strIoTSlaveObjectHostAdd, strDeviceAddress, strProtocol);
				// ROUTING POLICY: Send to the compute node where the device driver is
				routerConfig.configureHostMainPolicies(strIoTSlaveObjectHostAdd, strIoTSlaveObjectHostAdd, strDeviceAddress, strProtocol);
			} else if((iDestDeviceDriverPort == -1) && (strProtocol.equals(STR_NO_PROTOCOL))) {
				routerConfig.configureRouterMainPolicies(STR_ROUTER_ADD, strIoTSlaveObjectHostAdd, strDeviceAddress);
				routerConfig.configureHostMainPolicies(strIoTSlaveObjectHostAdd, strIoTSlaveObjectHostAdd, strDeviceAddress);
			} else if(strProtocol.equals(STR_TCPGW_PROTOCOL)) {
				// This is a TCP protocol that connects, e.g. a phone to our runtime system
				// that provides a gateway access (accessed through destination port number)
				commHan.addDevicePort(iDestDeviceDriverPort);
				routerConfig.configureRouterMainPolicies(STR_ROUTER_ADD, strIoTSlaveObjectHostAdd, strDeviceAddress, STR_TCP_PROTOCOL, iDestDeviceDriverPort);
				routerConfig.configureHostMainPolicies(strIoTSlaveObjectHostAdd, strIoTSlaveObjectHostAdd, strDeviceAddress, STR_TCP_PROTOCOL, iDestDeviceDriverPort);
				routerConfig.configureRouterHTTPPolicies(STR_ROUTER_ADD, strIoTSlaveObjectHostAdd, strDeviceAddress);
				routerConfig.configureHostHTTPPolicies(strIoTSlaveObjectHostAdd, strIoTSlaveObjectHostAdd, strDeviceAddress);
				// Configure MAC policies
				if (STR_ACTIVATE_SANDBOXING.equals("Yes"))
					processJailConfig.configureProcessJailGWDevicePolicies(strIoTSlaveObjectHostAdd, STR_ROUTER_ADD, INT_DNS_PORT);
			} else {
				// Other port numbers...
				commHan.addDevicePort(iDestDeviceDriverPort);
				routerConfig.configureRouterMainPolicies(STR_ROUTER_ADD, strIoTSlaveObjectHostAdd, strDeviceAddress, strProtocol, 
					commHan.getComPort(strDeviceAddressKey), iDestDeviceDriverPort);
				routerConfig.configureHostMainPolicies(strIoTSlaveObjectHostAdd, strIoTSlaveObjectHostAdd, strDeviceAddress, strProtocol, 
					commHan.getComPort(strDeviceAddressKey), iDestDeviceDriverPort);
				// Configure MAC policies
				if (STR_ACTIVATE_SANDBOXING.equals("Yes"))
					processJailConfig.configureProcessJailDevicePolicies(strIoTSlaveObjectHostAdd, strProtocol,
						commHan.getComPort(strDeviceAddressKey), strDeviceAddress, iDestDeviceDriverPort);
			}
		}
	}

	/**
	 * A private method to set router policies for IoTAddress objects
	 *
	 * @params  strFieldIdentifier        String field name + object ID
	 * @params  Map.Entry<String,Object>  Entry of map IoTSet instrumentation
	 * @params  strHostAddress            String host address
	 * @return  void
	 */
	private void setRouterPolicyIoTSetAddress(String strFieldIdentifier, Map.Entry<String,Object> map, 
		String strHostAddress, String strControllerName) {

		// Get information from the set
		SetInstrumenter setInstrumenter = (SetInstrumenter) map.getValue();
		int iRows = setInstrumenter.numberOfRows();
		RuntimeOutput.print("IoTMaster: Number of rows for IoTAddress: " + iRows, BOOL_VERBOSE);
		// Transfer the address
		for(int iRow=0; iRow<iRows; iRow++) {
			arrFieldValues = setInstrumenter.fieldValues(iRow);
			objAddInitHand.addField(strFieldIdentifier, arrFieldValues);	// Save this for object instantiation
			// Get device address
			String strAddress = (String) arrFieldValues[0];
			// Setting up router policies for HTTP/HTTPs
			if (STR_ACTIVATE_SANDBOXING.equals("Yes")) {
				if (strControllerName != null) {
					processJailConfig.configureProcessJailInetAddressPolicies(strControllerName, strAddress);
				} else {
					processJailConfig.configureProcessJailInetAddressPolicies(strHostAddress, strAddress);
				}
			}
			routerConfig.configureRouterHTTPPolicies(STR_ROUTER_ADD, strHostAddress, strAddress);
			routerConfig.configureHostHTTPPolicies(strHostAddress, strHostAddress, strAddress);
		}
	}

	/**
	 * A private method to instrument an object's IoTSet and IoTRelation field to up policies
	 * <p>
	 * Mostly the IoTSet fields would contain IoTDeviceAddress objects
	 *
	 * @params  strFieldObjectID  	String field object ID
	 * @params  strLanguage  		String language
	 * @return  void
	 */
	private void instrumentObjectIoTSet(String strFieldObjectID, String strLanguage) throws IOException {

		// If this is a new object ... then create one
		// Instrument the class source code and look for IoTSet for device addresses
		// e.g. @config private IoTSet<IoTDeviceAddress> lb_addresses;
		HashMap<String,Object> hmObjectFieldObjects = null;
		if(strLanguage.equals(STR_JAVA)) {
			String strObjectClassNamePath = STR_IOT_CODE_PATH + strObjClassName + "/" + strObjClassName + STR_CLS_FILE_EXT;
			FileInputStream fis = new FileInputStream(strObjectClassNamePath);
			ClassReader cr = new ClassReader(fis);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			// We need Object ID to instrument IoTDeviceAddress
			ClassRuntimeInstrumenterMaster crim = new ClassRuntimeInstrumenterMaster(cw, strFieldObjectID, BOOL_VERBOSE);
			cr.accept(crim, 0);
			fis.close();
			mapClassNameToCrim.put(strObjClassName + strFieldObjectID, crim);
			hmObjectFieldObjects = crim.getFieldObjects();
		} else {	// For C++
			String strObjectClassNamePath = STR_IOT_CODE_PATH + strObjClassName + "/" + strObjClassName + STR_CFG_FILE_EXT;
			CRuntimeInstrumenterMaster crim = new CRuntimeInstrumenterMaster(strObjectClassNamePath, strFieldObjectID, BOOL_VERBOSE);
			mapClassNameToCrim.put(strObjClassName + strFieldObjectID, crim);
			hmObjectFieldObjects = crim.getFieldObjects();
		}
		// Get the object and the class names
		// Build objects for IoTSet and IoTRelation fields in the device object classes
		RuntimeOutput.print("IoTMaster: Going to instrument for " + strObjClassName + " with objectID " + 
			strFieldObjectID, BOOL_VERBOSE);
		for(Map.Entry<String,Object> map : hmObjectFieldObjects.entrySet()) {
			RuntimeOutput.print("IoTMaster: Object name: " + map.getValue().getClass().getName(), BOOL_VERBOSE);
			// Iterate over HashMap and choose between processing
			String strFieldName = map.getKey();
			String strClassName = map.getValue().getClass().getName();
			String strFieldIdentifier = strFieldName + strFieldObjectID;
			if(strClassName.equals(STR_SET_INSTRUMENTER_CLS)) {
				SetInstrumenter setInstrumenter = (SetInstrumenter) map.getValue();
				if(setInstrumenter.getObjTableName().equals(STR_IOT_DEV_ADD_CLS)) { 
				// Instrument the normal IoTDeviceAddress
					setRouterPolicyIoTSetDevice(strFieldIdentifier, map, strIoTSlaveObjectHostAdd);
				} else if(setInstrumenter.getObjTableName().equals(STR_IOT_ADD_CLS)) { 
				// Instrument the IoTAddress
					setRouterPolicyIoTSetAddress(strFieldIdentifier, map, strIoTSlaveObjectHostAdd, null);
				} else if(setInstrumenter.getObjTableName().equals(STR_IOT_ZB_ADD_CLS)) { 
				// Instrument the IoTZigbeeAddress - special feature for Zigbee device support
					RuntimeOutput.print("IoTMaster: IoTZigbeeAddress found! No router policy is set here..", 
						BOOL_VERBOSE);
				} else {
					String strErrMsg = "IoTMaster: Device driver object" +
										" can only have IoTSet<IoTAddress>, IoTSet<IoTDeviceAddress>," +
										" or IoTSet<IoTZigbeeAddress>!";
					throw new Error(strErrMsg);
				}
			} else {
				String strErrMsg = "IoTMaster: Device driver object can only have IoTSet for addresses!";
				throw new Error(strErrMsg);
			}
		}
	}


	/**
	 * A private method to send files to a Java slave driver
	 *
	 * @params  serverSocket				ServerSocket
	 * @params  _inStream					InputStream
	 * @params  _outStream					OutputStream
	 * @params  strObjName					String
	 * @params  strObjClassName				String
	 * @params  strObjClassInterfaceName	String
	 * @params  strObjStubClsIntfaceName	String
	 * @params  strIoTSlaveObjectHostAdd	String
	 * @params  strFieldObjectID			String
	 * @params  arrFieldValues				Object[]
	 * @params  arrFieldClasses				Class[]
	 * @return  void
	 */
	private void sendFileToJavaSlaveDriver(ServerSocket serverSocket, InputStream _inStream, OutputStream _outStream,
		String strObjName, String strObjClassName, String strObjClassInterfaceName, String strObjStubClsIntfaceName,
		String strIoTSlaveObjectHostAdd, String strFieldObjectID, Object[] arrFieldValues, Class[] arrFieldClasses) 
			throws IOException, ClassNotFoundException {

		ObjectInputStream inStream = (ObjectInputStream) _inStream;
		ObjectOutputStream outStream = (ObjectOutputStream) _outStream;
		// Create message to transfer file first
		String sFileName = strObjClassName + STR_JAR_FILE_EXT;
		String sPath = STR_IOT_CODE_PATH + strObjClassName + "/" + sFileName;
		File file = new File(sPath);
		commMasterToSlave(new MessageSendFile(IoTCommCode.TRANSFER_FILE, sFileName, file.length()),
			"Sending file!", inStream, outStream);
		// Send file - JAR file for object creation
		sendFile(serverSocket.accept(), sPath, file.length());
		Message msgReply = (Message) inStream.readObject();
		RuntimeOutput.print("IoTMaster: Reply message: " + msgReply.getMessage(), BOOL_VERBOSE);
		// Pack object information to create object on a IoTSlave
		Message msgObjIoTSlave = new MessageCreateObject(IoTCommCode.CREATE_OBJECT, strIoTSlaveObjectHostAdd,
			strObjClassName, strObjName, strObjClassInterfaceName, strObjStubClsIntfaceName, commHan.getRMIRegPort(strObjName), 
			commHan.getRMIStubPort(strObjName), arrFieldValues, arrFieldClasses);
		// Send message
		commMasterToSlave(msgObjIoTSlave, "Sending object information", inStream, outStream);
	}


	/**
	 * A private method to send files to a Java slave driver
	 *
	 * @return  void
	 */
	private void sendFileToCppSlaveDriver(String strObjClassName, String strIoTSlaveObjectHostAdd) 
			throws IOException, ClassNotFoundException {

		// Create message to transfer file first
		String sFileName = strObjClassName + STR_ZIP_FILE_EXT;
		String sFile = STR_IOT_CODE_PATH + strObjClassName + "/" + sFileName;
		String strCmdSend = STR_SCP + " " + sFile + " " + STR_USERNAME + strIoTSlaveObjectHostAdd + ":" + STR_SLAVE_DIR;
		runCommand(strCmdSend);
		RuntimeOutput.print("IoTMaster: Executing: " + strCmdSend, BOOL_VERBOSE);
		// Unzip file
		String strCmdUnzip = STR_SSH + " " + STR_USERNAME + strIoTSlaveObjectHostAdd + " cd " +
					STR_SLAVE_DIR + " sudo unzip -o " + sFileName + ";";
		runCommand(strCmdUnzip);
		RuntimeOutput.print("IoTMaster: Executing: " + strCmdUnzip, BOOL_VERBOSE);

	}


	/**
	 * Construct command line for Java IoTSlave
	 *
	 * @return       String
	 */
	private String getCmdJavaDriverIoTSlave(String strIoTMasterHostAdd, String strIoTSlaveObjectHostAdd, String strObjName) {

		// Create an Shell executable
		String strJavaCommand =	STR_SHELL_HEADER + "\nexec " + STR_JAVA_PATH + " " + STR_CLS_PATH + " " + STR_RMI_PATH + " " + 
			STR_RMI_HOSTNAME + strIoTSlaveObjectHostAdd + " " + STR_IOT_SLAVE_CLS + " " + strIoTMasterHostAdd + " " +
			commHan.getComPort(strObjName) + " " + commHan.getRMIRegPort(strObjName) + " " +
			commHan.getRMIStubPort(strObjName) + " > " + STR_LOG_FILE_PATH + strObjName + ".log &";
		String shellFile = "./" + strObjName + STR_SHELL_FILE_EXT;
		createWrapperShellScript(strJavaCommand, shellFile);
		// Send the file to the compute node
		String strCmdSend = "scp " + shellFile + " " + STR_USERNAME + strIoTSlaveObjectHostAdd + ":" + STR_RUNTIME_DIR;
		runCommand(strCmdSend);
		System.out.println("IoTMaster: Sending shell file: " + strCmdSend);
		return STR_SSH + " " + STR_USERNAME + strIoTSlaveObjectHostAdd + " cd " + STR_RUNTIME_DIR + " " + shellFile;
	}


	/**
	 * Construct command line for C++ IoTSlave
	 *
	 * @return       String
	 */
	private String getCmdCppDriverIoTSlave(String strIoTMasterHostAdd, String strIoTSlaveObjectHostAdd, String strObjName) {

		return STR_SSH + " " + STR_USERNAME + strIoTSlaveObjectHostAdd + " cd " +
					STR_SLAVE_DIR + " sudo " + STR_IOTSLAVE_CPP + " " + strIoTMasterHostAdd + " " +
					commHan.getComPort(strObjName) + " " + strObjName;
	}


	/**
	 * createWrapperShellScript() gets a wrapper shell script
	 *
	 * @param   strCommand 		String command
	 * @param   strObjectName 	String object name
	 * @return  PrintWriter
	 */
	private void createWrapperShellScript(String strCommand, String strFileName) {

		PrintWriter printWriter = getPrintWriter(strFileName);
		printWriter.println(strCommand);
		printWriter.close();
		runCommand("chmod 755 " + strFileName);
	}


	/**
	 * A private method to create an object on a specific machine
	 *
	 * @params  strObjName  				String object name
	 * @params  strObjClassName 			String object class name
	 * @params  strObjClassInterfaceName 	String object class interface name
	 * @params  strIoTSlaveObjectHostAdd 	String IoTSlave host address
	 * @params  strFieldObjectID  			String field object ID
	 * @params  arrFieldValues				Array of field values
	 * @params  arrFieldClasses				Array of field classes
	 * @return  void
	 */
	private void createObject(String strObjName, String strObjClassName, String strObjClassInterfaceName, String strObjStubClsIntfaceName,
		String strIoTSlaveObjectHostAdd, String strFieldObjectID, Object[] arrFieldValues, Class[] arrFieldClasses) 
		throws IOException, FileNotFoundException, ClassNotFoundException, InterruptedException {

		// Read config file
		String sCfgFile = STR_IOT_CODE_PATH + strObjClassName + "/" + strObjClassName + STR_CFG_FILE_EXT;
		String strLanguageDriver = parseConfigFile(sCfgFile, STR_LANGUAGE + "_" + strObjName);
		if(strLanguageDriver == null)	// Read just the field LANGUAGE if the first read is null
			strLanguageDriver = parseConfigFile(sCfgFile, STR_LANGUAGE);
		if(strLanguageDriver == null) // Check nullness for the second time - report if it is still null
			throw new Error("IoTMaster: Language specification missing in config file: " + sCfgFile);
		// PROFILING
		long start = 0;
		long result = 0;
		// PROFILING
		start = System.currentTimeMillis();

		// Construct ssh command line
		// e.g. ssh rtrimana@dw-2.eecs.uci.edu cd <path>;
		//      java -cp $CLASSPATH:./*.jar
		//           -Djava.rmi.server.codebase=file:./*.jar
		//           iotruntime.IoTSlave dw-1.eecs.uci.edu 46151 23829 42874 &
		// The In-Port for IoTMaster is the Out-Port for IoTSlave and vice versa
		String strSSHCommand = null;
		if(strLanguageDriver.equals(STR_JAVA))
			strSSHCommand = getCmdJavaDriverIoTSlave(strIoTMasterHostAdd, strIoTSlaveObjectHostAdd, strObjName);
		else if(strLanguageDriver.equals(STR_CPP))
			strSSHCommand = getCmdCppDriverIoTSlave(strIoTMasterHostAdd, strIoTSlaveObjectHostAdd, strObjName);
		else
			throw new Error("IoTMaster: Language specification not recognized: " + strLanguageDriver);
		RuntimeOutput.print("IoTMaster: Language for " + strObjName + " is " + strLanguageDriver, BOOL_VERBOSE);

		RuntimeOutput.print(strSSHCommand, BOOL_VERBOSE);
		// Start a new thread to start a new JVM
		createThread(strSSHCommand);
		ServerSocket serverSocket = new ServerSocket(commHan.getComPort(strObjName));
		Socket socket = serverSocket.accept();
		//InputStream inStream = new ObjectInputStream(socket.getInputStream());
		//OutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
		InputStream inStream = null;
		OutputStream outStream = null;
		if(strLanguageDriver.equals(STR_JAVA)) {
			inStream = new ObjectInputStream(socket.getInputStream());
			outStream = new ObjectOutputStream(socket.getOutputStream());
		} else {	// At this point the language is certainly C++, otherwise would've complained above
			inStream = new BufferedInputStream(socket.getInputStream());
			outStream = new BufferedOutputStream(socket.getOutputStream());
			recvAck(inStream);
		}

		// PROFILING
		result = System.currentTimeMillis()-start;
		System.out.println("\n\n ==> Time needed to start JVM for " + strObjName + ": " + result + "\n\n");

		// PROFILING
		start = System.currentTimeMillis();

		if(strLanguageDriver.equals(STR_JAVA)) {
			sendFileToJavaSlaveDriver(serverSocket, inStream, outStream, strObjName, 
				strObjClassName, strObjClassInterfaceName, strObjStubClsIntfaceName,
				strIoTSlaveObjectHostAdd, strFieldObjectID, arrFieldValues, arrFieldClasses);
		} else {
			sendFileToCppSlaveDriver(strObjClassName, strIoTSlaveObjectHostAdd);
			createObjectCpp(strObjName, strObjClassName, strObjClassInterfaceName, strIoTSlaveObjectHostAdd,
			commHan.getRMIRegPort(strObjName), commHan.getRMIStubPort(strObjName), arrFieldValues, arrFieldClasses,
			outStream, inStream);
		}

		// PROFILING
		result = System.currentTimeMillis()-start;
		System.out.println("\n\n ==> Time needed to send JAR file for " + strObjName + ": " + result + "\n\n");

		// PROFILING
		start = System.currentTimeMillis();

		// Instrument the class source code and look for IoTSet for device addresses
		// e.g. @config private IoTSet<IoTDeviceAddress> lb_addresses;
		RuntimeOutput.print("IoTMaster: Instantiating for " + strObjClassName + " with objectID " + strFieldObjectID, BOOL_VERBOSE);
		// Get the object and the class names
		// Build objects for IoTSet and IoTRelation fields in the device object classes
		Object crimObj = mapClassNameToCrim.get(strObjClassName + strFieldObjectID);
		HashMap<String,Object> hmObjectFieldObjects = null;
		if (crimObj instanceof ClassRuntimeInstrumenterMaster) {
			ClassRuntimeInstrumenterMaster crim = (ClassRuntimeInstrumenterMaster) crimObj;
			hmObjectFieldObjects = crim.getFieldObjects();
		} else if (crimObj instanceof CRuntimeInstrumenterMaster) {
			CRuntimeInstrumenterMaster crim = (CRuntimeInstrumenterMaster) crimObj;
			hmObjectFieldObjects = crim.getFieldObjects();
		}
		for(Map.Entry<String,Object> map : hmObjectFieldObjects.entrySet()) {
			RuntimeOutput.print("IoTMaster: Object name: " + map.getValue().getClass().getName(), BOOL_VERBOSE);
			// Iterate over HashMap and choose between processing
			String strFieldName = map.getKey();
			String strClassName = map.getValue().getClass().getName();
			String strFieldIdentifier = strFieldName + strFieldObjectID;
			if(strClassName.equals(STR_SET_INSTRUMENTER_CLS)) {
				SetInstrumenter setInstrumenter = (SetInstrumenter) map.getValue();
				if(setInstrumenter.getObjTableName().equals(STR_IOT_DEV_ADD_CLS)) { 
				// Instrument the normal IoTDeviceAddress
					synchronized(this) {
						instrumentIoTSetDevice(strFieldIdentifier, strObjName, strFieldName, strIoTSlaveObjectHostAdd, inStream, outStream, strLanguageDriver);
					}
				} else if(setInstrumenter.getObjTableName().equals(STR_IOT_ZB_ADD_CLS)) { 
				// Instrument the IoTZigbeeAddress - special feature for Zigbee device support
					synchronized(this) {
						instrumentIoTSetZBDevice(map, strObjName, strFieldName, strIoTSlaveObjectHostAdd, inStream, outStream, strLanguageDriver);
					}
				} else if(setInstrumenter.getObjTableName().equals(STR_IOT_ADD_CLS)) { 
				// Instrument the IoTAddress
					synchronized(this) {
						instrumentIoTSetAddress(strFieldIdentifier, strFieldName, inStream, outStream, strLanguageDriver);
					}
				} else {
					String strErrMsg = "IoTMaster: Device driver object can only have IoTSet<IoTAddress>, IoTSet<IoTDeviceAddress>," +
										" or IoTSet<IoTZigbeeAddress>!";
					throw new Error(strErrMsg);
				}
			} else {
				String strErrMsg = "IoTMaster: Device driver object can only have IoTSet for addresses!";
				throw new Error(strErrMsg);
			}
		}
		// End the session
		// TODO: Change this later
		if(strLanguageDriver.equals(STR_JAVA)) {
			ObjectOutputStream oStream = (ObjectOutputStream) outStream;
			oStream.writeObject(new MessageSimple(IoTCommCode.END_SESSION));
		} else {	// C++ side for now will be running continuously because it's an infinite loop (not in a separate thread)
			createDriverObjectCpp(outStream, inStream);
			//endSessionCpp(outStream);
		}

		// PROFILING
		result = System.currentTimeMillis()-start;
		System.out.println("\n\n ==> Time needed to create object " + strObjName + " and instrument IoTDeviceAddress: " + result + "\n\n");

		// Closing streams
		outStream.close();
		inStream.close();
		socket.close();
		serverSocket.close();
	}


	/**
	 * A private method to create controller objects
	 *
	 * @return  void
	 */
	private void createDriverObjects() throws InterruptedException {

		// Create a list of threads
		List<Thread> threads = new ArrayList<Thread>();
		// Get the list of active controller objects and loop it
		List<String> listActiveControllerObject = commHan.getActiveControllerObjectList();
		for(String strObjName : listActiveControllerObject) {

			ObjectCreationInfo objCrtInfo = commHan.getObjectCreationInfo(strObjName);
			Thread objectThread = new Thread(new Runnable() {
				public void run() {
					synchronized(this) {
						try {
							createObject(strObjName, objCrtInfo.getObjectClassName(), objCrtInfo.getObjectClassInterfaceName(),
								objCrtInfo.getObjectStubClassInterfaceName(), objCrtInfo.getIoTSlaveObjectHostAdd(), 
								commHan.getFieldObjectID(strObjName), commHan.getArrayFieldValues(strObjName), 
								commHan.getArrayFieldClasses(strObjName));
						} catch (IOException 			| 
								 ClassNotFoundException |
								 InterruptedException ex) {
							ex.printStackTrace();
						}
					}
				}
			});
			threads.add(objectThread);
			objectThread.start();
		}
		// Join all threads
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}	


	/**
	 * A private method to instrument IoTSet
	 *
	 * @params  Map.Entry<String,Object>  Entry of map IoTSet instrumentation
	 * @params  strFieldName              String field name
	 * @params  strLanguage				  String language
	 * @return  void
	 */
	private void instrumentIoTSet(Map.Entry<String,Object> map, String strFieldName, String strObjControllerName, String strLanguage) 
		throws IOException, ClassNotFoundException, InterruptedException {
				
		// Get information from the set
		SetInstrumenter setInstrumenter = (SetInstrumenter) map.getValue();
		objInitHand.addField(strFieldName, IoTCommCode.CREATE_NEW_IOTSET);

		int iRows = setInstrumenter.numberOfRows();
		for(int iRow=0; iRow<iRows; iRow++) {
			// Get field classes and values
			arrFieldClasses = setInstrumenter.fieldClasses(iRow);
			arrFieldValues = setInstrumenter.fieldValues(iRow);
			// Get object ID and class name
			String strObjID = setInstrumenter.fieldObjectID(iRow);
			strObjClassName = setInstrumenter.fieldEntryType(strObjID);
			// Call the method to create an object
			instrumentObject(strObjID, strObjControllerName, strLanguage);
			objInitHand.addObjectIntoField(strFieldName, strIoTSlaveObjectHostAdd, strObjName,
				strObjClassName, strObjClassInterfaceName, strObjStubClsIntfaceName, commHan.getRMIRegPort(strObjName), 
				commHan.getRMIStubPort(strObjName));
		}
	}


	/**
	 * A private method to instrument IoTRelation
	 *
	 * @params  Map.Entry<String,Object>  Entry of map IoTRelation instrumentation
	 * @params  strFieldName              String field name
	 * @params  strLanguage				  String language
	 * @return  void
	 */
	private void instrumentIoTRelation(Map.Entry<String,Object> map, String strFieldName, String strObjControllerName, String strLanguage) 
		throws IOException, ClassNotFoundException, InterruptedException {

			// Get information from the set
		RelationInstrumenter relationInstrumenter = (RelationInstrumenter) map.getValue();
		int iRows = relationInstrumenter.numberOfRows();
		objInitHand.addField(strFieldName, IoTCommCode.CREATE_NEW_IOTRELATION);

		for(int iRow=0; iRow<iRows; iRow++) {
			// Operate on the first set first
			arrFieldClasses = relationInstrumenter.firstFieldClasses(iRow);
			arrFieldValues = relationInstrumenter.firstFieldValues(iRow);
			String strObjID = relationInstrumenter.firstFieldObjectID(iRow);
			strObjClassName = relationInstrumenter.firstEntryFieldType(strObjID);
			// Call the method to create an object
			instrumentObject(strObjID, strObjControllerName, strLanguage);
			// Get the first object controller host address
			String strFirstIoTSlaveObjectHostAdd = strIoTSlaveObjectHostAdd;
			objInitHand.addObjectIntoField(strFieldName, strIoTSlaveObjectHostAdd, strObjName,
				strObjClassName, strObjClassInterfaceName, strObjStubClsIntfaceName, 
				commHan.getRMIRegPort(strObjName), commHan.getRMIStubPort(strObjName));
			// Operate on the second set
			arrFieldClasses = relationInstrumenter.secondFieldClasses(iRow);
			arrFieldValues = relationInstrumenter.secondFieldValues(iRow);
			strObjID = relationInstrumenter.secondFieldObjectID(iRow);
			strObjClassName = relationInstrumenter.secondEntryFieldType(strObjID);
			// Call the method to create an object
			instrumentObject(strObjID, strObjControllerName, strLanguage);
			// Get the second object controller host address
			String strSecondIoTSlaveObjectHostAdd = strIoTSlaveObjectHostAdd;
			objInitHand.addSecondObjectIntoField(strFieldName, strIoTSlaveObjectHostAdd, strObjName,
				strObjClassName, strObjClassInterfaceName, strObjStubClsIntfaceName, 
				commHan.getRMIRegPort(strObjName), commHan.getRMIStubPort(strObjName));
			// ROUTING POLICY: first and second controller objects in IoTRelation
			routerConfig.configureRouterMainPolicies(STR_ROUTER_ADD, strFirstIoTSlaveObjectHostAdd,
				strSecondIoTSlaveObjectHostAdd, STR_TCP_PROTOCOL);
			// ROUTING POLICY: Send the same routing policy to both the hosts
			routerConfig.configureHostMainPolicies(strFirstIoTSlaveObjectHostAdd, strFirstIoTSlaveObjectHostAdd,
				strSecondIoTSlaveObjectHostAdd, STR_TCP_PROTOCOL);
			routerConfig.configureHostMainPolicies(strSecondIoTSlaveObjectHostAdd, strFirstIoTSlaveObjectHostAdd,
				strSecondIoTSlaveObjectHostAdd, STR_TCP_PROTOCOL);
		}
	}

	/**
	 * A method to reinitialize IoTSet and IoTRelation in the code based on ObjectInitHandler information
	 *
	 * @params  inStream                  ObjectInputStream communication
	 * @params  outStream                 ObjectOutputStream communication
	 * @return	void
	 */
	private void initializeSetsAndRelationsJava(InputStream inStream, OutputStream outStream)  
		throws IOException, ClassNotFoundException {
		// Get list of fields
		List<String> strFields = objInitHand.getListOfFields();
		// Iterate on HostAddress
		for(String str : strFields) {
			IoTCommCode iotcommMsg = objInitHand.getFieldMessage(str);
			if (iotcommMsg == IoTCommCode.CREATE_NEW_IOTSET) {
				// == COMMUNICATION WITH IOTSLAVE CONTROLLER TO CREATE IOTSET
				Message msgCrtIoTSet = new MessageCreateSetRelation(IoTCommCode.CREATE_NEW_IOTSET, str);
				commMasterToSlave(msgCrtIoTSet, "Create new IoTSet!", inStream, outStream);
				List<ObjectInitInfo> listObject = objInitHand.getListObjectInitInfo(str);
				for (ObjectInitInfo objInitInfo : listObject) {
					// == COMMUNICATION WITH IOTSLAVE CONTROLLER TO FILL IN IOTSET
					commMasterToSlave(new MessageGetObject(IoTCommCode.GET_IOTSET_OBJECT, objInitInfo.getIoTSlaveObjectHostAdd(),
						objInitInfo.getObjectName(), objInitInfo.getObjectClassName(), objInitInfo.getObjectClassInterfaceName(), 
						objInitInfo.getObjectStubClassInterfaceName(), objInitInfo.getRMIRegistryPort(), objInitInfo.getRMIStubPort()), 
						"Get IoTSet object!", inStream, outStream);

				}
				// == COMMUNICATION WITH IOTSLAVE CONTROLLER TO REINITIALIZE IOTSET FIELD
				commMasterToSlave(new MessageSimple(IoTCommCode.REINITIALIZE_IOTSET_FIELD),
					"Renitialize IoTSet field!", inStream, outStream);
			} else if (iotcommMsg == IoTCommCode.CREATE_NEW_IOTRELATION) {
				// == COMMUNICATION WITH IOTSLAVE CONTROLLER TO CREATE IOTRELATION
				Message msgCrtIoTRel = new MessageCreateSetRelation(IoTCommCode.CREATE_NEW_IOTRELATION, str);
				commMasterToSlave(msgCrtIoTRel, "Create new IoTRelation!", inStream, outStream);
				List<ObjectInitInfo> listObject = objInitHand.getListObjectInitInfo(str);
				List<ObjectInitInfo> listSecondObject = objInitHand.getSecondObjectInitInfo(str);
				Iterator it = listSecondObject.iterator();
				for (ObjectInitInfo objInitInfo : listObject) {
					// == COMMUNICATION WITH IOTSLAVE CONTROLLER TO FILL IN IOTRELATION (FIRST OBJECT)
					commMasterToSlave(new MessageGetObject(IoTCommCode.GET_IOTRELATION_FIRST_OBJECT, 
						objInitInfo.getIoTSlaveObjectHostAdd(), objInitInfo.getObjectName(), objInitInfo.getObjectClassName(),
						objInitInfo.getObjectClassInterfaceName(), objInitInfo.getObjectStubClassInterfaceName(),
						objInitInfo.getRMIRegistryPort(), objInitInfo.getRMIStubPort()), 
						"Get IoTRelation first object!", inStream, outStream);
					ObjectInitInfo objSecObj = (ObjectInitInfo) it.next();
					// == COMMUNICATION WITH IOTSLAVE CONTROLLER TO FILL IN IOTRELATION (SECOND OBJECT)
					commMasterToSlave(new MessageGetObject(IoTCommCode.GET_IOTRELATION_SECOND_OBJECT,
						objSecObj.getIoTSlaveObjectHostAdd(), objSecObj.getObjectName(), objSecObj.getObjectClassName(),
						objSecObj.getObjectClassInterfaceName(), objSecObj.getObjectStubClassInterfaceName(),
						objSecObj.getRMIRegistryPort(), objSecObj.getRMIStubPort()), 
						"Get IoTRelation second object!", inStream, outStream);
				}
				// == COMMUNICATION WITH IOTSLAVE CONTROLLER TO REINITIALIZE IOTRELATION FIELD
				commMasterToSlave(new MessageSimple(IoTCommCode.REINITIALIZE_IOTRELATION_FIELD),
					"Renitialize IoTRelation field!", inStream, outStream);
			}
		}
	}

	/**
	 * A method to reinitialize IoTSet and IoTRelation in the code based on ObjectInitHandler information
	 *
	 * @params  inStream                  ObjectInputStream communication
	 * @params  outStream                 ObjectOutputStream communication
	 * @return	void
	 */
	private void initializeSetsAndRelationsCpp(InputStream inStream, OutputStream outStream)  
		throws IOException, ClassNotFoundException {
		// Get list of fields
		List<String> strFields = objInitHand.getListOfFields();
		// Iterate on HostAddress
		for(String str : strFields) {
			IoTCommCode iotcommMsg = objInitHand.getFieldMessage(str);
			if (iotcommMsg == IoTCommCode.CREATE_NEW_IOTSET) {
				// == COMMUNICATION WITH IOTSLAVE CONTROLLER TO CREATE IOTSET
				createNewIoTSetCpp(str, outStream, inStream);
				List<ObjectInitInfo> listObject = objInitHand.getListObjectInitInfo(str);
				for (ObjectInitInfo objInitInfo : listObject) {
					// == COMMUNICATION WITH IOTSLAVE CONTROLLER TO FILL IN IOTSET
					getIoTSetRelationObjectCpp(IoTCommCode.GET_IOTSET_OBJECT, objInitInfo.getIoTSlaveObjectHostAdd(), objInitInfo.getObjectName(), 
						objInitInfo.getObjectClassName(), objInitInfo.getObjectClassInterfaceName(), objInitInfo.getObjectStubClassInterfaceName(),
 						objInitInfo.getRMIRegistryPort(), objInitInfo.getRMIStubPort(), outStream, inStream);
				}
				// == COMMUNICATION WITH IOTSLAVE CONTROLLER TO REINITIALIZE IOTSET FIELD
				reinitializeIoTSetFieldCpp(outStream, inStream);
			} else if (iotcommMsg == IoTCommCode.CREATE_NEW_IOTRELATION) {
				// == COMMUNICATION WITH IOTSLAVE CONTROLLER TO CREATE IOTRELATION
				// TODO: createNewIoTRelation needs to be created here!
				createNewIoTRelationCpp(str, outStream, inStream);
				List<ObjectInitInfo> listObject = objInitHand.getListObjectInitInfo(str);
				List<ObjectInitInfo> listSecondObject = objInitHand.getSecondObjectInitInfo(str);
				Iterator it = listSecondObject.iterator();
				for (ObjectInitInfo objInitInfo : listObject) {
					// == COMMUNICATION WITH IOTSLAVE CONTROLLER TO FILL IN IOTRELATION (FIRST OBJECT)
					getIoTSetRelationObjectCpp(IoTCommCode.GET_IOTRELATION_FIRST_OBJECT, objInitInfo.getIoTSlaveObjectHostAdd(), objInitInfo.getObjectName(), 
						objInitInfo.getObjectClassName(), objInitInfo.getObjectClassInterfaceName(), objInitInfo.getObjectStubClassInterfaceName(),
 						objInitInfo.getRMIRegistryPort(), objInitInfo.getRMIStubPort(), outStream, inStream);
					ObjectInitInfo objSecObj = (ObjectInitInfo) it.next();
					// == COMMUNICATION WITH IOTSLAVE CONTROLLER TO FILL IN IOTRELATION (SECOND OBJECT)
					getIoTSetRelationObjectCpp(IoTCommCode.GET_IOTRELATION_SECOND_OBJECT, objSecObj.getIoTSlaveObjectHostAdd(), objSecObj.getObjectName(), 
						objSecObj.getObjectClassName(), objSecObj.getObjectClassInterfaceName(), objSecObj.getObjectStubClassInterfaceName(),
						objSecObj.getRMIRegistryPort(), objSecObj.getRMIStubPort(), outStream, inStream);
				}
				// == COMMUNICATION WITH IOTSLAVE CONTROLLER TO REINITIALIZE IOTRELATION FIELD
				reinitializeIoTRelationFieldCpp(outStream, inStream);
			}
		}
	}

	/**
	 * A method to set router basic policies at once
	 *
	 * @param	strRouter String router name
	 * @return	void
	 */
	private void setRouterBasicPolicies(String strRouter) {

		String strMonitorHost = routerConfig.getIPFromMACAddress(STR_MONITORING_HOST);
		routerConfig.configureRouterICMPPolicies(strRouter, strMonitorHost);
		routerConfig.configureRouterDHCPPolicies(strRouter);
		routerConfig.configureRouterDNSPolicies(strRouter);
		routerConfig.configureRouterSSHPolicies(strRouter, strMonitorHost);
		routerConfig.configureRejectPolicies(strRouter);
	}

	/**
	 * A method to set host basic policies at once
	 *
	 * @param	strHost String host name
	 * @return	void
	 */
	private void setHostBasicPolicies(String strHost) {

		String strMonitorHost = routerConfig.getIPFromMACAddress(STR_MONITORING_HOST);
		routerConfig.configureHostDHCPPolicies(strHost);
		routerConfig.configureHostDNSPolicies(strHost);
		if (strHost.equals(strMonitorHost)) {
		// Check if this is the monitoring host
			routerConfig.configureHostICMPPolicies(strHost);
			routerConfig.configureHostSSHPolicies(strHost);
		} else {
			routerConfig.configureHostICMPPolicies(strHost, strMonitorHost);
			routerConfig.configureHostSSHPolicies(strHost, strMonitorHost);
		}
		// Apply SQL allowance policies to master host
		if (strHost.equals(strIoTMasterHostAdd)) {
			routerConfig.configureHostSQLPolicies(strHost);
		}
		routerConfig.configureRejectPolicies(strHost);
	}

	/**
	 * A method to create a thread for policy deployment
	 *
	 * @param  strRouterAddress		String router address to configure
	 * @param  setHostAddresses		Set of strings for host addresses to configure
	 * @return            			void
	 */
	private void createPolicyThreads(String strRouterAddress, Set<String> setHostAddresses) throws IOException {

		// Create a list of threads
		List<Thread> threads = new ArrayList<Thread>();
		// Start threads for hosts
		for(String strAddress : setHostAddresses) {
			Thread policyThread = new Thread(new Runnable() {
				public void run() {
					synchronized(this) {
						routerConfig.sendHostPolicies(strAddress);
					}
				}
			});
			threads.add(policyThread);
			policyThread.start();
			RuntimeOutput.print("Deploying policies for: " + strAddress, BOOL_VERBOSE);
		}
		// A thread for router
		Thread policyThread = new Thread(new Runnable() {
			public void run() {
				synchronized(this) {
					routerConfig.sendRouterPolicies(strRouterAddress);
				}
			}
		});
		threads.add(policyThread);
		policyThread.start();
		RuntimeOutput.print("Deploying policies on router: " + strRouterAddress, BOOL_VERBOSE);		
		// Join all threads
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * A method to create a thread for policy deployment
	 *
	 * @param  setHostAddresses		Set of strings for host addresses to configure
	 * @return            			void
	 */
	private void createMACPolicyThreads(Set<String> setHostAddresses) throws IOException {

		// Create a list of threads
		List<Thread> threads = new ArrayList<Thread>();
		// Start threads for hosts
		for(String strAddress : setHostAddresses) {
			Thread policyThread = new Thread(new Runnable() {
				public void run() {
					synchronized(this) {
						processJailConfig.sendMACPolicies(strAddress);
					}
				}
			});
			threads.add(policyThread);
			policyThread.start();
			RuntimeOutput.print("Deploying MAC policies for: " + strAddress, BOOL_VERBOSE);
		}
		// Join all threads
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}


	/**
	 * A method to send files to Java IoTSlave
	 *
	 * @params  strObjControllerName      String
	 * @params  serverSocket              ServerSocket
	 * @params  inStream                  ObjectInputStream communication
	 * @params  outStream                 ObjectOutputStream communication
	 * @return       void
	 */	
	private void sendFileToJavaSlave(String strObjControllerName, ServerSocket serverSocket, 
			InputStream _inStream, OutputStream _outStream) throws IOException, ClassNotFoundException {

		ObjectInputStream inStream = (ObjectInputStream) _inStream;
		ObjectOutputStream outStream = (ObjectOutputStream) _outStream;
		// Send .jar file
		String strControllerJarName = strObjControllerName + STR_JAR_FILE_EXT;
		String strControllerJarNamePath = STR_CONT_PATH + strObjControllerName + "/" +
			strControllerJarName;
		File file = new File(strControllerJarNamePath);
		commMasterToSlave(new MessageSendFile(IoTCommCode.TRANSFER_FILE, strControllerJarName, file.length()),
			"Sending file!", inStream, outStream);
		// Send file - Class file for object creation
		sendFile(serverSocket.accept(), strControllerJarNamePath, file.length());
		Message msgReply = (Message) inStream.readObject();
		RuntimeOutput.print("IoTMaster: Reply message: " + msgReply.getMessage(), BOOL_VERBOSE);
		// Send .zip file if additional zip file is specified
		String strObjCfgFile = strObjControllerName + STR_CFG_FILE_EXT;
		String strObjCfgFilePath = STR_CONT_PATH + strObjControllerName + "/" + strObjCfgFile;
		String strAdditionalFile = parseConfigFile(strObjCfgFilePath, STR_FILE_TRF_CFG);
		if (strAdditionalFile.equals(STR_YES)) {
			String strControllerCmpName = strObjControllerName + STR_ZIP_FILE_EXT;
			String strControllerCmpNamePath = STR_CONT_PATH + strObjControllerName + "/" +
				strControllerCmpName;
			file = new File(strControllerCmpNamePath);
			commMasterToSlave(new MessageSendFile(IoTCommCode.TRANSFER_FILE, strControllerCmpName, file.length()),
				"Sending file!", inStream, outStream);
			// Send file - Class file for object creation
			sendFile(serverSocket.accept(), strControllerCmpNamePath, file.length());
			msgReply = (Message) inStream.readObject();
			RuntimeOutput.print("IoTMaster: Reply message: " + msgReply.getMessage(), BOOL_VERBOSE);
		}
	}


	/**
	 * A method to send files to C++ IoTSlave
	 *
	 * @return       void
	 * TODO: Need to look into this (as of now, file transferred retains the "data" format, 
	 * hence it is unreadable from outside world
	 */
	private void sendFileToCppSlave(String sFilePath, String sFileName, Socket fileSocket, 
			InputStream inStream, OutputStream outStream) throws IOException {

		sendCommCode(IoTCommCode.TRANSFER_FILE, outStream, inStream);
		// Send file name
		sendString(sFileName, outStream); recvAck(inStream);
		File file = new File(sFilePath + sFileName);
		int iFileLen = toIntExact(file.length());
		RuntimeOutput.print("IoTMaster: Sending file " + sFileName + " with length " + iFileLen + " bytes...", BOOL_VERBOSE);
		// Send file length
		sendInteger(iFileLen, outStream); recvAck(inStream);
		RuntimeOutput.print("IoTMaster: Sent file size!", BOOL_VERBOSE);
		byte[] bytFile = new byte[iFileLen];
		InputStream inFileStream = new FileInputStream(file);
		RuntimeOutput.print("IoTMaster: Opened file!", BOOL_VERBOSE);

		OutputStream outFileStream = fileSocket.getOutputStream();
		RuntimeOutput.print("IoTMaster: Got output stream!", BOOL_VERBOSE);
		int iCount;
		while ((iCount = inFileStream.read(bytFile)) > 0) {
			outFileStream.write(bytFile, 0, iCount);
		}
		RuntimeOutput.print("IoTMaster: File sent!", BOOL_VERBOSE);
		recvAck(inStream);
	}


	/**
	 * A method to send files to C++ IoTSlave (now master using Process() to start 
	 * file transfer using scp)
	 *
	 * @return       void
	 */
	private void sendFileToCppSlave(String sFilePath, String sFileName) throws IOException {

		// Construct shell command to transfer file	
		String sFile = sFilePath + sFileName;
		String strCmdSend = STR_SCP + " " + sFile + " " + STR_USERNAME + strIoTSlaveControllerHostAdd + ":" + STR_SLAVE_DIR;
		runCommand(strCmdSend);
		RuntimeOutput.print("IoTMaster: Executing: " + strCmdSend, BOOL_VERBOSE);
		// Unzip file
		String strCmdUnzip = STR_SSH + " " + STR_USERNAME + strIoTSlaveControllerHostAdd + " cd " +
					STR_SLAVE_DIR + " sudo unzip -o " + sFileName + ";";
		runCommand(strCmdUnzip);
		RuntimeOutput.print("IoTMaster: Executing: " + strCmdUnzip, BOOL_VERBOSE);
	}


	/**
	 * runCommand() method runs shell command
	 *
	 * @param   strCommand 	String that contains command line
	 * @return  void
	 */
	private void runCommand(String strCommand) {

		try {
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(strCommand);
			process.waitFor();
		} catch (IOException ex) {
			System.out.println("RouterConfig: IOException: " + ex.getMessage());
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			System.out.println("RouterConfig: InterruptException: " + ex.getMessage());
			ex.printStackTrace();
		}
	}


	/**
	 * Construct command line for Java IoTSlave
	 *
	 * @return       String
	 */
	private String getCmdJavaIoTSlave(String strObjControllerName) {

		// Create an Shell executable
		String strJavaCommand =	STR_SHELL_HEADER + "\nexec " + STR_JAVA_PATH + " " + STR_JVM_INIT_HEAP_SIZE + " " + 
					STR_JVM_MAX_HEAP_SIZE + " " + STR_CLS_PATH + " " + STR_RMI_PATH + " " + STR_IOT_SLAVE_CLS + " " + 
					strIoTMasterHostAdd + " " + commHan.getComPort(strObjControllerName) + " " +
					commHan.getRMIRegPort(strObjControllerName) + " " + commHan.getRMIStubPort(strObjControllerName) + 
					" > " + STR_LOG_FILE_PATH + strObjControllerName + ".log &";
		String shellFile = "./" + strObjControllerName + STR_SHELL_FILE_EXT;
		createWrapperShellScript(strJavaCommand, shellFile);
		// Send the file to the compute node
		String strCmdSend = "scp " + shellFile + " " + STR_USERNAME + strIoTSlaveControllerHostAdd + ":" + STR_RUNTIME_DIR;
		runCommand(strCmdSend);
		System.out.println("IoTMaster: Sending main controller shell file: " + strCmdSend);
		return STR_SSH + " " + STR_USERNAME + strIoTSlaveControllerHostAdd + " cd " + STR_RUNTIME_DIR + " " + shellFile;
	}


	/**
	 * Construct command line for C++ IoTSlave
	 *
	 * @return       String
	 */
	private String getCmdCppIoTSlave(String strObjControllerName) {

		return STR_SSH + " " + STR_USERNAME + strIoTSlaveControllerHostAdd + " cd " +
					STR_SLAVE_DIR + " sudo " + STR_IOTSLAVE_CPP + " " + strIoTMasterHostAdd + " " +
					commHan.getComPort(strObjControllerName) + " " + strObjControllerName;
	}


	/**
	 * sendInteger() sends an integer in bytes
	 */
	public void sendInteger(int intSend, OutputStream outStream) throws IOException {

		BufferedOutputStream output = (BufferedOutputStream) outStream;
		// Transform integer into bytes
		ByteBuffer bb = ByteBuffer.allocate(INT_SIZE);
		bb.putInt(intSend);
		// Send the byte array
		output.write(bb.array(), 0, INT_SIZE);
		output.flush();
	}


	/**
	 * recvInteger() receives integer in bytes
	 */
	public int recvInteger(InputStream inStream) throws IOException {

		BufferedInputStream input = (BufferedInputStream) inStream;
		// Wait until input is available
		while(input.available() == 0);
		// Read integer - 4 bytes
		byte[] recvInt = new byte[INT_SIZE];
		input.read(recvInt, 0, INT_SIZE);
		int retVal = ByteBuffer.wrap(recvInt).getInt();

		return retVal;
	}


	/**
	 * recvString() receives String in bytes
	 */
	public String recvString(InputStream inStream) throws IOException {

		BufferedInputStream input = (BufferedInputStream) inStream;
		int strLen = recvInteger(inStream);
		// Wait until input is available
		while(input.available() == 0);
		// Read String per strLen
		byte[] recvStr = new byte[strLen];
		input.read(recvStr, 0, strLen);
		String retVal = new String(recvStr);

		return retVal;
	}


	/**
	 * sendString() sends a String in bytes
	 */
	public void sendString(String strSend, OutputStream outStream) throws IOException {

		BufferedOutputStream output = (BufferedOutputStream) outStream;
		// Transform String into bytes
		byte[] strSendBytes = strSend.getBytes();
		int strLen = strSend.length();
		// Send the string length first
		sendInteger(strLen, outStream);
		// Send the byte array
		output.write(strSendBytes, 0, strLen);
		output.flush();
	}


	/**
	 * Convert integer to enum
	 */
	public IoTCommCode getCode(int intCode) throws IOException {

		IoTCommCode[] commCode = IoTCommCode.values();
		IoTCommCode retCode = commCode[intCode];
		return retCode;

	}


	/**
	 * Receive ACK
	 */
	public synchronized boolean recvAck(InputStream inStream) throws IOException {

		int intAck = recvInteger(inStream);
		IoTCommCode codeAck = getCode(intAck);
		if (codeAck == IoTCommCode.ACKNOWLEDGED)
			return true;
		return false;

	}


	/**
	 * Send END
	 */
	public void sendEndTransfer(OutputStream outStream) throws IOException {

		int endCode = IoTCommCode.END_TRANSFER.ordinal();
		sendInteger(endCode, outStream);
	}


	/**
	 * Send communication code to C++
	 */
	public void sendCommCode(IoTCommCode inpCommCode, OutputStream outStream, InputStream inStream) throws IOException {


		IoTCommCode commCode = inpCommCode;
		int intCode = commCode.ordinal();
		// TODO: delete this later
		System.out.println("DEBUG: Sending " + commCode + " with ordinal: " + intCode);
		sendInteger(intCode, outStream); recvAck(inStream);
	}


	/**
	 * Create a main controller object for C++
	 */
	public void createMainObjectCpp(String strObjControllerName, OutputStream outStream, InputStream inStream) throws IOException {

		sendCommCode(IoTCommCode.CREATE_MAIN_OBJECT, outStream, inStream);
		String strMainObjName = strObjControllerName;
		sendString(strMainObjName, outStream); recvAck(inStream);
		RuntimeOutput.print("IoTMaster: Create a main object: " + strMainObjName, BOOL_VERBOSE);
	}


	/**
	 * A helper function that converts Class into String
	 *
	 * @param  strDataType  String MySQL data type
	 * @return              Class
	 */
	public String getClassConverted(Class<?> cls) {

		if (cls == String.class) {
			return "string";
		} else if (cls == int.class) {
			return "int";
		} else {
			return null;
		}
	}


	/**
	 * A helper function that converts Object into String for transfer to C++ slave
	 *
	 * @param  obj           Object to be converted
	 * @param  strClassType  String Java Class type
	 * @return               Object
	 */
	public String getObjectConverted(Object obj) {

		if (obj instanceof String) {
			return (String) obj;
		} else if (obj instanceof Integer) {
			return Integer.toString((Integer) obj);
		} else {
			return null;
		}
	}


	/**
	 * Create a driver object for C++
	 */
	public void createObjectCpp(String strObjName, String strObjClassName, String strObjClassInterfaceName, String strIoTSlaveObjectHostAdd, 
		Integer iRMIRegistryPort, Integer iRMIStubPort, Object[] arrFieldValues, Class[] arrFieldClasses, 
		OutputStream outStream, InputStream inStream) throws IOException {

		sendCommCode(IoTCommCode.CREATE_OBJECT, outStream, inStream);
		RuntimeOutput.print("IoTMaster: Send request to create a driver object... ", BOOL_VERBOSE);
		RuntimeOutput.print("IoTMaster: Driver object name: " + strObjName, BOOL_VERBOSE);
		sendString(strObjName, outStream); recvAck(inStream);
		RuntimeOutput.print("IoTMaster: Driver object class name: " + strObjClassName, BOOL_VERBOSE);
		sendString(strObjClassName, outStream); recvAck(inStream);
		RuntimeOutput.print("IoTMaster: Driver object interface name: " + strObjClassInterfaceName, BOOL_VERBOSE);
		sendString(strObjStubClsIntfaceName, outStream); recvAck(inStream);
		RuntimeOutput.print("IoTMaster: Driver object skeleton class name: " + strObjClassInterfaceName + STR_SKEL_CLASS_SUFFIX, BOOL_VERBOSE);
		sendString(strObjClassInterfaceName + STR_SKEL_CLASS_SUFFIX, outStream); recvAck(inStream);
		RuntimeOutput.print("IoTMaster: Driver object registry port: " + iRMIRegistryPort, BOOL_VERBOSE);
		sendInteger(iRMIRegistryPort, outStream); recvAck(inStream);
		RuntimeOutput.print("IoTMaster: Driver object stub port: " + iRMIStubPort, BOOL_VERBOSE);
		sendInteger(iRMIStubPort, outStream); recvAck(inStream);
		int numOfArgs = arrFieldValues.length;
		RuntimeOutput.print("IoTMaster: Send constructor arguments! Number of arguments: " + numOfArgs, BOOL_VERBOSE);
		sendInteger(numOfArgs, outStream); recvAck(inStream);
		for(Object obj : arrFieldValues) {
			String str = getObjectConverted(obj);
			sendString(str, outStream); recvAck(inStream);
		}
		RuntimeOutput.print("IoTMaster: Send constructor argument classes!", BOOL_VERBOSE);
		for(Class cls : arrFieldClasses) {
			String str = getClassConverted(cls);
			sendString(str, outStream); recvAck(inStream);
		}
	}


	/**
	 * Create new IoTSet for C++
	 */
	public void createNewIoTSetCpp(String strObjFieldName, OutputStream outStream, InputStream inStream) throws IOException {

		sendCommCode(IoTCommCode.CREATE_NEW_IOTSET, outStream, inStream);
		RuntimeOutput.print("IoTMaster: Creating new IoTSet...", BOOL_VERBOSE);
		RuntimeOutput.print("IoTMaster: Send object field name: " + strObjFieldName, BOOL_VERBOSE);
		sendString(strObjFieldName, outStream); recvAck(inStream);
	}


	/**
	 * Create new IoTRelation for C++
	 */
	public void createNewIoTRelationCpp(String strObjFieldName, OutputStream outStream, InputStream inStream) throws IOException {

		sendCommCode(IoTCommCode.CREATE_NEW_IOTRELATION, outStream, inStream);
		RuntimeOutput.print("IoTMaster: Creating new IoTRelation...", BOOL_VERBOSE);
		RuntimeOutput.print("IoTMaster: Send object field name: " + strObjFieldName, BOOL_VERBOSE);
		sendString(strObjFieldName, outStream); recvAck(inStream);
	}


	/**
	 * Get a IoTDeviceAddress object for C++
	 */
	public void getDeviceIoTSetObjectCpp(OutputStream outStream, InputStream inStream,
			String strDeviceAddress, int iSourcePort, int iDestPort, boolean bSourceWildCard, boolean bDestWildCard) throws IOException {

		sendCommCode(IoTCommCode.GET_DEVICE_IOTSET_OBJECT, outStream, inStream);
		RuntimeOutput.print("IoTMaster: Getting IoTDeviceAddress...", BOOL_VERBOSE);
		sendString(strDeviceAddress, outStream); recvAck(inStream);
		sendInteger(iSourcePort, outStream); recvAck(inStream);
		sendInteger(iDestPort, outStream); recvAck(inStream);
		int iSourceWildCard = (bSourceWildCard ? 1 : 0);
		sendInteger(iSourceWildCard, outStream); recvAck(inStream);
		int iDestWildCard = (bDestWildCard ? 1 : 0);
		sendInteger(iDestWildCard, outStream); recvAck(inStream);
		RuntimeOutput.print("IoTMaster: Send device address: " + strDeviceAddress, BOOL_VERBOSE);
	}


	/**
	 * Get a IoTSet content object for C++
	 */
	public void getIoTSetRelationObjectCpp(IoTCommCode iotCommCode, String strIoTSlaveHostAddress, String strObjectName, String strObjectClassName, 
			String strObjectClassInterfaceName, String strObjectStubClassInterfaceName, int iRMIRegistryPort, int iRMIStubPort, 
			OutputStream outStream, InputStream inStream) throws IOException {

		sendCommCode(iotCommCode, outStream, inStream);
		RuntimeOutput.print("IoTMaster: Getting IoTSet object content...", BOOL_VERBOSE);
		// Send info
		RuntimeOutput.print("IoTMaster: Send host address: " + strIoTSlaveHostAddress, BOOL_VERBOSE);
		sendString(strIoTSlaveHostAddress, outStream); recvAck(inStream);
		RuntimeOutput.print("IoTMaster: Driver object name: " + strObjectName, BOOL_VERBOSE);
		sendString(strObjectName, outStream); recvAck(inStream);
		RuntimeOutput.print("IoTMaster: Driver object class name: " + strObjectClassName, BOOL_VERBOSE);
		sendString(strObjectClassName, outStream); recvAck(inStream);
		RuntimeOutput.print("IoTMaster: Driver object interface name: " + strObjectClassInterfaceName, BOOL_VERBOSE);
		sendString(strObjectClassInterfaceName, outStream); recvAck(inStream);
		RuntimeOutput.print("IoTMaster: Driver object stub class name: " + strObjectStubClassInterfaceName + STR_STUB_CLASS_SUFFIX, BOOL_VERBOSE);
		sendString(strObjectStubClassInterfaceName + STR_STUB_CLASS_SUFFIX, outStream); recvAck(inStream);
		RuntimeOutput.print("IoTMaster: Driver object registry port: " + iRMIRegistryPort, BOOL_VERBOSE);
		sendInteger(iRMIRegistryPort, outStream); recvAck(inStream);
		RuntimeOutput.print("IoTMaster: Driver object stub port: " + iRMIStubPort, BOOL_VERBOSE);
		sendInteger(iRMIStubPort, outStream); recvAck(inStream);
	}


	/**
	 * Reinitialize IoTRelation field for C++
	 */
	private void reinitializeIoTRelationFieldCpp(OutputStream outStream, InputStream inStream) throws IOException {

		RuntimeOutput.print("IoTMaster: About to Reinitialize IoTRelation field!", BOOL_VERBOSE);
		sendCommCode(IoTCommCode.REINITIALIZE_IOTRELATION_FIELD, outStream, inStream);
		RuntimeOutput.print("IoTMaster: Reinitialize IoTRelation field!", BOOL_VERBOSE);
	}


	/**
	 * Reinitialize IoTSet field for C++
	 */
	private void reinitializeIoTSetFieldCpp(OutputStream outStream, InputStream inStream) throws IOException {

		RuntimeOutput.print("IoTMaster: About to Reinitialize IoTSet field!", BOOL_VERBOSE);
		sendCommCode(IoTCommCode.REINITIALIZE_IOTSET_FIELD, outStream, inStream);
		RuntimeOutput.print("IoTMaster: Reinitialize IoTSet field!", BOOL_VERBOSE);
	}


	/**
	 * Create driver object for C++
	 */
	private void createDriverObjectCpp(OutputStream outStream, InputStream inStream) throws IOException {

		sendCommCode(IoTCommCode.CREATE_DRIVER_OBJECT, outStream, inStream);
		RuntimeOutput.print("IoTMaster: Send command to create driver object!", BOOL_VERBOSE);
	}


	/**
	 * Invoke init() for C++
	 */
	private void invokeInitMethodCpp(OutputStream outStream, InputStream inStream) throws IOException {

		sendCommCode(IoTCommCode.INVOKE_INIT_METHOD, outStream, inStream);
		RuntimeOutput.print("IoTMaster: Invoke init method!", BOOL_VERBOSE);
	}


	/**
	 * End session for C++
	 */
	public void endSessionCpp(OutputStream outStream) throws IOException {

		// Send message to end session
		IoTCommCode endSessionCode = IoTCommCode.END_SESSION;
		int intCode = endSessionCode.ordinal();
		sendInteger(intCode, outStream);
		//RuntimeOutput.print("IoTMaster: Send request to create a main object: " + strObjName, BOOL_VERBOSE);
		RuntimeOutput.print("IoTMaster: Send request to end session!", BOOL_VERBOSE);
	}


	/**
	 * A method to assign objects to multiple JVMs, including
	 * the controller/device object that uses other objects
	 * in IoTSet and IoTRelation
	 *
	 * @return       void
	 */
	private void createObjects() {

		// PROFILING
		long start = 0;
		long result = 0;

		try {
			// Extract hostname for this IoTMaster from MySQL DB
			strIoTMasterHostAdd = routerConfig.getIPFromMACAddress(STR_MASTER_MAC_ADD);
			// Loop as we can still find controller/device classes
			for(int i=0; i<strObjectNames.length; i++) {
				// PROFILING
				start = System.currentTimeMillis();

				// Assign a new list of PrintWriter objects
				routerConfig.renewPrintWriter();
				// Get controller names one by one
				String strObjControllerName = strObjectNames[i];
				// Use LoadBalancer to assign a host address
				//strIoTSlaveControllerHostAdd = lbIoT.selectHost();
				strIoTSlaveControllerHostAdd = routerConfig.getIPFromMACAddress(lbIoT.selectHost());
				if (strIoTSlaveControllerHostAdd == null)
					throw new Error("IoTMaster: Could not translate MAC to IP address! Please check the router's /tmp/dhcp.leases!");
				// == START INITIALIZING CONTROLLER/DEVICE IOTSLAVE ==
				// Add port connection and get port numbers
				// Naming for objects ProximitySensor becomes ProximitySensor0, ProximitySensor1, etc.
				commHan.addPortConnection(strIoTSlaveControllerHostAdd, strObjControllerName);
				// ROUTING POLICY: IoTMaster and main controller object
				routerConfig.configureRouterMainPolicies(STR_ROUTER_ADD, strIoTMasterHostAdd,
					strIoTSlaveControllerHostAdd, STR_TCP_PROTOCOL, commHan.getComPort(strObjControllerName));
				// ROUTING POLICY: Send the same routing policy to both the hosts
				routerConfig.configureHostMainPolicies(strIoTMasterHostAdd, strIoTMasterHostAdd,
					strIoTSlaveControllerHostAdd, STR_TCP_PROTOCOL, commHan.getComPort(strObjControllerName));
				routerConfig.configureHostMainPolicies(strIoTSlaveControllerHostAdd, strIoTMasterHostAdd,
					strIoTSlaveControllerHostAdd, STR_TCP_PROTOCOL, commHan.getComPort(strObjControllerName));
				// Read config file
				String strControllerCfg = STR_CONT_PATH + strObjControllerName + "/" + strObjControllerName + STR_CFG_FILE_EXT;
				STR_LANGUAGE_CONTROLLER = parseConfigFile(strControllerCfg, STR_LANGUAGE);
				if(STR_LANGUAGE_CONTROLLER == null)
					throw new Error("IoTMaster: Language specification missing in config file: " + strControllerCfg);
				// Construct ssh command line and create a controller thread for e.g. AcmeProximity
				String strSSHCommand = null;
				if(STR_LANGUAGE_CONTROLLER.equals(STR_JAVA))
					strSSHCommand = getCmdJavaIoTSlave(strObjControllerName);
				else if(STR_LANGUAGE_CONTROLLER.equals(STR_CPP))
					strSSHCommand = getCmdCppIoTSlave(strObjControllerName);
				else
					throw new Error("IoTMaster: Language specification not recognized: " + STR_LANGUAGE_CONTROLLER);
				RuntimeOutput.print(strSSHCommand, BOOL_VERBOSE);
				createThread(strSSHCommand);
				// Wait for connection
				// Create a new socket for communication
				ServerSocket serverSocket = new ServerSocket(commHan.getComPort(strObjControllerName));
				Socket socket = serverSocket.accept();
				InputStream inStream = null;
				OutputStream outStream = null;
				if(STR_LANGUAGE_CONTROLLER.equals(STR_JAVA)) {
					inStream = new ObjectInputStream(socket.getInputStream());
					outStream = new ObjectOutputStream(socket.getOutputStream());
				} else {	// At this point the language is certainly C++, otherwise would've complained above
					inStream = new BufferedInputStream(socket.getInputStream());
					outStream = new BufferedOutputStream(socket.getOutputStream());
					recvAck(inStream);
				}
				RuntimeOutput.print("IoTMaster: Communication established!", BOOL_VERBOSE);

				// PROFILING
				result = System.currentTimeMillis()-start;
				System.out.println("\n\n ==> From start until after SSH for main controller: " + result);
				// PROFILING
				start = System.currentTimeMillis();

				// Send files for every controller class
				// e.g. AcmeProximity.jar and AcmeProximity.zip
				String strControllerClassName = strObjControllerName + STR_CLS_FILE_EXT;
				String strControllerClassNamePath = STR_CONT_PATH + strObjControllerName + "/" +
					strControllerClassName;

				if(STR_LANGUAGE_CONTROLLER.equals(STR_JAVA)) {
					sendFileToJavaSlave(strObjControllerName, serverSocket, inStream, outStream);
					// Create main controller/device object
					commMasterToSlave(new MessageCreateMainObject(IoTCommCode.CREATE_MAIN_OBJECT, strObjControllerName),
						"Create main object!", inStream, outStream);
				} else {
					String strControllerZipFile = strObjControllerName + STR_ZIP_FILE_EXT;
					String strControllerFilePath = STR_CONT_PATH + strObjControllerName + "/";
					sendFileToCppSlave(strControllerFilePath, strControllerZipFile);
					createMainObjectCpp(strObjControllerName, outStream, inStream);
				}
				// Write basic MAC policies for controller
				//String strFileName = STR_MAC_POL_PATH + strObjControllerName + STR_MAC_POLICY_EXT;
				if (STR_ACTIVATE_SANDBOXING.equals("Yes")) {
					String strFileName = STR_MAC_POL_PATH + STR_JAVA + STR_MAC_POLICY_EXT;
					processJailConfig.configureProcessJailControllerPolicies(strObjControllerName, strFileName, 
						strIoTMasterHostAdd, commHan.getComPort(strObjControllerName));
				}
				// PROFILING
				result = System.currentTimeMillis()-start;
				System.out.println("\n\n ==> From IoTSlave start until main controller object is created: " + result);
				System.out.println(" ==> Including file transfer times!\n\n");
				// PROFILING
				start = System.currentTimeMillis();

				// == END INITIALIZING CONTROLLER/DEVICE IOTSLAVE ==
				// Instrumenting one file
				RuntimeOutput.print("IoTMaster: Opening class file: " + strControllerClassName, BOOL_VERBOSE);
				RuntimeOutput.print("IoTMaster: Class file path: " + strControllerClassNamePath, BOOL_VERBOSE);
				HashMap<String,Object> hmControllerFieldObjects = null;
				if(STR_LANGUAGE_CONTROLLER.equals(STR_JAVA)) {
					FileInputStream fis = new FileInputStream(strControllerClassNamePath);
					ClassReader cr = new ClassReader(fis);
					ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
					ClassRuntimeInstrumenterMaster crim = new ClassRuntimeInstrumenterMaster(cw, null, BOOL_VERBOSE);
					cr.accept(crim, 0);
					fis.close();
					hmControllerFieldObjects = crim.getFieldObjects();
				} else {
					String strControllerConfigFile = STR_CONT_PATH + strObjControllerName + "/" + strObjControllerName + STR_CFG_FILE_EXT;
					CRuntimeInstrumenterMaster crim = new CRuntimeInstrumenterMaster(strControllerConfigFile, null, BOOL_VERBOSE);
					hmControllerFieldObjects = crim.getFieldObjects();
				}
				// Get the object and the class names
				// Build objects for IoTSet and IoTRelation fields in the controller/device classes
				//HashMap<String,Object> hmControllerFieldObjects = crim.getFieldObjects();
				for(Map.Entry<String,Object> map : hmControllerFieldObjects.entrySet()) {
					RuntimeOutput.print("IoTMaster: Object name: " + map.getValue().getClass().getName(), BOOL_VERBOSE);
					// Iterate over HashMap and choose between processing
					// SetInstrumenter vs. RelationInstrumenter
					String strFieldName = map.getKey();
					String strClassName = map.getValue().getClass().getName();
					if(strClassName.equals(STR_SET_INSTRUMENTER_CLS)) {
						SetInstrumenter setInstrumenter = (SetInstrumenter) map.getValue();
						if(setInstrumenter.getObjTableName().equals(STR_IOT_DEV_ADD_CLS)) { 
							String strErrMsg = "IoTMaster: Controller object" +
								" cannot have IoTSet<IoTDeviceAddress>!";
							throw new Error(strErrMsg);
						} else if(setInstrumenter.getObjTableName().equals(STR_IOT_ZB_ADD_CLS)) { 
							String strErrMsg = "IoTMaster: Controller object" +
								" cannot have IoTSet<ZigbeeAddress>!";
							throw new Error(strErrMsg);
						} else if(setInstrumenter.getObjTableName().equals(STR_IOT_ADD_CLS)) { 
						// Instrument the IoTAddress
							setRouterPolicyIoTSetAddress(strFieldName, map, strIoTSlaveControllerHostAdd, strObjControllerName);
							instrumentIoTSetAddress(strFieldName, strFieldName, inStream, outStream, STR_LANGUAGE_CONTROLLER);
						} else {
						// Any other cases
							instrumentIoTSet(map, strFieldName, strObjControllerName, STR_LANGUAGE_CONTROLLER);
						}
					} else if (strClassName.equals(STR_REL_INSTRUMENTER_CLS)) {
						instrumentIoTRelation(map, strFieldName, strObjControllerName, STR_LANGUAGE_CONTROLLER);
					}
				}
				// Combine controller MAC policies with the main policy file for the host
				String strTempFileName = "./" + strObjControllerName + STR_MAC_POLICY_EXT;
				processJailConfig.combineControllerMACPolicies(strIoTSlaveControllerHostAdd, strObjControllerName, strTempFileName);
				processJailConfig.close();

				// PROFILING
				result = System.currentTimeMillis()-start;
				System.out.println("\n\n ==> Time needed to instrument device driver objects: " + result + "\n\n");
				System.out.println(" ==> #Objects: " + commHan.getActiveControllerObjectList().size() + "\n\n");

				// PROFILING
				start = System.currentTimeMillis();

				// ROUTING POLICY: Deploy basic policies if this is the last controller
				if (i == strObjectNames.length-1) {
					// ROUTING POLICY: implement basic policies to reject all other irrelevant traffics
					for(String s: commHan.getHosts()) {
						setHostBasicPolicies(s);
					}
					// We retain all the basic policies for router, 
					// but we delete the initial allowance policies for internal all TCP and UDP communications
					setRouterBasicPolicies(STR_ROUTER_ADD);
				}
				// Close access to policy files and deploy policies
				routerConfig.close();
				// Deploy the policy
				//HashSet<String> setAddresses = new HashSet<String>(commHan.getHosts());
				//setAddresses.add(strIoTMasterHostAdd);
				//createPolicyThreads(STR_ROUTER_ADD, setAddresses);

				// PROFILING
				result = System.currentTimeMillis()-start;
				System.out.println("\n\n ==> Time needed to send policy files and deploy them : " + result + "\n\n");

				// PROFILING
				start = System.currentTimeMillis();

				// Separating object creations and Set/Relation initializations
				createDriverObjects();

				// PROFILING
				result = System.currentTimeMillis()-start;
				System.out.println("\n\n ==> Time needed to instantiate objects: " + result + "\n\n");
				// PROFILING
				start = System.currentTimeMillis();

				// Sets and relations initializations
				if(STR_LANGUAGE_CONTROLLER.equals(STR_JAVA))
					initializeSetsAndRelationsJava(inStream, outStream);
				else
					initializeSetsAndRelationsCpp(inStream, outStream);;

				// PROFILING
				result = System.currentTimeMillis()-start;
				System.out.println("\n\n ==> Time needed to initialize sets and relations: " + result + "\n\n");

				if(STR_LANGUAGE_CONTROLLER.equals(STR_JAVA))
					// == COMMUNICATION WITH IOTSLAVE CONTROLLER TO EXECUTE INIT METHOD
					commMasterToSlave(new MessageSimple(IoTCommCode.INVOKE_INIT_METHOD), "Invoke init() method!", inStream, outStream);
				else
					invokeInitMethodCpp(outStream, inStream);
				// == COMMUNICATION WITH IOTSLAVE CONTROLLER TO END PROCESS
				if(STR_LANGUAGE_CONTROLLER.equals(STR_JAVA)) {
					ObjectOutputStream oStream = (ObjectOutputStream) outStream;
					oStream.writeObject(new MessageSimple(IoTCommCode.END_SESSION));
				} else	// C++ side will wait until the program finishes, it's not generating a separate thread for now
					//endSessionCpp(outStream);
				outStream.close();
				inStream.close();
				socket.close();
				serverSocket.close();
				commHan.printLists();
				lbIoT.printHostInfo();
				if (STR_ACTIVATE_SANDBOXING.equals("Yes")){
					//createMACPolicyThreads(setAddresses);
                }   
            }

		} catch (IOException          |
				 InterruptedException |
				 ClassNotFoundException ex) {
			System.out.println("IoTMaster: Exception: "
				+ ex.getMessage());
			ex.printStackTrace();
		}
	}

	public static void main(String args[]) {

		// Detect the available controller/device classes
		// Input args[] should be used to list the controllers/devices
		// e.g. java IoTMaster AcmeProximity AcmeThermostat AcmeVentController
		IoTMaster iotMaster = new IoTMaster(args);
		// Read config file
		iotMaster.parseIoTMasterConfigFile();
		// Initialize CommunicationHandler, LoadBalancer, and RouterConfig
		iotMaster.initLiveDataStructure();
		// Create objects
		iotMaster.createObjects();
	}
}
