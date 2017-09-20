#include "LifxLightBulb.cpp"
#include "LightBulb_Skeleton.cpp"
#include "LightBulbTest_Stub.cpp"
#include "IoTSet.hpp"


//typedef void* create_t(string className, void** params);
//typedef void destroy_t(void*);


// Transferring members of IoTSet<void*> into IoTSet<IoTDeviceAddress*>
IoTSet<IoTDeviceAddress*>* createDeviceAddressSet(unordered_set<void*>* iotSet) {

	unordered_set<IoTDeviceAddress*>* devSet = new unordered_set<IoTDeviceAddress*>();
	//for (auto itr = iotSet->begin(); itr != iotSet->end(); ++itr) {
	for (unordered_set<void*>::const_iterator itr = iotSet->begin(); itr != iotSet->end(); ++itr) {
		IoTDeviceAddress* deviceAddress = (IoTDeviceAddress*) *itr;
		devSet->insert(deviceAddress);
	}
	IoTSet<IoTDeviceAddress*>* iotDevSet = new IoTSet<IoTDeviceAddress*>(devSet);

	delete iotSet;
	return iotDevSet;
}


/*
// External creator/destroyer
extern "C" void* create(string className, void** params) {

	if (className.compare("LifxLightBulb") == 0) {
		// Arguments: IoTSet<IoTDeviceAddress*>* _devAddress, string macAddress
		// We pass in a pointer to string and then we pass in just the value for the class
		return new LifxLightBulb((IoTSet<IoTDeviceAddress*>*) params[0], *((string*) params[1]));
	} else if (className.compare("LightBulb_Skeleton") == 0) {
		// Arguments: LightBulb *_mainObj, string _callbackAddress, int _port
		// We pass in pointers to string and integer, and read the values again
		return new LightBulb_Skeleton((LightBulb*) params[0], *((string*) params[1]), *((int*) params[2]));
	} else if (className.compare("LightBulbTest_Stub") == 0) {
		// int _port, const char* _skeletonAddress, string _callbackAddress, int _rev, bool* _bResult, vector<int> _ports
		// We pass in pointers to string and integer, and read the values again
		return new LightBulbTest_Stub(*((int*) params[0]), (const char*) params[1], *((string*) params[2]), *((int*) params[3]), 
				(bool*) params[4], *((vector<int>*) params[5]));
	} else {	// Class is not recognized
		cerr << "ObjectFactory: Class is not recognized: " << className << endl;
		exit(1);
	}
}

extern "C" void destroy(string className, void* ob) {

	if (ob != NULL) {	// Check that this pointer is not NULL

		if (className.compare("LifxLightBulb") == 0) {
			LifxLightBulb* obj = (LifxLightBulb*) ob;
			delete obj;
		} else if (className.compare("LightBulb_Skeleton") == 0) {
			LightBulb_Skeleton* obj = (LightBulb_Skeleton*) ob;
			delete obj;
		} else if (className.compare("LightBulbTest_Stub") == 0) {
			LightBulbTest_Stub* obj = (LightBulbTest_Stub*) ob;
			delete obj;
		} else {	// Class is not recognized
			cerr << "ObjectFactory: Class is not recognized: " << className << endl;
			exit(1);
		}
	}
}
*/

