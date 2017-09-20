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


/** Abstract class IoTRMIComm is a class that combines IoTRMIObject and IoTRMICall
 *  <p>
 *  We will arbitrate packets into 2 queues and wake up the right threads/callers.
 *  We separate traffics one-directionally.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2017-01-27
 */
public abstract class IoTRMIComm {

	/**
	 * Class Properties
	 */
	protected IoTRMIUtil rmiUtil;
	protected byte[] methodBytes;
	protected byte[] retValueBytes;
	protected ConcurrentLinkedQueue<byte[]> methodQueue;
	protected ConcurrentLinkedQueue<byte[]> returnQueue;
	protected Map<Integer,AtomicBoolean> mapSkeletonId;
	protected Map<String,AtomicBoolean> mapStubId;
	protected AtomicBoolean didGetMethodBytes;
	protected AtomicBoolean didGetReturnBytes;
	protected int objectIdCounter = Integer.MAX_VALUE;

	/**
	 * Constructor (for skeleton)
	 */
	public IoTRMIComm() throws  
		ClassNotFoundException, InstantiationException, 
			IllegalAccessException, IOException {

		rmiUtil = new IoTRMIUtil();
		methodBytes = null;
		retValueBytes = null;
		methodQueue = new ConcurrentLinkedQueue<byte[]>();
		returnQueue = new ConcurrentLinkedQueue<byte[]>();
		mapSkeletonId = new HashMap<Integer,AtomicBoolean>();
		mapStubId = new HashMap<String,AtomicBoolean>();
		didGetMethodBytes = new AtomicBoolean(false);
		didGetReturnBytes = new AtomicBoolean(false);
		wakeUpThreadOnMethodCall();
		wakeUpThreadOnReturnValue();
	}


	/**
	 * wakeUpThreadOnMethodCall() wakes up the correct thread when receiving method call
	 */
	private void wakeUpThreadOnMethodCall() {

		Thread thread = new Thread() {
			public void run() {
				while(true) {
					// Take the current method from the queue and wake up the correct thread
					methodBytes = methodQueue.poll();
					if (methodBytes != null) {	// If there is method bytes
						int currObjId = getObjectId(methodBytes);
						AtomicBoolean methRecv = mapSkeletonId.get(currObjId);
						didGetMethodBytes.set(false);
						while(!methRecv.compareAndSet(false, true));
						while(!didGetMethodBytes.get());	// While skeleton is still processing
					}
				}
			}
		};
		thread.start();
	}


	/**
	 * wakeUpThreadOnReturnValue() wakes up the correct thread when receiving return value
	 */
	private void wakeUpThreadOnReturnValue() {

		Thread thread = new Thread() {
			public void run() {
				while(true) {
					// Take the current method from the queue and wake up the correct thread
					retValueBytes = returnQueue.poll();
					if (retValueBytes != null) {	// If there is method bytes
						//System.out.println("retValBytes in wake up thread: " + Arrays.toString(retValueBytes));
						int objectId = getObjectId(retValueBytes);
						int methodId = getMethodId(retValueBytes);
						String strKey = objectId + "-" + methodId;
						AtomicBoolean retRecv = mapStubId.get(strKey);
						//System.out.println("boolean status: " + retRecv + " with key: " + strKey);
						didGetReturnBytes.set(false);
						while(!retRecv.compareAndSet(false, true));
						//System.out.println("boolean status: " + retRecv + " - map has: " + mapStubId.size());
						while(!didGetReturnBytes.get());	// While skeleton is still processing
					}
				}
			}
		};
		thread.start();
	}


	/**
	 * registerSkeleton() registers the skeleton to be woken up
	 */
	public synchronized void registerSkeleton(int objectId, AtomicBoolean methodReceived) {

		mapSkeletonId.put(objectId, methodReceived);
	}


	/**
	 * registerStub() registers the skeleton to be woken up
	 */
	public synchronized void registerStub(int objectId, int methodId, AtomicBoolean retValueReceived) {

		String strKey = objectId + "-" + methodId;
		//System.out.println("Key exist? " + mapStubId.containsKey(strKey));
		mapStubId.put(strKey, retValueReceived);
		//System.out.println("\n\nAdding keyBytes: " + strKey + " now size: " + mapStubId.size() + "\n\n");
	}


	/**
	 * getObjectIdCounter() gets object Id counter
	 */
	public int getObjectIdCounter() {

		return objectIdCounter;
	}


	/**
	 * setObjectIdCounter() sets object Id counter
	 */
	public void setObjectIdCounter(int objIdCounter) {

		objectIdCounter = objIdCounter;
	}


	/**
	 * decrementObjectIdCounter() gets object Id counter
	 */
	public void decrementObjectIdCounter() {

		objectIdCounter--;
	}


	/**
	 * setGetMethodBytes() set didGetMethodBytes to true after getting the bytes
	 */
	public boolean setGetMethodBytes() {

		return didGetMethodBytes.compareAndSet(false, true);
	}


	/**
	 * setGetReturnBytes() set didGetReturnBytes if there is a new return value already
	 */
	public synchronized boolean setGetReturnBytes() {

		return didGetReturnBytes.compareAndSet(false, true);
	}


	/**
	 * getMethodBytes() get the method in bytes
	 */
	public byte[] getMethodBytes() throws IOException {

		// Just return the methodBytes content
		return methodBytes;
	}


	/**
	 * static version of getObjectId()
	 */
	public static int getObjectId(byte[] packetBytes) {

		// Get object Id bytes
		byte[] objectIdBytes = new byte[IoTRMIUtil.OBJECT_ID_LEN];
		System.arraycopy(packetBytes, 0, objectIdBytes, 0, IoTRMIUtil.OBJECT_ID_LEN);
		// Get object Id
		int objectId = IoTRMIUtil.byteArrayToInt(objectIdBytes);
		return objectId;
	}


	/**
	 * static version of getMethodId()
	 */
	public static int getMethodId(byte[] packetBytes) {

		// Get method Id bytes
		byte[] methodIdBytes = new byte[IoTRMIUtil.METHOD_ID_LEN];
		// Method Id is positioned after object Id in the byte array
		System.arraycopy(packetBytes, IoTRMIUtil.OBJECT_ID_LEN, methodIdBytes, 0, IoTRMIUtil.METHOD_ID_LEN);
		// Get method Id
		int methodId = IoTRMIUtil.byteArrayToInt(methodIdBytes);
		// Get method Id
		return methodId;
	}


	/**
	 * static version of getPacketType() - either method or return value (position is after object Id and method Id)
	 */
	public static int getPacketType(byte[] packetBytes) {

		// Get packet type bytes
		byte[] packetTypeBytes = new byte[IoTRMIUtil.PACKET_TYPE_LEN];
		int offset = IoTRMIUtil.OBJECT_ID_LEN + IoTRMIUtil.METHOD_ID_LEN;
		System.arraycopy(packetBytes, offset, packetTypeBytes, 0, IoTRMIUtil.PACKET_TYPE_LEN);
		// Get packet type (for now we assume 1 as method and -1 as return value
		int packetType = IoTRMIUtil.byteArrayToInt(packetTypeBytes);
		return packetType;
	}


	/**
	 * getMethodParams() gets method params based on byte array received
	 * <p>
	 * Basically this is the format of a method in bytes:
	 * 1) 32-bit value of object ID
	 * 2) 32-bit value of method ID
	 * 3) m parameters with n-bit value each (m x n-bit)
	 * For the parameters that don't have definite length,
	 * we need to extract the length from a preceding 32-bit
	 * field in front of it.
	 *
	 * For primitive objects:
	 * | 32-bit object ID | 32-bit method ID | m-bit actual data (fixed length)  | ...
	 * 
	 * For string, arrays, and non-primitive objects:
	 * | 32-bit object ID | 32-bit method ID | 32-bit length | n-bit actual data | ...
	 * 
	 */
	public Object[] getMethodParams(Class<?>[] arrCls, Class<?>[] arrGenValCls, byte[] methodBytes) {

		// Byte scanning position
		int pos = IoTRMIUtil.OBJECT_ID_LEN + IoTRMIUtil.METHOD_ID_LEN + IoTRMIUtil.PACKET_TYPE_LEN;
		Object[] paramObj = new Object[arrCls.length];
		for (int i=0; i < arrCls.length; i++) {

			String paramType = arrCls[i].getSimpleName();
			int paramSize = rmiUtil.getTypeSize(paramType);
			// Get the 32-bit field in the byte array to get the actual
			// 		length (this is a param with indefinite length)
			if (paramSize == -1) {
				byte[] bytPrmLen = new byte[IoTRMIUtil.PARAM_LEN];
				System.arraycopy(methodBytes, pos, bytPrmLen, 0, IoTRMIUtil.PARAM_LEN);
				pos = pos + IoTRMIUtil.PARAM_LEN;
				paramSize = IoTRMIUtil.byteArrayToInt(bytPrmLen);
			}
			byte[] paramBytes = new byte[paramSize];
			System.arraycopy(methodBytes, pos, paramBytes, 0, paramSize);
			pos = pos + paramSize;
			paramObj[i] = IoTRMIUtil.getParamObject(arrCls[i], arrGenValCls[i], paramBytes);
		}

		return paramObj;
	}


	/**
	 * sendReturnObj() abstract version
	 */
	public abstract void sendReturnObj(Object retObj, byte[] methodBytes);


	/**
	 * sendReturnObj() abstract version
	 */
	public abstract void sendReturnObj(Class<?>[] retCls, Object[] retObj, byte[] methodBytes);


	/**
	 * returnToBytes() takes array of objects and generates bytes
	 */
	public byte[] returnToBytes(Class<?>[] retCls, Object[] retObj) {

		// Get byte arrays and calculate method bytes length
		int numbRet = retObj.length;
		int retLen = 0;
		byte[][] objBytesArr = new byte[numbRet][];
		for (int i = 0; i < numbRet; i++) {
			// Get byte arrays for the objects
			objBytesArr[i] = IoTRMIUtil.getObjectBytes(retObj[i]);
			String clsName = retCls[i].getSimpleName();
			int retObjLen = rmiUtil.getTypeSize(clsName);
			if (retObjLen == -1) { 		// indefinite length - store the length first
				retLen = retLen + IoTRMIUtil.RETURN_LEN;
			}
			retLen = retLen + objBytesArr[i].length;
		}
		// Construct return in byte array
		byte[] retBytes = new byte[retLen];
		int pos = 0;
		// Iteration for copying bytes
		for (int i = 0; i < numbRet; i++) {

			String clsName = retCls[i].getSimpleName();
			int retObjLen = rmiUtil.getTypeSize(clsName);
			if (retObjLen == -1) { 		// indefinite length
				retObjLen = objBytesArr[i].length;
				byte[] retLenBytes = IoTRMIUtil.intToByteArray(retObjLen);
				System.arraycopy(retLenBytes, 0, retBytes, pos, IoTRMIUtil.RETURN_LEN);
				pos = pos + IoTRMIUtil.RETURN_LEN;
			}		
			System.arraycopy(objBytesArr[i], 0, retBytes, pos, retObjLen);
			pos = pos + retObjLen;
		}

		return retBytes;
	}


	/**
	 * remoteCall() abstract version
	 */
	public abstract void remoteCall(int objectId, int methodId, Class<?>[] paramCls, Object[] paramObj);


	/**
	 * methodToBytes() returns byte representation of a method
	 */
	public byte[] methodToBytes(int objectId, int methId, Class<?>[] paramCls, Object[] paramObj) {

		// Initialized to the length of method ID
		int methodLen = IoTRMIUtil.OBJECT_ID_LEN;
		byte[] objId = IoTRMIUtil.intToByteArray(objectId);
		// Get method ID in bytes
		byte[] methodId = IoTRMIUtil.intToByteArray(methId);
		// Get byte arrays and calculate method bytes length
		int numbParam = paramObj.length;
		methodLen = methodLen + IoTRMIUtil.METHOD_ID_LEN;
		methodLen = methodLen + IoTRMIUtil.PACKET_TYPE_LEN;
		byte[][] objBytesArr = new byte[numbParam][];
		for (int i = 0; i < numbParam; i++) {
			// Get byte arrays for the objects
			objBytesArr[i] = IoTRMIUtil.getObjectBytes(paramObj[i]);
			String clsName = paramCls[i].getSimpleName();
			int paramLen = rmiUtil.getTypeSize(clsName);
			if (paramLen == -1) { 		// indefinite length - store the length first
				methodLen = methodLen + IoTRMIUtil.PARAM_LEN;
			}
			methodLen = methodLen + objBytesArr[i].length;
		}
		// Construct method in byte array
		byte[] method = new byte[methodLen];
		int pos = 0;
		System.arraycopy(objId, 0, method, 0, IoTRMIUtil.METHOD_ID_LEN);
		pos = pos + IoTRMIUtil.OBJECT_ID_LEN;
		System.arraycopy(methodId, 0, method, pos, IoTRMIUtil.METHOD_ID_LEN);
		pos = pos + IoTRMIUtil.METHOD_ID_LEN;
		int packetType = IoTRMIUtil.METHOD_TYPE;	// This is a method
		byte[] packetTypeBytes = IoTRMIUtil.intToByteArray(packetType);
		System.arraycopy(packetTypeBytes, 0, method, pos, IoTRMIUtil.PACKET_TYPE_LEN);
		pos = pos + IoTRMIUtil.PACKET_TYPE_LEN;
		// Second iteration for copying bytes
		for (int i = 0; i < numbParam; i++) {

			String clsName = paramCls[i].getSimpleName();
			int paramLen = rmiUtil.getTypeSize(clsName);
			if (paramLen == -1) { 		// indefinite length
				paramLen = objBytesArr[i].length;
				byte[] paramLenBytes = IoTRMIUtil.intToByteArray(paramLen);
				System.arraycopy(paramLenBytes, 0, method, pos, IoTRMIUtil.PARAM_LEN);
				pos = pos + IoTRMIUtil.PARAM_LEN;
			}		
			System.arraycopy(objBytesArr[i], 0, method, pos, paramLen);
			pos = pos + paramLen;
		}

		return method;
	}


	/**
	 * getReturnValue() returns return value object
	 */
	public Object getReturnValue(Class<?> retType, Class<?> retGenTypeVal) {

		// Receive return value and return it to caller
		// Now just strip off the object ID and method ID
		int headerLen = IoTRMIUtil.OBJECT_ID_LEN + IoTRMIUtil.METHOD_ID_LEN + IoTRMIUtil.PACKET_TYPE_LEN;
		//if (retValueBytes == null)
		//	System.out.println("retValueBytes is null!");

		int valByteLen = retValueBytes.length - headerLen;
		byte[] retValBytes = new byte[valByteLen];
		// Method Id is positioned after object Id in the byte array

		//System.out.println("Val byte len: " + valByteLen);
		//System.out.println("Length retValBytes: " + retValBytes.length);

		//System.arraycopy(retValueBytes, headerLen, retValBytes, 0, valByteLen);
		//Object retObj = IoTRMIUtil.getParamObject(retType, retGenTypeVal, retValBytes);
		Object retObj = null;
		if (valByteLen != 0) {
			System.arraycopy(retValueBytes, headerLen, retValBytes, 0, valByteLen);
			retObj = IoTRMIUtil.getParamObject(retType, retGenTypeVal, retValBytes);
		}
		// This means the right object and method have gotten the return value, so we set this back to false
		return retObj;
	}


	/**
	 * getStructObjects() calls a method remotely by passing in parameters and getting a return Object
	 */
	public Object[] getStructObjects(Class<?>[] retType, Class<?>[] retGenTypeVal) {

		// Receive return value and return it to caller
		// Now just strip off the object ID and method ID
		int headerLen = IoTRMIUtil.OBJECT_ID_LEN + IoTRMIUtil.METHOD_ID_LEN + IoTRMIUtil.PACKET_TYPE_LEN;
		int valByteLen = retValueBytes.length - headerLen;
		byte[] retValBytes = new byte[valByteLen];
		// Method Id is positioned after object Id in the byte array
		System.arraycopy(retValueBytes, headerLen, retValBytes, 0, valByteLen);
		Object[] retObj = getReturnObjects(retValBytes, retType, retGenTypeVal);

		return retObj;
	}


	/**
	 * remoteCall() calls a method remotely by passing in parameters and getting a return Object
	 */
	public Object[] getReturnObjects(byte[] retBytes, Class<?>[] arrCls, Class<?>[] arrGenValCls) {

		// Byte scanning position
		int pos = 0;
		Object[] retObj = new Object[arrCls.length];
		for (int i=0; i < arrCls.length; i++) {

			String retType = arrCls[i].getSimpleName();
			int retSize = rmiUtil.getTypeSize(retType);
			// Get the 32-bit field in the byte array to get the actual
			// 		length (this is a param with indefinite length)
			if (retSize == -1) {
				byte[] bytRetLen = new byte[IoTRMIUtil.RETURN_LEN];
				System.arraycopy(retBytes, pos, bytRetLen, 0, IoTRMIUtil.RETURN_LEN);
				pos = pos + IoTRMIUtil.RETURN_LEN;
				retSize = IoTRMIUtil.byteArrayToInt(bytRetLen);
			}
			byte[] retObjBytes = new byte[retSize];
			System.arraycopy(retBytes, pos, retObjBytes, 0, retSize);
			pos = pos + retSize;
			retObj[i] = IoTRMIUtil.getParamObject(arrCls[i], arrGenValCls[i], retObjBytes);
		}

		return retObj;
	}

}
