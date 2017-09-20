#ifndef _CALLBACK_SKELETON_HPP__
#define _CALLBACK_SKELETON_HPP__

#include <iostream>
#include "../IoTRMIObject.hpp"
#include "CallBack.hpp"

using namespace std;

class CallBack_Skeleton : public CallBackInterface {
	public:
		CallBack_Skeleton(CallBackInterface* _cb, int _port);
		~CallBack_Skeleton();

		void			___waitRequestInvokeMethod();
		int				printInt();
		void			setInt(int _i);

		void			___printInt();
		void			___setInt();

	private:		
		CallBackInterface	*cb;
		IoTRMIObject		*rmiObj;
};


// Constructor
CallBack_Skeleton::CallBack_Skeleton(CallBackInterface* _cb, int _port) {

	bool _bResult = false;
	cb = _cb;
	rmiObj = new IoTRMIObject(_port, &_bResult);
	___waitRequestInvokeMethod();
}


CallBack_Skeleton::~CallBack_Skeleton() {

	if (rmiObj != NULL) {
		delete rmiObj;
		rmiObj = NULL;
	}
}


int CallBack_Skeleton::printInt() {

	return cb->printInt();
}


void CallBack_Skeleton::___printInt() {

	string paramCls[] = { };
	int numParam = 0;
	void* paramObj[] = { };
	rmiObj->getMethodParams(paramCls, numParam, paramObj);
	int retVal = printInt();
	void* retObj = &retVal;
	rmiObj->sendReturnObj(retObj, "int");
}


void CallBack_Skeleton::setInt(int _i) {

	cb->setInt(_i);
}


void CallBack_Skeleton::___setInt() {

	string paramCls[] = { "int" };
	int numParam = 1;
	int param1 = 1;
	void* paramObj[] = { &param1 };
	rmiObj->getMethodParams(paramCls, numParam, paramObj);
	setInt(param1);
}


void CallBack_Skeleton::___waitRequestInvokeMethod() {

	// Loop continuously waiting for incoming bytes
	while (true) {

		rmiObj->getMethodBytes();
		int methodId = rmiObj->getMethodId();
		
		switch (methodId) {
			case 0 : ___printInt(); break;
			case 1 : ___setInt(); break;
			default:
				string error = "Method Id not recognized!";
				throw error;
		}
	}
}


#endif

