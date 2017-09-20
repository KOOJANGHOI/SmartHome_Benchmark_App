package iotrmi.Java.sample;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class TestClass implements TestClassInterface {

	/**
	 * Class Properties
	 */
	private int intA;
	private float floatB;
	private String stringC;
	private CallBackInterface cb;
	private List<CallBackInterface> cblist;

	/**
	 * Constructors
	 */
	public TestClass() {

		intA = 1;
		floatB = 2;
		stringC = "345";
		cb = null;
		cblist = new ArrayList<CallBackInterface>();
	}


	public TestClass(int _int, float _float, String _string) {

		intA = _int;
		floatB = _float;
		stringC = _string;
		cb = null;
		cblist = new ArrayList<CallBackInterface>();
	}


	public void setA(int _int) {

		intA = _int;
	}


	public void setB(float _float) {

		floatB = _float;
	}


	public void setC(String _string) {

		stringC = _string;
	}


	// Getters
	public String sumArray(String[] newA) {

		String sum = "";
		for (String i : newA) 
			sum = sum + i;
		return sum;
	}


	public int setAndGetA(int newA) {

		intA = newA;
		return intA;
	}


	public int setACAndGetA(String newC, int newA) {

		stringC = newC;
		intA = newA;
		return intA;
	}


	public void registerCallback(CallBackInterface _cb) {

		cb = _cb;
	}


	public void registerCallback(CallBackInterface[] _cb) {

		for (CallBackInterface cb : _cb) {
			cblist.add(cb);
			System.out.println("Registering callback object!");
		}
	}


	//public int callBack() {
	//	return cb.printInt();
	//}


	public int callBack() {

		int sum = 0;
		for (CallBackInterface cb : cblist) {
			sum = sum + cb.printInt();
		}
		
		/*final CallBackInterface cb1 = cblist.get(1);
		final CallBackInterface cb2 = cblist.get(2);

		Thread thread1 = new Thread() {
			public void run() {
	            try{
					for(int i = 0; i < 10; i++) {
						cb1.printInt();
						Thread.sleep(1000);
					}
				} catch (Exception ex){
					ex.printStackTrace();
					throw new Error("Error running thread!");
	            }
	        }
	    };
		thread1.start();

		Thread thread2 = new Thread() {
			public void run() {
	            try{
					for(int i = 0; i < 10; i++) {
						cb2.printInt();
						Thread.sleep(1000);
					}
				} catch (Exception ex){
					ex.printStackTrace();
					throw new Error("Error running thread!");
	            }
	        }
	    };
		thread2.start();

		return 1;*/
		return sum;
	}

	public StructJ[] handleStruct(StructJ[] data) {

		for (StructJ str : data) {
			System.out.println("Name: " + str.name);
			System.out.println("Value: " + str.value);
			System.out.println("Year: " + str.year);
		}

		StructJ test = new StructJ();
		test.name = "Anonymous";
		test.value = 1.33f;
		test.year = 2016;

		data[0] = test;

		return data;
	}


	public EnumJ[] handleEnum(EnumJ[] en) {

		for (EnumJ e : en) {
			System.out.println("Enum: " + e);
		}
		
		return en;
	}


	public static void main(String[] args) {

		//TestClass tc = new TestClass();
		//CallBack cb = new CallBack(3);

		//tc.registerCallback(cb);
		//System.out.println("Return value: " + tc.callBack());
	}
}
