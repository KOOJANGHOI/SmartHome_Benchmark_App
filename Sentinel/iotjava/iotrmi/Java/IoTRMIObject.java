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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/** Class IoTRMIObject is a class that stores info of an object.
 *  <p>
 *  It stores object ID, methods, method ID, method's signature 
 *  and parameters.
 *  This class also receive calls from different objects as they
 *  ask to execute certain methods remotely. This will have the 
 *  execution result (return value) sent back to 
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2016-10-03
 */
public final class IoTRMIObject {

	/**
	 * Class Properties
	 */
	private List<String> listMethodId2Sign;	// List of method signature (we use list index as method Id)
	private IoTRMIUtil rmiUtil;
	private IoTSocketServer rmiServer;
	private byte[] methodBytes;
	private Lock lock = new ReentrantLock();


	/**
	 * Constructors
	 */
	public IoTRMIObject(int _port) throws  
		ClassNotFoundException, InstantiationException, 
			IllegalAccessException, IOException {

		rmiUtil = new IoTRMIUtil();
		methodBytes = null;
		rmiServer = new IoTSocketServer(_port);
		rmiServer.connect();
	}


	/**
	 * getMethodBytes() waits for method transmission in bytes
	 */
	public byte[] getMethodBytes() throws IOException {

		// Receive method info
		//System.out.println("Method RMIObj before: " + Arrays.toString(methodBytes));
		methodBytes = rmiServer.receiveBytes(methodBytes);
		//System.out.println("Method RMIObj after: " + Arrays.toString(methodBytes));
		return methodBytes;
	}


	/**
	 * getObjectId() gets object Id from bytes
	 */
	public int getObjectId() {

		// Get object Id bytes
		byte[] objectIdBytes = new byte[IoTRMIUtil.OBJECT_ID_LEN];
		System.arraycopy(methodBytes, 0, objectIdBytes, 0, IoTRMIUtil.OBJECT_ID_LEN);
		// Get object Id
		int objectId = IoTRMIUtil.byteArrayToInt(objectIdBytes);
		return objectId;
	}


	/**
	 * static version of getObjectId()
	 */
	public static int getObjectId(byte[] methodBytes) {

		// Get object Id bytes
		byte[] objectIdBytes = new byte[IoTRMIUtil.OBJECT_ID_LEN];
		System.arraycopy(methodBytes, 0, objectIdBytes, 0, IoTRMIUtil.OBJECT_ID_LEN);
		// Get object Id
		int objectId = IoTRMIUtil.byteArrayToInt(objectIdBytes);
		return objectId;
	}


	/**
	 * setMethodBytes() sets bytes for method
	 */
	/*public void setMethodBytes(byte[] _methodBytes) throws IOException {

		// Set method bytes
		methodBytes = _methodBytes;
	}*/


	/**
	 * getMethodId() gets method Id from bytes
	 */
	public int getMethodId() {

		// Get method Id bytes
		byte[] methodIdBytes = new byte[IoTRMIUtil.METHOD_ID_LEN];
		// Method Id is positioned after object Id in the byte array
		System.arraycopy(methodBytes, IoTRMIUtil.OBJECT_ID_LEN, methodIdBytes, 0, IoTRMIUtil.METHOD_ID_LEN);
		// Get method Id
		int methodId = IoTRMIUtil.byteArrayToInt(methodIdBytes);
		// Get method Id
		return methodId;
	}


	/**
	 * static version of getMethodId()
	 */
	public static int getMethodId(byte[] methodBytes) {

		// Get method Id bytes
		byte[] methodIdBytes = new byte[IoTRMIUtil.METHOD_ID_LEN];
		// Method Id is positioned after object Id in the byte array
		System.arraycopy(methodBytes, IoTRMIUtil.OBJECT_ID_LEN, methodIdBytes, 0, IoTRMIUtil.METHOD_ID_LEN);
		// Get method Id
		int methodId = IoTRMIUtil.byteArrayToInt(methodIdBytes);
		// Get method Id
		return methodId;
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
	public Object[] getMethodParams(Class<?>[] arrCls, Class<?>[] arrGenValCls) {

		// Byte scanning position
		int pos = IoTRMIUtil.OBJECT_ID_LEN + IoTRMIUtil.METHOD_ID_LEN;
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
	 * sendReturnObj() sends back return Object to client
	 */
	public void sendReturnObj(Object retObj) throws IOException {

		// Send back return value
		byte[] retObjBytes = IoTRMIUtil.getObjectBytes(retObj);
		rmiServer.sendBytes(retObjBytes);
	}


	/**
	 * sendReturnObj() overloaded to send multiple return objects for structs
	 */
	public void sendReturnObj(Class<?>[] retCls, Object[] retObj) throws IOException {

		// Send back return value
		byte[] retObjBytes = returnToBytes(retCls, retObj);
		rmiServer.sendBytes(retObjBytes);
	}


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
}
