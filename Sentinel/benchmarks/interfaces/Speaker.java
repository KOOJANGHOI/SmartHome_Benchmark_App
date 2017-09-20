package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface Speaker {
	public void init();
	public boolean startPlayback();
	public boolean stopPlayback();
	public boolean getPlaybackState();
	public boolean setVolume(float _percent);
	public float getVolume();
	public int getPosition();
	public void setPosition(int _mSec);
	public void loadData(short _samples[], int _offs, int _len);
	public void clearData();
	public void registerCallback(SpeakerSmartCallback _cb);
}
