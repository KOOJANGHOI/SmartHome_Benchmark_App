#ifndef _ITERATOR_HPP__
#define _ITERATOR_HPP__

#include "IoTDeviceAddress.hpp"
#include "LightBulbTest.hpp"

namespace std
{
	template<> struct hash<IoTDeviceAddress>
	{
		size_t operator()(IoTDeviceAddress const& devAddress) const
		{
			return devAddress.hash(devAddress);
		}
	};
}


bool operator==(const IoTDeviceAddress& lhs, const IoTDeviceAddress& rhs) {
    return lhs.hash(lhs) == rhs.hash(rhs);
}


namespace std
{
	template<> struct hash<LightBulbTest>
	{
		size_t operator()(LightBulbTest const& device) const
		{
			return device.hash(device);
		}
	};
}


bool operator==(const LightBulbTest& lhs, const LightBulbTest& rhs) {
	return lhs.hash(lhs) == rhs.hash(rhs);
}

#endif
