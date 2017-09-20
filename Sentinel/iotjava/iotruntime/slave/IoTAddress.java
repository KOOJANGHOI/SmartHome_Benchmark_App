package iotruntime.slave;

// Java packages
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/** Class IoTAddress is a wrapper class to pass
 *  IoTSet of any addresses from master to slave
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-04-22
 */
public class IoTAddress {

	/**
	 * IoTDeviceAddress class properties
	 */
	protected final InetAddress inetAddress;

	/**
	 * Class constructor
	 *
	 * @param   sAddress  String address
	 */
	protected IoTAddress(String sAddress) throws UnknownHostException {

		inetAddress = InetAddress.getByName(sAddress);
	}

	/**
	 * getHostAddress() method
	 *
	 * @return  String
	 */
	public String getHostAddress() {

		return inetAddress.getHostAddress();

	}

	/**
	 * getHostName() method
	 *
	 * @return  String
	 */
	public String getHostName() {

		return inetAddress.getHostName();

	}

	/**
	 * getURL() method
	 *
	 * @return  String
	 */
	public String getURL(String strURLComplete) {

		//e.g. http:// + inetAddress.getHostAddress() + strURLComplete
		//     http://192.168.2.254/cgi-bin/mjpg/video.cgi?
		return "http://" + inetAddress.getHostAddress() + strURLComplete;

	}

	/**
	 * getCompleteAddress() method
	 *
	 * @return  String
	 */
	public String getCompleteAddress() {

		return inetAddress.toString();

	}
}
