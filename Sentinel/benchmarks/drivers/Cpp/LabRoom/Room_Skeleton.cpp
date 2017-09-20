#include <iostream>
#include "Room_Skeleton.hpp"

using namespace std;

Room_Skeleton::Room_Skeleton(Room *_mainObj, int _portSend, int _portRecv) {
	// Logging
	int i=0;
	string file = "Room_Skeleton_cpp" + to_string(i) + ".log";
	while (ifstream(file.c_str())) {
		i++;
		file = "Room_Skeleton_cpp" + to_string(i) + ".log";
	}
	log.open(file);
	log << "Port send: " << _portSend << endl;
	log << "Port receive: " << _portRecv << endl;
	bool _bResult = false;
	mainObj = _mainObj;
	rmiComm = new IoTRMICommServer(_portSend, _portRecv, &_bResult);
	log << "Established connection with slave! Wait request invoke now..." << endl;
	IoTRMIUtil::mapSkel->insert(make_pair(_mainObj, this));
	IoTRMIUtil::mapSkelId->insert(make_pair(_mainObj, objectId));
	rmiComm->registerSkeleton(objectId, &methodReceived);
	thread th1 (&Room_Skeleton::___waitRequestInvokeMethod, this, this);
	th1.join();
}

Room_Skeleton::Room_Skeleton(Room *_mainObj, IoTRMIComm *_rmiComm, int _objectId) {
	bool _bResult = false;
	mainObj = _mainObj;
	rmiComm = _rmiComm;
	objectId = _objectId;
	rmiComm->registerSkeleton(objectId, &methodReceived);
}

Room_Skeleton::~Room_Skeleton() {
	if (rmiComm != NULL) {
		delete rmiComm;
		rmiComm = NULL;
	}
}

bool Room_Skeleton::didInitWaitInvoke() {
	return didAlreadyInitWaitInvoke;
}

int Room_Skeleton::getRoomID() {
	return mainObj->getRoomID();
}

void Room_Skeleton::___getRoomID(Room_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	int retVal = getRoomID();
	void* retObj = &retVal;
	rmiComm->sendReturnObj(retObj, "int", localMethodBytes);
	delete[] localMethodBytes;
}

void Room_Skeleton::___waitRequestInvokeMethod(Room_Skeleton* skel) {
	skel->didAlreadyInitWaitInvoke = true;
	while (true) {
		if (!methodReceived) {
			continue;
		}
		skel->methodBytes = skel->rmiComm->getMethodBytes();
		skel->methodLen = skel->rmiComm->getMethodLength();
		methodReceived = false;
		int _objectId = skel->rmiComm->getObjectId(skel->methodBytes);
		int methodId = skel->rmiComm->getMethodId(skel->methodBytes);
		if (_objectId == objectId) {
			if (set0Allowed.find(methodId) == set0Allowed.end()) {
				cerr << "Object with object Id: " << _objectId << "  is not allowed to access method: " << methodId << endl;
				return;
			}
		}
		else {
			continue;
		}
		switch (methodId) {
			case 0: {
				thread th0 (&Room_Skeleton::___getRoomID, std::ref(skel), skel);
				th0.detach(); break;
			}
			default: 
			cerr << "Method Id " << methodId << " not recognized!" << endl;
			return;
		}
	}
}

extern "C" void* createRoom_Skeleton(void** params) {
	// Args: *_mainObj, int _portSend, int _portRecv
	return new Room_Skeleton((Room*) params[0], *((int*) params[1]), *((int*) params[2]));
}

extern "C" void destroyRoom_Skeleton(void* t) {
	Room_Skeleton* obj = (Room_Skeleton*) t;
	delete obj;
}

extern "C" void initRoom_Skeleton(void* t) {
}

int main() {
	return 0;
}
