#include <iostream>
#include "RoomSmart_Stub.hpp"

using namespace std;

RoomSmart_Stub::RoomSmart_Stub(int _portSend, int _portRecv, const char* _skeletonAddress, int _rev, bool* _bResult) {
	// Logging
	int i=0;
	string file = "RoomSmart_Stub_cpp" + to_string(i) + ".log";
	while (ifstream(file.c_str())) {
		i++;
		file = "RoomSmart_Stub_cpp" + to_string(i) + ".log";
	}
	log.open(file);
	log << "Send port: " << _portSend << endl;
	log << "Recv port: " << _portRecv << endl;
	log << "Skeleton address: " << _skeletonAddress << endl;
	log << "Rev: " << _rev << endl;
	log << "bResult: " << *_bResult << endl;
	rmiComm = new IoTRMICommClient(_portSend, _portRecv, _skeletonAddress, _rev, _bResult);
	log << "Established connection with skeleton!" << endl;
	rmiComm->registerStub(objectId, 0, &retValueReceived0);
	IoTRMIUtil::mapStub->insert(make_pair(objectId, this));
}

RoomSmart_Stub::RoomSmart_Stub(IoTRMIComm* _rmiComm, int _objectId) {
	rmiComm = _rmiComm;
	objectId = _objectId;
	rmiComm->registerStub(objectId, 0, &retValueReceived0);
}

RoomSmart_Stub::~RoomSmart_Stub() {
	if (rmiComm != NULL) {
		delete rmiComm;
		rmiComm = NULL;
	}
}

mutex mtxRoomSmart_StubMethodExec0;
int RoomSmart_Stub::getRoomID() { 
	lock_guard<mutex> guard(mtxRoomSmart_StubMethodExec0);
	int methodId = 0;
	string retType = "int";
	int numParam = 0;
	string paramCls[] = {  };
	void* paramObj[] = {  };
	int retVal = 0;
	void* retObj = &retVal;
	rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);
	// Waiting for return value
	while (!retValueReceived0);
	rmiComm->getReturnValue(retType, retObj);
	retValueReceived0 = false;
	didGetReturnBytes.exchange(true);

	return retVal;
}

extern "C" void* createRoomSmart_Stub(void** params) {
	// Args: int _portSend, int _portRecv, const char* _skeletonAddress, int _rev, bool* _bResult
	return new RoomSmart_Stub(*((int*) params[0]), *((int*) params[1]), ((string*) params[2])->c_str(), *((int*) params[3]), (bool*) params[4]);
}

extern "C" void destroyRoomSmart_Stub(void* t) {
	RoomSmart_Stub* obj = (RoomSmart_Stub*) t;
	delete obj;
}

extern "C" void initRoomSmart_Stub(void* t) {
}

int main() {
	return 0;
}
