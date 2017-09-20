package iotpolicy.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Class RequiresDecl is a data structure for "requires"
 *  declaration section in the policy file.
 *  This section declares the needed interfaces based on
 *  different combinations of capabilities.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-09-20
 */
public class RequiresDecl extends Declaration {

	/**
	 * A "requires" statement:
	 * 		requires Camera with VideoRecording, ImageCapture as interface CameraWithCaptureAndData;
	 *
	 * In this data structure we will record its new interface name, i.e. CameraWithCaptureAndData
	 * 		and its required capabilities, i.e. VideoRecording and ImageCapture.
	 */

	/**
	 * Class properties
	 */
	private Map<String,List<String>> mapRequires;

	/**
	 * Class constructors
	 */
	public RequiresDecl() {

		super();
		mapRequires = new HashMap<String,List<String>>();
	}


	public RequiresDecl(String _origInt) {

		super(_origInt);
		mapRequires = new HashMap<String,List<String>>();
	}


	/**
	 * addNewInterface() adds a new interface name into the map
	 */
	public void addNewIntface(String newInt) {

		mapRequires.put(newInt, new ArrayList<String>());
	}


	/**
	 * addNewCapability() adds a new capability name into the map
	 */
	public void addNewCapability(String intFace, String newCapab) {

		List<String> listCapab = mapRequires.get(intFace);
		listCapab.add(newCapab);
	}


	/**
	 * getInterfaces() gets set of interfaces
	 */
	public Set<String> getInterfaces() {

		return mapRequires.keySet();
	}


	/**
	 * getCapabList() gets list of capabilities
	 */
	public List<String> getCapabList(String intFace) {

		return mapRequires.get(intFace);
	}
}
