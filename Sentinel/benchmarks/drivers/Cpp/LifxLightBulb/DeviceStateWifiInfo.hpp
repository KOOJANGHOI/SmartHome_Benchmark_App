#ifndef _DEVICESTATEWIFIINFO_HPP__
#define _DEVICESTATEWIFIINFO_HPP__
#include <iostream>

class DeviceStateWifiInfo {
	private:
		int64_t signal;
		int64_t tx;
		int64_t rx;

	public:

		DeviceStateWifiInfo(int64_t _signal, int64_t _tx, int64_t _rx) {

			signal = _signal;
			tx = _tx;
			rx = _rx;
		}


		~DeviceStateWifiInfo() {
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
