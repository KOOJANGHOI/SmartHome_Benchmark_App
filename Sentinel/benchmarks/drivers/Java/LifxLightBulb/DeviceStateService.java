package iotcode.LifxLightBulb;

public class DeviceStateService {
	private final int service;
	private final long port;

	public DeviceStateService(int _service, long _port) {
		service = _service;
		port = _port;
	}

	public int getService() {
		return service;
	}

	public long getPort() {
		return port;
	}
}
