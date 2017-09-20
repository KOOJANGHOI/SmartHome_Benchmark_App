package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface CameraSmart {

	public int getMaxFPS();
	public boolean setFPS(int _fps);
	public int getMinFPS();
	public boolean setResolution(Resolution _res);
	public void stop();
	public void start();
	public long getTimestamp();
	public byte[] getLatestFrame();
	public void init();
	public void registerCallback(CameraCallback _callbackTo);
	public List<Resolution> getSupportedResolutions();
}
