#include <iostream>
#include <string>
#include "TestClassComplete_Stub.hpp"
#include "CallBack.hpp"

using namespace std;

int main(int argc, char *argv[])
{

	int port = 5010;
	const char* address = "localhost";
	//const char* address = "128.195.136.170";	// dc-9.calit2.uci.edu
	int rev = 0;
	bool bResult = false;
	vector<int> ports;
	ports.push_back(12345);

	TestClassComplete *tcStub = new TestClassComplete_Stub(port, address, rev, &bResult, ports);
	cout << "==== SINGLE ====" << endl;
	cout << "Return value: " << tcStub->getByte(68) << endl;
	cout << "Return value: " << tcStub->getShort(1234) << endl;
	cout << "Return value: " << tcStub->getLong(12345678) << endl;
	cout << "Return value: " << tcStub->getFloat(12.345) << endl;
	cout << "Return value: " << tcStub->getDouble(12345.678) << endl;
	cout << "Return value: " << tcStub->getBoolean(true) << endl;
	cout << "Return value: " << tcStub->getChar('c') << endl;
	cout << "==== ARRAY ====" << endl;
	vector<char> in1;
	in1.push_back(68);
	in1.push_back(69);
	cout << "Return value: " << tcStub->getByteArray(in1)[0] << ", " << tcStub->getByteArray(in1)[1] << endl;
	vector<short> in2;
	in2.push_back(1234);
	in2.push_back(1235);
	cout << "Return value: " << tcStub->getShortArray(in2)[0] << ", "  << tcStub->getShortArray(in2)[1] << endl;
	vector<int64_t> in3;
	in3.push_back(12345678);
	in3.push_back(12356782);
	cout << "Return value: " << tcStub->getLongArray(in3)[0] << ", "  << tcStub->getLongArray(in3)[1] << endl;
	vector<float> in4;
	in4.push_back(12.345);
	in4.push_back(12.346);
	cout << "Return value: " << tcStub->getFloatArray(in4)[0] << ", "  << tcStub->getFloatArray(in4)[1] << endl;
	vector<double> in5;
	in5.push_back(12345.678);
	in5.push_back(12345.543);
	cout << "Return value: " << tcStub->getDoubleArray(in5)[0] << ", "  << tcStub->getDoubleArray(in5)[1] << endl;
	vector<bool> in6;
	in6.push_back(true);
	in6.push_back(false);
	cout << "Return value: " << tcStub->getBooleanArray(in6)[0] << ", "  << tcStub->getBooleanArray(in6)[1] << endl;
	vector<char> in7;
	in7.push_back('c');
	in7.push_back('e');
	cout << "Return value: " << tcStub->getCharArray(in7)[0] << ", "  << tcStub->getCharArray(in7)[1] << endl;
	cout << "==== VECTOR/LIST ====" << endl;
	vector<char> inl1;
	inl1.push_back(68);
	inl1.push_back(69);
	cout << "Return value: " << tcStub->getByteList(inl1)[0] << ", " << tcStub->getByteList(inl1)[1] << endl;
	vector<short> inl2;
	inl2.push_back(1234);
	inl2.push_back(1235);
	cout << "Return value: " << tcStub->getShortList(inl2)[0] << ", "  << tcStub->getShortList(inl2)[1] << endl;
	vector<int64_t> inl3;
	inl3.push_back(12345678);
	inl3.push_back(12356782);
	cout << "Return value: " << tcStub->getLongList(inl3)[0] << ", "  << tcStub->getLongList(inl3)[1] << endl;
	vector<float> inl4;
	inl4.push_back(12.345);
	inl4.push_back(12.346);
	cout << "Return value: " << tcStub->getFloatList(inl4)[0] << ", "  << tcStub->getFloatList(inl4)[1] << endl;
	vector<double> inl5;
	inl5.push_back(12345.678);
	inl5.push_back(12345.543);
	cout << "Return value: " << tcStub->getDoubleList(inl5)[0] << ", "  << tcStub->getDoubleList(inl5)[1] << endl;
	vector<bool> inl6;
	inl6.push_back(true);
	inl6.push_back(false);
	cout << "Return value: " << tcStub->getBooleanList(inl6)[0] << ", "  << tcStub->getBooleanList(inl6)[1] << endl;
	vector<char> inl7;
	inl7.push_back('c');
	inl7.push_back('e');
	cout << "Return value: " << tcStub->getCharList(inl7)[0] << ", "  << tcStub->getCharList(inl7)[1] << endl;
	cout << "==== ENUM ====" << endl;
	Enum en;
	en = APPLE;
	Enum res = tcStub->handleEnum(en);
	cout << "Return value: " << res << endl;
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
	cout << "==== STRUCT ====" << endl;
	Struct str;
	str.name = "Rahmadi";
	str.value = 0.123;
	str.year = 2016;
	Struct resStr = tcStub->handleStruct(str);
	cout << "Name: " << resStr.name << endl;
	cout << "Value:" << resStr.value << endl;
	cout << "Year" << resStr.year << endl;
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
		cout << "Value:" << st.value << endl;
		cout << "Year" << st.year << endl;
	}
	vector<Struct> vecRetStr2 = tcStub->handleStructList(vecStr);
	for (Struct st : vecRetStr2) {
		cout << "Name: " << st.name << endl;
		cout << "Value:" << st.value << endl;
		cout << "Year" << st.year << endl;
	}
	cout << "==== CALLBACK ====" << endl;
	CallBackInterface *cbSingle = new CallBack(2354);
	tcStub->registerCallback(cbSingle);
	cout << "Return value from callback: " << tcStub->callBack() << endl;
	CallBackInterface *cb1 = new CallBack(23);
	CallBackInterface *cb2 = new CallBack(33);
	CallBackInterface *cb3 = new CallBack(43);
	vector<CallBackInterface*> cb;
	cb.push_back(cb1);
	cb.push_back(cb2);
	cb.push_back(cb3);
	tcStub->registerCallbackArray(cb);
	cout << "Return value from callback: " << tcStub->callBack() << endl;
	CallBackInterface *cb4 = new CallBack(53);
	CallBackInterface *cb5 = new CallBack(63);
	CallBackInterface *cb6 = new CallBack(73);
	vector<CallBackInterface*> cblist;
	cblist.push_back(cb4);
	cblist.push_back(cb5);
	cblist.push_back(cb6);
	tcStub->registerCallbackList(cblist);
	cout << "Return value from callback: " << tcStub->callBack() << endl;

	cout << "==== OTHERS ====" << endl;
	cout << "Return value: " << tcStub->getA() << endl;
	cout << "Return value: " << tcStub->setAndGetA(123) << endl;
	cout << "Return value: " << tcStub->setACAndGetA("string", 123) << endl;
	vector<string> input;
	input.push_back("123");
	input.push_back("456");
	input.push_back("987");

	cout << "Return value: " << tcStub->sumArray(input) << endl;
	
	return 0;
}
