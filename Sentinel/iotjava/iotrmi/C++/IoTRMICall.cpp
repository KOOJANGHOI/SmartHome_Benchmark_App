#include <iostream>
#include <string>
#include "IoTRMICall.hpp"

using namespace std;

int main(int argc, char *argv[])
{
	int port = 5010;
	const char* address = "localhost";
	int rev = 0;
	bool bResult = false;

	int numRet = 3;
	string retCls[] = { "int", "string", "int" };
	int param1 = 0;
	string param2 = "";
	int param3 = 0;
	void* retObj[] = { &param1, &param2, &param3 };

	IoTRMICall *rc = new IoTRMICall(port, address, rev, &bResult);
	char retBytes[] = { 0, 0, 4, -46, 0, 0, 0, 10, 116, 101, 115, 116, 115, 116, 114, 105, 110, 103, 0, 0, 21, 56 };
	rc->getReturnObjects(retBytes, retCls, numRet, retObj);
	cout << "Param1: " << param1 << endl;
	cout << "Param2: " << param2 << endl;
	cout << "Param3: " << param3 << endl;

	return 0;
}
