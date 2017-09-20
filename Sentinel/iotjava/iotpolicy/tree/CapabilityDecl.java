package iotpolicy.tree;

import java.util.ArrayList;
import java.util.List;

/** Class CapabilityDecl is a data structure for capability
 *  declaration section (list of capabilities) in the policy file.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-09-20
 */
public class CapabilityDecl extends Declaration {

	/**
	 * A "capability" statement:
	 * 		capability Camera.ImageCapture {
	 *			description = "The quick brown fox jumps over the smart dog";
	 *			description = "Another description";
	 *			method = MethodA;
	 *			method = MethodB;
	 *		}
	 * In this data structure we will record its capability name, i.e. ImageCapture
	 * 		and its descriptions and methods.
	 */

	/**
	 * Class properties
	 */
	private List<String> listCapabs;		// list of capabilities
	private List<List<String>> listDescs;	// list of descriptions
	private List<List<String>> listMethods;	// list of methods

	/**
	 * Class constructors
	 */
	public CapabilityDecl() {

		super();
		listCapabs = new ArrayList<String>();
		listDescs = new ArrayList<List<String>>();
		listMethods = new ArrayList<List<String>>();
	}


	public CapabilityDecl(String _origInt) {

		super(_origInt);
		listCapabs = new ArrayList<String>();
		listDescs = new ArrayList<List<String>>();
		listMethods = new ArrayList<List<String>>();
	}


	/**
	 * addNewCapability() adds a new capability into the list
	 */
	public void addNewCapability(String newCap) {

		listCapabs.add(newCap);
		listDescs.add(new ArrayList<String>());
		listMethods.add(new ArrayList<String>());
	}


	/**
	 * addNewDescription() adds a new description into the list
	 */
	public void addNewDescription(String cap, String newDesc) {

		int index = listCapabs.indexOf(cap);
		List<String> listDesc = listDescs.get(index);
		listDesc.add(newDesc);
	}


	/**
	 * addNewMethod() adds a new method into the list
	 */
	public void addNewMethod(String cap, String newMethod) {

		int index = listCapabs.indexOf(cap);
		List<String> listMethod = listMethods.get(index);
		listMethod.add(newMethod);
	}


	/**
	 * getCapabilities() gets list of capabilities
	 */
	public List<String> getCapabilities() {

		return listCapabs;
	}


	/**
	 * getDescriptions() gets list of descriptions
	 */
	public List<String> getDescriptions(String cap) {

		int index = listCapabs.indexOf(cap);
		return listDescs.get(index);
	}


	/**
	 * getMethods() gets list of methods
	 */
	public List<String> getMethods(String cap) {

		int index = listCapabs.indexOf(cap);
		// If index=-1, it means that it's not found.
		// There is perhaps a discrepancy in the policy file
		//		between the list of capabilities and requires
		//		sections
		if (index == -1)
			throw new Error("CapabilityDecl: Capability " + cap + 
				" does not exist in this interface! Please check your (requires) policy file...");
		return listMethods.get(index);
	}
}
