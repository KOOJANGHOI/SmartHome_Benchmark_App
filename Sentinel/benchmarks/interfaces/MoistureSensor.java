package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface MoistureSensor {
	public void init();
	public float getMoisture();
	public long getTimestampOfLastReading();
	public void setId(int id);
	public int getId();
	public void registerCallback(MoistureSensorSmartCallback _callbackTo);
}
