#include <iostream>
#include <string>
#include "TestClassInterface_Skeleton.hpp"
#include "TestClass.hpp"

using namespace std;

int main(int argc, char *argv[])
{
	TestClassInterface *tc;
	TestClassInterface_Skeleton *tcSkel;

	int port = 5010;
	//tc = new TestClass(3, 5.0, "7911");
	tc = new TestClassProfiling();
	string callbackAddress = "128.195.204.132";
	tcSkel = new TestClassInterface_Skeleton(tc, callbackAddress, port);

	delete tc;
	delete tcSkel;
	return 0;
}
