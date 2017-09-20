/** Class IoTRMIObject provides methods that the upper
 *  layers can use to transport and invoke methods
 *  when using IoTSocket, IoTSocketClient and IoTSocketServer.
 *  <p>
 *  This class serves in the skeleton part of the RMI
 *  communication. It instatiate an RMI object and activate
 *  a server process that handles RMI requests.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2016-10-24
 */
#ifndef _IOTRMIOBJECT_HPP__
#define _IOTRMIOBJECT_HPP__

#include <iostream>
#include <string>
#include "IoTRMIUtil.hpp"
#include "IoTSocketServer.hpp"

using namespace std;

class IoTRMIObject final {
	public:
		IoTRMIObject(int _port, bool* _bResult);
		~IoTRMIObject();
		// Public methods
		void				sendReturnObj(void* retObj, string type);
		void				sendReturnObj(void* retObj[], string type[], int numRet);
		int					returnLength(void* retObj[], string retCls[], int numRet);
		char*				returnToBytes(void* retObj[], string retCls[], char* retBytes, int numRet);
		char*				getMethodBytes();
		int					getMethodBytesLen();
		void				setMethodBytes(char* _methodBytes);
		int					getObjectId();
		static int			getObjectId(char* methodBytes);
		int					getMethodId();
		static int			getMethodId(char* methodBytes);
		void**				getMethodParams(string paramCls[], int numParam, void* paramObj[]);

	private:
		IoTRMIUtil			*rmiUtil;
		IoTSocketServer		*rmiServer;
		char*				methodBytes;
		int					methodLen;

		// Private methods
		void				getMethodIds(const string methodSign[], const int size);
};


// Constructor
IoTRMIObject::IoTRMIObject(int _port, bool* _bResult) {

	rmiUtil = new IoTRMIUtil();
	if (rmiUtil == NULL) {
		perror("IoTRMIObject: IoTRMIUtil isn't initialized!");
	}

	methodBytes = NULL;
	methodLen = 0;

	rmiServer = new IoTSocketServer(_port, _bResult);
	if (rmiServer == NULL) {
		perror("IoTRMIObject: IoTSocketServer isn't initialized!");
	}
	fflush(NULL);
	rmiServer->connect();
	fflush(NULL);

}


// Destructor
IoTRMIObject::~IoTRMIObject() {

	// Clean up
	if (rmiUtil != NULL) {
		
		delete rmiUtil;
		rmiUtil = NULL;		
	}
	if (rmiServer != NULL) {

		fflush(NULL);
		rmiServer->close();	
		delete rmiServer;
		rmiServer = NULL;		
	}
}


// Send return values in bytes to the caller
void IoTRMIObject::sendReturnObj(void* retObj, string type) {

	// Find the length of return object in bytes
	int retLen = rmiUtil->getTypeSize(type);
	if (retLen == -1) {
		retLen = rmiUtil->getVarTypeSize(type, retObj);
	}
	// Need object bytes variable
	char retObjBytes[retLen];
	IoTRMIUtil::getObjectBytes(retObjBytes, retObj, type.c_str());
	rmiServer->sendBytes(retObjBytes, retLen);
}


// Send return values in bytes to the caller (for more than one object - struct)
void IoTRMIObject::sendReturnObj(void* retObj[], string type[], int numRet) {

	// Find the length of return object in bytes
	int retLen = returnLength(retObj, type, numRet);
	// Need object bytes variable
	char retObjBytes[retLen];
	returnToBytes(retObj, type, retObjBytes, numRet);
	rmiServer->sendBytes(retObjBytes, retLen);
}


// Get method bytes from the socket
char* IoTRMIObject::getMethodBytes() {

	// Get method in bytes and update method length
	fflush(NULL);
	methodBytes = rmiServer->receiveBytes(methodBytes, &methodLen);
	fflush(NULL);
	return methodBytes;
}


// Get method bytes length
int IoTRMIObject::getMethodBytesLen() {

	return methodLen;
}


// Get object Id from bytes
int IoTRMIObject::getObjectId() {

	char objectIdBytes[IoTRMIUtil::OBJECT_ID_LEN];
	memcpy(objectIdBytes, methodBytes, IoTRMIUtil::OBJECT_ID_LEN);
	// Get method signature 
	int objectId = 0;
	IoTRMIUtil::byteArrayToInt(&objectId, objectIdBytes);
	
	return objectId;
}


// Get object Id from bytes (static version)
int IoTRMIObject::getObjectId(char* methodBytes) {

	char objectIdBytes[IoTRMIUtil::OBJECT_ID_LEN];
	memcpy(objectIdBytes, methodBytes, IoTRMIUtil::OBJECT_ID_LEN);
	// Get method signature 
	int objectId = 0;
	IoTRMIUtil::byteArrayToInt(&objectId, objectIdBytes);
	
	return objectId;
}


// Get methodId
int IoTRMIObject::getMethodId() {

	// Get method Id
	char methodIdBytes[IoTRMIUtil::METHOD_ID_LEN];
	memcpy(methodIdBytes, methodBytes + IoTRMIUtil::OBJECT_ID_LEN, IoTRMIUtil::METHOD_ID_LEN);
	// Get method signature 
	int methodId = 0;
	IoTRMIUtil::byteArrayToInt(&methodId, methodIdBytes);
	
	return methodId;
}


// Get methodId from bytes (static version)
int IoTRMIObject::getMethodId(char* methodBytes) {

	// Get method Id
	char methodIdBytes[IoTRMIUtil::METHOD_ID_LEN];
	memcpy(methodIdBytes, methodBytes + IoTRMIUtil::OBJECT_ID_LEN, IoTRMIUtil::METHOD_ID_LEN);
	// Get method signature 
	int methodId = 0;
	IoTRMIUtil::byteArrayToInt(&methodId, methodIdBytes);
	
	return methodId;
}


// Get method parameters and return an array of parameter objects
//
// For primitive objects:
// | 32-bit method ID | m-bit actual data (fixed length)  |
// 
// For string, arrays, and non-primitive objects:
// | 32-bit method ID | 32-bit length | n-bit actual data | ...
void** IoTRMIObject::getMethodParams(string paramCls[], int numParam, void* paramObj[]) {

	// Byte scanning position
	int pos = IoTRMIUtil::OBJECT_ID_LEN + IoTRMIUtil::METHOD_ID_LEN;
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
	// Delete methodBytes
	delete[] methodBytes;

	return paramObj;
}


// Find the bytes length of a return object (struct that has more than 1 member)
int	IoTRMIObject::returnLength(void* retObj[], string retCls[], int numRet) {

	// Get byte arrays and calculate return bytes length
	int returnLen = 0;
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
char* IoTRMIObject::returnToBytes(void* retObj[], string retCls[], char* retBytes, int numRet) {

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


#endif


