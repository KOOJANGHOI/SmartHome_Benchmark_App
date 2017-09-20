#ifndef _CALLBACK_CBSTUB_HPP__
#define _CALLBACK_CBSTUB_HPP__

#include <iostream>
#include "CallBackInterface.hpp"
#include "../IoTRMICall.hpp"

using namespace std;

class CallBack_CBStub : public CallBackInterface {
	public:
		CallBack_CBStub();
		CallBack_CBStub(IoTRMICall* _rmiCall, int _objectId);
		~CallBack_CBStub();

		int						printInt();
		void					setInt(int _i);

	private:		

		IoTRMICall	*rmiCall;
		int 		objectId = 0;	// Default value is 0
};


// Constructor
CallBack_CBStub::CallBack_CBStub() {

	rmiCall = NULL;
}


CallBack_CBStub::CallBack_CBStub(IoTRMICall* _rmiCall, int _objectId) {

	objectId = _objectId;
	rmiCall = _rmiCall;
}


CallBack_CBStub::~CallBack_CBStub() {

	if (rmiCall != NULL) {
		delete rmiCall;
		rmiCall = NULL;
	}
}


int CallBack_CBStub::printInt() {

	cout << "Got here in printInt()" << endl;
	int numParam = 0;
	int methodId = 0;
	string retType = "int";
	string paramCls[] = { };
	void* paramObj[] = { };
	int retVal = 0;
	void* retObj = &retVal;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
	return retVal;
}


void CallBack_CBStub::setInt(int _i) {

	int numParam = 1;
	int methodId = 1;
	string retType = "void";
	string paramCls[] = { "int" };
	void* paramObj[] = { &_i };
	void* retObj = NULL;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
}

#endif

