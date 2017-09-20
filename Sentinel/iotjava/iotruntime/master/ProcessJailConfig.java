package iotruntime.master;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Class ProcessJailConfig is a class that configures the compute
 *  nodes in our network with the relevant process jail policies;
 *  <p>
 *  We use Tomoyo 2.5 as a Mandatory Access Control (MAC) that is
 *  simple, easy to maintain, and lightweight (suitable for embedded
 *  devices).
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     2.0
 * @since       2017-04-07
 */
public final class ProcessJailConfig {

	/**
	 * ProcessJailConfig constants
	 */
	private static final String STR_SSH_USERNAME_ROUTER = "root";
	private static final String STR_SSH_USERNAME_HOST   = "iotuser";
	private static final String STR_TCP_PROTOCOL = "tcp";
	private static final String STR_UDP_PROTOCOL = "udp";
	private static final String STR_TCPGW_PROTOCOL = "tcpgw";
	private static final String STR_NO_PROTOCOL = "nopro";

	private static final String STR_MAC_POLICY_EXT 		= ".tomoyo.pol";
	private static final String STR_OBJECT_NAME   		= "<object-name>";
	private static final String STR_OBJECT_CLASS_NAME	= "<object-class-name>";
	private static final String STR_MASTER_IP_ADDRESS	= "<master-ip-address>";
	private static final String STR_MASTER_COM_PORT		= "<master-com-port>";
	private static final String STR_RMI_REG_PORT  		= "<rmi-reg-port>";
	private static final String STR_RMI_STUB_PORT  		= "<rmi-stub-port>";
	private static final String STR_DEV_IP_ADDRESS		= "<dev-ip-address>";
	private static final String STR_DEV_COM_PORT		= "<dev-com-port>";
	private static final String STR_DEV_PORT			= "<dev-port>";


	/**
	 * ProcessJailConfig properties
	 */
	private Map<String, PrintWriter> mapHostToFile;
	private Map<String, String> mapMACtoIPAdd;


	/**
	 * Constructor
	 */
	public ProcessJailConfig() {
		// This maps hostname to file PrintWriter
		mapHostToFile = new HashMap<String, PrintWriter>();
		mapMACtoIPAdd = null;
	}


	/**
	 * renewPrintWriter() renews the mapHostToFile object that lists all PrintWriters
	 *
	 * @return  void
	 */
	public void renewPrintWriter() {

		mapHostToFile = new HashMap<String, PrintWriter>();
	}


	/**
	 * getPrintWriter() gets the right PrintWriter object to print policies to the right file
	 *
	 * @param   strConfigHost String hostname to be configured
	 * @return  PrintWriter
	 */
	private PrintWriter getPrintWriter(String strConfigHost) {

		// Return object if existing
		if (mapHostToFile.containsKey(strConfigHost)) {
			return mapHostToFile.get(strConfigHost);
		} else {
		// Simply create a new one if it doesn't exist
			FileWriter fw = null;
			try {
				fw = new FileWriter(strConfigHost + STR_MAC_POLICY_EXT);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			PrintWriter pwConfig = new PrintWriter(new BufferedWriter(fw));
			mapHostToFile.put(strConfigHost, pwConfig);
			return pwConfig;
		}
	}
	

	/**
	 * close() closes all PrintWriter objects
	 *
	 * @return  void
	 */
	public void close() {

		for(PrintWriter pwConfig: mapHostToFile.values()) {
			pwConfig.close();
		}
	}


	/**
	 * sendMACPolicies() deploys policies on MAC implementation for process jailing
	 *
	 * @param   strConfigHost String hostname to be configured
	 * @return  void
	 */
	public void sendMACPolicies(String strConfigHost) {

		String strCmdSend = "scp " + strConfigHost + STR_MAC_POLICY_EXT + " " + 
			STR_SSH_USERNAME_HOST + "@" + strConfigHost + ":~;";
		System.out.println(strCmdSend);
		runCommand(strCmdSend);
		String strCmdDeploy = "ssh " + STR_SSH_USERNAME_HOST + "@" + strConfigHost +
			" sudo tomoyo-loadpolicy -df < ~/" + strConfigHost + STR_MAC_POLICY_EXT + "; rm ~/" + strConfigHost + 
			STR_MAC_POLICY_EXT + ";";
		System.out.println(strCmdDeploy);
		runCommand(strCmdDeploy);
	}


	/**
	 * deployPolicies() method configures the policies
	 *
	 * @param   strCommand 	String that contains command line
	 * @return  void
	 */
	private void deployPolicies(String strCommand) {

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
	 * setAddressListObject() method sets the map for IP and MAC addresses
	 * <p>
	 * This method gets the mapping from RouterConfig
	 */
	public void setAddressListObject(Map<String, String> _mapMACtoIPAdd) {

		mapMACtoIPAdd = _mapMACtoIPAdd;
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
	 * getAddressList() method gets list of IP addresses
	 * <p>
	 * This method sends an inquiry to the router to look for
	 * the list of DHCP leased addresses and their mapping to MAC
	 * addresses
	 *
	 * @param  strRouterAddress  String that contains address of router
	 */
	public void getAddressList(String strRouterAddress) {

		//HashMap<String,String> hmMACToIPAdd = new HashMap<String,String>();
		try {
			// We can replace "cat /tmp/dhcp.leases" with "cat /proc/net/arp"
			String cmd = "ssh " + STR_SSH_USERNAME_ROUTER + "@" + strRouterAddress +
                         " cat /tmp/dhcp.leases";
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(cmd);

			InputStream inStream = process.getInputStream();
			InputStreamReader isReader = new InputStreamReader(inStream);
			BufferedReader bReader = new BufferedReader(isReader);
			String strRead = null;
			while((strRead = bReader.readLine()) != null){
				String[] str = strRead.split(" ");
				mapMACtoIPAdd.put(str[1], str[2]);
			}
		} catch (IOException ex) {
			System.out.println("RouterConfig: IOException: " + ex.getMessage());
			ex.printStackTrace();
		}
	}


	/**
	 * getIPFromMACAddress() method gets IP from MAC address
	 *
	 * @return  String  String that contains IP address from the MAC-IP mapping
	 */	 
	public String getIPFromMACAddress(String strMACAddress) {

		String strIPAddress = mapMACtoIPAdd.get(strMACAddress);
		if (strIPAddress == null) {
			throw new Error("RouterConfig: MAC address " + strMACAddress + 
				" not found on the list! Please check if device is present in /tmp/dhcp.leases!");
		}
		return strIPAddress;
	}


	/**
	 * readFile() read the entire file and return a string
	 *
	 * @return  String  String that contains the content of the file
	 */	 
	public String readFile(String filePath) {

		String retStr = null;
		try {
			retStr = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return retStr;
	}


	/**
	 * configureProcessJailDeviceDriverPolicies() method configures the main MAC policies
	 * <p>
	 * This method configures the main policies between controller and device driver
	 *
	 * @param   strConfigHost 	 		String hostname to be configured
	 * @param   strObjectName 	 		String object name
	 * @param   strObjectClassName 		String object class name
	 * @param   strFileName 			String policy file path and name
	 * @param   strMasterIPAddress		String master IP address
	 * @param   iComPort				Integer communication port (controller-driver)
	 * @param   iRMIRegPort				Integer RMI registry port
	 * @param   iRMIStubPort			Integer RMI stub port
	 * @return  void
	 */
	public void configureProcessJailDeviceDriverPolicies(String strConfigHost, String strObjectName, String strObjectClassName, 
			String strFileName, String strMasterIPAddress, int iComPort, int iRMIRegPort, int iRMIStubPort) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		String strPolicyList = readFile(strFileName);
		// Replace the strings with the actual values
		String strNewPolicyList = strPolicyList.replace(STR_OBJECT_NAME, strObjectName).
			replace(STR_OBJECT_CLASS_NAME, strObjectClassName).
			replace(STR_MASTER_IP_ADDRESS, strMasterIPAddress).
			replace(STR_MASTER_COM_PORT, String.valueOf(iComPort));
			//replace(STR_RMI_REG_PORT, String.valueOf(iRMIRegPort)).
			//replace(STR_RMI_STUB_PORT, String.valueOf(iRMIStubPort));
		pwConfig.println("\n");
		pwConfig.print(strNewPolicyList);
		pwConfig.println("network inet stream bind/listen :: " + iRMIRegPort);
		pwConfig.println("network inet stream bind/listen :: " + iRMIStubPort);
	}


	/**
	 * configureProcessJailDevicePolicies() method configures the device MAC policies
	 * <p>
	 * This method configures the device policies between device driver and device
	 *
	 * @param   strConfigHost 	 		String hostname to be configured
	 * @param   strProtocol 			String protocol name
	 * @param	iDeviceComPort			Integer device communication port
	 * @param   strDeviceIPAddress		String device IP address
	 * @param   iDevicePort				Integer device port
	 * @return  void
	 */
	public void configureProcessJailDevicePolicies(String strConfigHost, String strProtocol,
			int iDeviceComPort, String strDeviceIPAddress, int iDevicePort) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		if (strProtocol.equals(STR_TCP_PROTOCOL)) {
			pwConfig.println("network inet stream connect ::ffff:" + strDeviceIPAddress + " " + String.valueOf(iDevicePort));
		} else {
			pwConfig.println("network inet dgram bind :: " + String.valueOf(iDeviceComPort));
			pwConfig.println("network inet dgram send ::ffff:" + strDeviceIPAddress + " " + String.valueOf(iDevicePort));
		}
	}


	/**
	 * configureProcessJailDevicePolicies() method configures the device MAC policies
	 * <p>
	 * This method configures the device policies between device driver and device
	 *
	 * @param   strConfigHost 	 		String hostname to be configured
	 * @param   strRouterAddress 		String router address
	 * @param   iPort					Integer port
	 * @return  void
	 */
	public void configureProcessJailGWDevicePolicies(String strConfigHost, String strRouterAddress, int iPort) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		pwConfig.println("file read /home/iotuser/iot2/iotjava/iotruntime/\\*.jks");
		pwConfig.println("file read /etc/resolv.conf");
		pwConfig.println("file read /etc/hosts");
		pwConfig.println("network inet dgram send " + strRouterAddress + " " + String.valueOf(iPort));
	}


	/**
	 * configureProcessJailDeviceDriverInetAddressPolicies() method configures the device MAC policies
	 * <p>
	 *
	 * @param   strConfigHost 	String hostname to be configured
	 * @param   strAddress		String device IP address
	 * @return  void
	 */
	public void configureProcessJailInetAddressPolicies(String strConfigHost, String strAddress) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		//System.out.println("\n\nDEBUG: Writing the config host address setup!!!\n\n");
		pwConfig.println("network inet stream connect ::ffff:" + strAddress + " " + String.valueOf(80));	// HTTP access for this address
	}


	/**
	 * configureProcessJailControllerPolicies() method configures the main MAC policies for controller
	 *
	 * @param   strControllerName 		String controller name to be configured
	 * @param   strFileName 			String policy file path and name
	 * @param   strMasterIPAddress		String master IP address
	 * @param   iComPort				Integer communication port (controller-driver)
	 * @return  void
	 */
	public void configureProcessJailControllerPolicies(String strControllerName, String strFileName, 
			String strMasterIPAddress, int iComPort) {

		PrintWriter pwConfig = getPrintWriter(strControllerName);
		String strPolicyList = readFile(strFileName);
		// Replace the strings with the actual values
		String strNewPolicyList = strPolicyList.replace(STR_OBJECT_NAME, strControllerName).
			replace(STR_OBJECT_CLASS_NAME, strControllerName).
			replace(STR_MASTER_IP_ADDRESS, strMasterIPAddress).
			replace(STR_MASTER_COM_PORT, String.valueOf(iComPort));
		pwConfig.println("\n");
		pwConfig.print(strNewPolicyList);
	}


	/**
	 * configureProcessJailContRMIPolicies() method configures the MAC policies for RMI ports of controller
	 *
	 * @param   strControllerName 		String controller name to be configured
	 * @param   strFileName 			String policy file path and name
	 * @param   strMasterIPAddress		String master IP address
	 * @param   iComPort				Integer communication port (controller-driver)
	 * @return  void
	 */
	public void configureProcessJailContRMIPolicies(String strControllerName, String strDeviceDriverIPAddress, 
			int iRMIRegPort, int iRMIStubPort) {

		PrintWriter pwConfig = getPrintWriter(strControllerName);
		// Replace the strings with the actual values
		pwConfig.println("network inet stream connect ::ffff:" + strDeviceDriverIPAddress + " " + String.valueOf(iRMIRegPort));
		pwConfig.println("network inet stream connect ::ffff:" + strDeviceDriverIPAddress + " " + String.valueOf(iRMIStubPort));
	}


	/**
	 * combineControllerMACPolicies() method combines the controller MAC policies into the right host policy file
	 *
	 * @param   strConfigHost 	 		String hostname to be configured
	 * @param   strFileName 			String policy file path and name
	 * @return  void
	 */
	public void combineControllerMACPolicies(String strConfigHost, String strObjectControllerName, String strFileName) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		PrintWriter pwCont = getPrintWriter(strObjectControllerName);
		pwCont.close();
		String strPolicyList = readFile(strFileName);
		pwConfig.println(strPolicyList);
		runCommand("rm -rf " + strFileName);
	}
}


