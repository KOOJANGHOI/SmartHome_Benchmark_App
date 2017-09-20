import java.util.Map;
import java.net.*;
import java.io.*;
import java.util.*;

import iotruntime.*;
import iotruntime.zigbee.*;

//import iotcode.interfaces.*;
import java.rmi.RemoteException;

public class ZigbeeTest_motion implements SmartthingsSensorCallback {
    public final int SOCKET_SEND_BUFFER_SIZE = 1024;
    public final int SOCKET_RECEIVE_BUFFER_SIZE = 1024;
    private static final String MY_IP_ADDRESS = "192.168.1.198";
    public static final String DEVIDE_MAC_ADDRESS = "000d6f000bbd5398"; //motion

//000d6f000bbd5398
//000d6f00057c92a7
    public void newReadingAvailable(int _value, boolean _activeValue) {
        System.out.println("New Message!!!!");
        System.out.println("motion : "+ _value);
        System.out.println("active? : "+ _activeValue);
    }

    public static void main(String[] args) throws UnknownHostException, SocketException, InterruptedException, IOException, IOException, RemoteException{

        String message = "type: policy_set\n";
        message += "ip_address: " + MY_IP_ADDRESS + "\n"; // local ip address
        message += "port: " + "5956\n";  // port number
        message += "device_address_long: " + DEVIDE_MAC_ADDRESS + "\n";
        DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName("192.168.1.192"), 5005); // address and port of the gateway which means Raspberry PI's IP address
        //DatagramSocket socket = new DatagramSocket(11222);
        DatagramSocket socket = new DatagramSocket(12345);
        socket.setSendBufferSize(4096);
        socket.setReceiveBufferSize(4096);
        socket.send(sendPacket);
	socket.setReuseAddress(true);
	socket.close();

        //IoTDeviceAddress zigUdpAddr  = new IoTDeviceAddress("192.168.2.227", 5956, 5005,false,false);
        IoTDeviceAddress zigUdpAddr  = new IoTDeviceAddress("192.168.1.192", 5956, 5005,false,false);
        IoTZigbeeAddress zigAddrLong = new IoTZigbeeAddress(DEVIDE_MAC_ADDRESS);

        Set<IoTZigbeeAddress> zigSet = new HashSet<IoTZigbeeAddress>();
        zigSet.add(zigAddrLong);
        IoTSet<IoTZigbeeAddress> zigIotSet = new IoTSet<IoTZigbeeAddress>(zigSet);

        Set<IoTDeviceAddress> devSet = new HashSet<IoTDeviceAddress>();
        devSet.add(zigUdpAddr);
        IoTSet<IoTDeviceAddress> devIotSet = new IoTSet<IoTDeviceAddress>(devSet);
        MotionSensor sen = new MotionSensor(devIotSet, zigIotSet);
        
        System.out.println("About to init");
        sen.init();

        ZigbeeTest_motion zTest = new ZigbeeTest_motion();
	sen.registerCallback(zTest);

        System.out.println("Loop Begin");
        while (true) {

        }
    }
}
