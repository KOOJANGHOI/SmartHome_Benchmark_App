import java.util.Map;
import java.net.*;
import java.io.*;
import java.util.*;

import iotruntime.*;
import iotruntime.zigbee.*;

import iotcode.interfaces.*;
import java.rmi.RemoteException;

//public class ZigbeeTest_doorlock implements IoTZigbeeCallback {
public class ZigbeeTest_doorlock implements SmartthingsSensorCallback {
    public final int SOCKET_SEND_BUFFER_SIZE = 1024;
    public final int SOCKET_RECEIVE_BUFFER_SIZE = 1024;
    private static final String MY_IP_ADDRESS = "192.168.1.198";
    public static final String DEVIDE_MAC_ADDRESS = "002446fffd00b0ba"; //doorlock sensor

    //public void newReadingAvailable(SmartthingsSensor _sensor) {
	public void newReadingAvailable(int _value, boolean _activeValue) {

		System.out.println("New Message!!!!");
		//int status = ((DoorlockSensor)_sensor).getStatus();
		int status = _value;
		switch (status) {
			case 0:
				System.out.println("Not fully locked");
				break;
			case 1:
				System.out.println("Locked");
				break;
			case 2:
				System.out.println("Unlocked");
				break;
			default:
				System.out.println("Unknown value: " + status);
				break;
		}
	}

    public static void main(String[] args) throws UnknownHostException, SocketException, InterruptedException, IOException, IOException, RemoteException{

        String message = "type: policy_set\n";
        message += "ip_address: " + MY_IP_ADDRESS + "\n"; // local ip address
        message += "port: " + "5959\n";  // port number
        message += "device_address_long: " + DEVIDE_MAC_ADDRESS + "\n";
        DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName("192.168.1.192"), 5005); // address and port of the gateway which means Raspberry PI's IP address
        DatagramSocket socket = new DatagramSocket(12345/*test number*/);
        socket.setSendBufferSize(4096);
        socket.setReceiveBufferSize(4096);
        socket.send(sendPacket);
        socket.setReuseAddress(true);
        socket.close();

        IoTDeviceAddress zigUdpAddr  = new IoTDeviceAddress("192.168.1.192", 5959, 5005,false,false);
        IoTZigbeeAddress zigAddrLong = new IoTZigbeeAddress(DEVIDE_MAC_ADDRESS);

        Set<IoTZigbeeAddress> zigSet = new HashSet<IoTZigbeeAddress>();
        zigSet.add(zigAddrLong);
        IoTSet<IoTZigbeeAddress> zigIotSet = new IoTSet<IoTZigbeeAddress>(zigSet);

        Set<IoTDeviceAddress> devSet = new HashSet<IoTDeviceAddress>();
        devSet.add(zigUdpAddr);
        IoTSet<IoTDeviceAddress> devIotSet = new IoTSet<IoTDeviceAddress>(devSet);
        DoorlockSensor sen = new DoorlockSensor(devIotSet, zigIotSet);
        
        ZigbeeTest_doorlock zTest = new ZigbeeTest_doorlock();
        sen.registerCallback(zTest);

        System.out.println("About to init");
        sen.init();

        // ZigbeeTest_doorlock zTest = new ZigbeeTest_doorlock();
        // sen.registerCallback(zTest);


        System.out.println("Loop Begin");
        while (true) {

        }
    }

}
