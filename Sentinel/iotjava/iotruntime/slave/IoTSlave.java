package iotruntime.slave;

import iotruntime.*;
import iotruntime.zigbee.*;
import iotruntime.messages.*;
import iotruntime.master.RuntimeOutput;

// Java packages
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.ClassNotFoundException;
import java.lang.Class;
import java.lang.reflect.*;
import java.lang.ClassLoader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

// Zip/Unzip utility
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.core.ZipFile;

/** Class IoTSlave is run by IoTMaster on a different JVM's.
 *  It needs to respond to IoTMaster's commands
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-06-16
 */
public final class IoTSlave {

	/**
	 * IoTSlave class properties
	 */
	private Message sIoTMasterMsg;
	private String sIoTMasterHostAdd;
	private String sMainObjectName;
	private int iComPort;
	private int iRMIRegPort;
	private int iRMIStubPort;
	private String strFieldName;
	private Class<?> clsMain;
	private Object objMainCls;
	private Object iRelFirstObject;
	private Object iRelSecondObject;
	private Socket socket;
	private ObjectOutputStream outStream;
	private ObjectInputStream inStream;
	private Map<String,Object> mapObjNameStub;

	/**
	 * IoTSet object, e.g. IoTSet<ProximitySensor> proximity_sensors;
	 * IoTRelation object, e.g. IoTRelation<ProximitySensor, LightBulb> ps_lb_relation;
	 */
	private ISet<Object> isetObject;
	private IoTSet<Object> iotsetObject;
	private IRelation<Object,Object> irelObject;
	private IoTRelation<Object,Object> iotrelObject;

	// Constants that are to be extracted from config file
	private static String STR_JAR_FILE_PATH;
	private static String STR_OBJ_CLS_PFX;
	private static String STR_INTERFACE_PFX;
	private static String SKEL_CLASS_SUFFIX;
	private static String STUB_CLASS_SUFFIX;
	private static boolean BOOL_VERBOSE;
	private static boolean CAPAB_BASED_RMI;

	/**
	 * IoTSlave class constants - not to be changed by users
	 */
	private static final String STR_IOT_SLAVE_NAME = "IoTSlave";
	private static final String STR_CFG_FILE_EXT = ".config";
	private static final String STR_CLS_FILE_EXT = ".class";
	private static final String STR_JAR_FILE_EXT = ".jar";
	private static final String STR_ZIP_FILE_EXT = ".zip";
	private static final String STR_UNZIP_DIR = "./";
	private static final Class<?>[] STR_URL_PARAM = new Class[] {URL.class };
	private static final String STR_YES = "Yes";
	private static final String STR_NO = "No";

	/**
	 * Class constructor
	 *
	 */
	public IoTSlave(String[] argInp) {

		sIoTMasterMsg = null;
		sIoTMasterHostAdd = argInp[0];
		iComPort = Integer.parseInt(argInp[1]);
		iRMIRegPort = Integer.parseInt(argInp[2]);
		iRMIStubPort = Integer.parseInt(argInp[3]);
		sMainObjectName = null;
		strFieldName = null;
		clsMain = null;
		objMainCls = null;
		isetObject = null;
		iotsetObject = null;
		irelObject = null;
		iotrelObject = null;
		iRelFirstObject = null;
		iRelSecondObject = null;
		socket = null;
		outStream = null;
		inStream = null;
		mapObjNameStub = new HashMap<String,Object>();

		STR_JAR_FILE_PATH = null;
		STR_OBJ_CLS_PFX = null;
		STR_INTERFACE_PFX = null;
		SKEL_CLASS_SUFFIX = null;
		STUB_CLASS_SUFFIX = null;
		BOOL_VERBOSE = false;
		CAPAB_BASED_RMI = false;
	}

	/**
	 * A method to initialize constants from config file
	 *
	 * @return void
	 */
	private void parseIoTSlaveConfigFile() {
		// Parse configuration file
		Properties prop = new Properties();
		String strCfgFileName = STR_IOT_SLAVE_NAME + STR_CFG_FILE_EXT;
		File file = new File(strCfgFileName);
		try {
			FileInputStream fis = new FileInputStream(file);
			prop.load(fis);
		} catch (IOException ex) {
			System.out.println("IoTMaster: Error reading config file: " + strCfgFileName);
			ex.printStackTrace();
		}
		System.out.println("IoTMaster: Extracting information from config file: " + strCfgFileName);
		// Initialize constants from config file
		STR_JAR_FILE_PATH = prop.getProperty("JAR_FILE_PATH");
		STR_OBJ_CLS_PFX = prop.getProperty("OBJECT_CLASS_PREFIX");
		STR_INTERFACE_PFX = prop.getProperty("INTERFACE_PREFIX");
		SKEL_CLASS_SUFFIX = prop.getProperty("SKEL_CLASS_SUFFIX");
		STUB_CLASS_SUFFIX = prop.getProperty("STUB_CLASS_SUFFIX");
		if (prop.getProperty("VERBOSE").equals(STR_YES)) {
			BOOL_VERBOSE = true;
		}
		if (prop.getProperty("CAPAB_BASED_RMI").equals(STR_YES)) {
			CAPAB_BASED_RMI = true;
		}

		System.out.println("JAR_FILE_PATH=" + STR_JAR_FILE_PATH);
		System.out.println("OBJECT_CLASS_PREFIX=" + STR_OBJ_CLS_PFX);
		System.out.println("INTERFACE_PREFIX=" + STR_INTERFACE_PFX);
		System.out.println("SKEL_CLASS_SUFFIX=" + SKEL_CLASS_SUFFIX);
		System.out.println("STUB_CLASS_SUFFIX=" + STUB_CLASS_SUFFIX);
		System.out.println("CAPAB_BASED_RMI=" + CAPAB_BASED_RMI);
		System.out.println("IoTMaster: Information extracted successfully!");
	}

	/**
	 * Adds the content pointed by the URL to the classpath dynamically at runtime (hack!!!)
	 *
	 * @param  url         the URL pointing to the content to be added
	 * @throws IOException
	 * @see    <a href="http://stackoverflow.com/questions/60764/how-should-i-load-jars-dynamically-at-runtime</a>
	 */
	private static void addURL(URL url) throws IOException {

		URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		Class<?> sysclass = URLClassLoader.class;

		try {

			Method method = sysclass.getDeclaredMethod("addURL", STR_URL_PARAM);
			method.setAccessible(true);
			method.invoke(sysloader,new Object[] { url });

		} catch (Throwable t) {

			t.printStackTrace();
			throw new IOException("IoTSlave: Could not add URL to system classloader!");
		}
	}

	/**
	 * A private method to create object
	 *
	 * @return  void
	 */
	private void createCapabBasedRMIJava(MessageCreateObject sMessage) throws 
		ClassNotFoundException, NoSuchMethodException, UnknownHostException {

		// Instantiate the skeleton and put in the object
		String strObjSkelName = STR_OBJ_CLS_PFX + "." + sMessage.getObjectClass() +
									"." + sMessage.getObjectInterfaceName() + SKEL_CLASS_SUFFIX;
		RuntimeOutput.print("IoTSlave: Skeleton object: " + strObjSkelName, BOOL_VERBOSE);
		Class<?> clsSkel = Class.forName(strObjSkelName);
		Class<?> clsInt = Class.forName(STR_OBJ_CLS_PFX + "." + STR_INTERFACE_PFX + 
			"." + sMessage.getObjectInterfaceName());
		Class[] clsSkelParams = { clsInt, int.class, int.class };	// Port number is integer
		Constructor<?> objSkelCons = clsSkel.getDeclaredConstructor(clsSkelParams);
		Object objSkelParams[] = { objMainCls, iRMIStubPort, iRMIRegPort };
		// Create a new thread for each skeleton
		Thread objectThread = new Thread(new Runnable() {
			public void run() {
				try {
					Object objSkel = objSkelCons.newInstance(objSkelParams);
				} catch (InstantiationException |
						 IllegalAccessException |
						 InvocationTargetException ex) {
					ex.printStackTrace();
				}
			}
		});
		objectThread.start();
		RuntimeOutput.print("IoTSlave: Done generating object!", BOOL_VERBOSE);
	}

	/**
	 * A private method to create object
	 *
	 * @return  void
	 */
	private void createObject() throws IOException,
		ClassNotFoundException, NoSuchMethodException, InstantiationException,
			RemoteException, AlreadyBoundException, IllegalAccessException,
				InvocationTargetException {

		// Translating into the actual Message class
		MessageCreateObject sMessage = (MessageCreateObject) sIoTMasterMsg;
		// Instantiate object using reflection
		String strObjClassName = STR_OBJ_CLS_PFX + "." + sMessage.getObjectClass() +
														 "." + sMessage.getObjectClass();
		File file = new File(STR_JAR_FILE_PATH + sMessage.getObjectClass() + STR_JAR_FILE_EXT);
		RuntimeOutput.print("IoTSlave: DEBUG print path: " + STR_JAR_FILE_PATH +
											 sMessage.getObjectClass() + STR_JAR_FILE_EXT, BOOL_VERBOSE);
		addURL(file.toURI().toURL());
		clsMain = Class.forName(strObjClassName);
		Class[] clsParams = sMessage.getObjectFldCls();
		Constructor<?> ct = clsMain.getDeclaredConstructor(clsParams);
		Object objParams[] = sMessage.getObjectFields();
		objMainCls = ct.newInstance(objParams);
		RuntimeOutput.print("IoTSlave: Creating RMI skeleton: " +
			sMessage.getHostAddress() + ":" + sMessage.getRMIRegPort() +
			" with RMI stub port: " + iRMIStubPort, BOOL_VERBOSE);
		if (CAPAB_BASED_RMI) {
		// Use the new capability-based RMI in Java
			createCapabBasedRMIJava(sMessage);
		} else {
			// Register object to RMI - there are 2 ports: RMI registry port and RMI stub port
			Object objStub = (Object)
				UnicastRemoteObject.exportObject((Remote) objMainCls, iRMIStubPort);
			Registry registry = LocateRegistry.createRegistry(iRMIRegPort);
			registry.bind(sMessage.getObjectName(), (Remote) objStub);
		}
		outStream.writeObject(new MessageSimple(IoTCommCode.ACKNOWLEDGED));
		RuntimeOutput.print("IoTSlave: Registering object via RMI!", BOOL_VERBOSE);

	}

	
	/**
	 * A private method to transfer file
	 *
	 * @return  void
	 */
	private void transferFile() throws IOException,
		UnknownHostException, FileNotFoundException {

		// Translating into the actual Message class
		MessageSendFile sMessage = (MessageSendFile) sIoTMasterMsg;

		// Send back the received message as acknowledgement
		outStream.writeObject(new MessageSimple(IoTCommCode.ACKNOWLEDGED));

		// Write file to the current location
		Socket filesocket = new Socket(sIoTMasterHostAdd, iComPort);
		InputStream inFileStream = filesocket.getInputStream();
		OutputStream outFileStream = new FileOutputStream(sMessage.getFileName());
		byte[] bytFile = new byte[Math.toIntExact(sMessage.getFileSize())];

		int iCount = 0;
		while ((iCount = inFileStream.read(bytFile)) > 0) {
			outFileStream.write(bytFile, 0, iCount);
		}
		// Unzip if this is a zipped file
		if (sMessage.getFileName().contains(STR_ZIP_FILE_EXT)) {
			RuntimeOutput.print("IoTSlave: Unzipping file: " + sMessage.getFileName(), BOOL_VERBOSE);
			try {
				ZipFile zipFile = new ZipFile(sMessage.getFileName());
				zipFile.extractAll(STR_UNZIP_DIR);
			} catch (ZipException ex) {
				System.out.println("IoTSlave: Error in unzipping file!");
				ex.printStackTrace();
			}
		}
		outStream.writeObject(new MessageSimple(IoTCommCode.ACKNOWLEDGED));
		RuntimeOutput.print("IoTSlave: Receiving file transfer!", BOOL_VERBOSE);
	}

	/**
	 * A private method to create a main object
	 *
	 * @return  void
	 */
	private void createMainObject() throws IOException,
		ClassNotFoundException, InstantiationException, IllegalAccessException,
			InvocationTargetException {

		// Translating into the actual Message class
		MessageCreateMainObject sMessage = (MessageCreateMainObject) sIoTMasterMsg;

		// Getting controller class
		File file = new File(STR_JAR_FILE_PATH + sMessage.getObjectName() + STR_JAR_FILE_EXT);
		RuntimeOutput.print("IoTSlave: DEBUG print path: " + STR_JAR_FILE_PATH +
											 sMessage.getObjectName() + STR_JAR_FILE_EXT, BOOL_VERBOSE);
		addURL(file.toURI().toURL());
		// We will always have a package name <object name>.<object name>
		// e.g. SmartLightsController.SmartLightsController
		sMainObjectName = sMessage.getObjectName();
		clsMain = Class.forName(sMainObjectName + "." + sMainObjectName);
		objMainCls = clsMain.newInstance();

		// Send back the received message as acknowledgement
		outStream.writeObject(new MessageSimple(IoTCommCode.ACKNOWLEDGED));
		RuntimeOutput.print("IoTSlave: Instantiating main controller/device class "
											 + sMessage.getObjectName(), BOOL_VERBOSE);

	}

	/**
	 * A private method to create a new IoTSet
	 *
	 * @return  void
	 */
	private void createNewIoTSet() throws IOException {

		// Translating into the actual Message class
		MessageCreateSetRelation sMessage = (MessageCreateSetRelation) sIoTMasterMsg;

		// Initialize field name
		strFieldName = sMessage.getObjectFieldName();
		RuntimeOutput.print("IoTSlave: Setting up field " + strFieldName, BOOL_VERBOSE);

		// Creating a new IoTSet object
		isetObject = new ISet<Object>();

		// Send back the received message as acknowledgement
		outStream.writeObject(new MessageSimple(IoTCommCode.ACKNOWLEDGED));
		RuntimeOutput.print("IoTSlave: Creating a new IoTSet object!", BOOL_VERBOSE);

	}

	/**
	 * A private method to create a new IoTRelation
	 *
	 * @return  void
	 */
	private void createNewIoTRelation() throws IOException {

		// Translating into the actual Message class
		MessageCreateSetRelation sMessage = (MessageCreateSetRelation) sIoTMasterMsg;

		// Initialize field name
		strFieldName = sMessage.getObjectFieldName();
		RuntimeOutput.print("IoTSlave: Setting up field " + strFieldName, BOOL_VERBOSE);

		// Creating a new IoTRelation object
		irelObject = new IRelation<Object,Object>();

		// Send back the received message as acknowledgement
		outStream.writeObject(new MessageSimple(IoTCommCode.ACKNOWLEDGED));
		RuntimeOutput.print("IoTSlave: Creating a new IoTRelation object!", BOOL_VERBOSE);

	}

	/**
	 * A private method to get an object from the registry
	 *
	 * @return  Object
	 */
	private Object getObjectFromRegistry() throws RemoteException,
			ClassNotFoundException, NotBoundException {

		// Translating into the actual Message class
		MessageGetObject sMessage = (MessageGetObject) sIoTMasterMsg;

		// Locate RMI registry and add object into IoTSet
		Registry registry =
			LocateRegistry.getRegistry(sMessage.getHostAddress(), sMessage.getRMIRegPort());
		RuntimeOutput.print("IoTSlave: Looking for RMI registry: " +
			sMessage.getHostAddress() + ":" + sMessage.getRMIRegPort() +
			" with RMI stub port: " + sMessage.getRMIStubPort(), BOOL_VERBOSE);
		Object stubObj = registry.lookup(sMessage.getObjectName());
		RuntimeOutput.print("IoTSlave: Looking for object name: " + sMessage.getObjectName(), BOOL_VERBOSE);

		// Class conversion to interface class of this class,
		// e.g. ProximitySensorImpl has ProximitySensor interface
		String strObjClassInterfaceName = STR_OBJ_CLS_PFX + "." + STR_INTERFACE_PFX + "." +
			sMessage.getObjectInterfaceName();
		Class<?> clsInf = Class.forName(strObjClassInterfaceName);
		Object stubObjConv = clsInf.cast(stubObj);

		return stubObjConv;
	}

	/**
	 * A private method to get an object and create a stub
	 * <p>
	 * This is using the capability-based RMI skeleton and stub scheme
	 *
	 * @return  Object
	 */
	private Object getObjectFromStub() throws RemoteException,
			ClassNotFoundException, NoSuchMethodException, InstantiationException, 
			IllegalAccessException, NotBoundException, InvocationTargetException, UnknownHostException {

		// Translating into the actual Message class
		MessageGetObject sMessage = (MessageGetObject) sIoTMasterMsg;
		Object stubObjConv = null;
		String strObjectName = sMessage.getObjectName();
		String strObjClassInterfaceName = STR_OBJ_CLS_PFX + "." + STR_INTERFACE_PFX + "." +
			sMessage.getObjectStubInterfaceName();
		Class<?> clsInf = Class.forName(strObjClassInterfaceName);
		if (mapObjNameStub.containsKey(strObjectName)) {
			RuntimeOutput.print("IoTSlave: Getting back object on slave: " + strObjectName, BOOL_VERBOSE);
			stubObjConv = clsInf.cast(mapObjNameStub.get(strObjectName));
		} else {
			// Instantiate the stub and put in the object
			String strObjStubName = sMainObjectName + "." + sMessage.getObjectStubInterfaceName() + STUB_CLASS_SUFFIX;
			Class<?> clsStub = Class.forName(strObjStubName);	// Port number is integer
			Class[] clsStubParams = { int.class, int.class, int.class, int.class, String.class, int.class };
			Constructor<?> objStubCons = clsStub.getDeclaredConstructor(clsStubParams);

			int rev = 0;
			Object objStubParams[] = { 0, 0, sMessage.getRMIStubPort(), sMessage.getRMIRegPort(), sMessage.getHostAddress(), rev };
			RuntimeOutput.print("IoTSlave: Creating RMI stub: " +
				sMessage.getHostAddress() + ":" + sMessage.getRMIRegPort() + 
				" and RMI stub port: " + sMessage.getRMIStubPort(), BOOL_VERBOSE);
			Object stubObj = objStubCons.newInstance(objStubParams);
			// Class conversion to interface class of this class,
			// e.g. ProximitySensorImpl has ProximitySensor interface
			RuntimeOutput.print("IoTSlave: Registering new stub object: " + strObjectName, BOOL_VERBOSE);
			mapObjNameStub.put(strObjectName, stubObj);
			stubObjConv = clsInf.cast(stubObj);
		}

		return stubObjConv;
	}

	/**
	 * A private method to get an IoTSet object
	 *
	 * @return  void
	 */
	private void getIoTSetObject() throws IOException,
		ClassNotFoundException, RemoteException, NotBoundException, NoSuchMethodException,
		InstantiationException, IllegalAccessException, InvocationTargetException {
		Object objRegistry = null;
		if (CAPAB_BASED_RMI)
			objRegistry = getObjectFromStub();
		else
			objRegistry = getObjectFromRegistry();
		isetObject.add(objRegistry);
		RuntimeOutput.print("IoTSlave: This IoTSet now has: " + isetObject.size() + " entry(s)", BOOL_VERBOSE);

		// Send back the received message as acknowledgement
		outStream.writeObject(new MessageSimple(IoTCommCode.ACKNOWLEDGED));
		RuntimeOutput.print("IoTSlave: Getting an object for IoTSet!", BOOL_VERBOSE);
	}

	/**
	 * A private method to get an IoTRelation first object
	 *
	 * @return  void
	 */
	private void getIoTRelationFirstObject() throws IOException,
		ClassNotFoundException, RemoteException, NotBoundException, NoSuchMethodException,
		InstantiationException, IllegalAccessException, InvocationTargetException {
		Object objRegistry = null;
		if (CAPAB_BASED_RMI)
			objRegistry = getObjectFromStub();
		else
			objRegistry = getObjectFromRegistry();
		iRelFirstObject = objRegistry;

		// Send back the received message as acknowledgement
		outStream.writeObject(new MessageSimple(IoTCommCode.ACKNOWLEDGED));
		RuntimeOutput.print("IoTSlave: Getting a first object for IoTRelation!", BOOL_VERBOSE);

	}

	/**
	 * A private method to get an IoTRelation second object
	 *
	 * @return  void
	 */
	private void getIoTRelationSecondObject() throws IOException,
		ClassNotFoundException, RemoteException, NotBoundException, NoSuchMethodException,
		InstantiationException, IllegalAccessException, InvocationTargetException {
		Object objRegistry = null;
		if (CAPAB_BASED_RMI)
			objRegistry = getObjectFromStub();
		else
			objRegistry = getObjectFromRegistry();
		iRelSecondObject = objRegistry;

		// Now add the first and the second object into IoTRelation
		irelObject.put(iRelFirstObject, iRelSecondObject);
		RuntimeOutput.print("IoTSlave: This IoTRelation now has: " + irelObject.size() + " entry(s)", BOOL_VERBOSE);

		// Send back the received message as acknowledgement
		outStream.writeObject(new MessageSimple(IoTCommCode.ACKNOWLEDGED));
		RuntimeOutput.print("IoTSlave: Getting a second object for IoTRelation!", BOOL_VERBOSE);

	}

	/**
	 * A private method to reinitialize IoTSet field
	 *
	 * @return  void
	 */
	private void reinitializeIoTSetField() throws IOException,
		IllegalAccessException, NoSuchFieldException {

		// Reinitialize IoTSet field after getting all the objects
		iotsetObject = new IoTSet<Object>(isetObject.values());
		// Private fields need getDeclaredField(), while public fields use getField()
		Field fld = clsMain.getDeclaredField(strFieldName);
		boolean bAccess = fld.isAccessible();
		fld.setAccessible(true);
		fld.set(objMainCls, iotsetObject);
		fld.setAccessible(bAccess);
		RuntimeOutput.print("IoTSlave: Reinitializing field " + strFieldName, BOOL_VERBOSE);

		// Send back the received message as acknowledgement
		outStream.writeObject(new MessageSimple(IoTCommCode.ACKNOWLEDGED));
		RuntimeOutput.print("IoTSlave: Reinitializing IoTSet field!", BOOL_VERBOSE);

	}

	/**
	 * A private method to reinitialize IoTRelation field
	 *
	 * @return  void
	 */
	private void reinitializeIoTRelationField() throws IOException,
		IllegalAccessException, NoSuchFieldException {

		// Reinitialize IoTSet field after getting all the objects
		iotrelObject = new IoTRelation<Object,Object>(irelObject.relationMap(), irelObject.size());
		// Private fields need getDeclaredField(), while public fields use getField()
		Field fld = clsMain.getDeclaredField(strFieldName);
		boolean bAccess = fld.isAccessible();
		fld.setAccessible(true);
		fld.set(objMainCls, iotrelObject);
		fld.setAccessible(bAccess);
		RuntimeOutput.print("IoTSlave: Reinitializing field " + strFieldName, BOOL_VERBOSE);

		// Send back the received message as acknowledgement
		outStream.writeObject(new MessageSimple(IoTCommCode.ACKNOWLEDGED));
		RuntimeOutput.print("IoTSlave: Reinitializing IoTRelation field!", BOOL_VERBOSE);

	}

	/**
	 * A private method to get the device driver object's IoTSet
	 * <p>
	 * This is to handle device driver's IoTSet that contains IP addresses
	 *
	 * @return  void
	 */
	private void getDeviceIoTSetObject() throws IOException {

		// Translating into the actual Message class
		MessageGetDeviceObject sMessage = (MessageGetDeviceObject) sIoTMasterMsg;
		// Get IoTSet objects for IP address set on device driver/controller
		IoTDeviceAddress objDeviceAddress = new IoTDeviceAddress(sMessage.getHostAddress(),
			sMessage.getSourceDeviceDriverPort(),
			sMessage.getDestinationDeviceDriverPort(),
			sMessage.isSourcePortWildCard(),
			sMessage.isDestinationPortWildCard());
		RuntimeOutput.print("IoTSlave: Device address transferred: " + sMessage.getHostAddress(), BOOL_VERBOSE);
		isetObject.add(objDeviceAddress);
		RuntimeOutput.print("IoTSlave: This IoTSet now has: " + isetObject.size() + " entry(s)", BOOL_VERBOSE);

		// Send back the received message as acknowledgement
		outStream.writeObject(new MessageSimple(IoTCommCode.ACKNOWLEDGED));
		RuntimeOutput.print("IoTSlave: Getting an object for IoTSet!", BOOL_VERBOSE);

	}

	/**
	 * A private method to get the device driver object's IoTSet for IoTZigbeeAddress
	 * <p>
	 * This is to handle device driver's IoTSet that contains Zigbee addresses
	 *
	 * @return  void
	 */
	private void getZBDevIoTSetObject() throws IOException {

		// Translating into the actual Message class
		MessageGetSimpleDeviceObject sMessage = (MessageGetSimpleDeviceObject) sIoTMasterMsg;
		// Get IoTSet objects for IP address set on device driver/controller
		IoTZigbeeAddress objZBDevAddress = new IoTZigbeeAddress(sMessage.getHostAddress());
		RuntimeOutput.print("IoTSlave: Device address transferred: " + sMessage.getHostAddress(), BOOL_VERBOSE);
		isetObject.add(objZBDevAddress);
		RuntimeOutput.print("IoTSlave: This IoTSet now has: " + isetObject.size() + " entry(s)", BOOL_VERBOSE);

		// Send back the received message as acknowledgement
		outStream.writeObject(new MessageSimple(IoTCommCode.ACKNOWLEDGED));
		RuntimeOutput.print("IoTSlave: Getting an object for IoTSet!", BOOL_VERBOSE);

	}

	
	/**
	 * A private method to get IoTAddress objects for IoTSet
	 *
	 * @return  void
	 */
	private void getAddIoTSetObject() throws IOException {

		// Translating into the actual Message class
		MessageGetSimpleDeviceObject sMessage = (MessageGetSimpleDeviceObject) sIoTMasterMsg;
		// Get IoTSet objects for IP address set on device driver/controller
		IoTAddress objAddress = new IoTAddress(sMessage.getHostAddress());
		RuntimeOutput.print("IoTSlave: Address transferred: " + sMessage.getHostAddress(), BOOL_VERBOSE);
		isetObject.add(objAddress);
		RuntimeOutput.print("IoTSlave: This IoTSet now has: " + isetObject.size() + " entry(s)", BOOL_VERBOSE);
		// Send back the received message as acknowledgement
		outStream.writeObject(new MessageSimple(IoTCommCode.ACKNOWLEDGED));
		RuntimeOutput.print("IoTSlave: Getting an object for IoTSet!", BOOL_VERBOSE);

	}
	
	/**
	 * A private method to invoke init() method in the controller object
	 *
	 * @return  void
	 */
	private void invokeInitMethod() throws IOException {

		new Thread() {
			public void run() {
				try {
					Class<?> noparams[] = {};
					Method method = clsMain.getDeclaredMethod("init", noparams);
					method.invoke(objMainCls);
				} catch (NoSuchMethodException  |
						 IllegalAccessException |
						 InvocationTargetException ex) {
					System.out.println("IoTSlave: Exception: "
						 + ex.getMessage());
					ex.printStackTrace();
				}
			}
		}.start();

		// Start a new thread to invoke the init function
		RuntimeOutput.print("IoTSlave: Invoke init method! Job done!", BOOL_VERBOSE);

		// Send back the received message as acknowledgement
		outStream.writeObject(new MessageSimple(IoTCommCode.ACKNOWLEDGED));

	}

	/**
	 * A public method to do communication with IoTMaster
	 *
	 * @params  iIndex  Integer index
	 * @return  void
	 */
	public void commIoTMaster() {

		try {

			// Loop, receive and process commands from IoTMaster
			socket = new Socket(sIoTMasterHostAdd, iComPort);
			outStream = new ObjectOutputStream(socket.getOutputStream());
			inStream = new ObjectInputStream(socket.getInputStream());

			LOOP:
			while(true) {
				// Get the first payload
				RuntimeOutput.print("IoTSlave: Slave waiting...", BOOL_VERBOSE);
				sIoTMasterMsg = (Message) inStream.readObject();

				// Check payload message from IoTMaster and make a decision
				switch (sIoTMasterMsg.getMessage()) {

				case CREATE_OBJECT:
					createObject();
					break;

				case TRANSFER_FILE:
					transferFile();
					break;

				case CREATE_MAIN_OBJECT:
					createMainObject();
					break;

				case CREATE_NEW_IOTSET:
					createNewIoTSet();
					break;

				case CREATE_NEW_IOTRELATION:
					createNewIoTRelation();
					break;

				case GET_IOTSET_OBJECT:
					getIoTSetObject();
					break;

				case GET_IOTRELATION_FIRST_OBJECT:
					getIoTRelationFirstObject();
					break;

				case GET_IOTRELATION_SECOND_OBJECT:
					getIoTRelationSecondObject();
					break;

				case REINITIALIZE_IOTSET_FIELD:
					reinitializeIoTSetField();
					break;

				case REINITIALIZE_IOTRELATION_FIELD:
					reinitializeIoTRelationField();
					break;

				case GET_DEVICE_IOTSET_OBJECT:
					getDeviceIoTSetObject();
					break;

				case GET_ZB_DEV_IOTSET_OBJECT:
					getZBDevIoTSetObject();
					break;

				case GET_ADD_IOTSET_OBJECT:
					getAddIoTSetObject();
					break;

				case INVOKE_INIT_METHOD:
					invokeInitMethod();
					break;

				case END_SESSION:
					// END of session
					break LOOP;

				default:
					break;
				}
			}
			RuntimeOutput.print("IoTSlave: Session ends!", BOOL_VERBOSE);

			// Closing streams and end session
			outStream.close();
			inStream.close();
			socket.close();
			RuntimeOutput.print("IoTSlave: Closing!", BOOL_VERBOSE);
			// We have to continuously loop because we are preserving our stubs and skeletons
			//while(true) { }

		} catch (IOException               |
				 ClassNotFoundException    |
				 NoSuchMethodException     |
				 InstantiationException    |
				 AlreadyBoundException     |
				 IllegalAccessException    |
				 InvocationTargetException |
				 NotBoundException         |
				 NoSuchFieldException ex) {
			System.out.println("IoTSlave: Exception: "
				 + ex.getMessage());
			ex.printStackTrace();
		}
	}

	public static void main(String args[]) {
		IoTSlave iotSlave = new IoTSlave(args);
		iotSlave.parseIoTSlaveConfigFile();
		iotSlave.commIoTMaster();
	}
}
