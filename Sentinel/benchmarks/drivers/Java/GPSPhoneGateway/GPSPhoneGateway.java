package iotcode.GPSPhoneGateway;

// Java standard library
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Iterator;
import java.util.List;
import java.net.UnknownHostException;

// RMI Packages
import java.rmi.Remote;
import java.rmi.RemoteException;

// IoTRuntime library
import iotruntime.stub.IoTRemoteCall;
import iotruntime.slave.IoTSet;
import iotruntime.slave.IoTDeviceAddress;
import iotcode.annotation.*;
import iotcode.interfaces.*;

// Checker annotations
//import iotchecker.qual.*;

/** GPSPhoneGateway that uses IoTRemoteCall and PhoneInfo class
 *  to get information from a phone app
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0                
 * @since       2016-04-27
 */
public class GPSPhoneGateway implements GPSGateway {

	/**
	 * PhoneGateway class properties
	 */
	private PhoneInfo phoneInfo;
	private IoTRemoteCall iotRemCall;
	private List<GPSGatewaySmartCallback> listPGWCallback;
	private AtomicBoolean doEnd;
	private Thread callbackThread;
	private Thread workerThread;
	private IoTDeviceAddress iotDevAdd;

	@config private IoTSet<IoTDeviceAddress> gps_address;

	/**
	 * Constructor
	 */
	public GPSPhoneGateway() throws RemoteException {
	}

	/**
	 * Init() function
	 */
	public void init() {

		// Get address
		Iterator it = gps_address.iterator();
		iotDevAdd = (IoTDeviceAddress) it.next();
//		try {
//			iotDevAdd = new IoTDeviceAddress("192.168.2.100", 1234, 8000);
//		} catch (Exception ex) {
//		}
		System.out.println("Address: " + iotDevAdd.getCompleteAddress());
		System.out.println("Source port: " + iotDevAdd.getSourcePortNumber());
		System.out.println("Destination port: " + iotDevAdd.getDestinationPortNumber());

		// Get server
		phoneInfo = new PhoneInfo();
		listPGWCallback = new ArrayList<GPSGatewaySmartCallback>();
		doEnd = new AtomicBoolean(false);

		// Threads
		callbackThread = null;
		workerThread = null;
	}

	/**
	 * Start() function to start threads
	 */
	public void start() {
		doEnd.set(false);

		// Launch IoTRemoteCall server in a separate thread
		workerThread = new Thread(new Runnable() {
			public void run() {
				iotRemCall = new IoTRemoteCall(PhoneInfoInterface.class, 
					phoneInfo, iotDevAdd.getDestinationPortNumber(),
					IoTDeviceAddress.getLocalHostAddress());
			}
		});
		workerThread.start();
		System.out.println("GPSPhoneGateway: Worker thread started!");

		callbackThread = new Thread(new Runnable() {
			public void run() {
				doCallbacks();
			}
		});
		callbackThread.start();
		System.out.println("GPSPhoneGateway: Callback thread started!");
	}

	/**
	 * Stop() function to stop threads
	 */
	public void stop() {
		doEnd.set(true);

		try {
			callbackThread.join();
			workerThread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Register callbacks
	 */
	public void registerCallback(GPSGatewaySmartCallback _c) {
		listPGWCallback.add(_c);
	}

	/**
	 * Do callbacks
	 */
	private void doCallbacks() {

		while (!doEnd.get()) {

			for (GPSGatewaySmartCallback c : listPGWCallback) {

				//try {
					// Only call back if there is new data	
					if (phoneInfo.isNewRoomIDAvailable()) {

						System.out.println("GPSPhoneGateway: new room ID available - call back!");
						// Call back!
						//c.newRoomIDRetrieved(this);
						c.newRoomIDRetrieved(this.getRoomID());
						//this.setNewRoomIDAvailable(false);

						// Set back to false after reading
						phoneInfo.setNewRoomIDAvailable(false);

					} else if (phoneInfo.isNewRingStatusAvailable()) {

						System.out.println("GPSPhoneGateway: new ring status available - call back!");
						// Call back!
						//c.newRingStatusRetrieved(this);
						c.newRingStatusRetrieved(this.getRingStatus());
						//this.setNewRingStatusAvailable(false);

						// Set back to false after reading
						phoneInfo.setNewRingStatusAvailable(false);
					}

				//} catch (RemoteException ex) {
				//	ex.printStackTrace();
				//}
			}
		}
	}

	/**
	 * Simply return phoneInfo.iRoomIdentifier
	 */
	public int getRoomID() {

		return phoneInfo.getRoomID();
	}

	/**
	 * Simply return phoneInfo.bRingStatus
	 */
	public boolean getRingStatus() {

		return phoneInfo.getRingStatus();
	}

	/**
	 * Set phoneInfo.bNewRoomIDAvail
	 */
	public void setNewRoomIDAvailable(boolean bValue) {

		phoneInfo.setNewRoomIDAvailable(bValue);
	}

	/**
	 * Set phoneInfo.bNewRingStatusAvail
	 */
	public void setNewRingStatusAvailable(boolean bValue) {

		phoneInfo.setNewRingStatusAvailable(bValue);
	}

/*	public static void main(String[] args) throws UnknownHostException, RemoteException {

		@LocalRemote GPSPhoneGateway gpg = new @LocalRemote GPSPhoneGateway();
		gpg.init();
		gpg.start();
	}*/
}
