#ifndef _ROOM_SKELETON_HPP__
#define _ROOM_SKELETON_HPP__
#include <iostream>
#include "Room.hpp"

#include <vector>
#include <set>
#include "IoTRMIComm.hpp"
#include "IoTRMICommClient.hpp"
#include "IoTRMICommServer.hpp"

#include <fstream>

using namespace std;

class Room_Skeleton : public Room
{
	private:

	Room *mainObj;
	IoTRMIComm *rmiComm;
	char* methodBytes;
	int methodLen;
	int objectId = 0;
	static set<int> set0Allowed;
	// Synchronization variables
	bool methodReceived = false;
	bool didAlreadyInitWaitInvoke = false;
	ofstream log;

	public:

	Room_Skeleton();
	Room_Skeleton(Room*_mainObj, int _portSend, int _portRecv);
	Room_Skeleton(Room*_mainObj, IoTRMIComm *rmiComm, int _objectId);
	~Room_Skeleton();
	bool didInitWaitInvoke();
	int getRoomID();
	void ___getRoomID(Room_Skeleton* skel);
	void ___waitRequestInvokeMethod(Room_Skeleton* skel);
};
set<int> Room_Skeleton::set0Allowed { 0 };
#endif
