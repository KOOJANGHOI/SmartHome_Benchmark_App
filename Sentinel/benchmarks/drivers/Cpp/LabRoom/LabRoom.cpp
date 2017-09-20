#include <iostream>
#include "LabRoom.hpp"

using namespace std;

// External functions to create, destroy and initialize this class object
extern "C" void* createLabRoom(void** params) {
	// Arguments: IoTSet<IoTDeviceAddress*>* _devAddress, string macAddress
	return new LabRoom();
}


extern "C" void destroyLabRoom(void* t) {
	LabRoom* lr = (LabRoom*) t;
	delete lr;
}


extern "C" void initLabRoom(void* t) {
	// TODO: We actually need init() in LabRoom class
	// But, this is declared here just for the sake of consistency for Sentinel
	// In this case, we need the symbol "init" when loading object handlers with .so files
	//LabRoom* lr = (LabRoom*) t;
	//lr->init();
}


// Constructor
LabRoom::LabRoom() {

}

LabRoom::~LabRoom() {

}

int LabRoom::getRoomID() {

	return 0;
}
