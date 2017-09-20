package iotcode.EspAlarm;

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

public class Alarm_Skeleton implements Alarm {

	private Alarm mainObj;
	private int objectId = 1;
	// Communications and synchronizations
	private IoTRMIComm rmiComm;
	private AtomicBoolean didAlreadyInitWaitInvoke;
	private AtomicBoolean methodReceived;
	private byte[] methodBytes = null;
	// Permissions
	private static Integer[] object1Permission = { 4, 2, 0, 1, 3 };
	private static List<Integer> set1Allowed;
	

	public Alarm_Skeleton(Alarm _mainObj, int _portSend, int _portRecv) throws Exception {
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

	public Alarm_Skeleton(Alarm _mainObj, IoTRMIComm _rmiComm, int _objectId) throws Exception {
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

	public void setZone(int _zone, boolean _onOff, int _onDurationSeconds) {
		mainObj.setZone(_zone, _onOff, _onDurationSeconds);
	}

	public List<ZoneState> getZoneStates() {
		return mainObj.getZoneStates();
	}

	public int getNumberOfZones() {
		return mainObj.getNumberOfZones();
	}

	public boolean doesHaveZoneTimers() {
		return mainObj.doesHaveZoneTimers();
	}

	public void ___init() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		init();
	}

	public void ___setZone() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { int.class, boolean.class, int.class }, new Class<?>[] { null, null, null }, localMethodBytes);
		setZone((int) paramObj[0], (boolean) paramObj[1], (int) paramObj[2]);
	}

	public void ___getZoneStates() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		List<ZoneState> retStruct = getZoneStates();
		int retLen = retStruct.size();
		Object retLenObj = retLen;
		rmiComm.sendReturnObj(retLenObj, localMethodBytes);
		Class<?>[] retCls = new Class<?>[3*retLen];
		Object[] retObj = new Object[3*retLen];
		int retPos = 0;
		for(int i = 0; i < retLen; i++) {
			retCls[retPos] = int.class;
			retObj[retPos++] = retStruct.get(i).zoneNumber;
			retCls[retPos] = boolean.class;
			retObj[retPos++] = retStruct.get(i).onOffState;
			retCls[retPos] = int.class;
			retObj[retPos++] = retStruct.get(i).duration;
		}
		rmiComm.sendReturnObj(retCls, retObj, localMethodBytes);
	}

	public void ___getNumberOfZones() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getNumberOfZones();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___doesHaveZoneTimers() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = doesHaveZoneTimers();
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
							___setZone();
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
							___getZoneStates();
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
							___getNumberOfZones();
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
							___doesHaveZoneTimers();
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
