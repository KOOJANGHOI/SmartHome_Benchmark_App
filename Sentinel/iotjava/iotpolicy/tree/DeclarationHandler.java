package iotpolicy.tree;

import java.util.HashMap;
import java.util.Map;

/** Abstract class Declaration is a parent class of InterfaceDecl,
 *  CapabilityDecl, and RequiresDecl
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-09-30
 */
public class DeclarationHandler {

	/**
	 * Class properties
	 */
	private Map<String,Declaration> mapInt2IntfaceDecl;
	private Map<String,Declaration> mapInt2CapabDecl;
	private Map<String,Declaration> mapInt2ReqDecl;
	private Map<String,Declaration> mapInt2EnumDecl;
	private Map<String,Declaration> mapInt2StructDecl;

	/**
	 * Class constructors
	 */
	public DeclarationHandler() {

		mapInt2IntfaceDecl = new HashMap<String,Declaration>();
		mapInt2CapabDecl = new HashMap<String,Declaration>();
		mapInt2ReqDecl = new HashMap<String,Declaration>();
		mapInt2EnumDecl = new HashMap<String,Declaration>();
		mapInt2StructDecl = new HashMap<String,Declaration>();
	}


	/**
	 * Setters/adders
	 */
	public void addInterfaceDecl(String origInt, Declaration intDecl) {

		mapInt2IntfaceDecl.put(origInt, intDecl);
	}


	public void addCapabilityDecl(String origInt, Declaration capDecl) {

		mapInt2CapabDecl.put(origInt, capDecl);
	}


	public void addRequiresDecl(String origInt, Declaration reqDecl) {

		mapInt2ReqDecl.put(origInt, reqDecl);
	}


	public void addEnumDecl(String origInt, Declaration enumDecl) {

		mapInt2EnumDecl.put(origInt, enumDecl);
	}


	public void addStructDecl(String origInt, Declaration structDecl) {

		mapInt2StructDecl.put(origInt, structDecl);
	}


	/**
	 * Getters
	 */
	public Declaration getInterfaceDecl(String origInt) {

		return mapInt2IntfaceDecl.get(origInt);
	}


	public Declaration getCapabilityDecl(String origInt) {

		return mapInt2CapabDecl.get(origInt);
	}


	public Declaration getRequiresDecl(String origInt) {

		return mapInt2ReqDecl.get(origInt);
	}


	public Declaration getEnumDecl(String origInt) {

		return mapInt2EnumDecl.get(origInt);
	}


	public Declaration getStructDecl(String origInt) {

		return mapInt2StructDecl.get(origInt);
	}
}
