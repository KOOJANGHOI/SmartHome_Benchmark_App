import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import iotruntime.master.CommunicationHandler;

public class TestClassCallbacks_Stub {

	public static void main(String[] args) throws Exception {

		CommunicationHandler comHan = new CommunicationHandler(true);
		int numOfPorts = 2;
		//int[] ports = comHan.getCallbackPorts(numOfPorts);

		int localportsend = 5011;
		int localportrecv = 6011;
		int portsend = 5000;
		int portrecv = 6000;
		//String address = "localhost";
		//String address = "192.168.2.191";	// RPi2
		//String skeletonAddress = "128.195.136.170";	// dc-9.calit2.uci.edu
		String skeletonAddress = "128.195.204.132";
		String callbackAddress = "128.195.204.132";	// dw-2.eecs.uci.edu (this machine)
		//String skeletonAddress = "192.168.2.108";	// RPi1
		//String callbackAddress = "192.168.2.191";	// RPi2
		int rev = 0;

		TestClassComplete_Stub tcstub = new TestClassComplete_Stub(localportsend, localportrecv, portsend, portrecv, 
			skeletonAddress, rev);
		System.out.println("==== CALLBACKS ====");
		CallBackInterface cbSingle = new CallBack(2354);
		tcstub.registerCallback(cbSingle);
		System.out.println("Registered callback!");
//		CallBackInterface cbSingle1 = new CallBack(2356);
//		tcstub.registerCallback(cbSingle1);
//		System.out.println("Registered callback!");
//		CallBackInterface cbSingle2 = new CallBack(2360);
//		tcstub.registerCallback(cbSingle2);
//		System.out.println("Registered callback!");
		/*CallBackInterface cb1 = new CallBack(23);
		CallBackInterface cb2 = new CallBack(33);
		CallBackInterface cb3 = new CallBack(43);
		CallBackInterface[] cb = { cb1, cb2, cb3 };
		tcstub.registerCallbackArray(cb);
		List<CallBackInterface> cblist = new ArrayList<CallBackInterface>();
		CallBackInterface cb4 = new CallBack(53); cblist.add(cb4);
		CallBackInterface cb5 = new CallBack(63); cblist.add(cb5);
		CallBackInterface cb6 = new CallBack(73); cblist.add(cb6);
		tcstub.registerCallbackList(cblist);*/
		/*Enum[] enArr = { Enum.APPLE, Enum.ORANGE, Enum.APPLE, Enum.GRAPE };
		List<Enum> enArr2 = new ArrayList(Arrays.asList(enArr));
		List<Enum> resArr2 = tcstub.handleEnumList(enArr2);
		System.out.println("Enum members: " + resArr2.toString());*/
		Struct str = new Struct();
		str.name = "Rahmadi";
		str.value = 0.123f;
		str.year = 2016;
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

//		System.out.println("Return value from callback 1: " + tcstub.callBack() + "\n\n");
//		System.out.println("\n\nCalling short one more time value: " + tcstub.getShort((short)4576) + "\n\n");
//		System.out.println("Return value from callback 2: " + tcstub.callBack() + "\n\n");
//		System.out.println("\n\nCalling short one more time value: " + tcstub.getShort((short)1233) + "\n\n");
//		System.out.println("\n\nCalling short one more time value: " + tcstub.getShort((short)1321) + "\n\n");
		while(true) {}
	}
}
