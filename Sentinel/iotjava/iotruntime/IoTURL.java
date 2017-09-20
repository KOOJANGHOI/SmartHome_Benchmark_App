package iotruntime;

// Java packages
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import iotruntime.slave.IoTAddress;

/** Class IoTURL is a wrapper class that provides
 *  minimum interfaces for user to interact with IoT
 *  devices in our system
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-03-23
 */
public final class IoTURL {

	/**
	 * IoTURL class properties
	 */
	private IoTAddress iotAddress;
	private URL internalURL;

	/**
	 * Class constructor
	 */
	public IoTURL(IoTAddress _iotAddress) {

		iotAddress = _iotAddress;
		internalURL = null;
	}

	/**
	 * setURL() method
	 *
	 * @param  _strUrlComplete String to complete the URL
	 * @return void
	 */
	public void setURL(String _strUrlComplete) throws MalformedURLException {
		internalURL = new URL(iotAddress.getURL(_strUrlComplete));
	}

	/**
	 * getAuthority() method inherited from URL class.
	 *
	 * @return String.
	 */
	public String getAuthority() {
		return internalURL.getAuthority();
	}

	/**
	 * getDefaultPort() method inherited from URL class.
	 *
	 * @return int.
	 */
	public int getDefaultPort() {
		return internalURL.getDefaultPort();
	}

	/**
	 * getFile() method inherited from URL class.
	 *
	 * @return String.
	 */
	public String getFile() {
		return internalURL.getFile();
	}

	/**
	 * getHost() method inherited from URL class.
	 *
	 * @return String.
	 */
	public String getHost() {
		return internalURL.getHost();
	}

	/**
	 * getPath() method inherited from URL class.
	 *
	 * @return String.
	 */
	public String getPath() {
		return internalURL.getPath();
	}

	/**
	 * getPort() method inherited from URL class.
	 *
	 * @return int.
	 */
	public int getPort() {
		return internalURL.getPort();
	}

	/**
	 * getProtocol() method inherited from URL class.
	 *
	 * @return String.
	 */
	public String getProtocol() {
		return internalURL.getProtocol();
	}

	/**
	 * getQuery() method inherited from URL class.
	 *
	 * @return String.
	 */
	public String getQuery() {
		return internalURL.getQuery();
	}

	/**
	 * getRef() method inherited from URL class.
	 *
	 * @return String.
	 */
	public String getRef() {
		return internalURL.getRef();
	}

	/**
	 * getUserInfo() method inherited from URL class.
	 *
	 * @return String.
	 */
	public String getUserInfo() {
		return internalURL.getUserInfo();
	}

	/**
	 * hashCode() method inherited from URL class.
	 *
	 * @return int.
	 */
	public int hashCode() {
		return internalURL.hashCode();
	}

	/**
	 * toExternalForm() method inherited from URL class.
	 *
	 * @return String.
	 */
	public String toExternalForm() {
		return internalURL.toExternalForm();
	}

	/**
	 * toString() method inherited from URL class.
	 *
	 * @return String.
	 */
	public String toString() {
		return internalURL.toString();
	}


	/**
	 * openConnection() method inherited from URL class.
	 *
	 * @return URLConnection.
	 */
	public URLConnection openConnection() throws IOException {
		return internalURL.openConnection();
	}

	/**
	 * openStream() method inherited from URL class.
	 *
	 * @return InputStream.
	 */
	public InputStream openStream() throws IOException {
		return internalURL.openStream();
	}

	/**
	 * getContent() method inherited from URL class.
	 *
	 * @return Object.
	 */
	public Object getContent() throws IOException {
		return internalURL.getContent();
	}

	/**
	 * getContent(Class[] classes) method inherited from URL class.
	 *
	 * @param classes.
	 * @return Object.
	 */
	public Object getContent(Class[] classes) throws IOException {
		return internalURL.getContent(classes);
	}
}
