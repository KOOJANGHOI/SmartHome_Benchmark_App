package iotcode.LifxLightBulb;

public class DeviceStateWifiInfo {
	final long signal;
	final long tx;
	final long rx;

	public DeviceStateWifiInfo(long _signal, long _tx, long _rx) {
		signal = _signal;
		tx = _tx;
		rx = _rx;
	}

	public long getSignal() {
		return signal;
	}

	public long getTx() {
		return tx;
	}

	public long getRx() {
		return rx;
	}
}
