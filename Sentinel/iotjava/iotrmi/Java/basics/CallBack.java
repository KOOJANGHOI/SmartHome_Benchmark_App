
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
	
	
	public void needCallback(TestClassComplete tc) {

		//System.out.println("Going to invoke getShort()!");
		//for(int i=0; i<10; i++)
		System.out.println("Short from TestClass: " + tc.getShort((short)1234));
	}
}
