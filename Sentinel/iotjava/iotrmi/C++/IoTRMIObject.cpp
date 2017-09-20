#include <iostream>
#include <string>
#include "IoTRMIObject.hpp"

using namespace std;

int main(int argc, char *argv[])
{
	int port = 5010;
	bool bResult = false;
	IoTRMIObject *ro = new IoTRMIObject(port, &bResult);

	int numRet = 3;
	string retCls[] = { "int", "string", "int" };
	int param1 = 1234;
	string param2 = "teststring";
	int param3 = 5432;
	void* retObj[] = { &param1, &param2, &param3 };
	ro->sendReturnObj(retObj, retCls, numRet);
	
	return 0;
}
