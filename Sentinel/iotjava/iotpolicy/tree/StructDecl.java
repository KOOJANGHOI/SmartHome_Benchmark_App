package iotpolicy.tree;

import java.util.ArrayList;
import java.util.List;


/** Class StructDecl is a data structure for struct
 *  declaration section in the policy file.
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-11-11
 */
public class StructDecl extends Declaration {

	/**
	 * A "struct" declaration:
	 *	struct Struct {
	 *
	 *		string 	name;
	 *		float	value;
	 * 		int		year;
	 *}
	 * In this data structure we will record its struct name, i.e. Struct,
	 * 		and its member types and members.
	 */

	/**
	 * Class properties
	 */
	private List<String> listStructs;			// Struct types/names (more than one struct)
	private List<List<String>> listMemberTypes;	// Member types, e.g. string, float, int, etc.
	private List<List<String>> listMembers;		// Member names, e.g. name, value, year, etc.

	/**
	 * Class constructors
	 */
	public StructDecl() {

		super();
		listStructs = new ArrayList<String>();
		listMemberTypes = new ArrayList<List<String>>();
		listMembers = new ArrayList<List<String>>();
	}


	public StructDecl(String _origInt) {

		super(_origInt);
		listStructs = new ArrayList<String>();
		listMemberTypes = new ArrayList<List<String>>();
		listMembers = new ArrayList<List<String>>();
	}


	/**
	 * addNewMember() adds a new member type and value into the list
	 */
	public void addNewMember(String structType, String newMemberType, String newMember) {

		if (listStructs.contains(structType)) {
		// Existing enum declaration
			int index = listStructs.indexOf(structType);
			List<String> memberTypeList = listMemberTypes.get(index);
			memberTypeList.add(newMemberType);
			List<String> memberList = listMembers.get(index);
			memberList.add(newMember);
		} else {
		// New declaration
			listStructs.add(structType);
			List<String> newMemberTypeList = new ArrayList<String>();
			newMemberTypeList.add(newMemberType);
			listMemberTypes.add(newMemberTypeList);
			List<String> newMemberList = new ArrayList<String>();
			newMemberList.add(newMember);
			listMembers.add(newMemberList);
		}
	}


	/**
	 * getStructTypes() gets list of recorded list structs
	 */
	public List<String> getStructTypes() {

		return listStructs;
	}


	/**
	 * getMemberTypes() gets list of member types
	 */
	public List<String> getMemberTypes(String structType) {

		int index = listStructs.indexOf(structType);
		return listMemberTypes.get(index);
	}


	/**
	 * getMembers() gets list of members
	 */
	public List<String> getMembers(String structType) {

		int index = listStructs.indexOf(structType);
		return listMembers.get(index);
	}


	/**
	 * getNumOfMembers() gets number of members
	 */
	public int getNumOfMembers(String structType) {

		int index = listStructs.indexOf(structType);
		return listMembers.get(index).size();
	}
}
