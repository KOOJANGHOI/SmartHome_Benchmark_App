package iotruntime.zigbee;

// Java packages
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.HashSet;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;

import iotruntime.slave.IoTZigbeeAddress;
import iotruntime.slave.IoTDeviceAddress;

/** Class IoTZigbee
 *
 * @author      Ali Younis <ayounis @ uci.edu>, Changwoo Lee, Jiawei Gu
 * @version     1.0
 * @since       2016-04-12
 */
public final class IoTZigbee {

	public final int SOCKET_SEND_BUFFER_SIZE = 1024;
	public final int SOCKET_RECEIVE_BUFFER_SIZE = 1024;
	public final int SHORT_ADDRESS_UPDATE_TIME_MSEC = 10000;
	public final int SHORT_ADDRESS_UPDATE_TIME_FAST_MSEC = 500;
	public final int RESEND_WAIT_TIME = 500;

	/**
	 * IoTZigbee class properties
	 */

	// UDP connection stuff
	private final String strHostAddress;
	private final int iSrcPort;
	private final int iDstPort;
	private DatagramSocket socket;	// the socket interface that we are guarding
	private boolean didClose;								// make sure that the clean up was done correctly

	private final IoTZigbeeAddress zigbeeAddress;

	// list that holds the callbacks
	private List<IoTZigbeeCallback> callbackList = new ArrayList<IoTZigbeeCallback>();

	/**
	 * IoTZigbee class concurrency and concurrency control
	 */
	private Thread receiveThread = null;

	private AtomicBoolean endTask = new AtomicBoolean(false);
	private AtomicBoolean didSuccesfullySendAddress = new AtomicBoolean(false);

	/**
	 * Class constructor
	 */
	public IoTZigbee(IoTDeviceAddress iotDevAdd, IoTZigbeeAddress zigAddress) throws SocketException, IOException, InterruptedException {

		strHostAddress = iotDevAdd.getHostAddress();
		iSrcPort = iotDevAdd.getSourcePortNumber();
		iDstPort = iotDevAdd.getDestinationPortNumber();
		didClose = false;
		zigbeeAddress = zigAddress;

		socket = new DatagramSocket(iSrcPort);
		socket.setSendBufferSize(SOCKET_SEND_BUFFER_SIZE);
		socket.setReceiveBufferSize(SOCKET_RECEIVE_BUFFER_SIZE);

		receiveThread = new Thread(new Runnable() {
			public void run() {
				receieveWorker();
			}
		});
		receiveThread.start();
	}

	public void init() throws IOException {
		while (!didSuccesfullySendAddress.get()) {

			sendDeviceAddress();

			try {
				Thread.sleep(RESEND_WAIT_TIME);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//made by changwoo
	public void sendChangeSwtichRequest(int packetId, int clusterId, int profileId, int value, int deviceEndpoint) throws IOException {
		String message = "type: zcl_change_switch_request\n";
		message += "packet_id: " + String.format("%04x", packetId) + "\n";
		message += "value: " + String.format("%01x", value) + "\n";
		message += "cluster_id: " + String.format("%04x", clusterId) + "\n";
		message += "profile_id: " + String.format("%04x", profileId) + "\n";
		message += "device_address_long: " + zigbeeAddress.getAddress() + "\n";
		message += "device_endpoint: " + String.format("%02x", deviceEndpoint) + "\n";
		DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(strHostAddress), iDstPort);
		socket.send(sendPacket);
	}

	//made by Jiawei
	public void sendLockOrUnlockDoorRequest(int packetId, int clusterId, int profileId, int deviceEndpoint, int value) throws IOException {
		String message = "type: zcl_lock_or_unlock_door_request\n";
		message += "packet_id: " + String.format("%04x", packetId) + "\n";
		message += "value: " + String.format("%01x", value) + "\n";
		message += "cluster_id: " + String.format("%04x", clusterId) + "\n";
		message += "profile_id: " + String.format("%04x", profileId) + "\n";
		message += "device_address_long: " + zigbeeAddress.getAddress() + "\n";
		message += "device_endpoint: " + String.format("%02x", deviceEndpoint) + "\n";
		DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(strHostAddress), iDstPort);
		socket.send(sendPacket);
	}

	//made by Jiawei
	public void sendReadDoorStatusRequest(int packetId, int clusterId, int profileId, int deviceEndpoint, int framecontrol, int commandframe, int attribute_id) throws IOException {
		String message = "type: zcl_read_door_status_request\n";
		message += "packet_id: " + String.format("%04x", packetId) + "\n";
		message += "framecontrol: " + String.format("%02x", framecontrol) + "\n";
		message += "cluster_id: " + String.format("%04x", clusterId) + "\n";
		message += "profile_id: " + String.format("%04x", profileId) + "\n";
		message += "device_address_long: " + zigbeeAddress.getAddress() + "\n";
		message += "device_endpoint: " + String.format("%02x", deviceEndpoint) + "\n";
		message += "commandframe: " + String.format("%02x", commandframe) + "\n";
		message += "attribute_id: " + String.format("%04x", attribute_id) + "\n";
		DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(strHostAddress), iDstPort);
		socket.send(sendPacket);
	}

	//made by changwoo
	public void sendBroadcastingRouteRecordRequest(int packetId) throws IOException {
		String message = "type: zdo_broadcast_route_record_request\n";
		message += "packet_id: " + String.format("%04x", packetId) + "\n";
		message += "device_address_long: " + zigbeeAddress.getAddress() + "\n";
		DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(strHostAddress), iDstPort);
		socket.send(sendPacket);
	}

	//made by changwoo
	public void sendEnrollmentResponse(int packetId, int clusterId, int profileId, int deviceEndpoint) throws IOException {
		String message = "type: zcl_enrollment_response\n";
		message += "packet_id: " + String.format("%04x", packetId) + "\n";
		message += "device_address_long: " + zigbeeAddress.getAddress() + "\n";
		message += "cluster_id: " + String.format("%04x", clusterId) + "\n";
		message += "profile_id: " + String.format("%04x", profileId) + "\n";
		message += "device_endpoint: " + String.format("%02x", deviceEndpoint) + "\n";
		DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(strHostAddress), iDstPort);
		socket.send(sendPacket);
	}

	//made by changwoo
	public void sendWriteAttributesCommand(int packetId, int clusterId, int profileId, int deviceEndpoint) throws IOException {
		String message = "type: zcl_write_attributes\n";
		message += "packet_id: " + String.format("%04x", packetId) + "\n";
		message += "device_address_long: " + zigbeeAddress.getAddress() + "\n";
		message += "cluster_id: " + String.format("%04x", clusterId) + "\n";
		message += "profile_id: " + String.format("%04x", profileId) + "\n";
		message += "device_endpoint: " + String.format("%02x", deviceEndpoint) + "\n";
		DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(strHostAddress), iDstPort);
		socket.send(sendPacket);
	}

	//made by changwoo
	public void sendManagementPermitJoiningRequest(int packetId, int clusterId, int deviceEndpoint) throws IOException {
		String message = "type: management_permit_joining_request\n";
		message += "packet_id: " + String.format("%04x", packetId) + "\n";
		message += "device_address_long: " + zigbeeAddress.getAddress() + "\n";
		message += "cluster_id: " + String.format("%04x", clusterId) + "\n";
		message += "device_endpoint: " + String.format("%02x", deviceEndpoint) + "\n";
		DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(strHostAddress), iDstPort);
		socket.send(sendPacket);
	}

	public void sendBindRequest(int packetId, int clusterId, int deviceEndpoint) throws IOException {
		String message = "type: zdo_bind_request\n";
		message += "packet_id: " + String.format("%04x", packetId) + "\n";
		message += "device_address_long: " + zigbeeAddress.getAddress() + "\n";
		message += "cluster_id: " + String.format("%04x", clusterId) + "\n";
		message += "device_endpoint: " + String.format("%02x", deviceEndpoint) + "\n";
		DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(strHostAddress), iDstPort);
		socket.send(sendPacket);
	}

	public void sendUnBindRequest(int packetId, int clusterId, int deviceEndpoint) throws IOException {
		String message = "type: zdo_unbind_request\n";
		message += "packet_id: " + String.format("%04x", packetId) + "\n";
		message += "device_address_long: " + zigbeeAddress.getAddress() + "\n";
		message += "cluster_id: " + String.format("%04x", clusterId) + "\n";
		message += "device_endpoint: " + String.format("%02x", deviceEndpoint) + "\n";
		DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(strHostAddress), iDstPort);
		socket.send(sendPacket);
	}

	public void sendReadAttributesCommand(int packetId, int clusterId, int profileId, int deviceEndpoint, List<Integer> attributeIds) throws IOException {
		String message = "type: zcl_read_attributes\n";
		message += "packet_id: " + String.format("%04x", packetId) + "\n";
		message += "device_address_long: " + zigbeeAddress.getAddress() + "\n";
		message += "cluster_id: " + String.format("%04x", clusterId) + "\n";
		message += "profile_id: " + String.format("%04x", profileId) + "\n";
		message += "device_endpoint: " + String.format("%02x", deviceEndpoint) + "\n";

		message += "attribute_ids: ";

		for (Integer i : attributeIds) {
			message += String.format("%04x", i) + ",";
		}

		message = message.substring(0, message.length() - 1);
		message += "\n";

		DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(strHostAddress), iDstPort);
		socket.send(sendPacket);
	}

	public void sendConfigureReportingCommand(int packetId, int clusterId, int profileId, int src_endpoint, int dest_endpoint, int attributeId, int dataType, int minReportingInterval, int maxReportingInterval, byte[] reportableChange) throws IOException {
		String message = "type: zcl_configure_reporting\n";
		message += "packet_id: " + String.format("%04x", packetId) + "\n";
		message += "device_address_long: " + zigbeeAddress.getAddress() + "\n";
		message += "cluster_id: " + String.format("%04x", clusterId) + "\n";
		message += "profile_id: " + String.format("%04x", profileId) + "\n";
		message += "src_endpoint: " + String.format("%02x", src_endpoint) + "\n";
		message += "device_endpoint: " + String.format("%02x", dest_endpoint) + "\n";
		message += "attribute_id: " + String.format("%04x", attributeId) + "\n";
		message += "data_type: " + String.format("%02x", dataType) + "\n";
		message += "min_reporting_interval: " + String.format("%04x", minReportingInterval) + "\n";
		message += "max_reporting_interval: " + String.format("%04x", maxReportingInterval) + "\n";

		if (reportableChange != null) {
			message += "reportable_change: ";
			for (Byte b : reportableChange) {
				message += String.format("%02x", (int)b);
			}
			message += "\n";
		}

		DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(strHostAddress), iDstPort);
		socket.send(sendPacket);
	}

	public void sendConfigureReportingCommand(int packetId, int clusterId, int profileId, int dest_endpoint, int attributeId, int dataType, int minReportingInterval, int maxReportingInterval, byte[] reportableChange) throws IOException {
		sendConfigureReportingCommand(packetId, clusterId, profileId, 0x00, dest_endpoint, attributeId, dataType, minReportingInterval, maxReportingInterval, reportableChange);
	}

	public void registerCallback(IoTZigbeeCallback callbackTo) {
		callbackList.add(callbackTo);
	}

	public void close() throws InterruptedException {
		endTask.set(true);

		// wait for the threads to end
		receiveThread.join();

		socket.close();
		didClose = true;
	}

	/**
	 * close() called by the garbage collector right before trashing object
	 */
	public void Finalize() throws SocketException, InterruptedException {

		if (!didClose) {
			close();
			throw new SocketException("Socket not closed before object destruction, must call close method.");
		}
	}

	private void sendDeviceAddress() throws IOException {
		String message = "type: send_address\n";
		message += "packet_id: 00\n";
		message += "device_address_long: " + zigbeeAddress.getAddress() + "\n";
		DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(strHostAddress), iDstPort);
		socket.send(sendPacket);
	}

	private void receieveWorker() {
		while (!(endTask.get())) {

			byte[] recBuffer = new byte[SOCKET_RECEIVE_BUFFER_SIZE];
			try {
				DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
				socket.receive(recPacket);

				// Convert the UDP data into a string format
				String dataString = new String(recPacket.getData());

				// split the data by line so we can start procesisng
				String[] lines = dataString.split("\n");

				Map<String, String> packetData = new HashMap<String, String>();
				for (String line : lines) {

					// trim the line
					String trimmedLine = line.trim();
					// make sure this is a valid data line and not just blank
					if (trimmedLine.length() == 0) {
						continue;
					}

					// Split the data into parts
					String[] parts = trimmedLine.split(":");
					parts[0] = parts[0].trim();
					parts[1] = parts[1].trim();
					packetData.put(parts[0], parts[1]);
				}

				if (packetData.get("type").equals("send_address_response")) {
					didSuccesfullySendAddress.set(true);

				} else {
					IoTZigbeeMessage callbackMessage = null;

					//made by changwoo
					if (packetData.get("type").equals("zcl_zone_status_change_notification")){
						int packetId = Integer.parseInt(packetData.get("packet_id"), 16);
						int clusterId = Integer.parseInt(packetData.get("cluster_id"), 16);
						int profileId = Integer.parseInt(packetData.get("profile_id"), 16);
						int status = Integer.parseInt(packetData.get("status"), 10);
						boolean successOrFail = false;
						if(packetData.get("attributes").equals("success")) successOrFail=true;
						callbackMessage = new IoTZigbeeMessageZclZoneStatusChangeNotification(packetId, clusterId, profileId, status, successOrFail);

					//made by changwoo
					} else if (packetData.get("type").equals("zcl_write_attributes_response")) {

						int packetId = Integer.parseInt(packetData.get("packet_id"), 16);
						int clusterId = Integer.parseInt(packetData.get("cluster_id"), 16);
						int profileId = Integer.parseInt(packetData.get("profile_id"), 16);
						boolean successOrFail = false;
						if(packetData.get("attributes").equals("success")) successOrFail=true;
						
						callbackMessage = new IoTZigbeeMessageZclWriteAttributesResponse(packetId, clusterId, profileId, successOrFail);

					} else if (packetData.get("type").equals("zcl_read_attributes_response")) {
						int packetId = Integer.parseInt(packetData.get("packet_id"), 16);
						int clusterId = Integer.parseInt(packetData.get("cluster_id"), 16);
						int profileId = Integer.parseInt(packetData.get("profile_id"), 16);

						List<IoTZigbeeMessageZclReadAttributesResponse.Attribute> attrList = new ArrayList<IoTZigbeeMessageZclReadAttributesResponse.Attribute>();

						String[] attributes = packetData.get("attributes").split(";");
						for (String attr : attributes) {
							attr = attr.trim();
							String[] parts = attr.split(",");

							if (parts.length == 2) {
								parts[0] = parts[0].trim();
								parts[1] = parts[1].trim();

								IoTZigbeeMessageZclReadAttributesResponse.Attribute at = new IoTZigbeeMessageZclReadAttributesResponse.Attribute(Integer.parseInt(parts[0], 16), 0, false, null);
								attrList.add(at);
							} else {
								parts[0] = parts[0].trim();
								parts[1] = parts[1].trim();
								parts[2] = parts[2].trim();
								parts[3] = parts[3].trim();
								IoTZigbeeMessageZclReadAttributesResponse.Attribute at = new IoTZigbeeMessageZclReadAttributesResponse.Attribute(Integer.parseInt(parts[0], 16), Integer.parseInt(parts[1], 16), true, hexStringToByteArray(parts[3]));
								attrList.add(at);
							}
						}

						callbackMessage = new IoTZigbeeMessageZclReadAttributesResponse(packetId, clusterId, profileId, attrList);

					} else if (packetData.get("type").equals("zcl_configure_reporting_response")) {
						int packetId = Integer.parseInt(packetData.get("packet_id"), 16);
						int clusterId = Integer.parseInt(packetData.get("cluster_id"), 16);
						int profileId = Integer.parseInt(packetData.get("profile_id"), 16);

						if (packetData.get("attributes").equals("all_success")) {
							callbackMessage = new IoTZigbeeMessageZclConfigureReportingResponse(packetId, clusterId, profileId, true, null);
						} else {
							List<IoTZigbeeMessageZclConfigureReportingResponse.Attribute> attrList = new ArrayList<IoTZigbeeMessageZclConfigureReportingResponse.Attribute>();

							String[] attributes = packetData.get("attributes").split(";");
							for (String attr : attributes) {
								attr = attr.trim();
								String[] parts = attr.split(",");
								parts[0] = parts[0].trim();
								parts[1] = parts[1].trim();
								parts[2] = parts[2].trim();
								IoTZigbeeMessageZclConfigureReportingResponse.Attribute at = new IoTZigbeeMessageZclConfigureReportingResponse.Attribute(Integer.parseInt(parts[0], 16), parts[1].equals("success"), parts[2].equals("reported"));
								attrList.add(at);
							}
							callbackMessage = new IoTZigbeeMessageZclConfigureReportingResponse(packetId, clusterId, profileId, false, attrList);
						}

					} else if (packetData.get("type").equals("zcl_report_attributes")) {
						int packetId = Integer.parseInt(packetData.get("packet_id"), 16);
						int clusterId = Integer.parseInt(packetData.get("cluster_id"), 16);
						int profileId = Integer.parseInt(packetData.get("profile_id"), 16);

						List<IoTZigbeeMessageZclReportAttributes.Attribute> attrList = new ArrayList<IoTZigbeeMessageZclReportAttributes.Attribute>();

						String[] attributes = packetData.get("attributes").split(";");
						for (String attr : attributes) {
							attr = attr.trim();
							String[] parts = attr.split(",");

							parts[0] = parts[0].trim();
							parts[1] = parts[1].trim();
							parts[2] = parts[2].trim();
							IoTZigbeeMessageZclReportAttributes.Attribute at = new IoTZigbeeMessageZclReportAttributes.Attribute(Integer.parseInt(parts[0], 16), Integer.parseInt(parts[1], 16), hexStringToByteArray(parts[2]));
							attrList.add(at);
						}

						callbackMessage = new IoTZigbeeMessageZclReportAttributes(packetId, clusterId, profileId, attrList);

					} else if (packetData.get("type").equals("zcl_read_attributes")) {
						int packetId = Integer.parseInt(packetData.get("packet_id"), 16);
						boolean success = packetData.get("response").equals("success");

						if (success) {
							callbackMessage = new IoTZigbeeMessageZclReadAttributes(packetId, success, "");
						} else {
							callbackMessage = new IoTZigbeeMessageZclReadAttributes(packetId, success, packetData.get("reason"));
						}

					} else if (packetData.get("type").equals("zcl_configure_reporting")) {
						int packetId = Integer.parseInt(packetData.get("packet_id"), 16);
						boolean success = packetData.get("response").equals("success");

						if (success) {
							callbackMessage = new IoTZigbeeMessageZclConfigureReporting(packetId, success, "");
						} else {
							callbackMessage = new IoTZigbeeMessageZclConfigureReporting(packetId, success, packetData.get("reason"));
						}

					} else if (packetData.get("type").equals("zdo_bind_request")) {
						int packetId = Integer.parseInt(packetData.get("packet_id"), 16);
						boolean success = packetData.get("response").equals("success");

						if (success) {
							callbackMessage = new IoTZigbeeMessageZdoBindResponse(packetId, success, "");
						} else {
							callbackMessage = new IoTZigbeeMessageZdoBindResponse(packetId, success, packetData.get("reason"));
						}
					}

					else if (packetData.get("type").equals("zdo_unbind_request")) {
						int packetId = Integer.parseInt(packetData.get("packet_id"), 16);
						boolean success = packetData.get("response").equals("success");

						if (success) {
							callbackMessage = new IoTZigbeeMessageZdoUnBindResponse(packetId, success, "");
						} else {
							callbackMessage = new IoTZigbeeMessageZdoUnBindResponse(packetId, success, packetData.get("reason"));
						}
					}

					if (callbackMessage != null) {
						for (IoTZigbeeCallback c : callbackList) {
							c.newMessageAvailable(callbackMessage);
						}
					}
				}



			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String changeHexEndianness(String hexData) {

		List<String> pairedValues = new ArrayList<String>();
		for (int i = 0; i < hexData.length(); i += 2) {
			String part = hexData.substring(i, Math.min(i + 2, hexData.length()));
			pairedValues.add(part);
		}

		String retString  = "";
		for (int i = (pairedValues.size() - 1); i >= 0; i--) {
			retString += pairedValues.get(i);
		}
		return retString;
	}

	// taken from: http://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
			                      + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

}
















