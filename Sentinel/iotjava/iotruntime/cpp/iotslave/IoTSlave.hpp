#ifndef _IOTSLAVE_HPP__
#define _IOTSLAVE_HPP__

#include <iostream>
#include <fstream>
#include <vector>
#include <thread>
#include <cstdlib>

#include <dlfcn.h>		// For dlopen, dlsym, etc.

#include "IoTSet.hpp"
#include "IoTDeviceAddress.hpp"
#include "IoTRelation.hpp"
#include "Socket.cpp"

/** Class IoTSlave is a communication class
 *  that interacts with IoTSlave.java to set up C++
 *  objects in Sentinel.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2017-01-12
 */
// Enumeration of master-slave communication codes
enum IoTCommCode {

	ACKNOWLEDGED,
	CREATE_DRIVER_OBJECT,
	CREATE_OBJECT,
	CREATE_MAIN_OBJECT,
	CREATE_NEW_IOTSET,
	CREATE_NEW_IOTRELATION,
	END_TRANSFER,
	END_SESSION,
	GET_ADD_IOTSET_OBJECT,
	GET_DEVICE_IOTSET_OBJECT,
	GET_IOTSET_OBJECT,
	GET_IOTRELATION_FIRST_OBJECT,
	GET_IOTRELATION_SECOND_OBJECT,
	GET_ZB_DEV_IOTSET_OBJECT,
	INVOKE_INIT_METHOD,
	REINITIALIZE_IOTSET_FIELD,
	REINITIALIZE_IOTRELATION_FIELD,
	TRANSFER_FILE,
	
};


// Defining generic function pointers for 
// create, destroy, and init functions of each class object
typedef void* create_t(void**);
typedef void destroy_t(void*);
typedef void init_t(void*);


class IoTSlave final {

	private:
		// Constants
		const static int RCVBUFSIZE = 1024;			// Size of receive buffer
		const static int SKELPARAMSIZE = 3;			// Number of params for skeleton
		const static int STUBPARAMSIZE = 5;			// Number of params for stub
		const static string FILEPATH;    			// File path
		const static string FILEEXT;    			// File extension
		const static string SOEXT;	    			// Shared object (.so) extension
		const static string STRINGCLASS;   			// String class
		const static string INTCLASS;   			// Int class
		const static string CREATEFUNCTION;			// The create function in class
		const static string DESTROYFUNCTION; 		// The destroy function in class
		const static string INITFUNCTION;	 		// The init function in class
		const static string LOCALHOST;		 		// String "localhost"
		const static string SHELL;			 		// String ".sh"

		// Class properties
		string serverAddress;
		int serverPort;
		string hostAddress;
		string mainObjectName;
		string objectName;
		string objectClassName;
		string objectInterfaceName;
		string objectSkelClass;		// Need to send from Java IoTSlave: sMessage.getObjectInterfaceName() + SKEL_CLASS_SUFFIX
		string objectStubClass;		// Need to send from Java IoTSlave: sMessage.getObjectStubInterfaceName() + STUB_CLASS_SUFFIX
		int objectRegPort;
		int objectStubPort;
		string objectFieldName;						// Field name that is going to be initialized with IoTSet or IoTRelation
		unordered_set<void*>* isetObject;			// Set of object
		IoTSet<void*>* iotsetObject;				// IoTSet of object
		vector<IoTSet<void*>*> vecIoTSet;			// IoTSet of object
		void* irelFirstObject;							// First object of IoTRelation
		void* irelSecondObject;							// Second object of IoTRelation
		unordered_multimap<void*,void*>* irelObject;	// Relation of object
		IoTRelation<void*,void*>* iotrelObject;			// IoTRelation of objects
		vector<IoTRelation<void*,void*>*> vecIoTRel;	// IoTRelation of object

		TCPSocket* socket;
		ofstream log;						// Log the messages
		vector<string> args;				// Hold the arguments for constructor (in string format)
		vector<string> argClasses;			// Hold the argument classes
		//bool isDriverObject;				// Set to true if this is IoTSlave instance for a driver object
		void* objMainCls;					// Main class handler, i.e. driver or controller object
		void* objSkelCls;					// Skeleton handler
		void* objStubCls;					// Stub handler
		unordered_map<string, void*> mapObjNameStub;	// Mapping between object name and stub
		// Object handlers
		create_t* create_object;
		destroy_t* destroy_object;
		init_t* init_object;

	public:
		// Constructors
		IoTSlave(string _serverAddress, int _serverPort, string _objectName);
		~IoTSlave();
		// Class methods
		string getServerAddress();
		int getServerPort();
		string getObjectName();
		void sendInteger(int intSend);
		int recvInteger();
		void sendString(string strSend);
		string recvString();
		string recvFile();
		void unzipFile(string fileName);
		// Main loop
		void sendAck();
		bool recvEndTransfer();
		void commIoTMaster();
		void createObject();		// Create driver object
		void createMainObject();	// Create main object
		void createNewIoTSet();
		void createNewIoTRelation();
		void getDeviceIoTSetObject();
		void getIoTRelationFirstObject();
		void getIoTRelationSecondObject();
		void reinitializeIoTSetField();
		void reinitializeIoTRelationField();
		void getIoTSetObject();
		void invokeInitMethod();
		void createDriverObject();
		void transferFile();

	private:
		// Private helper functions
		int* byteToInt(int* result, char* bytes);
		char* intToByteArray(int i, char* bytes);
		char* recvIter(char* recvBuffer, int recvLen);
		char* recvFileIter(char* recvBuffer, int recvLen);
		void* getObjectConverted(void* retObj, string object, string objectClass);
		void openFile(string fileName);
		void writeToFile(string logMsg);
		void closeFile();
		void getObjectHandler(string objectClassName);
		void instantiateMainObject();
		void instantiateDriverObject();
		void instantiateSkelObject();
		void instantiateStubObject();
		void runInitObject(IoTSlave* iotslave);
		void getIoTSetRelationObject();
		void createStub();
};

// Constant initialization
const string IoTSlave::FILEPATH = "./";
const string IoTSlave::FILEEXT = "_cpp.log";
const string IoTSlave::SOEXT = ".so";
const string IoTSlave::STRINGCLASS = "string";
const string IoTSlave::INTCLASS = "int";
const string IoTSlave::CREATEFUNCTION = "create";
const string IoTSlave::DESTROYFUNCTION = "destroy";
const string IoTSlave::INITFUNCTION = "init";
const string IoTSlave::LOCALHOST = "localhost";
const string IoTSlave::SHELL = ".sh";

#endif
