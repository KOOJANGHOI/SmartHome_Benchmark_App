#include <iostream>
#include "LightBulbTest_Stub.hpp"

using namespace std;

LightBulbTest_Stub::LightBulbTest_Stub(int _portSend, int _portRecv, const char* _skeletonAddress, int _rev, bool* _bResult) {
	rmiComm = new IoTRMICommClient(_portSend, _portRecv, _skeletonAddress, _rev, _bResult);
	rmiComm->registerStub(objectId, 6, &retValueReceived6);
	rmiComm->registerStub(objectId, 3, &retValueReceived3);
	rmiComm->registerStub(objectId, 8, &retValueReceived8);
	rmiComm->registerStub(objectId, 7, &retValueReceived7);
	rmiComm->registerStub(objectId, 9, &retValueReceived9);
	IoTRMIUtil::mapStub->insert(make_pair(objectId, this));
}

LightBulbTest_Stub::LightBulbTest_Stub(IoTRMIComm* _rmiComm, int _objectId) {
	rmiComm = _rmiComm;
	objectId = _objectId;
	rmiComm->registerStub(objectId, 6, &retValueReceived6);
	rmiComm->registerStub(objectId, 3, &retValueReceived3);
	rmiComm->registerStub(objectId, 8, &retValueReceived8);
	rmiComm->registerStub(objectId, 7, &retValueReceived7);
	rmiComm->registerStub(objectId, 9, &retValueReceived9);
}

LightBulbTest_Stub::~LightBulbTest_Stub() {
	if (rmiComm != NULL) {
		delete rmiComm;
		rmiComm = NULL;
	}
}

mutex mtxLightBulbTest_StubMethodExec2;
void LightBulbTest_Stub::turnOn() { 
	lock_guard<mutex> guard(mtxLightBulbTest_StubMethodExec2);
	int methodId = 2;
	string retType = "void";
	int numParam = 0;
	string paramCls[] = {  };
	void* paramObj[] = {  };
	rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);
}

mutex mtxLightBulbTest_StubMethodExec6;
double LightBulbTest_Stub::getBrightness() { 
	lock_guard<mutex> guard(mtxLightBulbTest_StubMethodExec6);
	int methodId = 6;
	string retType = "double";
	int numParam = 0;
	string paramCls[] = {  };
	void* paramObj[] = {  };
	double retVal = 0;
	void* retObj = &retVal;
	rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);
	// Waiting for return value
	while (!retValueReceived6);
	rmiComm->getReturnValue(retType, retObj);
	retValueReceived6 = false;
	didGetReturnBytes.exchange(true);

	return retVal;
}

mutex mtxLightBulbTest_StubMethodExec1;
void LightBulbTest_Stub::turnOff() { 
	lock_guard<mutex> guard(mtxLightBulbTest_StubMethodExec1);
	int methodId = 1;
	string retType = "void";
	int numParam = 0;
	string paramCls[] = {  };
	void* paramObj[] = {  };
	rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);
}

mutex mtxLightBulbTest_StubMethodExec3;
bool LightBulbTest_Stub::getState() { 
	lock_guard<mutex> guard(mtxLightBulbTest_StubMethodExec3);
	int methodId = 3;
	string retType = "boolean";
	int numParam = 0;
	string paramCls[] = {  };
	void* paramObj[] = {  };
	bool retVal = false;
	void* retObj = &retVal;
	rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);
	// Waiting for return value
	while (!retValueReceived3);
	rmiComm->getReturnValue(retType, retObj);
	retValueReceived3 = false;
	didGetReturnBytes.exchange(true);

	return retVal;
}

mutex mtxLightBulbTest_StubMethodExec4;
void LightBulbTest_Stub::setColor(double _hue, double _saturation, double _brightness) { 
	lock_guard<mutex> guard(mtxLightBulbTest_StubMethodExec4);
	int methodId = 4;
	string retType = "void";
	int numParam = 3;
	string paramCls[] = { "double", "double", "double" };
	void* paramObj[] = { &_hue, &_saturation, &_brightness };
	rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);
}

mutex mtxLightBulbTest_StubMethodExec8;
double LightBulbTest_Stub::getSaturation() { 
	lock_guard<mutex> guard(mtxLightBulbTest_StubMethodExec8);
	int methodId = 8;
	string retType = "double";
	int numParam = 0;
	string paramCls[] = {  };
	void* paramObj[] = {  };
	double retVal = 0;
	void* retObj = &retVal;
	rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);
	// Waiting for return value
	while (!retValueReceived8);
	rmiComm->getReturnValue(retType, retObj);
	retValueReceived8 = false;
	didGetReturnBytes.exchange(true);

	return retVal;
}

mutex mtxLightBulbTest_StubMethodExec0;
void LightBulbTest_Stub::init() { 
	lock_guard<mutex> guard(mtxLightBulbTest_StubMethodExec0);
	int methodId = 0;
	string retType = "void";
	int numParam = 0;
	string paramCls[] = {  };
	void* paramObj[] = {  };
	rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);
}

mutex mtxLightBulbTest_StubMethodExec5;
void LightBulbTest_Stub::setTemperature(int _temperature) { 
	lock_guard<mutex> guard(mtxLightBulbTest_StubMethodExec5);
	int methodId = 5;
	string retType = "void";
	int numParam = 1;
	string paramCls[] = { "int" };
	void* paramObj[] = { &_temperature };
	rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);
}

mutex mtxLightBulbTest_StubMethodExec7;
double LightBulbTest_Stub::getHue() { 
	lock_guard<mutex> guard(mtxLightBulbTest_StubMethodExec7);
	int methodId = 7;
	string retType = "double";
	int numParam = 0;
	string paramCls[] = {  };
	void* paramObj[] = {  };
	double retVal = 0;
	void* retObj = &retVal;
	rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);
	// Waiting for return value
	while (!retValueReceived7);
	rmiComm->getReturnValue(retType, retObj);
	retValueReceived7 = false;
	didGetReturnBytes.exchange(true);

	return retVal;
}

mutex mtxLightBulbTest_StubMethodExec9;
int LightBulbTest_Stub::getTemperature() { 
	lock_guard<mutex> guard(mtxLightBulbTest_StubMethodExec9);
	int methodId = 9;
	string retType = "int";
	int numParam = 0;
	string paramCls[] = {  };
	void* paramObj[] = {  };
	int retVal = 0;
	void* retObj = &retVal;
	rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);
	// Waiting for return value
	while (!retValueReceived9);
	rmiComm->getReturnValue(retType, retObj);
	retValueReceived9 = false;
	didGetReturnBytes.exchange(true);

	return retVal;
}

extern "C" void* createLightBulbTest_Stub(void** params) {
	// Args: int _portSend, int _portRecv, const char* _skeletonAddress, int _rev, bool* _bResult
	return new LightBulbTest_Stub(*((int*) params[0]), *((int*) params[1]), ((string*) params[2])->c_str(), *((int*) params[3]), (bool*) params[4]);
}

extern "C" void destroyLightBulbTest_Stub(void* t) {
	LightBulbTest_Stub* obj = (LightBulbTest_Stub*) t;
	delete obj;
}

extern "C" void initLightBulbTest_Stub(void* t) {
}

int main() {
	return 0;
}
