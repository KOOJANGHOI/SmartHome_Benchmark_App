#ifndef _CALLBACK_STUB_HPP__
#define _CALLBACK_STUB_HPP__

#include <iostream>
#include "CallBackInterface.hpp"
#include "../IoTRMICall.hpp"

using namespace std;

class CallBack_Stub : public CallBackInterface {
	public:
		CallBack_Stub();
		CallBack_Stub(int _port, const char* _address, int _rev, bool* _bResult);
		~CallBack_Stub();

		int				printInt();
		void			setInt(int _i);

	private:		

		IoTRMICall	*rmiCall;
		string 		address;
		int 		objectId = 0;	// Default value is 0
};


// Constructor
CallBack_Stub::CallBack_Stub() {

	address = "";
	rmiCall = NULL;
}


CallBack_Stub::CallBack_Stub(int _port, const char* _address, int _rev, bool* _bResult) {

	address = _address;
	rmiCall = new IoTRMICall(_port, _address, _rev, _bResult);
}


CallBack_Stub::~CallBack_Stub() {

	if (rmiCall != NULL) {
		delete rmiCall;
		rmiCall = NULL;
	}
}


int CallBack_Stub::printInt() {

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


void CallBack_Stub::setInt(int _i) {

	int numParam = 1;
	int methodId = 1;
	string retType = "void";
	string paramCls[] = { "int" };
	void* paramObj[] = { &_i };
	void* retObj = NULL;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
}

#endif
