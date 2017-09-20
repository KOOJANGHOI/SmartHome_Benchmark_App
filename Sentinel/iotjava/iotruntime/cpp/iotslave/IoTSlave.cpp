#include <iostream>
#include <fstream>

#include "IoTSlave.hpp"

IoTSlave::IoTSlave(string _serverAddress, int _serverPort, string _objectName) {

	//isDriverObject = false;		// Default to false
	serverAddress = _serverAddress;
	serverPort = _serverPort;
	objectName = _objectName;
	socket = new TCPSocket(serverAddress, serverPort);
	openFile(objectName);
	writeToFile("IoTSlave object created! Connection established!");
}


IoTSlave::~IoTSlave() {

	if (socket != NULL) {
		delete socket;
		socket = NULL;
	}
	/*if (objMainCls != NULL) {
		delete objMainCls;
		objMainCls = NULL;
	}
	if (objSkelCls != NULL) {
		delete objSkelCls;
		objSkelCls = NULL;
	}*/
	for (IoTSet<void*>* iotset : vecIoTSet) {
		delete iotset;
		iotset = NULL;
	}
	closeFile();
}


// Private helper functions
int* IoTSlave::byteToInt(int* result, char* bytes) {

	int i = 0;
	memcpy(&i, bytes, sizeof(int));
	*result = be32toh(i);

	return result;
}


char* IoTSlave::intToByteArray(int i, char* bytes) {

	int iInvert = htobe32(i);
	memcpy(bytes, &iInvert, sizeof(int));

	return bytes;
}


void* IoTSlave::getObjectConverted(void* retObj, string object, string objectClass) {

	// Returning new objects in heap - so we need to delete them afterwards
	if (objectClass.compare(STRINGCLASS) == 0) {
		string* retStr = new string(object);
		retObj = retStr;
	} else if (objectClass.compare(INTCLASS) == 0) {
		int* retInt = new int(atoi(object.c_str()));
		retObj = retInt;
	} else	// return NULL if class is not identifiable
		return NULL;

	return retObj;
}


// Factoring out iteration
char* IoTSlave::recvIter(char* recvBuffer, int recvLen) {

    int bytesReceived = 0;              // Bytes read on each recv()
    int totalBytesReceived = 0;         // Total bytes read

	while (totalBytesReceived < recvLen) {
		// Receive up to the buffer size bytes from the sender
		if ((bytesReceived = (socket->recv(recvBuffer, RCVBUFSIZE))) <= 0) {
			string errMsg = "IoTSlave: Unable to read!";
			cerr << errMsg << endl;
			writeToFile(errMsg);
			exit(1);
		}
		totalBytesReceived += bytesReceived;     // Keep tally of total bytes
	}

	return recvBuffer;
}


// Factoring out iteration
char* IoTSlave::recvFileIter(char* recvBuffer, int recvLen) {

    int bytesReceived = 0;              // Bytes read on each recv()
    int totalBytesReceived = 0;         // Total bytes read

	while (totalBytesReceived < recvLen) {
		// Receive up to the buffer size bytes from the sender
		if ((bytesReceived = (socket->recv(recvBuffer, recvLen))) <= 0) {
			string errMsg = "IoTSlave: Unable to read!";
			cerr << errMsg << endl;
			writeToFile(errMsg);
			exit(1);
		}
		totalBytesReceived += bytesReceived;     // Keep tally of total bytes
	}

	return recvBuffer;
}


void IoTSlave::openFile(string fileName) {

	log.open(FILEPATH + fileName + FILEEXT);
}


void IoTSlave::writeToFile(string logMsg) {

	log << "IoTSlave: " << logMsg << endl;
}


void IoTSlave::closeFile() {

	log.close();
}


void IoTSlave::getObjectHandler(string objectClassName) {

	// Object handling
	string strObj = FILEPATH + objectClassName + SOEXT;
	void* handle = dlopen (strObj.c_str(), RTLD_LAZY);
	if (!handle) {
		fputs (dlerror(), stderr);
		writeToFile("Error handling object!");
		exit(1);
	}
	writeToFile("Object handled!");
	// Create handler
	string createFunction = CREATEFUNCTION + objectClassName;
	create_object = (create_t*) dlsym(handle, createFunction.c_str());
	const char* dlsym_error = dlerror();
    if (dlsym_error) {
        cerr << "Cannot load symbol create: " << dlsym_error << '\n';
		writeToFile("Cannot load symbol create!");
        exit(1);
    }
	writeToFile("Object factory created for " + objectClassName);
	// Destroy handler
	string destroyFunction = DESTROYFUNCTION + objectClassName;
    destroy_object = (destroy_t*) dlsym(handle, destroyFunction.c_str());
    dlsym_error = dlerror();
    if (dlsym_error) {
        cerr << "Cannot load symbol destroy: " << dlsym_error << '\n';
		writeToFile("Cannot load symbol destroy!");
        exit(1);
    }
	writeToFile("Object destroyer created for " + objectClassName);
	// Create initializer
	string initFunction = INITFUNCTION + objectClassName;
    init_object = (init_t*) dlsym(handle, initFunction.c_str());
    dlsym_error = dlerror();
    if (dlsym_error) {
        cerr << "Cannot load symbol init: " << dlsym_error << '\n';
		writeToFile("Cannot load symbol init!");
        exit(1);
    }
	writeToFile("Object initializer created for " + objectClassName);
}


// Run init_object function
void IoTSlave::runInitObject(IoTSlave* iotslave) {

	iotslave->init_object(iotslave->objMainCls);
}


// Instantiate main object!
// Use handler obtained by getObjectHandler() and instantiate object!
void IoTSlave::instantiateMainObject() {

	// IoTSet + IoTRelation objects
	int paramSize = vecIoTSet.size() + vecIoTRel.size();
	void* params[paramSize];
	int j = 0;
	for(int i=0; i<vecIoTSet.size(); i++) {
		params[j] = vecIoTSet[i]; j++;
	}
	writeToFile("Vector IoTSet size: " + to_string(vecIoTSet.size()));
	for(int i=0; i<vecIoTRel.size(); i++) {
		params[j] = vecIoTRel[i]; j++;
	}
	writeToFile("Vector IoTRelation size: " + to_string(vecIoTRel.size()));
	objMainCls = create_object(params);
	writeToFile("Object created for " + mainObjectName);
	init_object(objMainCls);
	//thread th1 (&IoTSlave::runInitObject, this, this);
	//th1.detach();
	//thread th1 (&IoTSlave::runInitObject, this, this);
	//th1.join();
	writeToFile("Initialized object " + mainObjectName);
}


// Instantiate driver object!
// Use handler obtained by getObjectHandler() and instantiate object!
void IoTSlave::instantiateDriverObject() {

	// IoTDeviceAddress + other arguments
	int paramSize = vecIoTSet.size() + args.size();
	void* params[paramSize];
	for(int i=0; i<vecIoTSet.size(); i++) {
		params[i] = vecIoTSet[i];	// Just the first object is taken in this case
	}
	writeToFile("Vector IoTSet size: " + to_string(vecIoTSet.size()));
	writeToFile("Arg size: " + to_string(args.size()));
	int countArg = vecIoTSet.size();	// Start from after the address set
	// Iterate over arguments
	for(int i=0; i<args.size(); i++) {
		params[countArg] = getObjectConverted(params[countArg], args[i], argClasses[i]);
		countArg++; 
	}
	objMainCls = create_object(params);
	// Delete unused object after conversion and instantiation
	for(int i=1; i<paramSize; i++) {
		if (argClasses[i-1].compare(STRINGCLASS) == 0) {		
			delete (string*) params[i];
		} else if (argClasses[i-1].compare(INTCLASS) == 0)
			delete (int*) params[i];
	}		
	writeToFile("Object created for " + objectClassName);
}


// Use handler obtained by getObjectHandler() and instantiate skeleton object!
void IoTSlave::instantiateSkelObject() {

	void* params[SKELPARAMSIZE];
	params[0] = objMainCls;
	params[1] = &objectStubPort;
	params[2] = &objectRegPort;
	writeToFile("Skeleton Object " + objectSkelClass + " created for " + objectClassName);
	// After this, this slave needs to be killed using "pkill IoTSlave" because it's waiting in an infinite while-loop
	objSkelCls = create_object(params);
}


// Use handler obtained by getObjectHandler() and instantiate stub object!
void IoTSlave::instantiateStubObject() {

	void* params[STUBPARAMSIZE];
	params[0] = &objectStubPort;
	params[1] = &objectRegPort;
	params[2] = &hostAddress;
	int rev = 0;
	params[3] = &rev;
	bool result = false;
	params[4] = &result;
	writeToFile("Stub Object " + objectStubClass + " created for " + objectClassName);
	writeToFile("Success 1!");
	objStubCls = create_object(params);
	writeToFile("Success 2!");
}


// Public methods
string IoTSlave::getServerAddress() {

	return serverAddress;
}


int IoTSlave::getServerPort() {

	return serverPort;
}


string IoTSlave::getObjectName() {

	return objectName;
}


void IoTSlave::sendInteger(int intSend) {

	char charInt[sizeof(int)];
	// Convert int to byte array and fix endianness
	intToByteArray(intSend, charInt);
	// Send the length first
	void* toSend = charInt;
	socket->send(toSend, sizeof(int));
}


int IoTSlave::recvInteger() {

	int toBeReceived = sizeof(int);
	char recvInt[sizeof(int)];			// Normally 4 bytes

	// Receive and iterate until complete
	//writeToFile("Receiving Integer! Size: " + to_string(toBeReceived));
	recvIter(recvInt, toBeReceived);

	int retVal = 0;
	byteToInt(&retVal, recvInt);

	return retVal;
}


void IoTSlave::sendString(string strSend) {

	// Send the length first
	int strLen = strSend.length();
	sendInteger(strLen);

	// Send the string
	char* chStrSend = new char[strLen];
	strcpy(chStrSend, strSend.c_str());
	void* toSend = chStrSend;
	socket->send(toSend, strLen);
	// Avoid memory leak
	delete[] chStrSend;
}


string IoTSlave::recvString() {

	// Get the length of string first
	int strLen = recvInteger();
	char* recvStr = new char[strLen];

	// Receive and iterate until complete
	//writeToFile("Receiving String! Size: " + to_string(strLen));
	recvIter(recvStr, strLen);

	string retVal(recvStr, strLen);
	delete[] recvStr;

	return retVal;
}


// Receive file from IoTMaster
void IoTSlave::transferFile() {

	string fileName = recvFile(); sendAck();
	//unzipFile(fileName);
}


void IoTSlave::unzipFile(string fileName) {

	// Unzip file (what we are sending is a zipped file)
	// TODO: perhaps we need to replace this with libzip or zlib later	
	writeToFile("Unzipping file!");
	string chmodCmd = FILEPATH + fileName + SHELL;
	//std::system(chmodCmd.c_str());
	thread th1 (std::system, chmodCmd.c_str());
	th1.detach();
	writeToFile("Finished unzipping file!");
}


string IoTSlave::recvFile() {

	// Get the length of string first
	string fileName = recvString(); sendAck();
	int fileLen = recvInteger(); sendAck();
	writeToFile("Receiving file " + fileName + " with length " + to_string(fileLen) + " bytes...");
	char* recvFil = new char[fileLen];
	// Receive and iterate until complete
	recvFileIter(recvFil, fileLen);
	// Write into file
	ofstream fileStream;
	fileStream.open(FILEPATH + fileName);
	if (!fileStream) {
		writeToFile("Error opening file: " + FILEPATH + fileName);
		exit(1);
	}
	fileStream.write(recvFil, fileLen);
	delete[] recvFil;
	fileStream.close();
	// TODO: Experimental
	//string chmodCmd = FILEPATH + fileName + SHELL;
	//execv(chmodCmd.c_str(), 0);
	return fileName;
}


// Create a driver object, e.g. LifxLightBulb
void IoTSlave::createObject() {

	writeToFile("Creating a driver object now...");
	// Receiving object info
	objectName = recvString(); sendAck();
	writeToFile("=> Driver object name: " + objectName);
	objectClassName = recvString(); sendAck();
	writeToFile("=> Driver object class name: " + objectClassName);
	objectInterfaceName = recvString(); sendAck();
	writeToFile("=> Driver object interface name: " + objectInterfaceName);
	objectSkelClass = recvString(); sendAck();
	writeToFile("=> Driver object skeleton class name: " + objectSkelClass);
	objectRegPort = recvInteger(); sendAck();
	writeToFile("=> Driver object registry port: " + to_string(objectRegPort));
	objectStubPort = recvInteger(); sendAck();
	writeToFile("=> Driver object stub port: " + to_string(objectStubPort));
	int numOfArgs = recvInteger(); sendAck();
	writeToFile("=> Number of args: " + to_string(numOfArgs));
	for (int i = 0; i < numOfArgs; i++) {
		string arg = recvString(); sendAck();
		args.push_back(arg);
		writeToFile("==> Got argument: " + arg);
	}
	for (int i = 0; i < numOfArgs; i++) {
		string argClass = recvString(); sendAck();
		argClasses.push_back(argClass);
		writeToFile("==> Got argument class: " + argClass);
	}
	// We are just receiving object information here
	// Instantiation will be done when IoTDeviceAddress has been sent
}


// Create a new IoTSet object to hold objects
void IoTSlave::createNewIoTSet() {

	objectFieldName = recvString(); sendAck();
	// Instantiating new IoTSet object
	isetObject = new unordered_set<void*>();
	writeToFile("Creating new IoTSet for field: " + objectFieldName);
}


// Get IoTDeviceAddress object reference and put it inside IoTSet object
void IoTSlave::getDeviceIoTSetObject() {

	writeToFile("Getting IoTDeviceAddress... ");
	// Get the IoTDeviceAddress info
	hostAddress = recvString(); sendAck();
	writeToFile("=> Host address: " + hostAddress);
	int sourcePort = recvInteger(); sendAck();
	writeToFile("=> Source port: " + to_string(sourcePort));
	int destPort = recvInteger(); sendAck();
	writeToFile("=> Destination port: " + to_string(destPort));
	bool sourcePortWildCard = (bool) recvInteger(); sendAck();
	writeToFile("=> Is source port wild card? " + to_string(sourcePortWildCard));
	bool destPortWildCard = (bool) recvInteger(); sendAck();
	writeToFile("=> Is destination port wild card? " + to_string(destPortWildCard));
	// Create IoTDeviceAddress	
	IoTDeviceAddress* objDeviceAddress = new IoTDeviceAddress(hostAddress, sourcePort, destPort, 
		sourcePortWildCard, destPortWildCard);
	// Insert it into isetObject!
	isetObject->insert(objDeviceAddress);
	writeToFile("=> Inserting IoTDeviceAddress into set...");
	writeToFile("==> Now we have " + to_string(isetObject->size()) + " object(s)!");
	// Set flag to true;
	//isDriverObject = true;
}


void IoTSlave::createStub() {
	// Create Stub object
	unordered_map<string,void*>::const_iterator itr = mapObjNameStub.find(objectName);
	if (itr != mapObjNameStub.end()) {	// Stub has been created earlier
		writeToFile("=> Stub has been created! Getting back reference...");
		objStubCls = itr->second;
	} else {	// Instantiate a new stub and map it
		writeToFile("=> Stub has not been created! Creating a new stub...");
		getObjectHandler(objectStubClass);
		instantiateStubObject();
		mapObjNameStub.insert(make_pair(objectName,objStubCls));
		writeToFile("=> Map has: " + to_string(mapObjNameStub.size()) + " members");
	}
}


// Get IoTSet object content reference and put it inside IoTSet object
// This is basically the stub objects
void IoTSlave::getIoTSetObject() {

	writeToFile("Getting IoTSet object... ");
	getIoTSetRelationObject();
	createStub();
	// Insert it into isetObject!
	isetObject->insert(objStubCls);
	writeToFile("=> Inserting stub object into set...");
	writeToFile("==> Now we have " + to_string(isetObject->size()) + " object(s)!");
}


// Reinitialize IoTSet field!
void IoTSlave::reinitializeIoTSetField() {

	writeToFile("Reinitialize IoTSet field...");
	iotsetObject = new IoTSet<void*>(isetObject);
	// Collect IoTSet field first in a vector
	vecIoTSet.push_back(iotsetObject);

}


// Instantiate driver object
void IoTSlave::createDriverObject() {

	// Instantiate driver object
	getObjectHandler(objectClassName);
	instantiateDriverObject();
	// Instantiate skeleton object
	getObjectHandler(objectSkelClass);
	instantiateSkelObject();
}


// Create a new IoTRelation object to hold objects
void IoTSlave::createNewIoTRelation() {

	objectFieldName = recvString(); sendAck();
	// Instantiating new IoTSet object
	irelObject = new unordered_multimap<void*,void*>();
	writeToFile("Creating new IoTRelation for field: " + objectFieldName);
}


// Get IoTRelation object
void IoTSlave::getIoTSetRelationObject() {

	hostAddress = recvString(); sendAck();
	writeToFile("=> Host address: " + hostAddress);
	objectName = recvString(); sendAck();
	writeToFile("=> Driver object name: " + objectName);
	objectClassName = recvString(); sendAck();
	writeToFile("=> Driver object class name: " + objectClassName);
	objectInterfaceName = recvString(); sendAck();
	writeToFile("=> Driver object interface name: " + objectInterfaceName);
	objectStubClass = recvString(); sendAck();
	writeToFile("=> Driver object stub class name: " + objectStubClass);
	objectRegPort = recvInteger(); sendAck();
	writeToFile("=> Driver object registry port: " + to_string(objectRegPort));
	objectStubPort = recvInteger(); sendAck();
	writeToFile("=> Driver object stub port: " + to_string(objectStubPort));
}


// Get the first object of IoTRelation
void IoTSlave::getIoTRelationFirstObject() {

	writeToFile("Getting IoTRelation first object... ");
	getIoTSetRelationObject();
	createStub();
	// Hold the first object of IoTRelation
	irelFirstObject = objStubCls;
	writeToFile("=> Holding first stub object...");
}


// Get the second object of IoTRelation
void IoTSlave::getIoTRelationSecondObject() {

	writeToFile("Getting IoTRelation second object... ");
	getIoTSetRelationObject();
	createStub();
	// Hold the first object of IoTRelation
	irelSecondObject = objStubCls;
	writeToFile("=> Holding second stub object...");
	pair<void*,void*>* iotrelPair = new pair<void*,void*>(irelFirstObject, irelSecondObject);
	writeToFile("=> Creating a pair of stub objects and inserting into IoTRelation object...");
	irelObject->insert(*iotrelPair);
}


// Reinitialize IoTRelation
void IoTSlave::reinitializeIoTRelationField() {

	writeToFile("Reinitialize IoTRelation field...");
	iotrelObject = new IoTRelation<void*,void*>(irelObject);
	// Collect IoTSet field first in a vector
	vecIoTRel.push_back(iotrelObject);
}


// Invoke init() method in main controller
void IoTSlave::invokeInitMethod() {

	writeToFile("Invoke init() method for: " + mainObjectName);
	// Instantiate main controller object
	getObjectHandler(mainObjectName);
	instantiateMainObject();

}


// Create a main object, e.g. Lifxtest
void IoTSlave::createMainObject() {

	mainObjectName = recvString(); sendAck();
	writeToFile("Creating main object: " + mainObjectName);
	// Just receive the name of the class object here
	// We will instantiate the object after we get the set/relation objects
}


void IoTSlave::sendAck() {

	int codeAck = (int) ACKNOWLEDGED;
	sendInteger(codeAck);
}


bool IoTSlave::recvEndTransfer() {

	int codeEndTransfer = (int) END_TRANSFER;
	int recvCode = recvInteger();
	if (recvCode == codeEndTransfer)
		return true;
	return false;
}


void IoTSlave::commIoTMaster() {

	writeToFile("Starting main loop...");
	// Main iteration/loop
	while(true) {
		IoTCommCode message = (IoTCommCode) recvInteger(); 
		writeToFile("Message: " + to_string(message));
		sendAck();
		
		switch(message) {

			case CREATE_OBJECT:
				createObject();
				break;

			case TRANSFER_FILE:
				transferFile();
				break;

			case CREATE_MAIN_OBJECT:
				createMainObject();
				break;

			case CREATE_NEW_IOTSET:
				createNewIoTSet();
				break;

			case CREATE_NEW_IOTRELATION:
				createNewIoTRelation();
				break;

			case GET_IOTSET_OBJECT:
				getIoTSetObject();
				break;

			case GET_IOTRELATION_FIRST_OBJECT:
				getIoTRelationFirstObject();
				break;

			case GET_IOTRELATION_SECOND_OBJECT:
				getIoTRelationSecondObject();
				break;

			case REINITIALIZE_IOTSET_FIELD:
				reinitializeIoTSetField();
				break;

			case REINITIALIZE_IOTRELATION_FIELD:
				reinitializeIoTRelationField();
				break;

			case GET_DEVICE_IOTSET_OBJECT:
				getDeviceIoTSetObject();
				break;

			case GET_ZB_DEV_IOTSET_OBJECT:
				//getZBDevIoTSetObject();
				break;

			case GET_ADD_IOTSET_OBJECT:
				//getAddIoTSetObject();
				break;

			case INVOKE_INIT_METHOD:
				invokeInitMethod();
				break;

			case CREATE_DRIVER_OBJECT:
				createDriverObject();
				break;

			case END_SESSION:
				// END of session
				goto ENDLOOP;
				break;

			default:
				break;
		}
	}
	ENDLOOP:
	writeToFile("End of loop!");
}


int main(int argc, char *argv[]) {

	string serverAddress = argv[1];
	char* servPort = argv[2];
	int serverPort = atoi(servPort);
	string strObjName = argv[3];
	IoTSlave *iotSlave = new IoTSlave(serverAddress, serverPort, strObjName);
	iotSlave->sendAck();
	iotSlave->commIoTMaster();
	
	return 0;
}
