#ifndef _DEVICESTATEINFO_HPP__
#define _DEVICESTATEINFO_HPP__
#include <iostream>

class DeviceStateInfo {
	private:
		int64_t time;
		int64_t upTime;
		int64_t downTime;

	public:

		DeviceStateInfo(int64_t _time, int64_t _upTime, int64_t _downTime) {

			time = _time;
			upTime = _upTime;
			downTime = _downTime;
		}


		~DeviceStateInfo() {
		}


		int64_t getTime() {
			return time;
		}


		int64_t getUpTime() {
			return upTime;
		}


		int64_t getDownTime() {
			return downTime;
		}
};
#endif
