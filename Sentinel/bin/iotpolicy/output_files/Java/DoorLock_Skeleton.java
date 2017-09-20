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

public class DoorLock_Skeleton implements DoorLock {

	private DoorLock mainObj;
	private int objectId = 1;
	// Communications and synchronizations
	private IoTRMIComm rmiComm;
	private AtomicBoolean didAlreadyInitWaitInvoke;
	private AtomicBoolean methodReceived;
	private byte[] methodBytes = null;
	// Permissions
	private static Integer[] object1Permission = { 6, 0, 5, 8, 9, 2, 4, 1, 7, 3 };
	private static List<Integer> set1Allowed;
	

	public DoorLock_Skeleton(DoorLock _mainObj, int _portSend, int _portRecv) throws Exception {
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

	public DoorLock_Skeleton(DoorLock _mainObj, IoTRMIComm _rmiComm, int _objectId) throws Exception {
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

	public void UnlockDoor() {
		mainObj.UnlockDoor();
	}

	public void LockDoor() {
		mainObj.LockDoor();
	}

	public void StatusRequest() {
		mainObj.StatusRequest();
	}

	public void init() {
		mainObj.init();
	}

	public int getValue() {
		return mainObj.getValue();
	}

	public boolean isActiveValue() {
		return mainObj.isActiveValue();
	}

	public long getTimestampOfLastReading() {
		return mainObj.getTimestampOfLastReading();
	}

	public void setId(int id) {
		mainObj.setId(id);
	}

	public int getId() {
		return mainObj.getId();
	}

	public void registerCallback(DoorLockCallbackSmart _callbackTo) {
		mainObj.registerCallback(_callbackTo);
	}

	public void ___UnlockDoor() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		UnlockDoor();
	}

	public void ___LockDoor() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		LockDoor();
	}

	public void ___StatusRequest() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		StatusRequest();
	}

	public void ___init() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		init();
	}

	public void ___getValue() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getValue();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___isActiveValue() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = isActiveValue();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___getTimestampOfLastReading() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getTimestampOfLastReading();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___setId() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { int.class }, new Class<?>[] { null }, localMethodBytes);
		setId((int) paramObj[0]);
	}

	public void ___getId() throws IOException {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] {  }, new Class<?>[] {  }, localMethodBytes);
		Object retObj = getId();
		rmiComm.sendReturnObj(retObj, localMethodBytes);
	}

	public void ___registerCallback() {
		byte[] localMethodBytes = methodBytes;
		rmiComm.setGetMethodBytes();
		Object[] paramObj = rmiComm.getMethodParams(new Class<?>[] { int[].class }, new Class<?>[] { null }, localMethodBytes);
		try {
			int[] stubIdArray0 = (int[]) paramObj[0];
			int objIdRecv0 = stubIdArray0[0];
			DoorLockCallbackSmart newStub0 = null;
			if(!IoTRMIUtil.mapStub.containsKey(objIdRecv0)) {
				newStub0 = new DoorLockCallbackSmart_Stub(rmiComm, objIdRecv0);
				IoTRMIUtil.mapStub.put(objIdRecv0, newStub0);
				rmiComm.setObjectIdCounter(objIdRecv0);
				rmiComm.decrementObjectIdCounter();
			}
			else {
				newStub0 = (DoorLockCallbackSmart_Stub) IoTRMIUtil.mapStub.get(objIdRecv0);
			}
			DoorLockCallbackSmart stub0 = newStub0;
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
							___UnlockDoor();
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
							___LockDoor();
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
							___StatusRequest();
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
							___init();
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
							___getValue();
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
							___isActiveValue();
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
							___getTimestampOfLastReading();
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
							___setId();
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
							___getId();
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
