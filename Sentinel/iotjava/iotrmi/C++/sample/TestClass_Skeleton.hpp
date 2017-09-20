#ifndef _TESTCLASS_SKELETON_HPP__
#define _TESTCLASS_SKELETON_HPP__

#include <iostream>
#include <exception>
#include <set>
#include "../IoTRMIObject.hpp"
#include "../IoTRMICall.hpp"
#include "CallBack_CBStub.hpp"
#include "TestClassInterface.hpp"

using namespace std;

class TestClass_Skeleton : public TestClassInterface {
	public:
		TestClass_Skeleton(TestClassInterface* _tc, int _port);
		~TestClass_Skeleton();

		void			___waitRequestInvokeMethod();
		void			setA(int _int);
		void			setB(float _float);
		void			setC(string _string);
		string			sumArray(vector<string> newA);
		//int64_t		sumArray(vector<int> newA);
		int				setAndGetA(int newA);
		int				setACAndGetA(string newC, int newA);
		void			registerCallback(CallBackInterface* _cb);
		void			registerCallback(vector<CallBackInterface*> _cb);
		int				callBack();
		vector<data>	handleStruct(vector<data> vecData);
		vector<EnumC>	handleEnum(vector<EnumC> vecEn);
		
		void			___setA();
		void			___setB();
		void			___setC();
		void			___sumArray();
		//int64_t		____sumArray();
		void			___setAndGetA();
		void			___setACAndGetA();
		void			___registerCallback();
		void			____registerCallback();
		// For array of callbacks
		void			___regCB();
		void			___callBack();
		// For array of structs
		int				___structSize();
		void			___handleStruct(int structsize1);
		int				___enumSize();
		void			___handleEnum(int enumsize1);

	private:		
		TestClassInterface			*tc;
		IoTRMIObject				*rmiObj;
		// Permission setup
		const static int			object0Id = 0;
		//const static int			object0Permission[];
		const static set<int>		set0Allowed;
		
		IoTRMICall					*rmiCall;
		static int					objIdCnt;
		vector<CallBackInterface*>	vecCBObj;
		//CallBackInterface cbstub;
};


// Permission setup
//const int TestClass_Skeleton::object0Id = 0;
//const int TestClass_Skeleton::object0Permission[] = {0, 1, 2, 3, 4, 5};
const set<int> TestClass_Skeleton::set0Allowed {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

int TestClass_Skeleton::objIdCnt = 0;


TestClass_Skeleton::TestClass_Skeleton(TestClassInterface* _tc, int _port) {

	bool _bResult = false;
	tc = _tc;
	rmiObj = new IoTRMIObject(_port, &_bResult);
	___waitRequestInvokeMethod();
}


TestClass_Skeleton::~TestClass_Skeleton() {

	if (rmiObj != NULL) {
		delete rmiObj;
		rmiObj = NULL;
	}
	if (rmiCall != NULL) {
		delete rmiCall;
		rmiCall = NULL;
	}
	for(CallBackInterface* cb : vecCBObj) {
		delete cb;
		cb = NULL;
	}
}


void TestClass_Skeleton::setA(int _int) {

	tc->setA(_int);
}


void TestClass_Skeleton::___setA() {

	string paramCls[] = { "int" };
	int numParam = 1;
	int param1 = 0;
	void* paramObj[] = { &param1 };
	rmiObj->getMethodParams(paramCls, numParam, paramObj);
	setA(param1);
}


void TestClass_Skeleton::setB(float _float) {

	tc->setB(_float);
}


void TestClass_Skeleton::___setB() {

	string paramCls[] = { "float" };
	int numParam = 1;
	float param1 = 0.0;
	void* paramObj[] = { &param1 };
	rmiObj->getMethodParams(paramCls, numParam, paramObj);
	setB(param1);
}


void TestClass_Skeleton::setC(string _string) {

	tc->setC(_string);
}


void TestClass_Skeleton::___setC() {

	string paramCls[] = { "string" };
	int numParam = 1;
	string param1 = "";
	void* paramObj[] = { &param1 };
	rmiObj->getMethodParams(paramCls, numParam, paramObj);
	setC(param1);
}


string TestClass_Skeleton::sumArray(vector<string> newA) {

	return tc->sumArray(newA);
}


void TestClass_Skeleton::___sumArray() {

	string paramCls[] = { "string[]" };
	int numParam = 1;
	vector<string> param1;
	void* paramObj[] = { &param1 };
	rmiObj->getMethodParams(paramCls, numParam, paramObj);
	string retVal = sumArray(param1);
	void* retObj = &retVal;
	rmiObj->sendReturnObj(retObj, "string");
}


/*int64_t TestClass_Skeleton::sumArray(vector<int> newA) {

	return tc->sumArray(newA);
}*/


/*int64_t TestClass_Skeleton::____sumArray() {

}*/


int TestClass_Skeleton::setAndGetA(int newA) {

	return tc->setAndGetA(newA);
}


void TestClass_Skeleton::___setAndGetA() {

	string paramCls[] = { "int" };
	int numParam = 1;
	int param1 = 0;
	void* paramObj[] = { &param1 };
	rmiObj->getMethodParams(paramCls, numParam, paramObj);
	int retVal = setAndGetA(param1);
	void* retObj = &retVal;
	rmiObj->sendReturnObj(retObj, "int");
}


int TestClass_Skeleton::setACAndGetA(string newC, int newA) {

	return tc->setACAndGetA(newC, newA);
}


void TestClass_Skeleton::___setACAndGetA() {

	string paramCls[] = { "string", "int" };
	int numParam = 2;
	string param1 = "";
	int param2 = 0;
	void* paramObj[] = { &param1, &param2 };
	rmiObj->getMethodParams(paramCls, numParam, paramObj);
	int retVal = setACAndGetA(param1, param2);
	void* retObj = &retVal;
	rmiObj->sendReturnObj(retObj, "int");
}


void TestClass_Skeleton::registerCallback(CallBackInterface* _cb) {

	tc->registerCallback(_cb);
}


void TestClass_Skeleton::___registerCallback() {

}


void TestClass_Skeleton::registerCallback(vector<CallBackInterface*> _cb) {

	tc->registerCallback(_cb);
}


void TestClass_Skeleton::___regCB() {

	string paramCls[] = { "int", "string", "int" };
	int numParam = 3;
	int param1 = 0;
	string param2 = "";
	int param3 = 0;
	void* paramObj[] = { &param1, &param2, &param3 };
	rmiObj->getMethodParams(paramCls, numParam, paramObj);
	// Instantiate IoTRMICall object
	bool bResult = false;
	cout << "Port: " << param1 << endl;
	cout << "Address: " << param2 << endl;

	rmiCall = new IoTRMICall(param1, param2.c_str(), param3, &bResult);
}


void TestClass_Skeleton::____registerCallback() {

	string paramCls[] = { "int" };
	int numParam = 1;
	int numStubs = 0;
	void* paramObj[] = { &numStubs };
	rmiObj->getMethodParams(paramCls, numParam, paramObj);
	vector<CallBackInterface*> stub;
	for (int objId = 0; objId < numStubs; objId++) {
		CallBackInterface* cb = new CallBack_CBStub(rmiCall, objIdCnt);
		stub.push_back(cb);
		vecCBObj.push_back(cb);
		objIdCnt++;
	}
	registerCallback(stub);
}


int TestClass_Skeleton::callBack() {

	tc->callBack();
}


void TestClass_Skeleton::___callBack() {

	int retVal = callBack();
	void* retObj = &retVal;
	rmiObj->sendReturnObj(retObj, "int");
}


vector<data> TestClass_Skeleton::handleStruct(vector<data> vecData) {

	return tc->handleStruct(vecData);
}


int TestClass_Skeleton::___structSize() {

	string paramCls[] = { "int" };
	int numParam = 1;
	int param1 = 0;
	void* paramObj[] = { &param1 };
	rmiObj->getMethodParams(paramCls, numParam, paramObj);
	return param1;
}


void TestClass_Skeleton::___handleStruct(int structsize1) {

	string paramCls[3*structsize1];
	void* paramObj[3*structsize1];
	int numParam = 3*structsize1;
	// define array of everything
	string param1[structsize1];
	float param2[structsize1];
	int param3[structsize1];
	int pos = 0;
	for(int i=0; i < structsize1; i++) {
		paramCls[pos] = "string";
		paramObj[pos++] = &param1[i];
		paramCls[pos] = "float";
		paramObj[pos++] = &param2[i];
		paramCls[pos] = "int";
		paramObj[pos++] = &param3[i];
	}
	rmiObj->getMethodParams(paramCls, numParam, paramObj);
	vector<data> dat(structsize1);
	pos = 0;
	for (int i=0; i < structsize1; i++) {
		dat[i].name = param1[i];
		dat[i].value = param2[i];
		dat[i].year = param3[i];
	}
	// This is a return value of type vector of struct
	// If no return value, then just "handleStruct(dat)"
	vector<data> retData = handleStruct(dat);
	// Send the length first!
	int retLength = retData.size();
	void* retObj = &retLength;
	rmiObj->sendReturnObj(retObj, "int");
	// Send the actual bytes - struct of 3 members
	int numRetObj = 3*retLength;
	string retCls[numRetObj];
	void* retObj2[numRetObj];
	pos = 0;
	for(int i = 0; i < retLength; i++) {
		retCls[pos] = "string";
		retObj2[pos] = &retData[i].name; pos++;
		retCls[pos] = "float";
		retObj2[pos] = &retData[i].value; pos++;
		retCls[pos] = "int";
		retObj2[pos] = &retData[i].year; pos++;
	}
	rmiObj->sendReturnObj(retObj2, retCls, numRetObj);
}


vector<EnumC> TestClass_Skeleton::handleEnum(vector<EnumC> vecEn) {

	return tc->handleEnum(vecEn);
}


int TestClass_Skeleton::___enumSize() {

	string paramCls[] = { "int" };
	int numParam = 1;
	int param1 = 0;
	void* paramObj[] = { &param1 };
	rmiObj->getMethodParams(paramCls, numParam, paramObj);
	return param1;
}


void TestClass_Skeleton::___handleEnum() {

	int numParam = 1;
	string paramCls[] = { "int[]" };
	vector<int> paramInt;
	void* paramObj[] = { &paramInt };
	// Receive the array of integers
	rmiObj->getMethodParams(paramCls, numParam, paramObj);
	int enumSize1 = paramInt.size();
	vector<EnumC> en(enumSize1);
	for (int i=0; i < enumSize1; i++) {
		en[i] = (EnumC) paramInt[i];
	}
	//handleEnum(en);
	// if void, just "handleEnum(en)"
	// this is when we have return value vector<EnumC>
	vector<EnumC> retEnum = handleEnum(en);
	// Get length first
	int enumSize2 = retEnum.size();
	// Now send the array of integers
	vector<int> retEnumInt(enumSize2);
	for (int i=0; i < enumSize2; i++) {
		retEnumInt[i] = (int) retEnum[i];
	}
	void* retObj = &retEnumInt;
	rmiObj->sendReturnObj(retObj, "int[]");
}


void TestClass_Skeleton::___waitRequestInvokeMethod() {

	int structsize1 = 0;

	// Loop continuously waiting for incoming bytes
	while (true) {

		rmiObj->getMethodBytes();
		int _objectId = rmiObj->getObjectId();
		int methodId = rmiObj->getMethodId();
		if (_objectId == object0Id) {
		// Multiplex based on object Id
			// Complain if the method is not allowed
			if (set0Allowed.find(methodId) == set0Allowed.end()) {
				cerr << "TestClass_Skeleton: This object is not allowed to access method " << methodId << endl;
				//exit(1);
				throw exception();
			}
		// If we have more than 1 object Id...
		//else if (_objectId == object1Id) {

		} else {
			cerr << "TestClass_Skeleton: Unrecognizable object Id: " << _objectId << endl;
			throw exception();
			//exit(1);
		}
		
		switch (methodId) {
			case 0: ___setA(); break;
			case 1: ___setB(); break;
			case 2: ___setC(); break;
			case 3: ___sumArray(); break;
		/*  case 3: ____sumArray(); break;*/
			case 4: ___setAndGetA(); break;
			case 5: ___setACAndGetA(); break;
			case 6: ___callBack(); break; 
			case 7: ___registerCallback(); break;
			case 8:	____registerCallback(); break;
			case 9: ___regCB(); break;
			// Handle struct
			case 10: ___handleStruct(structsize1); break;
			case 11: structsize1 = ___structSize(); break;
			case 12: ___handleEnum(); break;
			default:
				string error = "Method Id not recognized!";
				throw error;
		}
	}
}


#endif

