package iotruntime;

// Java packages
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;

import iotruntime.slave.IoTDeviceAddress;

/** Class IoTTCP is a wrapper class that provides
 *  minimum interfaces for user to interact with IoT
 *  devices in our system
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-02-18
 */
public final class IoTTCP {

	/**
	 * IoTTCP class properties
	 */
	private Socket socket;

	protected IoTTCP(Socket _socket) {
		socket = _socket;
	}

	/**
	 * Class constructor
	 */
	public IoTTCP(IoTDeviceAddress iotDevAdd) throws UnknownHostException, IOException {

		String strHostAddress = iotDevAdd.getHostAddress();
		int iSrcPort = iotDevAdd.getSourcePortNumber();
		int iDstPort = iotDevAdd.getDestinationPortNumber();

		socket = new Socket(strHostAddress, iDstPort, InetAddress.getLocalHost(), iSrcPort);
	}

	/**
	 * getInputStream() method
	 */
	public InputStream getInputStream() throws UnknownHostException, IOException {

		return socket.getInputStream();
	}

	/**
	 * getOutputStream() method
	 */
	public OutputStream getOutputStream() throws UnknownHostException, IOException {

		return socket.getOutputStream();
	}

	/**
	* setReuseAddress(boolean on) method
	*/
	public void setReuseAddress(boolean on) throws SocketException {

		socket.setReuseAddress(on);
	}


	/**
	 * close() method
	 */
	public void close() throws UnknownHostException, IOException {

		socket.close();
	}
}
