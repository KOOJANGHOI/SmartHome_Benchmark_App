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

public class WeatherGatewaySmart_Stub implements WeatherGatewaySmart {

	private int objectId = 3;
	private IoTRMIComm rmiComm;
	// Synchronization variables
	private AtomicBoolean retValueReceived3 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived6 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived5 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived4 = new AtomicBoolean(false);
	

	public WeatherGatewaySmart_Stub(int _localPortSend, int _localPortRecv, int _portSend, int _portRecv, String _skeletonAddress, int _rev) throws Exception {
		if (_localPortSend != 0 && _localPortRecv != 0) {
			rmiComm = new IoTRMICommClient(_localPortSend, _localPortRecv, _portSend, _portRecv, _skeletonAddress, _rev);
		} else
		{
			rmiComm = new IoTRMICommClient(_portSend, _portRecv, _skeletonAddress, _rev);
		}
		rmiComm.registerStub(objectId, 3, retValueReceived3);
		rmiComm.registerStub(objectId, 6, retValueReceived6);
		rmiComm.registerStub(objectId, 5, retValueReceived5);
		rmiComm.registerStub(objectId, 4, retValueReceived4);
		IoTRMIUtil.mapStub.put(objectId, this);
	}

	public WeatherGatewaySmart_Stub(IoTRMIComm _rmiComm, int _objectId) throws Exception {
		rmiComm = _rmiComm;
		objectId = _objectId;
		rmiComm.registerStub(objectId, 3, retValueReceived3);
		rmiComm.registerStub(objectId, 6, retValueReceived6);
		rmiComm.registerStub(objectId, 5, retValueReceived5);
		rmiComm.registerStub(objectId, 4, retValueReceived4);
	}

	public double getInchesPerWeek() {
		int methodId = 3;
		Class<?> retType = double.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived3.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived3.set(false);
		rmiComm.setGetReturnBytes();

		return (double)retObj;
	}

	public double getInchesPerMinute() {
		int methodId = 6;
		Class<?> retType = double.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived6.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived6.set(false);
		rmiComm.setGetReturnBytes();

		return (double)retObj;
	}

	public int getDaysToWaterOn() {
		int methodId = 5;
		Class<?> retType = int.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived5.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived5.set(false);
		rmiComm.setGetReturnBytes();

		return (int)retObj;
	}

	public void registerCallback(WeatherGatewayCallback _callbackTo) {
		int[] objIdSent0 = new int[1];
		try {
			if (!IoTRMIUtil.mapSkel.containsKey(_callbackTo)) {
				int newObjIdSent = rmiComm.getObjectIdCounter();
				objIdSent0[0] = newObjIdSent;
				rmiComm.decrementObjectIdCounter();
				WeatherGatewayCallback_Skeleton skel0 = new WeatherGatewayCallback_Skeleton(_callbackTo, rmiComm, newObjIdSent);
				IoTRMIUtil.mapSkel.put(_callbackTo, skel0);
				IoTRMIUtil.mapSkelId.put(_callbackTo, newObjIdSent);
				Thread thread = new Thread() {
					public void run() {
						try {
							skel0.___waitRequestInvokeMethod();
							} catch (Exception ex) {
							ex.printStackTrace();
							throw new Error("Exception when trying to run ___waitRequestInvokeMethod() for WeatherGatewayCallback_Skeleton!");
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

		int methodId = 7;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] { int[].class };
		Object[] paramObj = new Object[] { objIdSent0 };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
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

	public void init() {
		int methodId = 0;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public int getWeatherZipCode() {
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

}
