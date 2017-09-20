import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import iotruntime.master.CommunicationHandler;

public class TestClass_ProfilingStub {

	public static void main(String[] args) throws Exception {
		CommunicationHandler comHan = new CommunicationHandler(true);
		int numOfPorts = 2;
		//int[] ports = comHan.getCallbackPorts(numOfPorts);

		//int localportsend = 5011;
		//int localportrecv = 6011;
		int portsend = 5000;
		int portrecv = 6000;
		//String skeletonAddress = "128.195.136.163";
		//String skeletonAddress = "128.195.204.132";
		String skeletonAddress = "192.168.2.108";
		//String callbackAddress = "128.195.204.132";		// dw-2.eecs.uci.edu (this		machine)
		String callbackAddress = "192.168.2.191";
		int rev = 0;

		//TestClassComplete_Stub tcstub = new TestClassComplete_Stub(localportsend, localportrecv, portsend, portrecv, 
			//skeletonAddress, rev);
		TestClassComplete_Stub tcstub = new TestClassComplete_Stub(0, 0, portsend, portrecv, skeletonAddress, rev);
		//byte[] in1 = { 68, 69 };
		//int counter = 100;
		//byte[] in1 = new byte[counter];
		//for (int i=0; i<counter; i++)
		//	in1[i] = 68;
		System.out.println("==== STRUCT ====");
		Struct str = new Struct();
		str.name = "Rahmadi";
		str.value = 0.123f;
		str.year = 2016;
		// PROFILING
		long start = 0;
		long end = 0;
		//byte[] returned = null;
		int exp = 10;
		long avg = 0;
		//Struct strRes = null;
		long longVar = 0;
		long inVar = 1234;
		double doubleVar = 0;
		double inDouble = 1234.1234;
		for (int i = 0; i < exp; i++) {
			start = System.currentTimeMillis();
			//start = System.nanoTime();
			//System.out.println("Return value: " + Arrays.toString(tcstub.getByteArray(in1)));
			//returned = tcstub.getByteArray(in1);
			//strRes = tcstub.handleStruct(str);
			//longVar = tcstub.getLong(inVar);
			doubleVar = tcstub.getDouble(inDouble);
			end = System.currentTimeMillis();
			//end = System.nanoTime();
			//long res = (end - start) / 1000;
			long res = (end - start);
			System.out.println("\n\n ==> Time: " + res);
			//System.out.println("\n\n ==> Time: " + (end - start));
			avg = avg + res;
		}
		System.out.println("Average: " + (avg / exp));
	}

}
