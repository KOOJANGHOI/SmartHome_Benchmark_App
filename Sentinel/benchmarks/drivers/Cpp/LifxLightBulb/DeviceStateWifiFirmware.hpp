#ifndef _DEVICESTATEWIFIFIRMWARE_HPP__
#define _DEVICESTATEWIFIFIRMWARE_HPP__
#include <iostream>

class DeviceStateWifiFirmware {
	// time of build in nanosecond accuracy
	// after some tests
	private:
		int64_t build;
		int64_t version;	// firmware version

	public:

		DeviceStateWifiFirmware(int64_t _build, int64_t _version) {

			build = _build;
			version = _version;
		}


		~DeviceStateWifiFirmware() {
		}


		int64_t getBuild() {
			return build;
		}


		int64_t getVersion() {
			return version;
		}
};
#endif
