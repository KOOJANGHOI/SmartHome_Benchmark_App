package iotcode.LifxLightBulb;

// Standard Java Packages
import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

// IoT Packages
import iotcode.annotation.*;
import iotcode.interfaces.LightBulb;
import iotruntime.IoTUDP;
import iotruntime.slave.IoTDeviceAddress;
import iotruntime.slave.IoTSet;

// String to byte conversion
import javax.xml.bind.DatatypeConverter;

public class LifxLightBulb implements LightBulb {

	/*******************************************************************************************************************************************
	**
	**  Constants
	**
	*******************************************************************************************************************************************/
	public static final long GET_BULB_VERSION_RESEND_WAIT_SECONDS = 10;



	/*******************************************************************************************************************************************
	**
	**  Variables
	**
	*******************************************************************************************************************************************/
	private IoTUDP communicationSockect;
	private byte[] bulbMacAddress = new byte[8];
	static Semaphore socketMutex = new Semaphore(1);
	static boolean sendSocketFlag = false;
	private long lastSentGetBulbVersionRequest = 0;	// time last request sent

	// Current Bulb Values
	private int currentHue = 0;
	private int currentSaturation = 0;
	private int currentBrightness = 65535;
	private int currentTemperature = 9000;
	private boolean bulbIsOn = false;



	private AtomicBoolean didAlreadyInit = new AtomicBoolean(false);

	private AtomicBoolean didGetBulbVersion = new AtomicBoolean(false);
	static Semaphore settingBulbColorMutex = new Semaphore(1);
	static Semaphore settingBulbTempuraturerMutex = new Semaphore(1);
	static Semaphore bulbStateMutex = new Semaphore(1);

	// color and temperature ranges for the bulbs
	private int hueLowerBound = 0;
	private int hueUpperBound = 0;
	private int saturationLowerBound = 0;
	private int saturationUpperBound = 0;
	private int brightnessLowerBound = 0;
	private int brightnessUpperBound = 0;
	private int temperatureLowerBound = 2500;
	private int temperatureUpperBound = 9000;



	// Check if a state change was requested, used to poll the bulb for if the bulb did
	// preform the requested state change
	private boolean stateDidChange = false;

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
	@config private IoTSet<IoTDeviceAddress> lb_addresses;

	/**
	 * Used for testing only
	 */
	/*public LifxLightBulb(IoTUDP udp, byte[] macAddress) {
		communicationSockect = udp;
		bulbMacAddress = macAddress;
	}

	public LifxLightBulb(IoTSet<IoTDeviceAddress> _lb_addresses, String macAddress) {
		this(macAddress);
		lb_addresses = _lb_addresses;
	}*/

	public LifxLightBulb(String macAddress) {
		communicationSockect = null;

		// Set the Mac Address to a default value
		// Probably not needed for anything
		/*bulbMacAdd[0] = (byte)0x00;
		   bulbMacAdd[1] = (byte)0x00;
		   bulbMacAdd[2] = (byte)0x00;
		   bulbMacAdd[3] = (byte)0x00;
		   bulbMacAdd[4] = (byte)0x00;
		   bulbMacAdd[5] = (byte)0x00;
		   bulbMacAdd[6] = (byte)0x00;
		   bulbMacAdd[7] = (byte)0x00;*/

		bulbMacAddress = DatatypeConverter.parseHexBinary(macAddress);
	}



	/*******************************************************************************************************************************************
	**  Sending
	**  Device Messages
	**
	*******************************************************************************************************************************************/
	private void sendGetServicePacket() {
		LifxHeader header = new LifxHeader();
		header.setSize(36);
		header.setTagged(true);
		header.setMacAddress(bulbMacAddress);
		header.setSource(0);	// randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(2);

		byte[] dataBytes = header.getHeaderBytes();
		sendPacket(dataBytes);
	}

	private void sendGetHostInfoPacket() {
		LifxHeader header = new LifxHeader();
		header.setSize(36);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10);	// randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(12);

		byte[] dataBytes = header.getHeaderBytes();
		sendPacket(dataBytes);
	}

	private void sendGetHostFirmwarePacket() {
		LifxHeader header = new LifxHeader();
		header.setSize(36);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10);	// randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(14);

		byte[] dataBytes = header.getHeaderBytes();
		sendPacket(dataBytes);
	}

	private void sendGetWifiInfoPacket() {
		LifxHeader header = new LifxHeader();
		header.setSize(36);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10);	// randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(16);

		byte[] dataBytes = header.getHeaderBytes();
		sendPacket(dataBytes);
	}

	private void sendGetWifiFirmwarePacket() {
		LifxHeader header = new LifxHeader();
		header.setSize(36);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10);	// randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(18);

		byte[] dataBytes = header.getHeaderBytes();
		sendPacket(dataBytes);
	}

	private void sendGetPowerPacket() {
		LifxHeader header = new LifxHeader();
		header.setSize(36);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10);	// randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(20);

		byte[] dataBytes = header.getHeaderBytes();
		sendPacket(dataBytes);
	}

	private void sendSetPowerPacket(int level) {
		// Currently only 0 and 65535 are supported
		// This is a fix for now
		if ((level != 65535) && (level != 0)) {
			throw new InvalidParameterException("Invalid parameter values");
		}

		if ((level > 65535) || (level < 0)) {
			throw new InvalidParameterException("Invalid parameter values");
		}

		byte[] packetBytes = new byte[38];

		LifxHeader header = new LifxHeader();
		header.setSize(38);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10);	// randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(21);
		byte[] headerBytes = header.getHeaderBytes();

		for (int i = 0; i < 36; i++) {
			packetBytes[i] = headerBytes[i];
		}

		packetBytes[36] = (byte)(level & 0xFF);
		packetBytes[37] = (byte)((level >> 8) & 0xFF);

		sendPacket(packetBytes);
	}

	private void sendGetLabelPacket() {
		LifxHeader header = new LifxHeader();
		header.setSize(36);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10); // randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(23);

		byte[] dataBytes = header.getHeaderBytes();
		sendPacket(dataBytes);
	}

	private void sendSetLabelPacket(String label) {
		// Currently only 0 and 65535 are supported
		// This is a fix for now
		if (label.length() != 32) {
			throw new InvalidParameterException("Invalid parameter values, label must be 32 bytes long");
		}

		byte[] packetBytes = new byte[68];

		LifxHeader header = new LifxHeader();
		header.setSize(68);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10); // randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(24);
		byte[] headerBytes = header.getHeaderBytes();

		for (int i = 0; i < 36; i++) {
			packetBytes[i] = headerBytes[i];
		}

		for (int i = 0; i < 32; i++) {
			packetBytes[i + 36] = label.getBytes()[i];
		}

		sendPacket(packetBytes);
	}

	private void sendGetVersionPacket() {
		LifxHeader header = new LifxHeader();
		header.setSize(36);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10); // randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(32);

		byte[] dataBytes = header.getHeaderBytes();
		sendPacket(dataBytes);
	}

	private void sendGetInfoPacket() {
		LifxHeader header = new LifxHeader();
		header.setSize(36);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10); // randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(34);

		byte[] dataBytes = header.getHeaderBytes();
		sendPacket(dataBytes);
	}

	private void sendGetLocationPacket() {
		LifxHeader header = new LifxHeader();
		header.setSize(36);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10); // randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(34);

		byte[] dataBytes = header.getHeaderBytes();
		sendPacket(dataBytes);
	}

	private void sendGetGroupPacket() {
		LifxHeader header = new LifxHeader();
		header.setSize(36);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10); // randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(51);

		byte[] dataBytes = header.getHeaderBytes();
		sendPacket(dataBytes);
	}


	/*******************************************************************************************************************************************
	**  Sending
	**  Light Messages
	**
	*******************************************************************************************************************************************/
	private void sendGetLightStatePacket() {
		LifxHeader header = new LifxHeader();
		header.setSize(36);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10); // randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(101);

		byte[] dataBytes = header.getHeaderBytes();
		sendPacket(dataBytes);
	}

	private void sendSetLightColorPacket(BulbColor bulbColor, long duration) {

		if ((duration > 4294967295l) || (duration < 0)) {
			throw new InvalidParameterException("Invalid parameter value, duration out of range (0 - 4294967295)");
		}

		byte[] packetBytes = new byte[49];

		LifxHeader header = new LifxHeader();
		header.setSize(49);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10); // randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(102);
		byte[] headerBytes = header.getHeaderBytes();

		for (int i = 0; i < 36; i++) {
			packetBytes[i] = headerBytes[i];
		}

		// 1 reserved packet
		packetBytes[37] = (byte)(bulbColor.getHue() & 0xFF);
		packetBytes[38] = (byte)((bulbColor.getHue() >> 8) & 0xFF);

		packetBytes[39] = (byte)(bulbColor.getSaturation() & 0xFF);
		packetBytes[40] = (byte)((bulbColor.getSaturation() >> 8) & 0xFF);

		packetBytes[41] = (byte)(bulbColor.getBrightness() & 0xFF);
		packetBytes[42] = (byte)((bulbColor.getBrightness() >> 8) & 0xFF);

		packetBytes[43] = (byte)(bulbColor.getKelvin() & 0xFF);
		packetBytes[44] = (byte)((bulbColor.getKelvin() >> 8) & 0xFF);

		packetBytes[45] = (byte)((duration >> 0) & 0xFF);
		packetBytes[46] = (byte)((duration >> 8) & 0xFF);
		packetBytes[47] = (byte)((duration >> 16) & 0xFF);
		packetBytes[48] = (byte)((duration >> 24) & 0xFF);

		sendPacket(packetBytes);
	}

	private void sendGetLightPowerPacket() {
		LifxHeader header = new LifxHeader();
		header.setSize(36);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10); // randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(116);

		byte[] dataBytes = header.getHeaderBytes();
		sendPacket(dataBytes);
	}

	private void sendSetLightPowerPacket(int level, long duration) {

		if ((level > 65535) || (duration > 4294967295l)
		        || (level < 0) || (duration < 0)) {
			throw new InvalidParameterException("Invalid parameter values");
		}

		byte[] packetBytes = new byte[42];


		LifxHeader header = new LifxHeader();
		header.setSize(42);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10); // randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(117);
		byte[] headerBytes = header.getHeaderBytes();

		for (int i = 0; i < 36; i++) {
			packetBytes[i] = headerBytes[i];
		}

		packetBytes[36] = (byte)(level & 0xFF);
		packetBytes[37] = (byte)((level >> 8) & 0xFF);

		packetBytes[38] = (byte)((duration >> 0) & 0xFF);
		packetBytes[39] = (byte)((duration >> 8) & 0xFF);
		packetBytes[40] = (byte)((duration >> 16) & 0xFF);
		packetBytes[41] = (byte)((duration >> 24) & 0xFF);

		System.out.println(Arrays.toString(packetBytes));

		sendPacket(packetBytes);
	}

	private void sendEchoRequestPacket(byte[] data) {
		// Currently only 0 and 65535 are supported
		// This is a fix for now
		if (data.length != 64) {
			throw new InvalidParameterException("Invalid parameter values, must have 64 bytes");
		}

		byte[] packetBytes = new byte[100];

		LifxHeader header = new LifxHeader();
		header.setSize(100);
		header.setTagged(false);
		header.setMacAddress(bulbMacAddress);
		header.setSource(10); // randomly picked
		header.setAck_required(false);
		header.setRes_required(false);
		header.setSequence(0);
		header.setType(58);
		byte[] headerBytes = header.getHeaderBytes();

		for (int i = 0; i < 36; i++) {
			packetBytes[i] = headerBytes[i];
		}

		for (int i = 0; i < 64; i++) {
			packetBytes[i + 36] = data[i];
		}

		sendPacket(packetBytes);
	}


	/*******************************************************************************************************************************************
	**  Receiving
	**  Device Messages
	**
	*******************************************************************************************************************************************/
	private DeviceStateService parseDeviceStateServiceMessage(LifxHeader header, byte[] payloadData) {
		int service = payloadData[0];
		long port = ((payloadData[3] & 0xFF) << 24);
		port |= ((payloadData[2] & 0xFF) << 16);
		port |= ((payloadData[1] & 0xFF) << 8);
		port |= (payloadData[0] & 0xFF);

		return new DeviceStateService(service, port);
	}

	private DeviceStateHostInfo parseDeviceStateHostInfoMessage(LifxHeader header, byte[] payloadData) {
		long signal = ((payloadData[3] & 0xFF) << 24);
		signal |= ((payloadData[2] & 0xFF) << 16);
		signal |= ((payloadData[1] & 0xFF) << 8);
		signal |= (payloadData[0] & 0xFF);

		long tx = ((payloadData[7] & 0xFF) << 24);
		tx |= ((payloadData[6] & 0xFF) << 16);
		tx |= ((payloadData[5] & 0xFF) << 8);
		tx |= (payloadData[4] & 0xFF);

		long rx = ((payloadData[11] & 0xFF) << 24);
		rx |= ((payloadData[10] & 0xFF) << 16);
		rx |= ((payloadData[9] & 0xFF) << 8);
		rx |= (payloadData[8] & 0xFF);

		return new DeviceStateHostInfo(signal, tx, rx);
	}

	private DeviceStateHostFirmware parseDeviceStateHostFirmwareMessage(LifxHeader header, byte[] payloadData) {
		long build = 0;
		for (int i = 0; i < 8; i++) {
			build += ((long) payloadData[i] & 0xffL) << (8 * i);
		}

		// 8 reserved bytes

		long version = ((payloadData[19] & 0xFF) << 24);
		version |= ((payloadData[18] & 0xFF) << 16);
		version |= ((payloadData[17] & 0xFF) << 8);
		version |= (payloadData[16] & 0xFF);

		return new DeviceStateHostFirmware(build, version);
	}

	private DeviceStateWifiInfo parseDeviceStateWifiInfoMessage(LifxHeader header, byte[] payloadData) {
		long signal = ((payloadData[3] & 0xFF) << 24);
		signal |= ((payloadData[2] & 0xFF) << 16);
		signal |= ((payloadData[1] & 0xFF) << 8);
		signal |= (payloadData[0] & 0xFF);

		long tx = ((payloadData[7] & 0xFF) << 24);
		tx |= ((payloadData[6] & 0xFF) << 16);
		tx |= ((payloadData[5] & 0xFF) << 8);
		tx |= (payloadData[4] & 0xFF);

		long rx = ((payloadData[11] & 0xFF) << 24);
		rx |= ((payloadData[10] & 0xFF) << 16);
		rx |= ((payloadData[9] & 0xFF) << 8);
		rx |= (payloadData[8] & 0xFF);

		return new DeviceStateWifiInfo(signal, tx, rx);
	}

	private DeviceStateWifiFirmware parseDeviceStateWifiFirmwareMessage(LifxHeader header, byte[] payloadData) {
		long build = 0;
		for (int i = 0; i < 8; i++) {
			build += ((long) payloadData[i] & 0xffL) << (8 * i);
		}

		// 8 reserved bytes

		long version = ((payloadData[19] & 0xFF) << 24);
		version |= ((payloadData[18] & 0xFF) << 16);
		version |= ((payloadData[17] & 0xFF) << 8);
		version |= (payloadData[16] & 0xFF);

		return new DeviceStateWifiFirmware(build, version);
	}

	private int parseStatePowerMessage(LifxHeader header, byte[] payloadData) {
		int level = ((payloadData[1] & 0xFF) << 8);
		level |= (payloadData[0] & 0xFF);
		return level;
	}

	private String parseStateLabelMessage(LifxHeader header, byte[] payloadData) {
		return new String(payloadData);
	}


	private DeviceStateVersion parseDeviceStateVersionMessage(LifxHeader header, byte[] payloadData) {
		long vender = ((payloadData[3] & 0xFF) << 24);
		vender |= ((payloadData[2] & 0xFF) << 16);
		vender |= ((payloadData[1] & 0xFF) << 8);
		vender |= (payloadData[0] & 0xFF);

		long product = ((payloadData[7] & 0xFF) << 24);
		product |= ((payloadData[6] & 0xFF) << 16);
		product |= ((payloadData[5] & 0xFF) << 8);
		product |= (payloadData[4] & 0xFF);

		long version = ((payloadData[11] & 0xFF) << 24);
		version |= ((payloadData[10] & 0xFF) << 16);
		version |= ((payloadData[9] & 0xFF) << 8);
		version |= (payloadData[8] & 0xFF);

		return new DeviceStateVersion(vender, product, version);
	}

	private DeviceStateInfo parseDeviceStateInfoMessage(LifxHeader header, byte[] payloadData) {
		long time = 0;
		long upTime = 0;
		long downTime = 0;
		for (int i = 0; i < 8; i++) {
			time += ((long) payloadData[i] & 0xffL) << (8 * i);
			upTime += ((long) payloadData[i + 8] & 0xffL) << (8 * i);
			downTime += ((long) payloadData[i + 16] & 0xffL) << (8 * i);
		}

		return new DeviceStateInfo(time, upTime, downTime);
	}

	private DeviceStateLocation parseDeviceStateLocationMessage(LifxHeader header, byte[] payloadData) {
		byte[] location = new byte[16];
		for (int i = 0; i < 16; i++) {
			location[i] = payloadData[i];
		}

		byte[] labelBytes = new byte[32];
		for (int i = 0; i < 32; i++) {
			labelBytes[i] = payloadData[i + 16];
		}

		long updatedAt = 0;
		for (int i = 0; i < 8; i++) {
			updatedAt += ((long) payloadData[48] & 0xffL) << (8 * i);
		}

		return new DeviceStateLocation(location, new String(labelBytes), updatedAt);
	}

	private DeviceStateGroup parseDeviceStateGroupMessage(LifxHeader header, byte[] payloadData) {
		byte[] group = new byte[16];
		for (int i = 0; i < 16; i++) {
			group[i] = payloadData[i];
		}

		byte[] labelBytes = new byte[32];
		for (int i = 0; i < 32; i++) {
			labelBytes[i] = payloadData[i + 16];
		}

		long updatedAt = 0;
		for (int i = 0; i < 8; i++) {
			updatedAt += ((long) payloadData[48] & 0xffL) << (8 * i);
		}

		return new DeviceStateGroup(group, new String(labelBytes), updatedAt);
	}

	private byte[] parseDeviceEchoResponseMessage(LifxHeader header, byte[] payloadData) {
		return payloadData;
	}

	/*******************************************************************************************************************************************
	**  Receiving
	**  Light Messages
	**
	*******************************************************************************************************************************************/
	private LightState parseLightStateMessage(LifxHeader header, byte[] payloadData) {

		byte[] colorData = new byte[8];
		for (int i = 0; i < 8; i++) {
			colorData[i] = payloadData[i];
		}
		BulbColor color = new BulbColor(colorData);

		int power = ((payloadData[11] & 0xFF) << 8);
		power |= (payloadData[10] & 0xFF);

		String label = new String(payloadData);

		byte[] labelArray = new byte[32];
		for (int i = 0; i < 32; i++) {
			labelArray[i] = payloadData[12 + i];
		}

		return new LightState(color, power, label);
	}

	private int parseLightStatePowerMessage(LifxHeader header, byte[] payloadData) {
		int level = ((payloadData[1] & 0xFF) << 8);
		level |= (payloadData[0] & 0xFF);
		return level;
	}


	/*******************************************************************************************************************************************
	**
	**  Private Handlers
	**
	*******************************************************************************************************************************************/
	private void handleStateVersionMessageRecieved(LifxHeader header, byte[] payloadData) {

		DeviceStateVersion deviceState = parseDeviceStateVersionMessage(header, payloadData);
		int productNumber = (int)deviceState.getProduct();

		boolean isColor = false;

		if (productNumber == 1) {// Original 1000
			isColor = true;
		} else if (productNumber == 3) {//Color 650
			isColor = true;
		} else if (productNumber == 10) {// White 800 (Low Voltage)
			isColor = false;
		} else if (productNumber == 11) {// White 800 (High Voltage)
			isColor = false;
		} else if (productNumber == 18) {// White 900 BR30 (Low Voltage)
			isColor = false;
		} else if (productNumber == 20) {// Color 1000 BR30
			isColor = true;
		} else if (productNumber == 22) {// Color 1000
			isColor = true;
		}

		if (isColor) {
			hueLowerBound = 0;
			hueUpperBound = 65535;
			saturationLowerBound = 0;
			saturationUpperBound = 65535;
			brightnessLowerBound = 0;
			brightnessUpperBound = 65535;
			temperatureLowerBound = 2500;
			temperatureUpperBound = 9000;
		} else {
			hueLowerBound = 0;
			hueUpperBound = 0;
			saturationLowerBound = 0;
			saturationUpperBound = 0;
			brightnessLowerBound = 0;
			brightnessUpperBound = 65535;// still can dim bulb
			temperatureLowerBound = 2500;
			temperatureUpperBound = 9000;
		}

		didGetBulbVersion.set(true);

	}

	private void handleLightStateMessageRecieved(LifxHeader header, byte[] payloadData) {
		LightState lightState = parseLightStateMessage(header, payloadData);

		BulbColor color = lightState.getColor();
		int power = lightState.getPower();

		boolean bulbWrongColor = false;
		bulbWrongColor = bulbWrongColor || (color.getHue() != currentHue);
		bulbWrongColor = bulbWrongColor || (color.getSaturation() != currentSaturation);
		bulbWrongColor = bulbWrongColor || (color.getBrightness() != currentBrightness);
		bulbWrongColor = bulbWrongColor || (color.getKelvin() != currentTemperature);


		// gets set to true if any of the below if statements are taken
		stateDidChange = false;

		if (bulbWrongColor) {
			BulbColor newColor = new BulbColor(currentHue, currentSaturation, currentBrightness, currentTemperature);
			sendSetLightColorPacket(newColor, 250);
			// System.out.println("Failed Check 1");
		}

		try {
			bulbStateMutex.acquire();
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean bulbIsOnTmp = bulbIsOn;
		bulbStateMutex.release();

		if ((!bulbIsOnTmp) && (power != 0)) {
			turnOff();
			// System.out.println("Failed Check 2:  " + Integer.toString(power));

		}

		if (bulbIsOnTmp && (power < 65530)) {
			turnOn();
			// System.out.println("Failed Check 3:  " + Integer.toString(power));

		}
	}

	/*******************************************************************************************************************************************
	**
	**  Light Bulb Interface Methods
	**
	*******************************************************************************************************************************************/
	public double getHue() {
		double tmp = 0;
		try {
			settingBulbColorMutex.acquire();
			tmp = ((double)currentHue / 65535.0) * 360.0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		settingBulbColorMutex.release();


		return tmp;
	}

	public double getSaturation() {
		double tmp = 0;
		try {
			settingBulbColorMutex.acquire();
			tmp = ((double)currentSaturation / 65535.0) * 360.0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		settingBulbColorMutex.release();


		return tmp;
	}

	public double getBrightness() {
		double tmp = 0;
		try {
			settingBulbColorMutex.acquire();
			tmp = ((double)currentBrightness / 65535.0) * 360.0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		settingBulbColorMutex.release();

		return tmp;
	}

	public int getTemperature() {

		int tmp = 0;
		try {
			settingBulbTempuraturerMutex.acquire();
			tmp = currentTemperature;
		} catch (Exception e) {
			e.printStackTrace();
		}
		settingBulbTempuraturerMutex.release();

		return tmp;
	}

	public double getHueRangeLowerBound() {
		if (!didGetBulbVersion.get()) {
			return -1;
		}
		return ((double)hueLowerBound / 65535.0) * 360.0;
	}

	public double getHueRangeUpperBound() {
		if (!didGetBulbVersion.get()) {
			return -1;
		}
		return ((double)hueUpperBound / 65535.0) * 360.0;
	}

	public double getSaturationRangeLowerBound() {
		if (!didGetBulbVersion.get()) {
			return -1;
		}
		return ((double)saturationLowerBound / 65535.0) * 100.0;
	}

	public double getSaturationRangeUpperBound() {
		if (!didGetBulbVersion.get()) {
			return -1;
		}
		return ((double)saturationUpperBound / 65535.0) * 100.0;
	}

	public double getBrightnessRangeLowerBound() {
		if (!didGetBulbVersion.get()) {
			return -1;
		}
		return ((double)brightnessLowerBound / 65535.0) * 100.0;
	}

	public double getBrightnessRangeUpperBound() {
		if (!didGetBulbVersion.get()) {
			return -1;
		}
		return ((double)brightnessUpperBound / 65535.0) * 100.0;
	}

	public int getTemperatureRangeLowerBound() {
		if (!didGetBulbVersion.get()) {
			return -1;
		}
		return temperatureLowerBound;
	}

	public int getTemperatureRangeUpperBound() {
		if (!didGetBulbVersion.get()) {
			return -1;
		}
		return temperatureUpperBound;
	}

	public void setTemperature(int _temperature) {

		try {
			settingBulbTempuraturerMutex.acquire();
		} catch (Exception e) {
			e.printStackTrace();
		}

		BulbColor newColor = new BulbColor(currentHue, currentSaturation, currentBrightness, _temperature);
		sendSetLightColorPacket(newColor, 250);

		currentTemperature = _temperature;
		stateDidChange = true;

		settingBulbTempuraturerMutex.release();
	}

	public void setColor(double _hue, double _saturation, double _brightness) {

		try {
			settingBulbColorMutex.acquire();
		} catch (Exception e) {
			e.printStackTrace();
		}


		_hue /= 360.0;
		_saturation /= 100.0;
		_brightness /= 100.0;


		int newHue = (int)(_hue * 65535.0);
		int newSaturation = (int)(_saturation * 65535.0);
		int newBrightness = (int)(_brightness * 65535.0);

		BulbColor newColor = new BulbColor(newHue, newSaturation, newBrightness, currentTemperature);
		sendSetLightColorPacket(newColor, 250);

		currentHue = newHue;
		currentSaturation = newSaturation;
		currentBrightness = newBrightness;
		stateDidChange = true;

		settingBulbColorMutex.release();
	}


	public void turnOff() {

		try {
			bulbStateMutex.acquire();
			bulbIsOn = false;
			sendSetLightPowerPacket(0, 0);
			stateDidChange = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		bulbStateMutex.release();
	}

	public void turnOn() {
		try {
			bulbStateMutex.acquire();
			bulbIsOn = true;
			sendSetLightPowerPacket(65535, 0);
			stateDidChange = true;

		} catch (Exception e) {
			e.printStackTrace();
		}


		bulbStateMutex.release();
	}

	public boolean getState() {

		boolean tmp = false;
		try {
			bulbStateMutex.acquire();
			tmp = bulbIsOn;
		} catch (Exception e) {
			e.printStackTrace();
		}

		bulbStateMutex.release();

		return tmp;
	}


	/*******************************************************************************************************************************************
	**
	**  Communication Helpers
	**
	*******************************************************************************************************************************************/
	private void recievedPacket(byte[] packetData) {

		byte[] headerBytes = new byte[36];
		for (int i = 0; i < 36; i++) {
			headerBytes[i] = packetData[i];
		}

		LifxHeader recHeader = new LifxHeader();
		recHeader.setFromBytes(headerBytes);

		// load the payload bytes (strip away the header)
		byte[] payloadBytes = new byte[recHeader.getSize()];
		for (int i = 36; i < recHeader.getSize(); i++) {
			payloadBytes[i - 36] = packetData[i];
		}

		System.out.println("Received: " + Integer.toString(recHeader.getType()));

		switch (recHeader.getType()) {
		case 3:
			DeviceStateService dat = parseDeviceStateServiceMessage(recHeader, payloadBytes);
			// System.out.println("Service: " + Integer.toString(dat.getService()));
			// System.out.println("Port   : " + Long.toString(dat.getPort()));
			break;


		case 33:
			handleStateVersionMessageRecieved(recHeader, payloadBytes);
			break;

		case 35:
			parseDeviceStateInfoMessage(recHeader, payloadBytes);
			break;


		case 107:
			handleLightStateMessageRecieved(recHeader, payloadBytes);
			break;

		default:
			// System.out.println("unknown packet Type");
		}

	}

	private void sendPacket(byte[] packetData) {
		// System.out.println("About to send");
		sendSocketFlag = true;

		try {
			socketMutex.acquire();
		} catch (InterruptedException e) {
			System.out.println("mutex Error");
		}

		try {
			communicationSockect.sendData(packetData);

		} catch (IOException e) {
			System.out.println("Socket Send Error");
		}

		sendSocketFlag = false;
		socketMutex.release();
	}


	/**
	 *   Worker function which runs the while loop for receiving data from the bulb.
	 *   Is blocking
	 */
	private void workerFunction() {
		LifxHeader h = new LifxHeader();

		try {
			// Need timeout on receives since we are not sure if a packet will be available
			// for processing so don't block waiting
			communicationSockect.setSoTimeout(50);
		} catch (IOException e) {
		}

		// Start the bulb in the off state
		turnOff();

		while (true) {

			// Check if we got the bulb version yet
			// could have requested it but message could have gotten lost (UDP)
			if (!didGetBulbVersion.get()) {
				long currentTime = (new Date().getTime()) / 1000;
				if ((currentTime - lastSentGetBulbVersionRequest) > GET_BULB_VERSION_RESEND_WAIT_SECONDS) {
					// Get the bulb version so we know what type of bulb this is.
					sendGetVersionPacket();
					lastSentGetBulbVersionRequest = currentTime;
				}
			}

			// Communication resource is busy so try again later
			if (sendSocketFlag) {
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
				recievedPacket(dat);
			}

			// If a state change occurred then request the bulb state to ensure that the
			// bulb did indeed change its state to the correct state
			if (stateDidChange) {
				sendGetLightStatePacket();
			}

			// Wait a bit as to not tie up system resources
			try {
				Thread.sleep(100);
			} catch (Exception e) {

			}


		}
	}


	public void init() {

		if (didAlreadyInit.compareAndSet(false, true) == false) {
			return; // already init
		}

		try {
			// Get the bulb address from the IoTSet
			Iterator itr = lb_addresses.iterator();
			IoTDeviceAddress deviceAddress = (IoTDeviceAddress)itr.next();

			System.out.println("Address: " + deviceAddress.getCompleteAddress());

			// Create the communication channel
			communicationSockect = new IoTUDP(deviceAddress);

		} catch (IOException e) {
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


	/*public static void main(String[] args) throws Exception {

		System.out.println("Executing main function!");
		IoTDeviceAddress iotDevAdd = new IoTDeviceAddress("192.168.2.126", 12345, 56700, false, false);
		Set<IoTDeviceAddress> set = new HashSet<IoTDeviceAddress>();
		set.add(iotDevAdd);
		IoTSet<IoTDeviceAddress> iotset = new IoTSet<IoTDeviceAddress>(set);
		LifxLightBulb lb = new LifxLightBulb(iotset, "D073D5128E300000");
		lb.init();
	}*/
}




















