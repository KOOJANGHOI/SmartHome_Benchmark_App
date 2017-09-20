
// Java packages
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/** Class IoTDeviceAddress is a wrapper class to pass
 *  IoTSet of device addresses from master to slave
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-02-18
 */
public class IoTDeviceAddress extends IoTAddress {

	/**
	 * IoTDeviceAddress class properties
	 */
	private int iSrcPort;
	private int iDstPort;
	private final String sAddress;

	// the wildcard status of this address
	private final boolean isSrcPortWildCard;
	private final boolean isDstPortWildCard;


	/**
	 * Class constructor
	 *
	 * @param   sAddress  			String address
	 * @param   _iSrcPort      		Source port number
	 * @param   _iDstPort      		Destination port number
	 * @param   _isSrcPortWildCard  Is this source port a wild card (=can change port number)?
	 * @param   _isDstPortWildCard  Is this destination port a wild card (=can change port number)?
	 */
	public IoTDeviceAddress(String _sAddress, int _iSrcPort, int _iDstPort, boolean _isSrcPortWildCard, 
		boolean _isDstPortWildCard) throws UnknownHostException {

		super(_sAddress);
		sAddress = _sAddress;
		iSrcPort = _iSrcPort;
		iDstPort = _iDstPort;

		isSrcPortWildCard = _isSrcPortWildCard;
		isDstPortWildCard = _isDstPortWildCard;
	}

	/**
	 * getSourcePortNumber() method
	 *
	 * @return  int
	 */
	public int getSourcePortNumber() {

		return iSrcPort;

	}

	/**
	 * getDestinationPortNumber() method
	 *
	 * @return  int
	 */
	public int getDestinationPortNumber() {

		return iDstPort;

	}

	/**
	 * setSrcPort() method
	 *
	 * @param   port 	Port number
	 * @return  void
	 */
	public void setSrcPort(int port) {
		if (isSrcPortWildCard) {
			iSrcPort = port;
		}
	}

	/**
	 * setDstPort() method
	 *
	 * @param   port 	Port number
	 * @return  void
	 */
	public void setDstPort(int port) {
		if (isDstPortWildCard) {
			iDstPort = port;
		}
	}

	/**
	 * getAddress() method
	 *
	 * @return  String
	 */
	public String getAddress() {
		return sAddress;
	}

	/**
	 * getHostAddress() method
	 *
	 * @return  String
	 */
	public static String getLocalHostAddress() {

		String strLocalHostAddress = null;
		try {
			strLocalHostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		}		
		return strLocalHostAddress;
	}

	/**
	 * getIsSrcPortWildcard() method
	 *
	 * @return  boolean
	 */
	public boolean getIsSrcPortWildcard() {
		return isSrcPortWildCard;
	}

	/**
	 * getIsDstPortWildcard() method
	 *
	 * @return  boolean
	 */
	public boolean getIsDstPortWildcard() {
		return isDstPortWildCard;
	}
}
