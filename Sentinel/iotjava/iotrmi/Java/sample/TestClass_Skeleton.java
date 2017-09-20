package iotrmi.Java.sample;

import java.io.IOException;
import java.util.Arrays;

import java.util.List;

import iotrmi.Java.IoTRMIObject;
import iotrmi.Java.IoTRMICall;

public class TestClass_Skeleton implements TestClassInterface {

	/**
	 * Class Constants
	 */
	private TestClassInterface tc;
	private int port;
	private IoTRMIObject rmiObj;

	// Callback stuff
	private static int objIdCnt = 0; // Counter for callback object Ids
	private IoTRMICall rmiCall;
	private CallBackInterface cbstub;

	// Object permission
	private int object0Id = 0;
	private static Integer[] object0Permission = { 0, 1, 2, 3, 4, 5 };
	private List<Integer> set0Allowed;


	/**
	 * Constructors
	 */
	public TestClass_Skeleton(TestClass _tc, int _port) throws
		ClassNotFoundException, InstantiationException,
			IllegalAccessException, IOException {

		tc = _tc;
		port = _port;
		rmiObj = new IoTRMIObject(_port);
		set0Allowed = Arrays.asList(object0Permission);
		___waitRequestInvokeMethod();
	}

	
	public void setA(int _int) {
		
		tc.setA(_int);
	}
	
	
	public void ___setA() {

		Object[] paramObj = rmiObj.getMethodParams(new Class<?>[] { int.class }, 
			new Class<?>[] { null });
		setA((int) paramObj[0]);
	}
	
	
	public void setB(float _float) {
		
		tc.setB(_float);
	}
	
	
	public void ___setB() {

		Object[] paramObj = rmiObj.getMethodParams(new Class<?>[] { float.class }, 
			new Class<?>[] { null });
		setB((float) paramObj[0]);
	}
	
	
	public void setC(String _string) {
		
		tc.setC(_string);
	}
	
	
	public void ___setC() {
		
		Object[] paramObj = rmiObj.getMethodParams(new Class<?>[] { String.class }, 
			new Class<?>[] { null });
		setC((String) paramObj[0]);
	}
	
	
	public String sumArray(String[] newA) {
		
		return tc.sumArray(newA);
	}
	
	
	public void ___sumArray() throws IOException {
		
		Object[] paramObj = rmiObj.getMethodParams(new Class<?>[] { String[].class }, 
			new Class<?>[] { null });
		Object retObj = sumArray((String[]) paramObj[0]);
		rmiObj.sendReturnObj(retObj);
	}
	
	
	public int setAndGetA(int newA) {
		
		return tc.setAndGetA(newA);
	}
	
	
	public void ___setAndGetA() throws IOException {
		
		Object[] paramObj = rmiObj.getMethodParams(new Class<?>[] { int.class }, 
			new Class<?>[] { null });
		Object retObj = setAndGetA((int) paramObj[0]);
		rmiObj.sendReturnObj(retObj);
	}
	
	
	public int setACAndGetA(String newC, int newA) {
		
		return tc.setACAndGetA(newC, newA);
	}
	
	
	public void ___setACAndGetA() throws IOException {
		
		Object[] paramObj = rmiObj.getMethodParams(new Class<?>[] { String.class, int.class }, 
			new Class<?>[] { null, null });
		Object retObj = setACAndGetA((String) paramObj[0], (int) paramObj[1]);
		rmiObj.sendReturnObj(retObj);
	}
	
	
	public void registerCallback(CallBackInterface _cb) {
		
		tc.registerCallback(_cb);
	}
	
	
	public void ___registerCallback() throws IOException {
		
		Object[] paramObj = rmiObj.getMethodParams(new Class<?>[] { int.class, String.class, int.class }, 
			new Class<?>[] { null, null, null });
		CallBackInterface cbstub = new CallBack_Stub((int) paramObj[0], (String) paramObj[1], (int) paramObj[2]);
		registerCallback((CallBackInterface) cbstub);
	}
	

	public void registerCallback(CallBackInterface[] _cb) {
		
		tc.registerCallback(_cb);
	}
	
	
	// Use 4 underscores because this is a second instance of registerCallback
	public void ____registerCallback() throws IOException {
		
		Object[] paramObj = rmiObj.getMethodParams(new Class<?>[] { int.class }, 
			new Class<?>[] { null });
		int numStubs = (int) paramObj[0];
		CallBackInterface[] stub = new CallBackInterface[numStubs];
		for (int objId = 0; objId < numStubs; objId++) {
			stub[objId] = new CallBack_CBStub(rmiCall, objIdCnt);
			objIdCnt++;
		}
		registerCallback(stub);
	}
	
	
	public void ___regCB() throws IOException {
		
		Object[] paramObj = rmiObj.getMethodParams(new Class<?>[] { int.class, String.class, int.class }, 
			new Class<?>[] { null, null, null });
		//String[] methodSignatures = CallBack_CBStub.getMethodSignatures();
		rmiCall = new IoTRMICall((int) paramObj[0], (String) paramObj[1], (int) paramObj[2]);
		System.out.println("Creating a new IoTRMICall object");
	}
	
	
	public int callBack() {
		
		return tc.callBack();
	}
	
	
	public void ___callBack() throws IOException {
		
		Object retObj = callBack();
		rmiObj.sendReturnObj(retObj);
	}
	

	public StructJ[] handleStruct(StructJ[] data) {

		return tc.handleStruct(data);
	}
	
	
	public int ___structSize() {
		
		Object[] paramObj = rmiObj.getMethodParams(new Class<?>[] { int.class }, 
			new Class<?>[] { null });
		return (int) paramObj[0];
	}
	
	
	public void ___handleStruct(int structsize1) throws IOException {

		Class<?>[] paramCls = new Class<?>[3*structsize1];
		Class<?>[] paramClsVal = new Class<?>[3*structsize1];
		int pos = 0;
		for(int i=0; i < structsize1; i++) {
			paramCls[pos] = String.class;
			paramClsVal[pos++] = null;
			paramCls[pos] = float.class;
			paramClsVal[pos++] = null;
			paramCls[pos] = int.class;
			paramClsVal[pos++] = null;
		}
		Object[] paramObj = rmiObj.getMethodParams(paramCls, 
			paramClsVal);
		StructJ[] data = new StructJ[structsize1];
		for (int i=0; i < structsize1; i++) {
			data[i] = new StructJ();
		}
		pos = 0;
		for(int i=0; i < structsize1; i++) {
			data[i].name = (String) paramObj[pos++];
			data[i].value = (float) paramObj[pos++];
			data[i].year = (int) paramObj[pos++];
		}
		// Just the following if there is no returned value
		//tc.handleStruct(data);
		StructJ[] retStruct = tc.handleStruct(data);
		// Return length first
		int structsize2 = retStruct.length;
		Object retObj = structsize2;
		rmiObj.sendReturnObj(retObj);		
		// Send the actual struct members
		// Calculate the size of the array
		Class<?>[] retCls = new Class<?>[3*structsize2];
		Object[] retObj2 = new Object[3*structsize2];
		// Handle with for loop
		pos = 0;
		for(int i = 0; i < structsize2; i++) {
			retCls[pos] = String.class;
			retObj2[pos++] = data[i].name;
			retCls[pos] = float.class;
			retObj2[pos++] = data[i].value;
			retCls[pos] = int.class;
			retObj2[pos++] = data[i].year;
		}
		rmiObj.sendReturnObj(retCls, retObj2);
	}


	public EnumJ[] handleEnum(EnumJ[] en) {

		return tc.handleEnum(en);
	}


	public void ___handleEnum() throws IOException {

		Object[] paramObj = rmiObj.getMethodParams(new Class<?>[] { int[].class }, 
			new Class<?>[] { null }); 
		// Encoder/decoder
		int paramInt[] = (int[]) paramObj[0];
		int enumSize1 = paramInt.length;
		EnumJ[] enumJ = EnumJ.values();
		EnumJ[] data = new EnumJ[enumSize1];
		for (int i=0; i < enumSize1; i++) {
			data[i] = enumJ[paramInt[i]];
		}
		// if void, just "handleEnum(data)"
		// this is when we have return value EnumJ[]
		EnumJ[] retEnum = handleEnum(data);
		// Get length first
		int enumSize2 = retEnum.length;
		// Now send the array of integers
		int[] retEnumInt = new int[enumSize2];
		for (int i=0; i < enumSize2; i++) {
			retEnumInt[i] = retEnum[i].ordinal();
		}
		Object retObj = retEnumInt;
		rmiObj.sendReturnObj(retObj);
	}
	

	private void ___waitRequestInvokeMethod() throws IOException {

		// Struct size
		int structSize1 = 0;
		int enumSize1 = 0;
		// Loop continuously waiting for incoming bytes
		while (true) {

			rmiObj.getMethodBytes();
			int _objectId = rmiObj.getObjectId();
			int methodId = rmiObj.getMethodId();
			if (_objectId == object0Id) {
			// Multiplex based on object Id
				// Complain if the method is not allowed
				if (!set0Allowed.contains(methodId))
					throw new Error("TestClass_Skeleton: This object is not allowed to access method " + methodId);
			// If we have more than 1 object Id...
			//else if (_objectId == object1Id) {

			} else
				throw new Error("TestClass_Skeleton: Unrecognizable object Id: " + _objectId);

			switch (methodId) {

				case 0: ___setA(); break;
				case 1: ___setB(); break;
				case 2: ___setC(); break;
				case 3: ___sumArray(); break;
				case 4: ___setAndGetA(); break;
				case 5: ___setACAndGetA(); break;
				case 6: ___callBack(); break; 
				case 7: ___registerCallback(); break;
				case 8: ____registerCallback(); break;
				// Special option to register callback
				case 9: ___regCB(); break;
				// Struct handling (3 is the size of the struct)
				case 10: ___handleStruct(structSize1); break;
				case 11: structSize1 = ___structSize(); break;
				case 12: ___handleEnum(); break;
				default:
					throw new Error("Method Id " + methodId + " not recognized!");
			}

		}
	}
	
	
	public static void main(String[] args) throws Exception {

		int port = 5010;
		TestClass tc = new TestClass(3, 5f, "7911");
		TestClass_Skeleton tcSkel = new TestClass_Skeleton(tc, port);

	}
}
