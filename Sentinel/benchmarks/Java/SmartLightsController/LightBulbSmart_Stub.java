package SmartLightsController;

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

public class LightBulbSmart_Stub implements LightBulbSmart {

	private int objectId = 1;
	private IoTRMIComm rmiComm;
	// Synchronization variables
	private AtomicBoolean retValueReceived10 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived3 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived11 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived8 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived12 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived7 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived13 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived9 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived6 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived16 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived17 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived14 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived15 = new AtomicBoolean(false);
	

	public LightBulbSmart_Stub(int _localPortSend, int _localPortRecv, int _portSend, int _portRecv, String _skeletonAddress, int _rev) throws Exception {
		if (_localPortSend != 0 && _localPortRecv != 0) {
			rmiComm = new IoTRMICommClient(_localPortSend, _localPortRecv, _portSend, _portRecv, _skeletonAddress, _rev);
		} else
		{
			rmiComm = new IoTRMICommClient(_portSend, _portRecv, _skeletonAddress, _rev);
		}
		rmiComm.registerStub(objectId, 10, retValueReceived10);
		rmiComm.registerStub(objectId, 3, retValueReceived3);
		rmiComm.registerStub(objectId, 11, retValueReceived11);
		rmiComm.registerStub(objectId, 8, retValueReceived8);
		rmiComm.registerStub(objectId, 12, retValueReceived12);
		rmiComm.registerStub(objectId, 7, retValueReceived7);
		rmiComm.registerStub(objectId, 13, retValueReceived13);
		rmiComm.registerStub(objectId, 9, retValueReceived9);
		rmiComm.registerStub(objectId, 6, retValueReceived6);
		rmiComm.registerStub(objectId, 16, retValueReceived16);
		rmiComm.registerStub(objectId, 17, retValueReceived17);
		rmiComm.registerStub(objectId, 14, retValueReceived14);
		rmiComm.registerStub(objectId, 15, retValueReceived15);
		IoTRMIUtil.mapStub.put(objectId, this);
	}

	public LightBulbSmart_Stub(IoTRMIComm _rmiComm, int _objectId) throws Exception {
		rmiComm = _rmiComm;
		objectId = _objectId;
		rmiComm.registerStub(objectId, 10, retValueReceived10);
		rmiComm.registerStub(objectId, 3, retValueReceived3);
		rmiComm.registerStub(objectId, 11, retValueReceived11);
		rmiComm.registerStub(objectId, 8, retValueReceived8);
		rmiComm.registerStub(objectId, 12, retValueReceived12);
		rmiComm.registerStub(objectId, 7, retValueReceived7);
		rmiComm.registerStub(objectId, 13, retValueReceived13);
		rmiComm.registerStub(objectId, 9, retValueReceived9);
		rmiComm.registerStub(objectId, 6, retValueReceived6);
		rmiComm.registerStub(objectId, 16, retValueReceived16);
		rmiComm.registerStub(objectId, 17, retValueReceived17);
		rmiComm.registerStub(objectId, 14, retValueReceived14);
		rmiComm.registerStub(objectId, 15, retValueReceived15);
	}

	public void turnOn() {
		int methodId = 2;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public double getBrightnessRangeLowerBound() {
		int methodId = 10;
		Class<?> retType = double.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived10.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived10.set(false);
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

	public double getBrightnessRangeUpperBound() {
		int methodId = 11;
		Class<?> retType = double.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived11.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived11.set(false);
		rmiComm.setGetReturnBytes();

		return (double)retObj;
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

	public double getHueRangeLowerBound() {
		int methodId = 12;
		Class<?> retType = double.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived12.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived12.set(false);
		rmiComm.setGetReturnBytes();

		return (double)retObj;
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

	public double getHueRangeUpperBound() {
		int methodId = 13;
		Class<?> retType = double.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived13.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived13.set(false);
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

	public int getTemperatureRangeLowerBound() {
		int methodId = 16;
		Class<?> retType = int.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived16.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived16.set(false);
		rmiComm.setGetReturnBytes();

		return (int)retObj;
	}

	public int getTemperatureRangeUpperBound() {
		int methodId = 17;
		Class<?> retType = int.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived17.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived17.set(false);
		rmiComm.setGetReturnBytes();

		return (int)retObj;
	}

	public void setColor(double _hue, double _saturation, double _brightness) {
		int methodId = 4;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] { double.class, double.class, double.class };
		Object[] paramObj = new Object[] { _hue, _saturation, _brightness };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public void init() {
		int methodId = 0;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public double getSaturationRangeLowerBound() {
		int methodId = 14;
		Class<?> retType = double.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived14.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived14.set(false);
		rmiComm.setGetReturnBytes();

		return (double)retObj;
	}

	public double getSaturationRangeUpperBound() {
		int methodId = 15;
		Class<?> retType = double.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived15.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived15.set(false);
		rmiComm.setGetReturnBytes();

		return (double)retObj;
	}

	public void setTemperature(int _temperature) {
		int methodId = 5;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] { int.class };
		Object[] paramObj = new Object[] { _temperature };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

}
