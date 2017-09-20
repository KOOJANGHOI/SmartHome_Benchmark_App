package iotcode.AmcrestCamera;

// IoT Packages
import iotcode.annotation.*;
import iotcode.interfaces.*;
import iotruntime.IoTHTTP;
import iotruntime.slave.IoTSet;
import iotruntime.slave.IoTDeviceAddress;

// Standard Java Packages
import java.io.IOException;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Base64;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Iterator;
import javax.imageio.ImageIO;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

// RMI Packages
import java.rmi.Remote;
import java.rmi.RemoteException;

// Checker annotations
//import iotchecker.qual.*;

public class AmcrestCamera implements Camera {

	/*******************************************************************************************************************************************
	**
	**  Variables
	**
	*******************************************************************************************************************************************/
	private String credentialUsername = "";
	private String credentialPassword = "";
	private DataInputStream dataInStream = null;
	private boolean isStreamConnected = false;
	private byte[] latestImage = null;
	private ReadWriteLock imageReadWriteLock = new ReentrantReadWriteLock();
	private Lock imageReadLock = imageReadWriteLock.readLock();
	private Lock imageWriteLock = imageReadWriteLock.writeLock();
	private AtomicBoolean newFrameAvailable = new AtomicBoolean(false);
	private ReadWriteLock timestampReadWriteLock = new ReentrantReadWriteLock();
	private Lock timestampReadLock = timestampReadWriteLock.readLock();
	private Lock timestampWriteLock = timestampReadWriteLock.writeLock();
	private Date latestImageTimestamp = null;
	private List <CameraSmartCallback> callbackList =
	    new CopyOnWriteArrayList <CameraSmartCallback> ();
	private AtomicBoolean doEnd = new AtomicBoolean(false);
	private IoTDeviceAddress deviceAddress = null;
	private AtomicBoolean didInit = new AtomicBoolean();
	private AtomicBoolean didStart = new AtomicBoolean();
	static Semaphore settingsSettings = new Semaphore(1);

	/*******************************************************************************************************************************************
	**
	**  Threads
	**
	*******************************************************************************************************************************************/
	private Thread callbackThread = null;
	private Thread workerThread = null;


	/*******************************************************************************************************************************************
	**
	**  IoT Sets and Relations
	**
	*******************************************************************************************************************************************/

	// IoTSet of Device Addresses.
	// Will be filled with only 1 address.
	@config private IoTSet<IoTDeviceAddress> cam_addresses;


	public AmcrestCamera(String _credentialUsername, String _credentialPassword) throws RemoteException {
		credentialUsername = _credentialUsername;
		credentialPassword = _credentialPassword;
	}

	/*******************************************************************************************************************************************
	**
	**  Camera Interface Methods
	**
	*******************************************************************************************************************************************/

	public byte[] getLatestFrame() {

		byte[] newImage = null;

		imageReadLock.lock();
		try {
			if (latestImage != null) {
				newImage = Arrays.copyOf(latestImage, latestImage.length);
			}
		} catch (Exception x) {
			x.printStackTrace();
		}
		imageReadLock.unlock();

		return newImage;
	}

	public long getTimestamp() {
		timestampReadLock.lock();
		Date ret = (Date)latestImageTimestamp.clone();
		timestampReadLock.unlock();
		long retLong = ret.getTime();
		return retLong;
	}

	public void registerCallback(CameraSmartCallback _callbackTo) {
		callbackList.add(_callbackTo);
	}

	public boolean setFPS(int _fps) {
		try {
			settingsSettings.acquire();

			String camUrlString = "/cgi-bin/configManager.cgi?action=setConfig&Encode[0].MainFormat[0].Video.FPS=" + Integer.toString(_fps);

			try {

				String credsPreBase64 = credentialUsername + ":" + credentialPassword;
				String credsBase64 = Base64.getEncoder().encodeToString(credsPreBase64.getBytes("utf-8"));
				String httpAuthCredentials = "Basic " + credsBase64;

				IoTHTTP httpConnection = new IoTHTTP(deviceAddress);
				httpConnection.setURL(camUrlString);
				httpConnection.openConnection();
				httpConnection.setDoInput(true);
				httpConnection.setRequestProperty("Authorization", httpAuthCredentials);
				httpConnection.connect();

				InputStream is = httpConnection.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				DataInputStream din = new DataInputStream(bis);

				// wait for a response
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}

				byte[] byteBuf = new byte[100];
				try {
					int r = din.read(byteBuf, 0, byteBuf.length);
					String retString = new String(byteBuf);

					if (!retString.substring(0, 2).equals("OK")) {
						httpConnection.disconnect();
						return false;
					}

				} catch (Exception e) {
					httpConnection.disconnect();
					return false;
					// e.printStackTrace();
				}

				httpConnection.disconnect();
			} catch (IOException e) {
				return false;
			} catch (Exception e) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		settingsSettings.release();

		return true;
	}

	public int getMaxFPS() {
		// Hard coded since this is hardware dependant
		return 30;
	}

	public int getMinFPS() {
		// Hard coded since this is hardware dependant
		return 5;
	}

	public List<Resolution> getSupportedResolutions() {

		// Hard coded since this is hardware dependant
		List<Resolution> ret = new ArrayList<Resolution>();
		ret.add(Resolution.RES_1080P);
		ret.add(Resolution.RES_720P);
		ret.add(Resolution.RES_VGA);
		return ret;
	}

	public boolean setResolution(Resolution _res) {

		try {
			settingsSettings.acquire();


			String camUrlString = "/cgi-bin/configManager.cgi?action=setConfig";

			if (_res == Resolution.RES_1080P) {
				camUrlString += "&Encode[0].MainFormat[0].Video.Height=1080&Encode[0].MainFormat[0].Video.Width=1920";

			} else if (_res == Resolution.RES_720P) {
				camUrlString += "&Encode[0].MainFormat[0].Video.Height=720&Encode[0].MainFormat[0].Video.Width=1280";

			} else if (_res == Resolution.RES_VGA) {
				camUrlString += "&Encode[0].MainFormat[0].Video.Height=480&Encode[0].MainFormat[0].Video.Width=640";
			}


			try {

				String credsPreBase64 = credentialUsername + ":" + credentialPassword;
				String credsBase64 = Base64.getEncoder().encodeToString(credsPreBase64.getBytes("utf-8"));
				String httpAuthCredentials = "Basic " + credsBase64;

				IoTHTTP httpConnection = new IoTHTTP(deviceAddress);
				httpConnection.setURL(camUrlString);
				httpConnection.openConnection();
				httpConnection.setDoInput(true);
				httpConnection.setRequestProperty("Authorization", httpAuthCredentials);
				httpConnection.connect();

				InputStream is = httpConnection.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				DataInputStream din = new DataInputStream(bis);

				// wait for a response
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}

				byte[] byteBuf = new byte[100];
				try {
					int r = din.read(byteBuf, 0, byteBuf.length);
					String retString = new String(byteBuf);

					if (!retString.substring(0, 2).equals("OK")) {
						httpConnection.disconnect();
						return false;
					}

				} catch (Exception e) {
					httpConnection.disconnect();
					return false;
					// e.printStackTrace();
				}

				httpConnection.disconnect();
			} catch (IOException e) {
				return false;
			} catch (Exception e) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		settingsSettings.release();

		return true;
	}

	public void start() {

		if (didStart.compareAndSet(false, true) == false) {
			return; // already started
		}



		doEnd.set(false);

		if (!streamConnect()) {
			return;
		}

		callbackThread = new Thread(new Runnable() {
			public void run() {
				doCallbacks();
			}
		});
		callbackThread.start();

		workerThread = new Thread(new Runnable() {
			public void run() {
				doWork();
			}
		});
		workerThread.start();
	}

	public void stop() {
		if (didStart.compareAndSet(true, false) == false) {
			return; // already stopped
		}

		doEnd.set(true);

		try {
			callbackThread.join();
			workerThread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}

		streamDisconnect();
	}

	public void init() {
		if (didInit.compareAndSet(false, true) == false) {
			return; // already init
		}

		// get the device address and save it for later use when creating HTTP connections
		Iterator itr = cam_addresses.iterator();
		deviceAddress = (IoTDeviceAddress)itr.next();

		System.out.println("Address: " + deviceAddress.getCompleteAddress());
	}

	/*******************************************************************************************************************************************
	**
	**  Helper Methods
	**
	*******************************************************************************************************************************************/
	private byte[] readFromStream(int num) {
		byte[] byteBuf = new byte[num];
		try {
			dataInStream.readFully(byteBuf, 0, byteBuf.length);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return byteBuf;
	}

	private void findBoundry() {
		String boundary = "**************";
		while (true) {
			byte b = readFromStream(1)[0];
			boundary = boundary.substring(1);
			boundary += (char)b;

			if (boundary.equals("--myboundary\r\n")) {
				break;
			}
		}
	}

	private String getLine() {
		String line = "";
		while (true) {
			byte b = readFromStream(1)[0];
			char c = (char)b;

			if (c == '\n') {
				break;
			} else if (c != '\r') {
				line += c;
			}
		}

		return line;
	}

	private BufferedImage parseImage() {

		findBoundry();

		String contentTypeString = getLine();
		String contentLengthString = getLine();

		// remove the new line characters \r\n
		readFromStream(2);

		int imageDataLength = Integer.parseInt(contentLengthString.substring(16));

		byte[] imageDataBuf = readFromStream(imageDataLength);

		// remove the new line characters \r\n
		readFromStream(2);


		try {
			InputStream imageInStream = new ByteArrayInputStream(imageDataBuf);
			return ImageIO.read(imageInStream);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Has Exception");

		}

		return null;
	}

	private boolean streamConnect() {

		try {

			String credsPreBase64 = credentialUsername + ":" + credentialPassword;
			String credsBase64 = Base64.getEncoder().encodeToString(credsPreBase64.getBytes("utf-8"));
			String httpAuthCredentials = "Basic " + credsBase64;

			IoTHTTP httpConnection = new IoTHTTP(deviceAddress);
			httpConnection.setURL("/cgi-bin/mjpg/video.cgi?");
			httpConnection.openConnection();
			httpConnection.setDoInput(true);
			httpConnection.setRequestProperty("Authorization", httpAuthCredentials);
			httpConnection.connect();

			InputStream is = httpConnection.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			dataInStream = new DataInputStream(bis);

			isStreamConnected = true;

		} catch (IOException e) {
			isStreamConnected = false;

		} catch (Exception e) {
			isStreamConnected = false;
		}

		return isStreamConnected;
	}

	private void streamDisconnect() {
		try {
			if (isStreamConnected) {
				dataInStream.close();
				isStreamConnected = false;
			}
		} catch (Exception e) {
		}
	}

	private void doCallbacks() {

		while (!doEnd.get()) {
			if (newFrameAvailable.compareAndSet(true, false)) {

				for (CameraSmartCallback c : callbackList) {

					c.newCameraFrameAvailable(this.getLatestFrame(), this.getTimestamp());
					//c.newCameraFrameAvailable(this);
				}
			} else {

				// Sleep for 15 millisec to give time for new frame to arrive
				try {
					Thread.sleep(15);
				} catch (InterruptedException ie) {
				}
			}
		}

	}

	private void doWork() {

		// parse the images that are loaded into the buffer
		while (!doEnd.get()) {

			BufferedImage img = parseImage();

			if (img != null) {

				timestampWriteLock.lock();
				latestImageTimestamp = new Date();
				timestampWriteLock.unlock();

				imageWriteLock.lock();

				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(img, "jpg", baos);
					baos.flush();
					latestImage = baos.toByteArray();
					baos.close();

				} catch (Exception e) {

				}
				imageWriteLock.unlock();

				newFrameAvailable.set(true);
			}

			try {
				if (dataInStream.available() > 120000) {
					dataInStream.skip(120000);
				}
			} catch (Exception e) {
			}
		}

	}

}
















