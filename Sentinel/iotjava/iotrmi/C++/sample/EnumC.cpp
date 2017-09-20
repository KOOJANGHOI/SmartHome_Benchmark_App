#include <iostream>
#include <vector>
#include "EnumC.hpp"

int main(int argc, char *argv[]) {

	// Enum to integer
	EnumC en = APPLE;
	int enum1 = (int) en;
	cout << "Enum 1: " << enum1 << endl;
	EnumC en2 = ORANGE;
	int enum2 = (int) en2;
	cout << "Enum 2: " << enum2 << endl;
	EnumC en3 = GRAPE;
	int enum3 = (int) en3;
	cout << "Enum 3: " << enum3 << endl;

	// Integer to enum
	cout << "Enum 1: " << (EnumC) enum1 << endl;
	cout << "Enum 2: " << (EnumC) enum2 << endl;
	cout << "Enum 3: " << (EnumC) enum3 << endl;

	return 0;
}
