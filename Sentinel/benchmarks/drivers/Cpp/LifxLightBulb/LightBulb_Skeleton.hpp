#ifndef _LIGHTBULB_SKELETON_HPP__
#define _LIGHTBULB_SKELETON_HPP__
#include <iostream>
#include "LightBulb.hpp"

#include <vector>
#include <set>
#include "IoTRMIComm.hpp"
#include "IoTRMICommClient.hpp"
#include "IoTRMICommServer.hpp"

using namespace std;

class LightBulb_Skeleton : public LightBulb
{
	private:

	LightBulb *mainObj;
	IoTRMIComm *rmiComm;
	char* methodBytes;
	int methodLen;
	int objectId = 0;
	static set<int> set0Allowed;
	// Synchronization variables
	bool methodReceived = false;
	bool didAlreadyInitWaitInvoke = false;
	

	public:

	LightBulb_Skeleton();
	LightBulb_Skeleton(LightBulb*_mainObj, int _portSend, int _portRecv);
	LightBulb_Skeleton(LightBulb*_mainObj, IoTRMIComm *rmiComm, int _objectId);
	~LightBulb_Skeleton();
	bool didInitWaitInvoke();
	void init();
	void turnOff();
	void turnOn();
	bool getState();
	void setColor(double _hue, double _saturation, double _brightness);
	void setTemperature(int _temperature);
	double getBrightness();
	double getHue();
	double getSaturation();
	int getTemperature();
	double getBrightnessRangeLowerBound();
	double getBrightnessRangeUpperBound();
	double getHueRangeLowerBound();
	double getHueRangeUpperBound();
	double getSaturationRangeLowerBound();
	double getSaturationRangeUpperBound();
	int getTemperatureRangeLowerBound();
	int getTemperatureRangeUpperBound();
	void ___init(LightBulb_Skeleton* skel);
	void ___turnOff(LightBulb_Skeleton* skel);
	void ___turnOn(LightBulb_Skeleton* skel);
	void ___getState(LightBulb_Skeleton* skel);
	void ___setColor(LightBulb_Skeleton* skel);
	void ___setTemperature(LightBulb_Skeleton* skel);
	void ___getBrightness(LightBulb_Skeleton* skel);
	void ___getHue(LightBulb_Skeleton* skel);
	void ___getSaturation(LightBulb_Skeleton* skel);
	void ___getTemperature(LightBulb_Skeleton* skel);
	void ___getBrightnessRangeLowerBound(LightBulb_Skeleton* skel);
	void ___getBrightnessRangeUpperBound(LightBulb_Skeleton* skel);
	void ___getHueRangeLowerBound(LightBulb_Skeleton* skel);
	void ___getHueRangeUpperBound(LightBulb_Skeleton* skel);
	void ___getSaturationRangeLowerBound(LightBulb_Skeleton* skel);
	void ___getSaturationRangeUpperBound(LightBulb_Skeleton* skel);
	void ___getTemperatureRangeLowerBound(LightBulb_Skeleton* skel);
	void ___getTemperatureRangeUpperBound(LightBulb_Skeleton* skel);
	void ___waitRequestInvokeMethod(LightBulb_Skeleton* skel);
};
set<int> LightBulb_Skeleton::set0Allowed { 2, 6, 1, 3, 4, 8, 0, 5, 7, 9 };
#endif
