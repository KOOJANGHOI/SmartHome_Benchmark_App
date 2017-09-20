#ifndef _DEVICESTATELOCATION_HPP__
#define _DEVICESTATELOCATION_HPP__
#include <iostream>
#include <string>

class DeviceStateLocation {
	private:
		char location[16];
		string label;
		int64_t updatedAt;

	public:

		DeviceStateLocation(char _location[16], string _label, long _updatedAt) {

			strcpy(location, _location);
			label = _label;
			updatedAt = _updatedAt;
		}


		~DeviceStateLocation() {
		}


		char* getLocation() {
			return location;
		}


		string getLabel() {
			return label;
		}


		int64_t getUpdatedAt() {
			return updatedAt;
		}
};
#endif
