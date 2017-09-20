/** Class IoTRMICommServer implements the server side
 *  of IoTRMIComm class.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2017-01-28
 */
#ifndef _IOTRMICOMMSERVER_HPP__
#define _IOTRMICOMMSERVER_HPP__

#include <iostream>
#include <string>
#include <atomic>
#include <limits>
#include <thread>
#include <mutex>

#include "IoTRMIComm.hpp"

using namespace std;


class IoTRMICommServer final : public IoTRMIComm {
	public:
		IoTRMICommServer(int _portSend, int _portRecv, bool* _bResult);
		~IoTRMICommServer();
		// Public methods
		void		sendReturnObj(void* retObj, string type, char* methodBytes);
		void		sendReturnObj(void* retObj[], string type[], int numRet, char* methodBytes);
		void		remoteCall(int objectId, int methodId, string paramCls[], void* paramObj[], int numParam);

	private:
		IoTSocketServer		*rmiServerSend;
		IoTSocketServer		*rmiServerRecv;

		// Private methods
		void		waitForConnectionOnServerRecv();
		void		waitForConnectionOnServerSend();
		void		waitForPackets(IoTRMICommServer* rmiComm);
};


// Constructor
IoTRMICommServer::IoTRMICommServer(int _portSend, int _portRecv, bool* _bResult) : IoTRMIComm() {

	rmiServerSend = new IoTSocketServer(_portSend, _bResult);
	rmiServerRecv = new IoTSocketServer(_portRecv, _bResult);
	thread th1 (&IoTRMICommServer::waitForConnectionOnServerSend, this);
	thread th2 (&IoTRMICommServer::waitForConnectionOnServerRecv, this);
	th1.join();
	th2.join();
	thread th3 (&IoTRMICommServer::waitForPackets, this, this);
	th3.detach();
}


// Destructor
IoTRMICommServer::~IoTRMICommServer() {

	// Clean up
	if (rmiServerSend != NULL) {	
		delete rmiServerSend;
		rmiServerSend = NULL;		
	}
	if (rmiServerRecv != NULL) {	
		delete rmiServerRecv;
		rmiServerRecv = NULL;		
	}
}


void IoTRMICommServer::waitForConnectionOnServerRecv() {

	cout << "Wait on connection ServerRecv!" << endl;
	rmiServerRecv->connect();
	cout << "Connected on connection ServerRecv!" << endl;
}


void IoTRMICommServer::waitForConnectionOnServerSend() {

	cout << "Wait on connection ServerSend!" << endl;
	rmiServerSend->connect();
	cout << "Connected on connection ServerSend!" << endl;
}


void IoTRMICommServer::waitForPackets(IoTRMICommServer* rmiComm) {

	char* packetBytes = NULL;
	int packetLen = 0;
	//cout << "Starting waitForPacketsOnServer()" << endl;
	while(true) {
		fflush(NULL);
		packetBytes = rmiComm->rmiServerRecv->receiveBytes(packetBytes, &packetLen);
		fflush(NULL);
		if (packetBytes != NULL) { // If there is method bytes
			//IoTRMIUtil::printBytes(packetBytes, packetLen, false);
			int packetType = IoTRMIComm::getPacketType(packetBytes);
			if (packetType == IoTRMIUtil::METHOD_TYPE) {
				rmiComm->methodQueue.enqueue(packetBytes, packetLen);
			} else if (packetType == IoTRMIUtil::RET_VAL_TYPE) {
				rmiComm->returnQueue.enqueue(packetBytes, packetLen);
			} else {
				// TODO: We need to log error message when we come to running this using IoTSlave
				// TODO: Beware that using "cout" in the process will kill it (as IoTSlave is loaded in Sentinel)
				cerr << "IoTRMICommServer: Packet type is unknown: " << packetType << endl;
				exit(1);
			}
		}
		packetBytes = NULL;
		packetLen = 0;
	}
}


// Send return values in bytes to the caller
void IoTRMICommServer::sendReturnObj(void* retObj, string type, char* methodBytes) {

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
	IoTRMIUtil::printBytes(retAllObjBytes, headerLen+retLen, false);
	rmiServerSend->sendBytes(retAllObjBytes, headerLen+retLen);
	fflush(NULL);
}


// Send return values in bytes to the caller (for more than one object - struct)
void IoTRMICommServer::sendReturnObj(void* retObj[], string type[], int numRet, char* methodBytes) {

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
	rmiServerSend->sendBytes(retAllObjBytes, headerLen+retLen);
	fflush(NULL);
}


// Calls a method remotely by passing in parameters and getting a return object
void IoTRMICommServer::remoteCall(int objectId, int methodId, string paramCls[], 
		void* paramObj[], int numParam) {

	// Critical section that is used by different objects
	lock_guard<mutex> guard(remoteCallMutex);
	// Send input parameters
	int len = methodLength(paramCls, paramObj, numParam);
	char method[len];
	methodToBytes(objectId, methodId, paramCls, paramObj, method, numParam);
	// Send bytes
	fflush(NULL);
	rmiServerSend->sendBytes(method, len);
	fflush(NULL);

}
#endif


