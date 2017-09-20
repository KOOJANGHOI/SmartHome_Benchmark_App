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

public class WeatherGateway_Skeleton implements WeatherGateway {

	private WeatherGateway mainObj;
	private int objectId = 3;
	// Communications and synchronizations
	private IoTRMIComm rmiComm;
	private AtomicBoolean didAlreadyInitWaitInvoke;
	private AtomicBoolean methodReceived;
	private byte[] methodBytes = null;
	// Permissions
	private static Integer[] object3Permission = { 3, 6, 5, 7, 2, 1, 0, 4 };
	private static List<Integer> set3Allowed;
	

	public WeatherGateway_Skeleton(WeatherGateway _mainObj, int _portSend, int _portRecv) throws Exception {
		mainObj = _mainObj;
		rmiComm = new IoTRMICommServer(_portSend, _portRecv);
		set3Allowed = new ArrayList<Integer>(Arrays.asList(object3Permission));
		IoTRMIUtil.mapSkel.put(_mainObj, this);
		IoTRMIUtil.mapSkelId.put(_mainObj, objectId);
		didAlreadyInitWaitInvoke = new AtomicBoolean(false);
		methodReceived = new AtomicBoolean(false);
		rmiComm.registerSkeleton(objectId, methodReceived);
		Thread thread1 = new Thread() {
			public void run() {
				try {
					___waitRequestInvokeMethod();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		};
		thread1.start();
	}

	public WeatherGateway_Skeleton(WeatherGateway _mainObj, IoTRMIComm _rmiComm, int _objectId) throws Exception {
		mainObj = _mainObj;
		rmiComm = _rmiComm;
		objectId = _objectId;
		set3Allowed = new ArrayList<Integer>(Arrays.asList(object3Permission));
		didAlreadyInitWaitInvoke = new AtomicBoolean(false);
		methodReceived = new AtomicBoolean(false);
		rmiComm.registerSkeleton(objectId, methodReceived);
	}

	public boolean didAlreadyInitWaitInvoke() {
		return didAlreadyInitWaitInvoke.get();
	}

	public void init() {
		mainObj.init();
	}

	public void start() {
		mainObj.start();
	}

	public void stop() {
		mainObj.stop();
	}

	public double getInchesPerWeek() {
		return mainObj.getInchesPerWeek();
	}

	public int getWeatherZipCode() {
		return mainObj.getWeatherZipCode();
	}

	public int getDaysToWaterOn() {
		return mainObj.getDaysToWaterOn();
	}

	public double getInchesPerMinute() {
		return mainObj.getInchesPerMinute();
	}

	public void registerCallback(WeatherGatewaySmartCallback _callbackTo) {
		mainObj.registerCallback(_callbackTo);
	}

	public void ___init() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		init();
	}

	public void ___start() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		start();
	}

	public void ___stop() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		stop();
	}

	public void ___getInchesPerWeek() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getInchesPerWeek();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getWeatherZipCode() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getWeatherZipCode();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getDaysToWaterOn() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getDaysToWaterOn();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getInchesPerMinute() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getInchesPerMinute();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___registerCallback() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { int[].class }, new Class<?>[] { null }, localMethodBytes);
		try {
			int[] stubIdArray0 = (int[]) paramObj[0];
			int objIdRecv0 = stubIdArray0[0];
			WeatherGatewaySmartCallback newStub0 = null;
			if(!IoTRMIUtil.mapStub.containsKey(objIdRecv0)) {
				newStub0 = new WeatherGatewaySmartCallback_Stub(rmiComm, objIdRecv0);
				IoTRMIUtil.mapStub.put(objIdRecv0, newStub0);
				rmiComm.setObjectIdCounter(objIdRecv0);
				rmiComm.decrementObjectIdCounter();
			}
			else {
				newStub0 = (WeatherGatewaySmartCallback_Stub) IoTRMIUtil.mapStub.get(objIdRecv0);
			}
			WeatherGatewaySmartCallback stub0 = newStub0;
			registerCallback(stub0);
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new Error("Exception from callback object instantiation!");
		}
	}

	public void ___waitRequestInvokeMethod() throws IOException {
		didAlreadyInitWaitInvoke.compareAndSet(false, true);
		while (true) {
			if (!methodReceived.get()) {
				continue;
			}
			methodBytes = rmiComm.getMethodBytes();
			methodReceived.set(false);
			int _objectId = IoTRMIComm.getObjectId(methodBytes);
			int methodId = IoTRMIComm.getMethodId(methodBytes);
			if (_objectId == objectId) {
				if (!set3Allowed.contains(methodId)) {
					throw new Error("Object with object Id: " + _objectId + "  is not allowed to access method: " + methodId);
				}
			}
			else {
				continue;
			}
			switch (methodId) {
				case 0:
				new Thread() {
					public void run() {
						try {
							___init();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 1:
				new Thread() {
					public void run() {
						try {
							___start();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 2:
				new Thread() {
					public void run() {
						try {
							___stop();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 3:
				new Thread() {
					public void run() {
						try {
							___getInchesPerWeek();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 4:
				new Thread() {
					public void run() {
						try {
							___getWeatherZipCode();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 5:
				new Thread() {
					public void run() {
						try {
							___getDaysToWaterOn();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 6:
				new Thread() {
					public void run() {
						try {
							___getInchesPerMinute();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 7:
				new Thread() {
					public void run() {
						try {
							___registerCallback();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				default: 
				throw new Error("Method Id " + methodId + " not recognized!");
			}
		}
	}

}
