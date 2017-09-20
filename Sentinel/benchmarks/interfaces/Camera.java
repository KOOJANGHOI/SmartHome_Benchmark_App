package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface Camera {
	public void init();
	public void start();
	public void stop();
	public byte[] getLatestFrame();
	public long getTimestamp();
	public List<Resolution> getSupportedResolutions();
	public boolean setResolution(Resolution _res);
	public boolean setFPS(int _fps);
	public int getMaxFPS();
	public int getMinFPS();
	public void registerCallback(CameraSmartCallback _callbackTo);
}
