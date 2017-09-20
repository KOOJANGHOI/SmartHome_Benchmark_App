import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import static java.lang.Math.toIntExact;

public class IoTSlave {

	private ServerSocket serverSocket;
	private Socket socket;
	private BufferedInputStream input;
	private BufferedOutputStream output;

	private static final String STR_LOCALHOST = "localhost";
	private static final String STR_IOTSLAVE_CPP = "./IoTSlave.o";
	private static final String STR_IOTSLAVE_PATH = "~/tmp/iot2/iotjava/iotruntime/cpp/iotslave/";

	//private static final String STR_LOG_FILE_PATH = "./";
	private static int INT_SIZE = 4;	// send length in the size of integer (4 bytes)


	public IoTSlave() {

		serverSocket = null;
		socket = null;
		input = null;
		output = null;
	}


	/**
	 * Prepare server socket connection with C++ IoTSlave
	 */
	public void setServerSocketCpp(int iPort) {

  		try {
			serverSocket = new ServerSocket(iPort);
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
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
	}


	private void sendFile(String sFilePath, String sFileName) throws IOException {

		sendCommCode(IoTCommCode.TRANSFER_FILE);
		// Send file name
		sendString(sFileName); recvAck();
		File file = new File(sFilePath + sFileName);
		int iFileLen = toIntExact(file.length());
		System.out.println("IoTSlave: Sending file " + sFileName + " with length " + iFileLen + " bytes...");
		// Send file length
		sendInteger(iFileLen); recvAck();
		byte[] bytFile = new byte[iFileLen];
		InputStream inFileStream = new FileInputStream(file);

		OutputStream outFileStream = socket.getOutputStream();
		int iCount;
		while ((iCount = inFileStream.read(bytFile)) > 0) {
			outFileStream.write(bytFile, 0, iCount);
		}
		System.out.println("IoTSlave: File sent!");
		recvAck();
	}

	/**
	 * sendInteger() sends an integer in bytes
	 */
	public void sendInteger(int intSend) throws IOException {

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
	public int recvInteger() throws IOException {

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
	public String recvString() throws IOException {

		int strLen = recvInteger();
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
	public void sendString(String strSend) throws IOException {

		// Transform String into bytes
		byte[] strSendBytes = strSend.getBytes();
		int strLen = strSend.length();
		// Send the string length first
		sendInteger(strLen);
		// Send the byte array
		output.write(strSendBytes, 0, strLen);
		output.flush();
	}


	/**
	 * Establish connection with C++ IoTSlave
	 */
	public void connectCpp() throws IOException	{

		socket = serverSocket.accept();
		input = new BufferedInputStream(socket.getInputStream());
		output = new BufferedOutputStream(socket.getOutputStream());
	}


	/**
	 * Construct a SSH command to run C++ program
	 */
	public static String constructCommand(String serverAddress, int serverPort, String strObjName) {

		String strCommand = "ssh rtrimana@localhost cd " + STR_IOTSLAVE_PATH + "; " +
				STR_IOTSLAVE_CPP + " " + serverAddress + " " + serverPort + " " + strObjName;
		return strCommand;
	}


	/**
	 * Create a new thread to start a new C++ process
	 */
	public static void createCppThread(String strCmd) {

		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					Runtime runtime = Runtime.getRuntime();
					Process process = runtime.exec(strCmd);
				} catch(IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		thread.start();
		//RuntimeOutput.print("IoTSlave: Executing: " + strCmd, BOOL_VERBOSE);
		System.out.println("IoTSlave: Executing: " + strCmd);
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
	public boolean recvAck() throws IOException {

		int intAck = recvInteger();
		IoTCommCode codeAck = getCode(intAck);
		if (codeAck == IoTCommCode.ACKNOWLEDGED)
			return true;
		return false;

	}


	/**
	 * Send END
	 */
	public void sendEndTransfer() throws IOException {

		int endCode = IoTCommCode.END_TRANSFER.ordinal();
		sendInteger(endCode);
	}


	/**
	 * Send communication code to C++
	 */
	public void sendCommCode(IoTCommCode inpCommCode) throws IOException {


		IoTCommCode commCode = inpCommCode;
		int intCode = commCode.ordinal();
		sendInteger(intCode); recvAck();
	}


	/**
	 * Create a main controller object for C++
	 */
	public void createMainObjectCpp() throws IOException {

		sendCommCode(IoTCommCode.CREATE_MAIN_OBJECT);
		String strMainObjName = "Lifxtest";
		sendString(strMainObjName); recvAck();
		System.out.println("IoTSlave: Create a main object: " + strMainObjName);
	}


	/**
	 * Create a driver object for C++
	 */
	public void createObjectCpp() throws IOException {

		sendCommCode(IoTCommCode.CREATE_OBJECT);
		String strDrvObjName = "LifxLightBulbLB2";
		String strDrvObjClsName = "LifxLightBulb";
		String strDrvObjIntfaceClsName = "LightBulb";
		String strDrvObjSkelClsName = "LightBulb_Skeleton";
		int iRegPort = 30313;
		int iStubPort = 55179;
		// TODO: On the actual slave we need to do conversion back to string before we send everything to C++ IoTSlave
		// TODO: Make it as array of string
		//String[] arrCppArgs = { "D073D50241DA0000" };
		String[] arrCppArgs = { "D073D5128E300000" };
		String[] arrCppArgClasses = { "string" };
		System.out.println("IoTSlave: Send request to create a driver object... ");
		System.out.println("IoTSlave: Driver object name: " + strDrvObjName);
		sendString(strDrvObjName); recvAck();
		System.out.println("IoTSlave: Driver object class name: " + strDrvObjClsName);
		sendString(strDrvObjClsName); recvAck();
		System.out.println("IoTSlave: Driver object interface name: " + strDrvObjIntfaceClsName);
		sendString(strDrvObjIntfaceClsName); recvAck();
		System.out.println("IoTSlave: Driver object skeleton class name: " + strDrvObjSkelClsName);
		sendString(strDrvObjSkelClsName); recvAck();
		System.out.println("IoTSlave: Driver object registry port: " + iRegPort);
		sendInteger(iRegPort); recvAck();
		System.out.println("IoTSlave: Driver object stub port: " + iStubPort);
		sendInteger(iStubPort); recvAck();
		int numOfArgs = arrCppArgs.length;
		System.out.println("IoTSlave: Send constructor arguments! Number of arguments: " + numOfArgs);
		sendInteger(numOfArgs); recvAck();
		for(String str : arrCppArgs) {
			sendString(str); recvAck();
		}
		System.out.println("IoTSlave: Send constructor argument classes!");
		for(String str : arrCppArgClasses) {
			sendString(str); recvAck();
		}
	}


	/**
	 * Create new IoTSet for C++
	 */
	//public void createNewIoTSetCpp() throws IOException {
	public void createNewIoTSetCpp(String strObjFieldName) throws IOException {

		sendCommCode(IoTCommCode.CREATE_NEW_IOTSET);
		System.out.println("IoTSlave: Creating new IoTSet...");
		//String strObjFieldName = "lb_addresses";
		System.out.println("IoTSlave: Send object field name: " + strObjFieldName);
		sendString(strObjFieldName); recvAck();
	}


	/**
	 * Get a IoTDeviceAddress object for C++
	 */
	public void getDeviceIoTSetObjectCpp() throws IOException {

		sendCommCode(IoTCommCode.GET_DEVICE_IOTSET_OBJECT);
		System.out.println("IoTSlave: Getting IoTDeviceAddress...");
		//String strHostAddress = "192.168.2.232";
		String strHostAddress = "192.168.2.126";
		sendString(strHostAddress); recvAck();
		int iSourcePort = 43583;
		sendInteger(iSourcePort); recvAck();
		int iDestPort = 56700;
		sendInteger(iDestPort); recvAck();
		boolean bSourceWildCard = false;
		int iSourceWildCard = (bSourceWildCard ? 1 : 0);
		sendInteger(iSourceWildCard); recvAck();
		boolean bDestWildCard = false;
		int iDestWildCard = (bDestWildCard ? 1 : 0);
		sendInteger(iDestWildCard); recvAck();
		System.out.println("IoTSlave: Send host address: " + strHostAddress);
	}


	/**
	 * Get a IoTSet content object for C++
	 */
	public void getIoTSetObjectCpp() throws IOException {

		sendCommCode(IoTCommCode.GET_IOTSET_OBJECT);
		System.out.println("IoTSlave: Getting IoTSet object content...");
		String strHostAddress = "localhost";
		String strDrvObjName = "LifxLightBulbLB2";
		String strDrvObjClsName = "LifxLightBulb";
		String strDrvObjIntfaceClsName = "LightBulb";
		String strDrvObjStubClsName = "LightBulbTest_Stub";	// Send a complete name with "_Stub"
		int iRegPort = 30313;
		int iStubPort = 55179;
		int[] callbackPorts = { 58551 };
		// Send info
		System.out.println("IoTSlave: Send host address: " + strHostAddress);
		sendString(strHostAddress); recvAck();
		System.out.println("IoTSlave: Driver object name: " + strDrvObjName);
		sendString(strDrvObjName); recvAck();
		System.out.println("IoTSlave: Driver object class name: " + strDrvObjClsName);
		sendString(strDrvObjClsName); recvAck();
		System.out.println("IoTSlave: Driver object interface name: " + strDrvObjIntfaceClsName);
		sendString(strDrvObjIntfaceClsName); recvAck();
		System.out.println("IoTSlave: Driver object skeleton class name: " + strDrvObjStubClsName);
		sendString(strDrvObjStubClsName); recvAck();
		System.out.println("IoTSlave: Driver object registry port: " + iRegPort);
		sendInteger(iRegPort); recvAck();
		System.out.println("IoTSlave: Driver object stub port: " + iStubPort);
		sendInteger(iStubPort); recvAck();
		sendInteger(callbackPorts.length); recvAck();
		for(int i : callbackPorts) {
			sendInteger(i); recvAck();
		}

	}


	/**
	 * Reinitialize IoTSet field for C++
	 */
	private void reinitializeIoTSetFieldCpp() throws IOException {

		System.out.println("IoTSlave: About to Reinitialize IoTSet field!");
		sendCommCode(IoTCommCode.REINITIALIZE_IOTSET_FIELD);
		System.out.println("IoTSlave: Reinitialize IoTSet field!");
	}


	/**
	 * Invoke init() for C++
	 */
	private void invokeInitMethodCpp() throws IOException {

		sendCommCode(IoTCommCode.INVOKE_INIT_METHOD);
		System.out.println("IoTSlave: Invoke init method!");
	}


	/**
	 * End session for C++
	 */
	public void endSessionCpp() throws IOException {

		// Send message to end session
		IoTCommCode endSessionCode = IoTCommCode.END_SESSION;
		int intCode = endSessionCode.ordinal();
		sendInteger(intCode);
		//RuntimeOutput.print("IoTSlave: Send request to create a main object: " + strObjName, BOOL_VERBOSE);
		System.out.println("IoTSlave: Send request to end session!");
	}


	public static void main(String[] args) throws IOException, InterruptedException {

		/*int iPort = 12345;
		IoTSlave iotSlave = new IoTSlave();
		iotSlave.setServerSocketCpp(iPort);
		iotSlave.connectCpp();
		System.out.println("Connection established with client!");
		iotSlave.sendInteger(1234);
		System.out.println("Integer sent!");
		System.out.println("Integer received: " + iotSlave.recvInteger());
		String strRecv = iotSlave.recvString();
		System.out.println("Received string: " + strRecv);
		strRecv = strRecv + " - ACKNOWLEDGED!";
		System.out.println("Sending back string: " + strRecv);
		iotSlave.sendString(strRecv);*/

		// =========================================
		// Create IoTSlave for controller object!
		int iPortMain =12346;
		String strAddressMain = "localhost";
		String strObjNameMain = "Lifxtest";
		IoTSlave iotSlaveMain = new IoTSlave();
		iotSlaveMain.setServerSocketCpp(iPortMain);
		// Run thread to spawn C++ IoTSlave
		String strCmdMain = iotSlaveMain.constructCommand(strAddressMain, iPortMain, strObjNameMain);
		iotSlaveMain.createCppThread(strCmdMain);
		iotSlaveMain.connectCpp();
		System.out.println("IoTSlave: Connection established with main!");
		// First contact with C++ IoTSlave
		System.out.println("IoTSlave: IoTSlave.o main is ready: " + iotSlaveMain.recvAck());
		//iotSlaveMain.sendFile("../", "Lifxtest.so");
		//iotSlaveMain.sendFile("../", "LightBulbTest_Stub.so");
		//iotSlaveMain.sendFile("../", "Lifxtest.zip");
		//iotSlaveMain.sendFile("../resources/", "Lifxtest.jar");
		//iotSlaveMain.sendFile("../", "unzip.zip");
		

		// =========================================
		// Create IoTSlave for driver object!
		int iPort =12345;
		String strAddress = "localhost";
		String strObjName = "LifxLightBulbLB2";
		IoTSlave iotSlave = new IoTSlave();
		iotSlave.setServerSocketCpp(iPort);
		// Run thread to spawn C++ IoTSlave
		String strCmd = IoTSlave.constructCommand(strAddress, iPort, strObjName);
		IoTSlave.createCppThread(strCmd);
		iotSlave.connectCpp();
		//RuntimeOutput.print("IoTSlave: Connection established!", BOOL_VERBOSE);
		System.out.println("IoTSlave: Connection established!");
		// First contact with C++ IoTSlave
		System.out.println("IoTSlave: IoTSlave.o is ready: " + iotSlave.recvAck());
		//iotSlave.sendFile("../", "LifxLightBulb.so");
		//iotSlave.sendFile("../", "LightBulb_Skeleton.so");
		//iotSlave.sendFile("../", "LifxLightBulb.zip");
		//iotSlave.sendFile("../", "unzip2.zip");
		iotSlave.createObjectCpp();
		//iotSlave.createNewIoTSetCpp();
		iotSlave.createNewIoTSetCpp("lb_addresses");
		iotSlave.getDeviceIoTSetObjectCpp();
		iotSlave.reinitializeIoTSetFieldCpp();
		//iotSlave.endSessionCpp();

		// =========================================
		// Continue with main object
		iotSlaveMain.createMainObjectCpp();
		iotSlaveMain.createNewIoTSetCpp("lifx_light_bulb");
		iotSlaveMain.getIoTSetObjectCpp();
		iotSlaveMain.reinitializeIoTSetFieldCpp();
		iotSlaveMain.invokeInitMethodCpp();
		iotSlaveMain.endSessionCpp();

		// Send message to create a main object
		/*commCode = IoTCommCode.CREATE_MAIN_OBJECT;
		intCode = commCode.ordinal();
		iotSlave.sendInteger(intCode);
		//RuntimeOutput.print("IoTSlave: Send request to create a main object: " + strObjName, BOOL_VERBOSE);
		System.out.println("IoTSlave: Send request to create a main object: " + strObjName);
		//RuntimeOutput.print("IoTSlave: IoTSlave.o is ready: " + strAck, BOOL_VERBOSE);
		System.out.println("IoTSlave: IoTSlave.o is ready: " + strAck);*/

		//Thread.sleep(1000);

	}
}
