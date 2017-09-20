package iotrmi.Java.sample;

import java.util.Set;

public interface TestClassInterface {

	public class StructJ {

		public static String name;
		public static float value;
		public static int year;
	}

	public enum EnumJ {

		APPLE,
		ORANGE,
		GRAPE
	}

	public void setA(int _int);
	public void setB(float _float);
	public void setC(String _string);
	public String sumArray(String[] newA);
	public int setAndGetA(int newA);
	public int setACAndGetA(String newC, int newA);
	public void registerCallback(CallBackInterface _cb);
	public void registerCallback(CallBackInterface[] _cb);
	public int callBack();
	public StructJ[] handleStruct(StructJ[] data);
	public EnumJ[] handleEnum(EnumJ[] en);
}
