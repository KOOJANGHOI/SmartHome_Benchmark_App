package iotcode.interfaces;

import java.util.List;
import java.util.ArrayList;

public interface AlarmSmart {

	public boolean doesHaveZoneTimers();
	public List<ZoneState> getZoneStates();
	public void init();
	public void setZone(int _zone, boolean _onOff, int _onDurationSeconds);
	public int getNumberOfZones();
}
