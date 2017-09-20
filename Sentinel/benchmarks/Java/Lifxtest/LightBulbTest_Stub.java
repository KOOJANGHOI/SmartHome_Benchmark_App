package Lifxtest;

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

import iotcode.interfaces.LightBulbTest;

public class LightBulbTest_Stub implements LightBulbTest {

	private int objectId = 1;
	private IoTRMIComm rmiComm;
	// Synchronization variables
	private AtomicBoolean retValueReceived6 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived3 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived8 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived7 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived9 = new AtomicBoolean(false);
	

	public LightBulbTest_Stub(int _localPortSend, int _localPortRecv, int _portSend, int _portRecv, String _skeletonAddress, int _rev) throws Exception {
		if (_localPortSend != 0 && _localPortRecv != 0) {
			rmiComm = new IoTRMICommClient(_localPortSend, _localPortRecv, _portSend, _portRecv, _skeletonAddress, _rev);
		} else
		{
			rmiComm = new IoTRMICommClient(_portSend, _portRecv, _skeletonAddress, _rev);
		}
		rmiComm.registerStub(objectId, 6, retValueReceived6);
		rmiComm.registerStub(objectId, 3, retValueReceived3);
		rmiComm.registerStub(objectId, 8, retValueReceived8);
		rmiComm.registerStub(objectId, 7, retValueReceived7);
		rmiComm.registerStub(objectId, 9, retValueReceived9);
		IoTRMIUtil.mapStub.put(objectId, this);
	}

	public LightBulbTest_Stub(IoTRMIComm _rmiComm, int _objectId) throws Exception {
		rmiComm = _rmiComm;
		objectId = _objectId;
		rmiComm.registerStub(objectId, 6, retValueReceived6);
		rmiComm.registerStub(objectId, 3, retValueReceived3);
		rmiComm.registerStub(objectId, 8, retValueReceived8);
		rmiComm.registerStub(objectId, 7, retValueReceived7);
		rmiComm.registerStub(objectId, 9, retValueReceived9);
	}

	public void turnOn() {
		int methodId = 2;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public double getBrightness() {
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

	public void turnOff() {
		int methodId = 1;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public boolean getState() {
		int methodId = 3;
		Class<?> retType = boolean.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived3.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived3.set(false);
		rmiComm.setGetReturnBytes();

		return (boolean)retObj;
	}

	public void setColor(double _hue, double _saturation, double _brightness) {
		int methodId = 4;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] { double.class, double.class, double.class };
		Object[] paramObj = new Object[] { _hue, _saturation, _brightness };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public double getSaturation() {
		int methodId = 8;
		Class<?> retType = double.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived8.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived8.set(false);
		rmiComm.setGetReturnBytes();

		return (double)retObj;
	}

	public void init() {
		int methodId = 0;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public void setTemperature(int _temperature) {
		int methodId = 5;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] { int.class };
		Object[] paramObj = new Object[] { _temperature };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public double getHue() {
		int methodId = 7;
		Class<?> retType = double.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived7.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived7.set(false);
		rmiComm.setGetReturnBytes();

		return (double)retObj;
	}

	public int getTemperature() {
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

}
