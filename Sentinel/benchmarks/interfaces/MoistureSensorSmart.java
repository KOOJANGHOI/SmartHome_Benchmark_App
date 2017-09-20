package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface MoistureSensorSmart {

	public long getTimestampOfLastReading();
	public int getId();
	public void registerCallback(MoistureSensorCallback _callbackTo);
	public float getMoisture();
	public void setId(int id);
	public void init();
}
