package SpeakerController;

import javazoom.jl.player.advanced.*;
import javazoom.jl.player.*;

// Standard Java Packages
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.LinkedList;
import java.io.FileInputStream;

/** Class MP3Decoder for the smart home application benchmark
 *  <p>
 *  This class decodes mp3 files into raw pcm data
 *
 * @author      Ali Younis <ayounis @ uci.edu>
 * @version     1.0                
 * @since       2016-05-01
 */
public class MP3Decoder extends AudioDeviceBase {

    private class PlaybackList extends PlaybackListener {
        private MP3Decoder decoder;
        public PlaybackList(MP3Decoder _d) {
            decoder = _d;
        }

        public void playbackFinished(PlaybackEvent evt) {
            decoder.decodeDone();
        }

        public void playbackStarted(PlaybackEvent evt) {
            // do nothing
        }
    }

    private String fileName = "";
    private LinkedList<short[]> audioLinkedList = new LinkedList<short[]>();
    private AtomicBoolean decodeDone = new AtomicBoolean();
    private long dataLength = 0;

    public MP3Decoder(String _fileName) {
        fileName = _fileName;
        decodeDone.set(false);

        PlaybackList pl = new PlaybackList(this);

        try {
            AdvancedPlayer ap = new AdvancedPlayer(new FileInputStream(fileName), this);
            ap.setPlayBackListener(pl);
            ap.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<short[]> getDecodedFrames() {

        while (decodeDone.get() == false) {
            // just block until done
        }

        return audioLinkedList;
    }

    public long getAudioFrameLength() {
        // stereo
        return dataLength / 2;
    }

    protected void decodeDone() {
        decodeDone.set(true);
    }

    public int getPosition() {
        // not used, just needed for AdvancedPlayer to work.
        return 0;
    }

    protected void writeImpl(short[] _samples, int _offs, int _len) {
        short[] sample = new short[_len];
        int j = _offs;
        for (int i = 0; i < _len; i++, j++) {
            sample[i] = _samples[j];
        }
        synchronized (audioLinkedList) {
            audioLinkedList.addLast(sample);
        }

        dataLength += (long)_len;
    }

}










