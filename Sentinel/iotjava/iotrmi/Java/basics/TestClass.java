import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class TestClass implements TestClassInterface {

	/**
	 * Class Properties
	 */
	private int intA;
	private float floatB;
	private String stringC;
	private List<CallBackInterfaceWithCallBack> cblist;

	/**
	 * Constructors
	 */
	public TestClass() {

		intA = 1;
		floatB = 2;
		stringC = "345";
		cblist = new ArrayList<CallBackInterfaceWithCallBack>();
	}


	public TestClass(int _int, float _float, String _string) {

		intA = _int;
		floatB = _float;
		stringC = _string;
		cblist = new ArrayList<CallBackInterfaceWithCallBack>();
	}

	
	public int callBack() {
		
		int sum = 0;
		System.out.println("Callback called! cblist: " + cblist.size());
		for (CallBackInterfaceWithCallBack cb : cblist) {
			sum = sum + cb.printInt();
			//cb.needCallback(this);
			TestClass tci = new TestClass();
			cb.needCallback(this);
			cb.needCallback(tci);
			System.out.println("\n\nInside the loop! Sum is now: " + sum + "\n\n");
		}
		System.out.println("Executed callback of callback! Returning value: " + sum + "\n\n");
		return sum;
	}

	// Callback
	//public void registerCallback(CallBackInterface _cb) {
	public void registerCallback(CallBackInterfaceWithCallBack _cb) {

		cblist.add(_cb);
		System.out.println("Registering callback object! Items: " + cblist.size());
	}


	public void registerCallbackArray(CallBackInterfaceWithCallBack _cb[]) {

		for (CallBackInterfaceWithCallBack cb : _cb) {
			cblist.add(cb);
			System.out.println("Registering callback objects in array!");
		}
	}


	public void registerCallbackList(List<CallBackInterfaceWithCallBack> _cb) {

		for (CallBackInterfaceWithCallBack cb : _cb) {
			cblist.add(cb);
			System.out.println("Registering callback objects in list!");
		}
	}


	public void registerCallbackComplex(int in, List<CallBackInterfaceWithCallBack> _cb, double db) {

		for (CallBackInterfaceWithCallBack cb : _cb) {
			cblist.add(cb);
			System.out.println("Registering callback objects in list!");
		}

		System.out.println("Integer: " + in);
		System.out.println("Double: " + db);
	}


	// Single variables
	public byte getByte(byte in) {

		return in;
	}


	public short getShort(short in) {

		return in;
	}


	public long getLong(long in) {

		return in;
	}


	public float getFloat(float in) {

		return in;
	}


	public double getDouble(double in) {

		return in;
	}


	public boolean getBoolean(boolean in) {

		return in;
	}


	public char getChar(char in) {

		return in;
	}


	// Arrays
	public byte[] getByteArray(byte[] in) {

		return in;
	}


	public short[] getShortArray(short[] in) {

		return in;
	}


	public long[] getLongArray(long[] in) {

		return in;
	}


	public float[] getFloatArray(float[] in) {

		return in;
	}


	public double[] getDoubleArray(double[] in) {

		return in;
	}


	public boolean[] getBooleanArray(boolean[] in) {

		return in;
	}


	public char[] getCharArray(char[] in) {

		return in;
	}


	// Lists
	public List<Byte> getByteList(List<Byte> in) {

		return in;
	}


	public List<Short> getShortList(List<Short> in) {

		return in;
	}


	public List<Long> getLongList(List<Long> in) {

		return in;
	}


	public List<Float> getFloatList(List<Float> in) {

		return in;
	}


	public List<Double> getDoubleList(List<Double> in) {

		return in;
	}


	public List<Boolean> getBooleanList(List<Boolean> in) {

		return in;
	}


	public List<Character> getCharList(List<Character> in) {

		return in;
	}


	// Other functions
	public int getA() {

		return intA;
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


	// Enum
	public Enum handleEnum(Enum en) {

		System.out.println("Enum: " + en);
				
		return en;
	}


	public Enum[] handleEnumArray(Enum[] en) {

		for (Enum e : en) {
			System.out.println("Enum: " + e);
		}
		
		return en;
	}


	public List<Enum> handleEnumList(List<Enum> en) {

		for (Enum e : en) {
			System.out.println("Enum: " + e);
		}
		
		return en;
	}


	public Enum handleEnumComplex(Enum en, int i, char c) {

		System.out.println("Enum: " + en);
		System.out.println("Integer: " + i);
		System.out.println("Char: " + c);
		
		return en;
	}


	public Enum[] handleEnumComplex2(Enum[] en, int in, char c) {

		for (Enum e : en) {
			System.out.println("Enum: " + e);
		}
		System.out.println("Integer: " + in);
		System.out.println("Char: " + c);
		
		return en;
	}


	public Enum[] handleEnumTwo(Enum en1[], Enum en2[]) {

		for (Enum e : en1) {
			System.out.println("Enum1: " + e);
		}
		for (Enum e : en2) {
			System.out.println("Enum2: " + e);
		}
		
		return en1;
	}


	public Enum[] handleEnumThree(Enum en1[], Enum en2[], List<Struct> str1, List<Struct> str2) {

		for (Enum e : en1) {
			System.out.println("Enum1: " + e);
		}
		for (Enum e : en2) {
			System.out.println("Enum2: " + e);
		}
		
		return en1;
	}


	// Struct
	public Struct handleStruct(Struct str) {

		//System.out.println("Name: " + str.name);
		//System.out.println("Value: " + str.value);
		//System.out.println("Year: " + str.year);


		//Struct test = new Struct();
		//test.name = "Anonymous";
		//test.value = 1.33f;
		//test.year = 2016;

		//str = test;

		return str;
	}


	public Struct[] handleStructArray(Struct str[]) {

		for (Struct st : str) {
			System.out.println("Name: " + st.name);
			System.out.println("Value: " + st.value);
			System.out.println("Year: " + st.year);
		}

		Struct test = new Struct();
		test.name = "Anonymous";
		test.value = 1.33f;
		test.year = 2016;

		str[0] = test;

		return str;
	}


	public List<Struct> handleStructList(List<Struct> str) {

		for (Struct st : str) {
			System.out.println("Name: " + st.name);
			System.out.println("Value: " + st.value);
			System.out.println("Year: " + st.year);
		}

		Struct test = new Struct();
		test.name = "Tests";
		test.value = 1.34f;
		test.year = 2017;

		str.add(test);

		return str;
	}


	public Struct handleStructComplex(int in, char c, Struct str) {

		System.out.println("Name: " + str.name);
		System.out.println("Value: " + str.value);
		System.out.println("Year: " + str.year);

		System.out.println("Integer: " + in);
		System.out.println("Char: " + c);

		Struct test = new Struct();
		test.name = "Anonymous";
		test.value = 1.33f;
		test.year = 2016;

		str = test;

		return str;
	}


	public List<Struct> handleStructComplex2(int in, char c, Struct str[]) {

		for (Struct st : str) {
			System.out.println("Name: " + st.name);
			System.out.println("Value: " + st.value);
			System.out.println("Year: " + st.year);
		}

		System.out.println("Integer: " + in);
		System.out.println("Char: " + c);

		return new ArrayList<Struct>(Arrays.asList(str));
	}


	public Enum[] handleEnumStruct(Enum en[], List<Struct> str, char c) {

		for (Struct st : str) {
			System.out.println("Name: " + st.name);
			System.out.println("Value: " + st.value);
			System.out.println("Year: " + st.year);
		}

		System.out.println("Char: " + c);

		return en;
	}


	public List<Struct> handleStructTwo(List<Struct> str1, List<Struct> str2) {

		for (Struct st : str1) {
			System.out.println("Name: " + st.name);
			System.out.println("Value: " + st.value);
			System.out.println("Year: " + st.year);
		}

		return str1;
	}


	public List<Struct> handleStructThree(List<Struct> str1, List<Struct> str2, List<Struct> str3) {

		for (Struct st : str1) {
			System.out.println("Name: " + st.name);
			System.out.println("Value: " + st.value);
			System.out.println("Year: " + st.year);
		}

		return str2;
	}


	public Enum[] handleAll(Enum en[], List<Struct> str, char c, List<CallBackInterfaceWithCallBack> _cb) {

		for (CallBackInterfaceWithCallBack cb : _cb) {
			cblist.add(cb);
			System.out.println("Registering callback objects in list!");
		}

		for (Struct st : str) {
			System.out.println("Name: " + st.name);
			System.out.println("Value: " + st.value);
			System.out.println("Year: " + st.year);
		}

		System.out.println("Char: " + c);

		return en;
	}


	public Enum[] handleCallbackEnum(Enum en[], char c, List<CallBackInterfaceWithCallBack> _cb) {

		for (CallBackInterfaceWithCallBack cb : _cb) {
			cblist.add(cb);
			System.out.println("Registering callback objects in list!");
		}

		System.out.println("Char: " + c);

		return en;
	}


	public Enum[] handleAllTwo(Enum en1[], Enum en2[], List<Struct> str1, List<Struct> str2, char c, List<CallBackInterfaceWithCallBack> _cb1, List<CallBackInterfaceWithCallBack> _cb2) {

		for (CallBackInterfaceWithCallBack cb : _cb1) {
			cblist.add(cb);
			System.out.println("Registering callback objects in list!");
		}

		for (Struct st : str1) {
			System.out.println("Name: " + st.name);
			System.out.println("Value: " + st.value);
			System.out.println("Year: " + st.year);
		}

		System.out.println("Char: " + c);

		return en1;
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

	public static void main(String[] args) {

		TestClass tc = new TestClass();
		System.out.println("Get short: " + tc.getShort((short) 1234));
	}
}
