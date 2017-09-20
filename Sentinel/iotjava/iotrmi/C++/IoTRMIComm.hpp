/** Class IoTRMIComm combines the functionalities
 *  of IoTRMICall and IoTRMIObject to create a single
 *  communication class with two sockets serving one
 *  directional traffic for each.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2017-01-28
 */
#ifndef _IOTRMICOMM_HPP__
#define _IOTRMICOMM_HPP__

#include <iostream>
#include <string>
#include <atomic>
#include <limits>
#include <thread>
#include <mutex>

#include "IoTSocketServer.hpp"
#include "IoTSocketClient.hpp"
#include "ConcurrentLinkedListQueue.cpp"

using namespace std;

std::atomic<bool> didGetMethodBytes(false);
std::atomic<bool> didGetReturnBytes(false);

mutex regSkelMutex;
mutex regStubMutex;
mutex retValMutex;
mutex remoteCallMutex;
mutex sendReturnObjMutex;

class IoTRMIComm {
	public:
		IoTRMIComm();
		~IoTRMIComm();
		// Public methods
		virtual void		sendReturnObj(void* retObj, string type, char* methodBytes) = 0;
		virtual void		sendReturnObj(void* retObj[], string type[], int numRet, char* methodBytes) = 0;
		int					returnLength(void* retObj[], string retCls[], int numRet);
		char*				returnToBytes(void* retObj[], string retCls[], char* retBytes, int numRet);
		char*				getMethodBytes();
		int					getMethodLength();
		int					getObjectIdFromMethod();
		static int			getObjectId(char* packetBytes);
		static int			getMethodId(char* packetBytes);
		static int			getPacketType(char* packetBytes);
		void**				getMethodParams(string paramCls[], int numParam, void* paramObj[], char* methodBytes);
		void				registerSkeleton(int objectId, bool* methodReceived);
		void				registerStub(int objectId, int methodId, bool* retValueReceived);
		int					getObjectIdCounter();
		void				setObjectIdCounter(int objIdCounter);
		void				decrementObjectIdCounter();

		int					methodLength(string paramCls[], void* paramObj[], int numParam);
		char*				methodToBytes(int objectId, int methId, string paramCls[], void* paramObj[],
								char* method, int numParam);
		virtual void		remoteCall(int objectId, int methodId, string paramCls[], 
								void* paramObj[], int numParam) = 0;
		void*				getReturnValue(string retType, void* retObj);
		
		void**				getStructObjects(string retType[], int numRet, void* retObj[]);
		void**				getReturnObjects(char* retBytes, string retCls[], int numRet, void* retObj[]);

	protected:
		IoTRMIUtil					*rmiUtil;
		char*						methodBytes;
		int							methodLen;
		char*						retValueBytes;
		int							retValueLen;
		ConcurrentLinkedListQueue	methodQueue;
		ConcurrentLinkedListQueue	returnQueue;
		map<int,bool*>				mapSkeletonId;
		map<string,bool*>			mapStubId;
		int							objectIdCounter = std::numeric_limits<int>::max();

	private:
		// Private methods
		void				wakeUpThreadOnMethodCall(IoTRMIComm* rmiComm);
		void				wakeUpThreadOnReturnValue(IoTRMIComm* rmiComm);
};


// Constructor
IoTRMIComm::IoTRMIComm() {

	rmiUtil = new IoTRMIUtil();
	methodBytes = NULL;
	retValueBytes = NULL;
	methodLen = 0;
	retValueLen = 0;
	thread th1 (&IoTRMIComm::wakeUpThreadOnMethodCall, this, this);
	th1.detach();
	thread th2 (&IoTRMIComm::wakeUpThreadOnReturnValue, this, this);
	th2.detach();	

}


// Destructor
IoTRMIComm::~IoTRMIComm() {

	// Clean up
	if (rmiUtil != NULL) {	
		delete rmiUtil;
		rmiUtil = NULL;		
	}
}


void IoTRMIComm::wakeUpThreadOnMethodCall(IoTRMIComm* rmiComm) {

	int methLen = 0;
	//cout << "Starting wakeUpThreadOnMethodCall()" << endl;
	while(true) {
		// Convert back to char*
		char* queueHead = rmiComm->methodQueue.deQAndGetLength(&methLen);
		if (queueHead != NULL) {
			rmiComm->methodBytes = queueHead;
			rmiComm->methodLen = methLen;
			//IoTRMIUtil::printBytes(rmiComm->methodBytes, rmiComm->methodLen, false);
			int currObjId = rmiComm->getObjectId(rmiComm->methodBytes);
			auto search = rmiComm->mapSkeletonId.find(currObjId);
			bool* methRecv = search->second;
			didGetMethodBytes.exchange(false);
			*methRecv = true;
			while(!didGetMethodBytes);
		}
	}
}


void IoTRMIComm::wakeUpThreadOnReturnValue(IoTRMIComm* rmiComm) {

	int retLen = 0;
	//cout << "Starting wakeUpThreadOnReturnValue()" << endl;
	while(true) {
		// Convert back to char*
		char* queueHead = rmiComm->returnQueue.deQAndGetLength(&retLen);
		if (queueHead != NULL) {
			rmiComm->retValueBytes = queueHead;
			rmiComm->retValueLen = retLen;
			//IoTRMIUtil::printBytes(rmiComm->retValueBytes, rmiComm->retValueLen, false);
			int objectId = rmiComm->getObjectId(rmiComm->retValueBytes);
			int methodId = rmiComm->getMethodId(rmiComm->retValueBytes);
			string strKey = to_string(objectId) + "-" + to_string(methodId);
			auto search = rmiComm->mapStubId.find(strKey);
			bool* retRecv = search->second;
			didGetReturnBytes.exchange(false);
			*retRecv = true;
			while(!didGetReturnBytes);
		}
	}
}


// registerSkeleton() registers the skeleton to be woken up
void IoTRMIComm::registerSkeleton(int objectId, bool* methodReceived) {

	lock_guard<mutex> guard(regSkelMutex);
	mapSkeletonId.insert(make_pair(objectId, methodReceived));
}


// registerStub() registers the skeleton to be woken up
void IoTRMIComm::registerStub(int objectId, int methodId, bool* retValueReceived) {

	lock_guard<mutex> guard(regStubMutex);
	string strKey = to_string(objectId) + "-" + to_string(methodId);
	mapStubId.insert(make_pair(strKey, retValueReceived));
}


// getObjectIdCounter() gets object Id counter
int	IoTRMIComm::getObjectIdCounter() {

	return objectIdCounter;
}


// setObjectIdCounter() sets object Id counter
void IoTRMIComm::setObjectIdCounter(int objIdCounter) {

	objectIdCounter = objIdCounter;
}


// decrementObjectIdCounter() gets object Id counter
void IoTRMIComm::decrementObjectIdCounter() {

	objectIdCounter--;
}


// Get method bytes from the socket
char* IoTRMIComm::getMethodBytes() {

	// Get method bytes
	return methodBytes;
}


// Get method length from the socket
int IoTRMIComm::getMethodLength() {

	// Get method bytes
	return methodLen;
}


// Get object Id from bytes
int IoTRMIComm::getObjectIdFromMethod() {

	char objectIdBytes[IoTRMIUtil::OBJECT_ID_LEN];
	memcpy(objectIdBytes, methodBytes, IoTRMIUtil::OBJECT_ID_LEN);
	// Get method signature 
	int objectId = 0;
	IoTRMIUtil::byteArrayToInt(&objectId, objectIdBytes);
	
	return objectId;
}


// Get object Id from bytes (static version)
int IoTRMIComm::getObjectId(char* packetBytes) {

	char objectIdBytes[IoTRMIUtil::OBJECT_ID_LEN];
	memcpy(objectIdBytes, packetBytes, IoTRMIUtil::OBJECT_ID_LEN);
	// Get method signature 
	int objectId = 0;
	IoTRMIUtil::byteArrayToInt(&objectId, objectIdBytes);
	
	return objectId;
}


// Get methodId from bytes (static version)
int IoTRMIComm::getMethodId(char* packetBytes) {

	// Get method Id
	char methodIdBytes[IoTRMIUtil::METHOD_ID_LEN];
	int offset = IoTRMIUtil::OBJECT_ID_LEN;
	memcpy(methodIdBytes, packetBytes + offset, IoTRMIUtil::METHOD_ID_LEN);
	// Get method signature 
	int methodId = 0;
	IoTRMIUtil::byteArrayToInt(&methodId, methodIdBytes);
	
	return methodId;
}


// Get methodId from bytes (static version)
int IoTRMIComm::getPacketType(char* packetBytes) {

	// Get method Id
	char packetTypeBytes[IoTRMIUtil::METHOD_ID_LEN];
	int offset = IoTRMIUtil::OBJECT_ID_LEN + IoTRMIUtil::METHOD_ID_LEN;
	memcpy(packetTypeBytes, packetBytes + offset, IoTRMIUtil::PACKET_TYPE_LEN);
	// Get method signature 
	int packetType = 0;
	IoTRMIUtil::byteArrayToInt(&packetType, packetTypeBytes);
	
	return packetType;
}


// Get method parameters and return an array of parameter objects
//
// For primitive objects:
// | 32-bit method ID | m-bit actual data (fixed length)  |
// 
// For string, arrays, and non-primitive objects:
// | 32-bit method ID | 32-bit length | n-bit actual data | ...
void** IoTRMIComm::getMethodParams(string paramCls[], int numParam, void* paramObj[], char* methodBytes) {

	// Byte scanning position
	int pos = IoTRMIUtil::OBJECT_ID_LEN + IoTRMIUtil::METHOD_ID_LEN + IoTRMIUtil::PACKET_TYPE_LEN;
	for (int i = 0; i < numParam; i++) {
		int paramLen = rmiUtil->getTypeSize(paramCls[i]);
		// Get the 32-bit field in the byte array to get the actual
		// 		length (this is a param with indefinite length)
		if (paramLen == -1) {
			char bytPrmLen[IoTRMIUtil::PARAM_LEN];
			memcpy(bytPrmLen, methodBytes + pos, IoTRMIUtil::PARAM_LEN);
			pos = pos + IoTRMIUtil::PARAM_LEN;
			int* prmLenPtr = IoTRMIUtil::byteArrayToInt(&paramLen, bytPrmLen);
			paramLen = *prmLenPtr;
		}
		char paramBytes[paramLen];
		memcpy(paramBytes, methodBytes + pos, paramLen);
		pos = pos + paramLen;
		paramObj[i] = IoTRMIUtil::getParamObject(paramObj[i], paramCls[i].c_str(), paramBytes, paramLen);
	}

	return paramObj;
}


// Find the bytes length of a return object (struct that has more than 1 member)
int	IoTRMIComm::returnLength(void* retObj[], string retCls[], int numRet) {

	// Get byte arrays and calculate return bytes length
	int returnLen = IoTRMIUtil::OBJECT_ID_LEN + IoTRMIUtil::METHOD_ID_LEN + IoTRMIUtil::PACKET_TYPE_LEN;
	for (int i = 0; i < numRet; i++) {
		// Find the return length
		int retObjLen = rmiUtil->getTypeSize(retCls[i]);
		if (retObjLen == -1) { // Store the length of the field - indefinite length
			retObjLen = rmiUtil->getVarTypeSize(retCls[i], retObj[i]);
			// Some space for return length, i.e. 32 bits for integer		
			returnLen = returnLen + IoTRMIUtil::RETURN_LEN;
		}
		// Calculate returnLen
		returnLen = returnLen + retObjLen;
	}

	return returnLen;
}


// Convert return object (struct members) into bytes
char* IoTRMIComm::returnToBytes(void* retObj[], string retCls[], char* retBytes, int numRet) {

	int pos = 0;
	// Get byte arrays and calculate return bytes length
	for (int i = 0; i < numRet; i++) {
		// Find the return length
		int retObjLen = rmiUtil->getTypeSize(retCls[i]);
		if (retObjLen == -1) { // Store the length of the field - indefinite length
			retObjLen = rmiUtil->getVarTypeSize(retCls[i], retObj[i]);
			// Write the return length
			char retLenBytes[IoTRMIUtil::RETURN_LEN];
			IoTRMIUtil::intToByteArray(retObjLen, retLenBytes);
			memcpy(retBytes + pos, retLenBytes, IoTRMIUtil::RETURN_LEN);			
			pos = pos + IoTRMIUtil::RETURN_LEN;
		}
		// Get array of bytes and put it in the array of array of bytes
		char objBytes[retObjLen];
		IoTRMIUtil::getObjectBytes(objBytes, retObj[i], retCls[i].c_str());
		memcpy(retBytes + pos, objBytes, retObjLen);
		pos = pos + retObjLen;
	}

	return retBytes;
}


// Get return value for single values (non-structs)
void* IoTRMIComm::getReturnValue(string retType, void* retObj) {

	// Receive return value and return it to caller
	lock_guard<mutex> guard(retValMutex);
	// Copy just the actual return value bytes
	int headerLen = IoTRMIUtil::OBJECT_ID_LEN + IoTRMIUtil::METHOD_ID_LEN + IoTRMIUtil::PACKET_TYPE_LEN;
	int retActualLen = retValueLen - headerLen;
	//char *retActualBytes = new char[retActualLen];
	char retActualBytes[retActualLen];
	memcpy(retActualBytes, retValueBytes + headerLen, retActualLen);
	//IoTRMIUtil::printBytes(retActualBytes, retActualLen, false);
	retObj = IoTRMIUtil::getParamObject(retObj, retType.c_str(), retActualBytes, retActualLen);
	// Delete received bytes object
	delete[] retValueBytes;
	//delete[] retActualBytes;
	
	return retObj;
}


// Get a set of return objects (struct)
void** IoTRMIComm::getStructObjects(string retType[], int numRet, void* retObj[]) {

	// Critical section that is used by different objects
	lock_guard<mutex> guard(retValMutex);
	// Copy just the actual return value bytes
	int headerLen = IoTRMIUtil::OBJECT_ID_LEN + IoTRMIUtil::METHOD_ID_LEN + IoTRMIUtil::PACKET_TYPE_LEN;
	int retActualLen = retValueLen - headerLen;
	char retActualBytes[retActualLen];
	memcpy(retActualBytes, retValueBytes + headerLen, retActualLen);
	// Return size of array of struct
	retObj = getReturnObjects(retActualBytes, retType, numRet, retObj);
	// Delete received bytes object
	delete[] retValueBytes;
	
	return retObj;
}


// Find the bytes length of a method
int IoTRMIComm::methodLength(string paramCls[], void* paramObj[], int numParam) {

	// Get byte arrays and calculate method bytes length
	// Start from the object Id + method Id...
	int methodLen = IoTRMIUtil::OBJECT_ID_LEN + IoTRMIUtil::METHOD_ID_LEN + IoTRMIUtil::PACKET_TYPE_LEN;
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
char* IoTRMIComm::methodToBytes(int objectId, int methId, string paramCls[], 
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
	char packetType[IoTRMIUtil::PACKET_TYPE_LEN];
	IoTRMIUtil::intToByteArray(IoTRMIUtil::METHOD_TYPE, methodId);
	memcpy(method + pos, methodId, IoTRMIUtil::PACKET_TYPE_LEN);
	pos = pos + IoTRMIUtil::PACKET_TYPE_LEN;
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


// Get return objects for structs
void** IoTRMIComm::getReturnObjects(char* retBytes, string retCls[], int numRet, void* retObj[]) {

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


