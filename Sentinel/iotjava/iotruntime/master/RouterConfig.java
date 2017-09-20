package iotruntime.master;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Class RouterConfig is a class that configures the router
 *  in our compute node network with the relevant netfilter
 *  policies;
 *  it uses ssh to contact the router and writes policy into it
 *  <p>
 *  To make the implementation faster, we use "iptables-restore"
 *  that doesn't require "iptables" command to be invoked many
 *  times - each invocation of "iptables" will load the existing
 *  table from the kernel space before appending the new rule.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     2.0
 * @since       2016-06-21
 */
public final class RouterConfig {

	/**
	 * RouterConfig constants
	 */
	private static final String STR_SSH_USERNAME_ROUTER = "root";
	private static final String STR_SSH_USERNAME_HOST   = "iotuser";
	private static final String STR_POLICY_FILE_EXT   	= ".policy";

	/**
	 * RouterConfig properties
	 */
	private Map<String, PrintWriter> mapHostToFile;
	private Map<String, String> mapMACtoIPAdd;

	/**
	 * Constructor
	 */
	public RouterConfig() {
		// This maps hostname to file PrintWriter
		mapHostToFile = null;
		mapMACtoIPAdd = new HashMap<String, String>();
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
				fw = new FileWriter(strConfigHost + STR_POLICY_FILE_EXT);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			PrintWriter pwConfig = new PrintWriter(new BufferedWriter(fw));
			pwConfig.println("*filter");	// Print header for iptables-restore
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
			pwConfig.println("COMMIT");		// Add "COMMIT" statement to end the list for iptables-restore
			pwConfig.close();
		}
	}

	/**
	 * sendRouterPolicies() deploys policies on router
	 *
	 * @param   strConfigHost String hostname to be configured
	 * @return  void
	 */
	public void sendRouterPolicies(String strConfigHost) {

		String strCmdSend = "scp " + strConfigHost + STR_POLICY_FILE_EXT + " " + 
			STR_SSH_USERNAME_ROUTER + "@" + strConfigHost + ":~;";
		//System.out.println(strCmdSend);
		deployPolicies(strCmdSend);
		String strCmdDeploy = "ssh " + STR_SSH_USERNAME_ROUTER + "@" + strConfigHost +
			" iptables-restore < ~/" + strConfigHost + STR_POLICY_FILE_EXT + "; rm ~/" + strConfigHost + 
			STR_POLICY_FILE_EXT + "; ";// + 
			// TODO: delete these later when we apply tight initial conditions (reject everything but SSH commands)
			//"iptables -F startup_filter_tcp; iptables -F startup_filter_udp; " +
			//"iptables -t filter -D FORWARD -j startup_filter_tcp; iptables -t filter -D FORWARD -j startup_filter_udp;";
		//System.out.println(strCmdDeploy);
		deployPolicies(strCmdDeploy);
	}
	
	/**
	 * sendHostPolicies() deploys policies on host
	 *
	 * @param   strConfigHost String hostname to be configured
	 * @return  void
	 */
	public void sendHostPolicies(String strConfigHost) {

		String strCmdSend = "scp " + strConfigHost + STR_POLICY_FILE_EXT + " " + 
			STR_SSH_USERNAME_HOST + "@" + strConfigHost + ":~;";
		//System.out.println(strCmdSend);
		deployPolicies(strCmdSend);
		String strCmdDeploy = "ssh " + STR_SSH_USERNAME_HOST + "@" + strConfigHost +
			" sudo iptables-restore < ~/" + strConfigHost + STR_POLICY_FILE_EXT + "; rm ~/" + strConfigHost + 
			STR_POLICY_FILE_EXT + ";";
		//System.out.println(strCmdDeploy);
		deployPolicies(strCmdDeploy);
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
	 * getAddressListObject() method returns the map from this class
	 * <p>
	 * This method is useful for MAC policy class so that it doesn't have
	 * to query the router again
	 */
	public Map<String, String> getAddressListObject() {

		return mapMACtoIPAdd;
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
			throw new Error("RouterConfig: MAC address " + strMACAddress + " not found on the list! Please check if device is present in /tmp/dhcp.leases!");
		}
		return strIPAddress;
	}

	/**
	 * configureRouterMainPolicies() method configures the router
	 * <p>
	 * This method configures the router's main policies
	 * This method creates a command line using 'ssh' and 'iptables'
	 * to access the router and create Netfilter statements
	 *
	 * @param   strConfigHost 	 String hostname to be configured
	 * @param   strFirstHost     String first host
	 * @param   strSecondHost    String second host
	 * @param   strProtocol      String protocol TCP/UDP
	 * @param   iSrcPort         Integer source port number
	 * @param   iDstPort         Integer destination port number
	 * @return  void
	 */
	public void configureRouterMainPolicies(String strConfigHost, String strFirstHost,
		String strSecondHost, String strProtocol, int iSrcPort, int iDstPort) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strFirstHost + " -d " + 
			strSecondHost + " -p " + strProtocol + " --dport " + iDstPort);
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strFirstHost + " -d " + 
			strSecondHost + " -p " + strProtocol + " --sport " + iSrcPort);
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strSecondHost + " -d " + 
			strFirstHost + " -p " + strProtocol + " --sport " + iDstPort);
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strSecondHost + " -d " + 
			strFirstHost + " -p " + strProtocol + " --dport " + iSrcPort);
	}

	/**
	 * configureRouterMainPolicies() method configures the router
	 * <p>
	 * This method configures the router's main policies
	 * This method creates a command line using 'ssh' and 'iptables'
	 * to access the router and create Netfilter statements
	 *
	 * @param   strConfigHost 	 String hostname to be configured
	 * @param   strFirstHost     String first host
	 * @param   strSecondHost    String second host
	 * @param   strProtocol      String protocol TCP/UDP
	 * @param   iPort            Integer port number
	 * @return  void
	 */
	public void configureRouterMainPolicies(String strConfigHost, String strFirstHost,
		String strSecondHost, String strProtocol, int iPort) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p " + strProtocol + " --dport " + iPort);
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p " + strProtocol + " --sport " + iPort);
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p " + strProtocol + " --dport " + iPort);
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p " + strProtocol + " --sport " + iPort);
	}

	/**
	 * configureRouterMainPolicies() method configures the router
	 * <p>
	 * This method is the same as the first configureRouterMainPolicies(),
	 * but it doesn't specify a certain port for the communication
	 *
	 * @param   strConfigHost 	 String hostname to be configured
	 * @param   strFirstHost     String first host
	 * @param   strSecondHost    String second host
	 * @param   strProtocol      String protocol TCP/UDP
	 * @return  void
	 */
	public void configureRouterMainPolicies(String strConfigHost, String strFirstHost,
		String strSecondHost, String strProtocol) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strFirstHost + 
			" -d " + strSecondHost + " -p " + strProtocol);
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strSecondHost + 
			" -d " + strFirstHost + " -p " + strProtocol);

	}

	/**
	 * configureRouterMainPolicies() method configures the router
	 * <p>
	 * This method is the same as the first configureRouterMainPolicies(),
	 * but it doesn't specify a certain port and protocol for the communication
	 *
	 * @param   strConfigHost 	 String hostname to be configured
	 * @param   strFirstHost     String first host
	 * @param   strSecondHost    String second host
	 * @return  void
	 */
	public void configureRouterMainPolicies(String strConfigHost, String strFirstHost, String strSecondHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost);
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost);

	}

	/**
	 * configureHostMainPolicies() method configures the host
	 * <p>
	 * This method configures the host with router's policies
	 *
	 * @param   strConfigHost 	 String hostname to be configured
	 * @param   strFirstHost     String first host
	 * @param   strSecondHost    String second host
	 * @param   strProtocol      String protocol TCP/UDP
	 * @param   iSrcPort         Integer source port number
	 * @param   iDstPort         Integer destination port number
	 * @return  void
	 */
	public void configureHostMainPolicies(String strConfigHost, String strFirstHost,
		String strSecondHost, String strProtocol, int iSrcPort, int iDstPort) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		pwConfig.println("-I INPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p " + strProtocol + " --dport " + iDstPort);
		pwConfig.println("-I INPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p " + strProtocol + " --sport " + iSrcPort);
		pwConfig.println("-I INPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p " + strProtocol + " --sport " + iDstPort);
		pwConfig.println("-I INPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p " + strProtocol + " --dport " + iSrcPort);
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p " + strProtocol + " --dport " + iDstPort);
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p " + strProtocol + " --sport " + iSrcPort);
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p " + strProtocol + " --sport " + iDstPort);
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p " + strProtocol + " --dport " + iSrcPort);

	}

	/**
	 * configureHostMainPolicies() method configures the host
	 * <p>
	 * This method configures the host with router's policies
	 *
	 * @param   strConfigHost 	 String hostname to be configured
	 * @param   strFirstHost     String first host
	 * @param   strSecondHost    String second host
	 * @param   strProtocol      String protocol TCP/UDP
	 * @param   iPort            Integer port number
	 * @return  void
	 */
	public void configureHostMainPolicies(String strConfigHost, String strFirstHost,
		String strSecondHost, String strProtocol, int iPort) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		pwConfig.println("-I INPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p " + strProtocol + " --dport " + iPort);
		pwConfig.println("-I INPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p " + strProtocol + " --sport " + iPort);
		pwConfig.println("-I INPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p " + strProtocol + " --dport " + iPort);
		pwConfig.println("-I INPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p " + strProtocol + " --sport " + iPort);
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p " + strProtocol + " --dport " + iPort);
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p " + strProtocol + " --sport " + iPort);
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p " + strProtocol + " --dport " + iPort);
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p " + strProtocol + " --sport " + iPort);
	}

	/**
	 * configureHostMainPolicies() method configures the host
	 * <p>
	 * This method is the same as the first configureHostMainPolicies(),
	 * but it doesn't specify a certain port for the communication
	 *
	 * @param   strConfigHost 	 String hostname to be configured
	 * @param   strFirstHost     String first host
	 * @param   strSecondHost    String second host
	 * @param   strProtocol      String protocol TCP/UDP
	 * @return  void
	 */
	public void configureHostMainPolicies(String strConfigHost, String strFirstHost, 
		String strSecondHost, String strProtocol) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		pwConfig.println("-I INPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p " + strProtocol);
		pwConfig.println("-I INPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p " + strProtocol);
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p " + strProtocol);
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p " + strProtocol);
	}

	/**
	 * configureHostMainPolicies() method configures the host
	 * <p>
	 * This method is the same as the first configureHostMainPolicies(),
	 * but it doesn't specify a certain port and protocol for the communication
	 *
	 * @param   strConfigHost 	 String hostname to be configured
	 * @param   strFirstHost     String first host
	 * @param   strSecondHost    String second host
	 * @return  void
	 */
	public void configureHostMainPolicies(String strConfigHost, String strFirstHost, String strSecondHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		pwConfig.println("-I INPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost);
		pwConfig.println("-I INPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost);
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost);
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost);
	}


	/**
	 * configureRouterHTTPPolicies() method configures the router
	 * <p>
	 * This method configures the router's basic policies
	 * This method creates a command line using 'ssh' and 'iptables'
	 * to access the router and create Netfilter statements
	 *
	 * @param   strConfigHost 	 String hostname to be configured
	 * @param   strFirstHost  String first host address (source)
	 * @param   strSecondHost   String second host address (destination)
	 * @return  void
	 */
	public void configureRouterHTTPPolicies(String strConfigHost, String strFirstHost, String strSecondHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		// Allow HTTP
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p tcp --dport http");
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p tcp --sport http");
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p tcp --dport http");
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p tcp --sport http");
		// Allow HTTPS
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p tcp --dport https");
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p tcp --sport https");
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p tcp --dport https");
		pwConfig.println("-I FORWARD -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p tcp --sport https");
	}



	/**
	 * configureRouterICMPPolicies() method configures the router
	 * <p>
	 * This method configures the router's basic policies
	 * This method creates a command line using 'ssh' and 'iptables'
	 * to access the router and create Netfilter statements
	 *
	 * @param   strConfigHost 	 String hostname to be configured
	 * @return  void
	 */
	public void configureRouterICMPPolicies(String strConfigHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		// Allow ICMP
		pwConfig.println("-A FORWARD -j ACCEPT -p icmp");
		pwConfig.println("-A INPUT -j ACCEPT -p icmp");
		pwConfig.println("-A OUTPUT -j ACCEPT -p icmp");
	}

	/**
	 * configureRouterICMPPolicies() method configures the router
	 * <p>
	 * This method configures the router's basic policies
	 * This method creates a command line using 'ssh' and 'iptables'
	 * to access the router and create Netfilter statements
	 *
	 * @param   strConfigHost 	 	String hostname to be configured
	 * @param   strMonitorHost 		String monitor address
	 * @return  void
	 */
	public void configureRouterICMPPolicies(String strConfigHost, String strMonitorHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		// Allow ICMP
		pwConfig.println("-A FORWARD -j ACCEPT -p icmp");
		pwConfig.println("-A INPUT -j ACCEPT -s " + strMonitorHost + 
			" -d " + strConfigHost + " -p icmp");
		pwConfig.println("-A INPUT -j ACCEPT -s " + strConfigHost + 
			" -d " + strMonitorHost + " -p icmp");
		pwConfig.println("-A OUTPUT -j ACCEPT -s " + strMonitorHost + 
			" -d " + strConfigHost + " -p icmp");
		pwConfig.println("-A OUTPUT -j ACCEPT -s " + strConfigHost + 
			" -d " + strMonitorHost + " -p icmp");
	}

	/**
	 * configureRouterSSHPolicies() method configures the router
	 * <p>
	 * This method configures the router's basic policies
	 * This method creates a command line using 'ssh' and 'iptables'
	 * to access the router and create Netfilter statements
	 *
	 * @param   strConfigHost 	 	String hostname to be configured
	 * @param   strMonitorHost 		String monitor address
	 * @return  void
	 */
	public void configureRouterSSHPolicies(String strConfigHost, String strMonitorHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		// Allow SSH - port 22 (only from monitor host)
		pwConfig.println("-A INPUT -j ACCEPT -s " + 
			strMonitorHost + " -d " + strConfigHost + " -p tcp --dport ssh");
		pwConfig.println("-A INPUT -j ACCEPT -s " + 
			strMonitorHost + " -d " + strConfigHost + " -p tcp --sport ssh");
		pwConfig.println("-A INPUT -j ACCEPT -s " + 
			strConfigHost + " -d " + strMonitorHost + " -p tcp --dport ssh");
		pwConfig.println("-A INPUT -j ACCEPT -s " + 
			strConfigHost + " -d " + strMonitorHost + " -p tcp --sport ssh");
		pwConfig.println("-A OUTPUT -j ACCEPT -s " + 
			strMonitorHost + " -d " + strConfigHost + " -p tcp --dport ssh");
		pwConfig.println("-A OUTPUT -j ACCEPT -s " + 
			strMonitorHost + " -d " + strConfigHost + " -p tcp --sport ssh");
		pwConfig.println("-A OUTPUT -j ACCEPT -s " + 
			strConfigHost + " -d " + strMonitorHost + " -p tcp --dport ssh");
		pwConfig.println("-A OUTPUT -j ACCEPT -s " + 
			strConfigHost + " -d " + strMonitorHost + " -p tcp --sport ssh");
		pwConfig.println("-A FORWARD -j ACCEPT -p tcp --dport ssh");
		pwConfig.println("-A FORWARD -j ACCEPT -p tcp --sport ssh");

	}

	/**
	 * configureRouterDHCPPolicies() method configures the router
	 * <p>
	 * This method configures the router's basic policies
	 * This method creates a command line using 'ssh' and 'iptables'
	 * to access the router and create Netfilter statements
	 *
	 * @param   strConfigHost 	 	String hostname to be configured
	 * @return  void
	 */
	public void configureRouterDHCPPolicies(String strConfigHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		// Allow DHCP renew - BOOTP Client port 68 / BOOTP Server port 67
		pwConfig.println("-A INPUT -j ACCEPT -p udp --dport bootpc");
		pwConfig.println("-A INPUT -j ACCEPT -p udp --sport bootpc");
		pwConfig.println("-A OUTPUT -j ACCEPT -p udp --dport bootps");
		pwConfig.println("-A OUTPUT -j ACCEPT -p udp --sport bootps");
	}

	/**
	 * configureRouterDNSPolicies() method configures the router
	 * <p>
	 * This method configures the router's basic policies
	 * This method creates a command line using 'ssh' and 'iptables'
	 * to access the router and create Netfilter statements
	 *
	 * @param   strConfigHost 	 	String hostname to be configured
	 * @return  void
	 */
	public void configureRouterDNSPolicies(String strConfigHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		// Allow DNS UDP and TCP port 53
		pwConfig.println("-A INPUT -j ACCEPT -p tcp --dport domain");
		pwConfig.println("-A INPUT -j ACCEPT -p tcp --sport domain");
		pwConfig.println("-A OUTPUT -j ACCEPT -p tcp --dport domain");
		pwConfig.println("-A OUTPUT -j ACCEPT -p tcp --sport domain");
		pwConfig.println("-A INPUT -j ACCEPT -p udp --dport domain");
		pwConfig.println("-A INPUT -j ACCEPT -p udp --sport domain");
		pwConfig.println("-A OUTPUT -j ACCEPT -p udp --dport domain");
		pwConfig.println("-A OUTPUT -j ACCEPT -p udp --sport domain");
	}

	/**
	 * configureRejectPolicies() method configures the router
	 * <p>
	 * This method configures the router's basic policies
	 * This method creates a command line using 'ssh' and 'iptables'
	 * to access the router and create Netfilter statements
	 *
	 * @param   strConfigHost 	 	String hostname to be configured
	 * @return  void
	 */
	public void configureRejectPolicies(String strConfigHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		// Reject every other thing
		pwConfig.println("-A FORWARD -j REJECT");
		pwConfig.println("-A INPUT -j REJECT");
		pwConfig.println("-A OUTPUT -j REJECT");
	}

	/**
	 * configureRouterNATPolicy() method configures the router
	 * <p>
	 * This method configures the NAT policy separately.
	 * Somehow SSH in Java is not able to combine other commands for
	 * iptables rules configuration and NAT configuration.
	 *
	 * @param   strConfigHost 	 	String hostname to be configured
	 * @return  void
	 */
	public void configureRouterNATPolicy(String strConfigHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		// Configure NAT
		pwConfig.println("-t nat -A POSTROUTING -o eth0 -j MASQUERADE");
	}

	/**
	 * configureHostHTTPPolicies() method configures the host
	 * <p>
	 * This method configures the host with HTTP policies
	 *
	 * @param   strConfigHost 	String hostname to be configured
	 * @param   strFirstHost	String first host address (source)
	 * @param   strSecondHost	String second host address (destination)
	 * @return  void
	 */
	public void configureHostHTTPPolicies(String strConfigHost, String strFirstHost, String strSecondHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		// Allow HTTP
		pwConfig.println("-I INPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p tcp --dport http");
		pwConfig.println("-I INPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p tcp --sport http");
		pwConfig.println("-I INPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p tcp --dport http");
		pwConfig.println("-I INPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p tcp --sport http");
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p tcp --dport http");
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p tcp --sport http");
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p tcp --dport http");
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p tcp --sport http");
		// Allow HTTPS
		pwConfig.println("-I INPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p tcp --dport https");
		pwConfig.println("-I INPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p tcp --sport https");
		pwConfig.println("-I INPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p tcp --dport https");
		pwConfig.println("-I INPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p tcp --sport https");
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p tcp --dport https");
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strFirstHost + " -d " + strSecondHost +
			" -p tcp --sport https");
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p tcp --dport https");
		pwConfig.println("-I OUTPUT -j ACCEPT -s " + strSecondHost + " -d " + strFirstHost +
			" -p tcp --sport https");
	}

	/**
	 * configureHostICMPPolicies() method configures the host
	 * <p>
	 * This method configures the host with router's policies
	 *
	 * @param   strConfigHost 	 	String hostname to be configured
	 * @return  void
	 */
	public void configureHostICMPPolicies(String strConfigHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		// Allow ICMP
		pwConfig.println("-A INPUT -j ACCEPT -p icmp");
		pwConfig.println("-A OUTPUT -j ACCEPT -p icmp");
	}

	/**
	 * configureHostSQLPolicies() method configures the host
	 * <p>
	 * This method configures the host with router's policies
	 *
	 * @param   strConfigHost 	 	String hostname to be configured
	 * @return  void
	 */
	public void configureHostSQLPolicies(String strConfigHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		// Allow ICMP
		pwConfig.println("-A INPUT -j ACCEPT -p tcp --dport mysql");
		pwConfig.println("-A INPUT -j ACCEPT -p tcp --sport mysql");
		pwConfig.println("-A OUTPUT -j ACCEPT -p tcp --dport mysql");
		pwConfig.println("-A OUTPUT -j ACCEPT -p tcp --sport mysql");
	}

	/**
	 * configureHostICMPPolicies() method configures the host
	 * <p>
	 * This method configures the host with router's policies
	 *
	 * @param   strConfigHost 	 	String hostname to be configured
	 * @param   strMonitorHost 		String monitor address
	 * @return  void
	 */
	public void configureHostICMPPolicies(String strConfigHost, String strMonitorHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		// Allow ICMP
		pwConfig.println("-A INPUT -j ACCEPT -s " + strMonitorHost + 
			" -d " + strConfigHost + " -p icmp");
		pwConfig.println("-A INPUT -j ACCEPT -s " + strConfigHost + 
			" -d " + strMonitorHost + " -p icmp");
		pwConfig.println("-A OUTPUT -j ACCEPT -s " + strMonitorHost + 
			" -d " + strConfigHost + " -p icmp");
		pwConfig.println("-A OUTPUT -j ACCEPT -s " + strConfigHost + 
			" -d " + strMonitorHost + " -p icmp");
	}


	/**
	 * configureHostSSHPolicies() method configures the host
	 * <p>
	 * This method configures the host with router's policies
	 *
	 * @param   strConfigHost 	 	String hostname to be configured
	 * @return  void
	 */
	public void configureHostSSHPolicies(String strConfigHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		// Allow SSH - port 22
		pwConfig.println("-A INPUT -j ACCEPT -p tcp --dport ssh");
		pwConfig.println("-A INPUT -j ACCEPT -p tcp --sport ssh");
		pwConfig.println("-A OUTPUT -j ACCEPT -p tcp --dport ssh");
		pwConfig.println("-A OUTPUT -j ACCEPT -p tcp --sport ssh");
		pwConfig.println("-A FORWARD -j ACCEPT -p tcp --dport ssh");
		pwConfig.println("-A FORWARD -j ACCEPT -p tcp --sport ssh");
	}


	/**
	 * configureHostSSHPolicies() method configures the host
	 * <p>
	 * This method configures the host with router's policies
	 *
	 * @param   strConfigHost 	 	String hostname to be configured
	 * @param   strMonitorHost 		String monitor address
	 * @return  void
	 */
	public void configureHostSSHPolicies(String strConfigHost, String strMonitorHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		// Allow SSH - port 22
		pwConfig.println("-A INPUT -j ACCEPT -s " + 
			strMonitorHost + " -d " + strConfigHost + " -p tcp --dport ssh");
		pwConfig.println("-A INPUT -j ACCEPT -s " + 
			strMonitorHost + " -d " + strConfigHost + " -p tcp --sport ssh");
		pwConfig.println("-A INPUT -j ACCEPT -s " + 
			strConfigHost + " -d " + strMonitorHost + " -p tcp --dport ssh");
		pwConfig.println("-A INPUT -j ACCEPT -s " + 
			strConfigHost + " -d " + strMonitorHost + " -p tcp --sport ssh");
		pwConfig.println("-A OUTPUT -j ACCEPT -s " + 
			strMonitorHost + " -d " + strConfigHost + " -p tcp --dport ssh");
		pwConfig.println("-A OUTPUT -j ACCEPT -s " + 
			strMonitorHost + " -d " + strConfigHost + " -p tcp --sport ssh");
		pwConfig.println("-A OUTPUT -j ACCEPT -s " + 
			strConfigHost + " -d " + strMonitorHost + " -p tcp --dport ssh");
		pwConfig.println("-A OUTPUT -j ACCEPT -s " + 
			strConfigHost + " -d " + strMonitorHost + " -p tcp --sport ssh");
	}


	/**
	 * configureHostDHCPPolicies() method configures the host
	 * <p>
	 * This method configures the host with router's policies
	 *
	 * @param   strConfigHost 	 	String hostname to be configured
	 * @return  void
	 */
	public void configureHostDHCPPolicies(String strConfigHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		// Allow DHCP renew - BOOTP Client port 68 / BOOTP Server port 67
		pwConfig.println("-A INPUT -j ACCEPT -p udp --dport bootpc");
		pwConfig.println("-A INPUT -j ACCEPT -p udp --sport bootpc");
		pwConfig.println("-A OUTPUT -j ACCEPT -p udp --dport bootps");
		pwConfig.println("-A OUTPUT -j ACCEPT -p udp --sport bootps");
	}


	/**
	 * configureHostDNSPolicies() method configures the host
	 * <p>
	 * This method configures the host with router's policies
	 *
	 * @param   strConfigHost 	 	String hostname to be configured
	 * @return  void
	 */
	public void configureHostDNSPolicies(String strConfigHost) {

		PrintWriter pwConfig = getPrintWriter(strConfigHost);
		// Allow DNS UDP and TCP port 53
		pwConfig.println("-A INPUT -j ACCEPT -p tcp --dport domain");
		pwConfig.println("-A INPUT -j ACCEPT -p tcp --sport domain");
		pwConfig.println("-A OUTPUT -j ACCEPT -p tcp --dport domain");
		pwConfig.println("-A OUTPUT -j ACCEPT -p tcp --sport domain");
		pwConfig.println("-A INPUT -j ACCEPT -p udp --dport domain");
		pwConfig.println("-A INPUT -j ACCEPT -p udp --sport domain");
		pwConfig.println("-A OUTPUT -j ACCEPT -p udp --dport domain");
		pwConfig.println("-A OUTPUT -j ACCEPT -p udp --sport domain");

	}
}
