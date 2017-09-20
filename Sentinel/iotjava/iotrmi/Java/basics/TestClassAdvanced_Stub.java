import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import iotruntime.master.CommunicationHandler;

public class TestClassAdvanced_Stub {

	public static void main(String[] args) throws Exception {

		CommunicationHandler comHan = new CommunicationHandler(true);
		int numOfPorts = 1;
		int[] ports = comHan.getCallbackPorts(numOfPorts);

		int port = 5010;
		//String address = "localhost";
		//String address = "192.168.2.191";	// RPi2
		//String skeletonAddress = "128.195.136.170";	// dc-9.calit2.uci.edu
		String skeletonAddress = "128.195.204.132";
		String callbackAddress = "128.195.204.132";	// dw-2.eecs.uci.edu (this machine)
		//String skeletonAddress = "192.168.2.108";	// RPi1
		//String callbackAddress = "192.168.2.191";	// RPi2
		int rev = 0;

		TestClassComplete_Stub tcstub = new TestClassComplete_Stub(port, skeletonAddress, callbackAddress, rev, ports);
		/*System.out.println("==== ENUM ====");
		Enum en = Enum.APPLE;
		Enum res = tcstub.handleEnum(en);
		System.out.println("Enum member: " + res);
		Enum resComp = tcstub.handleEnumComplex(en, 23, 'c');
		System.out.println("Enum member: " + resComp);

		Enum[] enArr = { Enum.APPLE, Enum.ORANGE, Enum.APPLE, Enum.GRAPE };
		Enum[] resArr = tcstub.handleEnumArray(enArr);
		System.out.println("Enum members: " + Arrays.toString(resArr));
		List<Enum> enArr2 = new ArrayList(Arrays.asList(enArr));
		List<Enum> resArr2 = tcstub.handleEnumList(enArr2);
		System.out.println("Enum members: " + resArr2.toString());
		Enum[] resArr3 = tcstub.handleEnumComplex2(enArr, 23, 'c');
		System.out.println("Enum members: " + Arrays.toString(resArr3));
		

		System.out.println("==== STRUCT ====");
		Struct str = new Struct();
		str.name = "Rahmadi";
		str.value = 0.123f;
		str.year = 2016;
		Struct strRes = tcstub.handleStruct(str);
		System.out.println("Name: " + strRes.name);
		System.out.println("Value: " + strRes.value);
		System.out.println("Year: " + strRes.year);
		Struct strRes2 = tcstub.handleStructComplex(23, 'c', str);
		System.out.println("Name: " + strRes2.name);
		System.out.println("Value: " + strRes2.value);
		System.out.println("Year: " + strRes2.year);
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
		List<Struct> stRetList2 = tcstub.handleStructComplex2(23, 'c', arrStr);
		for(Struct st : stRetList2) {
			System.out.println("Name: " + st.name);
			System.out.println("Value: " + st.value);
			System.out.println("Year: " + st.year);
		}
		Enum[] resArr3 = tcstub.handleEnumStruct(enArr, stList, 'c');
		System.out.println("Enum members: " + Arrays.toString(resArr3));*/

		System.out.println("==== CALLBACKS ====");
		CallBackInterface cbSingle = new CallBack(2354);
		tcstub.registerCallback(cbSingle);
		System.out.println("Return value from callback: " + tcstub.callBack());
		/*CallBackInterface cb1 = new CallBack(23);
		CallBackInterface cb2 = new CallBack(33);
		CallBackInterface cb3 = new CallBack(43);
		CallBackInterface[] cb = { cb1, cb2, cb3 };
		tcstub.registerCallbackArray(cb);
		System.out.println("Return value from callback: " + tcstub.callBack());
		List<CallBackInterface> cblist = new ArrayList<CallBackInterface>();
		CallBackInterface cb4 = new CallBack(53); cblist.add(cb4);
		CallBackInterface cb5 = new CallBack(63); cblist.add(cb5);
		CallBackInterface cb6 = new CallBack(73); cblist.add(cb6);
//		tcstub.registerCallbackList(cblist);
//		System.out.println("Return value from callback: " + tcstub.callBack());

		tcstub.registerCallbackComplex(23, cblist, 0.1234);
		System.out.println("Return value from callback: " + tcstub.callBack());
		Enum[] resArr4 = tcstub.handleAll(enArr, stList, 'c', cblist);
		System.out.println("Enum members: " + Arrays.toString(resArr4));

		Enum[] resArr5 = tcstub.handleCallbackEnum(enArr, 'c', cblist);
		System.out.println("Enum members: " + Arrays.toString(resArr5));
		//Enum[] resArr6 = tcstub.handleAllTwo(enArr, stList, stList, enArr, 'c', cblist, cblist);
//		Enum[] resArr6 = tcstub.handleAllTwo(enArr, enArr, stList, stList, 'c', cblist, cblist);
//		System.out.println("Enum members: " + Arrays.toString(resArr6));
		Enum[] resArr7 = tcstub.handleEnumTwo(enArr, enArr);
		System.out.println("Enum members: " + Arrays.toString(resArr7));
		Enum[] resArr8 = tcstub.handleEnumThree(enArr, enArr, stList, stList);
		System.out.println("Enum members: " + Arrays.toString(resArr8));
		List<Struct> stRetList2 = tcstub.handleStructTwo(stList, stList);
		for(Struct st : stRetList2) {
			System.out.println("Name: " + st.name);
			System.out.println("Value: " + st.value);
			System.out.println("Year: " + st.year);
		}
		List<Struct> stRetList3 = tcstub.handleStructThree(stList, stList, stList);
		for(Struct st : stRetList3) {
			System.out.println("Name: " + st.name);
			System.out.println("Value: " + st.value);
			System.out.println("Year: " + st.year);
		}*/
	}
}
