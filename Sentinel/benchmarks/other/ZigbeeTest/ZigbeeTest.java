import java.util.Map;
import java.net.*;
import java.io.*;
import java.util.*;

import iotruntime.*;
import iotruntime.zigbee.*;

//import iotcode.interfaces.*;
import java.rmi.RemoteException;

public class ZigbeeTest implements MoistureSensorSmartCallback {

    public final int SOCKET_SEND_BUFFER_SIZE = 1024;
    public final int SOCKET_RECEIVE_BUFFER_SIZE = 1024;


    public ZigbeeTest() {
    }


    public void newReadingAvailable(int sensorId, float moisture, long timeStampOfLastReading) {
        System.out.println("New Message!!!!");
        System.out.println(moisture);
        System.out.println("Reading time: " + timeStampOfLastReading);
    }






    public static void main(String[] args) throws UnknownHostException, SocketException, InterruptedException, IOException, IOException, RemoteException{

        String message = "type: policy_set\n";
	message += "ip_address: " + "192.168.2.108\n"; // local ip address
        //message += "port: " + "5959\n";  // port number
	message += "port: " + "5557\n";  // port number
        message += "device_address_long: " + "000d6f0003ebf2ee" + "\n";
        //DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName("128.195.204.110"), 5005); // address and port of the gateway
	DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName("192.168.2.192"), 5005); // address and port of the gateway

        DatagramSocket socket = new DatagramSocket();
        socket.setSendBufferSize(4096);
        socket.setReceiveBufferSize(4096);

        socket.send(sendPacket);




        //IoTDeviceAddress zigUdpAddr  = new IoTDeviceAddress("128.195.204.110", 5557, 5005,false,false);
	IoTDeviceAddress zigUdpAddr  = new IoTDeviceAddress("192.168.2.192", 5557, 5005,false,false);
        IoTZigbeeAddress zigAddrLong = new IoTZigbeeAddress("000d6f0003ebf2ee");

        Set<IoTZigbeeAddress> zigSet = new HashSet<IoTZigbeeAddress>();
        zigSet.add(zigAddrLong);
        IoTSet<IoTZigbeeAddress> zigIotSet = new IoTSet<IoTZigbeeAddress>(zigSet);

        Set<IoTDeviceAddress> devSet = new HashSet<IoTDeviceAddress>();
        devSet.add(zigUdpAddr);
        IoTSet<IoTDeviceAddress> devIotSet = new IoTSet<IoTDeviceAddress>(devSet);
        SpruceSensor sen = new SpruceSensor(devIotSet, zigIotSet);
        
        System.out.println("About to init");
        sen.init();
	System.out.println("Passed init!");
        ZigbeeTest zTest = new ZigbeeTest();
	System.out.println("ZigbeeTest created!");
        sen.registerCallback(zTest);


        System.out.println("Loop Begin");
        while (true) {

        }
    }
}
