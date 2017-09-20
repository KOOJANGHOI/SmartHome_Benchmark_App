package iotcode.LifxLightBulb;

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

import iotcode.interfaces.LightBulb;

public class LightBulb_Skeleton implements LightBulb {

	private LightBulb mainObj;
	private int objectId = 1;
	// Communications and synchronizations
	private IoTRMIComm rmiComm;
	private AtomicBoolean didAlreadyInitWaitInvoke;
	private AtomicBoolean methodReceived;
	private byte[] methodBytes = null;
	// Permissions
	private static Integer[] object1Permission = { 2, 10, 1, 3, 11, 8, 12, 7, 13, 9, 6, 16, 17, 4, 0, 14, 15, 5 };
	private static List<Integer> set1Allowed;
	

	public LightBulb_Skeleton(LightBulb _mainObj, int _portSend, int _portRecv) throws Exception {
		mainObj = _mainObj;
		rmiComm = new IoTRMICommServer(_portSend, _portRecv);
		set1Allowed = new ArrayList<Integer>(Arrays.asList(object1Permission));
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

	public LightBulb_Skeleton(LightBulb _mainObj, IoTRMIComm _rmiComm, int _objectId) throws Exception {
		mainObj = _mainObj;
		rmiComm = _rmiComm;
		objectId = _objectId;
		set1Allowed = new ArrayList<Integer>(Arrays.asList(object1Permission));
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

	public void turnOff() {
		mainObj.turnOff();
	}

	public void turnOn() {
		mainObj.turnOn();
	}

	public boolean getState() {
		return mainObj.getState();
	}

	public void setColor(double _hue, double _saturation, double _brightness) {
		mainObj.setColor(_hue, _saturation, _brightness);
	}

	public void setTemperature(int _temperature) {
		mainObj.setTemperature(_temperature);
	}

	public double getBrightness() {
		return mainObj.getBrightness();
	}

	public double getHue() {
		return mainObj.getHue();
	}

	public double getSaturation() {
		return mainObj.getSaturation();
	}

	public int getTemperature() {
		return mainObj.getTemperature();
	}

	public double getBrightnessRangeLowerBound() {
		return mainObj.getBrightnessRangeLowerBound();
	}

	public double getBrightnessRangeUpperBound() {
		return mainObj.getBrightnessRangeUpperBound();
	}

	public double getHueRangeLowerBound() {
		return mainObj.getHueRangeLowerBound();
	}

	public double getHueRangeUpperBound() {
		return mainObj.getHueRangeUpperBound();
	}

	public double getSaturationRangeLowerBound() {
		return mainObj.getSaturationRangeLowerBound();
	}

	public double getSaturationRangeUpperBound() {
		return mainObj.getSaturationRangeUpperBound();
	}

	public int getTemperatureRangeLowerBound() {
		return mainObj.getTemperatureRangeLowerBound();
	}

	public int getTemperatureRangeUpperBound() {
		return mainObj.getTemperatureRangeUpperBound();
	}

	public void ___init() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		init();
	}

	public void ___turnOff() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		turnOff();
	}

	public void ___turnOn() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		turnOn();
	}

	public void ___getState() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getState();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___setColor() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { double.class, double.class, double.class }, new Class<?>[] { null, null, null }, localMethodBytes);
		setColor((double) paramObj[0], (double) paramObj[1], (double) paramObj[2]);
	}

	public void ___setTemperature() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { int.class }, new Class<?>[] { null }, localMethodBytes);
		setTemperature((int) paramObj[0]);
	}

	public void ___getBrightness() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getBrightness();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getHue() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getHue();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getSaturation() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getSaturation();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getTemperature() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getTemperature();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getBrightnessRangeLowerBound() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getBrightnessRangeLowerBound();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getBrightnessRangeUpperBound() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getBrightnessRangeUpperBound();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getHueRangeLowerBound() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getHueRangeLowerBound();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getHueRangeUpperBound() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getHueRangeUpperBound();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getSaturationRangeLowerBound() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getSaturationRangeLowerBound();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getSaturationRangeUpperBound() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getSaturationRangeUpperBound();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getTemperatureRangeLowerBound() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getTemperatureRangeLowerBound();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getTemperatureRangeUpperBound() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getTemperatureRangeUpperBound();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
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
				if (!set1Allowed.contains(methodId)) {
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
							___turnOff();
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
							___turnOn();
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
							___getState();
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
							___setColor();
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
							___setTemperature();
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
							___getBrightness();
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
							___getHue();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 8:
				new Thread() {
					public void run() {
						try {
							___getSaturation();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 9:
				new Thread() {
					public void run() {
						try {
							___getTemperature();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 10:
				new Thread() {
					public void run() {
						try {
							___getBrightnessRangeLowerBound();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 11:
				new Thread() {
					public void run() {
						try {
							___getBrightnessRangeUpperBound();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 12:
				new Thread() {
					public void run() {
						try {
							___getHueRangeLowerBound();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 13:
				new Thread() {
					public void run() {
						try {
							___getHueRangeUpperBound();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 14:
				new Thread() {
					public void run() {
						try {
							___getSaturationRangeLowerBound();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 15:
				new Thread() {
					public void run() {
						try {
							___getSaturationRangeUpperBound();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 16:
				new Thread() {
					public void run() {
						try {
							___getTemperatureRangeLowerBound();
						}
						catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}.start();
				break;
				case 17:
				new Thread() {
					public void run() {
						try {
							___getTemperatureRangeUpperBound();
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
