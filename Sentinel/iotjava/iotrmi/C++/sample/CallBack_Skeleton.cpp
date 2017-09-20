#include <iostream>
#include <string>
#include "CallBack_Skeleton.hpp"

using namespace std;

int main(int argc, char *argv[])
{

	int port = 5010;
	CallBackInterface *cb = new CallBack(23);
	CallBack_Skeleton *cbSkel = new CallBack_Skeleton(cb, port);
	//cbSkel->waitRequestInvokeMethod();

	delete cb;
	delete cbSkel;
	return 0;
}
