package SpeakerController;

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

public class SpeakerSmart_Stub implements SpeakerSmart {

	private int objectId = 1;
	private IoTRMIComm rmiComm;
	// Synchronization variables
	private AtomicBoolean retValueReceived6 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived2 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived1 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived3 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived4 = new AtomicBoolean(false);
	private AtomicBoolean retValueReceived5 = new AtomicBoolean(false);
	

	public SpeakerSmart_Stub(int _localPortSend, int _localPortRecv, int _portSend, int _portRecv, String _skeletonAddress, int _rev) throws Exception {
		if (_localPortSend != 0 && _localPortRecv != 0) {
			rmiComm = new IoTRMICommClient(_localPortSend, _localPortRecv, _portSend, _portRecv, _skeletonAddress, _rev);
		} else
		{
			rmiComm = new IoTRMICommClient(_portSend, _portRecv, _skeletonAddress, _rev);
		}
		rmiComm.registerStub(objectId, 6, retValueReceived6);
		rmiComm.registerStub(objectId, 2, retValueReceived2);
		rmiComm.registerStub(objectId, 1, retValueReceived1);
		rmiComm.registerStub(objectId, 3, retValueReceived3);
		rmiComm.registerStub(objectId, 4, retValueReceived4);
		rmiComm.registerStub(objectId, 5, retValueReceived5);
		IoTRMIUtil.mapStub.put(objectId, this);
	}

	public SpeakerSmart_Stub(IoTRMIComm _rmiComm, int _objectId) throws Exception {
		rmiComm = _rmiComm;
		objectId = _objectId;
		rmiComm.registerStub(objectId, 6, retValueReceived6);
		rmiComm.registerStub(objectId, 2, retValueReceived2);
		rmiComm.registerStub(objectId, 1, retValueReceived1);
		rmiComm.registerStub(objectId, 3, retValueReceived3);
		rmiComm.registerStub(objectId, 4, retValueReceived4);
		rmiComm.registerStub(objectId, 5, retValueReceived5);
	}

	public int getPosition() {
		int methodId = 6;
		Class<?> retType = int.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived6.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived6.set(false);
		rmiComm.setGetReturnBytes();

		return (int)retObj;
	}

	public boolean stopPlayback() {
		int methodId = 2;
		Class<?> retType = boolean.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived2.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived2.set(false);
		rmiComm.setGetReturnBytes();

		return (boolean)retObj;
	}

	public void clearData() {
		int methodId = 9;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public boolean startPlayback() {
		int methodId = 1;
		Class<?> retType = boolean.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived1.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived1.set(false);
		rmiComm.setGetReturnBytes();

		return (boolean)retObj;
	}

	public boolean getPlaybackState() {
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

	public boolean setVolume(float _percent) {
		int methodId = 4;
		Class<?> retType = boolean.class;
		Class<?>[] paramCls = new Class<?>[] { float.class };
		Object[] paramObj = new Object[] { _percent };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived4.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived4.set(false);
		rmiComm.setGetReturnBytes();

		return (boolean)retObj;
	}

	public float getVolume() {
		int methodId = 5;
		Class<?> retType = float.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
		// Waiting for return value
		while (!retValueReceived5.get());
		Object retObj = rmiComm.getReturnValue(retType, null);
		retValueReceived5.set(false);
		rmiComm.setGetReturnBytes();

		return (float)retObj;
	}

	public void setPosition(int _mSec) {
		int methodId = 7;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] { int.class };
		Object[] paramObj = new Object[] { _mSec };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public void loadData(short _samples[], int _offs, int _len) {
		int methodId = 8;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] { short[].class, int.class, int.class };
		Object[] paramObj = new Object[] { _samples, _offs, _len };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public void init() {
		int methodId = 0;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] {  };
		Object[] paramObj = new Object[] {  };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

	public void registerCallback(SpeakerCallback _cb) {
		int[] objIdSent0 = new int[1];
		try {
			if (!IoTRMIUtil.mapSkel.containsKey(_cb)) {
				int newObjIdSent = rmiComm.getObjectIdCounter();
				objIdSent0[0] = newObjIdSent;
				rmiComm.decrementObjectIdCounter();
				SpeakerCallback_Skeleton skel0 = new SpeakerCallback_Skeleton(_cb, rmiComm, newObjIdSent);
				IoTRMIUtil.mapSkel.put(_cb, skel0);
				IoTRMIUtil.mapSkelId.put(_cb, newObjIdSent);
				Thread thread = new Thread() {
					public void run() {
						try {
							skel0.___waitRequestInvokeMethod();
							} catch (Exception ex) {
							ex.printStackTrace();
							throw new Error("Exception when trying to run ___waitRequestInvokeMethod() for SpeakerCallback_Skeleton!");
						}
					}
				};
				thread.start();
				while(!skel0.didAlreadyInitWaitInvoke());
			}
			else
			{
				int newObjIdSent = IoTRMIUtil.mapSkelId.get(_cb);
				objIdSent0[0] = newObjIdSent;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new Error("Exception when generating skeleton objects!");
		}

		int methodId = 10;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] { int[].class };
		Object[] paramObj = new Object[] { objIdSent0 };
		rmiComm.remoteCall(objectId, methodId, paramCls, paramObj);
	}

}
