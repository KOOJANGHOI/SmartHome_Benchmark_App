package iotpolicy.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Class InterfaceDecl is a data structure for interface
 *  declaration section in the policy file.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-09-20
 */
public class InterfaceDecl extends Declaration {

	/**
	 * A "interface" statement:
	 *		public interface Camera {
     *			public void MethodA(int A, int B);
     *			public int MethodB(int C, string D);
     *			public string MethodC(string E, int F);
     *			public float MethodD(int G, float H);
     *			public boolean MethodE(Camera I, boolean J);
     *			public void MethodF();
	 *		}
	 * In this data structure we will record its interface name, i.e. Camera
	 * 		its method names and the parameters for each method.
	 */

	/**
	 * Class properties
	 */
	private List<String> listMethods;					// Method signature (no spaces), e.g. MethodA(intA,SpeakerB)
	private List<String> listMethodIds;					// Method identifiers, e.g. MethodA
	private List<String> listMethodTypes;				// Method types, e.g. void
	private List<List<String>> listMethodParams;		// Method parameter names, e.g. A, B
	private List<List<String>> listMethodParamTypes;	// Method parameter types, e.g. int, int
	private Map<String,Integer> mapHelperNumMethodId;	// Helper method Id, e.g. for callbacks, structs.

	private static int helperMethodIdNum = -9999;

	/**
	 * Class constructors
	 */
	public InterfaceDecl() {

		super();
		listMethods = new ArrayList<String>();
		listMethodIds = new ArrayList<String>();
		listMethodTypes = new ArrayList<String>();
		listMethodParams = new ArrayList<List<String>>();
		listMethodParamTypes = new ArrayList<List<String>>();
		mapHelperNumMethodId = new HashMap<String,Integer>();
	}


	public InterfaceDecl(String _origInt) {

		super(_origInt);
		listMethods = new ArrayList<String>();
		listMethodIds = new ArrayList<String>();
		listMethodTypes = new ArrayList<String>();
		listMethodParams = new ArrayList<List<String>>();
		listMethodParamTypes = new ArrayList<List<String>>();
		mapHelperNumMethodId = new HashMap<String,Integer>();
	}


	/**
	 * addNewMethod() adds a new method name and type into the list
	 */
	public void addNewMethod(String newMethod, String newMethodId, String newMethodType) {

		listMethods.add(newMethod);
		listMethodIds.add(newMethodId);
		listMethodTypes.add(newMethodType);
		listMethodParams.add(new ArrayList<String>());
		listMethodParamTypes.add(new ArrayList<String>());
	}


	/**
	 * addMethodParam() adds the name and type of a parameter
	 */
	public void addMethodParam(String method, String paramName, String paramType) {

		int index = listMethods.indexOf(method);
		List<String> listMethodParam = listMethodParams.get(index);
		listMethodParam.add(paramName);
		List<String> listMethodParamType = listMethodParamTypes.get(index);
		listMethodParamType.add(paramType);
	}


	/**
	 * getMethods() gets list of methods
	 */
	public List<String> getMethods() {

		return listMethods;
	}


	/**
	 * getMethodNumId() gets Id number for a method
	 */
	public int getMethodNumId(String method) {

		return listMethods.indexOf(method);
	}


	/**
	 * getHelperMethodNumId() gets Id number for a method
	 */
	public int getHelperMethodNumId(String method) {

		if (!mapHelperNumMethodId.containsKey(method)) {
			mapHelperNumMethodId.put(method, helperMethodIdNum++);
			return mapHelperNumMethodId.get(method);
		} else {
			return mapHelperNumMethodId.get(method);
		}
	}


	/**
	 * getMethodIds() gets method identifiers
	 */
	public List<String> getMethodIds() {

		return listMethodIds;
	}


	/**
	 * getMethodTypes() gets method types
	 */
	public List<String> getMethodTypes() {

		return listMethodTypes;
	}


	/**
	 * getMethodId() gets a method identifier
	 */
	public String getMethodId(String method) {

		int index = listMethods.indexOf(method);
		// If index=-1, it means that it's not found.
		// There is perhaps a discrepancy in the policy file
		//		between the method signatures in the interface 
		//		and capability sections
		if (index == -1)
			throw new Error("InterfaceDecl: Discrepancies in method signature for " + 
				method + "! Please check your policy file...");
		return listMethodIds.get(index);
	}


	/**
	 * getMethodType() gets a method type
	 */
	public String getMethodType(String method) {

		int index = listMethods.indexOf(method);
		// If index=-1, it means that it's not found.
		// There is perhaps a discrepancy in the policy file
		//		between the method signatures in the interface 
		//		and capability sections
		if (index == -1)
			throw new Error("InterfaceDecl: Discrepancies in method signature for " + 
				method + "! Please check your policy file...");
		return listMethodTypes.get(index);
	}


	/**
	 * getMethodParams() gets list of method parameters for a method
	 */
	public List<String> getMethodParams(String method) {

		int index = listMethods.indexOf(method);
		// If index=-1, it means that it's not found.
		// There is perhaps a discrepancy in the policy file
		//		between the method signatures in the interface 
		//		and capability sections
		if (index == -1)
			throw new Error("InterfaceDecl: Discrepancies in method signature for " + 
				method + "! Please check your policy file...");
		return listMethodParams.get(index);
	}
	

	/**
	 * getMethodParams() gets list of method parameter types for a method
	 */
	public List<String> getMethodParamTypes(String method) {

		int index = listMethods.indexOf(method);
		// If index=-1, it means that it's not found.
		// There is perhaps a discrepancy in the policy file
		//		between the method signatures in the interface 
		//		and capability sections
		if (index == -1)
			throw new Error("InterfaceDecl: Discrepancies in method signature for " + 
				method + "! Please check your policy file...");
		return listMethodParamTypes.get(index);
	}
}
