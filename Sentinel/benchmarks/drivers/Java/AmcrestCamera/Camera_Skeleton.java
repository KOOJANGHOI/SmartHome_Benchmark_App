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

public class Camera_Skeleton implements Camera {

	private Camera mainObj;
	private int objectId = 2;
	// Communications and synchronizations
	private IoTRMIComm rmiComm;
	private AtomicBoolean didAlreadyInitWaitInvoke;
	private AtomicBoolean methodReceived;
	private byte[] methodBytes = null;
	// Permissions
	private static Integer[] object2Permission = { 8, 7, 9, 6, 2, 1, 4, 3, 0, 10, 5 };
	private static List<Integer> set2Allowed;
	

	public Camera_Skeleton(Camera _mainObj, int _portSend, int _portRecv) throws Exception {
		mainObj = _mainObj;
		rmiComm = new IoTRMICommServer(_portSend, _portRecv);
		set2Allowed = new ArrayList<Integer>(Arrays.asList(object2Permission));
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

	public Camera_Skeleton(Camera _mainObj, IoTRMIComm _rmiComm, int _objectId) throws Exception {
		mainObj = _mainObj;
		rmiComm = _rmiComm;
		objectId = _objectId;
		set2Allowed = new ArrayList<Integer>(Arrays.asList(object2Permission));
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

	public byte[] getLatestFrame() {
		return mainObj.getLatestFrame();
	}

	public long getTimestamp() {
		return mainObj.getTimestamp();
	}

	public List<Resolution> getSupportedResolutions() {
		return mainObj.getSupportedResolutions();
	}

	public boolean setResolution(Resolution _res) {
		return mainObj.setResolution(_res);
	}

	public boolean setFPS(int _fps) {
		return mainObj.setFPS(_fps);
	}

	public int getMaxFPS() {
		return mainObj.getMaxFPS();
	}

	public int getMinFPS() {
		return mainObj.getMinFPS();
	}

	public void registerCallback(CameraSmartCallback _callbackTo) {
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

	public void ___getLatestFrame() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getLatestFrame();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getTimestamp() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getTimestamp();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getSupportedResolutions() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		List<Resolution> retEnum = getSupportedResolutions();
		int retLen = retEnum.size();
		int[] retEnumVal = new int[retLen];
		for (int i = 0; i < retLen; i++) {
			retEnumVal[i] = retEnum.get(i).ordinal();
		}
		Object retObj = retEnumVal;
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___setResolution() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { int[].class }, new Class<?>[] { null }, localMethodBytes);
		int paramInt0[] = (int[]) paramObj[0];
		Resolution[] enumVals = Resolution.values();
		Resolution paramEnum0 = enumVals[paramInt0[0]];
		Object retObj = setResolution(paramEnum0);
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___setFPS() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { int.class }, new Class<?>[] { null }, localMethodBytes);
		Object retObj = setFPS((int) paramObj[0]);
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getMaxFPS() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getMaxFPS();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getMinFPS() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getMinFPS();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___registerCallback() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { int[].class }, new Class<?>[] { null }, localMethodBytes);
		try {
			int[] stubIdArray0 = (int[]) paramObj[0];
			int objIdRecv0 = stubIdArray0[0];
			CameraSmartCallback newStub0 = null;
			if(!IoTRMIUtil.mapStub.containsKey(objIdRecv0)) {
				newStub0 = new CameraSmartCallback_Stub(rmiComm, objIdRecv0);
				IoTRMIUtil.mapStub.put(objIdRecv0, newStub0);
				rmiComm.setObjectIdCounter(objIdRecv0);
				rmiComm.decrementObjectIdCounter();
			}
			else {
				newStub0 = (CameraSmartCallback_Stub) IoTRMIUtil.mapStub.get(objIdRecv0);
			}
			CameraSmartCallback stub0 = newStub0;
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
				if (!set2Allowed.contains(methodId)) {
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
							___getLatestFrame();
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
							___getTimestamp();
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
							___getSupportedResolutions();
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
							___setResolution();
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
							___setFPS();
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
							___getMaxFPS();
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
							___getMinFPS();
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
