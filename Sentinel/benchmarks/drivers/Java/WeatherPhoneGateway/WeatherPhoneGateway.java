package iotcode.WeatherPhoneGateway;

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

/** WeatherPhoneProxy that uses IoTRemoteCall and WeatherInfo class
 *  to get information from a phone app
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0                
 * @since       2016-04-26
 */
public class WeatherPhoneGateway implements WeatherGateway {

	/**
	 * PhoneGateway class properties
	 */
	private WeatherInfo weatherInfo;
	private IoTRemoteCall iotRemCall;
	private List<WeatherGatewaySmartCallback> listPGWCallback;
	private AtomicBoolean doEnd;
	private Thread callbackThread;
	private Thread workerThread;
	private IoTDeviceAddress iotDevAdd;

	@config private IoTSet<IoTDeviceAddress> ph_address;

	/**
	 * Constructor
	 */
	/*public WeatherPhoneGateway() throws RemoteException, UnknownHostException {

		iotDevAdd = new IoTDeviceAddress("192.168.2.101", 1234, 8000);
		weatherInfo = new WeatherInfo();

		// Launch IoTRemoteCall server in a separate thread
		workerThread = new Thread(new Runnable() {
			public void run() {
				iotRemCall = new IoTRemoteCall(WeatherInfoInterface.class, 
					weatherInfo, iotDevAdd.getDestinationPortNumber());
			}
		});
		workerThread.start();

		System.out.println("PhoneGateway is started");
	
	}*/
	public WeatherPhoneGateway() {
	}

	/**
	 * Init() function
	 */
	public void init() {

		// Get address
		Iterator it = ph_address.iterator();
		iotDevAdd = (IoTDeviceAddress) it.next();
//		try {
//			iotDevAdd = new IoTDeviceAddress("192.168.2.101", 1234, 8000);
//		} catch (Exception ex) {
//		}
		System.out.println("Address: " + iotDevAdd.getCompleteAddress());
		System.out.println("Source port: " + iotDevAdd.getSourcePortNumber());
		System.out.println("Destination port: " + iotDevAdd.getDestinationPortNumber());

		// Get server
		weatherInfo = new WeatherInfo();
		System.out.println("DEBUG: Is new data available: " + weatherInfo.isNewDataAvailable());
		listPGWCallback = new ArrayList<WeatherGatewaySmartCallback>();
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
				iotRemCall = new IoTRemoteCall(WeatherInfoInterface.class, 
					weatherInfo, iotDevAdd.getDestinationPortNumber(),
					IoTDeviceAddress.getLocalHostAddress());
			}
		});
		workerThread.start();
		System.out.println("DEBUG: Started IoTRemoteCall object!!!");

		callbackThread = new Thread(new Runnable() {
			public void run() {
				doCallbacks();
			}
		});
		callbackThread.start();
		System.out.println("DEBUG: Do Callbacks!!!");
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
	public void registerCallback(WeatherGatewaySmartCallback _c) {
		listPGWCallback.add(_c);
	}

	/**
	 * Do callbacks
	 */
	private void doCallbacks() {

		while (!doEnd.get()) {
			// Only call back if there is new data
			if (weatherInfo.isNewDataAvailable()) {
				System.out.println("We get into doCallbacks!");
				System.out.println("weatherInfo.isNewDataAvailable(): " + weatherInfo.isNewDataAvailable());
				for (WeatherGatewaySmartCallback c : listPGWCallback) {
					//try {
						//c.informationRetrieved(this);
						c.informationRetrieved(this.getInchesPerWeek(), this.getWeatherZipCode(), this.getDaysToWaterOn(), this.getInchesPerMinute());
					//} catch (RemoteException ex) {
					//	ex.printStackTrace();
					//}
					// We have read data - set this back to false
				}
				weatherInfo.setNewDataAvailable(false);
			}
		}
	}

	/**
	 * Simply return this.dInchesPerWeek
	 */
	public double getInchesPerWeek() {

		return weatherInfo.getInchesPerWeek();
	}

	/**
	 * Simply return this.iWeatherZipCode
	 */
	public int getWeatherZipCode() {

		return weatherInfo.getWeatherZipCode();
	}

	/**
	 * Simply return this.iDaysToWaterOn
	 */
	public int getDaysToWaterOn() {

		return weatherInfo.getDaysToWaterOn();
	}

	/**
	 * Simply return this.dInchesPerMinute
	 */
	public double getInchesPerMinute() {

		return weatherInfo.getInchesPerMinute();
	}


//	public static void main(String[] args) throws UnknownHostException, RemoteException {

//		@LocalRemote WeatherPhoneGateway wpg = new @LocalRemote WeatherPhoneGateway();
//		wpg.init();
//		wpg.start();
//	}
}
