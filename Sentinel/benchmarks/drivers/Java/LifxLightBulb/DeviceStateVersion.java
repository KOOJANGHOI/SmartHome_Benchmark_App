package iotcode.LifxLightBulb;

public class DeviceStateVersion {
	final long vender;
	final long product;
	final long version;

	public DeviceStateVersion(long _vender, long _product, long _version) {
		vender = _vender;
		product = _product;
		version = _version;
	}

	public long getVender() {
		return vender;
	}

	public long getProduct() {
		return product;
	}

	public long getVersion() {
		return version;
	}
}
