#ifndef _LIGHTBULBTEST_HPP__
#define _LIGHTBULBTEST_HPP__
#include <iostream>
#include <sstream>
#include <vector>
#include <set>
#include "IoTRMICall.hpp"
#include "IoTRMIObject.hpp"

using namespace std;

class LightBulbTest
{
	public:
	virtual void turnOn() = 0;
	virtual double getBrightness() = 0;
	virtual void turnOff() = 0;
	virtual bool getState() = 0;
	virtual void setColor(double _hue, double _saturation, double _brightness) = 0;
	virtual double getSaturation() = 0;
	virtual void init() = 0;
	virtual void setTemperature(int _temperature) = 0;
	virtual double getHue() = 0;
	virtual int getTemperature() = 0;

	// Custom hasher for LightBulbTest iterator
	size_t hash(LightBulbTest const& device) const {

		// Use device address for hashing
		std::stringstream ss;
		ss << &device;
		std::hash<std::string> hashVal;
		return hashVal(ss.str());
	}
};
#endif
