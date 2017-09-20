package iotruntime;

// Java packages
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import iotruntime.slave.IoTDeviceAddress;

/** Class IoTUDP is a wrapper class that provides
 *  minimum interfaces for user to interact with IoT
 *  devices in our system - adapted from my colleague's
 *  work (Ali Younis - ayounis @ uci.edu)
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-02-20
 */
public final class IoTUDP {

	/**
	 * IoTUDP class properties
	 */
	private final String strHostAddress;
	private final int iSrcPort;
	private final int iDstPort;
	private DatagramSocket socket;	// the socket interface that we are guarding
	private boolean didClose; 		// make sure that the clean up was done correctly

	/**
	 * Class constructor
	 */
	public IoTUDP(IoTDeviceAddress iotDevAdd) throws SocketException, IOException {

		strHostAddress = iotDevAdd.getHostAddress();
		iSrcPort = iotDevAdd.getSourcePortNumber();
		iDstPort = iotDevAdd.getDestinationPortNumber();

		socket = new DatagramSocket(iSrcPort);
		didClose = false;
	}

	/**
	 * sendData() method
	 *
	 * @param  bData     Byte type that passes the data to be sent
	 * @return void
	 */
	public void sendData(byte[] bData) throws UnknownHostException, IOException {

		DatagramPacket dpSendPacket = new DatagramPacket(bData, bData.length, InetAddress.getByName(strHostAddress), iDstPort);
		socket.send(dpSendPacket);
	}

	/**
	 * recieveData() method
	 *
	 * @param  iMaxDataLength  Integer maximum data length as reference
	 * @return byte[]
	 */
	public byte[] recieveData(int iMaxDataLength) throws IOException {

		byte[] bReceiveData = new byte[iMaxDataLength];
		DatagramPacket dpReceivePacket = new DatagramPacket(bReceiveData, bReceiveData.length);
		socket.receive(dpReceivePacket);

		return dpReceivePacket.getData();
	}

	/**
	 * setSoTimeout() method
	 *
	 * @param  iTimeout  Integer timeout time
	 */
	public void setSoTimeout(int iTimeout) throws SocketException {

		socket.setSoTimeout(iTimeout);

	}

	/**
	 * setSendBufferSize() method
	 *
	 * @param  iSize  Integer buffer size
	 */
	public void setSendBufferSize(int iSize) throws SocketException {

		socket.setSendBufferSize(iSize);

	}

	/**
	 * setReceiveBufferSize() method
	 *
	 * @param  iSize  Integer buffer size
	 */
	public void setReceiveBufferSize(int iSize) throws SocketException {

		socket.setReceiveBufferSize(iSize);

	}


	/**
	 * close() method
	 */
	public void close() {

		socket.close();
		didClose = true;

	}

	/**
	 * close() called by the garbage collector right before trashing object
	 */
	public void finalize() throws SocketException {

		if (!didClose) {
			close();
			throw new SocketException("Socket not closed before object destruction, must call close method.");
		}

	}
}
