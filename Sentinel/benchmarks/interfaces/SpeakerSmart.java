package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface SpeakerSmart {

	public int getPosition();
	public boolean stopPlayback();
	public void clearData();
	public boolean startPlayback();
	public boolean getPlaybackState();
	public boolean setVolume(float _percent);
	public float getVolume();
	public void setPosition(int _mSec);
	public void loadData(short _samples[], int _offs, int _len);
	public void init();
	public void registerCallback(SpeakerCallback _cb);
}
