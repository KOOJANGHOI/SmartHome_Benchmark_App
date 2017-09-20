package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface SmartthingsSensorSmart {

	public long getTimestampOfLastReading();
	public boolean isActiveValue();
	public int getId();
	public void registerCallback(SmartthingsSensorCallback _callbackTo);
	public int getValue();
	public void setId(int id);
	public void init();
}
