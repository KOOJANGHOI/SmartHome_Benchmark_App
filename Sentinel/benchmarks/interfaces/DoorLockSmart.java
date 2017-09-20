
package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface DoorLockSmart {

	public long getTimestampOfLastReading();
	public void UnlockDoor();
	public boolean isActiveValue();
	public int getId();
	public void registerCallback(DoorLockCallback _callbackTo);
	public void StatusRequest();
	public int getValue();
	public void LockDoor();
	public void setId(int id);
	public void init();
}
