import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import iotruntime.master.CommunicationHandler;

public class TestClass_Stub {

	public static void main(String[] args) throws Exception {

		CommunicationHandler comHan = new CommunicationHandler(true);
		int numOfPorts = 1;
		int[] ports = comHan.getCallbackPorts(numOfPorts);

		int port = 5010;
		String address = "localhost";
		//String address = "128.195.136.170";	// dc-9.calit2.uci.edu
		int rev = 0;

		System.out.println("Allocated ports: " + Arrays.toString(ports));

		TestClassComplete_Stub tcstub = new TestClassComplete_Stub(port, address, rev, ports);
		System.out.println("==== SINGLE ====");
		System.out.println("Return value: " + tcstub.getByte((byte)68));
		System.out.println("Return value: " + tcstub.getShort((short)1234));
		System.out.println("Return value: " + tcstub.getLong(12345678l));
		System.out.println("Return value: " + tcstub.getFloat(12.345f));
		System.out.println("Return value: " + tcstub.getDouble(12345.678));
		System.out.println("Return value: " + tcstub.getBoolean(true));
		System.out.println("Return value: " + tcstub.getChar('c'));

		System.out.println("==== ARRAY ====");
		byte[] in1 = { 68, 69 };
		System.out.println("Return value: " + Arrays.toString(tcstub.getByteArray(in1)));
		short[] in2 = { (short)1234, (short)1235 };
		System.out.println("Return value: " + Arrays.toString(tcstub.getShortArray(in2)));
		long[] in3 = { 12345678l, 12356782l };
		System.out.println("Return value: " + Arrays.toString(tcstub.getLongArray(in3)));
		float[] in4 = { 12.345f, 12.346f };
		System.out.println("Return value: " + Arrays.toString(tcstub.getFloatArray(in4)));
		double[] in5 = { 12345.678, 12345.543 };
		System.out.println("Return value: " + Arrays.toString(tcstub.getDoubleArray(in5)));
		boolean[] in6 = { true, false };
		System.out.println("Return value: " + Arrays.toString(tcstub.getBooleanArray(in6)));
		char[] in7 = { 'c', 'e' };
		System.out.println("Return value: " + Arrays.toString(tcstub.getCharArray(in7)));

		System.out.println("==== LIST ====");
		List<Byte> inl1 = Arrays.asList(new Byte[] { 68, 69 });
		System.out.println("Return value: " + tcstub.getByteList(inl1));
		List<Short> inl2 = Arrays.asList(new Short[] { (short)1234, (short)1235 });
		System.out.println("Return value: " + tcstub.getShortList(inl2));
		List<Long> inl3 = Arrays.asList(new Long[] { 12345678l, 12356782l });
		System.out.println("Return value: " + tcstub.getLongList(inl3));
		List<Float> inl4 = Arrays.asList(new Float[] { 12.345f, 12.346f });
		System.out.println("Return value: " + tcstub.getFloatList(inl4));
		List<Double> inl5 = Arrays.asList(new Double[] { 12345.678, 12345.543 });
		System.out.println("Return value: " + tcstub.getDoubleList(inl5));
		List<Boolean> inl6 = Arrays.asList(new Boolean[] { true, false });
		System.out.println("Return value: " + tcstub.getBooleanList(inl6));
		List<Character> inl7 = Arrays.asList(new Character[] { 'c', 'e' });
		System.out.println("Return value: " + tcstub.getCharList(inl7));

		System.out.println("==== ENUM ====");
		Enum en = Enum.APPLE;
		Enum res = tcstub.handleEnum(en);
		System.out.println("Enum member: " + res);
		Enum[] enArr = { Enum.APPLE, Enum.ORANGE, Enum.APPLE, Enum.GRAPE };
		Enum[] resArr = tcstub.handleEnumArray(enArr);
		System.out.println("Enum members: " + Arrays.toString(resArr));
		List<Enum> enArr2 = new ArrayList(Arrays.asList(enArr));
		List<Enum> resArr2 = tcstub.handleEnumList(enArr2);
		System.out.println("Enum members: " + resArr2.toString());

		System.out.println("==== STRUCT ====");
		Struct str = new Struct();
		str.name = "Rahmadi";
		str.value = 0.123f;
		str.year = 2016;
		Struct strRes = tcstub.handleStruct(str);
		System.out.println("Name: " + strRes.name);
		System.out.println("Value: " + strRes.value);
		System.out.println("Year: " + strRes.year);
		Struct str2 = new Struct();
		str2.name = "Trimananda";
		str2.value = 0.124f;
		str2.year = 2017;
		Struct[] arrStr = { str, str2 };
		Struct[] arrRet = tcstub.handleStructArray(arrStr);
		for(Struct st : arrRet) {
			System.out.println("Name: " + st.name);
			System.out.println("Value: " + st.value);
			System.out.println("Year: " + st.year);
		}
		List<Struct> stList = new ArrayList(Arrays.asList(arrStr));
		List<Struct> stRetList = tcstub.handleStructList(stList);
		for(Struct st : stRetList) {
			System.out.println("Name: " + st.name);
			System.out.println("Value: " + st.value);
			System.out.println("Year: " + st.year);
		}

		System.out.println("==== CALLBACKS ====");
		CallBackInterface cbSingle = new CallBack(2354);
		tcstub.registerCallback(cbSingle);
		System.out.println("Return value from callback: " + tcstub.callBack());
		//CallBackInterface cbSingle2 = new CallBack(2355);
		//tcstub.registerCallback(cbSingle2);
		//System.out.println("Return value from callback: " + tcstub.callBack());
		CallBackInterface cb1 = new CallBack(23);
		CallBackInterface cb2 = new CallBack(33);
		CallBackInterface cb3 = new CallBack(43);
		CallBackInterface[] cb = { cb1, cb2, cb3 };
		tcstub.registerCallbackArray(cb);
		System.out.println("Return value from callback: " + tcstub.callBack());
		List<CallBackInterface> cblist = new ArrayList<CallBackInterface>();
		CallBackInterface cb4 = new CallBack(53); cblist.add(cb4);
		CallBackInterface cb5 = new CallBack(63); cblist.add(cb5);
		CallBackInterface cb6 = new CallBack(73); cblist.add(cb6);
		tcstub.registerCallbackList(cblist);
		System.out.println("Return value from callback: " + tcstub.callBack());

		System.out.println("==== OTHERS ====");
		System.out.println("Return value: " + tcstub.getA());
		System.out.println("Return value: " + tcstub.setAndGetA(123));
		System.out.println("Return value: " + tcstub.setACAndGetA("string", 123));
		System.out.println("Return value: " + tcstub.sumArray(new String[] { "123", "456", "987" }));


	}
}
