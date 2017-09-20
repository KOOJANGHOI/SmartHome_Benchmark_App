#include <iostream>
#include <string>
#include "TestClassComplete_Stub.hpp"
#include "CallBack.hpp"

using namespace std;

int main(int argc, char *argv[])
{

	int port = 5010;
	const char* address = "localhost";
	//const char* address = "192.168.2.191";	// RPi2
	//const char* skeletonAddress = "128.195.136.170";	// dc-9.calit2.uci.edu
	const char* skeletonAddress = "128.195.204.132";
	const char* callbackAddress = "128.195.204.132";	// dw-2.eecs.uci.edu (this machine)
	//const char* skeletonAddress = "192.168.2.108";	// RPi1
	//const char* callbackAddress = "192.168.2.191";	// RPi2
	int rev = 0;
	bool bResult = false;
	vector<int> ports;
	ports.push_back(12345);

	TestClassComplete *tcStub = new TestClassComplete_Stub(port, skeletonAddress, callbackAddress, rev, &bResult, ports);
	/*cout << "==== ENUM ====" << endl;
	Enum en;
	en = APPLE;
	Enum res = tcStub->handleEnum(en);
	cout << "Return value: " << res << endl;
	Enum resComp = tcStub->handleEnumComplex(en, 23, 'c');
	cout << "Return value: " << resComp << endl;
	vector<Enum> vecEn;
	vecEn.push_back(APPLE);
	vecEn.push_back(ORANGE);
	vecEn.push_back(APPLE);
	vecEn.push_back(GRAPE);
	vector<Enum> vecRes = tcStub->handleEnumArray(vecEn);
	for (Enum en : vecRes) {
		cout << "Enum value: " << en << endl;
	}
	vector<Enum> vecRes2 = tcStub->handleEnumList(vecEn);
	for (Enum en : vecRes2) {
		cout << "Enum value: " << en << endl;
	}
	vector<Enum> vecRes3 = tcStub->handleEnumComplex2(vecEn, 23, 'c');
	for (Enum en : vecRes3) {
		cout << "Enum value: " << en << endl;
	}


	cout << "==== STRUCT ====" << endl;
	Struct str;
	str.name = "Rahmadi";
	str.value = 0.123;
	str.year = 2016;
	Struct resStr = tcStub->handleStruct(str);
	cout << "Name: " << resStr.name << endl;
	cout << "Value:" << resStr.value << endl;
	cout << "Year" << resStr.year << endl;
	Struct resStr2 = tcStub->handleStructComplex(23, 'c', str);
	cout << "Name: " << resStr2.name << endl;
	cout << "Value:" << resStr2.value << endl;
	cout << "Year" << resStr2.year << endl;
	Struct str2;
	str2.name = "Trimananda";
	str2.value = 0.124;
	str2.year = 2017;
	vector<Struct> vecStr;
	vecStr.push_back(str);
	vecStr.push_back(str2);
	vector<Struct> vecRetStr = tcStub->handleStructArray(vecStr);
	for (Struct st : vecRetStr) {
		cout << "Name: " << st.name << endl;
		cout << "Value: " << st.value << endl;
		cout << "Year: " << st.year << endl;
	}
	vector<Struct> vecRetStr2 = tcStub->handleStructList(vecStr);
	for (Struct st : vecRetStr2) {
		cout << "Name: " << st.name << endl;
		cout << "Value: " << st.value << endl;
		cout << "Year: " << st.year << endl;
	}
	vector<Struct> vecRetStr3 = tcStub->handleStructComplex2(23, 'c', vecStr);
	for (Struct st : vecRetStr3) {
		cout << "Name: " << st.name << endl;
		cout << "Value:" << st.value << endl;
		cout << "Year" << st.year << endl;
	}
	vector<Enum> vecRes4 = tcStub->handleEnumStruct(vecEn, vecStr, 'c');
	for (Enum en : vecRes4) {
		cout << "Enum value: " << en << endl;
	}*/

	cout << "==== CALLBACK ====" << endl;
	CallBackInterface *cbSingle = new CallBack(2354);
	tcStub->registerCallback(cbSingle);
	cout << "Return value from callback: " << tcStub->callBack() << endl;
	/*CallBackInterface *cb1 = new CallBack(23);
	CallBackInterface *cb2 = new CallBack(33);
	CallBackInterface *cb3 = new CallBack(43);
	vector<CallBackInterface*> cb;
	cb.push_back(cb1);
	cb.push_back(cb2);
	cb.push_back(cb3);
	tcStub->registerCallbackArray(cb);
	cout << "Return value from callback: " << tcStub->callBack() << endl;*/
	/*CallBackInterface *cb4 = new CallBack(53);
	CallBackInterface *cb5 = new CallBack(63);
	CallBackInterface *cb6 = new CallBack(73);
	vector<CallBackInterface*> cblist;
	cblist.push_back(cb4);
	cblist.push_back(cb5);
	cblist.push_back(cb6);*/
//	tcStub->registerCallbackList(cblist);
//	cout << "Return value from callback: " << tcStub->callBack() << endl;
/*	tcStub->registerCallbackComplex(23, cblist, 0.1234);
	cout << "Return value from callback: " << tcStub->callBack() << endl;
	vector<Enum> vecRes5 = tcStub->handleAll(vecEn, vecStr, 'c', cblist);
	for (Enum en : vecRes5) {
		cout << "Enum value: " << en << endl;
	}

	vector<Enum> vecRes6 = tcStub->handleCallbackEnum(vecEn, 'c', cblist);
	for (Enum en : vecRes6) {
		cout << "Enum value: " << en << endl;
	}*/
	//vector<Enum> vecRes7 = tcStub->handleAllTwo(vecEn, vecStr, vecStr, vecEn, 'c', cblist, cblist);
	/*vector<Enum> vecRes7 = tcStub->handleAllTwo(vecEn, vecEn, vecStr, vecStr, 'c', cblist, cblist);
	for (Enum en : vecRes7) {
		cout << "Enum value: " << en << endl;
	}
	vector<Enum> vecRes8 = tcStub->handleEnumTwo(vecEn, vecEn);
	for (Enum en : vecRes8) {
		cout << "Enum value: " << en << endl;
	}
	vector<Enum> vecRes9 = tcStub->handleEnumThree(vecEn, vecEn, vecStr, vecStr);
	for (Enum en : vecRes9) {
		cout << "Enum value: " << en << endl;
	}
	vector<Struct> vecRetStr2 = tcStub->handleStructTwo(vecStr, vecStr);
	for (Struct st : vecRetStr2) {
		cout << "Name: " << st.name << endl;
		cout << "Value: " << st.value << endl;
		cout << "Year: " << st.year << endl;
	}
	vector<Struct> vecRetStr3 = tcStub->handleStructThree(vecStr, vecStr, vecStr);
	for (Struct st : vecRetStr3) {
		cout << "Name: " << st.name << endl;
		cout << "Value: " << st.value << endl;
		cout << "Year: " << st.year << endl;
	}*/

	return 0;
}
