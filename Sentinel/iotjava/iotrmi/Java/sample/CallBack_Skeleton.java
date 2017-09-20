package iotrmi.Java.sample;

import java.io.IOException;
import java.util.Set;
import java.util.Arrays;
import iotrmi.Java.IoTRMIObject;

public class CallBack_Skeleton implements CallBackInterface {

	private int objectId = 0;	// Default value is 0
	private CallBackInterface cb;
	private IoTRMIObject rmiObj;


	/**
	 * Constructors
	 */
	public CallBack_Skeleton(CallBackInterface _cb, int _port) throws
		ClassNotFoundException, InstantiationException,
			IllegalAccessException, IOException {

		cb = _cb;
		System.out.println("Creating CallBack_Skeleton and waiting!");
		rmiObj = new IoTRMIObject(_port);
		___waitRequestInvokeMethod();
	}


	public int printInt() {
		return cb.printInt();
	}
	
	
	public void ___printInt() throws IOException {
		Object retObj = printInt();
		rmiObj.sendReturnObj(retObj);
	}


	public void setInt(int _i) {
		cb.setInt(_i);
	}
	
	
	public void ___setInt() {
		Object[] paramObj = rmiObj.getMethodParams(new Class<?>[] { int.class }, 
			new Class<?>[] { null });
		setInt((int) paramObj[0]);
	}
	

	private void ___waitRequestInvokeMethod() throws IOException {

		// Loop continuously waiting for incoming bytes
		while (true) {

			rmiObj.getMethodBytes();
			int objId = rmiObj.getObjectId();
			if (objId == objectId) {
			// Multiplex based on object Id
				rmiObj.getMethodBytes();
				int methodId = rmiObj.getMethodId();

				switch (methodId) {
					case 0:	___printInt(); break;
					case 1: ___setInt(); break;
					default:
						throw new Error("Method Id not recognized!");
				}
			}
		}
	}


	public static void main(String[] args) throws Exception {

		int port = 5010;
		CallBack cb = new CallBack(23);
		CallBack_Skeleton cbSkel = new CallBack_Skeleton(cb, port);
		//cbSkel.waitRequestInvokeMethod();
	}
}
