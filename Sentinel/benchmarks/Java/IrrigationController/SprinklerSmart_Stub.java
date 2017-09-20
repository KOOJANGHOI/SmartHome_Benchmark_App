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

public class SprinklerSmart_Stub implements SprinklerSmart {

	private int objectId = 6;
	private IoTRMIComm rmiComm;
	// Synchronization variables
	private AtomicBoolean retValueReceived4 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived2 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived3 = new AtomicBoolean(false);
	

	public SprinklerSmart_Stub(int _localPortSend, int _localPortRecv, int _portSend, int _portRecv, String _skeletonAddress, int _rev) throws Exception {
		if (_localPortSend != 0 && _localPortRecv != 0) {
			rmiComm = new IoTRMICommClient(_localPortSend, _localPortRecv, _portSend, _portRecv, _skeletonAddress, _rev);
		} else
		{
			rmiComm = new IoTRMICommClient(_portSend, _portRecv, _skeletonAddress, _rev);
		}
		rmiComm.registerStub(objectId, 4, retValueReceived4);
		rmiComm.registerStub(objectId, 2, retValueReceived2);
		rmiComm.registerStub(objectId, 3, retValueReceived3);
		IoTRMIUtil.mapStub.put(objectId, this);
	}

	public SprinklerSmart_Stub(IoTRMIComm _rmiComm, int _objectId) throws Exception {
		rmiComm = _rmiComm;
		objectId = _objectId;
		rmiComm.registerStub(objectId, 4, retValueReceived4);
		rmiComm.registerStub(objectId, 2, retValueReceived2);
		rmiComm.registerStub(objectId, 3, retValueReceived3);
	}

	public boolean doesHaveZoneTimers() {
		int methodId = 4;
		Class<?> retType = boolean.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived4.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived4.set(false);
		rmiComm.setGetReturnBytes();

		return (boolean)retObj;
	}

	public List<ZoneState> getZoneStates() {
		int methodId = 2;
		Class<?> retType = int.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived2.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived2.set(false);
		rmiComm.setGetReturnBytes();

		int retLen = (int) retObj;
		Class<?>[] retCls = new Class<?>[3*retLen];
		Class<?>[] retClsVal = new Class<?>[3*retLen];
		int retPos = 0;
		for(int i = 0; i < retLen; i++) {
			retCls[retPos] = int.class;
			retClsVal[retPos++] = null;
			retCls[retPos] = boolean.class;
			retClsVal[retPos++] = null;
			retCls[retPos] = int.class;
			retClsVal[retPos++] = null;
		}
		// Waiting for return value
		while (!retValueReceived2.get());
		Object[] retActualObj = rmiComm.getStructObjects(retCls, retClsVal);
		retValueReceived2.set(false);
		rmiComm.setGetReturnBytes();

		List<ZoneState> structRet = new ArrayList<ZoneState>();
		int retObjPos = 0;
		for(int i = 0; i < retLen; i++) {
			ZoneState structRetMem = new ZoneState();
			structRetMem.zoneNumber = (int) retActualObj[retObjPos++];
			structRetMem.onOffState = (boolean) retActualObj[retObjPos++];
			structRetMem.duration = (int) retActualObj[retObjPos++];
			structRet.add(structRetMem);
		}
		return structRet;
	}

	public void init() {
		int methodId = 0;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public void setZone(int _zone, boolean _onOff, int _onDurationSeconds) {
		int methodId = 1;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] { int.class, boolean.class, int.class };
		Object[] paramObj = new Object[] { _zone, _onOff, _onDurationSeconds };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public int getNumberOfZones() {
		int methodId = 3;
		Class<?> retType = int.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived3.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived3.set(false);
		rmiComm.setGetReturnBytes();

		return (int)retObj;
	}

}
