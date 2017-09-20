package IrrigationController;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import iotrmi.Java.IoTRMIComm;
import iotrmi.Java.IoTRMICommClient;
import iotrmi.Java.IoTRMICommServer;
import iotrmi.Java.IoTRMIUtil;

import iotcode.interfaces.*;

public class CameraSmart_Stub implements CameraSmart {

	private int objectId = 2;
	private IoTRMIComm rmiComm;
	// Synchronization variables
	private AtomicBoolean retValueReceived8 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived7 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived9 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived6 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived4 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived3 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived5 = new AtomicBoolean(false);
	

	public CameraSmart_Stub(int _localPortSend, int _localPortRecv, int _portSend, int _portRecv, String _skeletonAddress, int _rev) throws Exception {
		if (_localPortSend != 0 && _localPortRecv != 0) {
			rmiComm = new IoTRMICommClient(_localPortSend, _localPortRecv, _portSend, _portRecv, _skeletonAddress, _rev);
		} else
		{
			rmiComm = new IoTRMICommClient(_portSend, _portRecv, _skeletonAddress, _rev);
		}
		rmiComm.registerStub(objectId, 8, retValueReceived8);
		rmiComm.registerStub(objectId, 7, retValueReceived7);
		rmiComm.registerStub(objectId, 9, retValueReceived9);
		rmiComm.registerStub(objectId, 6, retValueReceived6);
		rmiComm.registerStub(objectId, 4, retValueReceived4);
		rmiComm.registerStub(objectId, 3, retValueReceived3);
		rmiComm.registerStub(objectId, 5, retValueReceived5);
		IoTRMIUtil.mapStub.put(objectId, this);
	}

	public CameraSmart_Stub(IoTRMIComm _rmiComm, int _objectId) throws Exception {
		rmiComm = _rmiComm;
		objectId = _objectId;
		rmiComm.registerStub(objectId, 8, retValueReceived8);
		rmiComm.registerStub(objectId, 7, retValueReceived7);
		rmiComm.registerStub(objectId, 9, retValueReceived9);
		rmiComm.registerStub(objectId, 6, retValueReceived6);
		rmiComm.registerStub(objectId, 4, retValueReceived4);
		rmiComm.registerStub(objectId, 3, retValueReceived3);
		rmiComm.registerStub(objectId, 5, retValueReceived5);
	}

	public int getMaxFPS() {
		int methodId = 8;
		Class<?> retType = int.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived8.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived8.set(false);
		rmiComm.setGetReturnBytes();

		return (int)retObj;
	}

	public boolean setFPS(int _fps) {
		int methodId = 7;
		Class<?> retType = boolean.class;
		Class<?>[] paramCls = new Class<?>[] { int.class };
		Object[] paramObj = new Object[] { _fps };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived7.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived7.set(false);
		rmiComm.setGetReturnBytes();

		return (boolean)retObj;
	}

	public int getMinFPS() {
		int methodId = 9;
		Class<?> retType = int.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived9.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived9.set(false);
		rmiComm.setGetReturnBytes();

		return (int)retObj;
	}

	public boolean setResolution(Resolution _res) {
		int methodId = 6;
		Class<?> retType = boolean.class;
		int paramEnum0[] = new int[1];
		paramEnum0[0] = _res.ordinal();
		Class<?>[] paramCls = new Class<?>[] { int[].class };
		Object[] paramObj = new Object[] { paramEnum0 };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived6.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived6.set(false);
		rmiComm.setGetReturnBytes();

		return (boolean)retObj;
	}

	public void stop() {
		int methodId = 2;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public void start() {
		int methodId = 1;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public long getTimestamp() {
		int methodId = 4;
		Class<?> retType = long.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived4.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived4.set(false);
		rmiComm.setGetReturnBytes();

		return (long)retObj;
	}

	public byte[] getLatestFrame() {
		int methodId = 3;
		Class<?> retType = byte[].class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived3.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived3.set(false);
		rmiComm.setGetReturnBytes();

		return (byte[])retObj;
	}

	public void init() {
		int methodId = 0;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public void registerCallback(CameraCallback _callbackTo) {
		int[] objIdSent0 = new int[1];
		try {
			if (!IoTRMIUtil.mapSkel.containsKey(_callbackTo)) {
				int newObjIdSent = rmiComm.getObjectIdCounter();
				objIdSent0[0] = newObjIdSent;
				rmiComm.decrementObjectIdCounter();
				CameraCallback_Skeleton skel0 = new CameraCallback_Skeleton(_callbackTo, rmiComm, newObjIdSent);
				IoTRMIUtil.mapSkel.put(_callbackTo, skel0);
				IoTRMIUtil.mapSkelId.put(_callbackTo, newObjIdSent);
				Thread thread = new Thread() {
					public void run() {
						try {
							skel0.___waitRequestInvokeMethod();
							} catch (Exception ex) {
							ex.printStackTrace();
							throw new Error("Exception when trying to run ___waitRequestInvokeMethod() for CameraCallback_Skeleton!");
						}
					}
				};
				thread.start();
				while(!skel0.didAlreadyInitWaitInvoke());
			}
			else
			{
				int newObjIdSent = IoTRMIUtil.mapSkelId.get(_callbackTo);
				objIdSent0[0] = newObjIdSent;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new Error("Exception when generating skeleton objects!");
		}

		int methodId = 10;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] { int[].class };
		Object[] paramObj = new Object[] { objIdSent0 };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public List<Resolution> getSupportedResolutions() {
		int methodId = 5;
		Class<?> retType = int[].class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived5.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived5.set(false);
		rmiComm.setGetReturnBytes();

		int[] retEnum = (int[]) retObj;
		Resolution[] enumVals = Resolution.values();
		int retLen = retEnum.length;
		List<Resolution> enumRetVal = new ArrayList<Resolution>();
		for (int i = 0; i < retLen; i++) {
			enumRetVal.add(enumVals[retEnum[i]]);
		}
		return enumRetVal;
	}

}
