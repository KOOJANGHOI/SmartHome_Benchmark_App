#ifndef _LIGHTBULBSMART_HPP__
#define _LIGHTBULBSMART_HPP__
#include <iostream>
#include <vector>
#include <set>
#include "IoTRMICall.hpp"
#include "IoTRMIObject.hpp"

using namespace std;

class LightBulbSmart
{
	public:
	virtual void turnOn() = 0;
	virtual double getBrightnessRangeLowerBound() = 0;
	virtual void turnOff() = 0;
	virtual bool getState() = 0;
	virtual double getBrightnessRangeUpperBound() = 0;
	virtual double getSaturation() = 0;
	virtual double getHueRangeLowerBound() = 0;
	virtual double getHue() = 0;
	virtual double getHueRangeUpperBound() = 0;
	virtual int getTemperature() = 0;
	virtual double getBrightness() = 0;
	virtual int getTemperatureRangeLowerBound() = 0;
	virtual int getTemperatureRangeUpperBound() = 0;
	virtual void setColor(double _hue, double _saturation, double _brightness) = 0;
	virtual void init() = 0;
	virtual double getSaturationRangeLowerBound() = 0;
	virtual double getSaturationRangeUpperBound() = 0;
	virtual void setTemperature(int _temperature) = 0;
};
#endif
