
package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface DoorLock {
	public void UnlockDoor();
	public void LockDoor();
	public void StatusRequest();
	public void init();
	public int getValue();
	public boolean isActiveValue();
	public long getTimestampOfLastReading();
	public void setId(int id);
	public int getId();
	public void registerCallback(DoorLockCallbackSmart _callbackTo);
}
