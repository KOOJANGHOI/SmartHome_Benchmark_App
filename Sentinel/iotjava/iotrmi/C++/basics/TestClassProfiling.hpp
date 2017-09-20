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

class TestClassProfiling : public TestClassInterface {
	public:
		TestClassProfiling();

		vector<char>		getByteArray(vector<char> in);

};


TestClassProfiling::TestClassProfiling() {

}

// Arrays
vector<char> TestClass::getByteArray(vector<char> in) {

	return in;
}


#endif

