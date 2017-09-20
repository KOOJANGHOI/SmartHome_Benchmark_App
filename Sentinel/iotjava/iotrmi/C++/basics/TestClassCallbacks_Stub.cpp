#include <iostream>
#include <string>
#include "TestClassComplete_Stub.hpp"
#include "CallBack.hpp"

#include "CallBackInterface_Skeleton.cpp"

using namespace std;


TestClassComplete_Stub::TestClassComplete_Stub(int _port, const char* _skeletonAddress, string _callbackAddress, int _rev, bool* _bResult, vector<int> _ports) {
	callbackAddress = _callbackAddress;
	ports = _ports;
	rmiCall = new IoTRMICall(_port, _skeletonAddress, _rev, _bResult);
	set0Allowed.insert(-9998);
	//thread th1 (&TestClassComplete_Stub::___initCallBack, this);
	//th1.detach();
	___regCB();
}

TestClassComplete_Stub::TestClassComplete_Stub(IoTRMICall* _rmiCall, string _callbackAddress, int _objIdCnt, vector<int> _ports) {
	callbackAddress = _callbackAddress;
	rmiCall = _rmiCall;
	objIdCnt = _objIdCnt;
	set0Allowed.insert(-9998);
	//thread th1 (&TestClassComplete_Stub::___initCallBack, this);
	//th1.detach();
	___regCB();
}

TestClassComplete_Stub::~TestClassComplete_Stub() {
	if (rmiCall != NULL) {
		delete rmiCall;
		rmiCall = NULL;
	}
	if (rmiObj != NULL) {
		delete rmiObj;
		rmiObj = NULL;
	}
	for(CallBackInterface* cb : vecCallbackObj) {
		delete cb;
		cb = NULL;
	}
}

void TestClassComplete_Stub::registerCallback(CallBackInterface* _cb) { 
	//CallBackInterface_CallbackSkeleton* skel0 = new CallBackInterface_CallbackSkeleton(_cb, callbackAddress, objIdCnt++);
	CallBackInterface_Skeleton* skel0 = new CallBackInterface_Skeleton(_cb, callbackAddress, objIdCnt++);
	vecCallbackObj.push_back(skel0);
	int ___paramCB0 = 1;
	int methodId = 1;
	string retType = "void";
	int numParam = 1;
	string paramCls[] = { "int" };
	void* paramObj[] = { &___paramCB0 };
	void* retObj = NULL;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
}

void TestClassComplete_Stub::___regCB() {
	int numParam = 3;
	int methodId = -9999;
	string retType = "void";
	string paramCls[] = { "int*", "String", "int" };
	int rev = 0;
	void* paramObj[] = { &ports, &callbackAddress, &rev };
	void* retObj = NULL;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
}

short TestClassComplete_Stub::getShort(short in) { 
	int methodId = 0;
	string retType = "short";
	int numParam = 1;
	string paramCls[] = { "short" };
	void* paramObj[] = { &in };
	short retVal = 0;
	void* retObj = &retVal;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
	return retVal;
}

int TestClassComplete_Stub::callBack() { 
	int methodId = 2;
	string retType = "int";
	int numParam = 0;
	string paramCls[] = {  };
	void* paramObj[] = {  };
	int retVal = 0;
	void* retObj = &retVal;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
	return retVal;
}


int main(int argc, char *argv[])
{

	int port = 5010;
	const char* address = "localhost";
	//const char* address = "192.168.2.191";	// RPi2
	//const char* skeletonAddress = "128.195.136.170";	// dc-9.calit2.uci.edu
	const char* skeletonAddress = "128.195.204.132";
	const char* callbackAddress = "128.195.204.132";	// dw-2.eecs.uci.edu (this machine)
	//const char* skeletonAddress = "192.168.2.108";	// RPi1
	//const char* callbackAddress = "192.168.2.191";	// RPi2
	int rev = 0;
	bool bResult = false;
	vector<int> ports;
	ports.push_back(12345);
	ports.push_back(22346);
	//ports.push_back(32344);
	//ports.push_back(43212);

	TestClassComplete *tcStub = new TestClassComplete_Stub(port, skeletonAddress, callbackAddress, rev, &bResult, ports);
	cout << "==== CALLBACK ====" << endl;
	CallBackInterface *cbSingle = new CallBack(2354);
	tcStub->registerCallback(cbSingle);
	cout << "Return value from callback: " << tcStub->callBack() << endl;

	return 0;
}
