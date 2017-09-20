package iotrmi.Java.sample;

import java.io.IOException;
import iotrmi.Java.IoTRMICall;

public class CallBack_Stub implements CallBackInterface {

	/**
	 * Class Properties
	 */
	private IoTRMICall rmiCall;

	private int objectId = 0;	// Default value is 0

	/**
	 * Constructors
	 */
	public CallBack_Stub(int _port, String _address, int _rev) throws IOException {

		rmiCall = new IoTRMICall(_port, _address, _rev);
	}


	public int printInt() {

		int methodId = 0;
		Class<?> retType = int.class;
		Class<?>[] paramCls = new Class<?>[] { };
		Object[] paramObj = new Object[] { };
		Object retObj = rmiCall.remoteCall(objectId, methodId, retType, null, paramCls, paramObj);
		return (int)retObj;
	}


	public void setInt(int _i) {

		int methodId = 1;
		Class<?> retType = void.class;
		Class<?>[] paramCls = new Class<?>[] { int.class };
		Object[] paramObj = new Object[] { _i };
		rmiCall.remoteCall(objectId, methodId, retType, null, paramCls, paramObj);
	}


	public static void main(String[] args) throws Exception {

		int port = 5010;
		String address = "localhost";
		int rev = 0;

		CallBack_Stub cbstub = new CallBack_Stub(port, address, rev);
		cbstub.setInt(23);
		cbstub.printInt();
	}
}
