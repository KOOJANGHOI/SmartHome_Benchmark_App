package iotrmi.Java;

// Java libraries
import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.nio.ByteBuffer;

import java.util.concurrent.Semaphore;


/** Class IoTSocket is the basic class for IoT RMI
 *  socket communication. This class will be extended
 *  by both IoTSocketServer and IoTSocketClient
 *  <p>
 *  Adapted from Java/C++ socket implementation
 *  by Keith Vertanen
 *  @see        <a href="https://www.keithv.com/software/socket/</a>
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2016-08-17
 */
public abstract class IoTSocket {

	/**
	 * Class Properties
	 */
	protected byte data[];
	protected int localPort;
	protected int port;
	protected Socket sock;
	protected BufferedInputStream input;
	protected BufferedOutputStream output;

	//protected static Semaphore sendRecvMutex = new Semaphore(1);

	/**
	 * Class Constant
	 */
	protected static int BUFFSIZE = 128000;	// how many bytes our incoming buffer can hold (original)
	//protected static int BUFFSIZE = 8388608;	// 8388608 = 2^23 bytes of memory (8MB) - this is required by our IHome speaker driver
	protected static int MSG_LEN_SIZE = 4;	// send length in the size of integer (4 bytes)

	/**
	 * Default constructor
	 */
	protected IoTSocket(int _port) throws IOException
	{
		localPort = 0;
		port = _port;
		data = new byte[BUFFSIZE];
	}
	
	 
	protected IoTSocket(int _localPort, int _port) throws IOException
	{
		localPort = _localPort;
		port = _port;
		data = new byte[BUFFSIZE];
	}


	/**
	 * sendBytes() sends an array of bytes
	 */
	public synchronized void sendBytes(byte vals[]) throws IOException
	{
		int len = vals.length;
		// Write the length first - convert to array of 4 bytes
		ByteBuffer bb = ByteBuffer.allocate(MSG_LEN_SIZE);
		bb.putInt(len);
		output.write(bb.array(), 0, MSG_LEN_SIZE);
		//System.out.println("Sender about to send: " + Arrays.toString(bb.array()));
		output.flush();
		// Write the byte array
		output.write(vals, 0, len);
		//System.out.println("Sender sending: " + len);
		output.flush();
		//System.out.println("Sender about to receive ACK!");
		receiveAck();
		//System.out.println("Sender about to send ACK!\n\n");
		sendAck();
	}


	/**
	 * receiveBytes() receives an array of bytes
	 */
	public synchronized byte[] receiveBytes(byte val[]) throws IOException
	{
		int i;
		int totalbytes = 0;
		int numbytes;

		// Wait until input is available
		if (input.available() == 0) {
			return null;
		}

		//System.out.println("Receiver about to receive: " + input.available());
		// Read the maxlen first - read 4 bytes here
		byte[] lenBytes = new byte[MSG_LEN_SIZE];
		input.read(lenBytes, 0, MSG_LEN_SIZE);
		//System.out.println("Receiver lenBytes: " + Arrays.toString(lenBytes));
		int maxlen = ByteBuffer.wrap(lenBytes).getInt();
		//System.out.println("Receiver received length: " + maxlen);
		// Receive until maxlen
		if (maxlen>BUFFSIZE) {
			System.out.println("IoTSocketClient/Server: Sending more bytes then will fit in buffer! Number of bytes: " + maxlen);
			// Allocate a bigger array when needed
			int newLen = 2;
			while (newLen < maxlen)	// Shift until we get a new buffer size that's bigger than maxLen (basically power of 2)
				newLen = newLen << 1;
			System.out.println("IoTSocketClient/Server: Allocating a bigger buffer now with size: " + newLen);
			BUFFSIZE = newLen;
			data = new byte[BUFFSIZE];
		}
		val = new byte[maxlen];
		while (totalbytes < maxlen)
		{
			numbytes = input.read(data);
			// copy the bytes into the result buffer
			for (i=totalbytes; i<totalbytes+numbytes; i++)
				val[i] = data[i-totalbytes];
			totalbytes += numbytes;
		}
		// we now send an acknowledgement to the server to let them
		// know we've got it
		//System.out.println("Receiver about to send ACK!");
		sendAck();
		//System.out.println("Receiver about to receive ACK!\n\n");
		receiveAck();

		return val;
	}


	/**
	 * Close socket connection
	 */
	public void close() throws IOException
	{
		sock.close();
	}


	/**
	 * Send ACK
	 */
	public synchronized void sendAck() throws IOException
	{
		int ack;
		ack = 0;
		output.write(ack);
		output.flush();
	}


	/**
	 * Receive ACK
	 */
	public synchronized void receiveAck() throws IOException
	{
		int ack;
		ack = (int) input.read();
	}


	/**
	 * Set SO TIMEOUT
	 */
	public void setSoTimeout(int timeout) throws SocketException {

		sock.setSoTimeout(timeout);
	}
}
