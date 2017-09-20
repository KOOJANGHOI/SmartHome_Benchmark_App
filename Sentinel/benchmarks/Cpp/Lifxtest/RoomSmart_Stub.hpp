#ifndef _ROOMSMART_STUB_HPP__
#define _ROOMSMART_STUB_HPP__
#include <iostream>
#include <thread>
#include <mutex>
#include <vector>
#include <set>
#include "IoTRMIComm.hpp"
#include "IoTRMICommClient.hpp"
#include "IoTRMICommServer.hpp"

#include "RoomSmart.hpp"
#include <fstream>

using namespace std;

class RoomSmart_Stub : public RoomSmart
{
	private:

	IoTRMIComm *rmiComm;
	int objectId = 0;
	// Synchronization variables
	bool retValueReceived0 = false;
	ofstream log;

	public:

	RoomSmart_Stub();
	RoomSmart_Stub(int _portSend, int _portRecv, const char* _skeletonAddress, int _rev, bool* _bResult);
	RoomSmart_Stub(IoTRMIComm* _rmiComm, int _objectId);
	~RoomSmart_Stub();
	int getRoomID();
};
#endif
