#ifndef _DEVICESTATEHOSTINFO_HPP__
#define _DEVICESTATEHOSTINFO_HPP__
#include <iostream>

class DeviceStateHostInfo {
	private:
		int64_t signal;
		int64_t tx;
		int64_t rx;

	public:

		DeviceStateHostInfo(int64_t _signal, int64_t _tx, int64_t _rx) {

			signal = _signal;
			tx = _tx;
			rx = _rx;
		}


		~DeviceStateHostInfo() {
		}


		int64_t getSignal() {
			return signal;
		}


		int64_t getTx() {
			return tx;
		}


		int64_t getRx() {
			return rx;
		}
};
#endif
