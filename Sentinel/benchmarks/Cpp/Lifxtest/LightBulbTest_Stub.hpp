#ifndef _LIGHTBULBTEST_STUB_HPP__
#define _LIGHTBULBTEST_STUB_HPP__
#include <iostream>
#include <thread>
#include <mutex>
#include <vector>
#include <set>
#include "IoTRMIComm.hpp"
#include "IoTRMICommClient.hpp"
#include "IoTRMICommServer.hpp"

#include "LightBulbTest.hpp"

using namespace std;

class LightBulbTest_Stub : public LightBulbTest
{
	private:

	IoTRMIComm *rmiComm;
	int objectId = 0;
	// Synchronization variables
	bool retValueReceived6 = false;
	bool retValueReceived3 = false;
	bool retValueReceived8 = false;
	bool retValueReceived7 = false;
	bool retValueReceived9 = false;
	

	public:

	LightBulbTest_Stub();
	LightBulbTest_Stub(int _portSend, int _portRecv, const char* _skeletonAddress, int _rev, bool* _bResult);
	LightBulbTest_Stub(IoTRMIComm* _rmiComm, int _objectId);
	~LightBulbTest_Stub();
	void turnOn();
	double getBrightness();
	void turnOff();
	bool getState();
	void setColor(double _hue, double _saturation, double _brightness);
	double getSaturation();
	void init();
	void setTemperature(int _temperature);
	double getHue();
	int getTemperature();
};
#endif
