#ifndef _LABROOM_HPP__
#define _LABROOM_HPP__
#include <iostream>
#include <fstream>

#include "Room.hpp"

using namespace std;

class LabRoom : public Room 
{

	public:
		LabRoom();
		~LabRoom();

		int getRoomID();
};
#endif
