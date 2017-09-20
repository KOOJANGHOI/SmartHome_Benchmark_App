import java.util.Map;
import java.net.*;
import java.io.*;
import java.util.*;

import iotruntime.*;
import iotruntime.zigbee.*;

import iotcode.interfaces.*;
import java.rmi.RemoteException;

//public class ZigbeeTest_outlet implements IoTZigbeeCallback {
public class ZigbeeTest_outlet implements SmartthingsSensorCallback {
    public final int SOCKET_SEND_BUFFER_SIZE = 1024;
    public final int SOCKET_RECEIVE_BUFFER_SIZE = 1024;
    private static final String MY_IP_ADDRESS = "127.0.0.1";
    public static final String DEVIDE_MAC_ADDRESS = "000d6f0005782e31"; //outlet sensor

    public void newReadingAvailable(SmartthingsSensor _sensor) {
	//public void newReadingAvailable(SmartthingsSensor _sensor) throws RemoteException {
        System.out.println("New Message!!!!");
        System.out.println(((OutletSensor)_sensor).getWatts());
    }

    public static void main(String[] args) throws UnknownHostException, SocketException, InterruptedException, IOException, IOException, RemoteException{

        String message = "type: policy_set\n";
        message += "ip_address: " + MY_IP_ADDRESS + "\n"; // local ip address
        message += "port: " + "5959\n";  // port number
        message += "device_address_long: " + DEVIDE_MAC_ADDRESS + "\n";
        DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName("127.0.0.1"), 5005); // address and port of the gateway which means Raspberry PI's IP address
        DatagramSocket socket = new DatagramSocket(12345/*test number*/);
        socket.setSendBufferSize(4096);
        socket.setReceiveBufferSize(4096);
        socket.send(sendPacket);
        socket.setReuseAddress(true);
        socket.close();

        IoTDeviceAddress zigUdpAddr  = new IoTDeviceAddress("127.0.0.1", 5959, 5005,false,false);
        IoTZigbeeAddress zigAddrLong = new IoTZigbeeAddress(DEVIDE_MAC_ADDRESS);

        Set<IoTZigbeeAddress> zigSet = new HashSet<IoTZigbeeAddress>();
        zigSet.add(zigAddrLong);
        IoTSet<IoTZigbeeAddress> zigIotSet = new IoTSet<IoTZigbeeAddress>(zigSet);

        Set<IoTDeviceAddress> devSet = new HashSet<IoTDeviceAddress>();
        devSet.add(zigUdpAddr);
        IoTSet<IoTDeviceAddress> devIotSet = new IoTSet<IoTDeviceAddress>(devSet);
        OutletSensor sen = new OutletSensor(devIotSet, zigIotSet);
        
        System.out.println("About to init");
        sen.init();

        ZigbeeTest_outlet zTest = new ZigbeeTest_outlet();
        sen.registerCallback(zTest);
        sen.TurnOn();
        sen.TurnOff();
	sen.TurnOn();
        System.out.println("Watt:");
        System.out.println(sen.getWatts());
        sen.TurnOff();
        System.out.println("Loop Begin");
        while (true) {

        }
    }

}
