package iotruntime.stub;

// Java libraries
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import java.lang.Class;
import java.lang.reflect.*;


/** IoTStubGenerator class that takes an interface,
 *  instrument it using Java Reflection, and generate
 *  static code that uses IoTRemoteCall as a stub object
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-04-14
 */
public class IoTStubCodeGenerator {

	/**
	 * IoTStubCodeGenerator properties
	 */
	PrintWriter pw;
	String strInterfaceName;
	String strNewClassName;
	String strCallbackIntName;

	/**
	 * Constructor
	 */
	public IoTStubCodeGenerator(String[] strArgs) throws Exception {

		this.strInterfaceName = strArgs[0];
		if (strArgs.length == 1) {
		// If callback interface is not specified
			this.strCallbackIntName = "Object";
		} else {
			this.strCallbackIntName = strArgs[1];
		}
		strNewClassName = strInterfaceName + "Implementation";
		FileWriter fw = new FileWriter(strNewClassName + ".java");
		pw = new PrintWriter(new BufferedWriter(fw));
	}

	/**
	 * Instrument interface class
	 */
	public void instrumentInterfaceClass() {

		// Write the imports
		generateImports();
		// Write the class header
		println("public final class " + strNewClassName + 
				" extends IoTJSONStub " +
				" implements " + strInterfaceName + " {");
		println("");
		generateFields();
		println("");
		generateConstructor();
		println("");

		try {
			Class<?>cls = Class.forName(strInterfaceName);
			for (Method mtd : cls.getDeclaredMethods()) {
				generateMethod(mtd);
				println("");
			}
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		generateMethodCallback();

		//Close class
		println("}");
		pw.close();
	}

	/**
	 * Generate import statements
	 */
	private void generateImports() {
		// Write the class properties
		println("import iotruntime.stub.IoTRemoteCall;");
		println("import iotruntime.stub.IoTJSONStub;");
		println("import iotruntime.slave.IoTDeviceAddress;");
		println("");
	}

	/**
	 * Generate fields
	 */
	private void generateFields() {
		// Write the class properties
		println("private IoTRemoteCall iotremotecall;");
		println("private " + strCallbackIntName + " callbackObject;");
	}

	/**
	 * Generate constructor
	 */
	private void generateConstructor() {
		// Write the constructor
		println("public " + strNewClassName + 
			"(IoTDeviceAddress _iotDevAdd) {");
		println("super(_iotDevAdd);");
		println("this.iotremotecall = new IoTRemoteCall();");
		print("String[] arrMethodName = { \"");
		// Get the interface class
		try {
			Class<?>cls = Class.forName(strInterfaceName);
			Method[] method = cls.getDeclaredMethods();
			for (Method mtd: method) {
				print(mtd.getName());
				// Check if this is the last element
				if (!mtd.equals(method[method.length-1])) {
					print("\", \"");
				}
			}
			println("\" };");
			println("this.iotremotecall.startHttpServer(arrMethodName, " +
					"iotDevAddress.getDestinationPortNumber());");
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		println("}");
	}

	/**
	 * Generate method body
	 */
	private void generateMethod(Method mtd) {

		Class<?> clsReturn = mtd.getReturnType();
		// Write the method declaration
		print("public " + clsReturn.getSimpleName() + " " + mtd.getName() + "(");
		Parameter[] params = mtd.getParameters();
		// Write the method params
		for (Parameter param:params) {
			print(param.getType().getSimpleName() + " " + param.getName());
			// Check if this is the last element
			if (!param.equals(params[params.length-1])) {
				print(", ");
			}
		}
		println(") {");
		// Write the method body
		// Handle return value
		println("String strMethodName = \"" + mtd.getName() + "\";");
		// Handle inputs
    print("Object[] arrInpValue = { ");
    for (Parameter param:params) {
      print(param.getName());
      // Check if this is the last element
      if (!param.equals(params[params.length-1])) {
        print(", ");
      }
    }
    println(" };");
    print("String[] arrInpType = { \"");
    for (Parameter param:params) {
      print(param.getType().getSimpleName());
      // Check if this is the last element
      if (!param.equals(params[params.length-1])) {
        print("\", \"");
      }
    }		
    println("\" };");
    println("Object _retval=iotremotecall.callMethod(strMethodName, iotDevAddress.getHostAddress(), iotDevAddress.getDestinationPortNumber(), arrInpValue, arrInpType, \""+ clsReturn.getSimpleName()+ "\");");
    if (!clsReturn.equals(Void.class)) {
      println("return ("+clsReturn.getSimpleName()+") _retval;");
    }
    
		println("}");
	}

	private void generateMethodCallback() {

		// Write the method
		println("public void registerCallback(Object objCallback) {");
		println("this.callbackObject = (" + strCallbackIntName + ") objCallback;");
		println("}");
	}


	boolean newline=true;
	int tablevel=0;

	private void print(String str) {
		if (newline) {
			int tab=tablevel;
			if (str.equals("}"))
				tab--;
			for(int i=0; i<tab; i++)
				pw.print("\t");
		}
		pw.print(str);
		updatetabbing(str);
		newline=false;
	}

	private void println(String str) {
		if (newline) {
			int tab = tablevel;
			if (str.equals("}"))
				tab--;
			for(int i=0; i<tab; i++)
				pw.print("\t");
		}
		pw.println(str);
		updatetabbing(str);
		newline = true;
	}

	private void updatetabbing(String str) {
		tablevel+=count(str,'{')-count(str,'}');
	}

	private int count(String str, char key) {
		char[] array = str.toCharArray();
		int count = 0;
		for(int i=0; i<array.length; i++) {
			if (array[i] == key)
				count++;
		}
		return count;
	}

	public static void main(String[] args) throws Exception {
		// args[0] = normal interface name
		// args[1] = callback interface name
		IoTStubCodeGenerator stub = new IoTStubCodeGenerator(args);
		stub.instrumentInterfaceClass();
	}
}
