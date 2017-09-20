package iotpolicy.tree;

/** Abstract class Declaration is a parent class of InterfaceDecl,
 *  CapabilityDecl, and RequiresDecl
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-09-30
 */
public abstract class Declaration {

	/**
	 * Class properties
	 */
	private String origInt;

	/**
	 * Class constructors
	 */
	public Declaration() {

		origInt = null;
	}


	public Declaration(String _origInt) {

		origInt = _origInt;
	}


	public String getInterface() {

		return origInt;
	}
}

