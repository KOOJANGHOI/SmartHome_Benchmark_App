#include <iostream>
#include <string>
#include "TestClass_Skeleton.hpp"
#include "TestClass.hpp"

using namespace std;

int main(int argc, char *argv[])
{
	TestClassInterface *tc;
	TestClass_Skeleton *tcSkel;
	try {
		int port = 5010;
		tc = new TestClass(3, 5.0, "7911");
		tcSkel = new TestClass_Skeleton(tc, port);
	} catch(const exception&) {
		return EXIT_FAILURE;
	}
	//tcSkel->waitRequestInvokeMethod();

	delete tc;
	delete tcSkel;
	return 0;
}
