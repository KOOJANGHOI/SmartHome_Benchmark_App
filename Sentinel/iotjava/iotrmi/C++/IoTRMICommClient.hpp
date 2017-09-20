/** Class IoTRMICommClient implements the client side
 *  of IoTRMIComm class.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2017-01-28
 */
#ifndef _IOTRMICOMMCLIENT_HPP__
#define _IOTRMICOMMCLIENT_HPP__

#include <iostream>
#include <string>
#include <atomic>
#include <limits>
#include <thread>
#include <mutex>

#include "IoTRMIComm.hpp"

using namespace std;

mutex clientRemoteCallMutex;
mutex clientSendReturnObjMutex;

class IoTRMICommClient final : public IoTRMIComm {
	public:
		IoTRMICommClient(int _portSend, int _portRecv, const char* _address, int _rev, bool* _bResult);
		~IoTRMICommClient();
		// Public methods
		void				sendReturnObj(void* retObj, string type, char* methodBytes);
		void				sendReturnObj(void* retObj[], string type[], int numRet, char* methodBytes);
		void				remoteCall(int objectId, int methodId, string paramCls[], void* paramObj[], int numParam);
		//void				waitForPackets();
		//void				waitForPackets(IoTRMICommClient* rmiComm);

	private:
		IoTSocketClient		*rmiClientSend;
		IoTSocketClient		*rmiClientRecv;

		// Private methods
		void				waitForPackets(IoTRMICommClient* rmiComm);
};


// Constructor
IoTRMICommClient::IoTRMICommClient(int _portSend, int _portRecv, const char* _address, int _rev, bool* _bResult) : IoTRMIComm() {

	rmiClientRecv = new IoTSocketClient(_portSend, _address, _rev, _bResult);
	rmiClientSend = new IoTSocketClient(_portRecv, _address, _rev, _bResult);
	thread th1 (&IoTRMICommClient::waitForPackets, this, this);
	th1.detach();

}


// Destructor
IoTRMICommClient::~IoTRMICommClient() {

	// Clean up
	if (rmiClientRecv != NULL) {	
		delete rmiClientRecv;
		rmiClientRecv = NULL;		
	}
	if (rmiClientSend != NULL) {	
		delete rmiClientSend;
		rmiClientSend = NULL;		
	}
}


void IoTRMICommClient::waitForPackets(IoTRMICommClient* rmiComm) {

	char* packetBytes = NULL;
	int packetLen = 0;
	while(true) {
		fflush(NULL);
		packetBytes = rmiClientRecv->receiveBytes(packetBytes, &packetLen);
		fflush(NULL);
		if (packetBytes != NULL) { // If there is method bytes
			//IoTRMIUtil::printBytes(packetBytes, packetLen, false);
			//packetBytesPtr = &packetBytes;
			int packetType = getPacketType(packetBytes);
			if (packetType == IoTRMIUtil::METHOD_TYPE) {
				rmiComm->methodQueue.enqueue(packetBytes, packetLen);
			} else if (packetType == IoTRMIUtil::RET_VAL_TYPE) {
				rmiComm->returnQueue.enqueue(packetBytes, packetLen);
			} else {
				// TODO: We need to log error message when we come to running this using IoTSlave
				// TODO: Beware that using "cout" in the process will kill it (as IoTSlave is loaded in Sentinel)
				cerr << "IoTRMICommClient: Packet type is unknown: " << packetType << endl;
				exit(1);
			}
		}
		packetBytes = NULL;
		packetLen = 0;
	}
}


// Send return values in bytes to the caller
void IoTRMICommClient::sendReturnObj(void* retObj, string type, char* methodBytes) {

	// Critical section that is used by different objects
	lock_guard<mutex> guard(sendReturnObjMutex);
	// Find the length of return object in bytes
	int retLen = rmiUtil->getTypeSize(type);
	if (retLen == -1) {
		retLen = rmiUtil->getVarTypeSize(type, retObj);
	}
	// Copy the header and object bytes
	int objAndMethIdLen = IoTRMIUtil::OBJECT_ID_LEN + IoTRMIUtil::METHOD_ID_LEN;
	int headerLen = objAndMethIdLen + IoTRMIUtil::PACKET_TYPE_LEN;
	char retAllObjBytes[headerLen+retLen];
	// Copy object and method Id first
	memcpy(retAllObjBytes, methodBytes, objAndMethIdLen);
	// Copy objectId + methodId + packet type in bytes
	char packType[IoTRMIUtil::PACKET_TYPE_LEN];
	IoTRMIUtil::intToByteArray(IoTRMIUtil::RET_VAL_TYPE, packType);
	memcpy(retAllObjBytes + objAndMethIdLen, packType, IoTRMIUtil::PACKET_TYPE_LEN);
	// Copy object into byte array
	char retObjBytes[retLen];
	IoTRMIUtil::getObjectBytes(retObjBytes, retObj, type.c_str());
	memcpy(retAllObjBytes + headerLen, retObjBytes, retLen);
	fflush(NULL);
	rmiClientSend->sendBytes(retAllObjBytes, headerLen+retLen);
	fflush(NULL);
}


// Send return values in bytes to the caller (for more than one object - struct)
void IoTRMICommClient::sendReturnObj(void* retObj[], string type[], int numRet, char* methodBytes) {

	// Critical section that is used by different objects
	lock_guard<mutex> guard(sendReturnObjMutex);
	// Find the length of return object in bytes
	int retLen = returnLength(retObj, type, numRet);
	// Copy the header and object bytes
	int objAndMethIdLen = IoTRMIUtil::OBJECT_ID_LEN + IoTRMIUtil::METHOD_ID_LEN;
	int headerLen = objAndMethIdLen + IoTRMIUtil::PACKET_TYPE_LEN;
	char retAllObjBytes[headerLen+retLen];
	// Copy object and method Id first
	memcpy(retAllObjBytes, methodBytes, objAndMethIdLen);
	// Copy objectId + methodId + packet type in bytes
	char packType[IoTRMIUtil::PACKET_TYPE_LEN];
	IoTRMIUtil::intToByteArray(IoTRMIUtil::RET_VAL_TYPE, packType);
	memcpy(retAllObjBytes + objAndMethIdLen, packType, IoTRMIUtil::PACKET_TYPE_LEN);
	// Copy object into byte array
	char retObjBytes[retLen];
	returnToBytes(retObj, type, retObjBytes, numRet);
	memcpy(retAllObjBytes + headerLen, retObjBytes, retLen);
	fflush(NULL);
	rmiClientSend->sendBytes(retAllObjBytes, headerLen+retLen);
	fflush(NULL);
}


// Calls a method remotely by passing in parameters and getting a return object
void IoTRMICommClient::remoteCall(int objectId, int methodId, string paramCls[], 
		void* paramObj[], int numParam) {

	// Critical section that is used by different objects
	lock_guard<mutex> guard(remoteCallMutex);
	// Send input parameters
	int len = methodLength(paramCls, paramObj, numParam);
	char method[len];
	methodToBytes(objectId, methodId, paramCls, paramObj, method, numParam);
	// Send bytes
	fflush(NULL);
	rmiClientSend->sendBytes(method, len);
	fflush(NULL);

}
#endif


