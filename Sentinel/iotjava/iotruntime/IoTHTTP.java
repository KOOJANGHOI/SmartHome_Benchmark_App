package iotruntime;

// Java packages
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.URL;
import java.net.ProtocolException;

import iotruntime.slave.IoTDeviceAddress;

/** Class IoTHTTP is a wrapper class that provides
 *  minimum interfaces for user to interact with IoT
 *  devices in our system
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-02-18
 */
public final class IoTHTTP {

	/**
	 * IoTHTTP class properties
	 */
	private IoTDeviceAddress iotDevAdd;
	private URL url;
	private HttpURLConnection httpConnection;

	/**
	 * Class constructor
	 */
	public IoTHTTP(IoTDeviceAddress _iotDevAdd) {

		iotDevAdd = _iotDevAdd;
		url = null;
		httpConnection = null;
	}

	/**
	 * setURL() method
	 *
	 * @param  strUrlComplete  String to complete the URL
	 * @return void
	 */
	public void setURL(String strUrlComplete) throws MalformedURLException {

		url = new URL(iotDevAdd.getURL(strUrlComplete));

	}

	/**
	 * openConnection() method
	 */
	public void openConnection() throws IOException {

		httpConnection = (HttpURLConnection) url.openConnection();

	}

	/**
	 * setDoInput() method inherited from HttpURLConnection class
	 *
	 * @param  bSetDoInput
	 * @return void
	 */
	public void setDoInput(boolean bSetDoInput) {

		httpConnection.setDoInput(bSetDoInput);

	}

	/**
	 * setRequestProperty() method inherited from HttpURLConnection class
	 *
	 * @param  strProperty             String property
	 * @param  strHttpAuthCredentials  String HTTP authentication credentials
	 * @return void
	 */
	public void setRequestProperty(String strProperty, String strHttpAuthCredentials) {

		httpConnection.setRequestProperty(strProperty, strHttpAuthCredentials);

	}

	/**
	 * setRequestMethod() method inherited from HttpURLConnection class
	 *
	 * @param  strMethod             String method
	 * @return void
	 */
	public void setRequestMethod(String strMethod) throws ProtocolException {

		httpConnection.setRequestMethod(strMethod);

	}

	/**
	 * setDoOutput() method inherited from HttpURLConnection class
	 *
	 * @param  doOut
	 * @return void
	 */
	public void setDoOutput(boolean doOut) {

		httpConnection.setDoOutput(doOut);

	}

	/**
	 * getOutputStream() method inherited from HttpURLConnection class
	 *
	 * @return OutputStream
	 */
	public OutputStream getOutputStream() throws IOException {

		return httpConnection.getOutputStream();

	}

	/**
	 * getInputStream() method inherited from HttpURLConnection class
	 *
	 * @return InputStream
	 */
	public InputStream getInputStream() throws IOException {

		return httpConnection.getInputStream();

	}

	/**
	 * connect() method inherited from HttpURLConnection class
	 */
	public void connect() throws IOException {

		httpConnection.connect();

	}

	/**
	 * disconnect() method inherited from HttpURLConnection class
	 */
	public void disconnect() throws IOException {

		httpConnection.disconnect();

	}
}
