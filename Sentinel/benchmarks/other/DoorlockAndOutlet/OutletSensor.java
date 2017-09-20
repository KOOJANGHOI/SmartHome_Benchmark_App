//package iotcode.OutletSensor;

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
public class OutletSensor implements IoTZigbeeCallback, SmartthingsSensor {

	private final int TIMEOUT_FOR_RESEND_MSEC = 900;

	private IoTZigbee zigConnection = null;

	private float Watts = 0;
	private boolean didClose; // make sure that the clean up was done correctly
	//private boolean detectStatus = false;

	//private int detectedValue = 0;
	private Date timestampOfLastDetecting = null;

	private AtomicBoolean didAlreadyClose = new AtomicBoolean(true);
	private AtomicBoolean didAlreadyInit = new AtomicBoolean(false);
	private AtomicBoolean didWriteAttrb = new AtomicBoolean(false);
	//private AtomicBoolean didMatchDscr = new AtomicBoolean(false);
	private AtomicBoolean didBind = new AtomicBoolean(false);/////////yuting
	private AtomicBoolean didConfigureReporting = new AtomicBoolean(false);/////////////yuting
	static Semaphore gettingLatestDataMutex = new Semaphore(1);

	private List < SmartthingsSensorCallback > callbackList = new CopyOnWriteArrayList < SmartthingsSensorCallback > ();

	private int sensorId = 0;

	@config private IoTSet<IoTDeviceAddress> OutletSensorUdpAddress; //
	@config private IoTSet<IoTZigbeeAddress> OutletSensorZigbeeAddress;//

	public OutletSensor(IoTSet<IoTDeviceAddress> dSet, IoTSet<IoTZigbeeAddress> zigSet) {
		OutletSensorUdpAddress = dSet;
		OutletSensorZigbeeAddress = zigSet;
	}

	public OutletSensor() {
	}

	public void init() {

		if (didAlreadyInit.compareAndSet(false, true) == false) {
			return; // already init
		}

		didAlreadyClose.set(false);

		try {
			Iterator itrUdp = OutletSensorUdpAddress.iterator();
			Iterator itrZig = OutletSensorZigbeeAddress.iterator();

			zigConnection = new IoTZigbee((IoTDeviceAddress)itrUdp.next(), (IoTZigbeeAddress)itrZig.next());

			// DEBUG
			System.out.println("DEBUG: Allocate iterators to print out addresses!");
			Iterator itrDebugUdp = OutletSensorUdpAddress.iterator();
			IoTDeviceAddress iotaddDebug = (IoTDeviceAddress)itrDebugUdp.next();
			System.out.println("IP address: " + iotaddDebug.getCompleteAddress());
			System.out.println("Source port: " + iotaddDebug.getSourcePortNumber());
			System.out.println("Destination port: " + iotaddDebug.getDestinationPortNumber());

			Iterator itrDebugZig = OutletSensorZigbeeAddress.iterator();
			IoTZigbeeAddress iotzbaddDebug = (IoTZigbeeAddress)itrDebugZig.next();
			System.out.println("Zigbee address: " + iotzbaddDebug.getAddress());

			zigConnection.registerCallback(this);
			System.out.println("Register callback!");
			zigConnection.init();
			System.out.println("Initialized!");


			
			sleep(10);

                        //System.out.println("Sending BroadcastingRouteRecordRequest");
			//zigConnection.sendBroadcastingRouteRecordRequest(0x0001);
                        

			System.out.println("Sending Management Permit Joining Request");
			for(int z=0; z<3; z++){
				zigConnection.sendManagementPermitJoiningRequest(0x0002, 0x0036, 0x00);
				sleep(0);
			}

			//made by yuting
                        while(!didBind.get()){
				System.out.println("Sending Bind Request");
				zigConnection.sendBindRequest(0x0003,0x0B04, 0x01);// 0x0021, 0x00);// 0x0B04, 0x01);
				sleep(0);
			}
			

			//made by yuting
                        while(!didConfigureReporting.get()){
				System.out.println("Sending Configure Reporting");
				byte [] reportableChange= {0x0005};
				zigConnection.sendConfigureReportingCommand(0x0004, 0x0B04, 0x0104, 0x01, 0x050b, 0x29, 0x0001, 0x0500, reportableChange); 
				sleep(0);
			}
			


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
        //made by yuting
        public float getWatts() {

		float tmp = 0;
		try {
			gettingLatestDataMutex.acquire();
		 	tmp = Watts;

		} catch (Exception e) {
			e.printStackTrace();
		}
		gettingLatestDataMutex.release();

		return tmp;
	}
        //made by yuting
        public void TurnOn(){
        try {
			System.out.println("the outlet sensor is turning on");
			zigConnection.sendChangeSwtichRequest(0x0005, 0x0006, 0x0104, 1, 0x01);
		}  catch(Exception e){
			e.printStackTrace();
		}
        }
        //made by yuting
        public void TurnOff(){
        try {
			System.out.println("the outlet sensor is turning off");
			zigConnection.sendChangeSwtichRequest(0x0005, 0x0006, 0x0104, 0, 0x01);
		}  catch(Exception e){
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
                //made by yuting
		else if (_zm instanceof IoTZigbeeMessageZclConfigureReportingResponse){
			IoTZigbeeMessageZclConfigureReportingResponse message = (IoTZigbeeMessageZclConfigureReportingResponse)_zm;
			if (message.getAllSuccess()) {
				didConfigureReporting.set(true);
			}
		}
                //made by yuting
                else if (_zm instanceof IoTZigbeeMessageZclChangeSwitchResponse){
			IoTZigbeeMessageZclChangeSwitchResponse message = (IoTZigbeeMessageZclChangeSwitchResponse)_zm;
			if (message.getSuccessOrFail()) {
				if (message.getStatus()==1){
                                System.out.println("change on/off response: turned on"); 
                                //System.out.println(message.getStatus());   
                                }
                                else if(message.getStatus()==0){
                                System.out.println("change on/off response: turned off");
                                }
			}
		}
                //made by yuting
		else if (_zm instanceof IoTZigbeeMessageZclReportAttributes) {
			IoTZigbeeMessageZclReportAttributes message = (IoTZigbeeMessageZclReportAttributes)_zm;
			List <IoTZigbeeMessageZclReportAttributes.Attribute> attrList = message.getAttributes();

			if (attrList.size() == 1) {
				if(attrList.get(0).getAttributeId() == 2821) {
					byte[] data = attrList.get(0).getData();
					int value = (data[0] * 256) + data[1];

					try {
						gettingLatestDataMutex.acquire();
						Watts = (float)value;
						timestampOfLastDetecting = new Date();
					} catch (Exception e) {
						e.printStackTrace();
					}
					gettingLatestDataMutex.release();

					try {
						for (SmartthingsSensorCallback cb : callbackList) {
							cb.newReadingAvailable(this);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
			}
		}
	}

	//public void registerCallback(SmartthingsSensorSmartCallback _callbackTo) {
	public void registerCallback(SmartthingsSensorCallback _callbackTo) {
		callbackList.add(_callbackTo);
	}
}
