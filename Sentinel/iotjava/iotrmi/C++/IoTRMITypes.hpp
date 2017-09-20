/** Class IoTRMITypes is a class that provides type translations.
 *  <p>
 *  It stores C++ and Java types.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2016-10-18
 */
#ifndef _IOTRMITYPES_HPP__
#define _IOTRMITYPES_HPP__

#include <iostream>
#include <string>
#include <map>
#include <vector>

using namespace std;

class IoTRMITypes final {

	public:
		/* Public constants */
		const static int NUM_PRIMITIVES = 19;
		const static int NUM_NONPRIMITIVES = 6;

		/**
		 * Primitive data types in Java
		 */
		const static string primitivesJava[NUM_PRIMITIVES];


		/**
		 * Primitive data types in C++ to map the primitives list
		 */
		const static string primitivesCplus[NUM_PRIMITIVES];


		/**
		 * Primitive sizes in Java/C++
		 */
		const static int primitivesSizes[NUM_PRIMITIVES];


		/**
		 * Non-primitive Java data types
		 */
		const static string nonPrimitivesJava[NUM_NONPRIMITIVES];


		/**
		 * Non-primitive C++ data types
		 */
		const static string nonPrimitivesCplus[NUM_NONPRIMITIVES];


		/* Methods */
		static void		arraysToMap(map<string,string> &srcMap, const vector<string> arrKey, 
			const vector<string> arrVal);
		static void		arraysToMap(map<string,int> &srcMap, const vector<string> arrKey, 
			const vector<int> arrVal);
		static void 	arraysToMap(map<void*,void*> &srcMap, const vector<void*> arrKey, 
			const vector<void*> arrVal);
};


const string IoTRMITypes::primitivesJava[IoTRMITypes::NUM_PRIMITIVES] = {

	"byte",			// 1 byte
	"Byte",			// 1 byte
	"short",		// 2 bytes
	"Short",		// 2 bytes
	"int",			// 4 bytes
	"Integer",		// 4 bytes
	"long",			// 8 bytes
	"Long",			// 8 bytes
	"float",		// 4 bytes
	"Float",		// 4 bytes
	"double",		// 8 bytes
	"Double",		// 8 bytes
	"boolean",		// 1 bytes
	"Boolean",		// 1 bytes
	"char",			// 2 bytes
	"Character",	// 2 bytes
	"string",		// indefinite
	"String",		// indefinite
	"void"			// 0 byte
};


const string IoTRMITypes::primitivesCplus[IoTRMITypes::NUM_PRIMITIVES] = {

	"char",			// 1 byte
	"char",			// 1 byte
	"short",		// 2 bytes
	"short",		// 2 bytes
	"int",			// 4 bytes
	"int",			// 4 bytes
	"int64_t",		// 8 bytes
	"int64_t",		// 8 bytes
	"float",		// 4 bytes
	"float",		// 4 bytes
	"double",		// 8 bytes
	"double",		// 8 bytes
	"bool",			// 1 byte
	"bool",			// 1 byte
	"char",			// 2 byte
	"char",			// 2 byte
	"string",		// indefinite
	"string",		// indefinite
	"void"			// 0 byte
};


const int IoTRMITypes::primitivesSizes[IoTRMITypes::NUM_PRIMITIVES] = {

	1, 1, 2, 2, 4, 4, 8, 8, 4, 4, 8, 8, 1, 1, 2, 2, -1, -1, 0
};


const string IoTRMITypes::nonPrimitivesJava[IoTRMITypes::NUM_NONPRIMITIVES] = {

	"Set",
	"HashSet",
	"Map",
	"HashMap",
	"List",
	"ArrayList"
};


const string IoTRMITypes::nonPrimitivesCplus[IoTRMITypes::NUM_NONPRIMITIVES] = {

	"set",
	"unordered_set",
	"map",
	"unordered_map",
	"list",
	"list"
};


/**================
 * Helper functions
 **================
 */
// Inserting array members into a Map object
// that maps arrKey to arrVal objects
void IoTRMITypes::arraysToMap(map<string,string> &srcMap, const vector<string> arrKey, 
	const vector<string> arrVal) {

	for(int i = 0; i < arrKey.size(); i++) {

		srcMap[arrKey[i]] = arrVal[i];
	}
}


// Inserting array members into a Map object
// that maps arrKey to arrVal objects
void IoTRMITypes::arraysToMap(map<string,int> &srcMap, const vector<string> arrKey, 
	const vector<int> arrVal) {

	for(int i = 0; i < arrKey.size(); i++) {

		srcMap[arrKey[i]] = arrVal[i];
	}
}


// Inserting array members into a Map object
// that maps arrKey to arrVal objects
void IoTRMITypes::arraysToMap(map<void*,void*> &srcMap, const vector<void*> arrKey, 
	const vector<void*> arrVal) {

	for(int i = 0; i < arrKey.size(); i++) {

		srcMap[arrKey[i]] = arrVal[i];
	}
}

#endif
