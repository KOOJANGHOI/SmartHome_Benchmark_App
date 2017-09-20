package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface SmartthingsSensor {
	public void init();
	public int getValue();
	public boolean isActiveValue();
	public long getTimestampOfLastReading();
	public void setId(int id);
	public int getId();
	public void registerCallback(SmartthingsSensorSmartCallback _callbackTo);
}
