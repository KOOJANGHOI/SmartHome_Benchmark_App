/** Class IoTRMICall provides methods that the upper
 *  layers can use to transport and invoke methods
 *  when using IoTSocket, IoTSocketClient and IoTSocketServer.
 *  <p>
 *  This class serves in the stub part of the RMI
 *  communication. It bridges and creates RMI requests to be
 *  transferred into the RMI object.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2016-10-18
 */
#ifndef _IOTRMICALL_HPP__
#define _IOTRMICALL_HPP__

#include <iostream>
#include <string>
#include <mutex>
#include "IoTRMIUtil.hpp"
#include "IoTSocketClient.hpp"

using namespace std;

mutex mtx;

class IoTRMICall final {
	public:
		IoTRMICall(int _port, const char* _address, int _rev, bool* _bResult);
		~IoTRMICall();
		// Public methods
		int		methodLength(string paramCls[], void* paramObj[], int numParam);
		char*	methodToBytes(int objectId, int methId, string paramCls[], void* paramObj[],
								char* method, int numParam);
		void*	remoteCall(int objectId, int methodId, string retType, string paramCls[], 
								void* paramObj[], int numParam, void* retObj);
		void**	getStructObjects(string retType[], int numRet, void* retObj[]);
		void**	getReturnObjects(char* retBytes, string retCls[], int numRet, void* retObj[]);

	private:
		IoTRMIUtil			*rmiUtil;
		IoTSocketClient		*rmiClient;

		// Private methods
		void				getMethodIds(const string methodSign[], const int size);
};


// Constructor
IoTRMICall::IoTRMICall(int _port, const char* _address, int _rev, bool* _bResult) {

	rmiUtil = new IoTRMIUtil();
	if (rmiUtil == NULL) {
		perror("IoTRMICall: IoTRMIUtil isn't initialized!");
	}
	rmiClient = new IoTSocketClient(_port, _address, _rev, _bResult);
	if (rmiClient == NULL) {
		perror("IoTRMICall: IoTSocketClient isn't initialized!");
	}
}


// Destructor
IoTRMICall::~IoTRMICall() {

	// Clean up
	if (rmiUtil != NULL) {
		
		delete rmiUtil;
		rmiUtil = NULL;		
	}
	if (rmiClient != NULL) {

		fflush(NULL);
		rmiClient->close();		
		delete rmiClient;
		rmiClient = NULL;		
	}
}


// Calls a method remotely by passing in parameters and getting a return object
void* IoTRMICall::remoteCall(int objectId, int methodId, string retType, string paramCls[], 
								void* paramObj[], int numParam, void* retObj) {

	// Critical section that is used by different objects
	lock_guard<mutex> guard(mtx);
	// Send input parameters
	int len = methodLength(paramCls, paramObj, numParam);
	char method[len];
	methodToBytes(objectId, methodId, paramCls, paramObj, method, numParam);
	// Send bytes
	fflush(NULL);
	rmiClient->sendBytes(method, len);
	fflush(NULL);
	// Receive return value and return it to caller
	if (retType.compare("void") == 0)
		// Just make it NULL if it's a void return
		retObj = NULL;
	else {
		int retLen = 0;
		char* retObjBytes = NULL;
		retObjBytes = rmiClient->receiveBytes(retObjBytes, &retLen);
		retObj = IoTRMIUtil::getParamObject(retObj, retType.c_str(), retObjBytes, retLen);
		// Delete received bytes object
		delete[] retObjBytes;
	}
	
	return retObj;
}


// Get a set of return objects (struct)
void** IoTRMICall::getStructObjects(string retType[], int numRet, void* retObj[]) {

	// Critical section that is used by different objects
	lock_guard<mutex> guard(mtx);
	// Receive struct return value and return it to caller
	int retLen = 0;
	char* retObjBytes = NULL;
	// Return size of array of struct
	retObjBytes = rmiClient->receiveBytes(retObjBytes, &retLen);
	retObj = getReturnObjects(retObjBytes, retType, numRet, retObj);
	// Delete received bytes object
	delete[] retObjBytes;
	
	return retObj;
}


// Find the bytes length of a method
int IoTRMICall::methodLength(string paramCls[], void* paramObj[], int numParam) {

	// Get byte arrays and calculate method bytes length
	// Start from the object Id + method Id...
	int methodLen = IoTRMIUtil::OBJECT_ID_LEN + IoTRMIUtil::METHOD_ID_LEN;
	for (int i = 0; i < numParam; i++) {
		// Find the parameter length
		int paramLen = rmiUtil->getTypeSize(paramCls[i]);
		if (paramLen == -1) { // Store the length of the field - indefinite length
			paramLen = rmiUtil->getVarTypeSize(paramCls[i], paramObj[i]);
			// Some space for param length, i.e. 32 bits for integer		
			methodLen = methodLen + IoTRMIUtil::PARAM_LEN;
		}
		// Calculate methodLen
		methodLen = methodLen + paramLen;
	}
	return methodLen;
}


// Convert method and its parameters into bytes
char* IoTRMICall::methodToBytes(int objectId, int methId, string paramCls[], 
		void* paramObj[], char* method, int numParam) {

	// Get object Id in bytes
	char objId[IoTRMIUtil::OBJECT_ID_LEN];
	IoTRMIUtil::intToByteArray(objectId, objId);
	memcpy(method, objId, IoTRMIUtil::OBJECT_ID_LEN);
	int pos = IoTRMIUtil::OBJECT_ID_LEN;
	// Get method Id in bytes
	char methodId[IoTRMIUtil::METHOD_ID_LEN];
	IoTRMIUtil::intToByteArray(methId, methodId);
	memcpy(method + pos, methodId, IoTRMIUtil::METHOD_ID_LEN);
	pos = pos + IoTRMIUtil::METHOD_ID_LEN;
	// Get byte arrays and calculate method bytes length
	for (int i = 0; i < numParam; i++) {
		// Find the parameter length
		int paramLen = rmiUtil->getTypeSize(paramCls[i]);
		if (paramLen == -1) { // Store the length of the field - indefinite length
			paramLen = rmiUtil->getVarTypeSize(paramCls[i], paramObj[i]);
			// Write the parameter length
			char prmLenBytes[IoTRMIUtil::PARAM_LEN];
			IoTRMIUtil::intToByteArray(paramLen, prmLenBytes);
			memcpy(method + pos, prmLenBytes, IoTRMIUtil::PARAM_LEN);			
			pos = pos + IoTRMIUtil::PARAM_LEN;
		}
		// Get array of bytes and put it in the array of array of bytes
		char objBytes[paramLen];
		IoTRMIUtil::getObjectBytes(objBytes, paramObj[i], paramCls[i].c_str());
		memcpy(method + pos, objBytes, paramLen);
		pos = pos + paramLen;
	}

	return method;
}


void** IoTRMICall::getReturnObjects(char* retBytes, string retCls[], int numRet, void* retObj[]) {

	// Byte scanning position
	int pos = 0;
	for (int i = 0; i < numRet; i++) {
		int retLen = rmiUtil->getTypeSize(retCls[i]);
		// Get the 32-bit field in the byte array to get the actual
		// 		length (this is a param with indefinite length)
		if (retLen == -1) {
			char bytRetLen[IoTRMIUtil::RETURN_LEN];
			memcpy(bytRetLen, retBytes + pos, IoTRMIUtil::RETURN_LEN);
			pos = pos + IoTRMIUtil::RETURN_LEN;
			int* retLenPtr = IoTRMIUtil::byteArrayToInt(&retLen, bytRetLen);
			retLen = *retLenPtr;
		}
		char retObjBytes[retLen];
		memcpy(retObjBytes, retBytes + pos, retLen);
		pos = pos + retLen;
		retObj[i] = IoTRMIUtil::getParamObject(retObj[i], retCls[i].c_str(), retObjBytes, retLen);
	}

	return retObj;
}


#endif


