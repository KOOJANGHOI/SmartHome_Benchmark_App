#ifndef _DEVICESTATEGROUP_HPP__
#define _DEVICESTATEGROUP_HPP__
#include <iostream>
#include <string>

class DeviceStateGroup {

	private:
		char group[16];
		string label;
		int64_t updatedAt;

	public:

		DeviceStateGroup(char _location[16], string _label, int64_t _updatedAt) {

			strcpy(group, _location);
			label = _label;
			updatedAt = _updatedAt;
		}


		~DeviceStateGroup() {
		}


		char* getGroup() {
			return group;
		}


		string getLabel() {
			return label;
		}


		int64_t getUpdatedAt() {
			return updatedAt;
		}
};
#endif
