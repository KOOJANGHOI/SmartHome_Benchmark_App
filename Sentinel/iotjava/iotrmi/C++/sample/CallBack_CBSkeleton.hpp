#ifndef _CALLBACK_CBSKELETON_HPP__
#define _CALLBACK_CBSKELETON_HPP__

#include <iostream>
#include "CallBackInterface.hpp"
#include "../IoTRMIObject.hpp"


using namespace std;

class CallBack_CBSkeleton : public CallBackInterface {
	public:
		CallBack_CBSkeleton(CallBackInterface* _cb, int _objectId);
		~CallBack_CBSkeleton();

		void			invokeMethod(IoTRMIObject* rmiObj);
		int				printInt();
		void			setInt(int _i);

		void			___printInt(IoTRMIObject* rmiObj);
		void			___setInt(IoTRMIObject* rmiObj);

	private:
		CallBackInterface	*cb;
		int							objectId = 0;
};


// Constructor
CallBack_CBSkeleton::CallBack_CBSkeleton(CallBackInterface* _cb, int _objectId) {

	cb = _cb;
	objectId = _objectId;
}


CallBack_CBSkeleton::~CallBack_CBSkeleton() {

}


int CallBack_CBSkeleton::printInt() {

	return cb->printInt();
}


void CallBack_CBSkeleton::___printInt(IoTRMIObject* rmiObj) {

	string paramCls[] = { };
	int numParam = 0;
	void* paramObj[] = { };
	rmiObj->getMethodParams(paramCls, numParam, paramObj);
	int retVal = printInt();
	void* retObj = &retVal;
	rmiObj->sendReturnObj(retObj, "int");
}


void CallBack_CBSkeleton::setInt(int _i) {

	cb->setInt(_i);
}


void CallBack_CBSkeleton::___setInt(IoTRMIObject* rmiObj) {

	string paramCls[] = { "int" };
	int numParam = 1;
	int param1 = 1;
	void* paramObj[] = { &param1 };
	rmiObj->getMethodParams(paramCls, numParam, paramObj);
	setInt(param1);
}


void CallBack_CBSkeleton::invokeMethod(IoTRMIObject* rmiObj) {

	int methodId = rmiObj->getMethodId();
	
	switch (methodId) {
		case 0 : ___printInt(rmiObj); break;
		case 1 : ___setInt(rmiObj); break;
		default:
			string error = "Method Id not recognized!";
			throw error;
	}
}


#endif

