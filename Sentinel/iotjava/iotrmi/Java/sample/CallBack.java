package iotrmi.Java.sample;

public class CallBack implements CallBackInterface {

	/**
	 * Class Properties
	 */
	private int intA;

	/**
	 * Constructors
	 */
	public CallBack(int _i) {

		intA = _i;
	}


	public int printInt() {

		System.out.println("Integer: " + intA);
		return intA;
	}


	public void setInt(int _i) {

		intA = _i;
	}
}
