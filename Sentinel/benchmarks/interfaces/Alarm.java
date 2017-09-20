package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface Alarm {
	public void init();
	public void setZone(int _zone, boolean _onOff, int _onDurationSeconds);
	public List<ZoneState> getZoneStates();
	public int getNumberOfZones();
	public boolean doesHaveZoneTimers();
}
