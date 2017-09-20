#include <iostream>
#include "IoTSet.hpp"
#include "IoTRelation.hpp"

using namespace std;

int main ()
{
	unordered_set<string> myset = { "red","green","blue" };

	IoTSet<string> iotset(&myset);

	unordered_set<string>::const_iterator got = iotset.find ("red");

	if ( got == iotset.end() )
		cout << "not found in myset" << endl;
	else
		cout << *got << " is in myset" << endl;

	cout << "size: " << iotset.size() << endl;

	unordered_multimap<string,string> mymap = {
		{"mom","church"},
		{"mom","college"},
		{"dad","office"},
		{"bro","school"} };

	unordered_set<string>* retset = iotset.values();
	cout << "Returned set: " << retset->size() << endl;
	retset->erase("red");
	cout << "Returned set: " << retset->size() << endl;
	cout << "Original set: " << myset.size() << endl;

	//cout << "one of the values for 'mom' is: ";
	//cout << mymap.find("mom")->second;
	//cout << endl;
	IoTRelation<string,string> iotrel(&mymap);

	std::pair<unordered_multimap<string,string>::const_iterator, 
		unordered_multimap<string,string>::const_iterator> ret;
	ret = iotrel.equal_range("mom");
	for (std::unordered_multimap<string,string>::const_iterator it=ret.first; it!=ret.second; ++it)
		cout << ' ' << it->second << endl;

	cout << "size: " << iotrel.size() << endl;

	return 0;
}
