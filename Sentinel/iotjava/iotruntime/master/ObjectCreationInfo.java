package iotruntime.master;

/** A class that construct object creation info
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2015-05-13
 */

public class ObjectCreationInfo {

	/**
	 * ObjectCreationInfo properties
	 */
	protected String strIoTSlaveObjectHostAdd;
	protected String strObjName;
	protected String strObjClassName;
	protected String strObjClassInterfaceName;
	protected String strObjStubClsIntfaceName;

	/**
	 * Constructor
	 */
	public ObjectCreationInfo(String _strIoTSlaveObjectHostAdd, String _strObjName, 
		String _strObjClassName, String _strObjClassInterfaceName, String _strObjStubClsIntfaceName) {

		strIoTSlaveObjectHostAdd = _strIoTSlaveObjectHostAdd;
		strObjStubClsIntfaceName = _strObjStubClsIntfaceName;
		strObjName = _strObjName;
		strObjClassName = _strObjClassName;
		strObjClassInterfaceName = _strObjClassInterfaceName;
	}

	/**
	 * Method getIoTSlaveObjectHostAdd()
	 */
	public String getIoTSlaveObjectHostAdd() {
		return strIoTSlaveObjectHostAdd;
	}

	/**
	 * Method getObjectName()
	 */
	public String getObjectName() {
		return strObjName;
	}

	/**
	 * Method getObjectClassName()
	 */
	public String getObjectClassName() {
		return strObjClassName;
	}

	/**
	 * Method getObjectClassInterfaceName()
	 */
	public String getObjectClassInterfaceName() {
		return strObjClassInterfaceName;
	}

	/**
	 * Method getObjectStubClassInterfaceName()
	 */
	public String getObjectStubClassInterfaceName() {
		return strObjStubClsIntfaceName;
	}
}
