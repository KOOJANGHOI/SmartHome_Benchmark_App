package iotruntime.master;

/** A class that construct object initialization info
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2015-05-12
 */

public final class ObjectInitInfo extends ObjectCreationInfo {

	/**
	 * ObjectInitInfo properties
	 */
	protected int iRMIRegPort;
	protected int iRMIStubPort;


	/**
	 * Constructor
	 */
	public ObjectInitInfo(String _strIoTSlaveObjectHostAdd, String _strObjName, 
		String _strObjClassName, String _strObjClassInterfaceName, String _strObjStubClsIntfaceName,
		int _iRMIRegPort, int _iRMIStubPort) {

		super(_strIoTSlaveObjectHostAdd, _strObjName, _strObjClassName, _strObjClassInterfaceName, _strObjStubClsIntfaceName);
		iRMIRegPort = _iRMIRegPort;
		iRMIStubPort = _iRMIStubPort;
	}

	/**
	 * Method getRMIRegistryPort()
	 */
	public int getRMIRegistryPort() {
		return iRMIRegPort;
	}

	/**
	 * Method getRMIStubPort()
	 */
	public int getRMIStubPort() {
		return iRMIStubPort;
	}
}
