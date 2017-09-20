package iotcode.LifxLightBulb;

public class DeviceStateGroup {
	byte[] group = new byte[16];
	final String label;
	final long updatedAt;

	public DeviceStateGroup(byte[] _location, String _label, long _updatedAt) {
		group = _location;
		label = _label;
		updatedAt = _updatedAt;
	}

	public byte[] getGroup() {
		return group;
	}

	public String getLabel() {
		return label;
	}

	public long getUpdatedAt() {
		return updatedAt;
	}
}
