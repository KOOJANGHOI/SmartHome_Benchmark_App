package iotcode.LifxLightBulb;

public class DeviceStateLocation {
	byte[] location = new byte[16];
	final String label;
	final long updatedAt;

	public DeviceStateLocation(byte[] _location, String _label, long _updatedAt) {
		location = _location;
		label = _label;
		updatedAt = _updatedAt;
	}

	public byte[] getLocation() {
		return location;
	}

	public String getLabel() {
		return label;
	}

	public long getUpdatedAt() {
		return updatedAt;
	}
}
