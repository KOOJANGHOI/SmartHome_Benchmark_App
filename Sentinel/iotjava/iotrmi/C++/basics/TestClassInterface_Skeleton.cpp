#include <iostream>
#include <string>
#include "TestClassInterface_Skeleton.hpp"
#include "TestClass.hpp"

#include "CallBackInterfaceWithCallBack_Stub.cpp"

using namespace std;

TestClassInterface_Skeleton::TestClassInterface_Skeleton(TestClassInterface *_mainObj, int _portSend, int _portRecv) {
	bool _bResult = false;
	mainObj = _mainObj;
	rmiComm = new IoTRMICommServer(_portSend, _portRecv, &_bResult);
	IoTRMIUtil::mapSkel->insert(make_pair(_mainObj, this));
	IoTRMIUtil::mapSkelId->insert(make_pair(_mainObj, objectId));
	rmiComm->registerSkeleton(objectId, &methodReceived);
	thread th1 (&TestClassInterface_Skeleton::___waitRequestInvokeMethod, this, this);
//	th1.detach();
	th1.join();
}

TestClassInterface_Skeleton::TestClassInterface_Skeleton(TestClassInterface *_mainObj, IoTRMIComm *_rmiComm, int _objectId) {
	bool _bResult = false;
	mainObj = _mainObj;
	rmiComm = _rmiComm;
	objectId = _objectId;
	rmiComm->registerSkeleton(objectId, &methodReceived);
}

TestClassInterface_Skeleton::~TestClassInterface_Skeleton() {
	if (rmiComm != NULL) {
		delete rmiComm;
		rmiComm = NULL;
	}
	for(CallBackInterfaceWithCallBack* cb : vecCallbackObj) {
		delete cb;
		cb = NULL;
	}
}

bool TestClassInterface_Skeleton::didInitWaitInvoke() {

	return didAlreadyInitWaitInvoke;
}

short TestClassInterface_Skeleton::getShort(short in) {
	return mainObj->getShort(in);
}

void TestClassInterface_Skeleton::registerCallback(CallBackInterfaceWithCallBack* _cb) {
	mainObj->registerCallback(_cb);
}

int TestClassInterface_Skeleton::callBack() {
	return mainObj->callBack();
}

void TestClassInterface_Skeleton::___getShort(TestClassInterface_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	//cout << "Bytes inside getShort: " << endl;
	//IoTRMIUtil::printBytes(localMethodBytes, methodLen, false);
	didGetMethodBytes.exchange(true);
	string paramCls[] = { "short" };
	int numParam = 1;
	short in;
	void* paramObj[] = { &in };
	skel->rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	short retVal = getShort(in);
	cout << "Getting return value getShort(): " << retVal << endl;
	void* retObj = &retVal;
	skel->rmiComm->sendReturnObj(retObj, "short", localMethodBytes);
	cout << "Sent return value for getShort()" << endl;
	delete[] localMethodBytes;
}

void TestClassInterface_Skeleton::___registerCallback(TestClassInterface_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = { "int" };
	int numParam = 1;
	int numStubs0 = 0;
	void* paramObj[] = { &numStubs0 };
	skel->rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	// Choosing the right stub
	int objIdRecv = numStubs0;
	CallBackInterfaceWithCallBack* stub0 = NULL;
	auto it = IoTRMIUtil::mapStub->find(objIdRecv);
	if (it == IoTRMIUtil::mapStub->end()) { // Not in the map, so new object
		stub0 = new CallBackInterfaceWithCallBack_Stub(rmiComm, objIdRecv);
		IoTRMIUtil::mapStub->insert(make_pair(objIdRecv, stub0));
		cout << "Create new stub for Callback! ID=" << objIdRecv << endl;
		rmiComm->setObjectIdCounter(objIdRecv);
		rmiComm->decrementObjectIdCounter();
	} else {
		stub0 = (CallBackInterfaceWithCallBack_Stub*) it->second;
		cout << "Stub exists for Callback! ID=" << objIdRecv << endl;
	}
	skel->vecCallbackObj.push_back(stub0);
	skel->registerCallback(stub0);
	delete[] localMethodBytes;
}

void TestClassInterface_Skeleton::___callBack(TestClassInterface_Skeleton* skel) {
	char* localMethodBytes = new char[methodLen];
	memcpy(localMethodBytes, skel->methodBytes, methodLen);
	didGetMethodBytes.exchange(true);
	string paramCls[] = {  };
	int numParam = 0;
	void* paramObj[] = {  };
	skel->rmiComm->getMethodParams(paramCls, numParam, paramObj, localMethodBytes);
	int retVal = callBack();
	void* retObj = &retVal;
	skel->rmiComm->sendReturnObj(retObj, "int", localMethodBytes);
	delete[] localMethodBytes;
}

void TestClassInterface_Skeleton::___waitRequestInvokeMethod(TestClassInterface_Skeleton* skel) {
	cout << "Running loop!" << endl;
	//didAlreadyInitWaitInvoke.exchange(true);
	skel->didAlreadyInitWaitInvoke = true;
	while (true) {
		if (!methodReceived)
			continue;
		skel->methodBytes = skel->rmiComm->getMethodBytes();
		skel->methodLen = skel->rmiComm->getMethodLength();
		cout << endl;
		// TODO: Get method length as well!!!
		//methodReceived.exchange(false);
		methodReceived = false;
		int _objectId = skel->rmiComm->getObjectId(skel->methodBytes);
		int methodId = skel->rmiComm->getMethodId(skel->methodBytes);
		if (_objectId == objectId) {
			if (skel->set0Allowed.find(methodId) == skel->set0Allowed.end()) {
				cerr << "Object with object Id: " << _objectId << "  is not allowed to access method: " << methodId << endl;
				return;
			}
		}
		else
			continue;
		switch (methodId) {
			case 0: { thread th0 (&TestClassInterface_Skeleton::___getShort, std::ref(skel), skel); th0.detach(); break; }
					//___getShort(skel); break;
			case 1: { thread th1 (&TestClassInterface_Skeleton::___registerCallback, std::ref(skel), skel); th1.detach(); break; }
					//___registerCallback(skel); break;
			case 2: { thread th2 (&TestClassInterface_Skeleton::___callBack, std::ref(skel), skel); th2.detach(); break; }
					//___callBack(skel); break;
			default: 
			cerr << "Method Id " << methodId << " not recognized!" << endl;
			return;
		}
		cout << "Out of switch statement!" << endl;
	}
}



int main(int argc, char *argv[])
{
	// First argument is port number
	/*int port = atoi(argv[1]);
	int argv2 = atoi(argv[2]);
	float argv3 = atof(argv[3]);
	string argv4 = string(argv[4]);

	cout << port << endl;
	cout << argv2 << endl;
	cout << argv3 << endl;
	cout << argv4 << endl;*/

	int portSend = 5000;
	int portRecv = 6000;
	//TestClassInterface *tc = new TestClass(argv2, argv3, argv4);
	TestClassInterface *tc = new TestClass(123, 2.345, "test");
	//TestClassInterface *tc = new TestClassProfiling();
	TestClassInterface_Skeleton *tcSkel = new TestClassInterface_Skeleton(tc, portSend, portRecv);

	//delete tc;
	//delete tcSkel;
	return 0;
}
