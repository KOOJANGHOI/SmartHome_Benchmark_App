#include <iostream>
#include <string>
#include "TestClass_Stub.hpp"
#include "CallBack.hpp"

using namespace std;

static exception_ptr teptr = nullptr;

int main(int argc, char *argv[])
{

	int port = 5010;
	const char* address = "localhost";
	int rev = 0;
	bool bResult = false;
	vector<int> ports;
	ports.push_back(12345);
	//ports.push_back(13234);

	TestClassInterface *tcStub = new TestClass_Stub(port, address, rev, &bResult, ports);
	cout << "Return value: " << tcStub->setAndGetA(123) << endl;
	cout << "Return value: " << tcStub->setACAndGetA("string", 123) << endl;
	vector<string> input;
	input.push_back("123");
	input.push_back("456");
	input.push_back("987");
	/*vector<int> input;
	input.push_back(123);
	input.push_back(456);
	input.push_back(987);*/

	cout << "Return value: " << tcStub->sumArray(input) << endl;
	
	CallBackInterface *cb1 = new CallBack(23);
	CallBackInterface *cb2 = new CallBack(33);
	CallBackInterface *cb3 = new CallBack(43);
	vector<CallBackInterface*> cb;
	cb.push_back(cb1);
	cb.push_back(cb2);
	cb.push_back(cb3);
	tcStub->registerCallback(cb);
	CallBackInterface *cb4 = new CallBack(53);
	CallBackInterface *cb5 = new CallBack(63);
	CallBackInterface *cb6 = new CallBack(73);
	vector<CallBackInterface*> cbsec;
	cbsec.push_back(cb4);
	cbsec.push_back(cb5);
	cbsec.push_back(cb6);
	tcStub->registerCallback(cbsec);
	cout << "Return value from callback: " << tcStub->callBack() << endl;

	vector<data> dataset;

	data testdata;
	testdata.name = "Rahmadi";
	testdata.value = 0.123;
	testdata.year = 2016;

	data testdata2;
	testdata2.name = "Trimananda";
	testdata2.value = 0.223;
	testdata2.year = 2017;

	dataset.push_back(testdata);
	dataset.push_back(testdata2);

	vector<data> result = tcStub->handleStruct(dataset);
	for (data dt : result) {
		cout << dt.name << " ";
		cout << dt.value << " ";
		cout << dt.year << endl;
	}

/*	vector<EnumC> vecEn;
	vecEn.push_back(APPLE);
	vecEn.push_back(ORANGE);
	vecEn.push_back(APPLE);
	vecEn.push_back(GRAPE);
	vector<EnumC> vecRes = tcStub->handleEnum(vecEn);
	for (EnumC en : vecRes) {
		cout << "EnumC: " << en << endl;
	}

	delete tcStub;
	delete cb1;
	delete cb2;
	delete cb3;*/

	return 0;
}
