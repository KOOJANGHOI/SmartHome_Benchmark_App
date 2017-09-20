package iotcode.DoorlockSensor;

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
public class DoorlockSensor implements IoTZigbeeCallback, DoorLock {

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
	private AtomicBoolean didBind = new AtomicBoolean(false);
	private AtomicBoolean didDoorLockConfigureReporting = new AtomicBoolean(false); //made by Jiawei
	static Semaphore gettingLatestDataMutex = new Semaphore(1);

	private List < DoorLockCallbackSmart > callbackList = new CopyOnWriteArrayList < DoorLockCallbackSmart > ();

	private int sensorId = 0;

	@config private IoTSet<IoTDeviceAddress> DoorlockSensorUdpAddress;
	@config private IoTSet<IoTZigbeeAddress> DoorlockSensorZigbeeAddress;

	public DoorlockSensor(IoTSet<IoTDeviceAddress> dSet, IoTSet<IoTZigbeeAddress> zigSet) {
		DoorlockSensorUdpAddress = dSet;
		DoorlockSensorZigbeeAddress = zigSet;
	}

	public DoorlockSensor() {
	}

	public void init() {

		if (didAlreadyInit.compareAndSet(false, true) == false) {
			return; // already init
		}

		didAlreadyClose.set(false);

		try {
			Iterator itrUdp = DoorlockSensorUdpAddress.iterator();
			Iterator itrZig = DoorlockSensorZigbeeAddress.iterator();

			zigConnection = new IoTZigbee((IoTDeviceAddress)itrUdp.next(), (IoTZigbeeAddress)itrZig.next());

			// DEBUG
			System.out.println("DEBUG: Allocate iterators to print out addresses!");
			Iterator itrDebugUdp = DoorlockSensorUdpAddress.iterator();
			IoTDeviceAddress iotaddDebug = (IoTDeviceAddress)itrDebugUdp.next();
			System.out.println("IP address: " + iotaddDebug.getCompleteAddress());
			System.out.println("Source port: " + iotaddDebug.getSourcePortNumber());
			System.out.println("Destination port: " + iotaddDebug.getDestinationPortNumber());

			Iterator itrDebugZig = DoorlockSensorZigbeeAddress.iterator();
			IoTZigbeeAddress iotzbaddDebug = (IoTZigbeeAddress)itrDebugZig.next();
			System.out.println("Zigbee address: " + iotzbaddDebug.getAddress());

			zigConnection.registerCallback(this);
			System.out.println("Register callback!");
			zigConnection.init();
			System.out.println("Initialized!");


            
			//made by changwoo
			sleep(10);

            // System.out.println("BroadcastingRouteRecordRequest ");
			// zigConnection.sendBroadcastingRouteRecordRequest(0x0001);
            // sleep(6);

			System.out.println("Sending Management Permit Joining Request");
			// for(int z=0; z<3; z++){
				zigConnection.sendManagementPermitJoiningRequest(0x0002, 0x0036, 0x00);
				sleep(0);
			// }


            while(!didBind.get()){
				System.out.println("Sending Bind Request");
				zigConnection.sendBindRequest(0x0003, 0x0101, 0x02);
				sleep(0);
			}
			
			while(!didDoorLockConfigureReporting.get()){
				System.out.println("Sending Door Lock: Configure Reporting");
				zigConnection.sendConfigureReportingCommand(0x0004, 0x0101, 0x0104, 0x01, 0x02, 0x0000, 0x30, 0x0000, 0x100E, null);
				sleep(0);
			}
/*
			while(true){
				Scanner in = new Scanner(System.in);
				System.out.println("\nUnlock door: 0");
				System.out.println("Lock door: 1");
				System.out.println("Read status: 2 (or anything else)");
				String str = in.next();
				if(str.equals("1")) {
					System.out.println("the doorlock sensor is locking");
					zigConnection.sendLockOrUnlockDoorRequest(0x0005, 0x0101, 0x0104, 0x02, 0);
					sleep(0);
				}else if(str.equals("0")){
					System.out.println("the doorlock sensor is unlocking");
					zigConnection.sendLockOrUnlockDoorRequest(0x0005, 0x0101, 0x0104, 0x02, 1);
					sleep(0);
				}else{
					System.out.println("Let's see the doorlock sensor's status currently");
					zigConnection.sendReadDoorStatusRequest(0x0005, 0x0101, 0x0104, 0x02, 0x10, 0x00, 0x0000);
					sleep(0);
				}
			}
*/

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void LockDoor(){
        try{
            System.out.println("the doorlock sensor is locking");
		    zigConnection.sendLockOrUnlockDoorRequest(0x0005, 0x0101, 0x0104, 0x02, 0);
    
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void UnlockDoor(){
        try{
            System.out.println("the doorlock sensor is unlocking");
	        zigConnection.sendLockOrUnlockDoorRequest(0x0005, 0x0101, 0x0104, 0x02, 1);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void StatusRequest(){
        try{       
            System.out.println("Let's see the doorlock sensor's status currently");
	        zigConnection.sendReadDoorStatusRequest(0x0005, 0x0101, 0x0104, 0x02, 0x10, 0x00, 0x0000);
        }catch(Exception e){
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

	// made by Jiawei
    //public int getStatus() {
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

	public boolean isActiveValue() {

		int tmp = getValue();
		if (tmp == 1)
			detectStatus = true;	// Door is locked
		else
			detectStatus = false;	// Door is not locked/not fully locked
		return detectStatus;
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
		
		//made by yuting
		if (_zm instanceof IoTZigbeeMessageZdoBindResponse) {
			IoTZigbeeMessageZdoBindResponse message = (IoTZigbeeMessageZdoBindResponse)_zm;
			if (message.getSucceeded()) {
				didBind.set(true);
			}
		}
		else if (_zm instanceof IoTZigbeeMessageZclConfigureReportingResponse){
			IoTZigbeeMessageZclConfigureReportingResponse message = (IoTZigbeeMessageZclConfigureReportingResponse)_zm;
			if (message.getAllSuccess()) {
				didDoorLockConfigureReporting.set(true);
			}
		}
		else if (_zm instanceof IoTZigbeeMessageZclReadAttributesResponse) {
			IoTZigbeeMessageZclReadAttributesResponse message = (IoTZigbeeMessageZclReadAttributesResponse)_zm;
			List <IoTZigbeeMessageZclReadAttributesResponse.Attribute> attrList = message.getAttributes();

			if (attrList.size() == 1) {
				if(attrList.get(0).getAttributeId() == 0) {
					byte[] data = attrList.get(0).getData();
					int value = data[0];

					try {
						gettingLatestDataMutex.acquire();
						detectedValue = value;
						timestampOfLastDetecting = new Date();
					} catch (Exception e) {
						e.printStackTrace();
					}
					gettingLatestDataMutex.release();

					try {
						for (DoorLockCallbackSmart cb : callbackList) {
							cb.newReadingAvailable(this.getId(), this.getValue(), this.isActiveValue());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
			}
		}
	}

	public void registerCallback(DoorLockCallbackSmart _callbackTo) {
		callbackList.add(_callbackTo);
	}
}
