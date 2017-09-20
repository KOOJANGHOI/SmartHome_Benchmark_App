package iotrmi.Java.sample;

public class EnumMain {

	public static void main (String[] args) {

		// Enum to int
		int enum1 = EnumJ.APPLE.ordinal();
		System.out.println("Enum 1: " + enum1);
		int enum2 = EnumJ.ORANGE.ordinal();
		System.out.println("Enum 2: " + enum2);
		int enum3 = EnumJ.GRAPE.ordinal();
		System.out.println("Enum 3: " + enum3);

		// Int to enum
		EnumJ[] enumJ = EnumJ.values();
		System.out.println("Enum 1: " + enumJ[enum1]);
		System.out.println("Enum 1: " + enumJ[enum2]);
		System.out.println("Enum 1: " + enumJ[enum3]);
	}
}
