package iotcode.LifxLightBulb;

public class DeviceStateInfo {
	// all values are in nanoseconds
	private final long time;
	private final long upTime;
	private final long downTime;

	public DeviceStateInfo(long _time, long _upTime, long _downTime) {
		time = _time;
		upTime = _upTime;
		downTime = _downTime;
	}

	public long getTime() {
		return time;
	}

	public long getUpTime() {
		return upTime;
	}

	public long getDownTime() {
		return downTime;
	}
}
