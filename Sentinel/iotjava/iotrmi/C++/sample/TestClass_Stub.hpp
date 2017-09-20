#ifndef _TESTCLASS_STUB_HPP__
#define _TESTCLASS_STUB_HPP__

#include <iostream>
#include <set>
#include <thread>
#include "../IoTRMICall.hpp"
#include "../IoTRMIObject.hpp"
#include "TestClassInterface.hpp"
#include "CallBack_CBSkeleton.hpp"
#include "StructC.hpp"

using namespace std;

class TestClass_Stub : public TestClassInterface {
	public:
		TestClass_Stub();
		TestClass_Stub(int _port, const char* _address, int _rev, bool* _bResult, vector<int> _ports);
		~TestClass_Stub();

		void				setA(int _int);
		void				setB(float _float);
		void				setC(string _string);
		string				sumArray(vector<string> newA);
		//int64_t				sumArray(vector<int> newA);
		int					setAndGetA(int newA);
		int					setACAndGetA(string newC, int newA);
		void				registerCallback(CallBackInterface* _cb);
		void				registerCallback(vector<CallBackInterface*>_cb);
		int					callBack();
		vector<data>		handleStruct(vector<data> vecData);
		vector<EnumC>		handleEnum(vector<EnumC> vecEn);
		void				____init_CallBack();	// thread
		void				____registerCallBack();	// tell the other side that we are ready

		//exception_ptr		teptr = nullptr;

	private:		
		int							intA;
		float						floatB;
		string						stringC;
		//CallBackInterface 			cb;
		IoTRMICall					*rmiCall;
		string						address;
		vector<int>					ports;
		const static int 			objectId = 0;	// Default value is 0

		// Specific for callbacks
		IoTRMIObject				*rmiObj;
		vector<CallBackInterface*> 	vecCBObj;
		static int	objIdCnt;
		// Callback permission
		const static set<int>		set0Allowed;
};


int TestClass_Stub::objIdCnt = 0;


const set<int> TestClass_Stub::set0Allowed { 0, 1 };


TestClass_Stub::TestClass_Stub() {

	address = "";
	rmiCall = NULL;
}


TestClass_Stub::TestClass_Stub(int _port, const char* _address, int _rev, bool* _bResult, vector<int> _ports) {

	address = _address;
	rmiCall = new IoTRMICall(_port, _address, _rev, _bResult);
	ports = _ports;
	// Start thread
	/*if (teptr) {
		try {
			thread th1 (&TestClass_Stub::____init_CallBack, this);
			th1.detach();
		} catch(const exception&) {
			cout << "Got here!" << endl;
			throw exception();
		}
	}*/
	thread th1 (&TestClass_Stub::____init_CallBack, this);
	th1.detach();
	//th1.join();
	____registerCallBack();
}


TestClass_Stub::~TestClass_Stub() {

	if (rmiCall != NULL) {
		delete rmiCall;
		rmiCall = NULL;
	}
	if (rmiObj != NULL) {
		delete rmiObj;
		rmiObj = NULL;
	}
	// Special for callbacks!!!
	for(CallBackInterface* cb : vecCBObj) {
		delete cb;
		cb = NULL;
	}
}


// Callback handler thread
void TestClass_Stub::____init_CallBack() {

	bool bResult = false;
	rmiObj = new IoTRMIObject(ports[0], &bResult);
	while (true) {
		char* method = rmiObj->getMethodBytes();
		int methodId = IoTRMIObject::getMethodId(method);
		// Permission check
		// Complain if the method is not allowed
		if (set0Allowed.find(methodId) == set0Allowed.end()) {
			cerr << "TestClass_Skeleton: This object is not allowed to access method " << methodId << endl;
			exit(-1);
			//throw exception();
			//teptr = current_exception();
		}
		int objId = IoTRMIObject::getObjectId(method);
		if (objId < vecCBObj.size()) {	// Check if still within range
			CallBack_CBSkeleton* skel = 
				dynamic_cast<CallBack_CBSkeleton*> (vecCBObj.at(objId));
			skel->invokeMethod(rmiObj);
		} else {
			string error = "TestClass_Stub: Illegal object Id: " + to_string(objId);
			throw error;
		}
	}
}


// Notify that callback thread is ready
void TestClass_Stub::____registerCallBack() {

	int numParam = 3;
	int methodId = 9;
	string retType = "void";
	string paramCls[] = { "int", "string", "int" };
	int rev = 0;
	void* paramObj[] = { &ports[0], &address, &rev };
	void* retObj = NULL;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
}


void TestClass_Stub::setA(int _int) {

	int numParam = 1;
	int methodId = 0;
	string retType = "void";
	string paramCls[] = { "int" };
	void* paramObj[] = { &_int };
	void* retObj = NULL;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
}


void TestClass_Stub::setB(float _float) {

	int numParam = 1;
	int methodId = 1;
	string retType = "void";
	string paramCls[] = { "float" };
	void* paramObj[] = { &_float };
	void* retObj = NULL;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
}


void TestClass_Stub::setC(string _string) {

	int numParam = 1;
	int methodId = 2;
	string retType = "void";
	string paramCls[] = { "string" };
	void* paramObj[] = { &_string };
	void* retObj = NULL;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
}


string TestClass_Stub::sumArray(vector<string> newA) {

	int numParam = 1;
	int methodId = 3;
	string retType = "string";
	string paramCls[] = { "string[]" };
	void* paramObj[] = { &newA };
	string retVal = "";
	void* retObj = &retVal;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
	return retVal;
}


/*int64_t TestClass_Stub::sumArray(vector<int> newA) {

	int numParam = 1;
	string sign = "sumArray(int[])";
	string retType = "long";
	string paramCls[] = { "int[]" };
	void* paramObj[] = { &newA };
	int64_t retVal = 0;
	void* retObj = &retVal;
	rmiCall->remoteCall(objectId, sign, retType, paramCls, paramObj, numParam, retObj);
	return retVal;
}*/



int TestClass_Stub::setAndGetA(int newA) {

	int numParam = 1;
	int methodId = 4;
	string retType = "int";
	string paramCls[] = { "int" };
	void* paramObj[] = { &newA };
	int retVal = 0;
	void* retObj = &retVal;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
	return retVal;
}


int TestClass_Stub::setACAndGetA(string newC, int newA) {

	int numParam = 2;
	int methodId = 5;
	string retType = "int";
	string paramCls[] = { "string", "int" };
	void* paramObj[] = { &newC, &newA };
	int retVal = 0;
	void* retObj = &retVal;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
	return retVal;
}


void TestClass_Stub::registerCallback(CallBackInterface* _cb) {

	//Should implement the callback here
}


void TestClass_Stub::registerCallback(vector<CallBackInterface*> _cb) {

	for (CallBackInterface* cb: _cb) {
		CallBack_CBSkeleton* skel = new CallBack_CBSkeleton(cb, objIdCnt++);
		vecCBObj.push_back(skel);
	}

	int numParam = 1;
	int methodId = 8;
	string retType = "void";
	string paramCls[] = { "int" };
	int param1 = _cb.size();
	void* paramObj[] = { &param1 };
	void* retObj = NULL;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
}


int TestClass_Stub::callBack() {

	int numParam = 0;
	int methodId = 6;
	string retType = "int";
	string paramCls[] = { };
	void* paramObj[] = { };
	int retVal = 0;
	void* retObj = &retVal;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
	return retVal;
}


vector<data> TestClass_Stub::handleStruct(vector<data> vecData) {

	int numParam = 1;
	int methodId = 11;
	string retType = "void";
	string paramCls[] = { "int" };
	int structsize = vecData.size();
	void* paramObj[] = { &structsize };
	void* retObj = NULL;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);

	int numParam2 = 3*vecData.size();
	int methodId2 = 10;
	string retType2 = "int";
	string paramCls2[numParam2];
	void* paramObj2[numParam2];
	int pos = 0;
	for(int i = 0; i < vecData.size(); i++) {
		paramCls2[pos] = "string";
		paramObj2[pos] = &vecData[i].name; pos++;
		paramCls2[pos] = "float";
		paramObj2[pos] = &vecData[i].value; pos++;
		paramCls2[pos] = "int";
		paramObj2[pos] = &vecData[i].year; pos++;
	}
	// RETURN STRUCT OBJECT
	// Get length of struct array
	int structsize1 = 0;
	void* retObj2 = { &structsize1 };
	// IF we don't have returned struct objects, then it's just "void* retObj2 = NULL;"
	rmiCall->remoteCall(objectId, methodId2, retType2, paramCls2, paramObj2, numParam2, retObj2);
	cout << "Struct length: " << structsize1 << endl;

	// Get the returned objects
	string retCls[3*structsize1];
	void* retObj3[3*structsize1];
	int numRet = 3*structsize1;
	// define array of everything
	string param1[structsize1];
	float param2[structsize1];
	int param3[structsize1];
	pos = 0;
	for(int i=0; i < structsize1; i++) {
		retCls[pos] = "string";
		retObj3[pos++] = &param1[i];
		retCls[pos] = "float";
		retObj3[pos++] = &param2[i];
		retCls[pos] = "int";
		retObj3[pos++] = &param3[i];
	}
	rmiCall->getStructObjects(retCls, numRet, retObj3);
	vector<data> dat(structsize1);
	pos = 0;
	for (int i=0; i < structsize1; i++) {
		dat[i].name = param1[i];
		dat[i].value = param2[i];
		dat[i].year = param3[i];
	}

	return dat;
}


vector<EnumC> TestClass_Stub::handleEnum(vector<EnumC> vecEn) {

	int numParam = 1;
	int numEl = vecEn.size();
	int methodId = 12;
	string retType = "int[]";
	string paramCls[] = { "int[]" };
	// Need to define this container for integer version of enum
	vector<int> paramInt(numEl);
	for(int i = 0; i < numEl; i++) {
		paramInt[i] = (int) vecEn[i]; // cast enum to integer
	}
	void* paramObj[] = { &paramInt };
	// if no return value just
	// void* retObj2 = NULL;
	// This is with return value:
	vector<int> retEnumInt;
	void* retObj = &retEnumInt;
	rmiCall->remoteCall(objectId, methodId, retType, paramCls, paramObj, numParam, retObj);
	int enumsize1 = retEnumInt.size();
	vector<EnumC> retVal(enumsize1);
	for (int i=0; i < enumsize1; i++) {
		retVal[i] = (EnumC) retEnumInt[i];
	}
	return retVal;
}



#endif
