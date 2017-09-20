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


/** Class IoTRMICommServer is a class that extends IoTRMI
 *  <p>
 *  We will arbitrate packets into 2 queues and wake up the right threads/callers.
 *  We separate traffics one-directionally.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2017-01-27
 */
public final class IoTRMICommServer extends IoTRMIComm {

	/**
	 * Class Properties
	 */
	private IoTSocketServer rmiServerSend;
	private IoTSocketServer rmiServerRecv;
	private AtomicBoolean didServerSendConnect;
	private AtomicBoolean didServerRecvConnect;
	

	/**
	 * Constructor (for skeleton)
	 */
	public IoTRMICommServer(int _portSend, int _portRecv) throws  
		ClassNotFoundException, InstantiationException, 
			IllegalAccessException, IOException {

		super();
		didServerSendConnect = new AtomicBoolean(false);
		didServerRecvConnect = new AtomicBoolean(false);
		rmiServerSend = new IoTSocketServer(_portSend);
		rmiServerRecv = new IoTSocketServer(_portRecv);
		waitForConnectionOnServerSend();
		waitForConnectionOnServerRecv();
		while(!didServerSendConnect.get());	// Wait until server is connected
		while(!didServerRecvConnect.get()); // Wait until server is connected
		waitForPacketsOnServer();
	}


	/**
	 * waitForConnectionOnServerRecv() starts a thread that waits server connection
	 */
	public void waitForConnectionOnServerRecv() {

		Thread thread = new Thread() {
			public void run() {
				try {
					rmiServerRecv.connect();
					didServerRecvConnect.set(true);
				} catch (Exception ex) {
					ex.printStackTrace();
					throw new Error("IoTRMICommServer: Error starting receiver server!");
				}
			}
		};
		thread.start();
	}


	/**
	 * waitForConnectionOnServerSend() starts a thread that waits server connection
	 */
	public void waitForConnectionOnServerSend() {

		Thread thread = new Thread() {
			public void run() {
				try {
					rmiServerSend.connect();
					didServerSendConnect.set(true);
				} catch (Exception ex) {
					ex.printStackTrace();
					throw new Error("IoTRMICommServer: Error starting sender server!");
				}
			}
		};
		thread.start();
	}


	/**
	 * waitForPacketsOnServer() starts a thread that waits for packet bytes on server side
	 */
	public void waitForPacketsOnServer() {

		Thread thread = new Thread() {
			public void run() {
				byte[] packetBytes = null;
				while(true) {
					try {
						packetBytes = rmiServerRecv.receiveBytes(packetBytes);
						if (packetBytes != null) {
							//System.out.println("Packet received: " + Arrays.toString(packetBytes));
							int packetType = IoTRMIComm.getPacketType(packetBytes);
							if (packetType == IoTRMIUtil.METHOD_TYPE) {
								//System.out.println("Method packet: " + Arrays.toString(packetBytes));
								methodQueue.offer(packetBytes);
							} else if (packetType == IoTRMIUtil.RET_VAL_TYPE) {
								//System.out.println("Return value packet: " + Arrays.toString(packetBytes));
								returnQueue.offer(packetBytes);
							} else
								throw new Error("IoTRMICommServer: Packet type is unknown: " + packetType);
						} //else
						//	Thread.sleep(100);
						packetBytes = null;
					} catch (Exception ex) {
						ex.printStackTrace();
						throw new Error("IoTRMICommServer: Error receiving return value bytes on server!");
					}
				}
			}
		};
		thread.start();
	}


	/**
	 * sendReturnObj() for non-struct objects (server side)
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
			rmiServerSend.sendBytes(retAllBytes);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new Error("IoTRMICommServer: Error sending bytes in sendReturnObj()!");
		}
	}


	/**
	 * sendReturnObj() overloaded to send multiple return objects for structs (server side)
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
		System.arraycopy(retObjBytes, 0, retAllBytes, headerLen, retObjBytes.length);
		try {
			rmiServerSend.sendBytes(retAllBytes);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new Error("IoTRMICommServer: Error sending bytes in sendReturnObj()!");
		}
	}


	/**
	 * remoteCall() calls a method remotely by passing in parameters (server side)
	 */
	public synchronized void remoteCall(int objectId, int methodId, Class<?>[] paramCls, Object[] paramObj) {

		// Send method info
		byte[] methodBytes = methodToBytes(objectId, methodId, paramCls, paramObj);
		try {
			rmiServerSend.sendBytes(methodBytes);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new Error("IoTRMICommServer: Error when sending bytes in remoteCall()!");
		}
	}
}
