package iotcode.AmcrestCamera;

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

public class CameraSmartCallback_Stub implements CameraSmartCallback {

	private int objectId = 3;
	private IoTRMIComm rmiComm;
	// Synchronization variables
	

	public CameraSmartCallback_Stub(int _localPortSend, int _localPortRecv, int _portSend, int _portRecv, String _skeletonAddress, int _rev) throws Exception {
		if (_localPortSend != 0 && _localPortRecv != 0) {
			rmiComm = new IoTRMICommClient(_localPortSend, _localPortRecv, _portSend, _portRecv, _skeletonAddress, _rev);
		} else
		{
			rmiComm = new IoTRMICommClient(_portSend, _portRecv, _skeletonAddress, _rev);
		}
		IoTRMIUtil.mapStub.put(objectId, this);
	}

	public CameraSmartCallback_Stub(IoTRMIComm _rmiComm, int _objectId) throws Exception {
		rmiComm = _rmiComm;
		objectId = _objectId;
	}

	public void newCameraFrameAvailable(byte latestFrame[], long timeStamp) {
		int methodId = 0;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] { byte[].class, long.class };
		Object[] paramObj = new Object[] { latestFrame, timeStamp };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

}
