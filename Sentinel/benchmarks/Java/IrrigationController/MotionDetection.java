package IrrigationController;
// IoT packages
import iotcode.interfaces.*;

// BoofCv packages
import boofcv.alg.background.BackgroundModelStationary;
import boofcv.factory.background.ConfigBackgroundGaussian;
import boofcv.factory.background.FactoryBackgroundModel;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.image.ImageGridPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.MediaManager;
import boofcv.io.UtilIO;
import boofcv.io.image.SimpleImageSequence;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.wrapper.DefaultMediaManager;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.ImageUInt8;
import boofcv.alg.filter.blur.BlurImageOps;


// Standard Java Packages
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

// RMI Packages
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// Checker annotations
//import iotchecker.qual.*;



/** Class MotionDetection to do motion detection using images
 *
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0
 * @since       2016-03-21
 */
class MotionDetection extends UnicastRemoteObject implements CameraCallback {

	// Define Like variables
	private static boolean DO_GRAPHICAL_USER_INTERFACE = false;

	/*******************************************************************************************************************************************
	**
	**  Constants
	**
	*******************************************************************************************************************************************/
	private final float MOTION_DETECTED_THRESHOLD_PERCENTAGE = 5;


	/*******************************************************************************************************************************************
	**
	**  Variables
	**
	*******************************************************************************************************************************************/

	// Timestamp buffer and locks needed for that safety on that buffer
	// This is the buffer for post-detection algorithm use
	private Date timestampOfLastMotion = null;
	private ReadWriteLock timestampReadWriteLock = new ReentrantReadWriteLock();
	private Lock timestampReadLock = timestampReadWriteLock.readLock();
	private Lock timestampWriteLock = timestampReadWriteLock.writeLock();

	// Flag for when new data is available and ready for processing
	private AtomicBoolean newFrameAvailable = new AtomicBoolean(false);

	// Flag for determining if motion has been detected and therefore
	// the callbacks should be issued
	private AtomicBoolean motionDetected = new AtomicBoolean(false);

	// Image and timestamp buffers and  locks needed for that safety on those buffers
	// Timestamp buffer for pre-detection algorithm use
	private BufferedImage latestImage = null;
	private Date possibleDate = null;
	private ReadWriteLock imageReadWriteLock = new ReentrantReadWriteLock();
	private Lock imageReadLock = imageReadWriteLock.readLock();
	private Lock imageWriteLock = imageReadWriteLock.writeLock();

	// List of objects wishing to receive callbacks from this class.
	private List<MotionDetectionCallback> callbackList = new ArrayList<MotionDetectionCallback>();

	// Variables to help with motion detection
	private ConfigBackgroundGaussian configGaussian = null;
	private BackgroundModelStationary backgroundDetector = null;
	private ImageUInt8 segmented = null;
	private ImageFloat32 newFrameFloat = null;

	// counts the number of frames since a background image is added to algorithm
	private int frameCounter = 0;



	/*******************************************************************************************************************************************
	**
	**  Threads
	**
	*******************************************************************************************************************************************/
	private Thread workThread = null;
	private Thread callBackThread = null;

	/*******************************************************************************************************************************************
	**
	**  GUI Stuff (Used Only for Testing)
	**
	*******************************************************************************************************************************************/
	ImageGridPanel gui;



	/** Constructor
	 *
	 *   @param _threshold       [float], Variable for gaussian background detector.
	 *   @param _learnSpeed      [float], Variable for gaussian background detector.
	 *   @param _initialVariance [float], Variable for gaussian background detector.
	 *   @param _minDifference   [float], Variable for gaussian background detector.
	 *
	 */
	public MotionDetection(float _threshold, float _learnSpeed, float _initialVariance, float _minDifference) throws RemoteException {

		// Configure the Gaussian model used for background detection
		configGaussian = new ConfigBackgroundGaussian(_threshold, _learnSpeed);
		configGaussian.initialVariance = _initialVariance;
		configGaussian.minimumDifference = _minDifference;

		// setup the background detector
		ImageType imageType = ImageType.single(ImageFloat32.class);
		backgroundDetector = FactoryBackgroundModel.stationaryGaussian(configGaussian, imageType);

		// setup the gui if we are going to use it
		if (DO_GRAPHICAL_USER_INTERFACE) {

			// create an image grid for images to place on, tile fashion
			gui = new ImageGridPanel(1, 2);

			// make the window large so we dont have to manually resize with the mouse
			gui.setSize(1920, 1080);

			// Make the window visible and set the title
			ShowImages.showWindow(gui, "Static Scene: Background Segmentation", true);
		}

		// Launch the worker thread
		workThread = new Thread(new Runnable() {
			public void run() {

				while (true) {
					runMotionDetection();
				}
			}
		});
		workThread.start();


		// Launch the callback thread
		callBackThread = new Thread(new Runnable() {
			public void run() {

				while (true) {
					doCallbacks();
				}
			}
		});
		callBackThread.start();
	}


	/*******************************************************************************************************************************************
	**
	**  Public Methods
	**
	*******************************************************************************************************************************************/

	/** Method to add a new frame to the motion detector.
	 *
	 *   @param _newFrame  [byte[]], Frame data of new frame.
	 *   @param _timestamp [Date]  , Timestamp of new frame.
	 *
	 *   @return [void] None.
	 */
	public void addFrame(byte[]  _newFrame, Date _timestamp) {
		BufferedImage img = null;

		try {
			// Parse the byte array into a Buffered Image
			InputStream in = new ByteArrayInputStream(_newFrame);
			img = ImageIO.read(in);

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// Save the image and timestamp for use later
		imageWriteLock.lock();						// lock the image and timestamp buffers since multithread
		latestImage = img;								// image into image buffer
		possibleDate = _timestamp;				// timestamp into timestamp buffer
		imageWriteLock.unlock();					// Never forget to unlock

		// flag the worker thread that there is new data ready for processing
		newFrameAvailable.set(true);
	}

	/** Method to get the timestamp of the last time motion was detected
	 *
	 *   @return [Date] timestamp of last motion or null if no motion was ever detected.
	 */
	public long getTimestampOfLastMotion() {
		Date ret = null;

		// Be safe because multithread
		timestampReadLock.lock();

		// Checks if there was ever motion, if not then timestampOfLastMotion
		// will be null
		if (timestampOfLastMotion != null) {
			// Clone since we don't know what the other person is going to do
			// with the timestamp
			ret = (Date)timestampOfLastMotion.clone();
		}

		timestampReadLock.unlock();
		long retLong = ret.getTime();

		return retLong;
	}


	/** Method to add a new frame to the motion detector from a camera
	 *
	 *   @param _camera  [Camera], Camera that has the new data.
	 *
	 *   @return [void] None.
	 */
	//public void newCameraFrameAvailable(@NonLocalRemote Camera _camera) {
	public void newCameraFrameAvailable(byte[] latestFrame, long timeStamp) {
		BufferedImage img = null;

		try {
			// Parse the byte array into a Buffered Image
			//InputStream in = new ByteArrayInputStream(_camera.getLatestFrame());
			InputStream in = new ByteArrayInputStream(latestFrame);
			img = ImageIO.read(in);

		} catch (RemoteException e) {
			e.printStackTrace();
			return;

		} catch (Exception e) {
			e.printStackTrace();
			return;

		}

		// Save the image and timestamp for use later
		imageWriteLock.lock();			// lock the image and timestamp buffers since multithread
		latestImage = img;					// image into image buffer

		// timestamp from camera into timestamo buffer
		//try {
			//long dateLong = _camera.getTimestamp();
			long dateLong = timeStamp;
			possibleDate = new Date(dateLong);
		//} catch (RemoteException e) {
		//	e.printStackTrace();
		//}

		imageWriteLock.unlock();		// Never forget to unlock

		// flag the worker thread that there is new data ready for processing
		newFrameAvailable.set(true);
	}

	/** Method to register an object to recieve callbacks from this motion detector
	 *
	 *   @param _mdc  [MotionDetectionCallback], object to recieve callbacks.
	 *
	 *   @return [void] None.
	 */
	public void registerCallback(MotionDetectionCallback _mdc) {
		callbackList.add(_mdc);
	}

	/*******************************************************************************************************************************************
	**
	**  Helper Methods
	**
	*******************************************************************************************************************************************/

	/** Method that constantly loops checking if new data is available.  If there is
	 *   new data, it is processed.
	 *   This method should be run on a separate thread.
	 *
	 *   @return [void] None.
	 */
	private void runMotionDetection() {

		// check if there is a new frame availble, only runs detection if there is new data to save
		// computation time
		if (!newFrameAvailable.get()) {
			return;
		}

		// Lock since we are accessing the data buffers
		imageReadLock.lock();

		// processing data so now the buffered data is old
		newFrameAvailable.set(false);

		// copy from buffer to local for processing
		Date tmpDate = possibleDate;

		// Allocate space for the segmented image based on the first image we received
		// cannot pre-allocate this since we do not know what the size of the images is
		// before the first image arrives
		if (segmented == null) {
			segmented = new ImageUInt8(latestImage.getWidth(), latestImage.getHeight());
		}

		// copy from data buffers and convert into correct data type for BoofCv libraries
		newFrameFloat = ConvertBufferedImage.convertFrom(latestImage, newFrameFloat);

		// All done accessing the data buffers
		imageReadLock.unlock();

		// Run background detection
		backgroundDetector.segment(newFrameFloat, segmented);

		// Update the background baseline every 10 frames, helps the algorithm
		frameCounter++;
		if (frameCounter > 10) {
			backgroundDetector.updateBackground(newFrameFloat);
			frameCounter = 0;
		}

		// get the raw pixel data, gray-scale image
		byte[] frameData = segmented.getData();

		// count the number of pixels of the image that was deemed as "motion"
		double count = 0;
		double countMotion = 0;
		for (byte b : frameData) {
			count++;
			if (b > 0) {
				countMotion++;
			}
		}

		// calculate the percentage of the image that was in motion
		double percentMotion = (countMotion / count) * 100.0;

		// Check if a high enough percentage of the image was in motion to say that there was motion in this frame of data
		if (percentMotion > MOTION_DETECTED_THRESHOLD_PERCENTAGE) {

			// Motion detected so save timestamp of this frame to another buffer
			timestampWriteLock.lock();
			timestampOfLastMotion = (Date)tmpDate.clone();			// clone to a different buffer
			timestampWriteLock.unlock();

			System.out.println("Motion Detected (with percentage: " + Double.toString(percentMotion) + "%)");
		}

		// Do output to the screen if we are using gui mode
		if (DO_GRAPHICAL_USER_INTERFACE) {

			// change image data unto correct type for rendering
			BufferedImage visualized1 = new BufferedImage(segmented.width, segmented.height, BufferedImage.TYPE_INT_RGB);
			VisualizeBinaryData.renderBinary(segmented, false, visualized1);

			// change image data unto correct type for rendering
			BufferedImage visualized2 = null;
			visualized2 = ConvertBufferedImage.convertTo(newFrameFloat, visualized2, true);

			// place the images into the image grid
			gui.setImage(0, 1, visualized1);
			gui.setImage(0, 2, visualized2);

			// trigger rendering
			gui.repaint();
		}
	}

	/** Method that constantly loops checking if the callbacks should be issues and
	 *   issues the callbacks if they should be issues.
	 *   This method should be run on a separate thread.
	 *
	 *   @return [void] None.
	 */
	private void doCallbacks() {

		// Keep looping forever for callback
		while (true) {

			// If motion detected
			if (motionDetected.compareAndSet(true, false)) {

				// Motion was detected so issue callbacks to all objects that registered
				// to receive callback from this class.
				for (MotionDetectionCallback c : callbackList) {
					//try {
						c.motionDetected(this.getTimestampOfLastMotion());
					//} catch (RemoteException re) {
					//}
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
}

