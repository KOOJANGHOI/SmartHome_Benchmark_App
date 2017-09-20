package iotpolicy.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/** Class EnumDecl is a data structure for enumeration
 *  declaration section in the policy file.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-11-11
 */
public class EnumDecl extends Declaration {

	/**
	 * A "enum" declaration:
	 *		enum Enum {
	 *
	 *			APPLE,
	 * 			ORANGE,
	 * 			GRAPE
	 *		} 
	 * In this data structure we will record its enum name, i.e. Enum
	 * 		and its members.
	 */

	/**
	 * Class properties
	 */
	// Members of enum (can be more than 1 declaration
	private Map<String, List<String>> mapEnumMembers;

	/**
	 * Class constructors
	 */
	public EnumDecl() {

		super();
		mapEnumMembers = new HashMap<String, List<String>>();
	}


	public EnumDecl(String _origInt) {

		super(_origInt);
		mapEnumMembers = new HashMap<String, List<String>>();
	}


	/**
	 * addNewMember() adds a new member into the list, e.g. MELON into the list of APPLE, ORANGE, and GRAPE
	 */
	public void addNewMember(String enumType, String newMember) {

		if (mapEnumMembers.containsKey(enumType)) {
		// Existing enum declaration
			List<String> memberList = mapEnumMembers.get(enumType);
			memberList.add(newMember);
		} else {
		// New declaration
			List<String> newMemberList = new ArrayList<String>();
			newMemberList.add(newMember);
			mapEnumMembers.put(enumType, newMemberList);
		}
	}


	/**
	 * getEnumDeclarations() gets list of enum declarations
	 */
	public Set<String> getEnumDeclarations() {

		return mapEnumMembers.keySet();
	}


	/**
	 * getMembers() gets list of enum members
	 */
	public List<String> getMembers(String enumType) {

		return mapEnumMembers.get(enumType);
	}
}
