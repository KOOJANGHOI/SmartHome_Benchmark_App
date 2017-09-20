import java.net.InetAddress;

public class TestClass_Skeleton {

	public static void main(String[] args) throws Exception {

		int portsend = 5000;
		int portrecv = 6000;
		String callbackAddress = InetAddress.getLocalHost().getHostAddress();
		//TestClassProfiling tc = new TestClassProfiling();
		TestClass tc = new TestClass();
		TestClassInterface_Skeleton tcSkel = new TestClassInterface_Skeleton(tc, portsend, portrecv);
	}
}
