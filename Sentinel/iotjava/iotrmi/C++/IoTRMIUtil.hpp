/** Class IoTRMIUtil provides methods that the upper
 *  layers can use to transport and invoke methods
 *  when using IoTSocket, IoTSocketClient and IoTSocketServer.
 *
 * @author      Rahmadi Trimananda <rtrimana @ uci.edu>
 * @version     1.0
 * @since       2016-10-18
 */
#ifndef _IOTRMIUTIL_HPP__
#define _IOTRMIUTIL_HPP__

#include <stdio.h>
#include <stdint.h>
#include <endian.h>
#include <cxxabi.h>
#include <cstdlib>
#include <memory>
#include <typeinfo>
#include <map>

#include <iostream>
#include <string>
#include <vector>
#include <string.h>

#include "IoTRMITypes.hpp"

using namespace std;

class IoTRMIUtil final {

	public:
		IoTRMIUtil();
		//~IoTRMIUtil();
		
		// Helper functions
		static void		printBytes(char* bytes, const int len, const bool hex);
		static int		hashCode(string str);
		static char*	getHashCodeBytes(string methodSign, char* bytes);
		int 			getTypeSize(string type);
		int				getVarTypeSize(string type, void* paramObj);
		static int		getArrStringLength(vector<string> arrString);
		static int		getByteStringLength(vector<string> arrString);
		
		// Primitives to byte array
		static char*	byteToByteArray(char c, char* bytes);
		static char*	shortToByteArray(short i, char* bytes);
		static char*	intToByteArray(int i, char* bytes);
		static char*	longToByteArray(int64_t i, char* bytes);
		static char*	floatToByteArray(float f, char* bytes);
		static char*	doubleToByteArray(double d, char* bytes);
		static char*	charToByteArray(char c, char* bytes);
		static char*	booleanToByteArray(bool c, char* bytes);
		static char*	stringToByteArray(string c, char* bytes);

		// Byte array to primitives
		static char*	byteArrayToByte(char* result, char* bytes);
		static short*	byteArrayToShort(short* result, char* bytes);
		static int* 	byteArrayToInt(int* result, char* bytes);
		static int64_t*	byteArrayToLong(int64_t* result, char* bytes);
		static float*	byteArrayToFloat(float* result, char* bytes);
		static double* 	byteArrayToDouble(double* result, char* bytes);
		static char*	byteArrayToChar(char* result, char* bytes);
		static bool* 	byteArrayToBoolean(bool* result, char* bytes);
		static string*	byteArrayToString(string* result, char* bytes);
		static string*	byteArrayToString(string* result, char* bytes, int len);

		// Get parameter object from byte array
		static void*	getParamObject(void* retObj, const char* type, char* paramBytes, int len);
		static char*	getObjectBytes(char* retObjBytes, void* obj, const char* type);

		// Arrays to bytes
		static char*	arrByteToByteArray(vector<char> arrByte, char* bytes);
		static char*	arrShortToByteArray(vector<short> arrShort, char* bytes);
		static char*	arrIntToByteArray(vector<int> arrInt, char* bytes);
		static char*	arrLongToByteArray(vector<int64_t> arrInt, char* bytes);
		static char*	arrFloatToByteArray(vector<float> arrFloat, char* bytes);
		static char*	arrDoubleToByteArray(vector<double> arrDouble, char* bytes);
		static char*	arrCharToByteArray(vector<char> arrChar, char* bytes);
		static char*	arrBooleanToByteArray(vector<bool> arrBoolean, char* bytes);
		static char*	arrStringToByteArray(vector<string> arrString, char* bytes);

		// Bytes to array
		static vector<char>*	byteArrayToByteArray(vector<char>* result, char* bytes, int len);
		static vector<short>*	byteArrayToShortArray(vector<short>* result, char* bytes, int len);
		static vector<int>* 	byteArrayToIntArray(vector<int>* result, char* bytes, int len);
		static vector<int64_t>*	byteArrayToLongArray(vector<int64_t>* result, char* bytes, int len);
		static vector<float>*	byteArrayToFloatArray(vector<float>* result, char* bytes, int len);
		static vector<double>* 	byteArrayToDoubleArray(vector<double>* result, char* bytes, int len);
		static vector<char>*	byteArrayToCharArray(vector<char>* result, char* bytes, int len);
		static vector<bool>* 	byteArrayToBooleanArray(vector<bool>* result, char* bytes, int len);
		static vector<string>*	byteArrayToStringArray(vector<string>* result, char* bytes, int len);

		// Aggregator functions
		static char*	getArrayObjectBytes(char* retObjBytes, void* obj, const char* type);
		static void*	getArrayParamObject(void* retObj, const char* type, char* paramBytes, int len);

		// Constants
		const static int 	OBJECT_ID_LEN = 4; 	// 4 bytes = 32 bits
		const static int 	METHOD_ID_LEN = 4; 	// 4 bytes = 32 bits
		const static int 	PACKET_TYPE_LEN = 4;// 4 bytes = 32 bits
		const static int 	PARAM_LEN = 4; 		// 4 bytes = 32 bits (4-byte field that stores the length of the param)
		const static int 	RETURN_LEN = 4;		// 4 bytes = 32 bits (4-byte field that stores the length of the return object)
		const static int 	CHAR_LEN = 2; 		// 2 bytes (we follow Java convention)
		const static int 	BYTE_LEN = 1; 		// 1 byte
		const static int 	BOOL_LEN = 1; 		// 1 byte
		const static int 	METHOD_TYPE = 1;	// Packet type of method
		const static int 	RET_VAL_TYPE = -1;	// Packet type of return value

		// Static containers
		static map<int,void*>* mapStub;		// Map object to its stub ID
		static map<void*,void*>* mapSkel;	// Map object to its skeleton
		static map<void*,int>* mapSkelId;	// Map object to its skeleton ID
		
	private:
		map<string,string>	mapPrimitives;
		map<string,int>		mapPrimitiveSizes;
		map<string,string>	mapNonPrimitives;
};

map<int,void*>* IoTRMIUtil::mapStub = new map<int,void*>();
map<void*,void*>* IoTRMIUtil::mapSkel = new map<void*,void*>();
map<void*,int>* IoTRMIUtil::mapSkelId = new map<void*,int>();


// Constructor
IoTRMIUtil::IoTRMIUtil() {

	// Prepare vectors for inputs
	std::vector<string> primJava (IoTRMITypes::primitivesJava, 
		IoTRMITypes::primitivesJava + sizeof(IoTRMITypes::primitivesJava)/sizeof(string));
	std::vector<string> primCplus (IoTRMITypes::primitivesCplus, 
		IoTRMITypes::primitivesCplus + sizeof(IoTRMITypes::primitivesCplus)/sizeof(string));
	std::vector<int> primSizes (IoTRMITypes::primitivesSizes, 
		IoTRMITypes::primitivesSizes + sizeof(IoTRMITypes::primitivesSizes)/sizeof(int));
	std::vector<string> nonPrimJava (IoTRMITypes::nonPrimitivesJava, 
		IoTRMITypes::nonPrimitivesJava + sizeof(IoTRMITypes::nonPrimitivesJava)/sizeof(string));
	std::vector<string> nonPrimCplus (IoTRMITypes::nonPrimitivesCplus, 
		IoTRMITypes::nonPrimitivesCplus + sizeof(IoTRMITypes::nonPrimitivesCplus)/sizeof(string));


	// Write into maps
	IoTRMITypes::arraysToMap(mapPrimitives, primJava, primCplus);
	IoTRMITypes::arraysToMap(mapPrimitiveSizes, primJava, primSizes); 
	IoTRMITypes::arraysToMap(mapNonPrimitives, nonPrimJava, nonPrimCplus);
}

// *************
//    Helpers
// *************
void IoTRMIUtil::printBytes(char* bytes, const int len, const bool hex) {

	printf("[ ");
	for (int i = 0; i < len; i++) {
		if (hex)	// print in hexadecimal
			printf("%x", bytes[i]);
		else
			printf("%d", bytes[i]);
		if (i < len - 1)
			printf(", ");
	}
	printf(" ]\n");
}


// Return hashCode value 
// This mimics the method Object.hashCode() in Java
int IoTRMIUtil::hashCode(string str)  
{
	int hash = 0;
	int len = str.length();
	char c;
	if (len == 0) 
		return hash;
	
	for (int i = 0; i < len; i++) {
		c = str.at(i);
		hash = (31*hash) + (int) c;
	}

	return hash;
}


char* IoTRMIUtil::getHashCodeBytes(string methodSign, char* bytes) {

	int hash = hashCode(methodSign);
	return intToByteArray(hash, bytes);
}


int IoTRMIUtil::getTypeSize(string type) {

	// Handle the types and find the sizes
	if (mapPrimitiveSizes.find(type) != mapPrimitiveSizes.end())
		return mapPrimitiveSizes.find(type)->second;
	else
		return -1; // Size is unknown
}


// Get variable type size, e.g. strings, arrays, etc.
int IoTRMIUtil::getVarTypeSize(string type, void* paramObj) {

	int paramLen = 0;
	if (type.compare("String") == 0) {
		// Get the length of the string through void* casting to string*
		paramLen = (*(string*)paramObj).length();
	} else if (	(type.compare("String*") == 0) ||
				(type.compare("string*") == 0) ||
				(type.compare("vector<String>") == 0)) {
		paramLen = IoTRMIUtil::getByteStringLength(*(vector<string>*) paramObj);
	} else if ( (type.compare("byte*") == 0) ||
				(type.compare("Byte*") == 0) ||
				(type.compare("vector<Byte>") == 0)) {
		int dataSize = getTypeSize("byte");
		paramLen = (*(vector<char>*) paramObj).size() * dataSize;
	} else if ( (type.compare("short*") == 0) ||
				(type.compare("Short*") == 0) ||
				(type.compare("vector<Short>") == 0)) {
		int dataSize = getTypeSize("short");
		paramLen = (*(vector<short>*) paramObj).size() * dataSize;
	} else if ( (type.compare("int*") == 0) ||
				(type.compare("Integer*") == 0) ||
				(type.compare("vector<Integer>") == 0)) {
		int dataSize = getTypeSize("int");
		paramLen = (*(vector<int>*) paramObj).size() * dataSize;
	} else if ( (type.compare("long*") == 0) ||
				(type.compare("Long*") == 0) ||
				(type.compare("vector<Long>") == 0)) {
		int dataSize = getTypeSize("long");
		paramLen = (*(vector<int64_t>*) paramObj).size() * dataSize;
	} else if ( (type.compare("float*") == 0) ||
				(type.compare("Float*") == 0) ||
				(type.compare("vector<Float>") == 0)) {
		int dataSize = getTypeSize("float");
		paramLen = (*(vector<float>*) paramObj).size() * dataSize;
	} else if ( (type.compare("double*") == 0) ||
				(type.compare("Double*") == 0) ||
				(type.compare("vector<Double>") == 0)) {
		int dataSize = getTypeSize("double");
		paramLen = (*(vector<double>*) paramObj).size() * dataSize;
	} else if ( (type.compare("boolean*") == 0) ||
				(type.compare("Boolean*") == 0) ||
				(type.compare("vector<Boolean>") == 0)) {
		int dataSize = getTypeSize("boolean");
		paramLen = (*(vector<bool>*) paramObj).size() * dataSize;
	} else if ( (type.compare("char*") == 0) ||
				(type.compare("Character*") == 0) ||
				(type.compare("vector<Character>") == 0)) {
		int dataSize = getTypeSize("char");
		paramLen = (*(vector<char>*) paramObj).size() * dataSize;
	} else {
		cerr << "IoTRMIUtil: Unrecognizable type: " << type << endl;
		exit(-1);
	}

	return paramLen;
}


int IoTRMIUtil::getArrStringLength(vector<string> arrString) {

	int len = 0;
	for (string& str : arrString) {
		len = len + str.length();
	}
	return len;
}


int IoTRMIUtil::getByteStringLength(vector<string> arrString) {

	int len = PARAM_LEN + (PARAM_LEN*arrString.size()) + getArrStringLength(arrString);
	return len;
}


// ****************************
//    Parameters Translation
// ****************************

// Getting parameter object based on received byte array
void* IoTRMIUtil::getParamObject(void* retObj, const char* type, char* paramBytes, int len) {

	if (strcmp(type, "b") == 0 ||
		strcmp(type, "byte") == 0) {
		retObj = (void*) byteArrayToByte((char*) retObj, paramBytes);
	} else if ( strcmp(type, "s") == 0 ||
				strcmp(type, "short") == 0) {
		retObj = (void*) byteArrayToShort((short*) retObj, paramBytes);
	} else if ( strcmp(type, "i") == 0 ||
				strcmp(type, "int") == 0) {
		retObj = (void*) byteArrayToInt((int*) retObj, paramBytes);
	} else if ( strcmp(type, "l") == 0 ||
				strcmp(type, "long") == 0) {
		retObj = (void*) byteArrayToLong((int64_t*) retObj, paramBytes);
	} else if ( strcmp(type, "f") == 0 ||
				strcmp(type, "float") == 0) {
		retObj = (void*) byteArrayToFloat((float*) retObj, paramBytes);
	} else if ( strcmp(type, "d") == 0 ||
				strcmp(type, "double") == 0) {
		retObj = (void*) byteArrayToDouble((double*) retObj, paramBytes);
	} else if ( strcmp(type, "b") == 0 ||
				strcmp(type, "boolean") == 0) {
		retObj = (void*) byteArrayToBoolean((bool*) retObj, paramBytes);
	} else if ( strcmp(type, "c") == 0 ||
				strcmp(type, "char") == 0) {
		retObj = (void*) byteArrayToChar((char*) retObj, paramBytes);
	} else if ( strcmp(type, "Ss") == 0 ||
				strcmp(type, "String") == 0) {
		retObj = (void*) byteArrayToString((string*) retObj, paramBytes, len);
	} else if ( string(type).find("*") != string::npos) {
		// This is an array type, i.e. vector
		retObj = getArrayParamObject(retObj, type, paramBytes, len);
	} else if ( (string(type).find("<") != string::npos) &&
				(string(type).find(">") != string::npos)) {
		// This is a vector/list type
		retObj = getArrayParamObject(retObj, type, paramBytes, len);
	} else {
		cerr << "IoTRMIUtil: Unrecognizable type: " << type << endl;
		exit(-1);
	}

	return retObj;
}


// Get array of objects from byte array
void* IoTRMIUtil::getArrayParamObject(void* retObj, const char* type, char* paramBytes, int len) {

	if ((strcmp(type, "byte*") == 0) ||
		(strcmp(type, "Byte*") == 0) ||
		(strcmp(type, "vector<Byte>") == 0)) {
		retObj = byteArrayToByteArray((vector<char>*) retObj, paramBytes, len);
	} else if ( (strcmp(type, "short*") == 0) ||
				(strcmp(type, "Short*") == 0) ||
				(strcmp(type, "vector<Short>") == 0)) {
		retObj = byteArrayToShortArray((vector<short>*) retObj, paramBytes, len);
	} else if ( (strcmp(type, "int*") == 0) ||
				(strcmp(type, "Integer*") == 0) ||
				(strcmp(type, "vector<Integer>") == 0)) {
		retObj = byteArrayToIntArray((vector<int>*) retObj, paramBytes, len);
	} else if ( (strcmp(type, "long*") == 0) ||
				(strcmp(type, "Long*") == 0) ||
				(strcmp(type, "vector<Long>") == 0)) {
		retObj = byteArrayToLongArray((vector<int64_t>*) retObj, paramBytes, len);
	} else if ( (strcmp(type, "float*") == 0) ||
				(strcmp(type, "Float*") == 0) ||
				(strcmp(type, "vector<Float>") == 0)) {
		retObj = byteArrayToFloatArray((vector<float>*) retObj, paramBytes, len);
	} else if ( (strcmp(type, "double*") == 0) ||
				(strcmp(type, "Double*") == 0) ||
				(strcmp(type, "vector<Double>") == 0)) {
		retObj = byteArrayToDoubleArray((vector<double>*) retObj, paramBytes, len);
	} else if ( (strcmp(type, "boolean*") == 0) ||
				(strcmp(type, "Boolean*") == 0) ||
				(strcmp(type, "vector<Boolean>") == 0)) {
		retObj = byteArrayToBooleanArray((vector<bool>*) retObj, paramBytes, len);
	} else if ( (strcmp(type, "char*") == 0)      ||
				(strcmp(type, "Character*") == 0) ||
				(strcmp(type, "vector<Character>") == 0)) {
		retObj = byteArrayToCharArray((vector<char>*) retObj, paramBytes, len);
	} else if ( (strcmp(type, "String*") == 0) ||
				(strcmp(type, "vector<String>") == 0)) {
		retObj = byteArrayToStringArray((vector<string>*) retObj, paramBytes, len);
	} else {
		cerr << "IoTRMIUtil: Unrecognizable type: " << type << endl;
		exit(-1);	
	}

	return retObj;
}


// Getting byte array based on parameter and its type
char* IoTRMIUtil::getObjectBytes(char* retObjBytes, void* obj, const char* type) {

	if (strcmp(type, "b") == 0 ||
		strcmp(type, "byte") == 0) {
		retObjBytes = byteToByteArray(*((char*) obj), retObjBytes);		
	} else if ( strcmp(type, "s") == 0 ||
				strcmp(type, "short") == 0) {
		retObjBytes = shortToByteArray(*((short*) obj), retObjBytes);
	} else if ( strcmp(type, "i") == 0 ||
				strcmp(type, "int") == 0) {
		retObjBytes = intToByteArray(*((int*) obj), retObjBytes);
	} else if ( strcmp(type, "l") == 0 ||
				strcmp(type, "long") == 0) {
		retObjBytes = longToByteArray(*((int64_t*) obj), retObjBytes);
	} else if ( strcmp(type, "f") == 0 ||
				strcmp(type, "float") == 0) {
		retObjBytes = floatToByteArray(*((float*) obj), retObjBytes);
	} else if ( strcmp(type, "d") == 0 ||
				strcmp(type, "double") == 0) {
		retObjBytes = doubleToByteArray(*((double*) obj), retObjBytes);
	} else if ( strcmp(type, "b") == 0 ||
				strcmp(type, "boolean") == 0) {
		retObjBytes = booleanToByteArray(*((bool*) obj), retObjBytes);
	} else if ( strcmp(type, "c") == 0 ||
				strcmp(type, "char") == 0) {
		retObjBytes = charToByteArray(*((char*) obj), retObjBytes);
	} else if ( strcmp(type, "Ss") == 0 ||
				strcmp(type, "String") == 0) {
		retObjBytes = stringToByteArray(*((string*) obj), retObjBytes);
	} else if ( string(type).find("*") != string::npos) {
	// This is an array type, i.e. vector
		retObjBytes = getArrayObjectBytes(retObjBytes, obj, type);
	} else if ( (string(type).find("<") != string::npos) &&
				(string(type).find(">") != string::npos)) {
	// This is a vector/list type
		retObjBytes = getArrayObjectBytes(retObjBytes, obj, type);
	} else {
		cerr << "IoTRMIUtil: Unrecognizable type: " << type << endl;
		exit(-1);
	}

	return retObjBytes;
}


// Getting byte array for arrays of primitives
char* IoTRMIUtil::getArrayObjectBytes(char* retObjBytes, void* obj, const char* type) {

	if ((strcmp(type, "byte*") == 0) ||
		(strcmp(type, "Byte*") == 0) ||
		(strcmp(type, "vector<Byte>") == 0)) {
		retObjBytes = arrByteToByteArray(*((vector<char>*) obj), retObjBytes);
	} else if ( (strcmp(type, "short*") == 0) ||
				(strcmp(type, "Short*") == 0) ||
				(strcmp(type, "vector<Short>") == 0)) {
		retObjBytes = arrShortToByteArray(*((vector<short>*) obj), retObjBytes);
	} else if ( (strcmp(type, "int*") == 0) ||
				(strcmp(type, "Integer*") == 0) ||
				(strcmp(type, "vector<Integer>") == 0)) {
		retObjBytes = arrIntToByteArray(*((vector<int>*) obj), retObjBytes);
	} else if ( (strcmp(type, "long*") == 0) ||
				(strcmp(type, "Long*") == 0) ||
				(strcmp(type, "vector<Long>") == 0)) {
		retObjBytes = arrLongToByteArray(*((vector<int64_t>*) obj), retObjBytes);
	} else if ( (strcmp(type, "float*") == 0) ||
				(strcmp(type, "Float*") == 0) ||
				(strcmp(type, "vector<Float>") == 0)) {
		retObjBytes = arrFloatToByteArray(*((vector<float>*) obj), retObjBytes);
	} else if ( (strcmp(type, "double*") == 0) ||
				(strcmp(type, "Double*") == 0) ||
				(strcmp(type, "vector<Double>") == 0)) {
		retObjBytes = arrDoubleToByteArray(*((vector<double>*) obj), retObjBytes);
	} else if ( (strcmp(type, "boolean*") == 0) ||
				(strcmp(type, "Boolean*") == 0) ||
				(strcmp(type, "vector<Boolean>") == 0)) {
		retObjBytes = arrBooleanToByteArray(*((vector<bool>*) obj), retObjBytes);
	} else if ( (strcmp(type, "char*") == 0) ||
				(strcmp(type, "Character*") == 0) ||
				(strcmp(type, "vector<Character>") == 0)) {
		retObjBytes = arrCharToByteArray(*((vector<char>*) obj), retObjBytes);
	} else if ( (strcmp(type, "String*") == 0) ||
				(strcmp(type, "vector<String>") == 0)) {
		retObjBytes = arrStringToByteArray(*((vector<string>*) obj), retObjBytes);
	} else {
		cerr << "IoTRMIUtil: Unrecognizable type: " << type << endl;
		exit(-1);
	}

	return retObjBytes;
}


// Conversions
// Array handlers - we use vector data type and not traditional arrays
// Array to bytes
char* IoTRMIUtil::arrByteToByteArray(vector<char> arrByte, char* bytes) {

	int pos = 0;
	for (char chr : arrByte) {
		char tmpBytes[BYTE_LEN];
		byteToByteArray(chr, tmpBytes);
		memcpy(bytes + pos, tmpBytes, BYTE_LEN);
		pos = pos + BYTE_LEN;
	}

	return bytes;
}


char* IoTRMIUtil::arrShortToByteArray(vector<short> arrShort, char* bytes) {

	int pos = 0;
	for (short& sht : arrShort) {
		char tmpBytes[sizeof(short)];
		shortToByteArray(sht, tmpBytes);
		memcpy(bytes + pos, tmpBytes, sizeof(short));
		pos = pos + sizeof(short);
	}

	return bytes;
}


char* IoTRMIUtil::arrIntToByteArray(vector<int> arrInt, char* bytes) {

	int pos = 0;
	for (int& in : arrInt) {
		char tmpBytes[sizeof(int)];
		intToByteArray(in, tmpBytes);
		memcpy(bytes + pos, tmpBytes, sizeof(int));
		pos = pos + sizeof(int);
	}

	return bytes;
}


char* IoTRMIUtil::arrLongToByteArray(vector<int64_t> arrLong, char* bytes) {

	int pos = 0;
	for (int64_t& lng : arrLong) {
		char tmpBytes[sizeof(int64_t)];
		longToByteArray(lng, tmpBytes);
		memcpy(bytes + pos, tmpBytes, sizeof(int64_t));
		pos = pos + sizeof(int64_t);
	}

	return bytes;
}


char* IoTRMIUtil::arrFloatToByteArray(vector<float> arrFloat, char* bytes) {

	int pos = 0;
	for (float& flt : arrFloat) {
		char tmpBytes[sizeof(float)];
		floatToByteArray(flt, tmpBytes);
		memcpy(bytes + pos, tmpBytes, sizeof(float));
		pos = pos + sizeof(float);
	}

	return bytes;
}


char* IoTRMIUtil::arrDoubleToByteArray(vector<double> arrDouble, char* bytes) {

	int pos = 0;
	for (double& dbl : arrDouble) {
		char tmpBytes[sizeof(double)];
		doubleToByteArray(dbl, tmpBytes);
		memcpy(bytes + pos, tmpBytes, sizeof(double));
		pos = pos + sizeof(double);
	}

	return bytes;
}


char* IoTRMIUtil::arrCharToByteArray(vector<char> arrChar, char* bytes) {

	int pos = 0;
	for (char& chr : arrChar) {
		char tmpBytes[CHAR_LEN];
		charToByteArray(chr, tmpBytes);
		memcpy(bytes + pos, tmpBytes, CHAR_LEN);
		pos = pos + CHAR_LEN;
	}

	return bytes;
}


char* IoTRMIUtil::arrBooleanToByteArray(vector<bool> arrBoolean, char* bytes) {

	int pos = 0;
	for (bool bl : arrBoolean) {
		char tmpBytes[BOOL_LEN];
		booleanToByteArray(bl, tmpBytes);
		memcpy(bytes + pos, tmpBytes, BOOL_LEN);
		pos = pos + BOOL_LEN;
	}

	return bytes;
}


char* IoTRMIUtil::arrStringToByteArray(vector<string> arrString, char* bytes) {

	int pos = 0;
	char strArrLenBytes[PARAM_LEN];
	intToByteArray(arrString.size(), strArrLenBytes);
	memcpy(bytes, strArrLenBytes, PARAM_LEN);
	pos = pos + PARAM_LEN;
	for (string& str : arrString) {

		// Copy string length
		int strLen = str.length();
		char strLenBytes[PARAM_LEN];
		intToByteArray(strLen, strLenBytes);
		memcpy(bytes + pos, strLenBytes, PARAM_LEN);
		pos = pos + PARAM_LEN;
		// Copy string
		char strBytes[strLen];
		stringToByteArray(str, strBytes);
		memcpy(bytes + pos, strBytes, strLen);
		pos = pos + strLen;
	}

	return bytes;
}


// Bytes to array
vector<char>* IoTRMIUtil::byteArrayToByteArray(vector<char>* result, char* bytes, int len) {

	// Single element bytes
	char elmt[BYTE_LEN];
	// Prepare vector
	int arrLen = len/BYTE_LEN;
	for(int i = 0; i < arrLen; i++) {
		int offset = i * BYTE_LEN;
		memcpy(elmt, bytes + offset, BYTE_LEN);
		char res;
		byteArrayToByte(&res, elmt);
		result->push_back(res);
	}

	return result;
}


vector<short>* IoTRMIUtil::byteArrayToShortArray(vector<short>* result, char* bytes, int len) {

	// Single element bytes
	char elmt[sizeof(short)];
	// Prepare vector
	int arrLen = len/sizeof(short);
	for(int i = 0; i < arrLen; i++) {
		int offset = i * sizeof(short);
		memcpy(elmt, bytes + offset, sizeof(short));
		short res = 0;
		byteArrayToShort(&res, elmt);
		result->push_back(res);
	}

	return result;
}


vector<int>* IoTRMIUtil::byteArrayToIntArray(vector<int>* result, char* bytes, int len) {

	// Single element bytes
	char elmt[sizeof(int)];
	// Prepare vector
	int arrLen = len/sizeof(int);
	for(int i = 0; i < arrLen; i++) {
		int offset = i * sizeof(int);
		memcpy(elmt, bytes + offset, sizeof(int));
		int res = 0;
		byteArrayToInt(&res, elmt);
		result->push_back(res);
	}

	return result;
}


vector<int64_t>* IoTRMIUtil::byteArrayToLongArray(vector<int64_t>* result, char* bytes, int len) {

	// Single element bytes
	char elmt[sizeof(int64_t)];
	// Prepare vector
	int arrLen = len/sizeof(int64_t);
	for(int i = 0; i < arrLen; i++) {
		int offset = i * sizeof(int64_t);
		memcpy(elmt, bytes + offset, sizeof(int64_t));
		int64_t res = 0;
		byteArrayToLong(&res, elmt);
		result->push_back(res);
	}

	return result;
}


vector<float>* IoTRMIUtil::byteArrayToFloatArray(vector<float>* result, char* bytes, int len) {

	// Single element bytes
	char elmt[sizeof(float)];
	// Prepare vector
	int arrLen = len/sizeof(float);
	for(int i = 0; i < arrLen; i++) {
		int offset = i * sizeof(float);
		memcpy(elmt, bytes + offset, sizeof(float));
		float res = 0;
		byteArrayToFloat(&res, elmt);
		result->push_back(res);
	}

	return result;
}


vector<double>* IoTRMIUtil::byteArrayToDoubleArray(vector<double>* result, char* bytes, int len) {

	// Single element bytes
	char elmt[sizeof(double)];
	// Prepare vector
	int arrLen = len/sizeof(double);
	for(int i = 0; i < arrLen; i++) {
		int offset = i * sizeof(double);
		memcpy(elmt, bytes + offset, sizeof(double));
		double res = 0;
		byteArrayToDouble(&res, elmt);
		result->push_back(res);
	}

	return result;
}


vector<char>* IoTRMIUtil::byteArrayToCharArray(vector<char>* result, char* bytes, int len) {

	// Single element bytes
	char elmt[CHAR_LEN];
	// Prepare vector
	int arrLen = len/CHAR_LEN;
	for(int i = 0; i < arrLen; i++) {
		int offset = i * CHAR_LEN;
		memcpy(elmt, bytes + offset, CHAR_LEN);
		char res;
		byteArrayToChar(&res, elmt);
		result->push_back(res);
	}

	return result;
}


vector<bool>* IoTRMIUtil::byteArrayToBooleanArray(vector<bool>* result, char* bytes, int len) {

	// Single element bytes
	char elmt[BOOL_LEN];
	// Prepare vector
	int arrLen = len/BOOL_LEN;
	for(int i = 0; i < arrLen; i++) {
		int offset = i * BOOL_LEN;
		memcpy(elmt, bytes + offset, BOOL_LEN);
		bool res = false;
		byteArrayToBoolean(&res, elmt);
		result->push_back(res);
	}

	return result;
}


vector<string>*	IoTRMIUtil::byteArrayToStringArray(vector<string>* result, char* bytes, int len) {

	// Format of bytes: | array length | length #1 | string #1 | length #2 | string #2 | ...
	// Get string array length
	int pos = 0;
	char strArrLenBytes[PARAM_LEN];
	memcpy(strArrLenBytes, bytes, PARAM_LEN);
	int strArrLen = 0;
	byteArrayToInt(&strArrLen, strArrLenBytes);
	pos = pos + PARAM_LEN;
	// Extract array of strings
	for(int i = 0; i < strArrLen; i++) {

		// Extract string length
		char strLenBytes[PARAM_LEN];
		memcpy(strLenBytes, bytes + pos, PARAM_LEN);
		int strLen = 0;
		byteArrayToInt(&strLen, strLenBytes);
		pos = pos + PARAM_LEN;
		// Extract string
		char strBytes[strLen];
		memcpy(strBytes, bytes + pos, strLen);
		pos = pos + strLen;
		string tmpStr = "";
		// Note: Somehow we need to instantiate the string
		// 		with the length here although we are passing
		//		an array of bytes with an exact length
		byteArrayToString(&tmpStr, strBytes, strLen);
		result->push_back(tmpStr);
	}

	return result;
}


// Conversions
// Primitives to byte array
char* IoTRMIUtil::byteToByteArray(char c, char* bytes) {

	// Just copy the char into char*
	bytes[0] = c;

	return bytes;
}


char* IoTRMIUtil::shortToByteArray(short s, char* bytes) {

	short sInvert = htobe16(s);
	//short sInvert = htons(s);
	memcpy(bytes, &sInvert, sizeof(short));

	return bytes;
}


char* IoTRMIUtil::intToByteArray(int i, char* bytes) {

	int iInvert = htobe32(i);
	//int iInvert = htonl(i);
	memcpy(bytes, &iInvert, sizeof(int));

	return bytes;
}


char* IoTRMIUtil::longToByteArray(int64_t l, char* bytes) {

	int64_t lInvert = htobe64(l);
	memcpy(bytes, &lInvert, sizeof(int64_t));

	return bytes;
}


char* IoTRMIUtil::floatToByteArray(float f, char* bytes) {

	// Copy to int to allow the usage of htobeXX() functions
	int i = 0;
	memcpy(&i, &f, sizeof(float));
	int iInvert = htobe32(i);
	memcpy(bytes, &iInvert, sizeof(int));
	
	return bytes;
}


char* IoTRMIUtil::doubleToByteArray(double d, char* bytes) {

	// Copy to int to allow the usage of htobeXX() functions
	int64_t i = 0;
	memcpy(&i, &d, sizeof(double));
	int64_t iInvert = htobe64(i);
	memcpy(bytes, &iInvert, sizeof(int64_t));
	
	return bytes;
}


char* IoTRMIUtil::charToByteArray(char c, char* bytes) {

	// We need 2 bytes to accommodate Java char type, whose size is 2
	bytes[0] = 0;
	bytes[1] = c;

	return bytes;
}


char* IoTRMIUtil::booleanToByteArray(bool b, char* bytes) {

	bytes[0] = (b) ? 1 : 0;
	return bytes;
}


char* IoTRMIUtil::stringToByteArray(string str, char* bytes) {

	strcpy(bytes, str.c_str());
	return bytes;
}


// Conversions
// Byte array to primitives
short* IoTRMIUtil::byteArrayToShort(short* result, char* bytes) {

	short s = 0;
	memcpy(&s, bytes, sizeof(short));
	//short result = be16toh(s);
	*result = be16toh(s);

	return result;
}


int* IoTRMIUtil::byteArrayToInt(int* result, char* bytes) {

	int i = 0;
	memcpy(&i, bytes, sizeof(int));
	*result = be32toh(i);

	return result;
}


int64_t* IoTRMIUtil::byteArrayToLong(int64_t* result, char* bytes) {

	int64_t l = 0;
	memcpy(&l, bytes, sizeof(int64_t));
	*result = be64toh(l);

	return result;
}


float* IoTRMIUtil::byteArrayToFloat(float* result, char* bytes) {

	// Copy to int to allow the usage of beXXtoh() functions
	int i = 0;
	memcpy(&i, bytes, sizeof(int));
	int iInvert = be32toh(i);
	memcpy(result, &iInvert, sizeof(float));

	return result;
}


double* IoTRMIUtil::byteArrayToDouble(double* result, char* bytes) {

	// Copy to int to allow the usage of beXXtoh() functions
	int64_t i = 0;
	memcpy(&i, bytes, sizeof(int64_t));
	int64_t iInvert = be64toh(i);
	memcpy(result, &iInvert, sizeof(double));

	return result;
}


char* IoTRMIUtil::byteArrayToByte(char* result, char* bytes) {

	*result = bytes[0];
	return result;
}


char* IoTRMIUtil::byteArrayToChar(char* result, char* bytes) {

	*result = bytes[1];
	return result;
}


bool* IoTRMIUtil::byteArrayToBoolean(bool* result, char* bytes) {

	*result = (bytes[0]) ? true : false;
	return result;
}


string* IoTRMIUtil::byteArrayToString(string* result, char* bytes) {

	*result = string(bytes);
	return result;
}


string* IoTRMIUtil::byteArrayToString(string* result, char* bytes, int strLen) {

	*result = string(bytes, strLen);
	return result;
}


#endif
