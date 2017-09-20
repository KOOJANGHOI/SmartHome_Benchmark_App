#ifndef _TESTCLASS_HPP__
#define _TESTCLASS_HPP__

#include <iostream>
#include <thread>
#include <chrono>
#include "TestClassInterface.hpp"
#include "StructC.hpp"

using namespace std;

class TestClass : public TestClassInterface {
	public:
		TestClass();
		TestClass(int _int, float _float, string _string);
		//~TestClass();

		void				setA(int _int);
		void				setB(float _float);
		void				setC(string _string);
		string				sumArray(vector<string> newA);
		//int64_t				sumArray(vector<int> newA);
		int					setAndGetA(int newA);
		int					setACAndGetA(string newC, int newA);
		void				registerCallback(CallBackInterface* _cb);
		void				registerCallback(vector<CallBackInterface*> _cb);
		int					callBack();
		vector<data>		handleStruct(vector<data> vecData);
		vector<EnumC>		handleEnum(vector<EnumC> vecEn);

		void				thread1();
		void				thread2();

	private:		
		int							intA;
		float						floatB;
		string						stringC;
		CallBackInterface 			*cb;
		vector<CallBackInterface*>	cbvec;

};


TestClass::TestClass() {

	intA = 1;
	floatB = 2;
	stringC = "345";
	cb = NULL;
	// cbvec doesn't need to be initialized again
}


TestClass::TestClass(int _int, float _float, string _string) {

	intA = _int;
	floatB = _float;
	stringC = _string;
	cb = NULL;
	// cbvec doesn't need to be initialized again
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


string TestClass::sumArray(vector<string> newA) {

	string sum = "";
	int len = newA.size();
	for(int c = 0; c < len; c++) {
		sum = sum + newA[c];
	}
	return sum;
}


/*int64_t TestClass::sumArray(vector<int> newA) {

	int64_t sum = 0;
	int len = newA.size();
	for(int c = 0; c < len; c++) {
		sum = sum + newA[c];
	}
	return sum;
}*/


int TestClass::setAndGetA(int newA) {

	intA = newA;
	return intA;
}


int TestClass::setACAndGetA(string newC, int newA) {

	stringC = newC;
	intA = newA;
	return intA;
}


void TestClass::registerCallback(CallBackInterface* _cb) {

	cb = _cb;
}


void TestClass::registerCallback(vector<CallBackInterface*> _cb) {

	for (CallBackInterface* cb : _cb) {
		cbvec.push_back(cb);
		cout << "Registering callback object!" << endl;
	}
}


vector<data> TestClass::handleStruct(vector<data> vecData) {

	for (data dat : vecData) {

		cout << "Name: " << dat.name << endl;
		cout << "Value: " << dat.value << endl;
		cout << "Year: " << dat.year << endl;
	}
	data newData;
	newData.name = "Anonymous";
	newData.value = 1.33;
	newData.year = 2016;
	vecData.push_back(newData);

	return vecData;
}


vector<EnumC> TestClass::handleEnum(vector<EnumC> vecEn) {

	for (EnumC en : vecEn) {
		cout << "Enum: " << en << endl;
	}
	
	return vecEn;
}


//int TestClass::callBack() {
//	return cb.printInt();
//}

void TestClass::thread1() {

	CallBackInterface* cb = cbvec[0];
	for(int i = 0; i < 10; i++) {
		cb->printInt();
		this_thread::sleep_for(chrono::seconds(1));
	}	
}

void TestClass::thread2() {

	CallBackInterface* cb = cbvec[1];
	for(int i = 0; i < 10; i++) {
		cb->printInt();
		this_thread::sleep_for(chrono::seconds(1));
	}	
}

int TestClass::callBack() {

	int sum = 0;
	for (CallBackInterface* cb : cbvec) {
		sum = sum + cb->printInt();
	}

	return sum;
/*	thread th1 (&TestClass::thread1, this);
	thread th2 (&TestClass::thread2, this);

	th1.join();
	th2.join();

	return 1;*/
}

#endif

