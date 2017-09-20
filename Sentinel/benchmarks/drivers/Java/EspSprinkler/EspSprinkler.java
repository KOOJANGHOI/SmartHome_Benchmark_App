package iotcode.EspSprinkler;

// Standard Java Packages
import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.Iterator;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

// IoT Packages
import iotruntime.IoTUDP;
import iotruntime.slave.IoTDeviceAddress;
import iotruntime.slave.IoTSet;
import iotcode.interfaces.ZoneState;
import iotcode.interfaces.Sprinkler;
import iotcode.annotation.*;

//import iotchecker.qual.*;

/** Class EspSprinkler for the ESP8266 plrg Sprinkler.
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-03-31
 */

public class EspSprinkler implements Sprinkler {

	/*******************************************************************************************************************************************
	**
	**  Variables
	**
	*******************************************************************************************************************************************/

	private IoTUDP communicationSockect;
	private Semaphore socketMutex = new Semaphore(1);
	private AtomicBoolean sendSocketFlag = new AtomicBoolean(false);
	private AtomicBoolean doingRead = new AtomicBoolean(false);
	private AtomicBoolean didInit = new AtomicBoolean(false);
	private Semaphore settingZone = new Semaphore(1);

	/*******************************************************************************************************************************************
	**
	**  Threads
	**
	*******************************************************************************************************************************************/

	// Main worker thread will do the receive loop
	Thread workerThread = null;


	/*******************************************************************************************************************************************
	**
	**  IoT Sets and Relations
	**
	*******************************************************************************************************************************************/

	// IoTSet of Device Addresses.
	// Will be filled with only 1 address.
	@config private IoTSet<IoTDeviceAddress> spr_Addresses;

	/*public EspSprinkler(IoTUDP _udp) {
		communicationSockect = _udp;
	}*/

	public EspSprinkler() {
		communicationSockect = null;
	}


	/*******************************************************************************************************************************************
	**
	**  Interface Methods
	**
	*******************************************************************************************************************************************/

	/** Method to set the state of a specified zone. Interface implementation.
	 *
	 *   @param _zone [int]             : zone number to set.
	 *   @param _onOff [boolean]        : the state to set the zone to, on or off.
	 *   @param _onDurationSeconds [int]: the duration to set the state on to, if -1 then infinite.
	 *
	 *   @return [void] None.
	 */
	public void setZone(int _zone, boolean _onOff, int _onDurationSeconds) {

		try {
			settingZone.acquire();
			String sendString = "SET,";
			sendString += Integer.toString(_zone);
			sendString += ", ";

			if (_onOff) {
				sendString += "1";
			} else {
				sendString += "0";
			}
			sendString += ", ";
			sendString += Integer.toString(_onDurationSeconds);

			sendPacket(sendString.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			e.printStackTrace();
		}
		settingZone.release();
	}


	/** Method to get the current state of all the zones. Interface implementation.
	 *
	 *   @param None.
	 *
	 *   @return [List<ZoneState>] list of the states for the zones.
	 */
	public List<ZoneState> getZoneStates() {
		doingRead.set(true);
		sendGetInformation();

		try {
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}

		int loopCount = 0;
		while (true) {
			// Communication resource is busy so try again later
			if (sendSocketFlag.get()) {
				continue;
			}

			try {
				socketMutex.acquire();
			} catch (InterruptedException e) {
			}

			byte[] dat = null;
			try {
				dat = communicationSockect.recieveData(1024);
			} catch (java.net.SocketTimeoutException e) {
				// Timeout occurred

			} catch (IOException e) {
				// Problem but might be able to recover??
				e.printStackTrace();

			}

			// Never forget to release!
			socketMutex.release();

			// A packed arrived
			if (dat != null) {
				doingRead.set(false);
				return parseGetResponse(dat);

				// return new ArrayList<ZoneState>();
			} else {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
				loopCount++;

				if (loopCount > 3) {
					sendGetInformation();
					loopCount = 0;
				}
			}
		}
	}


	/** Method to get the number of zones this sprinkler can control. Interface implementation.
	 *
	 *   @param None.
	 *
	 *   @return [int] number of zones that can be controlled.
	 */
	public int getNumberOfZones() {
		return 9;
	}


	/** Method to get whether or not this sprinkler can control durations. Interface implementation.
	 *
	 *   @param None.
	 *
	 *   @return [boolean] boolean if this sprinkler can do durations.
	 */
	public boolean doesHaveZoneTimers() {
		return true;
	}


	/** Method to initialize the sprinkler. Interface implementation.
	 *
	 *   @param None.
	 *
	 *   @return [void] None.
	 */
	public void init() {

		if (didInit.compareAndSet(false, true) == false) {
			return; // already init
		}

		try {
			Iterator itr = spr_Addresses.iterator();
			IoTDeviceAddress deviceAddress = (IoTDeviceAddress)itr.next();
			System.out.println("Address: " + deviceAddress.getCompleteAddress());

			// Create the communication channel
			communicationSockect = new IoTUDP(deviceAddress);
		} catch (Exception e) {
			e.printStackTrace();
		}


		// Launch the worker function in a separate thread.
		workerThread = new Thread(new Runnable() {
			public void run() {
				workerFunction();
			}
		});
		workerThread.start();
	}


	/*******************************************************************************************************************************************
	**
	**  Private Handlers
	**
	*******************************************************************************************************************************************/

	/** Method to send the get information udp packet to get the latest sprinkler state.
	 *
	 *   @param None.
	 *
	 *   @return [void] None.
	 */
	public void sendGetInformation() {
		String sendString = "GET";
		sendPacket(sendString.getBytes(StandardCharsets.UTF_8));
	}


	/** Method to parse the UDP packet data into a meaningful representation.
	 *
	 *   @param _packetData [byte[]] raw packet data from the udp packet.
	 *
	 *   @return [List<ZoneState>] Parsed zone data.
	 */
	private List<ZoneState> parseGetResponse(byte[] _packetData) {
		String recString = new String(_packetData);
		List<ZoneState> retStates = new ArrayList<ZoneState>();

		String[] lines = recString.split("\n");

		for (int i = 0; i < 9; i++) {
			String[] splitSting = lines[i].split(",");

			int zoneNum = Integer.parseInt(splitSting[0].trim());
			int onOffInt = Integer.parseInt(splitSting[1].trim());
			boolean onOff = onOffInt != 0;
			int duration = Integer.parseInt(splitSting[2].trim());


			//ZoneState zTmp = new ZoneState(zoneNum, onOff, duration);
			ZoneState zTmp = new ZoneState();
			zTmp.zoneNumber = zoneNum;
			zTmp.onOffState = onOff;
			zTmp.duration = duration;
			retStates.add(zTmp);
		}

		return retStates;
	}


	/** Method to parse the UDP packet data into a meaningful representation.
	 *
	 *   @param _packetData [byte[]] bytes to send over the udp channel.
	 *
	 *   @return [void] None.
	 */
	private void sendPacket(byte[] _packetData) {
		// System.out.println("About to send");
		sendSocketFlag.set(true);

		try {
			socketMutex.acquire();
		} catch (InterruptedException e) {
			System.out.println("mutex Error");
		}

		try {
			communicationSockect.sendData(_packetData);

		} catch (IOException e) {
			System.out.println("Socket Send Error");
		}

		sendSocketFlag.set(false);
		socketMutex.release();
	}


	/** Method to constantly flush the udp socket expect when we wish to read the incoming data.
	 *
	 *   @param None.
	 *
	 *   @return [void] None.
	 */
	private void workerFunction() {
		try {
			// Need timeout on receives since we are not sure if a packet will be available
			// for processing so don't block waiting
			communicationSockect.setSoTimeout(50);
		} catch (IOException e) {
		}



		while (true) {

			// Communication resource is busy so try again later
			if (sendSocketFlag.get()) {
				continue;
			}

			if (doingRead.get()) {
				continue;
			}

			try {
				socketMutex.acquire();
			} catch (InterruptedException e) {
			}

			byte[] dat = null;
			try {
				dat = communicationSockect.recieveData(1024);
			} catch (java.net.SocketTimeoutException e) {
				// Timeout occurred

			} catch (IOException e) {
				// Problem but might be able to recover??
				e.printStackTrace();

			}

			// Never forget to release!
			socketMutex.release();

			// Wait a bit as to not tie up system resources
			try {
				Thread.sleep(100);
			} catch (Exception e) {

			}
		}
	}

}








