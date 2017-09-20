package iotrmi.Java.sample;

public class StructMain {

	public static void main (String[] args) {

		StructJ data = new StructJ();
		data.name = "Rahmadi";
		data.value = 0.123f;
		data.year = 2016;

		System.out.println("Name: " + data.name);
		System.out.println("Value: " + data.value);
		System.out.println("Year: " + data.year);
	}
}

