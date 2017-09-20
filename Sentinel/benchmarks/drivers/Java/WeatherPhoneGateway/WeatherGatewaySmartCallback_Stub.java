package iotcode.WeatherPhoneGateway;

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

public class WeatherGatewaySmartCallback_Stub implements WeatherGatewaySmartCallback {

	private int objectId = 4;
	private IoTRMIComm rmiComm;
	// Synchronization variables
	

	public WeatherGatewaySmartCallback_Stub(int _localPortSend, int _localPortRecv, int _portSend, int _portRecv, String _skeletonAddress, int _rev) throws Exception {
		if (_localPortSend != 0 && _localPortRecv != 0) {
			rmiComm = new IoTRMICommClient(_localPortSend, _localPortRecv, _portSend, _portRecv, _skeletonAddress, _rev);
		} else
		{
			rmiComm = new IoTRMICommClient(_portSend, _portRecv, _skeletonAddress, _rev);
		}
		IoTRMIUtil.mapStub.put(objectId, this);
	}

	public WeatherGatewaySmartCallback_Stub(IoTRMIComm _rmiComm, int _objectId) throws Exception {
		rmiComm = _rmiComm;
		objectId = _objectId;
	}

	public void informationRetrieved(double _inchesPerWeek, int _weatherZipCode, int _daysToWaterOn, double _inchesPerMinute) {
		int methodId = 0;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] { double.class, int.class, int.class, double.class };
		Object[] paramObj = new Object[] { _inchesPerWeek, _weatherZipCode, _daysToWaterOn, _inchesPerMinute };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

}
