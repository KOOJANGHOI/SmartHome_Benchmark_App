package iotrmi.Java;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


/** Class IoTRMICommClient is a class that extends IoTRMIComm
 *  <p>
 *  This is a version of IoTRMIComm that sits on the main stub.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2017-01-27
 */
public final class IoTRMICommClient extends IoTRMIComm {

	/**
	 * Class Properties
	 */
	private IoTSocketClient rmiClientSend;
	private IoTSocketClient rmiClientRecv;


	/**
	 * Constructor (for stub) - send and recv from the perspective of RMI socket servers
	 */
	public IoTRMICommClient(int _localPortSend, int _localPortRecv, int _portSend, int _portRecv, String _address, int _rev) throws  
		ClassNotFoundException, InstantiationException, 
			IllegalAccessException, IOException {

		super();
		rmiClientRecv = new IoTSocketClient(_localPortSend, _portSend, _address, _rev);
		rmiClientSend = new IoTSocketClient(_localPortRecv, _portRecv, _address, _rev);
		waitForPacketsOnClient();
	}


	/**
	 * Constructor (for stub) - only destination port numbers
	 */
	public IoTRMICommClient(int _portSend, int _portRecv, String _address, int _rev) throws  
		ClassNotFoundException, InstantiationException, 
			IllegalAccessException, IOException {

		super();
		rmiClientRecv = new IoTSocketClient(_portSend, _address, _rev);
		rmiClientSend = new IoTSocketClient(_portRecv, _address, _rev);
		waitForPacketsOnClient();
	}


	/**
	 * waitForPacketsOnClient() starts a thread that waits for packet bytes on client side
	 */
	public void waitForPacketsOnClient() {

		Thread thread = new Thread() {
			public void run() {
				byte[] packetBytes = null;
				while(true) {
					try {
						packetBytes = rmiClientRecv.receiveBytes(packetBytes);
						if (packetBytes != null) {
							int packetType = IoTRMIComm.getPacketType(packetBytes);
							if (packetType == IoTRMIUtil.METHOD_TYPE) {
								//System.out.println("Method packet: " + Arrays.toString(packetBytes));
								methodQueue.offer(packetBytes);
							} else if (packetType == IoTRMIUtil.RET_VAL_TYPE) {
								//System.out.println("Return value packet: " + Arrays.toString(packetBytes));
								returnQueue.offer(packetBytes);
							} else
								throw new Error("IoTRMICommClient: Packet type is unknown: " + packetType);
						} //else
						//	Thread.sleep(100);
						packetBytes = null;
					} catch (Exception ex) {
						ex.printStackTrace();
						throw new Error("IoTRMICommClient: Error receiving return value bytes on client!");
					}
				}
			}
		};
		thread.start();
	}


	/**
	 * sendReturnObj() for non-struct objects (client side)
	 */
	public synchronized void sendReturnObj(Object retObj, byte[] methodBytes) {

		// Send back return value
		//byte[] retObjBytes = IoTRMIUtil.getObjectBytes(retObj);
		byte[] retObjBytes = null;
		if (retObj != null)	// Handle nullness
			retObjBytes = IoTRMIUtil.getObjectBytes(retObj);
		// Send return value together with OBJECT_ID and METHOD_ID for arbitration
		int objMethIdLen = IoTRMIUtil.OBJECT_ID_LEN + IoTRMIUtil.METHOD_ID_LEN;
		int headerLen = objMethIdLen + IoTRMIUtil.PACKET_TYPE_LEN;
		//byte[] retAllBytes = new byte[headerLen + retObjBytes.length];
		byte[] retAllBytes = null;
		if (retObj == null)	// Handle nullness
			retAllBytes = new byte[headerLen];
		else
			retAllBytes = new byte[headerLen + retObjBytes.length];
		// Copy OBJECT_ID and METHOD_ID
		System.arraycopy(methodBytes, 0, retAllBytes, 0, objMethIdLen);
		int packetType = IoTRMIUtil.RET_VAL_TYPE;	// This is a return value
		byte[] packetTypeBytes = IoTRMIUtil.intToByteArray(packetType);
		System.arraycopy(packetTypeBytes, 0, retAllBytes, objMethIdLen, IoTRMIUtil.PACKET_TYPE_LEN);
		// Copy array of bytes (return object)
		if (retObj != null)
			System.arraycopy(retObjBytes, 0, retAllBytes, headerLen, retObjBytes.length);
		try {
			rmiClientSend.sendBytes(retAllBytes);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new Error("IoTRMICommClient: Error sending bytes in sendReturnObj()!");
		}
	}


	/**
	 * sendReturnObj() overloaded to send multiple return objects for structs (client side)
	 */
	public synchronized void sendReturnObj(Class<?>[] retCls, Object[] retObj, byte[] methodBytes) {

		// Send back return value
		byte[] retObjBytes = returnToBytes(retCls, retObj);
		// Send return value together with OBJECT_ID and METHOD_ID for arbitration
		int objMethIdLen = IoTRMIUtil.OBJECT_ID_LEN + IoTRMIUtil.METHOD_ID_LEN;
		int headerLen = objMethIdLen + IoTRMIUtil.PACKET_TYPE_LEN;
		byte[] retAllBytes = new byte[headerLen + retObjBytes.length];
		// Copy OBJECT_ID and METHOD_ID
		System.arraycopy(methodBytes, 0, retAllBytes, 0, objMethIdLen);
		int packetType = IoTRMIUtil.RET_VAL_TYPE;	// This is a return value
		byte[] packetTypeBytes = IoTRMIUtil.intToByteArray(packetType);
		System.arraycopy(packetTypeBytes, 0, retAllBytes, objMethIdLen, IoTRMIUtil.PACKET_TYPE_LEN);
		// Copy array of bytes (return object)
		try {
			rmiClientSend.sendBytes(retAllBytes);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new Error("IoTRMICommClient: Error sending bytes in sendReturnObj()!");
		}
	}


	/**
	 * remoteCall() calls a method remotely by passing in parameters (client side)
	 */
	public synchronized void remoteCall(int objectId, int methodId, Class<?>[] paramCls, Object[] paramObj) {

		// Send method info
		byte[] methodBytes = methodToBytes(objectId, methodId, paramCls, paramObj);
		try {
			rmiClientSend.sendBytes(methodBytes);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new Error("IoTRMICommClient: Error when sending bytes in remoteCall()!");
		}
	}
}
