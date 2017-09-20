package iotrmi.Java.sample;

import java.io.IOException;
import java.util.Set;
import java.util.Arrays;
import iotrmi.Java.IoTRMIObject;

public class CallBack_CBSkeleton implements CallBackInterface {

	private int objectId = 0;	// Default value is 0
	private CallBackInterface cb;


	/**
	 * Constructors
	 */
	public CallBack_CBSkeleton(CallBackInterface _cb, int _objectId) throws
		ClassNotFoundException, InstantiationException,
			IllegalAccessException, IOException {

		cb = _cb;
		objectId = _objectId;
		System.out.println("Creating CallBack_Skeleton and waiting!");
	}

	
	public int printInt() {
		return cb.printInt();
	}
	
	
	public void ___printInt(IoTRMIObject rmiObj) throws IOException {
		Object retObj = printInt();
		rmiObj.sendReturnObj(retObj);
	}


	public void setInt(int _i) {
		cb.setInt(_i);
	}
	
	
	public void ___setInt(IoTRMIObject rmiObj) {
		Object[] paramObj = rmiObj.getMethodParams(new Class<?>[] { int.class }, 
			new Class<?>[] { null });
		setInt((int) paramObj[0]);
	}
	

	public void invokeMethod(IoTRMIObject rmiObj) throws IOException {

		int methodId = rmiObj.getMethodId();

		switch (methodId) {
			case 0: ___printInt(rmiObj); break;
			case 1: ___setInt(rmiObj); break;
			default: 
				throw new Error("Method Id not recognized!");
		}
	}


	public static void main(String[] args) throws Exception {

	}
}
