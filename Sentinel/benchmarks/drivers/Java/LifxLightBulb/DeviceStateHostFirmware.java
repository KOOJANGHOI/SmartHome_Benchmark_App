package iotcode.LifxLightBulb;

public class DeviceStateHostFirmware {
	// time of build in nanosecond accuracy
	// after some tests
	final long build;
	final long version;																																										// firmware version

	public DeviceStateHostFirmware(long _build, long _version) {
		build = _build;
		version = _version;
	}

	public long getBuild() {
		return build;
	}

	public long getVersion() {
		return version;
	}
}
