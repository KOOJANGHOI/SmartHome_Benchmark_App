#include <iostream>
#include <string>
#include "CallBack_Stub.hpp"

using namespace std;

int main(int argc, char *argv[])
{

	int port = 5010;
	const char* address = "localhost";
	int rev = 0;
	bool bResult = false;

	CallBackInterface *cbStub = new CallBack_Stub(port, address, rev, &bResult);
	cout << "Return value: " << cbStub->printInt() << endl;
	cbStub->setInt(123);
	cout << "Return value: " << cbStub->printInt() << endl;

	delete cbStub;

	return 0;
}
