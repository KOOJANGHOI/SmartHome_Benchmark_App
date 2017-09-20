// Standard Java Packages
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;


// Checker annotations
//import iotchecker.qual.*;

// IoT Packages
import iotruntime.slave.*;
//import iotcode.interfaces.MoistureSensor;
//import iotcode.interfaces.MoistureSensorSmartCallback;
import iotruntime.zigbee.*;
//import iotcode.annotation.*;

public class SpruceSensor implements IoTZigbeeCallback, MoistureSensor {

	private final int TIMEOUT_FOR_RESEND_MSEC = 1000;

	private IoTZigbee zigConnection = null;
	private boolean didClose;									// make sure that the clean up was done correctly

	private float humidity = 0;
	private Date timestampOfLastHumidity = null;

	private AtomicBoolean didBind = new AtomicBoolean(false);
	private AtomicBoolean didConfigureReporting = new AtomicBoolean(false);
	private AtomicBoolean didAlreadyInit = new AtomicBoolean(false);
	private AtomicBoolean didAlreadyClose = new AtomicBoolean(true);
	static Semaphore gettingLatestDataMutex = new Semaphore(1);

	private List < MoistureSensorSmartCallback > callbackList = new CopyOnWriteArrayList < MoistureSensorSmartCallback > ();

	private int sensorId = 0;

	@config private IoTSet<IoTDeviceAddress> devUdpAddress;
	@config private IoTSet<IoTZigbeeAddress> devZigbeeAddress;

	public SpruceSensor(IoTSet<IoTDeviceAddress> _devUdpAddress, IoTSet<IoTZigbeeAddress> _devZigbeeAddress) {
		devUdpAddress = _devUdpAddress;
		devZigbeeAddress = _devZigbeeAddress;
	}

	public void init() {

		if (didAlreadyInit.compareAndSet(false, true) == false) {
			return; // already init
		}

		didAlreadyClose.set(false);

		try {
			Iterator itrUdp = devUdpAddress.iterator();
			Iterator itrZig = devZigbeeAddress.iterator();

			zigConnection = new IoTZigbee((IoTDeviceAddress)itrUdp.next(), (IoTZigbeeAddress)itrZig.next());

			// DEBUG
			System.out.println("DEBUG: Allocate iterators to print out addresses!");
			Iterator itrDebugUdp = devUdpAddress.iterator();
			IoTDeviceAddress iotaddDebug = (IoTDeviceAddress)itrDebugUdp.next();
			System.out.println("IP address: " + iotaddDebug.getCompleteAddress());
			System.out.println("Source port: " + iotaddDebug.getSourcePortNumber());
			System.out.println("Destination port: " + iotaddDebug.getDestinationPortNumber());

			Iterator itrDebugZig = devZigbeeAddress.iterator();
			IoTZigbeeAddress iotzbaddDebug = (IoTZigbeeAddress)itrDebugZig.next();
			System.out.println("Zigbee address: " + iotzbaddDebug.getAddress());

			zigConnection.registerCallback(this);
			System.out.println("Register callback!");
			zigConnection.init();
			System.out.println("Initialized!");

			while (!didBind.get()) {
				zigConnection.sendBindRequest(0x0001, 0x0405, 0x01);
				System.out.println("Sending bind request!");
				try {
					Thread.sleep(TIMEOUT_FOR_RESEND_MSEC);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			while (!didConfigureReporting.get()) {
				zigConnection.sendConfigureReportingCommand(0x0001, 0x0405, 0x0104, 0x01, 0x0000, 0x21, 0x0001, 0x0001, null);
				try {
					Thread.sleep(TIMEOUT_FOR_RESEND_MSEC);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
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

	public float getMoisture() {

		float tmp = 0;
		try {
			gettingLatestDataMutex.acquire();
			tmp = humidity;

		} catch (Exception e) {
			e.printStackTrace();
		}
		gettingLatestDataMutex.release();

		return tmp;
	}

	public long getTimestampOfLastReading() {

		Date tmp = null;
		try {
			gettingLatestDataMutex.acquire();
			tmp = (Date)timestampOfLastHumidity.clone();

		} catch (Exception e) {
			e.printStackTrace();
		}
		gettingLatestDataMutex.release();
		long retLong = tmp.getTime();

		return retLong;
	}

	public void newMessageAvailable(IoTZigbeeMessage _zm) {

		if (_zm instanceof IoTZigbeeMessageZdoBindResponse) {
			IoTZigbeeMessageZdoBindResponse message = (IoTZigbeeMessageZdoBindResponse)_zm;
			if (message.getSucceeded()) {
				didBind.set(true);
			}

		} else if (_zm instanceof IoTZigbeeMessageZclConfigureReportingResponse) {
			IoTZigbeeMessageZclConfigureReportingResponse message = (IoTZigbeeMessageZclConfigureReportingResponse)_zm;
			if (message.getAllSuccess()) {
				didConfigureReporting.set(true);
			}

		} else if (_zm instanceof IoTZigbeeMessageZclReportAttributes) {
			IoTZigbeeMessageZclReportAttributes message = (IoTZigbeeMessageZclReportAttributes)_zm;
			List <IoTZigbeeMessageZclReportAttributes.Attribute> attrList = message.getAttributes();

			if (attrList.size() == 1) {
				if (attrList.get(0).getAttributeId() == 0) {
					byte[] data = attrList.get(0).getData();

					int value = (data[0] * 256) + data[1];

					try {
						gettingLatestDataMutex.acquire();
						humidity = (float)value / (float)100.0;
						timestampOfLastHumidity = new Date();
					} catch (Exception e) {
						e.printStackTrace();
					}
					gettingLatestDataMutex.release();

					try {
						for (MoistureSensorSmartCallback cb : callbackList) {
							cb.newReadingAvailable(this.getId(), this.getMoisture(), this.getTimestampOfLastReading());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	public void registerCallback(MoistureSensorSmartCallback _callbackTo) {
		callbackList.add(_callbackTo);
	}
}
