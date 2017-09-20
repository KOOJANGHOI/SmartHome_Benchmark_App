#ifndef _LIFXTEST_HPP__
#define _LIFXTEST_HPP__
#include <iostream>
#include <fstream>

#include "IoTSet.hpp"
#include "IoTRelation.hpp"
#include "LightBulb.hpp"
#include "LightBulbTest.hpp"
#include "Room.hpp"
#include "RoomSmart.hpp"


class Lifxtest {

	private:
		// IoTSet
		IoTSet<void*>* lifx_light_bulb;	// LightBulbTest
		IoTSet<void*>* lab_room;		// RoomSmart
		IoTRelation<void*,void*>* roomLightRelation;	// RoomSmart and LightBulbTest

		ofstream log;

	public:

		Lifxtest();
		Lifxtest(IoTSet<void*>* _lifx_light_bulb);
		Lifxtest(IoTSet<void*>* _lifx_light_bulb, IoTSet<void*>* _lab_room, IoTRelation<void*,void*>* _roomLightRelation);
		Lifxtest(void** args);
		~Lifxtest();
		void init();
};
#endif

