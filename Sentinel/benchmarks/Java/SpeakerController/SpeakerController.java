package SpeakerController;

// IoT Runtime packages
import iotruntime.slave.IoTSet;
import iotruntime.slave.IoTRelation;
//import iotcode.annotation.*;

// IoT driver packages
import iotcode.interfaces.*;
import iotcode.annotation.*;


// Standard Java packages
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.util.Date;	// TODO: Get rid of all depreciated stuff for date, switch to Calender
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Random;

// RMI packages
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// Checker annotations
//import iotchecker.qual.*;

/** Class SpeakerController for the smart home application benchmark
 *  <p>
 *  This controller controls speakers and streams music based on
 *  GPS-like input from a phone app that notifies the controller
 *  about the position of the person
 *
 * @author      Rahmadi Trimananda <rahmadi.trimananda @ uci.edu>
 * @version     1.0
 * @since       2016-04-28
 */
public class SpeakerController extends UnicastRemoteObject implements GPSGatewayCallback, SpeakerCallback {

	/*
	 *  Constants
	 */
	public static final int CHECK_TIME_WAIT_MSEC = 100;	// 1 means 1 x 100 = 0.1 second
	public static final String MUSIC_FILE_DIRECTORY = "./music/";	// file that the music files are in
	public static final int CURRENT_POSITION_SAMPLE_THRESHOLD = (int)((long)((long)3 * (long)CHECK_TIME_WAIT_MSEC * (long)44100) / (long)1000);

	/**
	 * IoT Sets of Devices
	 */
	@config private IoTSet < GPSGatewaySmart > gpsSet;
	@config private IoTSet < SpeakerSmart > speakerSet;

	/**
	 * IoT Sets of Things that are not devices such as rooms
	 */
	@config private IoTSet < RoomSmart > audioRooms;

	/**
	 * IoT Relations
	 */
	@config private IoTRelation < RoomSmart, SpeakerSmart > roomSpeakerRel;

	/**
	 * The state that the room main lights are supposed to be in
	 */
	Map < RoomSmart, Boolean > roomSpeakersOnOffStatus = new HashMap < RoomSmart, Boolean > ();

	// used to notify if new data is available
	private AtomicBoolean newDataAvailable = new AtomicBoolean(false);

	// the settings from the interface, used to setup the system
	private int roomIdentifier = 0;
	private boolean ringStatus = false;

	// playback state variables
	private int currentPosition = 0;
	private AtomicBoolean playbackDone = new AtomicBoolean(false);


	public SpeakerController() throws RemoteException {

	}


	/** Callback method for when room ID is retrieved.
	 *
	 * @param _roomIdentifier [int].
	 * @return [void] None.
	 */
	public void newRoomIDRetrieved(int _roomIdentifier) {

		roomIdentifier = _roomIdentifier;
		System.out.println("DEBUG: New room ID is retrieved from phone!!! Room: " + roomIdentifier);

		// new data available so set it to true
		newDataAvailable.set(true);
	}


	/** Callback method for when ring status is retrieved.
	 *
	 * @param _ringStatus [boolean].
	 * @return [void] None.
	 */
	public void newRingStatusRetrieved(boolean _ringStatus) {

		ringStatus = _ringStatus;
		System.out.println("DEBUG: New ring status is retrieved from phone!!! Status: " + ringStatus);

		// new data available so set it to true
		newDataAvailable.set(true);
	}


	/** Callback method when a speaker has finished playing what is in its audio buffer.
	 *
	 * @return [void] None.
	 */
	public void speakerDone() {
		for (SpeakerSmart speakers : speakerSet.values()) {
			playbackDone.set(true);
		}
	}


	/*******************************************************************************************************************************************
	**
	** Private Helper Methods
	**
	*******************************************************************************************************************************************/

	/**
	 * Update speakers action based on the updated speakers status
	 */
	private void updateSpeakersAction() {

		// Stream music on speakers based on their status
		for (RoomSmart room : audioRooms.values()) {

			// Check status of the room
			if (roomSpeakersOnOffStatus.get(room)) {

				// used to get the average of the speakers position
				long currPosTotal = 0;
				long currPosCount = 0;

				// Get the speaker objects one by one assuming that we could have
				// more than one speaker per room
				for (SpeakerSmart speakers : roomSpeakerRel.get(room)) {
					// System.out.println("DEBUG: Turn on speaker!");

					//try {

						// start the speaker playback if the speaker is not playing yet
						if (!speakers.getPlaybackState()) {

							System.out.println("Turning a speaker On in room: " + room.getRoomID());
							speakers.startPlayback();
							speakers.setPosition(currentPosition);

						} else {
							// get average of the positions
							currPosTotal += speakers.getPosition();
							currPosCount++;
						}


					//} catch (RemoteException e) {
					//	e.printStackTrace();
					//}
				}

				if (currPosCount != 0) {
					
					// get average position of the speakers
					int currentPosOfSpeakers = (int)(currPosTotal / currPosCount);

					// check how close we are to the correct position
					if (Math.abs(currentPosOfSpeakers - currentPosition) > CURRENT_POSITION_SAMPLE_THRESHOLD) {
						// we were kind of far so update all the positions

						for (SpeakerSmart speakers : roomSpeakerRel.get(room)) {
							//try {
								speakers.setPosition(currentPosOfSpeakers);
							//} catch (RemoteException e) {
							//	e.printStackTrace();
							//}
						}
					}

					// update the current position
					currentPosition = currentPosOfSpeakers;
				}



			} /*else {
				// Room status is "off"

				// used to get the average of the speakers position
				long currPosTotal = 0;
				long currPosCount = 0;

				// Get the speaker objects one by one assuming that we could have
				// more than one speaker per room
				for (Speaker speakers : roomSpeakerRel.get(room)) {
					// System.out.println("DEBUG: Turn off speaker!");
					try {
						// Turning off speaker if they are still on
						if (speakers.getPlaybackState()) {
							System.out.println("Turning a speaker off");

							currPosTotal += (long)speakers.getPosition();
							currPosCount++;
							boolean tmp = speakers.stopPlayback();

							if (!tmp) {
								System.out.println("Error Turning off");
							}
						}

					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}

				// get the average current position of the speakers
				// so we can resume other speakers from same position
				if (currPosCount != 0) {
					currentPosition = (int)(currPosTotal / currPosCount);
				}
			}*/
		}

		// a speaker has finished playing and so we should change all the audio buffers
		if (playbackDone.get()) {

			// song done so update the audio buffers
			prepareNextSong();
			playbackDone.set(false);
		}
	}


	/**
	 * Update speakers status based on room ID and ring status information from phone
	 */
	private void updateSpeakersStatus() {

		// If we have new data, we update the speaker streaming
		if (newDataAvailable.get()) {

			// System.out.println("DEBUG: New data is available!!!");
			// System.out.println("DEBUG: Ring status: " + ringStatus);
			// Check for ring status first
			if (ringStatus) {

				// Turn off all speakers if ring status is true (phone is ringing)
				for (RoomSmart room : audioRooms.values()) {

					// System.out.println("DEBUG: Update status off for speakers! Phone is ringing!!!");
					// Turn off speaker
					roomSpeakersOnOffStatus.put(room, false);
				}

			} else {
				// Phone is not ringing... just play music on the right speaker

				// Check for every room
				for (RoomSmart room : audioRooms.values()) {

					//try {
						// Turn on the right speaker based on room ID sent from phone app
						// Stream audio to a speaker based on room ID
						if (room.getRoomID() == roomIdentifier) {

							// System.out.println("DEBUG: This room ID: " + room.getRoomID());
							// System.out.println("DEBUG: Turn on the speaker(s) in this room!!!");
							// Set speaker status to on
							roomSpeakersOnOffStatus.put(room, true);

						} else {
							// for the rooms whose IDs aren't equal to roomIdentifier

							// System.out.println("DEBUG: Turn on speaker!");
							// Set speaker status to off
							roomSpeakersOnOffStatus.put(room, false);
						}

					//} catch (RemoteException ex) {
					//	ex.printStackTrace();
					//}
				}
			}
			// Finish processing data - put this back to false
			newDataAvailable.set(false);
		}
	}

	/**
	 * Prepare the speakers for a new song to start playing
	 */
	private void prepareNextSong() {
		System.out.println("Starting Music Prep");

		System.out.println("Stopping all device playback");
		// stop all devices that are still playing and clear their buffers
		// they are about to end playback anyways
		for (SpeakerSmart speakers : speakerSet.values()) {
			try {

				if (speakers.getPlaybackState()) {
					speakers.stopPlayback();
				}
				speakers.clearData();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// get the music file names that are in the music files directory
		File musicFolder = new File(MUSIC_FILE_DIRECTORY);
		File[] audioFiles = musicFolder.listFiles();
		List<String> audioFileNames = new ArrayList<String>();

		// put all names in a list
		for (int i = 0; i < audioFiles.length; i++) {
			if (audioFiles[i].isFile()) {
				try {
					audioFileNames.add(audioFiles[i].getCanonicalPath());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		// pick a random file to play
		Random rand = new Random(System.nanoTime());
		String audioFilename = audioFileNames.get(rand.nextInt(audioFileNames.size()));

		System.out.println("Going to load audio file");
		System.out.println(audioFilename);

		// decode the mp3 file
		System.out.println("Starting Decode");
		MP3Decoder dec = new MP3Decoder(audioFilename);
		List<short[]> dat = dec.getDecodedFrames();
		System.out.println("Ending Decode");

		currentPosition = 0;

		// count the number of samples
		int count = 0;
		for (short[] d : dat) {
			count += d.length;
		}

		// make into a single large buffer for 1 large RMI call
		short[] compressedArray = new short[count];
		count = 0;
		for (short[] d : dat) {
			for (short s : d) {
				compressedArray[count] = s;
				count++;
			}
		}


		System.out.println("Loading Speakers");
		// send the new data to all the speakers
		for (SpeakerSmart speakers : speakerSet.values()) {
			System.out.println("Loading a single speaker with data");
			try {
				speakers.loadData(compressedArray, 0, compressedArray.length);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Done loading a single speaker with data");
		}

		System.out.println("All Speakers done loading");

	}



	/********************************************************************************************************
	** Public methods, called by the runtime
	*********************************************************************************************************/

	/** Initialization method, called by the runtime (effectively the main of the controller)
	 *   This method runs a continuous loop and is blocking
	 *
	 *   @return [void] None;
	 */
	public void init() throws RemoteException, InterruptedException {

		// Initialize the rooms
		for (RoomSmart room : audioRooms.values()) {
			// All rooms start with the speakers turned off
			roomSpeakersOnOffStatus.put(room, false);
		}

		// Setup the cameras, start them all and assign each one a motion detector
		for (GPSGatewaySmart gw : gpsSet.values()) {

			//try {
				// initialize, register callback, and start the gateway
				gw.init();
				gw.registerCallback(this);
				gw.start();
			//} catch (RemoteException ex) {
			//	ex.printStackTrace();
			//}
		}


		//Initialize the speakers
		for (SpeakerSmart speakers : speakerSet.values()) {
			speakers.init();
		}

		prepareNextSong();

		// Run the main loop that will keep checking stuff
		while (true) {

			// Update speakers status (on/off) based on info from phone
			updateSpeakersStatus();

			// Check and turn on/off (stream music) through speakers based on its status
			updateSpeakersAction();

			try {
				Thread.sleep(CHECK_TIME_WAIT_MSEC); // sleep for a tenth of the time
			} catch (Exception e) {
			}
		}
	}
}




