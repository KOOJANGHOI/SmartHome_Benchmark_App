package iotcode.GPSPhoneGateway;

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

public class GPSGateway_Skeleton implements GPSGateway {

	private GPSGateway mainObj;
	private int objectId = 3;
	// Communications and synchronizations
	private IoTRMIComm rmiComm;
	private AtomicBoolean didAlreadyInitWaitInvoke;
	private AtomicBoolean methodReceived;
	private byte[] methodBytes = null;
	// Permissions
	private static Integer[] object3Permission = { 5, 6, 2, 1, 0, 3, 7, 4 };
	private static List<Integer> set3Allowed;
	

	public GPSGateway_Skeleton(GPSGateway _mainObj, int _portSend, int _portRecv) throws Exception {
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

	public GPSGateway_Skeleton(GPSGateway _mainObj, IoTRMIComm _rmiComm, int _objectId) throws Exception {
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

	public int getRoomID() {
		return mainObj.getRoomID();
	}

	public boolean getRingStatus() {
		return mainObj.getRingStatus();
	}

	public void setNewRoomIDAvailable(boolean bValue) {
		mainObj.setNewRoomIDAvailable(bValue);
	}

	public void setNewRingStatusAvailable(boolean bValue) {
		mainObj.setNewRingStatusAvailable(bValue);
	}

	public void registerCallback(GPSGatewaySmartCallback _callbackTo) {
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

	public void ___getRoomID() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getRoomID();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getRingStatus() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getRingStatus();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___setNewRoomIDAvailable() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { boolean.class }, new Class<?>[] { null }, localMethodBytes);
		setNewRoomIDAvailable((boolean) paramObj[0]);
	}

	public void ___setNewRingStatusAvailable() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { boolean.class }, new Class<?>[] { null }, localMethodBytes);
		setNewRingStatusAvailable((boolean) paramObj[0]);
	}

	public void ___registerCallback() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { int[].class }, new Class<?>[] { null }, localMethodBytes);
		try {
			int[] stubIdArray0 = (int[]) paramObj[0];
			int objIdRecv0 = stubIdArray0[0];
			GPSGatewaySmartCallback newStub0 = null;
			if(!IoTRMIUtil.mapStub.containsKey(objIdRecv0)) {
				newStub0 = new GPSGatewaySmartCallback_Stub(rmiComm, objIdRecv0);
				IoTRMIUtil.mapStub.put(objIdRecv0, newStub0);
				rmiComm.setObjectIdCounter(objIdRecv0);
				rmiComm.decrementObjectIdCounter();
			}
			else {
				newStub0 = (GPSGatewaySmartCallback_Stub) IoTRMIUtil.mapStub.get(objIdRecv0);
			}
			GPSGatewaySmartCallback stub0 = newStub0;
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
							___getRoomID();
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
							___getRingStatus();
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
							___setNewRoomIDAvailable();
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
							___setNewRingStatusAvailable();
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
