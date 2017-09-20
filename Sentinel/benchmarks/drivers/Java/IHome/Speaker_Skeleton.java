package iotcode.IHome;

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

public class Speaker_Skeleton implements Speaker {

	private Speaker mainObj;
	private int objectId = 1;
	// Communications and synchronizations
	private IoTRMIComm rmiComm;
	private AtomicBoolean didAlreadyInitWaitInvoke;
	private AtomicBoolean methodReceived;
	private byte[] methodBytes = null;
	// Permissions
	private static Integer[] object1Permission = { 6, 2, 9, 1, 3, 4, 5, 7, 8, 0, 10 };
	private static List<Integer> set1Allowed;
	

	public Speaker_Skeleton(Speaker _mainObj, int _portSend, int _portRecv) throws Exception {
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

	public Speaker_Skeleton(Speaker _mainObj, IoTRMIComm _rmiComm, int _objectId) throws Exception {
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

	public boolean startPlayback() {
		return mainObj.startPlayback();
	}

	public boolean stopPlayback() {
		return mainObj.stopPlayback();
	}

	public boolean getPlaybackState() {
		return mainObj.getPlaybackState();
	}

	public boolean setVolume(float _percent) {
		return mainObj.setVolume(_percent);
	}

	public float getVolume() {
		return mainObj.getVolume();
	}

	public int getPosition() {
		return mainObj.getPosition();
	}

	public void setPosition(int _mSec) {
		mainObj.setPosition(_mSec);
	}

	public void loadData(short _samples[], int _offs, int _len) {
		mainObj.loadData(_samples, _offs, _len);
	}

	public void clearData() {
		mainObj.clearData();
	}

	public void registerCallback(SpeakerSmartCallback _cb) {
		mainObj.registerCallback(_cb);
	}

	public void ___init() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		init();
	}

	public void ___startPlayback() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = startPlayback();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___stopPlayback() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = stopPlayback();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getPlaybackState() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getPlaybackState();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___setVolume() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { float.class }, new Class<?>[] { null }, localMethodBytes);
		Object retObj = setVolume((float) paramObj[0]);
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getVolume() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getVolume();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getPosition() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getPosition();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___setPosition() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { int.class }, new Class<?>[] { null }, localMethodBytes);
		setPosition((int) paramObj[0]);
	}

	public void ___loadData() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { short[].class, int.class, int.class }, new Class<?>[] { null, null, null }, localMethodBytes);
		loadData((short[]) paramObj[0], (int) paramObj[1], (int) paramObj[2]);
	}

	public void ___clearData() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		clearData();
	}

	public void ___registerCallback() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { int[].class }, new Class<?>[] { null }, localMethodBytes);
		try {
			int[] stubIdArray0 = (int[]) paramObj[0];
			int objIdRecv0 = stubIdArray0[0];
			SpeakerSmartCallback newStub0 = null;
			if(!IoTRMIUtil.mapStub.containsKey(objIdRecv0)) {
				newStub0 = new SpeakerSmartCallback_Stub(rmiComm, objIdRecv0);
				IoTRMIUtil.mapStub.put(objIdRecv0, newStub0);
				rmiComm.setObjectIdCounter(objIdRecv0);
				rmiComm.decrementObjectIdCounter();
			}
			else {
				newStub0 = (SpeakerSmartCallback_Stub) IoTRMIUtil.mapStub.get(objIdRecv0);
			}
			SpeakerSmartCallback stub0 = newStub0;
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
							___startPlayback();
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
							___stopPlayback();
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
							___getPlaybackState();
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
							___setVolume();
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
							___getVolume();
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
							___getPosition();
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
							___setPosition();
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
							___loadData();
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
							___clearData();
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
