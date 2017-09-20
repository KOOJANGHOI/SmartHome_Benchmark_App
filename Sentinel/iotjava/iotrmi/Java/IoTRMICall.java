package iotrmi.Java;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;

import java.util.HashSet;
import java.util.Set;


/** Class IoTRMICall is a class that serves method calls on stub.
 *  <p>
 *  A stub will use an object of this class to send the method
 *  information, e.g. object identifier, method identifier, and
 *  parameters.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2016-10-04
 */
public final class IoTRMICall {


	/**
	 * Class Properties
	 */
	private IoTRMIUtil rmiUtil;
	private IoTSocketClient rmiClient;


	/**
	 * Constructors
	 */
	public IoTRMICall(int _port, String _address, int _rev) throws IOException {

		rmiUtil = new IoTRMIUtil();
		rmiClient = new IoTSocketClient(_port, _address, _rev);
	}


	/**
	 * remoteCall() calls a method remotely by passing in parameters and getting a return Object
	 */
	public synchronized Object remoteCall(int objectId, int methodId, Class<?> retType, 
			Class<?> retGenTypeVal, Class<?>[] paramCls, Object[] paramObj) {

		// Send method info
		byte[] methodBytes = methodToBytes(objectId, methodId, paramCls, paramObj);
		try {
			rmiClient.sendBytes(methodBytes);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new Error("IoTRMICall: Error when sending bytes - rmiClient.sendBytes()");
		}
		// Receive return value and return it to caller
		Object retObj = null;
		if (retType != void.class) {
			byte[] retObjBytes = null;
			try {
				retObjBytes = rmiClient.receiveBytes(retObjBytes);
			} catch (IOException ex) {
				ex.printStackTrace();
				throw new Error("IoTRMICall: Error when receiving bytes - rmiClient.receiveBytes()");
			}
			retObj = IoTRMIUtil.getParamObject(retType, retGenTypeVal, retObjBytes);
		}
		return retObj;
	}


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
	 * remoteCall() calls a method remotely by passing in parameters and getting a return Object
	 */
	public synchronized Object[] getStructObjects(Class<?>[] retType, Class<?>[] retGenTypeVal) {

		// Receive return value and return it to caller
		Object[] retObj = null;
		byte[] retObjBytes = null;
		try {
			retObjBytes = rmiClient.receiveBytes(retObjBytes);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new Error("IoTRMICall: Error when receiving bytes - rmiClient.receiveBytes()");
		}
		retObj = getReturnObjects(retObjBytes, retType, retGenTypeVal);

		return retObj;
	}


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
