package iotrmi.Java;

// Java libraries
import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;


/** Class IoTSocketClient is a communication class
 *  that extends IoTSocket. This is the client side.
 *  <p>
 *  Adapted from Java/C++ socket implementation
 *  by Keith Vertanen
 *  @see        <a href="https://www.keithv.com/software/socket/</a>
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2016-08-17
 */
public final class IoTSocketClient extends IoTSocket {

	/**
	 * Default constructor
	 */
	public IoTSocketClient(int _port, String _address, int rev) throws IOException
		{
		super(_port);
  		try {
		  	sock = new Socket( InetAddress.getByName(_address), port );
			input = new BufferedInputStream(sock.getInputStream(), BUFFSIZE);
			output = new BufferedOutputStream(sock.getOutputStream(),BUFFSIZE);
		}
		catch ( IOException e ) {
		 	e.printStackTrace();
		}
		// now we want to tell the server if we want reversed bytes or not
		output.write(rev);
		output.flush();
	}

	/**
	 * Additional constructor
	 */
	public IoTSocketClient(int _localPort, int _port, String _address, int rev) throws IOException
	{
		super(_localPort, _port);
  		try {
		  	sock = new Socket( InetAddress.getByName(_address), 
						port, InetAddress.getByName(_address), localPort );
			input = new BufferedInputStream(sock.getInputStream(), BUFFSIZE);
			output = new BufferedOutputStream(sock.getOutputStream(),BUFFSIZE);
		}
		catch ( IOException e ) {
		 	e.printStackTrace();
		}
		// now we want to tell the server if we want reversed bytes or not
		output.write(rev);
		output.flush();
	}
}
