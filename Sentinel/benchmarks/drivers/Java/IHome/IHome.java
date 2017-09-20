package iotcode.IHome;

// IoT Packages
import iotcode.interfaces.*;
import iotcode.annotation.*;
import iotruntime.IoTUDP;
import iotruntime.IoTTCP;
import iotruntime.slave.IoTSet;
import iotruntime.slave.IoTDeviceAddress;

// RMI Packages
import java.rmi.Remote;
import java.rmi.RemoteException;

// Checker annotations
//import iotchecker.qual.*;

// Standard Java Packages
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CopyOnWriteArrayList;


public class IHome implements Speaker {


    /*******************************************************************************************************************************************
    **  Constants
    *******************************************************************************************************************************************/

    public static final float VOLUME_MUTED_VALUE_DB = (float) (-144.0);
    public static final float VOLUME_MIN_VALUE_DB = (float) (-30.0);
    public static final float VOLUME_MAX_VALUE_DB = (float) (-0.0);
    public static final float DEFAULT_VOLUME = (float) (30.0);

    public static final long SEQUENCE_NUMBER_INITIAL_VALUE = 18086;
    public static final long SEQUENCE_NUMBER_WRAP_AROUND = 32768L;
    public static final long RTP_TIMESTAMP_INITIAL_VALUE = 3132223670L;
    public static final long RTP_TIMESTAMP_INCREMENT_VALUE = 352L;
    public static final long SOURCE_ID = 1326796157;
    public static final long SEQUENCE_ID = 0x86b27741;

    private IoTDeviceAddress tcpAddress = null;
    private IoTDeviceAddress myAddress = null;
    private IoTDeviceAddress controlAddress = null;
    private IoTDeviceAddress timingAddress = null;
    private IoTDeviceAddress serverAddress = null;

    private IoTTCP iHomeTCPConnection = null;

    private AtomicBoolean driverIsShuttingDown = new AtomicBoolean();
    private boolean didClose = false;

    private AtomicBoolean didEnd = new AtomicBoolean();
    private AtomicBoolean playbackStarted = new AtomicBoolean();
    private AtomicBoolean playbackFileIsDone = new AtomicBoolean();
    private AtomicBoolean isDoneEnding = new AtomicBoolean();
    private AtomicBoolean playbackState = new AtomicBoolean();

    private AtomicBoolean didInit = new AtomicBoolean();
    private AtomicBoolean playbackAboutToStart = new AtomicBoolean();
    private AtomicBoolean settingVolume = new AtomicBoolean();
    private AtomicBoolean playbackAboutToStop = new AtomicBoolean();



    private long sequenceNumber = SEQUENCE_NUMBER_INITIAL_VALUE;
    private long rtpTimestamp = RTP_TIMESTAMP_INITIAL_VALUE;

    private long currentPlaybackTime = 0;
    static Semaphore currentPlaybackTimeMutex = new Semaphore(1);

    private long desiredPlaybackTime = 0;
    static Semaphore desiredPlaybackTimeMutex = new Semaphore(1);


    private String connectionURL = "";
    private float currentVolume = DEFAULT_VOLUME;
    private LinkedList audioLinkedList = new LinkedList();

    private List < SpeakerSmartCallback > callbackList = new CopyOnWriteArrayList< SpeakerSmartCallback > ();

    /*******************************************************************************************************************************************
    **  Threads
    *******************************************************************************************************************************************/
    private Thread timingThread = null;
    private Thread audioThread = null;
    private Thread controlThread = null;
    private Thread monitorThread = null;


    @config private IoTSet<IoTDeviceAddress> speakerAddresses;

    public IHome() {
        didInit.set(false);
        playbackAboutToStart.set(false);
        settingVolume.set(false);
    }

    /*******************************************************************************************************************************************
    **
    **  Speaker Interface Methods
    **
    *******************************************************************************************************************************************/

    public void init() {

        if (didInit.compareAndSet(false, true) == false) {
            return; // already init
        }

        didEnd.set(false);
        isDoneEnding.set(true);
        playbackFileIsDone.set(false);
        Map<String, Integer> addrCount = new HashMap<String, Integer>();


        // get correct addresses
        for (IoTDeviceAddress devAdrr : speakerAddresses.values()) {
            if (addrCount.containsKey(devAdrr.getAddress())) {
                addrCount.put(devAdrr.getAddress(), addrCount.get(devAdrr.getAddress()) + 1);
            } else {
                addrCount.put(devAdrr.getAddress(), 1);
            }
        }

        for (IoTDeviceAddress devAdrr : speakerAddresses.values()) {
            if (addrCount.get(devAdrr.getAddress()) <= 1) {
                myAddress = devAdrr;
            } else {
                if (devAdrr.getIsDstPortWildcard()) {
                    if (controlAddress == null) {
                        controlAddress = devAdrr;
                    } else if (timingAddress == null) {
                        timingAddress = devAdrr;
                    } else {
                        serverAddress = devAdrr;
                    }
                } else {
                    tcpAddress = devAdrr;
                }
            }
        }

        System.out.println("tcpAddress: " + tcpAddress.getAddress() + ":" + tcpAddress.getSourcePortNumber() +
                           ":" + tcpAddress.getDestinationPortNumber());
        System.out.println("myAddress: " + myAddress.getAddress() + ":" + myAddress.getSourcePortNumber() +
                           ":" + myAddress.getDestinationPortNumber());
        System.out.println("controlAddress: " + controlAddress.getAddress() + ":" + controlAddress.getSourcePortNumber() +
                           ":" + controlAddress.getDestinationPortNumber());
        System.out.println("timingAddress: " + timingAddress.getAddress() + ":" + timingAddress.getSourcePortNumber() +
                           ":" + timingAddress.getDestinationPortNumber());
        System.out.println("serverAddress: " + serverAddress.getAddress() + ":" + serverAddress.getSourcePortNumber() +
                           ":" + serverAddress.getDestinationPortNumber());

        // Launch the worker function in a separate thread.
        monitorThread = new Thread(new Runnable() {
            public void run() {
                monitorThreadWorker();
            }
        });
        monitorThread.start();
    }



    public boolean startPlayback() {


        if (playbackAboutToStart.compareAndSet(false, true) == false) {
            return false;
        }

        if (playbackStarted.get()) {
            return true;
        }

        if (isDoneEnding.get() == false) {
            return false;
        }

        // Reset all Parameters
        didEnd.set(false);
        playbackFileIsDone.set(false);
        playbackState.set(true);

        try {
            currentPlaybackTimeMutex.acquire();
            currentPlaybackTime = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentPlaybackTimeMutex.release();



        try {
            desiredPlaybackTimeMutex.acquire();
            desiredPlaybackTime = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        desiredPlaybackTimeMutex.release();

        sequenceNumber = SEQUENCE_NUMBER_INITIAL_VALUE;
        rtpTimestamp = RTP_TIMESTAMP_INITIAL_VALUE;

        try {
            // start TCP connection
            iHomeTCPConnection = new IoTTCP(tcpAddress);
            iHomeTCPConnection.setReuseAddress(true);

            // Get in and out communication
            PrintWriter tcpOut = new PrintWriter(iHomeTCPConnection.getOutputStream(), true);
            BufferedReader tcpIn = new BufferedReader(new InputStreamReader(iHomeTCPConnection.getInputStream()));


            String session = String.valueOf(SOURCE_ID);
            connectionURL = "rtsp://" + myAddress.getAddress() + "/" + session;

            // Construct The commands
            String optionsCommand = "OPTIONS * RTSP/1.0\r\n" +
                                    "CSeq: 1\r\n" +
                                    "User-Agent: iTunes/11.0.4 (Windows; N)\r\n" +
                                    "Client-Instance: c0cb804fd20e80f6\r\n" +
                                    "Apple-Challenge: i8j36XRYVmSZs9nZ7Kf0Cg\r\n\r\n";

            String announceCommandBody = "v=0\r\n" +
                                         "o=iTunes " + session + " 0 IN IP4 " + myAddress.getAddress() + "\r\n" +
                                         "s=iTunes\r\n" +
                                         "c=IN IP4 " + tcpAddress.getAddress() + "\r\n" +
                                         "t=0 0\r\n" +
                                         "m=audio 0 RTP/AVP 96\r\n" +
                                         "a=rtpmap:96 AppleLossless\r\n" +
                                         "a=fmtp:96 352 0 16 40 10 14 2 255 0 0 44100\r\n";

            String announceCommand = "ANNOUNCE " + connectionURL + " RTSP/1.0\r\n" +
                                     "CSeq: 1\r\n" +
                                     "Content-Type: application/sdp\r\n" +
                                     "Content-Length: " + announceCommandBody.length() + "\r\n" +
                                     "User-Agent: iTunes/11.0.4 (Windows; N)\r\n\r\n" +
                                     announceCommandBody;


            // get the ports that we are going to tell the iHome to use
            int ourControlPort = controlAddress.getSourcePortNumber();
            int ourTimingPort = timingAddress.getSourcePortNumber();

            String setupCommand = "SETUP " + connectionURL + " RTSP/1.0\r\n" +
                                  "CSeq: 2\r\n" +
                                  "Transport: RTP/AVP/UDP;unicast;interleaved=0-1;mode=record;control_port=" + Integer.toString(ourControlPort) + ";timing_port=" + Integer.toString(ourTimingPort) + "\r\n" +
                                  "User-Agent: iTunes/11.0.4 (Windows; N)\r\n\r\n";

            String recordCommand = "RECORD " + connectionURL + " RTSP/1.0\r\nCSeq: 3\r\nSession: 1\r\nRange: npt=0-\r\nRTP-Info: seq=" + sequenceNumber + ";rtptime=" + rtpTimestamp + "\r\nUser-Agent: iTunes/11.0.4 (Windows; N)\r\n\r\n";


            Thread.sleep(100);
            tcpOut.print(optionsCommand);
            tcpOut.flush();
            while (!tcpIn.ready()) {
            }
            while (tcpIn.ready()) {
                String answer = tcpIn.readLine();
                System.out.println(answer);
            }

            Thread.sleep(100);
            tcpOut.print(announceCommand);
            tcpOut.flush();

            while (!tcpIn.ready()) {
            }
            while (tcpIn.ready()) {
                String answer = tcpIn.readLine();
                System.out.println(answer);
            }

            Thread.sleep(100);
            tcpOut.print(setupCommand);
            tcpOut.flush();
            while (!tcpIn.ready()) {
            }

            // ports that the speaker told us to communicate over
            int serverPort = -1;
            int controlPort = -1;
            int timingPort = -1;

            while (tcpIn.ready()) {
                String answer = tcpIn.readLine();
                System.out.println(answer);

                if (answer.contains("Transport")) {

                    String[] splitString = answer.split(";");

                    for (String str : splitString) {
                        String[] keyValue = str.split("=");

                        if (keyValue.length == 2) {
                            if (keyValue[0].equals("server_port")) {
                                serverPort = Integer.parseInt(keyValue[1]);

                            } else if (keyValue[0].equals("control_port")) {
                                controlPort = Integer.parseInt(keyValue[1]);

                            } else if (keyValue[0].equals("timing_port")) {
                                timingPort = Integer.parseInt(keyValue[1]);
                            }

                        }
                    }

                }
            }

            serverAddress.setDstPort(serverPort);
            controlAddress.setDstPort(controlPort);
            timingAddress.setDstPort(timingPort);

            // Launch the worker function in a separate thread.
            // Must launch timing thread before record message since record message
            // syncs with timing
            timingThread = new Thread(new Runnable() {
                public void run() {
                    timingWorkerFunction();
                }
            });
            timingThread.start();


            // give the timing thread some time to set itself up
            Thread.sleep(100);

            tcpOut.print(recordCommand);
            tcpOut.flush();
            while (!tcpIn.ready()) {
            }
            while (tcpIn.ready()) {
                String answer = tcpIn.readLine();
                System.out.println(answer);
            }




            // Launch the worker function in a separate thread.
            controlThread = new Thread(new Runnable() {
                public void run() {
                    controlWorkerFunction();
                }
            });
            controlThread.start();


            playbackFileIsDone.set(false);

            // wait for audio Data
            Thread.sleep(1000);

            // Launch the worker function in a separate thread.
            audioThread = new Thread(new Runnable() {
                public void run() {
                    audioWorkerFunction();
                }
            });
            audioThread.start();




            // playback has officially Started
            playbackStarted.set(true);

            // playback started
            playbackAboutToStart.set(true);

            // Set the volume to the current volume
            setVolume(currentVolume);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean stopPlayback() {

        if (playbackAboutToStop.compareAndSet(false, true) == false) {
            return false;
        }

        isDoneEnding.set(false);
        playbackState.set(false);
        if (playbackStarted.get() == false) {
            return false;
        }

        playbackStarted.set(false);
        didEnd.set(true);

        try {
            timingThread.join();
            audioThread.join();
            controlThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        isDoneEnding.set(true);


        String teardownCommand = "TEARDOWN " + connectionURL + " RTSP/1.0\r\n" +
                                 "CSeq: 32\r\n" +
                                 "Session: 1\r\n" +
                                 "User-Agent: iTunes/11.0.4 (Windows; N)\r\n\r\n";



        try {
            // Get in and out communication
            PrintWriter tcpOut = new PrintWriter(iHomeTCPConnection.getOutputStream(), true);
            BufferedReader tcpIn = new BufferedReader(new InputStreamReader(iHomeTCPConnection.getInputStream()));

            tcpOut.print(teardownCommand);
            tcpOut.flush();
            while (!tcpIn.ready()) {
            }
            while (tcpIn.ready()) {
                String answer = tcpIn.readLine();
                System.out.println(answer);
            }

            // close the connection
            iHomeTCPConnection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        playbackAboutToStop.set(false);

        return true;
    }


    public boolean getPlaybackState() {
        return playbackState.get();
    }

    public boolean setVolume(float _percent) {

        if (settingVolume.compareAndSet(false, true) == false) {
            return false;
        }

        // keep in range of percentage
        if (_percent < 0) {
            _percent = 0;
        } else if (_percent > 100) {
            _percent = 100;
        }

        // cant set the volume if there is no playback
        if (playbackStarted.get() == false) {
            return false;
        }

        // convert the volume from a percentage to a db
        float dbVolume = 0;
        if (_percent > 0) {

            dbVolume = ((float)(_percent / 100.0) * (float)(VOLUME_MAX_VALUE_DB - VOLUME_MIN_VALUE_DB)) + (float)VOLUME_MIN_VALUE_DB;

            // cap the volume to a level that the speaker supports
            if (dbVolume > VOLUME_MAX_VALUE_DB) {
                dbVolume = VOLUME_MAX_VALUE_DB;
            }
        }

        // construct the command
        String body = "volume: " + String.format("%f", dbVolume) + "\r\n";
        String volumeCommand = "SET_PARAMETER " + connectionURL + " RTSP/1.0\r\nCSeq: 4\r\nSession: 1\r\nContent-Type: text/parameters\r\nContent-Length: " + body.length() + "\r\nUser-Agent: iTunes/11.0.4 (Windows; N)\r\n\r\n" + body;



        try {
            // Get in and out communication
            PrintWriter tcpOut = new PrintWriter(iHomeTCPConnection.getOutputStream(), true);
            BufferedReader tcpIn = new BufferedReader(new InputStreamReader(iHomeTCPConnection.getInputStream()));

            // send and flush
            tcpOut.print(volumeCommand);
            tcpOut.flush();

            // Wait for data to come back
            while (!tcpIn.ready()) {
            }

            // read the data from the iHome
            while (tcpIn.ready()) {
                String answer = tcpIn.readLine();
                System.out.println(answer);
            }

            // update the current volume parameter
            currentVolume = _percent;
        } catch (Exception e) {
            e.printStackTrace();
        }

        settingVolume.set(false);

        return true;
    }

    public float getVolume() {

        while (settingVolume.get()) {
            // block until volume set is done
        }
        return currentVolume;
    }

    public void loadData(short[] _samples, int _offs, int _len) {

        short[] sample = new short[_len];
        int j = _offs;
        for (int i = 0; i < _len; i++, j++) {
            sample[i] = _samples[j];
        }
        synchronized (audioLinkedList) {
            audioLinkedList.addLast(sample);
        }
    }

    public void clearData() {
        synchronized (audioLinkedList) {
            audioLinkedList.clear();
        }
    }

    public int getPosition() {
        long pTime = 0;
        try {
            currentPlaybackTimeMutex.acquire();
            pTime = currentPlaybackTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentPlaybackTimeMutex.release();

        int mSecPos = (int)((pTime * 1000) / 44100);
        return mSecPos;
    }

    public void setPosition(int _mSec) {
        int sampleNumber = (_mSec * 44100) / 1000;

        try {
            desiredPlaybackTimeMutex.acquire();
            desiredPlaybackTime = sampleNumber;
        } catch (Exception e) {
            e.printStackTrace();
        }
        desiredPlaybackTimeMutex.release();
    }


    public void registerCallback(SpeakerSmartCallback _cb) {
        callbackList.add(_cb);
    }


    /*******************************************************************************************************************************************
    **
    **  Helper Methods
    **
    *******************************************************************************************************************************************/


    private void timingWorkerFunction() {
        try {
            IoTUDP timingUDP = new IoTUDP(timingAddress);

            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[32];

            while (didEnd.get() == false) {

                receiveData = timingUDP.recieveData(receiveData.length);

                long nanotime = nanoTime();
                int seconds = (int)((nanotime / 1000000000) & 0xffffffff);
                long fractions = ((( nanotime % 1000000000) * (0xffffffffL)) / 1000000000);

                sendData[0] = (byte)0x80;                   // Header bit field
                sendData[1] = (byte)0xd3;                   // mark bit and message payload number

                sendData[2] = (byte) 0x00;
                sendData[3] = (byte) 0x07;

                sendData[4] = (byte) 0x00;
                sendData[5] = (byte) 0x00;
                sendData[6] = (byte) 0x00;
                sendData[7] = (byte) 0x00;

                // origin time-stamp
                sendData[8] = receiveData[24];
                sendData[9] = receiveData[25];
                sendData[10] = receiveData[26];
                sendData[11] = receiveData[27];
                sendData[12] = receiveData[28];
                sendData[13] = receiveData[29];
                sendData[14] = receiveData[30];
                sendData[15] = receiveData[31];

                // arrival time-stamp
                sendData[16] = (byte)((seconds >> 24) & 0xff);
                sendData[17] = (byte)((seconds >> 16) & 0xff);
                sendData[18] = (byte)((seconds >> 8) & 0xff);
                sendData[19] = (byte)((seconds >> 0) & 0xff);
                sendData[20] = (byte)((fractions >> 24) & 0xff);
                sendData[21] = (byte)((fractions >> 16) & 0xff);
                sendData[22] = (byte)((fractions >> 8) & 0xff);
                sendData[23] = (byte)((fractions >> 0) & 0xff);


                nanotime = nanoTime();
                seconds = (int)( nanotime / 1000000000);
                fractions = ((( nanotime % 1000000000) * (0xffffffffL)) / 1000000000);

                // transmit time-stamp
                sendData[24] = (byte)((seconds >> 24) & 0xff);
                sendData[25] = (byte)((seconds >> 16) & 0xff);
                sendData[26] = (byte)((seconds >> 8) & 0xff);
                sendData[27] = (byte)((seconds >> 0) & 0xff);
                sendData[28] = (byte)((fractions >> 24) & 0xff);
                sendData[29] = (byte)((fractions >> 16) & 0xff);
                sendData[30] = (byte)((fractions >> 8) & 0xff);
                sendData[31] = (byte)((fractions >> 0) & 0xff);

                // Send the Data
                timingUDP.sendData(sendData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void controlWorkerFunction() {

        try {

            IoTUDP controlUDP = new IoTUDP(controlAddress);
            controlUDP.setSoTimeout(1);
            byte[] sendData = new byte[20];
            boolean first = true;


            while (didEnd.get() == false) {

                try {
                    byte[] receiveData = new byte[24];
                    receiveData = controlUDP.recieveData(receiveData.length);

                    // System.out.println("Control Packet Arrived");
                    // String packetData = bytesToHex(receiveData);
                    // System.out.println(packetData);

                } catch (Exception e) {
                    // e.printStackTrace();
                }


                long rtpTimestampCopy = rtpTimestamp;
                long nanotime = nanoTime();
                int seconds = (int)( nanotime / 1000000000);
                long fractions = (( nanotime % 1000000000) * (0xffffffffL)) / 1000000000;


                if (first) {
                    sendData[0] = (byte)0x90;                       // Header bit field
                    first = false;
                } else {
                    sendData[0] = (byte)0x80;                       // Header bit field
                }


                sendData[1] = (byte)0xd4;                   // mark bit and message payload number
                sendData[2] = (byte)0x00;
                sendData[3] = (byte)0x07;

                // time-stamp of packet
                sendData[4] = (byte)((rtpTimestampCopy >> 24) & 0xFF);
                sendData[5] = (byte)((rtpTimestampCopy >> 16) & 0xFF);
                sendData[6] = (byte)((rtpTimestampCopy >> 8) & 0xFF);
                sendData[7] = (byte)((rtpTimestampCopy >> 0) & 0xFF);

                // ntp time-stamp
                sendData[8] = (byte)((seconds >> 24) & 0xff);
                sendData[9] = (byte)((seconds >> 16) & 0xff);
                sendData[10] = (byte)((seconds >> 8) & 0xff);
                sendData[11] = (byte)((seconds >> 0) & 0xff);

                sendData[12] = (byte)((fractions >> 24) & 0xff);
                sendData[13] = (byte)((fractions >> 16) & 0xff);
                sendData[14] = (byte)((fractions >> 8) & 0xff);
                sendData[15] = (byte)((fractions >> 0) & 0xff);

                rtpTimestampCopy += 88200;
                sendData[16] = (byte)((rtpTimestampCopy >> 24) & 0xFF);
                sendData[17] = (byte)((rtpTimestampCopy >> 16) & 0xFF);
                sendData[18] = (byte)((rtpTimestampCopy >> 8) & 0xFF);
                sendData[19] = (byte)((rtpTimestampCopy >> 0) & 0xFF);

                // send the data
                controlUDP.sendData(sendData);

                // System.out.println("---------------------------------------------");
                // System.out.println("Sending Control Sync");
                // System.out.println("---------------------------------------------");

                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void audioWorkerFunction() {
        try {

            IoTUDP serverUDP = new IoTUDP(serverAddress);

            // current frame being played
            long frameCounter = 0;

            // used for bit packing for audio stream
            short[] array = null;
            int offset = 0;

            int noAudioCount = 0;

            while (didEnd.get() == false) {

                byte[] sendData = new byte[352 * 4 + 19];

                sendData[0] = (byte)0x80;

                if (frameCounter == 0) {
                    sendData[1] = (byte)0xe0;
                    // frameCounter = 1;
                } else {
                    sendData[1] = (byte)0x60;
                }

                sendData[2] = (byte)((sequenceNumber >> 8) & 0xFF);
                sendData[3] = (byte)((sequenceNumber >> 0) & 0xFF);

                long rtpTmp = rtpTimestamp;

                sendData[4] = (byte)((rtpTmp >> 24) & 0xFF);
                sendData[5] = (byte)((rtpTmp >> 16) & 0xFF);
                sendData[6] = (byte)((rtpTmp >> 8) & 0xFF);
                sendData[7] = (byte)((rtpTmp >> 0) & 0xFF);

                sendData[8] = (byte)((SEQUENCE_ID >> 24) & 0xFF);
                sendData[9] = (byte)((SEQUENCE_ID >> 16) & 0xFF);
                sendData[10] = (byte)((SEQUENCE_ID >> 8) & 0xFF);
                sendData[11] = (byte)((SEQUENCE_ID >> 0) & 0xFF);

                sendData[12] = (byte) 0x20;
                sendData[13] = (byte) 0x00;
                sendData[14] = (byte) 0x12;
                sendData[15] = (byte) 0x00;
                sendData[16] = (byte) 0x00;
                sendData[17] = (byte) 0x02;
                sendData[18] = (byte) 0xc0;

                for (int i = 19; i < sendData.length; i += 4) {
                    if (array != null && (offset + 1) >= array.length) {
                        array = null;
                    }

                    if (array == null) {
                        offset = 0;

                        synchronized (audioLinkedList) {
                            array = (short[])audioLinkedList.poll();
                        }
                    }

                    if (array != null) {

                        long time1 = 0;
                        long time2 = 0;

                        try {
                            desiredPlaybackTimeMutex.acquire();
                            time1 = desiredPlaybackTime;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        desiredPlaybackTimeMutex.release();


                        try {
                            currentPlaybackTimeMutex.acquire();
                            time2 = currentPlaybackTime;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        currentPlaybackTimeMutex.release();


                        while ((time2 < time1)) {
                            offset++;

                            try {
                                currentPlaybackTimeMutex.acquire();
                                currentPlaybackTime++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            currentPlaybackTimeMutex.release();


                            if ((offset + 1) >= array.length) {
                                offset = 0;
                                synchronized (audioLinkedList) {
                                    array = (short[])audioLinkedList.poll();
                                }

                                if (array == null) {
                                    break;
                                }
                            }
                        }
                    }

                    short l = 0;
                    short r = 0;

                    if (array != null) {
                        l = array[offset++];
                        r = array[offset++];

                        try {
                            currentPlaybackTimeMutex.acquire();
                            currentPlaybackTime++;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        currentPlaybackTimeMutex.release();


                        try {
                            desiredPlaybackTimeMutex.acquire();
                            desiredPlaybackTime++;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        desiredPlaybackTimeMutex.release();

                        noAudioCount = 0;
                    } else {
                        noAudioCount++;

                        if (noAudioCount > 10) {
                            noAudioCount = 0;
                            if (playbackFileIsDone.get() == false) {
                                playbackFileIsDone.set(true);
                            }
                        }
                    }

                    sendData[i - 1] |= (byte)((l >> 15) & 1);
                    sendData[i] = (byte)((l >> 7) & 0xff);
                    sendData[i + 1] = (byte)(((l << 1) & 0xfe) | ((r >> 15) & 1));
                    sendData[i + 2] = (byte)((r >> 7) & 0xff);
                    sendData[i + 3] = (byte)((r << 1) & 0xfe);
                }


                sequenceNumber++;
                sequenceNumber = sequenceNumber % SEQUENCE_NUMBER_WRAP_AROUND;
                rtpTimestamp += RTP_TIMESTAMP_INCREMENT_VALUE;

                frameCounter++;
                serverUDP.sendData(sendData);


                // need to sleep for a bit
                if ((frameCounter % 2) == 0) {
                    Thread.sleep(7);
                } else {
                    Thread.sleep(6);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void monitorThreadWorker() {
        while (driverIsShuttingDown.get() == false) {
            if (playbackFileIsDone.get()) {
                stopPlayback();
                playbackFileIsDone.set(false);

                for (SpeakerSmartCallback c : callbackList) {
                    try {
                        //c.speakerDone(this);
						c.speakerDone();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void endDriver() {
        stopPlayback();

        driverIsShuttingDown.set(true);
        try {
            monitorThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        didClose = true;
    }

    /**
     * close() called by the garbage collector right before trashing object
     */
    public void finalize() {
        if (!didClose) {
            endDriver();
        }
    }

    private static long nanoTime() {
        long nanotime = System.nanoTime();
        return nanotime;
    }
}




