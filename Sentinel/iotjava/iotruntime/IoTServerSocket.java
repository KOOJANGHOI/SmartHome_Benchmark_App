package iotruntime;

// Java packages
import java.io.IOException;
import java.net.UnknownHostException;
import java.net.SocketException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;

import iotruntime.slave.IoTDeviceAddress;

/** Class IoTServerSocket is a wrapper class that provides
 *  minimum interfaces for user to interact with IoT
 *  devices in our system using ServerSockets
 *
 * @author      Ali Younid <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-05-03
 */
public final class IoTServerSocket {

    /**
     * IoTTCP class properties
     */
    private ServerSocket sock;


    /**
     * Class constructor
     */
    public IoTServerSocket(IoTDeviceAddress iotDevAdd) throws UnknownHostException, IOException {
        int iDstPort = iotDevAdd.getDestinationPortNumber();
        sock = new ServerSocket(iDstPort);
    }


    /**
    * accept() method
    */
    public IoTTCP accept() throws UnknownHostException, IOException {
        Socket recSock = sock.accept();
        return new IoTTCP(recSock);
    }


    /**
    * setPerformancePreferences(int connectionTime, int latency, int bandwidth) method
    */
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) throws SocketException, IOException {
        sock.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    /**
    * setReceiveBufferSize(int size) method
    */
    public void setReceiveBufferSize(int size) throws SocketException, IOException {
        sock.setReceiveBufferSize(size);
    }

    /**
    * setReuseAddress(boolean on) method
    */
    public void setReuseAddress(boolean on) throws SocketException, IOException {
        sock.setReuseAddress(on);
    }

    /**
    * setSoTimeout(int timeout) method
    */
    public void setSoTimeout(int timeout) throws SocketException, IOException {
        sock.setSoTimeout(timeout);
    }

    /**
    * close() method
    */
    public void close() throws SocketException, IOException {
        sock.close();
    }

    /**
    * getLocalPort() method
    */
    public int getLocalPort() throws SocketException, IOException {
        return sock.getLocalPort();
    }

    /**
    * getReceiveBufferSize() method
    */
    public int getReceiveBufferSize() throws SocketException, IOException {
        return sock.getReceiveBufferSize();
    }

    /**
    * getReuseAddress() method
    */
    public boolean getReuseAddress() throws SocketException, IOException {
        return sock.getReuseAddress();
    }

    /**
    * getSoTimeout() method
    */
    public int getSoTimeout() throws SocketException, IOException {
        return sock.getSoTimeout();
    }

    /**
    * isClosed() method
    */
    public boolean isClosed() throws SocketException, IOException {
        return sock.isClosed();
    }

    /**
    * isBound() method
    */
    public boolean isBound() throws SocketException, IOException {
        return sock.isBound();
    }

}
