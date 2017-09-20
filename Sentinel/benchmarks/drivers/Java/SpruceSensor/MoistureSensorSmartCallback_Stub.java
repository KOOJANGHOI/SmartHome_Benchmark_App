package iotcode.SpruceSensor;

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

public class MoistureSensorSmartCallback_Stub implements MoistureSensorSmartCallback {

	private int objectId = 6;
	private IoTRMIComm rmiComm;
	// Synchronization variables
	

	public MoistureSensorSmartCallback_Stub(int _localPortSend, int _localPortRecv, int _portSend, int _portRecv, String _skeletonAddress, int _rev) throws Exception {
		if (_localPortSend != 0 && _localPortRecv != 0) {
			rmiComm = new IoTRMICommClient(_localPortSend, _localPortRecv, _portSend, _portRecv, _skeletonAddress, _rev);
		} else
		{
			rmiComm = new IoTRMICommClient(_portSend, _portRecv, _skeletonAddress, _rev);
		}
		IoTRMIUtil.mapStub.put(objectId, this);
	}

	public MoistureSensorSmartCallback_Stub(IoTRMIComm _rmiComm, int _objectId) throws Exception {
		rmiComm = _rmiComm;
		objectId = _objectId;
	}

	public void newReadingAvailable(int sensorId, float moisture, long timeStampOfLastReading) {
		int methodId = 0;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] { int.class, float.class, long.class };
		Object[] paramObj = new Object[] { sensorId, moisture, timeStampOfLastReading };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

}
