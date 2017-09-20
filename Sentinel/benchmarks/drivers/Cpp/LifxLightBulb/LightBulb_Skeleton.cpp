#include <iostream>
#include "LightBulb_Skeleton.hpp"

using namespace std;

LightBulb_Skeleton::LightBulb_Skeleton(LightBulb *_mainObj, int _portSend, int _portRecv) {
	bool _bResult = false;
	mainObj = _mainObj;
	rmiComm = new IoTRMICommServer(_portSend, _portRecv, &_bResult);
	IoTRMIUtil::mapSkel->insert(make_pair(_mainObj, this));
	IoTRMIUtil::mapSkelId->insert(make_pair(_mainObj, objectId));
	rmiComm->registerSkeleton(objectId, &methodReceived);
	thread th1 (&LightBulb_Skeleton::___waitRequestInvokeMethod, this, this);
	th1.join();
}

LightBulb_Skeleton::LightBulb_Skeleton(LightBulb *_mainObj, IoTRMIComm *_rmiComm, int _objectId) {
	bool _bResult = false;
	mainObj = _mainObj;
	rmiComm = _rmiComm;
	objectId = _objectId;
	rmiComm->registerSkeleton(objectId, &methodReceived);
}

LightBulb_Skeleton::~LightBulb_Skeleton() {
	if (rmiComm != NULL) {
		delete rmiComm;
		rmiComm = NULL;
	}
}

bool LightBulb_Skeleton::didInitWaitInvoke() {
	return didAlreadyInitWaitInvoke;
}

void LightBulb_Skeleton::init() {
	mainObj->init();
}

void LightBulb_Skeleton::turnOff() {
	mainObj->turnOff();
}

void LightBulb_Skeleton::turnOn() {
	mainObj->turnOn();
}

bool LightBulb_Skeleton::getState() {
	return mainObj->getState();
}

void LightBulb_Skeleton::setColor(double _hue, double _saturation, double _brightness) {
	mainObj->setColor(_hue, _saturation, _brightness);
}

void LightBulb_Skeleton::setTemperature(int _temperature) {
	mainObj->setTemperature(_temperature);
}

double LightBulb_Skeleton::getBrightness() {
	return mainObj->getBrightness();
}

double LightBulb_Skeleton::getHue() {
	return mainObj->getHue();
}

double LightBulb_Skeleton::getSaturation() {
	return mainObj->getSaturation();
}

int LightBulb_Skeleton::getTemperature() {
	return mainObj->getTemperature();
}

double LightBulb_Skeleton::getBrightnessRangeLowerBound() {
	return mainObj->getBrightnessRangeLowerBound();
}

double LightBulb_Skeleton::getBrightnessRangeUpperBound() {
	return mainObj->getBrightnessRangeUpperBound();
}

double LightBulb_Skeleton::getHueRangeLowerBound() {
	return mainObj->getHueRangeLowerBound();
}

double LightBulb_Skeleton::getHueRangeUpperBound() {
	return mainObj->getHueRangeUpperBound();
}

double LightBulb_Skeleton::getSaturationRangeLowerBound() {
	return mainObj->getSaturationRangeLowerBound();
}

double LightBulb_Skeleton::getSaturationRangeUpperBound() {
	return mainObj->getSaturationRangeUpperBound();
}

int LightBulb_Skeleton::getTemperatureRangeLowerBound() {
	return mainObj->getTemperatureRangeLowerBound();
}

int LightBulb_Skeleton::getTemperatureRangeUpperBound() {
	return mainObj->getTemperatureRangeUpperBound();
}

void LightBulb_Skeleton::___init(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	init();
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___turnOff(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	turnOff();
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___turnOn(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	turnOn();
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___getState(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	bool retVal = getState();
	void* retObj = &retVal;
	rmiComm->sendReturnObj(retObj, "boolean", localMethodBytes);
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___setColor(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = { "double", "double", "double" };
	int numParam = 3;
	double _hue;
	double _saturation;
	double _brightness;
	void* paramObj[] = { &_hue, &_saturation, &_brightness };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	setColor(_hue, _saturation, _brightness);
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___setTemperature(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = { "int" };
	int numParam = 1;
	int _temperature;
	void* paramObj[] = { &_temperature };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	setTemperature(_temperature);
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___getBrightness(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	double retVal = getBrightness();
	void* retObj = &retVal;
	rmiComm->sendReturnObj(retObj, "double", localMethodBytes);
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___getHue(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	double retVal = getHue();
	void* retObj = &retVal;
	rmiComm->sendReturnObj(retObj, "double", localMethodBytes);
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___getSaturation(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	double retVal = getSaturation();
	void* retObj = &retVal;
	rmiComm->sendReturnObj(retObj, "double", localMethodBytes);
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___getTemperature(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	int retVal = getTemperature();
	void* retObj = &retVal;
	rmiComm->sendReturnObj(retObj, "int", localMethodBytes);
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___getBrightnessRangeLowerBound(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	double retVal = getBrightnessRangeLowerBound();
	void* retObj = &retVal;
	rmiComm->sendReturnObj(retObj, "double", localMethodBytes);
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___getBrightnessRangeUpperBound(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	double retVal = getBrightnessRangeUpperBound();
	void* retObj = &retVal;
	rmiComm->sendReturnObj(retObj, "double", localMethodBytes);
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___getHueRangeLowerBound(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	double retVal = getHueRangeLowerBound();
	void* retObj = &retVal;
	rmiComm->sendReturnObj(retObj, "double", localMethodBytes);
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___getHueRangeUpperBound(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	double retVal = getHueRangeUpperBound();
	void* retObj = &retVal;
	rmiComm->sendReturnObj(retObj, "double", localMethodBytes);
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___getSaturationRangeLowerBound(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	double retVal = getSaturationRangeLowerBound();
	void* retObj = &retVal;
	rmiComm->sendReturnObj(retObj, "double", localMethodBytes);
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___getSaturationRangeUpperBound(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	double retVal = getSaturationRangeUpperBound();
	void* retObj = &retVal;
	rmiComm->sendReturnObj(retObj, "double", localMethodBytes);
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___getTemperatureRangeLowerBound(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	int retVal = getTemperatureRangeLowerBound();
	void* retObj = &retVal;
	rmiComm->sendReturnObj(retObj, "int", localMethodBytes);
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___getTemperatureRangeUpperBound(LightBulb_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	int retVal = getTemperatureRangeUpperBound();
	void* retObj = &retVal;
	rmiComm->sendReturnObj(retObj, "int", localMethodBytes);
	delete[] localMethodBytes;
}

void LightBulb_Skeleton::___waitRequestInvokeMethod(LightBulb_Skeleton* skel) {
	skel->didAlreadyInitWaitInvoke = true;
	while (true) {
		if (!methodReceived) {
			continue;
		}
		skel->methodBytes = skel->rmiComm->getMethodBytes();
		skel->methodLen = skel->rmiComm->getMethodLength();
		methodReceived = false;
		int _objectId = skel->rmiComm->getObjectId(skel->methodBytes);
		int methodId = skel->rmiComm->getMethodId(skel->methodBytes);
		if (_objectId == objectId) {
			if (set0Allowed.find(methodId) == set0Allowed.end()) {
				cerr << "Object with object Id: " << _objectId << "  is not allowed to access method: " << methodId << endl;
				return;
			}
		}
		else {
			continue;
		}
		switch (methodId) {
			case 0: {
				thread th0 (&LightBulb_Skeleton::___init, std::ref(skel), skel);
				th0.detach(); break;
			}
			case 1: {
				thread th1 (&LightBulb_Skeleton::___turnOff, std::ref(skel), skel);
				th1.detach(); break;
			}
			case 2: {
				thread th2 (&LightBulb_Skeleton::___turnOn, std::ref(skel), skel);
				th2.detach(); break;
			}
			case 3: {
				thread th3 (&LightBulb_Skeleton::___getState, std::ref(skel), skel);
				th3.detach(); break;
			}
			case 4: {
				thread th4 (&LightBulb_Skeleton::___setColor, std::ref(skel), skel);
				th4.detach(); break;
			}
			case 5: {
				thread th5 (&LightBulb_Skeleton::___setTemperature, std::ref(skel), skel);
				th5.detach(); break;
			}
			case 6: {
				thread th6 (&LightBulb_Skeleton::___getBrightness, std::ref(skel), skel);
				th6.detach(); break;
			}
			case 7: {
				thread th7 (&LightBulb_Skeleton::___getHue, std::ref(skel), skel);
				th7.detach(); break;
			}
			case 8: {
				thread th8 (&LightBulb_Skeleton::___getSaturation, std::ref(skel), skel);
				th8.detach(); break;
			}
			case 9: {
				thread th9 (&LightBulb_Skeleton::___getTemperature, std::ref(skel), skel);
				th9.detach(); break;
			}
			case 10: {
				thread th10 (&LightBulb_Skeleton::___getBrightnessRangeLowerBound, std::ref(skel), skel);
				th10.detach(); break;
			}
			case 11: {
				thread th11 (&LightBulb_Skeleton::___getBrightnessRangeUpperBound, std::ref(skel), skel);
				th11.detach(); break;
			}
			case 12: {
				thread th12 (&LightBulb_Skeleton::___getHueRangeLowerBound, std::ref(skel), skel);
				th12.detach(); break;
			}
			case 13: {
				thread th13 (&LightBulb_Skeleton::___getHueRangeUpperBound, std::ref(skel), skel);
				th13.detach(); break;
			}
			case 14: {
				thread th14 (&LightBulb_Skeleton::___getSaturationRangeLowerBound, std::ref(skel), skel);
				th14.detach(); break;
			}
			case 15: {
				thread th15 (&LightBulb_Skeleton::___getSaturationRangeUpperBound, std::ref(skel), skel);
				th15.detach(); break;
			}
			case 16: {
				thread th16 (&LightBulb_Skeleton::___getTemperatureRangeLowerBound, std::ref(skel), skel);
				th16.detach(); break;
			}
			case 17: {
				thread th17 (&LightBulb_Skeleton::___getTemperatureRangeUpperBound, std::ref(skel), skel);
				th17.detach(); break;
			}
			default: 
			cerr << "Method Id " << methodId << " not recognized!" << endl;
			return;
		}
	}
}

extern "C" void* createLightBulb_Skeleton(void** params) {
	// Args: *_mainObj, int _portSend, int _portRecv
	return new LightBulb_Skeleton((LightBulb*) params[0], *((int*) params[0]), *((int*) params[1]));
}

extern "C" void destroyLightBulb_Skeleton(void* t) {
	LightBulb_Skeleton* obj = (LightBulb_Skeleton*) t;
	delete obj;
}

extern "C" void initLightBulb_Skeleton(void* t) {
}

int main() {
	return 0;
}
