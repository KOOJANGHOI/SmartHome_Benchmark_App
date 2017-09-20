#ifndef _ROOMSMART_HPP__
#define _ROOMSMART_HPP__
#include <iostream>
#include <vector>
#include <set>
#include "IoTRMICall.hpp"
#include "IoTRMIObject.hpp"

using namespace std;

class RoomSmart
{
	public:
	virtual int getRoomID() = 0;
};
#endif
