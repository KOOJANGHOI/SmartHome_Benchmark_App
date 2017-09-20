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

public class MoistureSensorSmart_Stub implements MoistureSensorSmart {

	private int objectId = 5;
	private IoTRMIComm rmiComm;
	// Synchronization variables
	private AtomicBoolean retValueReceived2 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived4 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived1 = new AtomicBoolean(false);
	

	public MoistureSensorSmart_Stub(int _localPortSend, int _localPortRecv, int _portSend, int _portRecv, String _skeletonAddress, int _rev) throws Exception {
		if (_localPortSend != 0 && _localPortRecv != 0) {
			rmiComm = new IoTRMICommClient(_localPortSend, _localPortRecv, _portSend, _portRecv, _skeletonAddress, _rev);
		} else
		{
			rmiComm = new IoTRMICommClient(_portSend, _portRecv, _skeletonAddress, _rev);
		}
		rmiComm.registerStub(objectId, 2, retValueReceived2);
		rmiComm.registerStub(objectId, 4, retValueReceived4);
		rmiComm.registerStub(objectId, 1, retValueReceived1);
		IoTRMIUtil.mapStub.put(objectId, this);
	}

	public MoistureSensorSmart_Stub(IoTRMIComm _rmiComm, int _objectId) throws Exception {
		rmiComm = _rmiComm;
		objectId = _objectId;
		rmiComm.registerStub(objectId, 2, retValueReceived2);
		rmiComm.registerStub(objectId, 4, retValueReceived4);
		rmiComm.registerStub(objectId, 1, retValueReceived1);
	}

	public long getTimestampOfLastReading() {
		int methodId = 2;
		Class<?> retType = long.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived2.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived2.set(false);
		rmiComm.setGetReturnBytes();

		return (long)retObj;
	}

	public int getId() {
		int methodId = 4;
		Class<?> retType = int.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived4.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived4.set(false);
		rmiComm.setGetReturnBytes();

		return (int)retObj;
	}

	public void registerCallback(MoistureSensorCallback _callbackTo) {
		int[] objIdSent0 = new int[1];
		try {
			if (!IoTRMIUtil.mapSkel.containsKey(_callbackTo)) {
				int newObjIdSent = rmiComm.getObjectIdCounter();
				objIdSent0[0] = newObjIdSent;
				rmiComm.decrementObjectIdCounter();
				MoistureSensorCallback_Skeleton skel0 = new MoistureSensorCallback_Skeleton(_callbackTo, rmiComm, newObjIdSent);
				IoTRMIUtil.mapSkel.put(_callbackTo, skel0);
				IoTRMIUtil.mapSkelId.put(_callbackTo, newObjIdSent);
				Thread thread = new Thread() {
					public void run() {
						try {
							skel0.___waitRequestInvokeMethod();
							} catch (Exception ex) {
							ex.printStackTrace();
							throw new Error("Exception when trying to run ___waitRequestInvokeMethod() for MoistureSensorCallback_Skeleton!");
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

		int methodId = 5;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] { int[].class };
		Object[] paramObj = new Object[] { objIdSent0 };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public float getMoisture() {
		int methodId = 1;
		Class<?> retType = float.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived1.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived1.set(false);
		rmiComm.setGetReturnBytes();

		return (float)retObj;
	}

	public void setId(int id) {
		int methodId = 3;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] { int.class };
		Object[] paramObj = new Object[] { id };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public void init() {
		int methodId = 0;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

}
