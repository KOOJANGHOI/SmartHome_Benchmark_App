#ifndef _TESTCLASS_HPP__
#define _TESTCLASS_HPP__

#include <iostream>
#include <thread>
#include <chrono>
#include "TestClassInterface.hpp"
#include "CallBackInterfaceWithCallBack.hpp"
#include "Enum.hpp"
#include "Struct.hpp"

using namespace std;

class TestClass : public TestClassInterface {
	public:
		TestClass();
		TestClass(int _int, float _float, string _string);

		char 				getByte(char in);
		short 				getShort(short in);
		int64_t				getLong(int64_t in);
		float 				getFloat(float in);
		double 				getDouble(double in);
		bool 				getBoolean(bool in);
		char 				getChar(char in);

		vector<char>		getByteArray(vector<char> in);
		vector<short>		getShortArray(vector<short> in);
		vector<int64_t>		getLongArray(vector<int64_t> in);
		vector<float> 		getFloatArray(vector<float> in);
		vector<double> 		getDoubleArray(vector<double> in);
		vector<bool> 		getBooleanArray(vector<bool> in);
		vector<char> 		getCharArray(vector<char> in);

		vector<char>		getByteList(vector<char> in);
		vector<short>		getShortList(vector<short> in);
		vector<int64_t>		getLongList(vector<int64_t> in);
		vector<float> 		getFloatList(vector<float> in);
		vector<double> 		getDoubleList(vector<double> in);
		vector<bool> 		getBooleanList(vector<bool> in);
		vector<char> 		getCharList(vector<char> in);

		// Callbacks
		void				registerCallback(CallBackInterfaceWithCallBack* _cb);
		void				registerCallbackArray(vector<CallBackInterfaceWithCallBack*> _cb);
		void				registerCallbackList(vector<CallBackInterfaceWithCallBack*> _cb);
		void				registerCallbackComplex(int in, vector<CallBackInterfaceWithCallBack*> _cb, double db);
		int					callBack();
		vector<Enum>		handleCallbackEnum(vector<Enum> en, char c, vector<CallBackInterfaceWithCallBack*> _cb);

		// Enum
		Enum 				handleEnum(Enum en);
		vector<Enum>		handleEnumArray(vector<Enum> vecEn);
		vector<Enum>		handleEnumList(vector<Enum> vecEn);
		Enum				handleEnumComplex(Enum en, int i, char c);
		vector<Enum>		handleEnumComplex2(vector<Enum> en, int i, char c);
		vector<Enum>		handleEnumTwo(vector<Enum> en1, vector<Enum> en2);
		vector<Enum>		handleEnumThree(vector<Enum> en1, vector<Enum> en2, vector<Struct> str1, vector<Struct> str2);

		// Struct
		Struct				handleStruct(Struct str);
		vector<Struct>		handleStructArray(vector<Struct> vecStr);
		vector<Struct>		handleStructList(vector<Struct> vecStr);
		Struct				handleStructComplex(int in, char c, Struct str);
		vector<Struct>		handleStructComplex2(int in, char c, vector<Struct> vecStr);
		vector<Struct>		handleStructTwo(vector<Struct> str1, vector<Struct> str2);
		vector<Struct>		handleStructThree(vector<Struct> str1, vector<Struct> str2, vector<Struct> str3);

		vector<Enum>		handleEnumStruct(vector<Enum> en, vector<Struct> str, char c);
		vector<Enum>		handleAll(vector<Enum> en, vector<Struct> str, char c, vector<CallBackInterfaceWithCallBack*> _cb);
		vector<Enum>		handleAllTwo(vector<Enum> en1, vector<Enum> en2, vector<Struct> str1, vector<Struct> str2, char c, 
									vector<CallBackInterfaceWithCallBack*> _cb1, vector<CallBackInterfaceWithCallBack*> _cb2);

		int					getA();
		void				setA(int _int);
		void				setB(float _float);
		void				setC(string _string);
		string				sumArray(vector<string> newA);
		int					setAndGetA(int newA);
		int					setACAndGetA(string newC, int newA);

	private:		
		int										intA;
		float									floatB;
		string									stringC;
		vector<CallBackInterfaceWithCallBack*>	cbvec;
};


TestClass::TestClass() {

	intA = 1;
	floatB = 2;
	stringC = "345";
	// cbvec doesn't need to be initialized again
}


TestClass::TestClass(int _int, float _float, string _string) {

	intA = _int;
	floatB = _float;
	stringC = _string;
	// cbvec doesn't need to be initialized again
}


void TestClass::registerCallbackArray(vector<CallBackInterfaceWithCallBack*> _cb) {

	for (CallBackInterfaceWithCallBack* cb : _cb) {
		cbvec.push_back(cb);
		cout << "Registering callback object in array!" << endl;
	}
}


void TestClass::registerCallbackList(vector<CallBackInterfaceWithCallBack*> _cb) {

	for (CallBackInterfaceWithCallBack* cb : _cb) {
		cbvec.push_back(cb);
		cout << "Registering callback object in list!" << endl;
	}
}


void TestClass::registerCallbackComplex(int in, vector<CallBackInterfaceWithCallBack*> _cb, double db) {

	for (CallBackInterfaceWithCallBack* cb : _cb) {
		cbvec.push_back(cb);
		cout << "Registering callback object in list!" << endl;
	}

	cout << "Integer: " << in << endl;
	cout << "Double: " << db << endl;
}


void TestClass::registerCallback(CallBackInterfaceWithCallBack* _cb) {

	cbvec.push_back(_cb);
	cout << "Registering callback object!" << endl;
}


int TestClass::callBack() {

	int sum = 0;
	for (CallBackInterfaceWithCallBack* cb : cbvec) {
		//cout << "Sum: " << sum << endl;
		sum = sum + cb->printInt();
		cb->needCallback(this);
		//cb->needCallback(this);
		TestClass* tc = new TestClass();
		cb->needCallback(tc);
		//cout << "Sum after: " << sum << endl;
	}
	cout << "About to return sum: " << sum << endl;

	return sum;
}


// Single variables
char TestClass::getByte(char in) {

	return in;
}


short TestClass::getShort(short in) {

	return in;
}


int64_t TestClass::getLong(int64_t in) {

	return in;
}


float TestClass::getFloat(float in) {

	return in;
}


double TestClass::getDouble(double in) {

	return in;
}


bool TestClass::getBoolean(bool in) {

	return in;
}


char TestClass::getChar(char in) {

	return in;
}


// Arrays
vector<char> TestClass::getByteArray(vector<char> in) {

	return in;
}


vector<short> TestClass::getShortArray(vector<short> in) {

	return in;
}


vector<int64_t> TestClass::getLongArray(vector<int64_t> in) {

	return in;
}


vector<float> TestClass::getFloatArray(vector<float> in) {

	return in;
}


vector<double> TestClass::getDoubleArray(vector<double> in) {

	return in;
}


vector<bool> TestClass::getBooleanArray(vector<bool> in) {

	return in;
}


vector<char> TestClass::getCharArray(vector<char> in) {

	return in;
}

// List
vector<char> TestClass::getByteList(vector<char> in) {

	return in;
}


vector<short> TestClass::getShortList(vector<short> in) {

	return in;
}


vector<int64_t> TestClass::getLongList(vector<int64_t> in) {

	return in;
}


vector<float> TestClass::getFloatList(vector<float> in) {

	return in;
}


vector<double> TestClass::getDoubleList(vector<double> in) {

	return in;
}


vector<bool> TestClass::getBooleanList(vector<bool> in) {

	return in;
}


vector<char> TestClass::getCharList(vector<char> in) {

	return in;
}


int TestClass::getA() {

	return intA;
}


void TestClass::setA(int _int) {

	intA = _int;
}


void TestClass::setB(float _float) {

	floatB = _float;
}


void TestClass::setC(string _string) {

	stringC = _string;
}


// Enum
Enum TestClass::handleEnum(Enum en) {

	cout << "Enum: " << en << endl;
	
	return en;
}


vector<Enum> TestClass::handleEnumArray(vector<Enum> vecEn) {

	for (Enum en : vecEn) {
		cout << "Enum: " << en << endl;
	}
	
	return vecEn;
}


vector<Enum> TestClass::handleEnumList(vector<Enum> vecEn) {

	for (Enum en : vecEn) {
		cout << "Enum: " << en << endl;
	}
	
	return vecEn;
}


Enum TestClass::handleEnumComplex(Enum en, int i, char c) {

	cout << "Enum: " << en << endl;
	cout << "Integer: " << i << endl;
	cout << "Char: " << c << endl;
	
	return en;
}


vector<Enum> TestClass::handleEnumComplex2(vector<Enum> vecEn, int in, char c) {

	for (Enum en : vecEn) {
		cout << "Enum: " << en << endl;
	}
	cout << "Integer: " << in << endl;
	cout << "Char: " << c << endl;
	
	return vecEn;
}


vector<Enum> TestClass::handleEnumTwo(vector<Enum> en1, vector<Enum> en2) {

	for (Enum en : en1) {
		cout << "Enum1: " << en << endl;
	}
	for (Enum en : en2) {
		cout << "Enum2: " << en << endl;
	}
	
	return en1;
}


vector<Enum> TestClass::handleEnumThree(vector<Enum> en1, vector<Enum> en2, vector<Struct> str1, vector<Struct> str2) {

	for (Enum en : en1) {
		cout << "Enum1: " << en << endl;
	}
	for (Enum en : en2) {
		cout << "Enum2: " << en << endl;
	}
	
	return en1;
}



// Struct
Struct TestClass::handleStruct(Struct str) {

	cout << "Name: " << str.name << endl;
	cout << "Value: " << str.value << endl;
	cout << "Year: " << str.year << endl;

	Struct test;
	test.name = "Anonymous";
	test.value = 1.33;
	test.year = 2016;
	str = test;

	return str;
}


vector<Struct> TestClass::handleStructArray(vector<Struct> vecStr) {

	for (Struct str : vecStr) {

		cout << "Name: " << str.name << endl;
		cout << "Value: " << str.value << endl;
		cout << "Year: " << str.year << endl;
	}
	Struct test;
	test.name = "Anonymous";
	test.value = 1.33;
	test.year = 2016;
	vecStr.push_back(test);

	return vecStr;
}


vector<Struct> TestClass::handleStructList(vector<Struct> vecStr) {

	for (Struct str : vecStr) {

		cout << "Name: " << str.name << endl;
		cout << "Value: " << str.value << endl;
		cout << "Year: " << str.year << endl;
	}
	Struct test;
	test.name = "Trimananda";
	test.value = 1.33;
	test.year = 2016;
	vecStr.push_back(test);

	return vecStr;
}


vector<Struct> TestClass::handleStructTwo(vector<Struct> str1, vector<Struct> str2) {

	for (Struct str : str1) {

		cout << "Name: " << str.name << endl;
		cout << "Value: " << str.value << endl;
		cout << "Year: " << str.year << endl;
	}

	return str2;
}


vector<Struct> TestClass::handleStructThree(vector<Struct> str1, vector<Struct> str2, vector<Struct> str3) {

	for (Struct str : str1) {

		cout << "Name: " << str.name << endl;
		cout << "Value: " << str.value << endl;
		cout << "Year: " << str.year << endl;
	}

	return str2;
}


Struct TestClass::handleStructComplex(int in, char c, Struct str) {

	cout << "Name: " << str.name << endl;
	cout << "Value: " << str.value << endl;
	cout << "Year: " << str.year << endl;

	cout << "Integer: " << in << endl;
	cout << "Char: " << c << endl;

	Struct test;
	test.name = "Anonymous";
	test.value = 1.33;
	test.year = 2016;
	str = test;

	return str;
}


vector<Struct> TestClass::handleStructComplex2(int in, char c, vector<Struct> vecStr) {

	for (Struct str : vecStr) {
		cout << "Name: " << str.name << endl;
		cout << "Value: " << str.value << endl;
		cout << "Year: " << str.year << endl;
	}

	cout << "Integer: " << in << endl;
	cout << "Char: " << c << endl;

	return vecStr;
}


vector<Enum> TestClass::handleEnumStruct(vector<Enum> en, vector<Struct> str, char c) {

	for (Struct st : str) {
		cout << "Name: " << st.name << endl;
		cout << "Value: " << st.value << endl;
		cout << "Year: " << st.year << endl;
	}

	cout << "Char: " << c << endl;

	return en;
}


vector<Enum> TestClass::handleAll(vector<Enum> en, vector<Struct> str, char c, vector<CallBackInterfaceWithCallBack*> _cb) {

	for (CallBackInterfaceWithCallBack* cb : _cb) {
		cbvec.push_back(cb);
		cout << "Registering callback object in array!" << endl;
	}

	for (Struct st : str) {
		cout << "Name: " << st.name << endl;
		cout << "Value: " << st.value << endl;
		cout << "Year: " << st.year << endl;
	}

	cout << "Char: " << c << endl;

	return en;
}


vector<Enum> TestClass::handleAllTwo(vector<Enum> en1, vector<Enum> en2, vector<Struct> str1, vector<Struct> str2, char c, 
		vector<CallBackInterfaceWithCallBack*> _cb1, vector<CallBackInterfaceWithCallBack*> _cb2) {

	for (CallBackInterfaceWithCallBack* cb : _cb1) {
		cbvec.push_back(cb);
		cout << "Registering callback object in array!" << endl;
	}

	for (Struct st : str1) {
		cout << "Name: " << st.name << endl;
		cout << "Value: " << st.value << endl;
		cout << "Year: " << st.year << endl;
	}

	cout << "Char: " << c << endl;

	return en1;
}


vector<Enum> TestClass::handleCallbackEnum(vector<Enum> en, char c, vector<CallBackInterfaceWithCallBack*> _cb) {

	for (CallBackInterfaceWithCallBack* cb : _cb) {
		cbvec.push_back(cb);
		cout << "Registering callback object in array!" << endl;
	}

	cout << "Char: " << c << endl;

	return en;
}


string TestClass::sumArray(vector<string> newA) {

	string sum = "";
	int len = newA.size();
	for(int c = 0; c < len; c++) {
		sum = sum + newA[c];
	}
	return sum;
}


int TestClass::setAndGetA(int newA) {

	intA = newA;
	return intA;
}


int TestClass::setACAndGetA(string newC, int newA) {

	stringC = newC;
	intA = newA;
	return intA;
}

#endif

