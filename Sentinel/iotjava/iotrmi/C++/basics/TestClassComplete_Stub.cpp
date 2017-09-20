#include <iostream>
#include <string>
#include "TestClassComplete_Stub.hpp"
#include "CallBack.hpp"

#include "CallBackInterface_Skeleton.cpp"

using namespace std;


TestClassComplete_Stub::TestClassComplete_Stub(int _portSend, int _portRecv, const char* _skeletonAddress, int _rev, bool* _bResult) {
	rmiComm = new IoTRMICommClient(_portSend, _portRecv, _skeletonAddress, _rev, _bResult);
	rmiComm->registerStub(objectId, 0, &retValueReceived0);
	rmiComm->registerStub(objectId, 2, &retValueReceived2);
	IoTRMIUtil::mapStub->insert(make_pair(objectId, this));
}

TestClassComplete_Stub::TestClassComplete_Stub(IoTRMIComm* _rmiComm, int _objectId) {
	rmiComm = _rmiComm;
	objectId = _objectId;
	rmiComm->registerStub(objectId, 0, &retValueReceived0);
	rmiComm->registerStub(objectId, 2, &retValueReceived2);
}

TestClassComplete_Stub::~TestClassComplete_Stub() {
	if (rmiComm != NULL) {
		delete rmiComm;
		rmiComm = NULL;
	}
	for(CallBackInterface* cb : vecCallbackObj) {
		delete cb;
		cb = NULL;
	}
}

mutex mtxMethodExec1;	// TODO: We probably need to correlate this always with class name, e.g. methodExecCallBackInterfaceWithCallBack
void TestClassComplete_Stub::registerCallback(CallBackInterface* _cb) { 
	lock_guard<mutex> guard(mtxMethodExec1);
	int objIdSent = 0;
	auto it = IoTRMIUtil::mapSkel->find(_cb);
	if (it == IoTRMIUtil::mapSkel->end()) {	// Not in the map, so new object
		objIdSent = rmiComm->getObjectIdCounter();
		rmiComm->decrementObjectIdCounter();
		CallBackInterface_Skeleton* skel0 = new CallBackInterface_Skeleton(_cb, rmiComm, objIdSent);
		vecCallbackObj.push_back(skel0);
		IoTRMIUtil::mapSkel->insert(make_pair(_cb, skel0));
		IoTRMIUtil::mapSkelId->insert(make_pair(_cb, objIdSent));
		cout << "Create new skeleton for TestClass! ID=" << objIdSent << endl;
		thread th0 (&CallBackInterface_Skeleton::___waitRequestInvokeMethod, std::ref(skel0), std::ref(skel0));  
		th0.detach();
		while(!skel0->didInitWaitInvoke());
	} else {
		auto itId = IoTRMIUtil::mapSkelId->find(_cb);
		objIdSent = itId->second;
		cout << "Skeleton exists for TestClass! ID=" << objIdSent << endl;
	}

	int ___paramCB0 = objIdSent;
	int methodId = 1;
	string retType = "void";
	int numParam = 1;
	string paramCls[] = { "int" };
	void* paramObj[] = { &___paramCB0 };
	void* retObj = NULL;
	rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);
}

mutex mtxMethodExec0;	// TODO: We probably need to correlate this always with class name, e.g. methodExecCallBackInterfaceWithCallBack
short TestClassComplete_Stub::getShort(short in) {
	lock_guard<mutex> guard(mtxMethodExec0);
	cout << "getShort() is called!!!" << endl << endl;
	int methodId = 0;
	string retType = "short";
	int numParam = 1;
	string paramCls[] = { "short" };
	void* paramObj[] = { &in };
	short retVal = 0;
	void* retObj = &retVal;
	cout << "Calling remote call!" << endl;
	rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);
	cout << "Finished calling remote call!" << endl;
	// Waiting for return value
	while(!retValueReceived0);
	rmiComm->getReturnValue(retType, retObj);
	//retValueReceived0.exchange(false);
	retValueReceived0 = false;
	didGetReturnBytes.exchange(true);
	cout << "Getting return value for getShort(): " << retVal << endl;

	return retVal;
}

mutex mtxMethodExec2;	// TODO: We probably need to correlate this always with class name, e.g. methodExecCallBackInterfaceWithCallBack
int TestClassComplete_Stub::callBack() {
	lock_guard<mutex> guard(mtxMethodExec2);
	int methodId = 2;
	string retType = "int";
	int numParam = 0;
	string paramCls[] = {  };
	void* paramObj[] = {  };
	int retVal = 0;
	void* retObj = &retVal;
	rmiComm->remoteCall(objectId, methodId, paramCls, paramObj, numParam);
	// Waiting for return value
	while(!retValueReceived2);
	rmiComm->getReturnValue(retType, retObj);
	//retValueReceived2.exchange(false);
	retValueReceived2 = false;
	didGetReturnBytes.exchange(true);

	cout << "Getting return value for callback(): " << retVal << endl;

	return retVal;
}


int main(int argc, char *argv[])
{

	int portSend = 5000;
	int portRecv = 6000;
	const char* address = "localhost";
	//const char* address = "192.168.2.191";	// RPi2
	//const char* skeletonAddress = "128.195.136.163";	// dc-2.calit2.uci.edu
	const char* skeletonAddress = "128.195.204.132";
	const char* callbackAddress = "128.195.204.132";	// dw-2.eecs.uci.edu (this machine)
	//const char* skeletonAddress = "192.168.2.108";	// RPi1
	//const char* callbackAddress = "192.168.2.191";	// RPi2
	int rev = 0;
	bool bResult = false;
	//vector<int> ports;
	//ports.push_back(12345);
	//ports.push_back(22346);
	//ports.push_back(32344);
	//ports.push_back(43212);

	TestClassComplete *tcStub = new TestClassComplete_Stub(portSend, portRecv, skeletonAddress, rev, &bResult);
	vector<char> in;
	in.push_back(68);
	in.push_back(68);
	vector<char> result = tcStub->getByteArray(in);
	cout << "Test print: " << in[0] << endl;

	cout << "Getting return value from getShort(): " << tcStub->getShort(1234) << endl;
/*	//cout << "Getting return value from getShort(): " << tcStub->getShort(4321) << endl;
	//cout << "Getting return value from getShort(): " << tcStub->getShort(5678) << endl;
	cout << "==== CALLBACK ====" << endl;
	CallBackInterface *cbSingle = new CallBack(2354);
	tcStub->registerCallback(cbSingle);
	//tcStub->registerCallback(cbSingle);
	CallBackInterface *cbSingle1 = new CallBack(2646);
	tcStub->registerCallback(cbSingle1);
	CallBackInterface *cbSingle2 = new CallBack(2000);
	tcStub->registerCallback(cbSingle2);
	cout << "Return value from callback: " << tcStub->callBack() << endl;
	//cout << "Return value from callback: " << tcStub->callBack() << endl;

	// TODO: we need this while loop at the end to keep the threads running
	while(true);*/

	return 0;
}
