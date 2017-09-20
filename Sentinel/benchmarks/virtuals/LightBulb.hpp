#ifndef _LIGHTBULB_HPP__
#define _LIGHTBULB_HPP__
#include <iostream>

using namespace std;

class LightBulb
{
	public:
	virtual void init() = 0;
	virtual void turnOff() = 0;
	virtual void turnOn() = 0;
	virtual bool getState() = 0;
	virtual void setColor(double _hue, double _saturation, double _brightness) = 0;
	virtual void setTemperature(int _temperature) = 0;
	virtual double getBrightness() = 0;
	virtual double getHue() = 0;
	virtual double getSaturation() = 0;
	virtual int getTemperature() = 0;
	virtual double getBrightnessRangeLowerBound() = 0;
	virtual double getBrightnessRangeUpperBound() = 0;
	virtual double getHueRangeLowerBound() = 0;
	virtual double getHueRangeUpperBound() = 0;
	virtual double getSaturationRangeLowerBound() = 0;
	virtual double getSaturationRangeUpperBound() = 0;
	virtual int getTemperatureRangeLowerBound() = 0;
	virtual int getTemperatureRangeUpperBound() = 0;
};
#endif
