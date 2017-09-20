#include <iostream>
#include <string>
#include "TestClass.hpp"
#include "CallBack.hpp"

using namespace std;

int main(int argc, char *argv[])
{

	TestClassInterface *tc = new TestClass();
	cout << "Return value: " << tc->setAndGetA(123) << endl;
	cout << "Return value: " << tc->setACAndGetA("string", 123) << endl;
	vector<string> input;
	input.push_back("123");
	input.push_back("456");
	input.push_back("987");
	/*vector<int> input;
	input.push_back(123);
	input.push_back(456);
	input.push_back(987);*/

	cout << "Return value: " << tc->sumArray(input) << endl;
	delete tc;

	vector<CallBackInterface*> test;
	CallBackInterface *cb1 = new CallBack(12);
	CallBackInterface *cb2 = new CallBack(22);
	CallBackInterface *cb3 = new CallBack(32);
	test.push_back(cb1);
	test.push_back(cb2);
	test.push_back(cb3);
	for (CallBackInterface *cb : test) {
		cout << "Test print: " << cb->printInt() << endl;
	}

	delete cb1;
	delete cb2;
	delete cb3;

	return 0;
}
