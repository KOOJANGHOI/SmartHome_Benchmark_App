#include <iostream>
#include <string>
#include <cstring>
#include "IoTRMITypes.hpp"

using namespace std;


int main(int argc, char *argv[])
{
	std::array<int,5> myints;
	std::cout << "size of myints: " << myints.size() << std::endl;
	std::cout << "sizeof(myints): " << sizeof(myints) << std::endl;
	
	int test[5] = { 0 };
	std::memcpy(myints.data(), test, 5);

	std::vector<int> test2 (test, test + sizeof(test)/sizeof(int));

	string test3[2] = { "test1", "test2" };
	std::vector<string> test4 (test3, test3 + sizeof(test3)/sizeof(string));
	std::cout << "vector[0]: " << test4[0] << std::endl;

	std::vector<string> primJava (IoTRMITypes::primitivesJava, 
		IoTRMITypes::primitivesJava + sizeof(IoTRMITypes::primitivesJava)/sizeof(string));
	std::vector<string> primCplus (IoTRMITypes::primitivesCplus, 
		IoTRMITypes::primitivesCplus + sizeof(IoTRMITypes::primitivesCplus)/sizeof(string));

	map<string,string> mymap;
	IoTRMITypes::arraysToMap(mymap, primJava, primCplus);
	for (std::map<string,string>::iterator it=mymap.begin(); it!=mymap.end(); ++it)
		std::cout << it->first << " => " << it->second << '\n';

	std::cout << "Result of find: " << mymap.find("Boolean")->second << std::endl;

	return 0;
}
