package iotcode.MultipurposeSensor;

// Standard Java Packages
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

// Checker annotations
//import iotchecker.qual.*;
import iotcode.annotation.*;

// IoT Packages
import iotruntime.slave.*;
import iotcode.interfaces.*;
import iotruntime.zigbee.*;

/** Class Smartthings sensor driver for Smartthings sensor devices.
 *
 * @author      Changwoo Lee, Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2016-12-01
 */
public class MultipurposeSensor implements IoTZigbeeCallback, SmartthingsSensor {

	private final int TIMEOUT_FOR_RESEND_MSEC = 900;

	private IoTZigbee zigConnection = null;
	private boolean didClose; // make sure that the clean up was done correctly
	private boolean detectStatus = false;

	private int detectedValue = 0;
	private Date timestampOfLastDetecting = null;

	private AtomicBoolean didAlreadyClose = new AtomicBoolean(true);
	private AtomicBoolean didAlreadyInit = new AtomicBoolean(false);
	private AtomicBoolean didWriteAttrb = new AtomicBoolean(false);
	private AtomicBoolean didMatchDscr = new AtomicBoolean(false);
	static Semaphore gettingLatestDataMutex = new Semaphore(1);

	private List < SmartthingsSensorSmartCallback > callbackList = new CopyOnWriteArrayList < SmartthingsSensorSmartCallback > ();

	private int sensorId = 0;

	@config private IoTSet<IoTDeviceAddress> multipurposeSensorUdpAddress;
	@config private IoTSet<IoTZigbeeAddress> multipurposeSensorZigbeeAddress;

	//public MultipurposeSensor(IoTSet<IoTDeviceAddress> dSet, IoTSet<IoTZigbeeAddress> zigSet) {
		//multipurposeSensorUdpAddress = dSet;
		//multipurposeSensorZigbeeAddress = zigSet;
	//}
	public MultipurposeSensor() {
	}

	public void init() {

		if (didAlreadyInit.compareAndSet(false, true) == false) {
			return; // already init
		}
		didAlreadyClose.set(false);

		try {
			Iterator itrUdp = multipurposeSensorUdpAddress.iterator();
			Iterator itrZig = multipurposeSensorZigbeeAddress.iterator();

			zigConnection = new IoTZigbee((IoTDeviceAddress)itrUdp.next(), (IoTZigbeeAddress)itrZig.next());

			// DEBUG
			System.out.println("DEBUG: Allocate iterators to print out addresses!");
			Iterator itrDebugUdp = multipurposeSensorUdpAddress.iterator();
			IoTDeviceAddress iotaddDebug = (IoTDeviceAddress)itrDebugUdp.next();
			System.out.println("IP address: " + iotaddDebug.getCompleteAddress());
			System.out.println("Source port: " + iotaddDebug.getSourcePortNumber());
			System.out.println("Destination port: " + iotaddDebug.getDestinationPortNumber());

			Iterator itrDebugZig = multipurposeSensorZigbeeAddress.iterator();
			IoTZigbeeAddress iotzbaddDebug = (IoTZigbeeAddress)itrDebugZig.next();
			System.out.println("Zigbee address: " + iotzbaddDebug.getAddress());

			zigConnection.registerCallback(this);
			System.out.println("Register callback!");
			zigConnection.init();
			System.out.println("Initialized!");

			//made by changwoo
			sleep(10);
			System.out.println("Sending Management Permit Joining Request");
			for(int z=0; z<3; z++){
				zigConnection.sendManagementPermitJoiningRequest(0x0001, 0x0036, 0x00);
				sleep(0);
			}

			//made by changwoo
			while (!didWriteAttrb.get()) {
				System.out.println("Sending Write Attribute Request");
				zigConnection.sendWriteAttributesCommand(0x0002, 0x0500, 0x0104, 0x01);
				sleep(0);
			}

			//made by changwoo
			System.out.println("Sending Enrollment Reponse");
			zigConnection.sendEnrollmentResponse(0x0003, 0x0500, 0x0104, 0x01);
			sleep(0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//made by changwoo
	private void sleep(int multipleTime){
		if(multipleTime<=0){
			multipleTime=1;
		}
		try{
			Thread.sleep(TIMEOUT_FOR_RESEND_MSEC*multipleTime);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public void close() {

		if (didAlreadyClose.compareAndSet(false, true) == false) {
			return; // already init
		}

		didAlreadyInit.set(false);


		try {
			zigConnection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void Finalize() {
		if (!didClose) {
			close();
		}
	}

	public void setId(int id) {

		sensorId = id;

	}

	public int getId() {

		return sensorId;

	}

	public int getValue() {

		int tmp = 0;
		try {
			gettingLatestDataMutex.acquire();
			tmp = detectedValue;

		} catch (Exception e) {
			e.printStackTrace();
		}
		gettingLatestDataMutex.release();

		return tmp;
	}

	// MultipurposeSensor: 
	// - 24 = close = false
	// - 25 = open = true
	public boolean isActiveValue() {

		int tmp = getValue();
		if (tmp == 25)
			detectStatus = true;
		else // Getting 24 here
			detectStatus = false;

		return detectStatus;
	}

	public long getTimestampOfLastReading() {

		Date tmp = null;
		try {
			gettingLatestDataMutex.acquire();
			tmp = (Date)timestampOfLastDetecting.clone();

		} catch (Exception e) {
			e.printStackTrace();
		}
		gettingLatestDataMutex.release();
		long retLong = tmp.getTime();

		return retLong;
	}

	public void newMessageAvailable(IoTZigbeeMessage _zm) {

		//made by changwoo
		if(_zm instanceof IoTZigbeeMessageZclZoneStatusChangeNotification){
			IoTZigbeeMessageZclZoneStatusChangeNotification message = (IoTZigbeeMessageZclZoneStatusChangeNotification)_zm;
			if(message.getSuccessOrFail()){
				//do something!
				try {
					gettingLatestDataMutex.acquire();
					detectedValue = message.getStatus();
					timestampOfLastDetecting = new Date();
				} catch (Exception e) {
					e.printStackTrace();
				}
				gettingLatestDataMutex.release();
				try {
					for (SmartthingsSensorSmartCallback cb : callbackList) {
						cb.newReadingAvailable(this.getId(), this.getValue(), this.isActiveValue());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}//if
		
		//made by changwoo
		}//if
		else if (_zm instanceof IoTZigbeeMessageZclWriteAttributesResponse) {
			IoTZigbeeMessageZclWriteAttributesResponse message = (IoTZigbeeMessageZclWriteAttributesResponse)_zm;
			if (message.getSuccessOrFail()) {
				didWriteAttrb.set(true);
			}//if
		}//else if
	}

	public void registerCallback(SmartthingsSensorSmartCallback _callbackTo) {
		callbackList.add(_callbackTo);
	}
}
