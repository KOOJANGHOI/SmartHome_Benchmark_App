package iotrmi.Java;

// Java libraries
import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;


/** Class IoTSocketServer is a communication class
 *  that extends IoTSocket. This is the server side.
 *  <p>
 *  Adapted from Java/C++ socket implementation
 *  by Keith Vertanen
 *  @see        <a href="https://www.keithv.com/software/socket/</a>
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2016-08-17
 */
public final class IoTSocketServer extends IoTSocket {

	/**
	 * Class Properties
	 */
	ServerSocket server;

	/**
	 * Constructors
	 */
	public IoTSocketServer(int _port) throws IOException
	{
		super(_port);
  		try {
			server = new ServerSocket(port, 100);
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
    }


	/**
	 * Establish connection
	 */
	public void connect() throws IOException
	{
		byte rev[] = new byte[1];
		rev[0] = 0;
		sock = server.accept();
		input = new BufferedInputStream(sock.getInputStream(), BUFFSIZE);
		output = new BufferedOutputStream(sock.getOutputStream(),BUFFSIZE);
		// now find out if we want reversed bytes
		input.read(rev);
	}
}

