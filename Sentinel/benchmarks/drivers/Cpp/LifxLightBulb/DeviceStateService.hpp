#ifndef _DEVICESTATESERVICE_HPP__
#define _DEVICESTATESERVICE_HPP__
#include <iostream>

class DeviceStateService {
	private:
		int service;
		int64_t port;

	public:

		DeviceStateService(int _service, long _port) {

			service = _service;
			port = _port;
		}


		~DeviceStateService() {
		}


		int getService() {
			return service;
		}


		int64_t getPort() {
			return port;
		}
};
#endif
