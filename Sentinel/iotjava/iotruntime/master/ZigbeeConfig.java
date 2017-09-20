package iotruntime.master;

// Java packages
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/** Class ZigbeeConfig is a class that configures the zigbee
 *  gateway in our network with the relevant policies
 *  through the usage of static methods;
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-05-04
 */
public final class ZigbeeConfig {

	/**
	 * Constants
	 */
	public final int SOCKET_SEND_BUFFER_SIZE = 1024;
	public final int SOCKET_RECEIVE_BUFFER_SIZE = 1024;

	/**
	 * ZigbeeConfig properties
	 */
	private String strZigbeeGatewayAddress;
	private int iZigBeeGatewayPort;
	private int iMasterPort;
	private DatagramSocket socket;
	private boolean bVerbose;

	/**
	 * Class constructor
	 */
	public ZigbeeConfig(String _strZigbeeGatewayAddress, int _iZigBeeGatewayPort, int _iMasterPort, boolean _bVerbose) 
		throws SocketException {

		strZigbeeGatewayAddress = _strZigbeeGatewayAddress;
		iZigBeeGatewayPort = _iZigBeeGatewayPort;
		iMasterPort = _iMasterPort;
		bVerbose = _bVerbose;

		socket = new DatagramSocket(iMasterPort);
		socket.setSendBufferSize(SOCKET_SEND_BUFFER_SIZE);
		socket.setReceiveBufferSize(SOCKET_RECEIVE_BUFFER_SIZE);

		RuntimeOutput.print("ZigbeeConfig: Zigbee gateway policy support for: " + 
			strZigbeeGatewayAddress + " with IoTMaster port: " + iMasterPort + 
			" and gateway port: " + iZigBeeGatewayPort, bVerbose);
	}

	/**
	 * clearAllPolicies() method to delete the zigbee policies
	 *
	 * @return  void
	 */
	public void clearAllPolicies() throws IOException {

		// Clearing all policies on Zigbee gateway
		RuntimeOutput.print("ZigbeeConfig: Accessing Zigbee gateway and deleting policies...", bVerbose);

		String strMessage = "type: policy_clear\n";
		DatagramPacket dpSendPacket = new DatagramPacket(strMessage.getBytes(), 
			strMessage.getBytes().length, InetAddress.getByName(strZigbeeGatewayAddress), iZigBeeGatewayPort);
		socket.send(dpSendPacket);

		RuntimeOutput.print("ZigbeeConfig: Sending policy message to Zigbee gateway: " + strMessage, bVerbose);
	}


	/**
	 * setPolicy() method to set policies
	 *
	 * @param	String 	Host address where the Zigbee driver is running as assigned by master
	 * @param	int 	Host port number
	 * @param	String 	String Zigbee device address
	 * @return  void
	 */
	public void setPolicy(String strHostAddress, int iHostPort, String strZigbeeAddress) throws IOException {

		// Clearing all policies on Zigbee gateway
		RuntimeOutput.print("ZigbeeConfig: Accessing Zigbee gateway and sending a policy...", bVerbose);

		String strMessage = "type: policy_set\n";
		strMessage += "ip_address: " + strHostAddress + "\n";
		strMessage += "port: " + iHostPort + "\n";
		strMessage += "device_address_long: " + strZigbeeAddress + "\n";

		DatagramPacket dpSendPacket = new DatagramPacket(strMessage.getBytes(), 
			strMessage.getBytes().length, InetAddress.getByName(strZigbeeGatewayAddress), iZigBeeGatewayPort);
		socket.send(dpSendPacket);

		RuntimeOutput.print("ZigbeeConfig: Sending policy message to Zigbee gateway: " + strMessage, bVerbose);
	}
	
	/**
	 * closeConnection()
	 *
	 * @return  void
	 */
	public void closeConnection() throws IOException {
		socket.setReuseAddress(true);
		socket.close();
	}
}
