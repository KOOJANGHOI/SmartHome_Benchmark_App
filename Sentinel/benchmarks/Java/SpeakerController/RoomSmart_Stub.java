package SpeakerController;

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

public class RoomSmart_Stub implements RoomSmart {

	private int objectId = 4;
	private IoTRMIComm rmiComm;
	// Synchronization variables
	private AtomicBoolean retValueReceived0 = new AtomicBoolean(false);
	

	public RoomSmart_Stub(int _localPortSend, int _localPortRecv, int _portSend, int _portRecv, String _skeletonAddress, int _rev) throws Exception {
		if (_localPortSend != 0 && _localPortRecv != 0) {
			rmiComm = new IoTRMICommClient(_localPortSend, _localPortRecv, _portSend, _portRecv, _skeletonAddress, _rev);
		} else
		{
			rmiComm = new IoTRMICommClient(_portSend, _portRecv, _skeletonAddress, _rev);
		}
		rmiComm.registerStub(objectId, 0, retValueReceived0);
		IoTRMIUtil.mapStub.put(objectId, this);
	}

	public RoomSmart_Stub(IoTRMIComm _rmiComm, int _objectId) throws Exception {
		rmiComm = _rmiComm;
		objectId = _objectId;
		rmiComm.registerStub(objectId, 0, retValueReceived0);
	}

	public int getRoomID() {
		int methodId = 0;
		Class<?> retType = int.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived0.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived0.set(false);
		rmiComm.setGetReturnBytes();

		return (int)retObj;
	}

}
